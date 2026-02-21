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
            String category, String state, String noticeType, String branch, String period,
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
        String br = (branch == null || branch.isBlank()) ? null : branch.toUpperCase().trim();

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

        Page<JobNotice> notices = repository.findWithFilters(cat, st, nt, br, fromDateTime, toDateTime, pageable);
        return notices.map(this::toDto);
    }

    public long countNew() {
        return repository.countByFetchedAtAfter(LocalDateTime.now().minusHours(24));
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
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();
        boolean isNew = n.getFetchedAt() != null && n.getFetchedAt().isAfter(now.minusHours(24));
        boolean isDeadlineSoon = n.getLastDate() != null
                && !n.getLastDate().isBefore(today)
                && n.getLastDate().isBefore(today.plusDays(4));
        String sourceDomain = extractDomain(n.getSourceUrl());
        return JobNoticeDto.builder()
                .id(n.getId())
                .title(n.getTitle())
                .category(n.getCategory())
                .state(n.getState())
                .noticeType(n.getNoticeType())
                .engineeringBranches(n.getEngineeringBranches())
                .sourceName(n.getSourceName())
                .sourceUrl(n.getSourceUrl())
                .applyUrl(n.getApplyUrl())
                .publishedDate(n.getPublishedDate())
                .lastDate(n.getLastDate())
                .fetchedAt(n.getFetchedAt())
                .isNew(isNew)
                .isDeadlineSoon(isDeadlineSoon)
                .sourceDomain(sourceDomain)
                .build();
    }

    private String extractDomain(String url) {
        if (url == null || url.isBlank())
            return null;
        try {
            java.net.URI uri = java.net.URI.create(url);
            String host = uri.getHost();
            if (host == null)
                return null;
            // Strip leading www.
            return host.startsWith("www.") ? host.substring(4) : host;
        } catch (Exception e) {
            return null;
        }
    }
}
