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
 * Scrapes PSU job notifications.
 * Targets ONGC (more accessible than NHPC) for PSU central government jobs.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PsuScraper implements JobNoticeSource {

    private static final String BASE_URL = "https://www.ongcindia.com";
    private static final String URL = "https://www.ongcindia.com/wps/wcm/connect/en/career/";
    private final ScraperUtils utils;

    @Override
    public String getSourceName() {
        return "PSU Jobs (ONGC & Central PSUs)";
    }

    @Override
    public String getSourceUrl() {
        return BASE_URL;
    }

    @Override
    public String getCategory() {
        return "PSU";
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
            Elements links = doc.select(
                    "a[href*='recruit'], a[href*='career'], a[href*='notification'], " +
                            "a[href*='vacancy'], a[href*='pdf'], table tr a, ul li a, .ibm-columns a");

            for (Element link : links) {
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

                if (notices.size() >= 20)
                    break;
            }
            log.info("[PSU/ONGC] Fetched {} notices", notices.size());
        } catch (Exception e) {
            log.error("[PSU/ONGC] Failed to scrape: {}", e.getMessage());
        }
        return notices;
    }

    private boolean isJobRelated(String title) {
        String t = title.toLowerCase();
        return t.contains("recruit") || t.contains("vacancy") || t.contains("notification")
                || t.contains("career") || t.contains("job") || t.contains("post")
                || t.contains("engineer") || t.contains("officer") || t.contains("apprentice");
    }

}
