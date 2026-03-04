package in.govtjobs.scraper.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class StateGovtScraperTest {

    @Autowired
    private StateGovtScraper scraper;

    @Test
    public void testScrape() {
        System.out.println("TEST_STARTING_STATE_SCRAPE");
        var notices = scraper.fetchRaw();
        System.out.println("TEST_SCRAPE_COUNT: " + notices.size());
        notices.forEach(n -> System.out.println("  - " + n.getState() + ": " + n.getTitle()));
    }
}
