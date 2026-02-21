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
                String title = utils.buildTitle(link);
                if (title.length() < 10 || utils.isJunkTitle(title))
                    continue;
                if (!isJobLink(title))
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
                        .publishedDate(utils.parseDate(utils.extractDateFromAncestor(link, 0)))
                        .lastDate(utils.parseDate(utils.extractDateFromAncestor(link, 1)))
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
}
