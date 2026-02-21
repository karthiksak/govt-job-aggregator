import NoticeCard from './NoticeCard.jsx';
import SkeletonCard from './Skeleton.jsx';

function AdPlaceholder() {
    return (
        <div style={{
            background: '#f8fafc',
            border: '1px dashed #cbd5e1',
            borderRadius: '10px',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            minHeight: '120px',
            color: '#64748b',
            fontSize: '0.85rem',
            padding: '1rem',
            textAlign: 'center'
        }}>
            <span style={{ fontSize: '1.2rem', marginBottom: '0.25rem' }}>📢</span>
            <span style={{ fontWeight: 600 }}>Advertisement</span>
            <span style={{ fontSize: '0.7rem' }}>AdSense Space (In-Feed)</span>
        </div>
    );
}

import { useEffect, useRef } from 'react';

// Pagination component removed in favor of infinite scroll

export default function NoticeList({ notices, loading, page, totalPages, onPageChange }) {
    const observerTarget = useRef(null);

    useEffect(() => {
        const observer = new IntersectionObserver(
            entries => {
                if (entries[0].isIntersecting && !loading && page < totalPages - 1) {
                    onPageChange(page + 1);
                }
            },
            { threshold: 0.1 }
        );

        const currentTarget = observerTarget.current;
        if (currentTarget) {
            observer.observe(currentTarget);
        }

        return () => {
            if (currentTarget) {
                observer.unobserve(currentTarget);
            }
        };
    }, [observerTarget, loading, page, totalPages, onPageChange]);

    if (!notices || notices.length === 0) {
        if (loading) {
            return (
                <div className="notices-grid" aria-busy="true" aria-label="Loading notices">
                    {Array.from({ length: 9 }).map((_, i) => <SkeletonCard key={i} />)}
                </div>
            );
        }

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
                {notices.map((notice, index) => (
                    <div key={notice.id || index} style={{ display: 'contents' }}>
                        <NoticeCard notice={notice} />
                        {/* Inject an Ad placeholder every 6 items */}
                        {(index > 0 && (index + 1) % 6 === 0) && (
                            <AdPlaceholder />
                        )}
                    </div>
                ))}
            </div>

            {loading && (
                <div className="notices-grid" style={{ marginTop: '2rem' }} aria-busy="true">
                    {Array.from({ length: 3 }).map((_, i) => <SkeletonCard key={`skel-${i}`} />)}
                </div>
            )}

            {/* Intersection Observer Sentinel */}
            {!loading && page < totalPages - 1 && (
                <div ref={observerTarget} style={{ height: '50px', width: '100%' }} />
            )}
        </div>
    );
}
