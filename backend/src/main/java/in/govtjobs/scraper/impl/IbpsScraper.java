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
 * Scrapes IBPS recruitment notices from ibps.in
 * Correct URL changed to /category/recruitment/ (the
 * /recruitment-notifications/ path is 404)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IbpsScraper implements JobNoticeSource {

    private static final String BASE_URL = "https://www.ibps.in";
    private static final String URL = "https://www.ibps.in/category/recruitment/";
    private final ScraperUtils utils;

    @Override
    public String getSourceName() {
        return "IBPS (Institute of Banking Personnel Selection)";
    }

    @Override
    public String getSourceUrl() {
        return BASE_URL;
    }

    @Override
    public String getCategory() {
        return "BANK";
    }

    @Override
    public String getState() {
        return "Central";
    }

    @Override
    public List<RawNotice> fetchRaw() {
        List<RawNotice> notices = new ArrayList<>();
        try {
            Document doc = utils.fetchPageLax(URL);
            // IBPS WordPress site — posts are h2 or h3 article titles
            Elements items = doc.select("article a, h2 a, h3 a, .entry-title a, .post-title a");
            if (items.isEmpty()) {
                // Fallback: grab all anchors with recruitment in href or text
                items = doc.select("a[href*='ibps'], a[href*='recruit'], a[href*='notification']");
            }

            for (Element link : items) {
                String title = utils.buildTitle(link);
                if (title.length() < 10 || utils.isJunkTitle(title))
                    continue;
                if (!isJobRelated(title))
                    continue;

                String href = utils.absoluteUrl(BASE_URL, link.attr("href"));

                notices.add(RawNotice.builder()
                        .title(title)
                        .applyUrl(href)
                        .sourceName(getSourceName())
                        .sourceUrl(getSourceUrl())
                        .category(getCategory())
                        .state(getState())
                        .publishedDate(utils.parseDate(utils.extractDateFromAncestor(link, 0)))
                        .lastDate(utils.parseDate(utils.extractDateFromAncestor(link, 1)))
                        .build());

                if (notices.size() >= 25)
                    break;
            }
            log.info("[IBPS] Fetched {} notices", notices.size());
        } catch (Exception e) {
            log.error("[IBPS] Failed to scrape: {}", e.getMessage());
        }
        return notices;
    }

    private boolean isJobRelated(String title) {
        String t = title.toLowerCase();
        return t.contains("recruit") || t.contains("notification") || t.contains("vacancy")
                || t.contains("ibps") || t.contains("clerk") || t.contains("po ")
                || t.contains("officer") || t.contains("specialist") || t.contains("so ")
                || t.contains("rrb") || t.contains("crp") || t.contains("advt")
                || t.contains("advertisement") || t.contains("apply") || t.contains("result")
                || t.contains("admit") || t.contains("score card") || t.contains("selection")
                || t.contains("interview") || t.contains("exam") || t.contains("mains")
                || t.contains("prelim");
    }

    // Local extractDate removed — now using utils.extractDateFromAncestor()
}
