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

    private static final String TNPSC_URL = "https://www.tnpsc.gov.in/notifications.html";
    private static final String TNPSC_BASE = "https://www.tnpsc.gov.in";

    private static final String KPSC_URL = "https://kpsc.kar.nic.in/recruitment.aspx";
    private static final String KPSC_BASE = "https://kpsc.kar.nic.in";

    private static final String MPPSC_URL = "https://mppsc.mp.gov.in/Advertisements";
    private static final String MPPSC_BASE = "https://mppsc.mp.gov.in";

    private static final String OPSC_URL = "https://opsc.gov.in/Advt.aspx";
    private static final String OPSC_BASE = "https://opsc.gov.in";

    private final ScraperUtils utils;

    @Override
    public String getSourceName() {
        return "State Government PSC Jobs";
    }

    @Override
    public String getSourceUrl() {
        return TNPSC_BASE;
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
        notices.addAll(scrapeTnpsc());
        notices.addAll(scrapeKpsc());
        notices.addAll(scrapeMppsc());
        notices.addAll(scrapeOpsc());
        log.info("[StateGovt] Total fetched {} notices", notices.size());
        return notices;
    }

    // -------------------------------------------------------------------------
    // TNPSC
    // -------------------------------------------------------------------------
    private List<RawNotice> scrapeTnpsc() {
        List<RawNotice> list = new ArrayList<>();
        try {
            Document doc = utils.fetchPageLax(TNPSC_URL, 15000);
            Elements links = doc.select("table tr td a, ul li a, .notification a, a[href*='pdf'], a[href]");

            for (Element link : links) {
                String title = buildTitle(link);
                if (title.length() < 10 || utils.isJunkTitle(title) || !isJobRelated(title))
                    continue;

                String href = link.attr("href");
                if (href.toLowerCase().contains("javascript:"))
                    continue;
                href = utils.absoluteUrl(TNPSC_BASE, href);

                list.add(buildNotice(title, href, "TNPSC (Tamil Nadu PSC)", TNPSC_BASE, "Tamil Nadu", link));
                if (list.size() >= 15)
                    break;
            }
            log.info("[StateGovt/TNPSC] Fetched {} notices", list.size());
        } catch (Exception e) {
            log.warn("[StateGovt/TNPSC] Failed: {}", e.getMessage());
        }
        return list;
    }

    // -------------------------------------------------------------------------
    // KPSC (Karnataka)
    // -------------------------------------------------------------------------
    private List<RawNotice> scrapeKpsc() {
        List<RawNotice> list = new ArrayList<>();
        try {
            Document doc = utils.fetchPageLax(KPSC_URL, 15000);
            Elements links = doc.select("table tr td a, ul li a, a[href*='pdf'], a[href*='recruit'], a[href]");

            for (Element link : links) {
                String title = buildTitle(link);
                if (title.length() < 10 || utils.isJunkTitle(title) || !isJobRelated(title))
                    continue;

                String href = link.attr("href");
                if (href.toLowerCase().contains("javascript:"))
                    continue;
                href = utils.absoluteUrl(KPSC_BASE, href);

                list.add(buildNotice(title, href, "KPSC (Karnataka PSC)", KPSC_BASE, "Karnataka", link));
                if (list.size() >= 10)
                    break;
            }
            log.info("[StateGovt/KPSC] Fetched {} notices", list.size());
        } catch (Exception e) {
            log.warn("[StateGovt/KPSC] Failed: {}", e.getMessage());
        }
        return list;
    }

    // -------------------------------------------------------------------------
    // MPPSC (Madhya Pradesh)
    // -------------------------------------------------------------------------
    private List<RawNotice> scrapeMppsc() {
        List<RawNotice> list = new ArrayList<>();
        try {
            Document doc = utils.fetchPageLax(MPPSC_URL, 15000);
            Elements links = doc.select("table tr td a, ul li a, a[href*='pdf'], .advt a, a[href]");

            for (Element link : links) {
                String title = buildTitle(link);
                if (title.length() < 10 || utils.isJunkTitle(title) || !isJobRelated(title))
                    continue;

                String href = link.attr("href");
                if (href.toLowerCase().contains("javascript:"))
                    continue;
                href = utils.absoluteUrl(MPPSC_BASE, href);

                list.add(buildNotice(title, href, "MPPSC (Madhya Pradesh PSC)", MPPSC_BASE, "Madhya Pradesh", link));
                if (list.size() >= 10)
                    break;
            }
            log.info("[StateGovt/MPPSC] Fetched {} notices", list.size());
        } catch (Exception e) {
            log.warn("[StateGovt/MPPSC] Failed: {}", e.getMessage());
        }
        return list;
    }

    // -------------------------------------------------------------------------
    // OPSC (Odisha)
    // -------------------------------------------------------------------------
    private List<RawNotice> scrapeOpsc() {
        List<RawNotice> list = new ArrayList<>();
        try {
            Document doc = utils.fetchPageLax(OPSC_URL, 15000);
            Elements links = doc.select("table tr td a, ul li a, a[href*='pdf'], a[href*='advt'], a[href]");

            for (Element link : links) {
                String title = buildTitle(link);
                if (title.length() < 10 || utils.isJunkTitle(title) || !isJobRelated(title))
                    continue;

                String href = link.attr("href");
                if (href.toLowerCase().contains("javascript:"))
                    continue;
                href = utils.absoluteUrl(OPSC_BASE, href);

                list.add(buildNotice(title, href, "OPSC (Odisha PSC)", OPSC_BASE, "Odisha", link));
                if (list.size() >= 10)
                    break;
            }
            log.info("[StateGovt/OPSC] Fetched {} notices", list.size());
        } catch (Exception e) {
            log.warn("[StateGovt/OPSC] Failed: {}", e.getMessage());
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
