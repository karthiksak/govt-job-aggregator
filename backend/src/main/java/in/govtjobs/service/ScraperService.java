package in.govtjobs.service;

import in.govtjobs.dto.RawNotice;
import in.govtjobs.model.JobNotice;
import in.govtjobs.repository.JobNoticeRepository;
import in.govtjobs.scraper.JobNoticeSource;
import in.govtjobs.util.ScraperUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScraperService {

    private final List<JobNoticeSource> sources;
    private final JobNoticeRepository repository;
    private final ScraperUtils utils;

    /**
     * Guard against concurrent runs. H2 in file mode cannot handle two
     * simultaneous writers — if both the startup scheduler and a manual
     * /refresh API call fire at the same time we get table-lock timeouts.
     */
    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * Run all scrapers. Called by scheduler and admin API.
     * NOT @Transactional at this level — each notice is saved in its own
     * short transaction via {@link #saveNotice} so that one bad insert
     * does not roll back everything that came before it.
     */
    public ScraperResult runAll() {
        if (!running.compareAndSet(false, true)) {
            log.warn("Scrape run already in progress — skipping duplicate invocation");
            return new ScraperResult(0, 0, 0, 0);
        }

        try {
            return doRunAll();
        } finally {
            running.set(false);
        }
    }

    private ScraperResult doRunAll() {
        log.info("=== Starting scrape run at {} ===", LocalDateTime.now());
        AtomicInteger total = new AtomicInteger(0);
        AtomicInteger saved = new AtomicInteger(0);
        AtomicInteger skipped = new AtomicInteger(0);
        AtomicInteger errors = new AtomicInteger(0);

        for (JobNoticeSource source : sources) {
            try {
                log.info("Scraping: {}", source.getSourceName());
                List<RawNotice> raw = source.fetchRaw();
                total.addAndGet(raw.size());

                for (RawNotice notice : raw) {
                    try {
                        processNotice(notice, saved, skipped);
                    } catch (Exception e) {
                        log.warn("Error processing notice '{}': {}", notice.getTitle(), e.getMessage());
                        errors.incrementAndGet();
                    }
                }

                // Polite delay between sources (1 second)
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Source '{}' failed: {}", source.getSourceName(), e.getMessage());
                errors.incrementAndGet();
            }
        }

        ScraperResult result = new ScraperResult(total.get(), saved.get(), skipped.get(), errors.get());
        log.info("=== Scrape complete: {} total, {} saved, {} skipped, {} errors ===",
                result.total(), result.saved(), result.skipped(), result.errors());
        return result;
    }

    /**
     * Each notice is saved in its own independent transaction so that a
     * duplicate-key constraint violation only rolls back that single insert.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processNotice(RawNotice raw, AtomicInteger saved, AtomicInteger skipped) {
        if (raw.getTitle() == null || raw.getTitle().isBlank())
            return;

        String hash = utils.hash(raw.getTitle(), raw.getSourceName());

        if (repository.existsByContentHash(hash)) {
            skipped.incrementAndGet();
            return;
        }

        String category = normalizeCategory(raw.getCategory());
        String state = normalizeState(raw.getState());

        JobNotice notice = JobNotice.builder()
                .title(utils.cleanTitle(raw.getTitle()))
                .category(category)
                .state(state)
                .sourceName(raw.getSourceName())
                .sourceUrl(raw.getSourceUrl())
                .applyUrl(raw.getApplyUrl())
                .publishedDate(raw.getPublishedDate())
                .lastDate(raw.getLastDate())
                .contentHash(hash)
                .fetchedAt(LocalDateTime.now())
                .build();

        repository.save(notice);
        saved.incrementAndGet();
    }

    private String normalizeCategory(String raw) {
        if (raw == null)
            return "OTHERS";
        return switch (raw.toUpperCase().trim()) {
            case "BANK", "BANKING" -> "BANK";
            case "SSC" -> "SSC";
            case "RRB", "RAILWAYS", "RAILWAY" -> "RAILWAYS";
            case "UPSC" -> "UPSC";
            case "PSU" -> "PSU";
            case "STATE", "STATE GOVT" -> "STATE";
            case "DEFENCE", "DEFENSE" -> "DEFENCE";
            case "MEDICAL", "HEALTH" -> "MEDICAL";
            default -> "OTHERS";
        };
    }

    private String normalizeState(String raw) {
        if (raw == null || raw.isBlank())
            return "Central";
        return switch (raw.trim()) {
            case "central", "Central", "CENTRAL" -> "Central";
            case "Tamil Nadu", "TN", "TNPSC" -> "Tamil Nadu";
            case "Maharashtra", "MH" -> "Maharashtra";
            case "Karnataka", "KA" -> "Karnataka";
            case "Kerala", "KL" -> "Kerala";
            case "Delhi", "DL", "NCT" -> "Delhi";
            case "Gujarat", "GJ" -> "Gujarat";
            case "Rajasthan", "RJ" -> "Rajasthan";
            case "Uttar Pradesh", "UP" -> "Uttar Pradesh";
            default -> raw.trim();
        };
    }

    public record ScraperResult(int total, int saved, int skipped, int errors) {
    }
}
