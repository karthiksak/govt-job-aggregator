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
 * Scrapes TNPSC recruitment notifications from tnpsc.gov.in
 * The correct URL uses a redirect — try root page which has notice links.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TnpscScraper implements JobNoticeSource {

    private static final String BASE_URL = "https://www.tnpsc.gov.in";
    // home.aspx loads the "What's New" announcements list with dated notices.
    // Root URL (/) just redirects and selectors don't match; home.aspx is the
    // correct entry point.
    private static final String URL = "https://www.tnpsc.gov.in/home.aspx";
    private final ScraperUtils utils;

    @Override
    public String getSourceName() {
        return "TNPSC (Tamil Nadu Public Service Commission)";
    }

    @Override
    public String getSourceUrl() {
        return BASE_URL;
    }

    @Override
    public String getCategory() {
        return "STATE";
    }

    @Override
    public String getState() {
        return "Tamil Nadu";
    }

    @Override
    public List<RawNotice> fetchRaw() {
        List<RawNotice> notices = new ArrayList<>();
        try {
            Document doc = utils.fetchPageLax(URL, 15000);
            // TNPSC home.aspx has a "Whats new" section as a <ul> list with date + title
            // links.
            // Broad selector: capture all list and table-row links, then filter by title
            // relevance.
            Elements links = doc.select("ul li a, table tr td a, a[href*='pdf'], a[href*='aspx']");

            if (links.isEmpty()) {
                // Fallback: any anchor
                links = doc.select("a[href]");
            }

            for (Element link : links) {
                String title = utils.buildTitle(link);
                if (title.length() < 10 || utils.isJunkTitle(title))
                    continue;
                if (!isRelevantTitle(title))
                    continue;

                String href = utils.absoluteUrl(BASE_URL, link.attr("href"));

                notices.add(RawNotice.builder()
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

                if (notices.size() >= 30)
                    break;
            }
            log.info("[TNPSC] Fetched {} notices", notices.size());
        } catch (Exception e) {
            log.error("[TNPSC] Failed to scrape: {}", e.getMessage());
        }
        return notices;
    }

    private boolean isRelevantTitle(String title) {
        String t = title.toLowerCase();
        return t.contains("recruit") || t.contains("notification") || t.contains("post")
                || t.contains("exam") || t.contains("vacancy") || t.contains("selection")
                || t.contains("group") || t.contains("combined") || t.contains("tnpsc");
    }
}
