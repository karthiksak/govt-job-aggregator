package in.govtjobs.scraper;

import in.govtjobs.dto.RawNotice;

import java.util.List;

/**
 * Contract for all job notification source scrapers.
 * Each source must implement this interface.
 */
public interface JobNoticeSource {

    /**
     * Human-readable name of the source (e.g., "Staff Selection Commission")
     */
    String getSourceName();

    /**
     * Short code / display URL of the source website
     */
    String getSourceUrl();

    /**
     * Default category for notices from this source
     */
    String getCategory();

    /**
     * Default state for notices from this source ("Central" for central govt)
     */
    String getState();

    /**
     * Fetch raw notices from the source.
     * Implementations must handle their own errors and return empty list on
     * failure.
     */
    List<RawNotice> fetchRaw();
}
