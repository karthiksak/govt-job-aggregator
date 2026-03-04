package in.govtjobs.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ScraperServiceTest {

    @Autowired
    private ScraperService service;

    @Test
    public void testRunAllSynchronously() {
        System.out.println("TEST_STARTING_SYNC_SCRAPER");
        ScraperService.ScraperResult result = service.runAll();
        System.out.println("TEST_RESULT_TOTAL: " + result.total());
        System.out.println("TEST_RESULT_SAVED: " + result.saved());
        System.out.println("TEST_RESULT_SKIPPED: " + result.skipped());
        System.out.println("TEST_RESULT_ERRORS: " + result.errors());
    }
}
