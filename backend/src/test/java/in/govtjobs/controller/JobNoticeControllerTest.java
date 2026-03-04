package in.govtjobs.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class JobNoticeControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testGetStatesResponse() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/states", String.class);
        System.out.println("TEST_RESULT_STATUS: " + response.getStatusCode());
        System.out.println("TEST_RESULT_BODY: " + response.getBody());
    }
}
