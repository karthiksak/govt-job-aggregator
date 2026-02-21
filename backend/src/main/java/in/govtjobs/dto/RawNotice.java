package in.govtjobs.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class RawNotice {
    private String title;
    private String applyUrl;
    private LocalDate publishedDate;
    private LocalDate lastDate;
    // These are set by the scraper itself
    private String sourceName;
    private String sourceUrl;
    private String category;
    private String state;
    private String noticeType;
    private String engineeringBranches;
}
