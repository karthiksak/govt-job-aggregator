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
 * Scrapes State Government job notifications.
 *
 * Sources:
 * 1. TNPSC (Tamil Nadu Public Service Commission) — tnpsc.gov.in
 * 2. KPSC (Karnataka PSC) — kpsc.kar.nic.in
 * 3. MPPSC (Madhya Pradesh PSC) — mppsc.mp.gov.in
 * 4. OPSC (Odisha PSC) — opsc.gov.in
 *
 * These are all well-known state govt recruitment bodies with static pages.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StateGovtScraper implements JobNoticeSource {

    private record StateConfig(
            String state,
            String sourceName,
            String base,
            String url,
            String selectors) {
    }

    private static final List<StateConfig> SOURCES = List.of(
            // --- STATES ---
            new StateConfig("Andhra Pradesh", "APPSC", "https://psc.ap.gov.in", "https://psc.ap.gov.in/Default.aspx",
                    "a"),
            new StateConfig("Arunachal Pradesh", "APPSC", "https://appsc.gov.in", "https://appsc.gov.in", "a"),
            new StateConfig("Assam", "APSC", "https://apsc.nic.in", "https://apsc.nic.in", "a"),
            new StateConfig("Bihar", "BPSC", "https://www.bpsc.bih.nic.in", "https://www.bpsc.bih.nic.in", "a"),
            new StateConfig("Bihar", "BSSC", "https://bssc.bihar.gov.in", "https://bssc.bihar.gov.in/NoticeBoard.htm",
                    "a"),
            new StateConfig("Chhattisgarh", "CGPSC", "https://psc.cg.gov.in", "https://psc.cg.gov.in", "a"),
            new StateConfig("Goa", "GPSC", "https://gpsc.goa.gov.in", "https://gpsc.goa.gov.in/advertisement.php", "a"),
            new StateConfig("Gujarat", "GPSC", "https://gpsc.gujarat.gov.in",
                    "https://gpsc.gujarat.gov.in/Advertisements", "a"),
            new StateConfig("Haryana", "HPSC", "https://hpsc.gov.in", "https://hpsc.gov.in", "a"),
            new StateConfig("Haryana", "HSSC", "https://hssc.gov.in", "https://hssc.gov.in", "a"),
            new StateConfig("Himachal Pradesh", "HPPSC", "http://www.hppsc.hp.gov.in",
                    "http://www.hppsc.hp.gov.in/hppsc/", "a"),
            new StateConfig("Jharkhand", "JPSC", "https://jpsc.gov.in", "https://jpsc.gov.in", "a"),
            new StateConfig("Karnataka", "KPSC", "https://kpsc.kar.nic.in", "https://kpsc.kar.nic.in", "a"),
            new StateConfig("Kerala", "Kerala PSC", "https://www.keralapsc.gov.in",
                    "https://www.keralapsc.gov.in/notifications", "a"),
            new StateConfig("Madhya Pradesh", "MPPSC", "https://mppsc.mp.gov.in", "https://mppsc.mp.gov.in", "a"),
            new StateConfig("Maharashtra", "MPSC", "https://mpsc.gov.in", "https://mpsc.gov.in", "a"),
            new StateConfig("Manipur", "Manipur PSC", "https://mppsc.gov.in", "https://mppsc.gov.in", "a"),
            new StateConfig("Meghalaya", "Meghalaya PSC", "https://mpsc.nic.in", "https://mpsc.nic.in", "a"),
            new StateConfig("Mizoram", "Mizoram PSC", "https://mpsc.mizoram.gov.in", "https://mpsc.mizoram.gov.in",
                    "a"),
            new StateConfig("Nagaland", "NPSC", "https://npsc.nagaland.gov.in", "https://npsc.nagaland.gov.in", "a"),
            new StateConfig("Odisha", "OPSC", "https://opsc.gov.in", "https://opsc.gov.in", "a"),
            new StateConfig("Punjab", "PPSC", "https://ppsc.gov.in", "https://ppsc.gov.in", "a"),
            new StateConfig("Rajasthan", "RPSC", "https://rpsc.rajasthan.gov.in", "https://rpsc.rajasthan.gov.in", "a"),
            new StateConfig("Sikkim", "SPSC", "https://spsc.sikkim.gov.in", "https://spsc.sikkim.gov.in", "a"),
            new StateConfig("Tamil Nadu", "TNPSC", "https://www.tnpsc.gov.in", "https://www.tnpsc.gov.in/home.aspx",
                    "a"),
            new StateConfig("Telangana", "TSPSC", "https://websitenew.tspsc.gov.in", "https://websitenew.tspsc.gov.in",
                    "a"),
            new StateConfig("Tripura", "TPSC", "https://tpsc.tripura.gov.in", "https://tpsc.tripura.gov.in", "a"),
            new StateConfig("Uttar Pradesh", "UPPSC", "https://uppsc.up.nic.in", "https://uppsc.up.nic.in", "a"),
            new StateConfig("Uttar Pradesh", "UPSSSC", "https://upsssc.gov.in", "https://upsssc.gov.in/Default.aspx",
                    "a"),
            new StateConfig("Uttarakhand", "UKPSC", "https://psc.uk.gov.in", "https://psc.uk.gov.in", "a"),
            new StateConfig("West Bengal", "WBPSC", "https://psc.wb.gov.in", "https://psc.wb.gov.in", "a"),

            // --- UNION TERRITORIES ---
            new StateConfig("Delhi", "DSSSB", "https://dsssb.delhi.gov.in", "https://dsssb.delhi.gov.in", "a"),
            new StateConfig("Jammu & Kashmir", "JKSSB", "https://jkssb.nic.in", "https://jkssb.nic.in", "a"),
            new StateConfig("Puducherry", "Puducherry Admin", "https://recruitment.py.gov.in",
                    "https://recruitment.py.gov.in", "a"),
            new StateConfig("Chandigarh", "Chandigarh Admin", "https://chandigarh.gov.in", "https://chandigarh.gov.in",
                    "a"),
            new StateConfig("Andaman & Nicobar", "A&N Admin", "https://andaman.gov.in", "https://andaman.gov.in", "a"));

    private final ScraperUtils utils;

    @Override
    public String getSourceName() {
        return "State Government PSC Jobs";
    }

    @Override
    public String getSourceUrl() {
        return "Various State Portals";
    }

    @Override
    public String getCategory() {
        return "STATE";
    }

    @Override
    public String getState() {
        return "Various States";
    }

    @Override
    public List<RawNotice> fetchRaw() {
        List<RawNotice> notices = new ArrayList<>();

        for (StateConfig config : SOURCES) {
            notices.addAll(scrapeGeneric(config));
        }

        log.info("[StateGovt] Total fetched {} notices across all States/UTs", notices.size());
        return notices;
    }

    private List<RawNotice> scrapeGeneric(StateConfig config) {
        List<RawNotice> list = new ArrayList<>();
        try {
            // Use Lax connection to bypass SSL issues and handle timeouts cleanly
            Document doc = utils.fetchPageLax(config.url(), 10000);

            // Apply specific selectors or fallback to robust generic selection
            String selectors = (config.selectors() != null && !config.selectors().isBlank())
                    ? config.selectors()
                    : "table tr td a, ul li a, a[href*='pdf'], a[href*='advt'], a[href*='recruit']";

            Elements links = doc.select(selectors);

            for (Element link : links) {
                String title = buildTitle(link);
                if (title.length() < 10 || utils.isJunkTitle(title) || !isJobRelated(title))
                    continue;

                String href = link.attr("href");
                if (href.toLowerCase().contains("javascript:"))
                    continue;
                href = utils.absoluteUrl(config.base(), href);

                list.add(buildNotice(title, href, config.sourceName(), config.base(), config.state(), link));

                // Limit to max 10 latest notices per board to avoid flooding the DB with
                // ancient notices
                if (list.size() >= 10)
                    break;
            }
            log.info("[StateGovt/{}] Fetched {} notices", config.sourceName(), list.size());
        } catch (Exception e) {
            // Some state sites will naturally timeout or throw 403s (like BPSC/Kerala PSC).
            // We catch and softly warn instead of crashing the batch.
            log.warn("[StateGovt/{}] Failed: {}", config.sourceName(), e.getMessage());
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
     * Build readable title using the shared utility.
     */
    private String buildTitle(Element link) {
        return utils.buildTitle(link);
    }

    private boolean isJobRelated(String title) {
        String t = title.toLowerCase();

        // Exclude unwanted updates
        if (t.contains("result") || t.contains("answer key") || t.contains("admit card") ||
                t.contains("syllabus") || t.contains("mark sheet") || t.contains("corrigendum") ||
                t.contains("examination rules") || t.equals("odisha public service commission (opsc)")) {
            return false;
        }

        return t.contains("recruit") || t.contains("vacancy") || t.contains("notification")
                || t.contains("advt") || t.contains("advertisement") || t.contains("post")
                || t.contains("officer") || t.contains("engineer") || t.contains("inspector")
                || t.contains("grade") || t.contains("group") || t.contains("combined")
                || t.contains("direct recruit");
    }
}
