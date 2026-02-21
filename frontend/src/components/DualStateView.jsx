import { useState, useCallback, useEffect, useRef } from 'react';
import { fetchNotices } from '../api/notices.js';
import NoticeCard from './NoticeCard.jsx';
import SkeletonCard from './Skeleton.jsx';

function AdPlaceholder() {
    return (
        <div style={{
            background: '#f8fafc', border: '1px dashed #cbd5e1', borderRadius: '10px',
            display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center',
            minHeight: '100px', color: '#64748b', fontSize: '0.8rem', padding: '0.75rem', textAlign: 'center'
        }}>
            <span style={{ fontSize: '1rem', marginBottom: '0.2rem' }}>📢</span>
            <span style={{ fontWeight: 600 }}>Advertisement</span>
        </div>
    );
}

function NoticeColumn({ title, subtitle, emoji, notices, loading, page, totalPages, onLoadMore, isSaved, toggleSave, accentColor = '#0B2447' }) {
    const sentinelRef = useRef(null);

    useEffect(() => {
        const observer = new IntersectionObserver(
            entries => {
                if (entries[0].isIntersecting && !loading && page < totalPages - 1) {
                    onLoadMore();
                }
            },
            { threshold: 0.1 }
        );
        const current = sentinelRef.current;
        if (current) observer.observe(current);
        return () => { if (current) observer.unobserve(current); };
    }, [loading, page, totalPages, onLoadMore]);

    return (
        <div style={{ flex: '1 1 0', minWidth: 0 }}>
            {/* Section header */}
            <div style={{
                background: `linear-gradient(135deg, ${accentColor} 0%, ${accentColor}dd 100%)`,
                borderRadius: '12px 12px 0 0', padding: '0.85rem 1.1rem',
                display: 'flex', alignItems: 'center', gap: '0.6rem', marginBottom: '0'
            }}>
                <span style={{ fontSize: '1.3rem' }}>{emoji}</span>
                <div>
                    <div style={{ color: 'white', fontWeight: 700, fontSize: '0.95rem', lineHeight: 1.2 }}>{title}</div>
                    {subtitle && <div style={{ color: 'rgba(255,255,255,0.65)', fontSize: '0.7rem' }}>{subtitle}</div>}
                </div>
                {notices.length > 0 && (
                    <span style={{
                        marginLeft: 'auto', background: 'rgba(255,255,255,0.2)',
                        color: 'white', fontSize: '0.65rem', fontWeight: 600,
                        padding: '0.15rem 0.5rem', borderRadius: '100px'
                    }}>
                        {notices.length} loaded
                    </span>
                )}
            </div>

            {/* Cards */}
            <div style={{
                border: '1px solid #E2E8F0', borderTop: 'none',
                borderRadius: '0 0 12px 12px', background: 'white',
                minHeight: 300, padding: '0.75rem',
                display: 'flex', flexDirection: 'column', gap: '0.6rem'
            }}>
                {loading && notices.length === 0 ? (
                    Array.from({ length: 4 }).map((_, i) => <SkeletonCard key={i} />)
                ) : notices.length === 0 ? (
                    <div style={{ textAlign: 'center', padding: '2rem', color: '#94A3B8' }}>
                        <div style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>🔍</div>
                        <div style={{ fontWeight: 600, marginBottom: '0.25rem' }}>No notices found</div>
                        <div style={{ fontSize: '0.8rem' }}>Check back after next refresh</div>
                    </div>
                ) : (
                    notices.map((notice, index) => (
                        <div key={notice.id || index}>
                            <NoticeCard notice={notice} isSaved={isSaved} toggleSave={toggleSave} />
                            {(index > 0 && (index + 1) % 5 === 0) && <AdPlaceholder />}
                        </div>
                    ))
                )}

                {loading && notices.length > 0 && (
                    <div style={{ textAlign: 'center', padding: '0.5rem', color: '#94A3B8', fontSize: '0.8rem' }}>
                        Loading more...
                    </div>
                )}
                <div ref={sentinelRef} style={{ height: 1 }} />
            </div>
        </div>
    );
}

