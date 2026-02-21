package in.govtjobs.repository;

import in.govtjobs.model.JobNotice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface JobNoticeRepository extends JpaRepository<JobNotice, UUID> {

        boolean existsByContentHash(String contentHash);

        Page<JobNotice> findByCategory(String category, Pageable pageable);

        Page<JobNotice> findByState(String state, Pageable pageable);

        Page<JobNotice> findByPublishedDateBetween(LocalDate from, LocalDate to, Pageable pageable);

        @Query("SELECT DISTINCT j.category FROM JobNotice j ORDER BY j.category")
        List<String> findDistinctCategories();

        @Query("SELECT DISTINCT j.state FROM JobNotice j ORDER BY j.state")
        List<String> findDistinctStates();

        /**
         * Flexible filter query.
         * - category / state: treated as NULL = no filter
         * - period filter: uses fetchedAt (always present) so notices without
         * publishedDate are still included in "Today" / "This Week" views
         * - ORDER BY is omitted so Spring Data can apply the Pageable Sort
         */
        @Query("""
                        SELECT j FROM JobNotice j
                        WHERE (:category IS NULL OR j.category = :category)
                        AND   (:state    IS NULL OR j.state    = :state)
                        AND   (:noticeType IS NULL OR j.noticeType = :noticeType)
                        AND   (:fromDate IS NULL OR j.fetchedAt >= :fromDate)
                        AND   (:toDate   IS NULL OR j.fetchedAt <= :toDate)
                        """)
        Page<JobNotice> findWithFilters(
                        @Param("category") String category,
                        @Param("state") String state,
                        @Param("noticeType") String noticeType,
                        @Param("fromDate") java.time.LocalDateTime fromDate,
                        @Param("toDate") java.time.LocalDateTime toDate,
                        Pageable pageable);
}
