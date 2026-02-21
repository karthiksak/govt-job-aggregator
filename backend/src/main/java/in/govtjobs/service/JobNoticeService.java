package in.govtjobs.service;

import in.govtjobs.dto.JobNoticeDto;
import in.govtjobs.model.JobNotice;
import in.govtjobs.repository.JobNoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobNoticeService {

    private final JobNoticeRepository repository;

    public Page<JobNoticeDto> getNotices(
            String category, String state, String noticeType, String period,
            String sortBy, int page, int size) {

        LocalDate fromDate = null;
        LocalDate toDate = null;
        LocalDate today = LocalDate.now();

        if ("today".equalsIgnoreCase(period)) {
            fromDate = today;
            toDate = today;
        } else if ("this_week".equalsIgnoreCase(period)) {
            fromDate = today.minusDays(7);
            toDate = today;
        }

        String cat = (category == null || category.isBlank()) ? null : category.toUpperCase().trim();
        String st = (state == null || state.isBlank()) ? null : state.trim();
        String nt = (noticeType == null || noticeType.isBlank()) ? null : noticeType.toUpperCase().trim();

        Sort sort = switch (sortBy == null ? "newest" : sortBy.toLowerCase()) {
            case "deadline" ->
                // Soonest last date first; nulls pushed to end
                Sort.by(Sort.Order.asc("lastDate").nullsLast(), Sort.Order.desc("fetchedAt"));
            case "fetched" ->
                Sort.by(Sort.Direction.DESC, "fetchedAt");
            default -> // "newest"
                Sort.by(Sort.Order.desc("publishedDate").nullsLast(), Sort.Order.desc("fetchedAt"));
        };

        PageRequest pageable = PageRequest.of(page, Math.min(size, 50), sort);

        LocalDateTime fromDateTime = fromDate == null ? null : fromDate.atStartOfDay();
        LocalDateTime toDateTime = toDate == null ? null : toDate.atTime(23, 59, 59, 999999999);

        Page<JobNotice> notices = repository.findWithFilters(cat, st, nt, fromDateTime, toDateTime, pageable);
        return notices.map(this::toDto);
    }

    public Optional<JobNoticeDto> getById(UUID id) {
        return repository.findById(id).map(this::toDto);
    }

    @Cacheable("categories")
    public List<String> getCategories() {
        return repository.findDistinctCategories();
    }

    @Cacheable("states")
    public List<String> getStates() {
        return repository.findDistinctStates();
    }

    private JobNoticeDto toDto(JobNotice n) {
        return JobNoticeDto.builder()
                .id(n.getId())
                .title(n.getTitle())
                .category(n.getCategory())
                .state(n.getState())
                .sourceName(n.getSourceName())
                .sourceUrl(n.getSourceUrl())
                .applyUrl(n.getApplyUrl())
                .publishedDate(n.getPublishedDate())
                .lastDate(n.getLastDate())
                .fetchedAt(n.getFetchedAt())
                .build();
    }
}
