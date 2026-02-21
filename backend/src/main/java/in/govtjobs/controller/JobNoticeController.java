package in.govtjobs.controller;

import in.govtjobs.dto.ApiResponse;
import in.govtjobs.dto.JobNoticeDto;
import in.govtjobs.service.JobNoticeService;
import in.govtjobs.service.ScraperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class JobNoticeController {

    private final JobNoticeService noticeService;
    private final ScraperService scraperService;

    /**
     * GET /api/notices
     * Filters: category, state, period (today|this_week|all), page, size
     */
    @GetMapping("/notices")
    public ResponseEntity<ApiResponse<Page<JobNoticeDto>>> getNotices(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String noticeType,
            @RequestParam(defaultValue = "all") String period,
            @RequestParam(defaultValue = "newest") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<JobNoticeDto> result = noticeService.getNotices(category, state, noticeType, period, sortBy, page, size);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * GET /api/notices/{id}
     */
    @GetMapping("/notices/{id}")
    public ResponseEntity<ApiResponse<JobNoticeDto>> getById(@PathVariable UUID id) {
        return noticeService.getById(id)
                .map(dto -> ResponseEntity.ok(ApiResponse.ok(dto)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/categories
     */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<String>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.ok(noticeService.getCategories()));
    }

    /**
     * GET /api/states
     */
    @GetMapping("/states")
    public ResponseEntity<ApiResponse<List<String>>> getStates() {
        return ResponseEntity.ok(ApiResponse.ok(noticeService.getStates()));
    }

    /**
     * POST /api/admin/refresh - Manually trigger scrape
     */
    @PostMapping("/admin/refresh")
    public ResponseEntity<ApiResponse<ScraperService.ScraperResult>> refresh() {
        log.info("Manual refresh triggered via API");
        ScraperService.ScraperResult result = scraperService.runAll();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
