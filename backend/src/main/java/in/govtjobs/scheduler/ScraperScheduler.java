package in.govtjobs.scheduler;

import in.govtjobs.service.ScraperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScraperScheduler {

    private final ScraperService scraperService;

    /**
     * Run scraper every 6 hours: midnight, 6am, noon, 6pm IST
     */
    @Scheduled(cron = "0 0 0,6,12,18 * * *", zone = "Asia/Kolkata")
    public void scheduledScrape() {
        log.info("⏰ Scheduled scrape triggered");
        scraperService.runAll();
    }

    /**
     * Run once at startup to populate data immediately
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        log.info("🚀 Application ready - running initial scrape...");
        new Thread(() -> {
            try {
                Thread.sleep(3000); // Wait 3s for server to fully start
                scraperService.runAll();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "initial-scrape").start();
    }
}
