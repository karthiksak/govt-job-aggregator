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
 * Scrapes UPSC recruitment notices.
 * Correct base URL: upsc.gov.in (NOT www.upsc.gov.in — different cert)
 * Better page: /recruitment/recruitment-advertisement for recruitment ads.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UpscScraper implements JobNoticeSource {

    private static final String BASE_URL = "https://upsc.gov.in";
    private static final String URL = "https://upsc.gov.in/recruitment/recruitment-advertisement";
    private final ScraperUtils utils;

    @Override
    public String getSourceName() {
        return "UPSC (Union Public Service Commission)";
    }

    @Override
    public String getSourceUrl() {
        return BASE_URL;
    }

    @Override
    public String getCategory() {
        return "UPSC";
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
            // Try targeting any anchor with text about recruitment/exam
            Elements links = doc.select("a");

            for (Element link : links) {
                String title = utils.buildTitle(link);
                if (title.length() < 10 || utils.isJunkTitle(title))
                    continue;
                if (!isRelevant(title))
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

                if (notices.size() >= 25)
                    break;
            }
            log.info("[UPSC] Fetched {} notices", notices.size());
        } catch (Exception e) {
            log.error("[UPSC] Failed to scrape: {}", e.getMessage());
        }
        return notices;
    }

    private boolean isRelevant(String title) {
        String t = title.toLowerCase();
        return t.contains("recruit") || t.contains("vacancy") || t.contains("adverti")
                || t.contains("exam") || t.contains("post") || t.contains("selection")
                || t.contains("notification") || t.contains("civil service") || t.contains("upsc");
    }

}
