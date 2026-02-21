const CAT_ACCENTS = {
    BANK: '#1D6F42', SSC: '#1E40AF', RAILWAYS: '#C2410C',
    UPSC: '#6D28D9', PSU: '#0E7490', STATE: '#BE185D',
    MEDICAL: '#0F766E', DEFENCE: '#065F46', OTHERS: '#374151',
};

const CAT_ICONS = {
    BANK: '🏦', SSC: '📋', RAILWAYS: '🚂', UPSC: '🎖️',
    PSU: '🏭', STATE: '🗺️', MEDICAL: '🏥', DEFENCE: '🛡️', OTHERS: '📌',
};

function formatDate(dateStr) {
    if (!dateStr) return null;
    const d = new Date(dateStr);
    if (isNaN(d.getTime())) return null;
    return d.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
}

function isExpired(dateStr) {
    if (!dateStr) return false;
    return new Date(dateStr) < new Date();
}

function isNew(dateStr) {
    if (!dateStr) return false;
    const diff = (new Date() - new Date(dateStr)) / (1000 * 60 * 60 * 24);
    return diff <= 2;
}


export default function NoticeCard({ notice }) {
    const accent = CAT_ACCENTS[notice.category] || '#374151';
    const icon = CAT_ICONS[notice.category] || '📌';
    const expired = isExpired(notice.lastDate);
    const fresh = isNew(notice.publishedDate);

    // Show publishedDate, fallback to fetchedAt if not scraped
    const postedDate = notice.publishedDate || notice.fetchedAt;
    const postedLabel = formatDate(postedDate);

    return (
        <article
            className="notice-card"
            style={{ '--card-accent': accent }}
            aria-label={notice.title}
        >
            {/* Header: category badge + NEW + state */}
            <div className="card-header">
                <div style={{ display: 'flex', gap: '0.4rem', flexWrap: 'wrap', alignItems: 'center' }}>
                    <span className={`category-badge ${notice.category}`}>
                        {icon} {notice.category}
                    </span>
                    {notice.noticeType && (
                        <span style={{
                            background: 'var(--color-surface-2)',
                            color: 'var(--color-text-secondary)',
                            fontSize: '0.62rem',
                            fontWeight: 600,
                            padding: '0.15rem 0.45rem',
                            borderRadius: '4px',
                            border: '1px solid var(--color-border)'
                        }}>
                            {notice.noticeType.replace('_', ' ')}
                        </span>
                    )}
                    {fresh && (
                        <span style={{
                            background: '#FEF3C7', color: '#92400E', fontSize: '0.62rem',
                            fontWeight: 700, padding: '0.15rem 0.45rem', borderRadius: '4px',
                            textTransform: 'uppercase', letterSpacing: '0.06em'
                        }}>NEW</span>
                    )}
                </div>
                <span className="state-badge" title={notice.state}>
                    📍 {notice.state}
                </span>
            </div>

            {/* Title */}
            <h2 className="card-title" title={notice.title} style={{ fontSize: '1rem', letterSpacing: '-0.01em', fontWeight: 700 }}>
                {notice.title}
            </h2>

            {/* Date meta */}
            <div className="card-meta">
                {/* Published date — only when scraped from source */}
                {postedLabel && (
                    <span className="meta-item" title="Date published on official source">
                        <span className="icon">📅</span>
                        {postedLabel}
                    </span>
                )}

                {/* Last / deadline date */}
                {notice.lastDate && (
                    <span className={`meta-item deadline${expired ? ' expired' : ''}`}>
                        <span className="icon">{expired ? '⏰' : '⏳'}</span>
                        Last date: {formatDate(notice.lastDate)}
                        {expired && <span style={{ color: '#DC2626', fontWeight: 600 }}> · Expired</span>}
                    </span>
                )}
            </div>

            {/* Source */}
            <div className="card-source">
                <div className="source-icon">🏛️</div>
                <span className="source-name" title={notice.sourceName}>
                    {notice.sourceName}
                </span>
            </div>

            {/* Actions */}
            <div className="card-actions">
                <a
                    href={notice.applyUrl || notice.sourceUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="btn-apply"
                    aria-label={`View ${notice.title} on official website`}
                >
                    View on Official Site ↗
                </a>
                {notice.applyUrl && notice.applyUrl !== notice.sourceUrl && (
                    <a
                        href={notice.sourceUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="btn-official"
                        aria-label={`Visit ${notice.sourceName} official website`}
                        title="Visit Official Website"
                    >
                        🌐
                    </a>
                )}
            </div>
        </article>
    );
}
