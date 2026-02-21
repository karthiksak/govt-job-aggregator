const TABS = [
    { key: 'jobs', label: '🏛️ All Jobs' },
    { key: 'engineering', label: '⚙️ Engineering' },
    { key: 'saved', label: '⭐ Saved' },
];

export default function Header({ activeTab, onTabChange, newCount, savedCount }) {
    return (
        <header className="site-header">
            <div className="container header-inner">
                <div className="logo-block">
                    <span className="logo-badge">IN</span>
                    <div>
                        <div className="logo-text">GovtJobs.in</div>
                        <div className="logo-sub">Official Job Notification Aggregator</div>
                    </div>
                </div>

                {/* Tab navigation */}
                <nav className="header-tabs" aria-label="Main navigation">
                    {TABS.map(tab => {
                        const badge = tab.key === 'saved' && savedCount > 0 ? savedCount
                            : tab.key === 'jobs' && newCount > 0 ? null : null;
                        return (
                            <button
                                key={tab.key}
                                className={`header-tab${activeTab === tab.key ? ' active' : ''}`}
                                onClick={() => onTabChange(tab.key)}
                                aria-current={activeTab === tab.key ? 'page' : undefined}
                            >
                                {tab.label}
                                {tab.key === 'saved' && savedCount > 0 && (
                                    <span className="tab-badge">{savedCount}</span>
                                )}
                            </button>
                        );
                    })}
                </nav>

                <div className="header-meta">
                    <span className="live-dot" aria-hidden="true" />
                    <span>Updated every 6 hours</span>
                    {newCount > 0 && (
                        <span style={{
                            background: '#FF9933', color: 'white', fontSize: '0.65rem',
                            fontWeight: 700, padding: '0.15rem 0.5rem', borderRadius: '100px',
                        }}>
                            🆕 {newCount} new
                        </span>
                    )}
                </div>
            </div>
        </header>
    );
}
