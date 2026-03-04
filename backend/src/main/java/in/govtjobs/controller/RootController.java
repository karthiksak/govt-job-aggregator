package in.govtjobs.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    public ResponseEntity<Map<String, String>> root() {
        return ResponseEntity.ok(Map.of(
                "status", "online",
                "message", "Govt Job Aggregator Backend API is running.",
                "docs", "Check network traffic from the frontend for active endpoints."));
    }
}
