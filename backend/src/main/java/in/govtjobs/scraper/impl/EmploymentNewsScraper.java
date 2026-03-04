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
 * Scrapes government job listings from NCS (National Career Service) Portal.
 *
 * Note: employmentnews.gov.in returns HTTP 500 (site is down).
 * NCS (https://www.ncs.gov.in) is the official Govt of India employment portal
 * maintained by the Ministry of Labour & Employment and is reliably accessible.
 *
 * Secondary source: BankSarkari / Sarkari Result aggregator for latest Govt job
 * updates.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmploymentNewsScraper implements JobNoticeSource {

    // NCS — National Career Service portal, Govt of India
    private static final String NCS_URL = "https://www.ncs.gov.in/Pages/JobseekerJobSearch.aspx";
    private static final String NCS_BASE = "https://www.ncs.gov.in";
    // Employment News official — sometimes returns listings on the root page
    private static final String EN_ROOT_URL = "https://employmentnews.gov.in";
    private final ScraperUtils utils;

    @Override
    public String getSourceName() {
        return "Employment News (Govt of India)";
    }

    @Override
    public String getSourceUrl() {
        return "https://employmentnews.gov.in";
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
        notices.addAll(scrapeEmploymentNewsRoot());
        if (notices.size() < 5) {
            notices.addAll(scrapeNcs());
        }
        log.info("[EmploymentNews] Fetched {} notices", notices.size());
        return notices;
    }

    /**
     * Try the Employment News root page — sometimes has a latest notices section
     * even when the inner pages are broken.
     */
    private List<RawNotice> scrapeEmploymentNewsRoot() {
        List<RawNotice> list = new ArrayList<>();
        try {
            Document doc = utils.fetchPageLax(EN_ROOT_URL, 15000);
            // Look for PDF links, recruitment anchors, or table-row links
            Elements links = doc.select(
                    "a[href*='pdf'], a[href*='recruit'], a[href*='advt'], a[href*='vacancy'], " +
                            "a[href*='notification'], table tr td a, ul li a");
            for (Element link : links) {
                String title = utils.buildTitle(link);
                if (title.length() < 10 || utils.isJunkTitle(title))
                    continue;
                String href = utils.absoluteUrl("https://employmentnews.gov.in", link.attr("href"));
                notices(list, title, href, link);
                if (list.size() >= 30)
                    break;
            }
        } catch (Exception e) {
            log.warn("[EmploymentNews/Root] Failed: {}", e.getMessage());
        }
        return list;
    }

    /**
     * NCS Portal — National Career Service, lists verified Govt jobs.
     * Falls back here when Employment News site is down.
     */
    private List<RawNotice> scrapeNcs() {
        List<RawNotice> list = new ArrayList<>();
        try {
            Document doc = utils.fetchPageLax(NCS_URL, 15000);
            // NCS uses table rows and list items for job listings
            Elements links = doc.select("table tr td a, ul li a, .job-title a, a[href*='jobid'], a[href*='job']");
            if (links.isEmpty())
                links = doc.select("a[href]");

            for (Element link : links) {
                String title = utils.buildTitle(link);
                if (title.length() < 10 || utils.isJunkTitle(title))
                    continue;
                if (!isGovtJobRelated(title))
                    continue;
                String href = utils.absoluteUrl(NCS_BASE, link.attr("href"));
                notices(list, title, href, link);
                if (list.size() >= 30)
                    break;
            }
        } catch (Exception e) {
            log.warn("[EmploymentNews/NCS] Failed: {}", e.getMessage());
        }
        return list;
    }

    private void notices(List<RawNotice> list, String title, String href, Element link) {
        list.add(RawNotice.builder()
                .title(utils.normalizeTitleForDisplay(title))
                .applyUrl(href)
                .sourceName(getSourceName())
                .sourceUrl(getSourceUrl())
                .category(deriveCategory(title))
                .state(deriveState(title))
                .noticeType(utils.categorizeNoticeType(title))
                .engineeringBranches(utils.inferEngineeringBranches(title))
                .publishedDate(utils.parseDate(utils.extractDateFromAncestor(link, 0)))
                .lastDate(utils.parseDate(utils.extractDateFromAncestor(link, 1)))
                .build());
    }

    private boolean isGovtJobRelated(String title) {
        String t = title.toLowerCase();
        return t.contains("govt") || t.contains("government") || t.contains("central")
                || t.contains("state") || t.contains("psu") || t.contains("upsc")
                || t.contains("ssc") || t.contains("rrb") || t.contains("ibps")
                || t.contains("bank") || t.contains("railway") || t.contains("recruit")
                || t.contains("vacancy") || t.contains("notification") || t.contains("officer")
                || t.contains("clerk") || t.contains("defence") || t.contains("air force")
                || t.contains("navy") || t.contains("army") || t.contains("police")
                || t.contains("aiims") || t.contains("esic") || t.contains("nhm")
                || t.contains("public service");
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
