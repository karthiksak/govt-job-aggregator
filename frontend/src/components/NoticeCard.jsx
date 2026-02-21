const CAT_ACCENTS = {
    BANK: '#1D6F42', SSC: '#1E40AF', RAILWAYS: '#C2410C',
    UPSC: '#6D28D9', PSU: '#0E7490', STATE: '#BE185D',
    MEDICAL: '#0F766E', DEFENCE: '#065F46', OTHERS: '#374151',
};

const CAT_ICONS = {
    BANK: '🏦', SSC: '📋', RAILWAYS: '🚂', UPSC: '🎖️',
    PSU: '🏭', STATE: '🗺️', MEDICAL: '🏥', DEFENCE: '🛡️', OTHERS: '📌',
};

const NOTICE_TYPE_LABELS = {
    RECRUITMENT: 'Recruitment',
    EXAM_ADMIT_CARD: 'Admit Card',
    RESULT: 'Result',
    APPRENTICESHIP: 'Apprenticeship',
    CALENDAR: 'Calendar',
    GENERAL_INFO: 'General Info',
};

function formatDate(dateStr) {
    if (!dateStr) return null;
    const d = new Date(dateStr);
    if (isNaN(d.getTime())) return null;
    return d.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
}

function isExpired(dateStr) {
    if (!dateStr) return false;
    const d = new Date(dateStr);
    d.setHours(0, 0, 0, 0);
    return d < new Date();
}

export default function NoticeCard({ notice, isSaved, toggleSave }) {
    const accent = CAT_ACCENTS[notice.category] || '#374151';
    const icon = CAT_ICONS[notice.category] || '📌';
    const expired = isExpired(notice.lastDate);
    const saved = isSaved ? isSaved(notice.id) : false;
    const typeLabel = NOTICE_TYPE_LABELS[notice.noticeType] || notice.noticeType;

    return (
        <article
            className="notice-card"
            style={{ '--card-accent': accent }}
            aria-label={notice.title}
        >
            {/* Header: category + type badges + state */}
            <div className="card-header">
                <div style={{ display: 'flex', gap: '0.4rem', flexWrap: 'wrap', alignItems: 'center' }}>
                    <span className={`category-badge ${notice.category}`}>
                        {icon} {notice.category}
                    </span>
                    {notice.noticeType && notice.noticeType !== 'GENERAL_INFO' && (
                        <span style={{
                            background: notice.noticeType === 'APPRENTICESHIP' ? '#FEF3C7' :
                                notice.noticeType === 'RESULT' ? '#D1FAE5' :
                                    notice.noticeType === 'EXAM_ADMIT_CARD' ? '#EFF6FF' :
                                        'var(--color-surface-2)',
                            color: notice.noticeType === 'APPRENTICESHIP' ? '#92400E' :
                                notice.noticeType === 'RESULT' ? '#065F46' :
                                    notice.noticeType === 'EXAM_ADMIT_CARD' ? '#1E40AF' :
                                        'var(--color-text-secondary)',
                            fontSize: '0.62rem',
                            fontWeight: 600,
                            padding: '0.15rem 0.45rem',
                            borderRadius: '4px',
                            border: '1px solid var(--color-border)'
                        }}>
                            {typeLabel}
                        </span>
                    )}
                    {notice.isNew && (
                        <span style={{
                            background: '#FF9933', color: 'white', fontSize: '0.62rem',
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
            <h2 className="card-title" title={notice.title}>
                {notice.title}
            </h2>

            {/* DEADLINE — shown prominently first */}
            <div className="card-deadline">
                {notice.lastDate ? (
                    <span
                        className={`deadline-block${notice.isDeadlineSoon && !expired ? ' deadline-soon' : ''}${expired ? ' deadline-expired' : ''}`}
                        title="Last date to apply"
                    >
                        <span className="deadline-icon">{expired ? '⏰' : notice.isDeadlineSoon ? '⚠️' : '📅'}</span>
                        <span>
                            <strong>Last Date:</strong> {formatDate(notice.lastDate)}
                            {expired && <span className="expired-label"> · Expired</span>}
                            {notice.isDeadlineSoon && !expired && <span className="soon-label"> · Closing Soon!</span>}
                        </span>
                    </span>
                ) : (
                    <span className="deadline-unknown">
                        <span className="deadline-icon">ℹ️</span>
                        <span className="deadline-not-mentioned">Last date not mentioned — verify on official site</span>
                    </span>
                )}
            </div>

            {/* Published date — secondary */}
            {(notice.publishedDate || notice.fetchedAt) && (
                <div className="card-meta">
                    <span className="meta-item" title="Published on official source">
                        <span className="icon">🗓️</span>
                        Posted: {formatDate(notice.publishedDate || notice.fetchedAt)}
                    </span>
                </div>
            )}

            {/* Source */}
            <div className="card-source">
                <div className="source-icon">🏛️</div>
                <span className="source-name" title={notice.sourceName}>
                    {notice.sourceName}
                </span>
                {notice.sourceDomain && (
                    <span style={{
                        fontSize: '0.65rem', color: 'var(--color-text-muted)',
                        background: 'var(--color-surface-2)', borderRadius: '4px',
                        padding: '0.1rem 0.4rem', border: '1px solid var(--color-border)',
                        marginLeft: 'auto', whiteSpace: 'nowrap', flexShrink: 0
                    }}>
                        {notice.sourceDomain}
                    </span>
                )}
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
                    View Official Notice ↗
                </a>
                {toggleSave && (
                    <button
                        onClick={() => toggleSave(notice)}
                        className="btn-official"
                        aria-label={saved ? 'Remove from saved' : 'Save this notice'}
                        title={saved ? 'Remove from saved' : 'Save for later'}
                        style={{
                            background: saved ? '#FEF3C7' : 'transparent',
                            border: `1.5px solid ${saved ? '#D97706' : 'var(--color-border)'}`,
                            color: saved ? '#92400E' : 'var(--color-text-secondary)',
                        }}
                    >
                        {saved ? '⭐' : '☆'}
                    </button>
                )}
            </div>
        </article>
    );
}
