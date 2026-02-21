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
 * Scrapes SBI career notifications from sbi.co.in
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SbiScraper implements JobNoticeSource {

    private static final String BASE_URL = "https://bank.sbi";
    private static final String URL = "https://bank.sbi/web/careers/current-openings";
    private final ScraperUtils utils;

    @Override
    public String getSourceName() {
        return "State Bank of India (SBI)";
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
            Document doc = utils.fetchPage(URL);
            Elements links = doc
                    .select("table a, .portlet-body a, .career-item a, a[href*='recruit'], a[href*='career']");

            for (Element link : links) {
                String title = utils.cleanTitle(link.text());
                if (title.length() < 10)
                    continue;
                if (!isJobLink(title))
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
                        .lastDate(utils.parseDate(extractDate(parentText, 1)))
                        .build());

                if (notices.size() >= 20)
                    break;
            }
            log.info("[SBI] Fetched {} notices", notices.size());
        } catch (Exception e) {
            log.error("[SBI] Failed to scrape: {}", e.getMessage());
        }
        return notices;
    }

    private boolean isJobLink(String title) {
        String t = title.toLowerCase();
        return t.contains("recruit") || t.contains("officer") || t.contains("clerk")
                || t.contains("specialist") || t.contains("appointment") || t.contains("vacancy")
                || t.contains("post") || t.contains("notification");
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
