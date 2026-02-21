import NoticeCard from './NoticeCard.jsx';
import SkeletonCard from './Skeleton.jsx';

function Pagination({ page, totalPages, onPageChange }) {
    if (totalPages <= 1) return null;

    const pages = Array.from({ length: Math.min(totalPages, 7) }, (_, i) => {
        if (totalPages <= 7) return i;
        // Sliding window
        if (page < 4) return i;
        if (page > totalPages - 5) return totalPages - 7 + i;
        return page - 3 + i;
    });

    return (
        <nav className="pagination" aria-label="Pagination">
            <button
                className="page-btn"
                onClick={() => onPageChange(page - 1)}
                disabled={page === 0}
                aria-label="Previous page"
            >‹</button>
            {pages.map(p => (
                <button
                    key={p}
                    className={`page-btn ${p === page ? 'active' : ''}`}
                    onClick={() => onPageChange(p)}
                    aria-current={p === page ? 'page' : undefined}
                >
                    {p + 1}
                </button>
            ))}
            <button
                className="page-btn"
                onClick={() => onPageChange(page + 1)}
                disabled={page >= totalPages - 1}
                aria-label="Next page"
            >›</button>
        </nav>
    );
}

export default function NoticeList({ notices, loading, page, totalPages, onPageChange }) {
    if (loading) {
        return (
            <div>
                <div className="notices-grid" aria-busy="true" aria-label="Loading notices">
                    {Array.from({ length: 9 }).map((_, i) => <SkeletonCard key={i} />)}
                </div>
            </div>
        );
    }

    if (!notices || notices.length === 0) {
        return (
            <div className="empty-state" role="status">
                <div className="emoji">🔍</div>
                <h3>No notices found</h3>
                <p>No government job notifications match your current filters. Try selecting "All Jobs" or "All States" to see more results.</p>
            </div>
        );
    }

    return (
        <div>
            <div className="notices-grid">
                {notices.map(notice => (
                    <NoticeCard key={notice.id} notice={notice} />
                ))}
            </div>
            <Pagination page={page} totalPages={totalPages} onPageChange={onPageChange} />
        </div>
    );
}
