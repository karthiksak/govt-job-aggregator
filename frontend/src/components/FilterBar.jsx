const CATEGORIES = [
    { key: 'ALL', label: '🔍 All Jobs' },
    { key: 'BANK', label: '🏦 Bank' },
    { key: 'SSC', label: '📋 SSC' },
    { key: 'RAILWAYS', label: '🚂 Railways/RRB' },
    { key: 'UPSC', label: '🎖️ UPSC' },
    { key: 'PSU', label: '🏭 PSU' },
    { key: 'STATE', label: '🗺️ State Govt' },
    { key: 'MEDICAL', label: '🏥 Medical' },
    { key: 'DEFENCE', label: '🛡️ Defence' },
    { key: 'OTHERS', label: '📌 Others' },
];

const NOTICE_TYPES = [
    { key: '', label: '📑 All Types' },
    { key: 'RECRUITMENT', label: '💼 Recruitment' },
    { key: 'APPRENTICESHIP', label: '🔧 Apprenticeship' },
    { key: 'EXAM_ADMIT_CARD', label: '🎟️ Exam/Admit Card' },
    { key: 'RESULT', label: '🏆 Result' },
    { key: 'CALENDAR', label: '📆 Calendar' },
    { key: 'GENERAL_INFO', label: 'ℹ️ General Info' },
];

const PERIODS = [
    { key: 'all', label: '📅 All Time' },
    { key: 'today', label: '🔴 Today' },
    { key: 'this_week', label: '📆 This Week' },
];

const SORT_OPTIONS = [
    { key: 'newest', label: '🆕 Newest First' },
    { key: 'deadline', label: '⏳ Deadline Soon' },
    { key: 'fetched', label: '🔄 Recently Added' },
];

export default function FilterBar({ filters, onFilterChange, states, totalCount }) {
    const handleCategory = (cat) => {
        onFilterChange({ category: cat === 'ALL' ? '' : cat, page: 0 });
    };
    const handlePeriod = (period) => {
        onFilterChange({ period, page: 0 });
    };
    const handleNoticeType = (e) => {
        onFilterChange({ noticeType: e.target.value, page: 0 });
    };
    const handleState = (e) => {
        onFilterChange({ state: e.target.value, page: 0 });
    };
    const handleSort = (e) => {
        onFilterChange({ sortBy: e.target.value, page: 0 });
    };

    return (
        <section className="filter-section" aria-label="Job filters">
            <div className="container">
                <div className="filter-row">
                    {/* Category pills */}
                    <span className="filter-label">Category</span>
                    <div className="filter-pills" role="group" aria-label="Filter by category">
                        {CATEGORIES.map(c => (
                            <button
                                key={c.key}
                                className={`pill ${(filters.category === c.key || (c.key === 'ALL' && !filters.category)) ? 'active' : ''}`}
                                onClick={() => handleCategory(c.key)}
                                aria-pressed={(filters.category === c.key || (c.key === 'ALL' && !filters.category))}
                            >
                                {c.label}
                            </button>
                        ))}
                    </div>

                    <div className="filter-divider" aria-hidden="true" />

                    {/* Time period pills */}
                    <div className="filter-pills" role="group" aria-label="Filter by time">
                        {PERIODS.map(p => (
                            <button
                                key={p.key}
                                className={`pill period-pill ${filters.period === p.key ? 'active' : ''}`}
                                onClick={() => handlePeriod(p.key)}
                                aria-pressed={filters.period === p.key}
                            >
                                {p.label}
                            </button>
                        ))}
                    </div>

                    <div className="filter-divider" aria-hidden="true" />

                    {/* State dropdown */}
                    <select
                        className="filter-select"
                        value={filters.state || ''}
                        onChange={handleState}
                        aria-label="Filter by state"
                    >
                        <option value="">🗾 All States</option>
                        <option value="Central">🇮🇳 Central Govt</option>
                        <option value="Tamil Nadu">Tamil Nadu</option>
                        {states
                            .filter(s => s !== 'Central' && s !== 'Tamil Nadu')
                            .map(s => <option key={s} value={s}>{s}</option>)}
                    </select>

                    {/* Notice Type dropdown */}
                    <select
                        className="filter-select"
                        value={filters.noticeType || ''}
                        onChange={handleNoticeType}
                        aria-label="Filter by notice type"
                    >
                        {NOTICE_TYPES.map(n => (
                            <option key={n.key} value={n.key}>{n.label}</option>
                        ))}
                    </select>

                    {/* Sort dropdown */}
                    <select
                        className="filter-select"
                        value={filters.sortBy || 'newest'}
                        onChange={handleSort}
                        aria-label="Sort notices"
                    >
                        {SORT_OPTIONS.map(s => (
                            <option key={s.key} value={s.key}>{s.label}</option>
                        ))}
                    </select>

                    {totalCount !== undefined && (
                        <span className="filter-count" aria-live="polite">
                            {totalCount.toLocaleString('en-IN')} notices
                        </span>
                    )}
                </div>
            </div>
        </section>
    );
}
