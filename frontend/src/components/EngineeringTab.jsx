import { useState, useCallback } from 'react';
import { fetchNotices } from '../api/notices.js';
import NoticeCard from './NoticeCard.jsx';
import SkeletonCard from './Skeleton.jsx';

const BRANCHES = [
    { key: '', label: '⚙️ All Engineering' },
    { key: 'CIVIL', label: '🏗️ Civil' },
    { key: 'MECH', label: '🔧 Mechanical' },
    { key: 'EEE', label: '⚡ Electrical/EEE' },
    { key: 'ECE', label: '📡 Electronics/ECE' },
    { key: 'CSE', label: '💻 CSE/IT' },
    { key: 'CHEM', label: '⚗️ Chemical' },
    { key: 'INST', label: '🔬 Instrumentation' },
    { key: 'GENERAL_ENGG', label: '🔩 General Engg' },
];

const JOB_TYPES = [
    { key: '', label: 'All Types' },
    { key: 'RECRUITMENT', label: 'PSU Recruitment' },
    { key: 'APPRENTICESHIP', label: 'Apprenticeship' },
    { key: 'RESULT', label: 'Result' },
    { key: 'EXAM_ADMIT_CARD', label: 'Exam/Admit Card' },
];

function BranchPill({ label, active, onClick }) {
    return (
        <button
            className={`pill ${active ? 'active' : ''}`}
            onClick={onClick}
            style={{ fontSize: '0.75rem' }}
        >
            {label}
        </button>
    );
}

export default function EngineeringTab({ isSaved, toggleSave }) {
    const [branch, setBranch] = useState('');
    const [jobType, setJobType] = useState('');
    const [notices, setNotices] = useState([]);
    const [loading, setLoading] = useState(false);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);

    const load = useCallback(async (branchVal, typeVal, pageVal) => {
        setLoading(true);
        try {
            const params = {
                ...(branchVal && { branch: branchVal }),
                ...(typeVal && { noticeType: typeVal }),
                sortBy: 'newest',
                page: pageVal,
                size: 18,
                period: 'all',
            };
            const data = await fetchNotices(params);
            if (pageVal === 0) {
                setNotices(data.content || []);
            } else {
                setNotices(prev => {
                    const newItems = (data.content || []).filter(
                        n => !prev.some(p => p.id === n.id)
                    );
                    return [...prev, ...newItems];
                });
            }
            setTotalPages(data.totalPages || 0);
            setTotalElements(data.totalElements || 0);
        } catch (e) {
            console.error('Engineering tab load failed', e);
        } finally {
            setLoading(false);
        }
    }, []);

    // Load on mount and when filters change
    const handleBranch = (b) => {
        setBranch(b);
        setPage(0);
        load(b, jobType, 0);
    };
    const handleType = (t) => {
        setJobType(t);
        setPage(0);
        load(branch, t, 0);
    };

    // Initial load
    useState(() => { load('', '', 0); });

    return (
        <div>
            {/* SEO text */}
            <div style={{ padding: '1rem 0 0.5rem', borderBottom: '1px solid var(--color-border)', marginBottom: '1rem' }}>
                <p style={{ fontSize: '0.82rem', color: 'var(--color-text-muted)', marginBottom: '0.75rem' }}>
                    Engineering & Technical jobs — PSU, JE, GET, Apprenticeship — automatically filtered from official sources.
                </p>

                {/* Branch pills */}
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.4rem', marginBottom: '0.6rem' }}>
                    {BRANCHES.map(b => (
                        <BranchPill
                            key={b.key}
                            label={b.label}
                            active={branch === b.key}
                            onClick={() => handleBranch(b.key)}
                        />
                    ))}
                </div>

                {/* Job type filter */}
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.4rem', alignItems: 'center' }}>
                    <span style={{ fontSize: '0.72rem', fontWeight: 600, color: 'var(--color-text-muted)', textTransform: 'uppercase' }}>Type:</span>
                    {JOB_TYPES.map(t => (
                        <BranchPill
                            key={t.key}
                            label={t.label}
                            active={jobType === t.key}
                            onClick={() => handleType(t.key)}
                        />
                    ))}
                    {totalElements > 0 && (
                        <span style={{ marginLeft: 'auto', fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>
                            {totalElements.toLocaleString('en-IN')} notices
                        </span>
                    )}
                </div>
            </div>

            {/* Notices grid */}
            {loading && notices.length === 0 ? (
                <div className="notices-grid" aria-busy="true">
                    {Array.from({ length: 6 }).map((_, i) => <SkeletonCard key={i} />)}
                </div>
            ) : notices.length === 0 ? (
                <div className="empty-state" role="status">
                    <div className="emoji">⚙️</div>
                    <h3>No engineering notices found</h3>
                    <p>Try selecting "All Engineering" or a different job type.</p>
                </div>
            ) : (
                <div>
                    <div className="notices-grid">
                        {notices.map(notice => (
                            <NoticeCard key={notice.id} notice={notice} isSaved={isSaved} toggleSave={toggleSave} />
                        ))}
                    </div>
                    {!loading && page < totalPages - 1 && (
                        <div style={{ textAlign: 'center', paddingTop: '1.5rem' }}>
                            <button
                                className="pill"
                                onClick={() => {
                                    const next = page + 1;
                                    setPage(next);
                                    load(branch, jobType, next);
                                }}
                                style={{ padding: '0.6rem 2rem', fontSize: '0.85rem' }}
                            >
                                Load More
                            </button>
                        </div>
                    )}
                    {loading && (
                        <div className="notices-grid" style={{ marginTop: '1.5rem' }} aria-busy="true">
                            {Array.from({ length: 3 }).map((_, i) => <SkeletonCard key={`skel-${i}`} />)}
                        </div>
                    )}
                </div>
            )}
        </div>
    );
}
