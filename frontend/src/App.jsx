import { useState, useEffect, useCallback } from 'react';
import { Helmet } from 'react-helmet-async';
import { fetchNotices, fetchStates, triggerRefresh, fetchNewCount } from './api/notices.js';
import useSavedNotices from './hooks/useSavedNotices.js';
import NoticeCard from './components/NoticeCard.jsx';
import SkeletonCard from './components/Skeleton.jsx';
import Footer from './components/Footer.jsx';

const INDIAN_STATES = [
    'Andhra Pradesh', 'Arunachal Pradesh', 'Assam', 'Bihar', 'Chhattisgarh',
    'Delhi', 'Goa', 'Gujarat', 'Haryana', 'Himachal Pradesh', 'Jharkhand',
    'Jammu & Kashmir', 'Karnataka', 'Kerala', 'Madhya Pradesh', 'Maharashtra',
    'Manipur', 'Meghalaya', 'Mizoram', 'Nagaland', 'Odisha', 'Punjab',
    'Puducherry', 'Rajasthan', 'Sikkim', 'Tamil Nadu', 'Telangana', 'Tripura',
    'Uttar Pradesh', 'Uttarakhand', 'West Bengal',
];

const CATEGORIES = [
    { key: '', label: 'All' }, { key: 'BANK', label: '🏦 Bank' },
    { key: 'SSC', label: '📋 SSC' }, { key: 'RAILWAYS', label: '🚂 Railways' },
    { key: 'UPSC', label: '🎖️ UPSC' }, { key: 'PSU', label: '🏭 PSU' },
    { key: 'STATE', label: '🗺️ State' }, { key: 'MEDICAL', label: '🏥 Medical' },
    { key: 'DEFENCE', label: '🛡️ Defence' },
];

const STATE_KEY = 'govtjobs_state_v2';

function AdPlaceholder() {
    return (
        <div style={{
            background: '#f8fafc', border: '1px dashed #cbd5e1', borderRadius: '10px',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            minHeight: '80px', color: '#94A3B8', fontSize: '0.78rem', gap: '0.4rem',
        }}>
            📢 <span>Advertisement</span>
        </div>
    );
}

function NoticeGrid({ notices, loading, isSaved, toggleSave }) {
    if (loading && notices.length === 0) {
        return (
            <div className="notices-grid">
                {Array.from({ length: 6 }).map((_, i) => <SkeletonCard key={i} />)}
            </div>
        );
    }
    if (notices.length === 0) {
        return (
            <div className="empty-state">
                <div className="emoji">🔍</div>
                <h3>No notices found</h3>
                <p>Try changing the category filter or refreshing.</p>
            </div>
        );
    }
    return (
        <div className="notices-grid">
            {notices.map((n, i) => (
                <div key={n.id || i} style={{ display: 'contents' }}>
                    <NoticeCard notice={n} isSaved={isSaved} toggleSave={toggleSave} />
                    {i > 0 && (i + 1) % 6 === 0 && <AdPlaceholder />}
                </div>
            ))}
            {loading && Array.from({ length: 3 }).map((_, i) => <SkeletonCard key={`sk-${i}`} />)}
        </div>
    );
}

