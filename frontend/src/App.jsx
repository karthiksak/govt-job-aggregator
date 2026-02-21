import { useState, useEffect, useCallback, useRef } from 'react';
import { Helmet } from 'react-helmet-async';
import Header from './components/Header.jsx';
import FilterBar from './components/FilterBar.jsx';
import NoticeList from './components/NoticeList.jsx';
import EngineeringTab from './components/EngineeringTab.jsx';
import SavedNotices from './components/SavedNotices.jsx';
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

const LAST_VISIT_KEY = 'govtjobs_last_visit';

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
    const { savedIds, savedNotices, isSaved, toggleSave } = useSavedNotices();

    const showToast = (msg) => {
        setToast(msg);
        setTimeout(() => setToast(null), 3000);
    };

    // On mount: record last visit & fetch new count
    useEffect(() => {
        const last = localStorage.getItem(LAST_VISIT_KEY);
        localStorage.setItem(LAST_VISIT_KEY, new Date().toISOString());
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

            // Client-side "show new only" filter based on backend isNew flag
            if (filters.showNewOnly) {
                content = content.filter(n => n.new);
            }

            if (filters.page === 0) {
                setNotices(content);
            } else {
                setNotices(prev => {
                    const newNotices = content.filter(
                        newN => !prev.some(existingN => existingN.id === newN.id)
                    );
                    return [...prev, ...newNotices];
                });
            }
            setTotalPages(data.totalPages || 0);
            setTotalElements(data.totalElements || 0);
        } catch (err) {
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
        if (tab === 'jobs') {
            setFilters(DEFAULT_FILTERS);
        }
    };

    // SEO
    const pageTitle = activeTab === 'engineering'
        ? 'Engineering Govt Jobs — JE, PSU, Apprenticeship | GovtJobs.in'
        : activeTab === 'saved'
            ? 'Saved Notices | GovtJobs.in'
            : filters.category
                ? `${filters.category} Govt Jobs India — GovtJobs.in`
                : filters.state
                    ? `${filters.state} Sarkari Naukri — GovtJobs.in`
                    : 'Latest Sarkari Naukri 2026 — GovtJobs.in';

    const metaDescription = activeTab === 'engineering'
        ? 'Engineering government jobs — JE, GET, PSU Recruitment, Apprenticeship for B.E/B.Tech, Diploma, ITI graduates. Civil, Mechanical, Electrical, ECE, CSE branches. Updated every 6 hours.'
        : `Latest ${filters.category || 'government'} job notifications from Indian Govt, SSC, Banks, RRB, UPSC, PSU${filters.state ? ', ' + filters.state : ''}. Updated every 6 hours.`;

    return (
        <>
            <Helmet>
                <title>{pageTitle}</title>
                <meta name="description" content={metaDescription} />
                <meta property="og:title" content={pageTitle} />
                <meta property="og:description" content={metaDescription} />
            </Helmet>

            <Header
                activeTab={activeTab}
                onTabChange={handleTabChange}
                newCount={newCount}
                savedCount={savedIds.length}
            />

            {/* Hero */}
            <section className="hero" aria-labelledby="hero-heading">
                <div className="container">
                    <h1 id="hero-heading">
                        {activeTab === 'engineering'
                            ? '⚙️ Engineering Govt Jobs India'
                            : activeTab === 'saved'
                                ? '⭐ Your Saved Notices'
                                : '🇮🇳 Latest Government Job Notifications'}
                    </h1>
                    {activeTab === 'jobs' && (
                        <p>SSC · Bank · RRB · UPSC · PSU · State Govt — All official sources, one place.</p>
                    )}
                    {activeTab === 'engineering' && (
                        <p>JE · GET · PSU · Apprenticeship — B.E/B.Tech, Diploma & ITI — All branches covered.</p>
                    )}
                    {activeTab === 'jobs' && (
                        <div className="hero-stats">
                            <div className="hero-stat">
                                <div className="num">{totalElements.toLocaleString('en-IN')}</div>
                                <div className="lbl">Total Notices</div>
                            </div>
                            <div className="hero-stat">
                                <div className="num">8+</div>
                                <div className="lbl">Official Sources</div>
                            </div>
                            <div className="hero-stat">
                                <div className="num">6hrs</div>
                                <div className="lbl">Update Frequency</div>
                            </div>
                        </div>
                    )}
                </div>
            </section>

            {activeTab === 'jobs' && (
                <FilterBar
                    filters={filters}
                    onFilterChange={handleFilterChange}
                    states={states}
                    totalCount={totalElements}
                />
            )}

            <main className="notices-section" id="notices">
                <div className="container">
                    {activeTab === 'jobs' && (
                        <>
                            {/* Toolbar */}
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem', flexWrap: 'wrap', gap: '0.5rem' }}>
                                <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center', flexWrap: 'wrap' }}>
                                    <p style={{ fontSize: '0.82rem', color: 'var(--color-text-muted)' }}>
                                        Showing {notices.length} of {totalElements.toLocaleString('en-IN')} notices
                                        {filters.category && ` · ${filters.category}`}
                                        {filters.state && ` · ${filters.state}`}
                                    </p>
                                    {newCount > 0 && (
                                        <span style={{
                                            background: '#FF9933', color: 'white',
                                            fontSize: '0.7rem', fontWeight: 700,
                                            padding: '0.2rem 0.5rem', borderRadius: '100px'
                                        }}>
                                            🆕 {newCount} new in last 24h
                                        </span>
                                    )}
                                </div>
                                <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
                                    <button
                                        onClick={() => handleFilterChange({ showNewOnly: !filters.showNewOnly, page: 0 })}
                                        style={{
                                            padding: '0.35rem 0.8rem',
                                            border: `1.5px solid ${filters.showNewOnly ? '#FF9933' : 'var(--color-border)'}`,
                                            borderRadius: '100px',
                                            background: filters.showNewOnly ? '#FFF7ED' : 'white',
                                            color: filters.showNewOnly ? '#92400E' : 'var(--color-text-secondary)',
                                            fontSize: '0.75rem', cursor: 'pointer', fontFamily: 'inherit',
                                            fontWeight: filters.showNewOnly ? 600 : 400,
                                        }}
                                        aria-pressed={filters.showNewOnly}
                                    >
                                        🆕 New Only
                                    </button>
                                    <button
                                        onClick={handleRefresh}
                                        disabled={refreshing}
                                        style={{
                                            padding: '0.4rem 1rem', border: '1.5px solid var(--color-border)',
                                            borderRadius: '100px', background: 'white', color: 'var(--color-text-secondary)',
                                            fontSize: '0.78rem', cursor: 'pointer', fontFamily: 'inherit',
                                            display: 'flex', alignItems: 'center', gap: '0.4rem',
                                            opacity: refreshing ? 0.6 : 1,
                                        }}
                                        aria-label="Refresh notices from all sources"
                                    >
                                        {refreshing ? '⟳ Refreshing...' : '🔄 Refresh Now'}
                                    </button>
                                </div>
                            </div>

                            {error && (
                                <div role="alert" style={{
                                    background: '#FEF2F2', border: '1px solid #FCA5A5', borderRadius: '10px',
                                    padding: '1rem 1.25rem', marginBottom: '1rem', color: '#991B1B', fontSize: '0.875rem'
                                }}>
                                    ⚠️ {error}
                                </div>
                            )}

                            <NoticeList
                                notices={notices}
                                loading={loading}
                                page={filters.page}
                                totalPages={totalPages}
                                onPageChange={(p) => handleFilterChange({ page: p })}
                                isSaved={isSaved}
                                toggleSave={toggleSave}
                            />
                        </>
                    )}

                    {activeTab === 'engineering' && (
                        <EngineeringTab isSaved={isSaved} toggleSave={toggleSave} />
                    )}

                    {activeTab === 'saved' && (
                        <SavedNotices notices={savedNotices} isSaved={isSaved} toggleSave={toggleSave} />
                    )}
                </div>
            </main>

            <Footer />

            {toast && (
                <div className="toast" role="status" aria-live="polite">
                    {toast}
                </div>
            )}
        </>
    );
}
