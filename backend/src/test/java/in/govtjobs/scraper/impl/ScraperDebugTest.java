package in.govtjobs.scraper.impl;

import in.govtjobs.util.ScraperUtils;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

public class ScraperDebugTest {
    @Test
    public void runDebug() throws Exception {
        ScraperUtils utils = new ScraperUtils();

        String[] urls = {
                "https://kpsc.kar.nic.in/",
                "https://npsc.nagaland.gov.in/",
                "https://dsssb.delhi.gov.in/current-vacancies",
                "https://jkssb.nic.in/Jobs.html"
        };

        for (String url : urls) {
            System.out.println("\n\n=== Fetching " + url + " ===");
            try {
                Document doc = utils.fetchPageLax(url, 15000);
                String html = doc.html();
                System.out.println("HTML Length: " + html.length());
                if (html.length() > 0) {
                    System.out.println(html.substring(0, Math.min(html.length(), 500)));
                }
            } catch (Exception e) {
                System.out.println("Failed: " + e.getMessage());
            }
        }
    }
}
