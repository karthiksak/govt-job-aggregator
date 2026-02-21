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
 * Scrapes government medical / health sector job notifications.
 *
 * Sources:
 * 1. AIIMS New Delhi - premier central govt medical institute
 * 2. ESIC - Employees' State Insurance Corporation
 * 3. NHM (National Health Mission) - health recruitment portal
 * 4. MRB (Medical Recruitment Board, Tamil Nadu)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MedicalJobsScraper implements JobNoticeSource {

    private static final String AIIMS_BASE = "https://www.aiims.edu";
    private static final String AIIMS_URL = "https://www.aiims.edu/en/notices.html";

    private static final String ESIC_BASE = "https://www.esic.gov.in";
    private static final String ESIC_URL = "https://www.esic.gov.in/recruitments";

    private static final String NHM_BASE = "https://nhm.gov.in";
    // Direct recruitment listing page of NHM
    private static final String NHM_RECRUITMENT_URL = "https://nhm.gov.in/index1.php?lang=1&level=1&sublinkid=971&lid=235";

    private static final String MRB_BASE = "https://www.mrb.tn.gov.in";
    private static final String MRB_URL = "https://www.mrb.tn.gov.in";

    private final ScraperUtils utils;

    @Override
    public String getSourceName() {
        return "Medical / Health Govt Jobs (AIIMS, ESIC, NHM, MRB)";
    }

    @Override
    public String getSourceUrl() {
        return AIIMS_BASE;
    }

    @Override
    public String getCategory() {
        return "MEDICAL";
    }

    @Override
    public String getState() {
        return "Central";
    }

    @Override
    public List<RawNotice> fetchRaw() {
        List<RawNotice> notices = new ArrayList<>();
        notices.addAll(scrapeAiims());
        notices.addAll(scrapeEsic());
        notices.addAll(scrapeNhm());
        notices.addAll(scrapeMrb());
        log.info("[Medical] Total fetched {} notices (AIIMS + ESIC + NHM + MRB)", notices.size());
        return notices;
    }

    // -------------------------------------------------------------------------
    // AIIMS
    // -------------------------------------------------------------------------
    private List<RawNotice> scrapeAiims() {
        List<RawNotice> list = new ArrayList<>();
        try {
            Document doc = utils.fetchPageLax(AIIMS_URL);
            Elements links = doc.select("a[href*='recruit'], a[href*='notice'], a[href*='vacancy'], a[href*='pdf']");
            if (links.isEmpty())
                links = doc.select("table tr a, ul li a, div a");

            for (Element link : links) {
                String title = utils.buildTitle(link);
                if (title.length() < 10 || utils.isJunkTitle(title))
                    continue;
                if (!isMedicalOrRelevant(title))
                    continue;
                String href = utils.absoluteUrl(AIIMS_BASE, link.attr("href"));
                list.add(buildNotice(title, href, "AIIMS New Delhi", AIIMS_BASE, "Central", link));
                if (list.size() >= 20)
                    break;
            }
            log.info("[AIIMS] Fetched {} notices", list.size());
        } catch (Exception e) {
            log.error("[AIIMS] Failed to scrape: {}", e.getMessage());
        }
        return list;
    }

    // -------------------------------------------------------------------------
    // ESIC — fix SNI warning by disabling TLS extensions for this host
    // -------------------------------------------------------------------------
    private List<RawNotice> scrapeEsic() {
        List<RawNotice> list = new ArrayList<>();
        // The ESIC server throws "unrecognized_name" TLS SNI warning.
        // Setting jsse.enableSNIExtension=false disables SNI globally for this thread.
        String prev = System.getProperty("jsse.enableSNIExtension");
        try {
            System.setProperty("jsse.enableSNIExtension", "false");
            Document doc = utils.fetchPageLax(ESIC_URL);
            Elements links = doc.select("a[href*='recruit'], a[href*='pdf'], a[href*='vacancy'], table tr a, ul li a");
            for (Element link : links) {
                String title = utils.buildTitle(link);
                if (title.length() < 10 || utils.isJunkTitle(title))
                    continue;
                String href = utils.absoluteUrl(ESIC_BASE, link.attr("href"));
                list.add(buildNotice(title, href, "ESIC (Employees' State Insurance Corporation)", ESIC_BASE, "Central",
                        link));
                if (list.size() >= 20)
                    break;
            }
            log.info("[ESIC] Fetched {} notices", list.size());
        } catch (Exception e) {
            log.error("[ESIC] Failed to scrape: {}", e.getMessage());
        } finally {
            // Restore previous setting
            if (prev == null)
                System.clearProperty("jsse.enableSNIExtension");
            else
                System.setProperty("jsse.enableSNIExtension", prev);
        }
        return list;
    }

    // -------------------------------------------------------------------------
    // NHM — only pick links that are genuine job/recruitment notices
    // -------------------------------------------------------------------------
    private List<RawNotice> scrapeNhm() {
        List<RawNotice> list = new ArrayList<>();
        try {
            Document doc = utils.fetchPageLax(NHM_RECRUITMENT_URL);
            // Prefer PDF or recruitment links in tables/lists
            Elements links = doc.select(
                    "a[href*='.pdf'], a[href*='recruit'], a[href*='vacancy'], a[href*='advt'], table td a, ul li a");
            for (Element link : links) {
                String title = utils.buildTitle(link);
                if (title.length() < 10 || utils.isJunkTitle(title))
                    continue;
                // Strict filter: must have a recruitment-type word
                if (!hasRecruitmentKeyword(title))
                    continue;
                String href = utils.absoluteUrl(NHM_BASE, link.attr("href"));
                list.add(buildNotice(title, href, "NHM (National Health Mission)", NHM_BASE, "Central", link));
                if (list.size() >= 20)
                    break;
            }
            log.info("[NHM] Fetched {} notices", list.size());
        } catch (Exception e) {
            log.error("[NHM] Failed to scrape: {}", e.getMessage());
        }
        return list;
    }

    // -------------------------------------------------------------------------
    // MRB Tamil Nadu
    // -------------------------------------------------------------------------
    private List<RawNotice> scrapeMrb() {
        List<RawNotice> list = new ArrayList<>();
        try {
            Document doc = utils.fetchPageLax(MRB_URL);
            Elements links = doc.select(
                    "a[href*='recruit'], a[href*='notification'], a[href*='pdf'], a[href*='vacancy'], table tr a, ul li a");
            if (links.isEmpty())
                links = doc.select("a");

            for (Element link : links) {
                String title = utils.buildTitle(link);
                if (title.length() < 10 || utils.isJunkTitle(title))
                    continue;
                if (!isMedicalOrRelevant(title))
                    continue;
                String href = utils.absoluteUrl(MRB_BASE, link.attr("href"));
                list.add(buildNotice(title, href, "MRB Tamil Nadu (Medical Recruitment Board)",
                        MRB_BASE, "Tamil Nadu", link));
                if (list.size() >= 20)
                    break;
            }
            log.info("[MRB] Fetched {} notices", list.size());
        } catch (Exception e) {
            log.error("[MRB] Failed to scrape: {}", e.getMessage());
        }
        return list;
    }

    // -------------------------------------------------------------------------
    // Shared helpers
    // -------------------------------------------------------------------------
    private RawNotice buildNotice(String title, String href, String sourceName,
            String sourceUrl, String state, Element link) {
        return RawNotice.builder()
                .title(utils.normalizeTitleForDisplay(title))
                .applyUrl(href)
                .sourceName(sourceName)
                .sourceUrl(sourceUrl)
                .category(getCategory())
                .state(state)
                .noticeType(utils.categorizeNoticeType(title))
                .engineeringBranches(utils.inferEngineeringBranches(title))
                .publishedDate(utils.parseDate(utils.extractDateFromAncestor(link, 0)))
                .lastDate(utils.parseDate(utils.extractDateFromAncestor(link, 1)))
                .build();
    }

    /**
     * Broad check: at least one job/medical-role keyword present.
     * Used by AIIMS and MRB where the page is already recruitment-focused.
     */
    private boolean isMedicalOrRelevant(String title) {
        String t = title.toLowerCase();
        return t.contains("recruit") || t.contains("vacancy") || t.contains("notification")
                || t.contains("advt") || t.contains("advertisement") || t.contains("job")
                || t.contains("doctor") || t.contains("nurse") || t.contains("physician")
                || t.contains("pharmacist") || t.contains("radiographer") || t.contains("technician")
                || t.contains("specialist") || t.contains("surgeon") || t.contains("dental")
                || t.contains("paramedic") || t.contains("anm") || t.contains("assistant")
                || t.contains("officer") || t.contains("engineer") || t.contains("clerk")
                || t.contains("mrb") || t.contains("aiims") || t.contains("esic");
    }

    /**
     * Stricter check for pages that mix job notices with general content.
     * Requires an explicit recruitment-action word (not just a role name).
     */
    private boolean hasRecruitmentKeyword(String title) {
        String t = title.toLowerCase();
        return t.contains("recruit") || t.contains("vacancy") || t.contains("notification")
                || t.contains("advt") || t.contains("advertisement") || t.contains("application")
                || t.contains("selection") || t.contains("walk-in") || t.contains("walkin")
                || t.contains("engage") || t.contains("appoint") || t.contains("post of")
                || t.contains("position") || t.contains("hiring");
    }
}
