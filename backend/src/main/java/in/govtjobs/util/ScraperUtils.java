package in.govtjobs.util;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.net.ssl.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class ScraperUtils {

    @Value("${scraper.timeout.ms:10000}")
    private int timeoutMs;

    @Value("${scraper.user-agent:GovtJobAggregator/1.0}")
    private String userAgent;

    // A real browser UA — many Indian govt sites block Java's default UA
    private static final String BROWSER_UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36";

    private static final List<DateTimeFormatter> DATE_FORMATS = Arrays.asList(
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd.MM.yyyy"),
            DateTimeFormatter.ofPattern("d MMM yyyy"),
            DateTimeFormatter.ofPattern("dd MMM yyyy"),
            DateTimeFormatter.ofPattern("MMMM dd, yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("d-MM-yyyy"),
            DateTimeFormatter.ofPattern("dd MMM, yyyy"),
            DateTimeFormatter.ofPattern("d/M/yyyy"),
            DateTimeFormatter.ofPattern("dd-MMM-yyyy"),
            DateTimeFormatter.ofPattern("d-MMM-yyyy"),
            DateTimeFormatter.ofPattern("MMM dd, yyyy"),
            DateTimeFormatter.ofPattern("d MMMM yyyy"),
            DateTimeFormatter.ofPattern("dd MMMM yyyy"));

    /** Date pattern: matches dd-MM-yyyy, dd/MM/yyyy, dd.MM.yyyy variants */
    private static final Pattern DATE_REGEX = Pattern.compile(
            "\\b(\\d{1,2})[\\-./](\\d{1,2})[\\-./](\\d{4})\\b|" + // numeric
                    "\\b(\\d{1,2})\\s+(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*[,.\\-]?\\s*(?:-\\s*)?(\\d{4})\\b|"
                    + // 12 Jan 2025 or 13 Feb - 2026
                    "\\b(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\\s+(\\d{1,2})[,.\\-]?\\s*(?:-\\s*)?(\\d{4})\\b", // Jan
                                                                                                                                 // 12,
                                                                                                                                 // 2025
            Pattern.CASE_INSENSITIVE);

    /**
     * Known junk link texts that should be rejected as titles.
     * These are navigation links, generic CTAs, and boilerplate text.
     */
    private static final Set<String> JUNK_TITLES = Set.of(
            "click here", "download", "view", "here", "pdf", "read more",
            "more details", "details", "apply", "apply now", "apply online",
            "apply here", "link", "advertisement", "advt", "notification",
            "official notification", "official website", "visit", "open",
            "see details", "view details", "check here", "know more",
            "official", "english", "hindi", "corrigendum", "addendum",
            "important notice", "notice", "home", "news", "latest news",
            "recruitment", "vacancy", "result", "answer key", "about us",
            "contact us", "skip to main content", "login", "register",
            "syllabus", "careers", "tenders", "rti", "archives");

    static {
        // Install a trust-all SSL context so Indian govt sites with self-signed
        // or incomplete certificate chains don't cause scraper failures.
        try {
            TrustManager[] trustAll = new TrustManager[] {
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }

                        public void checkClientTrusted(X509Certificate[] c, String a) {
                        }

                        public void checkServerTrusted(X509Certificate[] c, String a) {
                        }
                    }
            };
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAll, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
        } catch (Exception e) {
            // Non-fatal — scrapers will still attempt connections
        }
    }

    /**
     * Fetch and parse an HTML page using Jsoup with standard headers.
     */
    public Document fetchPage(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent(userAgent)
                .timeout(timeoutMs)
                .method(Connection.Method.GET)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "en-IN,en;q=0.9")
                .followRedirects(true)
                .ignoreHttpErrors(false)
                .get();
    }

    /**
     * Fetch page with a real browser User-Agent and relaxed error handling.
     */
    public Document fetchPageLax(String url) throws IOException {
        return fetchPageLax(url, timeoutMs);
    }

    /**
     * Fetch page with browser UA, relaxed error handling, and custom timeout (ms).
     */
    public Document fetchPageLax(String url, int customTimeoutMs) throws IOException {
        return Jsoup.connect(url)
                .userAgent(BROWSER_UA)
                .timeout(customTimeoutMs)
                .method(Connection.Method.GET)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                .header("Accept-Language", "en-IN,en-GB;q=0.9,en;q=0.8")
                .header("Connection", "keep-alive")
                .header("Upgrade-Insecure-Requests", "1")
                .followRedirects(true)
                .ignoreHttpErrors(true)
                .ignoreContentType(true)
                .get();
    }

    /**
     * Parse various Indian date formats into LocalDate. Returns null if
     * unparseable.
     */
    public LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank())
            return null;
        String cleaned = dateStr.trim()
                .replaceAll("\\s+", " ")
                .replaceAll("(?i)(st|nd|rd|th)(?=\\s)", "")
                .replaceAll("(?i)([a-z]+)[,.\\-]?\\s*-\\s*(\\d{4})", "$1 $2") // Handles "Feb - 2026"
                .trim();
        for (DateTimeFormatter fmt : DATE_FORMATS) {
            try {
                return LocalDate.parse(cleaned, fmt);
            } catch (DateTimeParseException ignored) {
            }
        }
        log.debug("Could not parse date: '{}'", dateStr);
        return null;
    }

    /**
     * Extract the Nth date occurrence from freeform text.
     * Uses a broad regex covering numeric and month-name formats.
     */
    public String extractDate(String text, int index) {
        if (text == null)
            return null;
        Matcher m = DATE_REGEX.matcher(text);
        int i = 0;
        while (m.find()) {
            if (i == index)
                return m.group();
            i++;
        }
        return null;
    }

    /**
     * Walk up DOM ancestors to find date text, but stop at page-level containers.
     * Searches: the link itself → its parent (td/span) → the row (tr/li) → one
     * more.
     * Never climbs into body/main/section/div-that-contains-all-rows to avoid
     * attributing the FIRST date on the entire page to every notice.
     *
     * @param link  the anchor element
     * @param index 0 = published date, 1 = last/closing date
     */
    public String extractDateFromAncestor(Element link, int index) {
        Element el = link;
        for (int depth = 0; depth < 5; depth++) {
            if (el == null)
                break;
            String tag = el.tagName().toLowerCase();
            // Stop if we've climbed into a page-level container
            if (depth > 0 && (tag.equals("body") || tag.equals("html")
                    || tag.equals("main") || tag.equals("article")))
                break;
            // Search text of this element (own text only, not deep children beyond row)
            String text = el.ownText();
            // For tr/li/p also include the full subtree text (these are row-level)
            if (tag.equals("tr") || tag.equals("li") || tag.equals("p")
                    || tag.equals("td") || tag.equals("th") || tag.equals("span")
                    || tag.equals("a")) {
                text = el.text();
            }
            String found = extractDate(text, index);
            if (found != null)
                return found;
            // After reaching a row-boundary tag, stop climbing further
            if (tag.equals("tr") || tag.equals("li"))
                break;
            el = el.parent();
        }
        return null;
    }

    /**
     * Build a title for a link, in order of preference:
     * 1. Link text if it's ≥12 chars and NOT a junk phrase
     * 2. Title attribute of the link
     * 3. PDF filename from href (underscores/hyphens → spaces)
     * 4. Parent row text trimmed to first sentence-like segment
     *
     * Returns empty string when nothing useful found.
     */
    public String buildTitle(Element link) {
        // 1. Link text
        String text = cleanTitle(link.text());
        if (text.length() >= 12 && !isJunkTitle(text))
            return text;

        // 2. title= attribute
        String titleAttr = cleanTitle(link.attr("title"));
        if (titleAttr.length() >= 12 && !isJunkTitle(titleAttr))
            return titleAttr;

        // 3. PDF/DOC filename in href
        String href = link.attr("href");
        if (href != null && !href.isBlank()) {
            String filename = href;
            if (filename.contains("/"))
                filename = filename.substring(filename.lastIndexOf('/') + 1);
            if (filename.contains("?"))
                filename = filename.substring(0, filename.indexOf('?'));
            filename = filename.replaceAll("(?i)\\.(pdf|doc|docx|htm|html|php|aspx)$", "")
                    .replace("_", " ").replace("-", " ").replaceAll("\\s+", " ").trim();
            if (filename.length() >= 12 && !isJunkTitle(filename))
                return filename;
        }

        // 4. First meaningful segment from ancestor row text
        Element ancestor = link.parent();
        for (int d = 0; d < 4 && ancestor != null; d++) {
            String tag = ancestor.tagName().toLowerCase();
            if (tag.equals("body") || tag.equals("html") || tag.equals("main") || tag.equals("article")) {
                break;
            }
            String rowText = ancestor.text().trim();
            if (rowText.length() >= 15) {
                // Take up to 120 chars
                return cleanTitle(rowText.length() > 120 ? rowText.substring(0, 120) + "…" : rowText);
            }
            ancestor = ancestor.parent();
        }

        return text; // Return whatever we have even if short
    }

    /**
     * Returns true when the title is a known navigation/boilerplate phrase
     * that should not be stored as a job title.
     */
    public boolean isJunkTitle(String title) {
        if (title == null || title.isBlank())
            return true;
        String lower = title.toLowerCase().trim();
        if (lower.length() < 6)
            return true;
        return JUNK_TITLES.contains(lower);
    }

    /**
     * Generate SHA-256 hash for deduplication. Uses normalized title to prevent
     * duplicates.
     */
    public String hash(String title, String sourceName) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String normalizedTitle = normalizeTitleForDisplay(title);
            String input = (normalizedTitle + "|" + sourceName).toLowerCase().trim();
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf((title + sourceName).hashCode());
        }
    }

    /**
     * Categorizes a notice into RECRUITMENT, EXAM_ADMIT_CARD, RESULT, CALENDAR, or
     * GENERAL_INFO.
     */
    public String categorizeNoticeType(String title) {
        if (title == null || title.isBlank())
            return "GENERAL_INFO";
        String lower = title.toLowerCase();

        if (lower.contains("result") || lower.contains("merit list") || lower.contains("selection list")
                || lower.contains("marks") || lower.contains("cut off") || lower.contains("score")) {
            return "RESULT";
        }
        if (lower.contains("admit card") || lower.contains("hall ticket") || lower.contains("exam date")
                || lower.contains("interview schedule") || lower.contains("call letter")) {
            return "EXAM_ADMIT_CARD";
        }
        if (lower.contains("calendar") || lower.contains("planner") || lower.contains("schedule")) {
            return "CALENDAR";
        }
        if (lower.contains("apprentice") || lower.contains("nats") || lower.contains("trade apprentice")
                || lower.contains("act apprentice") || lower.contains("apprenticeship")) {
            return "APPRENTICESHIP";
        }
        if (lower.contains("recruit") || lower.contains("vacancy") || lower.contains("notification")
                || lower.contains("advt") || lower.contains("apply") || lower.contains("post")
                || lower.contains("officer") || lower.contains("clerk")) {
            return "RECRUITMENT";
        }
        return "GENERAL_INFO";
    }

    /**
     * Infers engineering branch codes from a notice title using keyword matching.
     * Returns a comma-separated string of matched branch codes, or null if none
     * found.
     * Codes: CIVIL, MECH, EEE, ECE, CSE, CHEM, INST, GENERAL_ENGG
     */
    public String inferEngineeringBranches(String title) {
        if (title == null || title.isBlank())
            return null;
        String lower = title.toLowerCase();
        java.util.List<String> branches = new java.util.ArrayList<>();

        if (lower.contains("civil") || lower.contains("structural"))
            branches.add("CIVIL");
        if (lower.contains("mechanical") || lower.contains(" mech ") || lower.contains("machinist")
                || lower.contains("fitter") || lower.contains("welder") || lower.contains("boiler"))
            branches.add("MECH");
        if (lower.contains("electrical") || lower.contains(" eee ") || lower.contains("electrician"))
            branches.add("EEE");
        if (lower.contains("electronics") || lower.contains(" ece ") || lower.contains("radio")
                || lower.contains("telecommunication"))
            branches.add("ECE");
        if (lower.contains("computer") || lower.contains(" cse ") || lower.contains(" it ")
                || lower.contains("software")
                || lower.contains("programmer") || lower.contains("data entry"))
            branches.add("CSE");
        if (lower.contains("chemical") || lower.contains("petrochem"))
            branches.add("CHEM");
        if (lower.contains("instrumentation") || lower.contains("instrument"))
            branches.add("INST");

        // If title has generic engineering keywords but no specific branch detected
        if (branches.isEmpty() && (lower.contains("engineer") || lower.contains(" je ") ||
                lower.contains(" get ") || lower.contains("technical officer") ||
                lower.contains("graduate engineer") || lower.contains("junior engineer")))
            branches.add("GENERAL_ENGG");

        return branches.isEmpty() ? null : String.join(",", branches);
    }

    /**
     * Returns true if the title contains keywords indicating an engineering-related
     * notice.
     */
    public boolean isEngineeringRelated(String title) {
        return inferEngineeringBranches(title) != null;
    }

    /**
     * Cleans up the title for display by removing redundant prefixes and extra
     * whitespace.
     */
    public String normalizeTitleForDisplay(String raw) {
        if (raw == null)
            return "";
        String cleaned = cleanTitle(raw);
        cleaned = cleaned
                .replaceAll("(?i)^(Update:|Flash:|New:|Latest:|Notice:|Advertisement:|Advt:|Notification:)\\s*", "");
        if (cleaned.length() > 200) {
            cleaned = cleaned.substring(0, 197) + "...";
        }
        return cleaned.trim();
    }

    /**
     * Clean and normalize a title string.
     */
    public String cleanTitle(String raw) {
        if (raw == null)
            return "";
        return raw.trim()
                .replaceAll("\\s+", " ")
                .replaceAll("[\\r\\n\\t]", " ")
                .trim();
    }

    /**
     * Make a relative URL absolute given a base URL.
     */
    public String absoluteUrl(String base, String relative) {
        if (relative == null || relative.isBlank())
            return base;
        if (relative.startsWith("http://") || relative.startsWith("https://"))
            return relative;
        if (relative.startsWith("//"))
            return "https:" + relative;
        if (base.endsWith("/"))
            return base + relative.replaceFirst("^/", "");
        return base + "/" + relative.replaceFirst("^/", "");
    }
}
