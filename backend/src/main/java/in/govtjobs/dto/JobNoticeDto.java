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
}
