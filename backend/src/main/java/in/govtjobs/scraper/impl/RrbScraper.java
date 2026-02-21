package in.govtjobs.scraper.impl;

import in.govtjobs.dto.RawNotice;
import in.govtjobs.scraper.JobNoticeSource;
import in.govtjobs.util.ScraperUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Scrapes Railway Recruitment Board notifications.
 *
 * Strategy: rrbcdg.gov.in consistently times out from outside India.
 * Instead we scrape multiple reliable railway job portals:
 * 1. RRC NR (Railway Recruitment Cell, Northern Railway) — static HTML
 * 2. RRB Chandigarh — rrbbbs.gov.in (static HTML)
 * 3. Indian Railways official recruitment page
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RrbScraper implements JobNoticeSource {

    // RRC NR — Northern Railway, Delhi — reliable static page
    private static final String RRC_NR_URL = "https://www.rrcnr.org/recr.aspx";
    private static final String RRC_NR_BASE = "https://www.rrcnr.org";

    // RRB Bhopal — static HTML recruitment notifications
    private static final String RRB_BPL_URL = "https://rrbbhopal.gov.in/";
    private static final String RRB_BPL_BASE = "https://rrbbhopal.gov.in";

    // RRB Mumbai
    private static final String RRB_MUM_URL = "https://www.rrbmumbai.gov.in/";
    private static final String RRB_MUM_BASE = "https://www.rrbmumbai.gov.in";

    private static final String BASE_URL = "https://indianrailways.gov.in";

    private final ScraperUtils utils;

    @Override
    public String getSourceName() {
        return "Railway Recruitment Board (RRB)";
    }

    @Override
    public String getSourceUrl() {
        return BASE_URL;
    }

    @Override
    public String getCategory() {
        return "RAILWAYS";
    }

    @Override
    public String getState() {
        return "Central";
    }

    @Override
    public List<RawNotice> fetchRaw() {
        List<RawNotice> notices = new ArrayList<>();

        // Try RRC NR first
        notices.addAll(scrapeSource(RRC_NR_URL, RRC_NR_BASE, "RRC NR (Northern Railway)"));

        // Supplement from RRB Bhopal if needed
        if (notices.size() < 8)
            notices.addAll(scrapeSource(RRB_BPL_URL, RRB_BPL_BASE, "RRB Bhopal"));

        // Supplement from RRB Mumbai if still sparse
        if (notices.size() < 8)
            notices.addAll(scrapeSource(RRB_MUM_URL, RRB_MUM_BASE, "RRB Mumbai"));

        log.info("[RRB] Total fetched {} notices", notices.size());
        return notices;
    }

    private List<RawNotice> scrapeSource(String url, String base, String label) {
        List<RawNotice> list = new ArrayList<>();
        try {
            Document doc = utils.fetchPageLax(url, 20000);
            Elements links = doc.select(
                    "a[href*='pdf'], a[href*='PDF'], a[href*='notification'], a[href*='Notification'], " +
                            "a[href*='Recruitment'], a[href*='advt'], a[href*='Advt'], table tr td a, ul li a");
            if (links.isEmpty())
                links = doc.select("a[href]");

            for (Element link : links) {
                String title = utils.buildTitle(link);
                if (title.length() < 10 || !isJobRelated(title) || utils.isJunkTitle(title))
                    continue;

                String href = utils.absoluteUrl(base, link.attr("href"));

                list.add(RawNotice.builder()
                        .title(utils.normalizeTitleForDisplay(title))
                        .applyUrl(href)
                        .sourceName(getSourceName())
                        .sourceUrl(getSourceUrl())
                        .category(getCategory())
                        .state(getState())
                        .noticeType(utils.categorizeNoticeType(title))
                        .engineeringBranches(utils.inferEngineeringBranches(title))
                        .publishedDate(utils.parseDate(utils.extractDateFromAncestor(link, 0)))
                        .lastDate(utils.parseDate(utils.extractDateFromAncestor(link, 1)))
                        .build());

                if (list.size() >= 15)
                    break;
            }
            log.info("[RRB/{}] Fetched {} notices", label, list.size());
        } catch (Exception e) {
            log.warn("[RRB/{}] Failed: {}", label, e.getMessage());
        }
        return list;
    }

    private boolean isJobRelated(String title) {
        String t = title.toLowerCase();
        return t.contains("recruitment") || t.contains("vacancy") || t.contains("notification")
                || t.contains("ntpc") || t.contains("group d") || t.contains("group-d")
                || t.contains("alp") || t.contains("technician") || t.contains("je ")
                || t.contains("junior engineer") || t.contains("rrb") || t.contains("rrc")
                || t.contains("railway") || t.contains("result") || t.contains("admit")
                || t.contains("selection") || t.contains("apprent") || t.contains("loco pilot")
                || t.contains("paramedical") || t.contains("ministerial");
    }
}