export default function App() {
    const selectedState = localStorage.getItem(STATE_KEY) || '';
    const [userState, setUserState] = useState(selectedState);
    const [stateTab, setStateTab] = useState('state'); // 'state' | 'central' — only used when state is set
    const [category, setCategory] = useState('');
    const [noticeType, setNoticeType] = useState('');
    const [sortBy, setSortBy] = useState('newest');
    const [notices, setNotices] = useState([]);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const [loading, setLoading] = useState(false);
    const [newCount, setNewCount] = useState(0);
    const [refreshing, setRefreshing] = useState(false);
    const [toast, setToast] = useState(null);
    const [showStateSelector, setShowStateSelector] = useState(false);
    const { savedIds, savedNotices, isSaved, toggleSave } = useSavedNotices();
    const [activeView, setActiveView] = useState('jobs'); // 'jobs' | 'saved'

    const showToast = (msg) => { setToast(msg); setTimeout(() => setToast(null), 3000); };

    // Which state to query
    const queryState = userState
        ? (stateTab === 'central' ? 'Central' : userState)
        : '';

    const load = useCallback(async (pg, reset = false) => {
        setLoading(true);
        try {
            const data = await fetchNotices({
                ...(category && { category }),
                ...(queryState && { state: queryState }),
                ...(noticeType && { noticeType }),
                sortBy, period: 'all', page: pg, size: 18,
            });
            const items = data.content || [];
            if (reset || pg === 0) {
                setNotices(items);
            } else {
                setNotices(prev => {
                    const ids = new Set(prev.map(n => n.id));
                    return [...prev, ...items.filter(n => !ids.has(n.id))];
                });
            }
            setTotalPages(data.totalPages || 0);
            setTotalElements(data.totalElements || 0);
        } catch { setNotices([]); }
        finally { setLoading(false); }
    }, [category, queryState, noticeType, sortBy]);

    useEffect(() => {
        setPage(0);
        load(0, true);
    }, [load]);

    useEffect(() => {
        fetchNewCount().then(c => setNewCount(c || 0)).catch(() => { });
    }, []);

    // Infinite scroll sentinel
    const sentinelRef = useCallback(node => {
        if (!node) return;
        const obs = new IntersectionObserver(entries => {
            if (entries[0].isIntersecting && !loading && page < totalPages - 1) {
                const next = page + 1;
                setPage(next);
                load(next);
            }
        }, { threshold: 0.1 });
        obs.observe(node);
    }, [loading, page, totalPages, load]);

    const handleSetState = (s) => {
        setUserState(s);
        localStorage.setItem(STATE_KEY, s);
        setShowStateSelector(false);
        setPage(0);
        setStateTab('state');
    };

    const handleClearState = () => {
        setUserState('');
        localStorage.removeItem(STATE_KEY);
        setPage(0);
    };

    const handleStateTab = (tab) => {
        setStateTab(tab);
        setPage(0);
    };

    const handleCategory = (cat) => { setCategory(cat); setPage(0); };

    const handleRefresh = async () => {
        if (refreshing) return;
        setRefreshing(true);
        try {
            const { triggerRefresh: tr } = await import('./api/notices.js');
            const result = await tr();
            showToast(`✅ ${result.saved || 0} new notices added`);
            load(0, true);
        } catch { showToast('❌ Refresh failed'); } finally { setRefreshing(false); }
    };

    const pageTitle = userState
        ? `${userState} & Central Govt Jobs 2026 | GovtJobs.in`
        : 'Latest Sarkari Naukri 2026 — Government Jobs India | GovtJobs.in';

    return (
        <>
            <Helmet>
                <title>{pageTitle}</title>
                <meta name="description" content="Latest government job notifications from SSC, Banks, RRB, UPSC, PSU, State Govts. Updated every 6 hours." />
            </Helmet>

            {/* ── HEADER ── */}
            <header className="site-header">
                <div className="container header-inner">
                    <div className="logo-block">
                        <span className="logo-badge">IN</span>
                        <div>
                            <div className="logo-text">GovtJobs.in</div>
                            <div className="logo-sub">Official Job Aggregator</div>
                        </div>
                    </div>
                    <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
                        {/* State pill/selector */}
                        {userState ? (
                            <button
                                onClick={() => setShowStateSelector(true)}
                                style={{
                                    background: 'rgba(255,255,255,0.15)', border: '1.5px solid rgba(255,255,255,0.25)',
                                    color: 'white', borderRadius: '100px', padding: '0.3rem 0.8rem',
                                    fontSize: '0.78rem', cursor: 'pointer', fontFamily: 'inherit', fontWeight: 600
                                }}
                            >
                                📍 {userState}
                            </button>
                        ) : (
                            <button
                                onClick={() => setShowStateSelector(true)}
                                style={{
                                    background: '#FF9933', border: 'none',
                                    color: 'white', borderRadius: '100px', padding: '0.3rem 0.9rem',
                                    fontSize: '0.78rem', cursor: 'pointer', fontFamily: 'inherit', fontWeight: 700
                                }}
                            >
                                📍 Select Your State
                            </button>
                        )}
                        {newCount > 0 && (
                            <span style={{ background: '#22C55E', color: 'white', fontSize: '0.65rem', fontWeight: 700, padding: '0.2rem 0.5rem', borderRadius: '100px' }}>
                                🆕 {newCount} new
                            </span>
                        )}
                    </div>
                </div>
            </header>

            {/* ── STATE SELECTOR SHEET ── */}
            {showStateSelector && (
                <div
                    style={{ position: 'fixed', inset: 0, zIndex: 999, background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'flex-end', justifyContent: 'center' }}
                    onClick={(e) => e.target === e.currentTarget && setShowStateSelector(false)}
                >
                    <div style={{
                        background: 'white', width: '100%', maxWidth: 540, borderRadius: '20px 20px 0 0',
                        padding: '1.5rem 1.25rem', maxHeight: '80vh', display: 'flex', flexDirection: 'column',
                    }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                            <h2 style={{ fontSize: '1.1rem', fontWeight: 700 }}>📍 Select Your State</h2>
                            <button onClick={() => setShowStateSelector(false)} style={{ background: 'none', border: 'none', fontSize: '1.3rem', cursor: 'pointer', color: '#64748b' }}>✕</button>
                        </div>
                        <div style={{ overflowY: 'auto', flex: 1, display: 'flex', flexDirection: 'column', gap: '0.4rem' }}>
                            {userState && (
                                <button
                                    onClick={handleClearState}
                                    style={{ padding: '0.7rem 1rem', border: '1.5px solid #E2E8F0', borderRadius: 10, background: '#FFF1F2', color: '#991B1B', fontSize: '0.85rem', cursor: 'pointer', fontFamily: 'inherit', textAlign: 'left' }}
                                >
                                    🔄 Clear — Show All India
                                </button>
                            )}
                            {INDIAN_STATES.map(s => (
                                <button
                                    key={s}
                                    onClick={() => handleSetState(s)}
                                    style={{
                                        padding: '0.7rem 1rem', border: `1.5px solid ${userState === s ? '#FF9933' : '#E2E8F0'}`,
                                        borderRadius: 10, background: userState === s ? '#FFF7ED' : 'white',
                                        color: userState === s ? '#92400E' : '#0F172A', fontFamily: 'inherit',
                                        fontSize: '0.88rem', cursor: 'pointer', textAlign: 'left', fontWeight: userState === s ? 600 : 400
                                    }}
                                >
                                    {userState === s ? '✅ ' : ''}{s}
                                </button>
                            ))}
                        </div>
                    </div>
                </div>
            )}

            {/* ── MAIN ── */}
            <main>
                {/* State / Central toggle tabs — only shown when a state is selected */}
                {userState && activeView === 'jobs' && (
                    <div style={{ background: 'white', borderBottom: '1px solid #E2E8F0', position: 'sticky', top: 54, zIndex: 90 }}>
                        <div className="container" style={{ display: 'flex', gap: 0 }}>
                            <button
                                onClick={() => handleStateTab('state')}
                                style={{
                                    padding: '0.75rem 1.5rem', border: 'none', borderBottom: stateTab === 'state' ? '3px solid #FF9933' : '3px solid transparent',
                                    background: 'none', fontFamily: 'inherit', fontWeight: stateTab === 'state' ? 700 : 400,
                                    color: stateTab === 'state' ? '#FF9933' : '#64748b', cursor: 'pointer', fontSize: '0.88rem'
                                }}
                            >
                                🗺️ {userState} Jobs
                            </button>
                            <button
                                onClick={() => handleStateTab('central')}
                                style={{
                                    padding: '0.75rem 1.5rem', border: 'none', borderBottom: stateTab === 'central' ? '3px solid #0B2447' : '3px solid transparent',
                                    background: 'none', fontFamily: 'inherit', fontWeight: stateTab === 'central' ? 700 : 400,
                                    color: stateTab === 'central' ? '#0B2447' : '#64748b', cursor: 'pointer', fontSize: '0.88rem'
                                }}
                            >
                                🇮🇳 Central Govt
                            </button>
                        </div>
                    </div>
                )}

                {/* View toggle: Jobs / Saved */}
                <div style={{ background: '#F4F6FB', borderBottom: '1px solid #E2E8F0' }}>
                    <div className="container" style={{ display: 'flex', gap: '0.5rem', padding: '0.6rem 0', alignItems: 'center', flexWrap: 'wrap' }}>
                        {/* Jobs / Saved toggle */}
                        <div style={{ display: 'flex', background: 'white', border: '1px solid #E2E8F0', borderRadius: '100px', padding: '0.2rem', gap: '0' }}>
                            {[{ key: 'jobs', label: '🏛️ Jobs' }, { key: 'saved', label: `⭐ Saved${savedIds.length > 0 ? ' (' + savedIds.length + ')' : ''}` }].map(v => (
                                <button
                                    key={v.key}
                                    onClick={() => setActiveView(v.key)}
                                    style={{
                                        padding: '0.3rem 0.9rem', border: 'none', borderRadius: '100px', fontFamily: 'inherit',
                                        background: activeView === v.key ? '#0B2447' : 'transparent',
                                        color: activeView === v.key ? 'white' : '#64748b',
                                        fontSize: '0.78rem', cursor: 'pointer', fontWeight: activeView === v.key ? 600 : 400, transition: '0.15s'
                                    }}
                                >
                                    {v.label}
                                </button>
                            ))}
                        </div>

                        {/* Category pills (only in jobs view) */}
                        {activeView === 'jobs' && CATEGORIES.map(c => (
                            <button
                                key={c.key}
                                className={`pill${category === c.key ? ' active' : ''}`}
                                style={{ fontSize: '0.75rem' }}
                                onClick={() => handleCategory(c.key)}
                            >
                                {c.label}
                            </button>
                        ))}

                        {activeView === 'jobs' && (
                            <div style={{ display: 'flex', gap: '0.4rem', alignItems: 'center', marginLeft: 'auto' }}>
                                <select
                                    value={noticeType}
                                    onChange={e => { setNoticeType(e.target.value); setPage(0); }}
                                    className="filter-select"
                                    style={{ fontSize: '0.75rem' }}
                                >
                                    <option value="">All Types</option>
                                    <option value="RECRUITMENT">Recruitment</option>
                                    <option value="APPRENTICESHIP">Apprenticeship</option>
                                    <option value="EXAM_ADMIT_CARD">Admit Card</option>
                                    <option value="RESULT">Result</option>
                                </select>
                                <select
                                    value={sortBy}
                                    onChange={e => { setSortBy(e.target.value); setPage(0); }}
                                    className="filter-select"
                                    style={{ fontSize: '0.75rem' }}
                                >
                                    <option value="newest">🆕 Newest</option>
                                    <option value="deadline">⏳ Deadline</option>
                                    <option value="fetched">🔄 Recent</option>
                                </select>
                                <button onClick={handleRefresh} disabled={refreshing} className="pill" style={{ fontSize: '0.72rem', whiteSpace: 'nowrap' }}>
                                    {refreshing ? '⟳' : '🔄'}
                                </button>
                            </div>
                        )}
                    </div>
                </div>

                <div className="container" style={{ padding: '1rem 1rem' }}>
                    {/* Count bar */}
                    {activeView === 'jobs' && (
                        <p style={{ fontSize: '0.78rem', color: '#94A3B8', marginBottom: '0.75rem' }}>
                            {totalElements > 0 ? `${totalElements.toLocaleString('en-IN')} notices` : ''}
                            {userState && stateTab === 'state' ? ` — ${userState} State Govt` : userState && stateTab === 'central' ? ' — Central Govt' : ''}
                        </p>
                    )}

                    {activeView === 'jobs' ? (
                        <>
                            <NoticeGrid notices={notices} loading={loading} isSaved={isSaved} toggleSave={toggleSave} />
                            {!loading && page < totalPages - 1 && <div ref={sentinelRef} style={{ height: 40 }} />}
                        </>
                    ) : (
                        /* Saved view */
                        savedNotices.length === 0 ? (
                            <div className="empty-state">
                                <div className="emoji">⭐</div>
                                <h3>No saved notices yet</h3>
                                <p>Tap ☆ on any job notice to save it here.</p>
                            </div>
                        ) : (
                            <div className="notices-grid">
                                {savedNotices.map(n => (
                                    <NoticeCard key={n.id} notice={n} isSaved={isSaved} toggleSave={toggleSave} />
                                ))}
                            </div>
                        )
                    )}
                </div>
            </main>

            <Footer />

            {toast && <div className="toast" role="status">{toast}</div>}
        </>
    );
}
