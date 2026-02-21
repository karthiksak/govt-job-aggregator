import { useState, useEffect, useCallback } from 'react';
import { Helmet } from 'react-helmet-async';
import Header from './components/Header.jsx';
import FilterBar from './components/FilterBar.jsx';
import NoticeList from './components/NoticeList.jsx';
import EngineeringTab from './components/EngineeringTab.jsx';
import SavedNotices from './components/SavedNotices.jsx';
import DualStateView from './components/DualStateView.jsx';
import StateOnboarding, { useStateOnboarding } from './components/StateOnboarding.jsx';
import Footer from './components/Footer.jsx';
import useSavedNotices from './hooks/useSavedNotices.js';
import { fetchNotices, fetchStates, triggerRefresh, fetchNewCount } from './api/notices.js';

const DEFAULT_FILTERS = {
    category: '',
    state: '',
    period: 'all',
    sortBy: 'newest',
    noticeType: '',
    showNewOnly: false,
    page: 0,
    size: 18,
};

export default function App() {
    const [activeTab, setActiveTab] = useState('jobs'); // 'jobs' | 'engineering' | 'saved'
    const [filters, setFilters] = useState(DEFAULT_FILTERS);
    const [notices, setNotices] = useState([]);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const [states, setStates] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [toast, setToast] = useState(null);
    const [refreshing, setRefreshing] = useState(false);
    const [newCount, setNewCount] = useState(0);
    const { selectedState, showOnboarding, completeOnboarding, resetState } = useStateOnboarding();
    const { savedIds, savedNotices, isSaved, toggleSave } = useSavedNotices();

    const showToast = (msg) => {
        setToast(msg);
        setTimeout(() => setToast(null), 3000);
    };

    useEffect(() => {
        fetchNewCount().then(c => setNewCount(c || 0)).catch(() => { });
        fetchStates().then(s => setStates(s || [])).catch(() => { });
    }, []);

    const loadNotices = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const params = {
                ...(filters.category && { category: filters.category }),
                ...(filters.state && { state: filters.state }),
                ...(filters.noticeType && { noticeType: filters.noticeType }),
                period: filters.period,
                sortBy: filters.sortBy || 'newest',
                page: filters.page,
                size: filters.size,
            };
            const data = await fetchNotices(params);
            let content = data.content || [];
            if (filters.showNewOnly) content = content.filter(n => n.new);
            if (filters.page === 0) {
                setNotices(content);
            } else {
                setNotices(prev => {
                    const newItems = content.filter(newN => !prev.some(e => e.id === newN.id));
                    return [...prev, ...newItems];
                });
            }
            setTotalPages(data.totalPages || 0);
            setTotalElements(data.totalElements || 0);
        } catch {
            setError('Unable to load notices. Please ensure the backend is running.');
            setNotices([]);
        } finally {
            setLoading(false);
        }
    }, [filters]);

    useEffect(() => {
        if (activeTab === 'jobs') loadNotices();
    }, [loadNotices, activeTab]);

    const handleFilterChange = (updates) => {
        setFilters(prev => ({ ...prev, ...updates, page: 'page' in updates ? updates.page : 0 }));
    };

    const handleRefresh = async () => {
        if (refreshing) return;
        setRefreshing(true);
        try {
            const result = await triggerRefresh();
            showToast(`✅ Refreshed! ${result.saved} new notices added.`);
            fetchNewCount().then(c => setNewCount(c || 0)).catch(() => { });
            loadNotices();
        } catch {
            showToast('❌ Refresh failed. Try again later.');
        } finally {
            setRefreshing(false);
        }
    };

    const handleTabChange = (tab) => {
        setActiveTab(tab);
        if (tab === 'jobs') setFilters(DEFAULT_FILTERS);
    };

    // SEO titles per context
    const pageTitle = activeTab === 'engineering'
        ? 'Engineering Govt Jobs — JE, PSU, Apprenticeship | GovtJobs.in'
        : activeTab === 'saved' ? 'Saved Notices | GovtJobs.in'
            : selectedState
                ? `${selectedState} & Central Govt Jobs 2026 | GovtJobs.in`
                : filters.state ? `${filters.state} Sarkari Naukri — GovtJobs.in`
                    : 'Latest Sarkari Naukri 2026 — GovtJobs.in';

    const metaDescription = selectedState
        ? `Latest ${selectedState} state government and central government job notifications — SSC, UPSC, Banks, Railways, PSU. Updated every 6 hours.`
        : `Latest government job notifications from Indian Govt, SSC, Banks, RRB, UPSC, PSU. Updated every 6 hours.`;

    // Show onboarding if not yet answered
    if (showOnboarding) {
        return <StateOnboarding onComplete={completeOnboarding} />;
    }

    return (
        <>
            <Helmet>
                <title>{pageTitle}</title>
                <meta name="description" content={metaDescription} />
                <meta property="og:title" content={pageTitle} />
            </Helmet>

            <Header
                activeTab={activeTab}
                onTabChange={handleTabChange}
                newCount={newCount}
                savedCount={savedIds.length}
                selectedState={selectedState}
                onChangeState={resetState}
            />

            {/* Hero */}
            <section className="hero" aria-labelledby="hero-heading">
                <div className="container">
                    <h1 id="hero-heading">
                        {activeTab === 'engineering' ? '⚙️ Engineering Govt Jobs India'
                            : activeTab === 'saved' ? '⭐ Your Saved Notices'
                                : selectedState ? `📍 ${selectedState} + Central Govt Jobs`
                                    : '🇮🇳 Latest Government Job Notifications'}
                    </h1>
                    <p>
                        {selectedState
                            ? `${selectedState} State Govt · SSC · Bank · RRB · UPSC · PSU — One page, two views.`
                            : 'SSC · Bank · RRB · UPSC · PSU · State Govt — All official sources, one place.'}
                    </p>
                    {activeTab === 'jobs' && !selectedState && (
                        <div className="hero-stats">
                            <div className="hero-stat"><div className="num">{totalElements.toLocaleString('en-IN')}</div><div className="lbl">Total Notices</div></div>
                            <div className="hero-stat"><div className="num">8+</div><div className="lbl">Official Sources</div></div>
                            <div className="hero-stat"><div className="num">6hrs</div><div className="lbl">Update Frequency</div></div>
                        </div>
                    )}
                </div>
            </section>

            {/* Only show FilterBar in 'All Jobs' mode without a selected state */}
            {activeTab === 'jobs' && !selectedState && (
                <FilterBar
                    filters={filters}
                    onFilterChange={handleFilterChange}
                    states={states}
                    totalCount={totalElements}
                />
            )}

            {/* Simplified filter bar for the dual-state view */}
            {activeTab === 'jobs' && selectedState && (
                <div className="filter-section" style={{ padding: '0.6rem 0' }}>
                    <div className="container" style={{ display: 'flex', gap: '0.75rem', alignItems: 'center', flexWrap: 'wrap' }}>
                        <span style={{ fontSize: '0.78rem', color: 'var(--color-text-muted)', fontWeight: 600 }}>Sort:</span>
                        {['newest', 'deadline', 'fetched'].map(s => (
                            <button
                                key={s}
                                className={`pill period-pill${filters.sortBy === s ? ' active' : ''}`}
                                style={{ fontSize: '0.75rem' }}
                                onClick={() => handleFilterChange({ sortBy: s })}
                            >
                                {s === 'newest' ? '🆕 Newest' : s === 'deadline' ? '⏳ Deadline' : '🔄 Recent'}
                            </button>
                        ))}
                        <span style={{ fontSize: '0.78rem', color: 'var(--color-text-muted)', marginLeft: '0.5rem', fontWeight: 600 }}>Type:</span>
                        <select
                            className="filter-select"
                            value={filters.noticeType || ''}
                            onChange={e => handleFilterChange({ noticeType: e.target.value })}
                            style={{ fontSize: '0.78rem' }}
                        >
                            <option value="">All Types</option>
                            <option value="RECRUITMENT">Recruitment</option>
                            <option value="APPRENTICESHIP">Apprenticeship</option>
                            <option value="EXAM_ADMIT_CARD">Exam/Admit Card</option>
                            <option value="RESULT">Result</option>
                        </select>
                        <button
                            onClick={handleRefresh}
                            disabled={refreshing}
                            className="pill"
                            style={{ marginLeft: 'auto', fontSize: '0.75rem' }}
                        >
                            {refreshing ? '⟳ Refreshing...' : '🔄 Refresh'}
                        </button>
                    </div>
                </div>
            )}

            <main className="notices-section" id="notices">
                <div className="container">
                    {activeTab === 'jobs' && selectedState && (
                        <DualStateView
                            selectedState={selectedState}
                            filters={filters}
                            isSaved={isSaved}
                            toggleSave={toggleSave}
                        />
                    )}

                    {activeTab === 'jobs' && !selectedState && (
                        <>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem', flexWrap: 'wrap', gap: '0.5rem' }}>
                                <p style={{ fontSize: '0.82rem', color: 'var(--color-text-muted)' }}>
                                    Showing {notices.length} of {totalElements.toLocaleString('en-IN')} notices
                                </p>
                                <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
                                    {newCount > 0 && (
                                        <span style={{ background: '#FF9933', color: 'white', fontSize: '0.7rem', fontWeight: 700, padding: '0.2rem 0.5rem', borderRadius: '100px' }}>
                                            🆕 {newCount} new
                                        </span>
                                    )}
                                    <button
                                        onClick={() => handleFilterChange({ showNewOnly: !filters.showNewOnly, page: 0 })}
                                        className={`pill${filters.showNewOnly ? ' active' : ''}`}
                                        style={{ fontSize: '0.75rem' }}
                                        aria-pressed={filters.showNewOnly}
                                    >
                                        🆕 New Only
                                    </button>
                                    <button onClick={handleRefresh} disabled={refreshing} className="pill" style={{ fontSize: '0.75rem' }}>
                                        {refreshing ? '⟳ Refreshing...' : '🔄 Refresh Now'}
                                    </button>
                                </div>
                            </div>
                            {error && (
                                <div role="alert" style={{ background: '#FEF2F2', border: '1px solid #FCA5A5', borderRadius: '10px', padding: '1rem', marginBottom: '1rem', color: '#991B1B', fontSize: '0.875rem' }}>
                                    ⚠️ {error}
                                </div>
                            )}
                            <NoticeList notices={notices} loading={loading} page={filters.page} totalPages={totalPages} onPageChange={(p) => handleFilterChange({ page: p })} isSaved={isSaved} toggleSave={toggleSave} />
                        </>
                    )}

                    {activeTab === 'engineering' && <EngineeringTab isSaved={isSaved} toggleSave={toggleSave} />}
                    {activeTab === 'saved' && <SavedNotices notices={savedNotices} isSaved={isSaved} toggleSave={toggleSave} />}
                </div>
            </main>

            <Footer />

            {toast && <div className="toast" role="status" aria-live="polite">{toast}</div>}
        </>
    );
}
