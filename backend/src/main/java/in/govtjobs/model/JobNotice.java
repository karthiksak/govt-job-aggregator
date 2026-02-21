package in.govtjobs.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "job_notices", indexes = {
        @Index(name = "idx_category", columnList = "category"),
        @Index(name = "idx_state", columnList = "state"),
        @Index(name = "idx_notice_type", columnList = "noticeType"),
        @Index(name = "idx_published_date", columnList = "publishedDate"),
        @Index(name = "idx_content_hash", columnList = "contentHash", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobNotice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 512)
    private String title;

    /**
     * Category: BANK, SSC, RRB, UPSC, PSU, STATE, RAILWAYS, DEFENCE, OTHERS
     */
    @Column(nullable = false, length = 50)
    private String category;

    /**
     * "Central" for central govt, or state name like "Tamil Nadu", "Delhi" etc.
     */
    @Column(nullable = false, length = 100)
    private String state;

    /**
     * Notice type: RECRUITMENT, EXAM_ADMIT_CARD, RESULT, CALENDAR, GENERAL_INFO
     */
    @Column(length = 50)
    private String noticeType;

    @Column(nullable = false, length = 200)
    private String sourceName;

    @Column(nullable = false, length = 1000)
    private String sourceUrl;

    @Column(length = 1000)
    private String applyUrl;

    private LocalDate publishedDate;
    private LocalDate lastDate;

    /**
     * SHA-256 of (title + sourceName) for deduplication
     */
    @Column(nullable = false, unique = true, length = 64)
    private String contentHash;

    @Column(nullable = false)
    private LocalDateTime fetchedAt;
}
