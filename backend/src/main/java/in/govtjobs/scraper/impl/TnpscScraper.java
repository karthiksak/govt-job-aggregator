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
    // Root page has job notifications via anchor links
    private static final String URL = "https://www.tnpsc.gov.in";
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
            Document doc = utils.fetchPageLax(URL);
            // Try all links with relevant href or text
            Elements links = doc.select(
                    "a[href*='notification'], a[href*='Notification'], a[href*='recruit'], " +
                            "a[href*='Recruit'], a[href*='pdf'], a[href*='vacancy'], a[href*='group']");

            if (links.isEmpty()) {
                // Fallback: table rows
                links = doc.select("table tr a, ul li a");
            }

            for (Element link : links) {
                String title = utils.cleanTitle(link.text());
                if (title.length() < 10)
                    continue;
                if (!isRelevantTitle(title))
                    continue;

                String href = utils.absoluteUrl(BASE_URL, link.attr("href"));
                String parentText = link.parent() != null ? link.parent().text() : "";

                notices.add(RawNotice.builder()
                        .title(title)
                        .applyUrl(href)
                        .sourceName(getSourceName())
                        .sourceUrl(getSourceUrl())
                        .category(getCategory())
                        .state(getState())
                        .publishedDate(utils.parseDate(extractDate(parentText, 0)))
                        .lastDate(utils.parseDate(extractDate(parentText, 0)))
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

    private String extractDate(String text, int index) {
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("\\d{2}[-./ ]\\d{2}[-./ ]\\d{4}").matcher(text);
        int i = 0;
        while (m.find()) {
            if (i == index)
                return m.group();
            i++;
        }
        return null;
    }
}
