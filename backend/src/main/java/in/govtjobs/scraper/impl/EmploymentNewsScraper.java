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
 * Scrapes Employment News from employmentnews.gov.in HTML page
 * (RSS feed at /RSS/enrss.aspx is unreliable — connection reset frequently)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmploymentNewsScraper implements JobNoticeSource {

    private static final String BASE_URL = "https://employmentnews.gov.in";
    private static final String URL = "https://employmentnews.gov.in/NewVer/Pages/Advt.aspx";
    private final ScraperUtils utils;

    @Override
    public String getSourceName() {
        return "Employment News (Govt of India)";
    }

    @Override
    public String getSourceUrl() {
        return BASE_URL;
    }

    @Override
    public String getCategory() {
        return "OTHERS";
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
            // Employment News site lists adverts as table rows or linked items
            Elements rows = doc.select("table tr, .advt-row, ul li, .list-item");
            if (rows.isEmpty()) {
                rows = doc.select("a[href*='Advt'], a[href*='advt'], a[href*='pdf'], a[href*='recruitment']");
            }

            for (Element row : rows) {
                Element link = row.tagName().equals("a") ? row : row.selectFirst("a");
                if (link == null)
                    continue;

                String title = utils.buildTitle(link);
                if (title.length() < 10 || utils.isJunkTitle(title))
                    continue;

                String href = utils.absoluteUrl(BASE_URL, link.attr("href"));
                String category = deriveCategory(title);

                notices.add(RawNotice.builder()
                        .title(title)
                        .applyUrl(href)
                        .sourceName(getSourceName())
                        .sourceUrl(getSourceUrl())
                        .category(category)
                        .state(deriveState(title))
                        .publishedDate(utils.parseDate(utils.extractDateFromAncestor(link, 0)))
                        .lastDate(utils.parseDate(utils.extractDateFromAncestor(link, 1)))
                        .build());

                if (notices.size() >= 30)
                    break;
            }
            log.info("[EmploymentNews] Fetched {} notices", notices.size());
        } catch (Exception e) {
            log.error("[EmploymentNews] Failed to scrape: {}", e.getMessage());
        }
        return notices;
    }

    private String deriveCategory(String title) {
        String t = title.toLowerCase();
        if (t.contains("bank") || t.contains("rbi") || t.contains("nabard") || t.contains("ibps"))
            return "BANK";
        if (t.contains("ssc") || t.contains("staff selection"))
            return "SSC";
        if (t.contains("railway") || t.contains("rrb"))
            return "RAILWAYS";
        if (t.contains("upsc") || t.contains("civil service") || t.contains("ias"))
            return "UPSC";
        if (t.contains("defence") || t.contains("army") || t.contains("navy") || t.contains("air force"))
            return "DEFENCE";
        if (t.contains("doctor") || t.contains("medical") || t.contains("nurse") || t.contains("health")
                || t.contains("aiims") || t.contains("esic") || t.contains("nhm"))
            return "MEDICAL";
        if (t.contains("psu") || t.contains("bhel") || t.contains("ongc") || t.contains("ntpc"))
            return "PSU";
        return "OTHERS";
    }

    private String deriveState(String title) {
        String t = title.toLowerCase();
        if (t.contains("tamil nadu") || t.contains("tnpsc"))
            return "Tamil Nadu";
        if (t.contains("maharashtra"))
            return "Maharashtra";
        if (t.contains("karnataka"))
            return "Karnataka";
        if (t.contains("kerala"))
            return "Kerala";
        if (t.contains("delhi") || t.contains("ndmc"))
            return "Delhi";
        if (t.contains("gujarat"))
            return "Gujarat";
        if (t.contains("rajasthan"))
            return "Rajasthan";
        if (t.contains("uttar pradesh") || t.contains("uppsc"))
            return "Uttar Pradesh";
        return "Central";
    }
}
