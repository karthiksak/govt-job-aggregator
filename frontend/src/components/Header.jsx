export default function Header() {
    return (
        <header className="header" role="banner">
            <div className="container">
                <div className="header-inner">
                    <div className="header-brand">
                        <div className="header-logo" aria-hidden="true">🇮🇳</div>
                        <div className="header-title">
                            <h1>GovtJobs.in</h1>
                            <p>Official Job Notification Aggregator</p>
                        </div>
                    </div>
                    <div className="header-badge">
                        <span className="pulse-dot" aria-hidden="true" />
                        Updated every 6 hours
                    </div>
                </div>
            </div>
        </header>
    );
}
