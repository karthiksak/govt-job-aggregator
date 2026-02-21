export default function SkeletonCard() {
    return (
        <div className="skeleton-card" aria-hidden="true">
            <div style={{ display: 'flex', justifyContent: 'space-between', gap: '0.5rem' }}>
                <div className="skeleton" style={{ width: '70px', height: '20px' }} />
                <div className="skeleton" style={{ width: '80px', height: '20px' }} />
            </div>
            <div>
                <div className="skeleton" style={{ width: '100%', height: '14px', marginBottom: '6px' }} />
                <div className="skeleton" style={{ width: '85%', height: '14px', marginBottom: '6px' }} />
                <div className="skeleton" style={{ width: '60%', height: '14px' }} />
            </div>
            <div style={{ display: 'flex', gap: '1rem' }}>
                <div className="skeleton" style={{ width: '100px', height: '14px' }} />
                <div className="skeleton" style={{ width: '110px', height: '14px' }} />
            </div>
            <div className="skeleton" style={{ width: '100%', height: '36px', borderRadius: '6px' }} />
        </div>
    );
}
