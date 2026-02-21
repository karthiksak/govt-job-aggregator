package in.govtjobs.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class JobNoticeDto {
    private UUID id;
    private String title;
    private String category;
    private String state;
    private String noticeType;
    private String sourceName;
    private String sourceUrl;
    private String applyUrl;
    private LocalDate publishedDate;
    private LocalDate lastDate;
    private LocalDateTime fetchedAt;
    /** True if fetched within the last 24 hours */
    private boolean isNew;
    /** True if lastDate is within the next 3 days and not already expired */
    private boolean isDeadlineSoon;
    /** Extracted hostname from sourceUrl, e.g. "upsc.gov.in" */
    private String sourceDomain;
    /** Comma-separated engineering branch codes, null if not engineering-related */
    private String engineeringBranches;
}