export default function DualStateView({ selectedState, filters, isSaved, toggleSave }) {
    const [stateNotices, setStateNotices] = useState([]);
    const [centralNotices, setCentralNotices] = useState([]);
    const [stateLoading, setStateLoading] = useState(true);
    const [centralLoading, setCentralLoading] = useState(true);
    const [statePage, setStatePage] = useState(0);
    const [centralPage, setCentralPage] = useState(0);
    const [stateTotalPages, setStateTotalPages] = useState(0);
    const [centralTotalPages, setCentralTotalPages] = useState(0);

    const loadStateJobs = useCallback(async (pageNum, reset = false) => {
        setStateLoading(true);
        try {
            const data = await fetchNotices({
                state: selectedState,
                sortBy: filters.sortBy || 'newest',
                period: filters.period || 'all',
                ...(filters.noticeType && { noticeType: filters.noticeType }),
                page: pageNum,
                size: 15,
            });
            const content = data.content || [];
            if (reset || pageNum === 0) {
                setStateNotices(content);
            } else {
                setStateNotices(prev => {
                    const ids = new Set(prev.map(n => n.id));
                    return [...prev, ...content.filter(n => !ids.has(n.id))];
                });
            }
            setStateTotalPages(data.totalPages || 0);
        } catch (e) {
            console.error('State jobs load failed', e);
        } finally {
            setStateLoading(false);
        }
    }, [selectedState, filters]);

    const loadCentralJobs = useCallback(async (pageNum, reset = false) => {
        setCentralLoading(true);
        try {
            const data = await fetchNotices({
                state: 'Central',
                sortBy: filters.sortBy || 'newest',
                period: filters.period || 'all',
                ...(filters.noticeType && { noticeType: filters.noticeType }),
                page: pageNum,
                size: 15,
            });
            const content = data.content || [];
            if (reset || pageNum === 0) {
                setCentralNotices(content);
            } else {
                setCentralNotices(prev => {
                    const ids = new Set(prev.map(n => n.id));
                    return [...prev, ...content.filter(n => !ids.has(n.id))];
                });
            }
            setCentralTotalPages(data.totalPages || 0);
        } catch (e) {
            console.error('Central jobs load failed', e);
        } finally {
            setCentralLoading(false);
        }
    }, [filters]);

    // Initial load and on filter change
    useEffect(() => {
        setStatePage(0);
        loadStateJobs(0, true);
    }, [loadStateJobs]);

    useEffect(() => {
        setCentralPage(0);
        loadCentralJobs(0, true);
    }, [loadCentralJobs]);

    const handleLoadMoreState = useCallback(() => {
        const next = statePage + 1;
        setStatePage(next);
        loadStateJobs(next);
    }, [statePage, loadStateJobs]);

    const handleLoadMoreCentral = useCallback(() => {
        const next = centralPage + 1;
        setCentralPage(next);
        loadCentralJobs(next);
    }, [centralPage, loadCentralJobs]);

    return (
        <div style={{ display: 'flex', gap: '1.25rem', alignItems: 'flex-start' }}>
            <NoticeColumn
                title={`${selectedState} Govt Jobs`}
                subtitle={`${selectedState} State Government`}
                emoji="🗺️"
                notices={stateNotices}
                loading={stateLoading}
                page={statePage}
                totalPages={stateTotalPages}
                onLoadMore={handleLoadMoreState}
                isSaved={isSaved}
                toggleSave={toggleSave}
                accentColor="#BE185D"
            />
            <NoticeColumn
                title="Central Govt Jobs"
                subtitle="India-wide · SSC, UPSC, Banks, Railways, PSU"
                emoji="🇮🇳"
                notices={centralNotices}
                loading={centralLoading}
                page={centralPage}
                totalPages={centralTotalPages}
                onLoadMore={handleLoadMoreCentral}
                isSaved={isSaved}
                toggleSave={toggleSave}
                accentColor="#0B2447"
            />
        </div>
    );
}
