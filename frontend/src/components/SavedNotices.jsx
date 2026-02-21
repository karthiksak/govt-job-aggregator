import NoticeCard from './NoticeCard.jsx';

export default function SavedNotices({ notices, isSaved, toggleSave }) {
    if (!notices || notices.length === 0) {
        return (
            <div className="empty-state" role="status">
                <div className="emoji">⭐</div>
                <h3>No saved notices yet</h3>
                <p>Click the ☆ star button on any notice to save it here for quick access later. Your saves are stored locally in your browser.</p>
            </div>
        );
    }

    return (
        <div>
            <p style={{ fontSize: '0.82rem', color: 'var(--color-text-muted)', marginBottom: '1rem' }}>
                {notices.length} saved notice{notices.length !== 1 ? 's' : ''} — stored locally in your browser.
            </p>
            <div className="notices-grid">
                {notices.map(notice => (
                    <NoticeCard key={notice.id} notice={notice} isSaved={isSaved} toggleSave={toggleSave} />
                ))}
            </div>
        </div>
    );
}
