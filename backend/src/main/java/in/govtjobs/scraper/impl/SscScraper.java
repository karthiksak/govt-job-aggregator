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
 * Scrapes SSC (Staff Selection Commission) recruitment notices.
 *
 * Strategy: ssc.gov.in main portal is an Angular SPA — no static HTML.
 * We scrape multiple reliable alternative sources:
 * 1. SSC NR (Northern Region) — static HTML site
 * 2. SSC CR (Central Region), Allahabad — static HTML
 * 3. SSC Official notice page (sometimes cached/static)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SscScraper implements JobNoticeSource {

    private static final String BASE_URL = "https://ssc.gov.in";

    // SSC NR (Northern Region) — static HTML recruitment page
    private static final String SSC_NR_URL = "https://sscnr.nic.in/newpages/latest.php";
    private static final String SSC_NR_BASE = "https://sscnr.nic.in";

    // SSC Patna (ER2)
    private static final String SSC_ER2_URL = "https://sscner.nic.in/newpages/latest.php";
    private static final String SSC_ER2_BASE = "https://sscner.nic.in";

    private final ScraperUtils utils;

    @Override
    public String getSourceName() {
        return "Staff Selection Commission (SSC)";
    }

    @Override
    public String getSourceUrl() {
        return BASE_URL;
    }

    @Override
    public String getCategory() {
        return "SSC";
    }

    @Override
    public String getState() {
        return "Central";
    }

    @Override
    public List<RawNotice> fetchRaw() {
        List<RawNotice> notices = new ArrayList<>();
        notices.addAll(scrapeSource(SSC_NR_URL, SSC_NR_BASE, "SSC NR (Northern Region)"));
        if (notices.size() < 5)
            notices.addAll(scrapeSource(SSC_ER2_URL, SSC_ER2_BASE, "SSC NER (North Eastern Region)"));
        log.info("[SSC] Total fetched {} notices", notices.size());
        return notices;
    }

    private List<RawNotice> scrapeSource(String url, String base, String label) {
        List<RawNotice> list = new ArrayList<>();
        try {
            Document doc = utils.fetchPageLax(url, 15000);
            // Static NIC pages use simple tables and <li> lists
            Elements links = doc.select("table tr td a, ul li a, .content a, p a, div a");
            if (links.isEmpty())
                links = doc.select("a[href]");

            for (Element link : links) {
                String title = utils.buildTitle(link);
                if (title.length() < 10 || !isRelevantNotice(title) || utils.isJunkTitle(title))
                    continue;

                String href = utils.absoluteUrl(base, link.attr("href"));

                list.add(RawNotice.builder()
                        .title(title)
                        .applyUrl(href)
                        .sourceName(getSourceName())
                        .sourceUrl(getSourceUrl())
                        .category(getCategory())
                        .state(getState())
                        .publishedDate(utils.parseDate(utils.extractDateFromAncestor(link, 0)))
                        .lastDate(utils.parseDate(utils.extractDateFromAncestor(link, 1)))
                        .build());

                if (list.size() >= 20)
                    break;
            }
            log.info("[SSC/{}] Fetched {} notices", label, list.size());
        } catch (Exception e) {
            log.warn("[SSC/{}] Failed: {}", label, e.getMessage());
        }
        return list;
    }

    private boolean isRelevantNotice(String title) {
        String t = title.toLowerCase();
        return t.contains("recruitment") || t.contains("vacancy") || t.contains("notification")
                || t.contains("adverti") || t.contains("selection") || t.contains("examination")
                || t.contains("result") || t.contains("admit card") || t.contains("cgl")
                || t.contains("chsl") || t.contains("gd") || t.contains("cpo")
                || t.contains("steno") || t.contains("mts") || t.contains("phase")
                || t.contains("call letter") || t.contains("cut off") || t.contains("merit list");
    }
}
