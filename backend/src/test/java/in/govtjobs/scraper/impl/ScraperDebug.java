package in.govtjobs.scraper.impl;

import in.govtjobs.util.ScraperUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ScraperDebug {
    public static void main(String[] args) throws Exception {
        ScraperUtils utils = new ScraperUtils();

        String[] urls = {
                "https://kpsc.kar.nic.in/recruitment.aspx",
                "https://mppsc.mp.gov.in/Advertisements",
                "https://psc.ap.gov.in/Default.aspx",
                "https://npsc.nagaland.gov.in/advertisements",
                "https://ppsc.gov.in/Advertisement/openadv.aspx"
        };

        for (String url : urls) {
            System.out.println("\n\n=== Fetching " + url + " ===");
            try {
                Document doc = utils.fetchPageLax(url, 15000);
                Elements links = doc.select("a");
                int fetched = 0;
                for (Element link : links) {
                    if (fetched > 20)
                        break; // just sample
                    String text = link.text().trim();
                    if (!text.isEmpty() && text.length() > 10) {
                        System.out.println("LINK TEXT: '" + text + "'");
                        fetched++;
                    }
                }
            } catch (Exception e) {
                System.out.println("Failed: " + e.getMessage());
            }
        }
    }
}
