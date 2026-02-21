import { useState, useEffect, useCallback } from 'react';
import { Helmet } from 'react-helmet-async';
import Header from './components/Header.jsx';
import FilterBar from './components/FilterBar.jsx';
import NoticeList from './components/NoticeList.jsx';
import Footer from './components/Footer.jsx';
import { fetchNotices, fetchStates, triggerRefresh } from './api/notices.js';

const DEFAULT_FILTERS = {
    category: '',
    state: '',
    period: 'all',
    sortBy: 'newest',
    page: 0,
    size: 18,
};


export default function App() {
    const [filters, setFilters] = useState(DEFAULT_FILTERS);
    const [notices, setNotices] = useState([]);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const [states, setStates] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [toast, setToast] = useState(null);
    const [refreshing, setRefreshing] = useState(false);

    const showToast = (msg) => {
        setToast(msg);
        setTimeout(() => setToast(null), 3000);
    };

    const loadNotices = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const params = {
                ...(filters.category && { category: filters.category }),
                ...(filters.state && { state: filters.state }),
                period: filters.period,
                sortBy: filters.sortBy || 'newest',
                page: filters.page,
                size: filters.size,
            };

            const data = await fetchNotices(params);
            setNotices(data.content || []);
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
        loadNotices();
    }, [loadNotices]);

    useEffect(() => {
        fetchStates()
            .then(s => setStates(s || []))
            .catch(() => { });
    }, []);

    const handleFilterChange = (updates) => {
        setFilters(prev => ({ ...prev, ...updates }));
    };

    const handleRefresh = async () => {
        if (refreshing) return;
        setRefreshing(true);
        try {
            const result = await triggerRefresh();
            showToast(`✅ Refreshed! ${result.saved} new notices added.`);
            loadNotices();
        } catch {
            showToast('❌ Refresh failed. Try again later.');
        } finally {
            setRefreshing(false);
        }
    };

    const pageTitle = filters.category
        ? `${filters.category} Govt Jobs India — GovtJobs.in`
        : 'Latest Sarkari Naukri — GovtJobs.in';

    return (
        <>
            <Helmet>
                <title>{pageTitle}</title>
                <meta name="description" content={
                    `Latest ${filters.category || 'government'} job notifications from Indian Govt, SSC, Banks, RRB, UPSC, PSU${filters.state ? ', ' + filters.state : ''}. Updated every 6 hours.`
                } />
            </Helmet>

            <Header />

            {/* Hero */}
            <section className="hero" aria-labelledby="hero-heading">
                <div className="container">
                    <h2 id="hero-heading">🇮🇳 Latest Government Job Notifications</h2>
                    <p>SSC · Bank · RRB · UPSC · PSU · State Govt — All official sources, one place.</p>
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
                </div>
            </section>

            <FilterBar
                filters={filters}
                onFilterChange={handleFilterChange}
                states={states}
                totalCount={totalElements}
            />

            <main className="notices-section" id="notices">
                <div className="container">
                    {/* Toolbar */}
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem', flexWrap: 'wrap', gap: '0.5rem' }}>
                        <p style={{ fontSize: '0.82rem', color: 'var(--color-text-muted)' }}>
                            Showing {notices.length} of {totalElements.toLocaleString('en-IN')} notices
                            {filters.category && ` · ${filters.category}`}
                            {filters.state && ` · ${filters.state}`}
                        </p>
                        <button
                            onClick={handleRefresh}
                            disabled={refreshing}
                            style={{
                                padding: '0.4rem 1rem', border: '1.5px solid var(--color-border)',
                                borderRadius: '100px', background: 'white', color: 'var(--color-text-secondary)',
                                fontSize: '0.78rem', cursor: 'pointer', fontFamily: 'inherit',
                                display: 'flex', alignItems: 'center', gap: '0.4rem',
                                transition: 'all 0.2s',
                                opacity: refreshing ? 0.6 : 1,
                            }}
                            aria-label="Refresh notices from all sources"
                        >
                            {refreshing ? '⟳ Refreshing...' : '🔄 Refresh Now'}
                        </button>
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
                    />
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
