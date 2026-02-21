import { useState, useEffect } from 'react';

const STATE_ONBOARDING_KEY = 'govtjobs_selected_state';

// All 28 Indian States + 8 Union Territories
const INDIAN_STATES = [
    // States
    { name: 'Andhra Pradesh', flag: '🏛️', category: 'state' },
    { name: 'Arunachal Pradesh', flag: '🏔️', category: 'state' },
    { name: 'Assam', flag: '🌿', category: 'state' },
    { name: 'Bihar', flag: '🌾', category: 'state' },
    { name: 'Chhattisgarh', flag: '🌳', category: 'state' },
    { name: 'Goa', flag: '🏖️', category: 'state' },
    { name: 'Gujarat', flag: '🦁', category: 'state' },
    { name: 'Haryana', flag: '🌻', category: 'state' },
    { name: 'Himachal Pradesh', flag: '⛰️', category: 'state' },
    { name: 'Jharkhand', flag: '🌲', category: 'state' },
    { name: 'Karnataka', flag: '🌺', category: 'state' },
    { name: 'Kerala', flag: '🌴', category: 'state' },
    { name: 'Madhya Pradesh', flag: '🐯', category: 'state' },
    { name: 'Maharashtra', flag: '🏙️', category: 'state' },
    { name: 'Manipur', flag: '💐', category: 'state' },
    { name: 'Meghalaya', flag: '🌧️', category: 'state' },
    { name: 'Mizoram', flag: '🏞️', category: 'state' },
    { name: 'Nagaland', flag: '🦅', category: 'state' },
    { name: 'Odisha', flag: '🛕', category: 'state' },
    { name: 'Punjab', flag: '🌾', category: 'state' },
    { name: 'Rajasthan', flag: '🏜️', category: 'state' },
    { name: 'Sikkim', flag: '🏔️', category: 'state' },
    { name: 'Tamil Nadu', flag: '🌊', category: 'state' },
    { name: 'Telangana', flag: '💧', category: 'state' },
    { name: 'Tripura', flag: '🌿', category: 'state' },
    { name: 'Uttar Pradesh', flag: '🕌', category: 'state' },
    { name: 'Uttarakhand', flag: '🏔️', category: 'state' },
    { name: 'West Bengal', flag: '🐯', category: 'state' },
    // Union Territories
    { name: 'Andaman & Nicobar Islands', flag: '🏝️', category: 'ut' },
    { name: 'Chandigarh', flag: '🌹', category: 'ut' },
    { name: 'Dadra & Nagar Haveli', flag: '🌿', category: 'ut' },
    { name: 'Daman & Diu', flag: '🌊', category: 'ut' },
    { name: 'Delhi', flag: '🏛️', category: 'ut' },
    { name: 'Jammu & Kashmir', flag: '⛄', category: 'ut' },
    { name: 'Ladakh', flag: '🏔️', category: 'ut' },
    { name: 'Lakshadweep', flag: '🐠', category: 'ut' },
    { name: 'Puducherry', flag: '🌺', category: 'ut' },
];

export default function StateOnboarding({ onComplete }) {
    const [search, setSearch] = useState('');
    const [hovered, setHovered] = useState(null);

    const filtered = INDIAN_STATES.filter(s =>
        s.name.toLowerCase().includes(search.toLowerCase())
    );
    const states = filtered.filter(s => s.category === 'state');
    const uts = filtered.filter(s => s.category === 'ut');

    const handleSelect = (stateName) => {
        localStorage.setItem(STATE_ONBOARDING_KEY, stateName);
        onComplete(stateName);
    };

    return (
        <div style={{
            position: 'fixed', inset: 0, zIndex: 999,
            background: 'linear-gradient(135deg, #0B2447 0%, #0D3B7A 60%, #1a5276 100%)',
            display: 'flex', flexDirection: 'column', alignItems: 'center',
            justifyContent: 'flex-start', overflowY: 'auto', padding: '2rem 1rem',
        }}>
            {/* Logo */}
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '2rem', color: 'white' }}>
                <div style={{
                    background: '#FF9933', width: 48, height: 48, borderRadius: 10,
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    fontWeight: 800, fontSize: '1.1rem', color: 'white'
                }}>IN</div>
                <div>
                    <div style={{ fontWeight: 800, fontSize: '1.4rem', lineHeight: 1 }}>GovtJobs.in</div>
                    <div style={{ fontSize: '0.65rem', opacity: 0.6, letterSpacing: '0.08em', textTransform: 'uppercase' }}>Official Job Notification Aggregator</div>
                </div>
            </div>

            {/* Headline */}
            <div style={{ textAlign: 'center', marginBottom: '2rem', maxWidth: 540 }}>
                <h1 style={{ color: 'white', fontSize: 'clamp(1.4rem, 4vw, 2rem)', fontWeight: 800, marginBottom: '0.5rem' }}>
                    📍 Which state are you from?
                </h1>
                <p style={{ color: 'rgba(255,255,255,0.65)', fontSize: '0.9rem' }}>
                    We'll show you <strong style={{ color: '#FF9933' }}>State Government</strong> jobs alongside <strong style={{ color: '#FF9933' }}>Central Government</strong> jobs in one view.
                </p>
            </div>

            {/* Search */}
            <div style={{ width: '100%', maxWidth: 480, marginBottom: '1.5rem' }}>
                <input
                    type="search"
                    placeholder="🔍 Search your state..."
                    value={search}
                    onChange={e => setSearch(e.target.value)}
                    autoFocus
                    style={{
                        width: '100%', padding: '0.75rem 1rem', borderRadius: 10,
                        border: '2px solid rgba(255,255,255,0.2)', background: 'rgba(255,255,255,0.1)',
                        color: 'white', fontSize: '0.95rem', fontFamily: 'inherit',
                        outline: 'none', backdropFilter: 'blur(10px)',
                    }}
                />
            </div>

            {/* States grid */}
            <div style={{ width: '100%', maxWidth: 700 }}>
                {states.length > 0 && (
                    <>
                        <div style={{ color: 'rgba(255,255,255,0.45)', fontSize: '0.7rem', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.1em', marginBottom: '0.6rem' }}>States</div>
                        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(180px, 1fr))', gap: '0.5rem', marginBottom: '1.5rem' }}>
                            {states.map(s => (
                                <StateCard key={s.name} state={s} onSelect={handleSelect} isHovered={hovered === s.name} onHover={setHovered} />
                            ))}
                        </div>
                    </>
                )}
                {uts.length > 0 && (
                    <>
                        <div style={{ color: 'rgba(255,255,255,0.45)', fontSize: '0.7rem', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.1em', marginBottom: '0.6rem' }}>Union Territories</div>
                        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(180px, 1fr))', gap: '0.5rem', marginBottom: '1.5rem' }}>
                            {uts.map(s => (
                                <StateCard key={s.name} state={s} onSelect={handleSelect} isHovered={hovered === s.name} onHover={setHovered} />
                            ))}
                        </div>
                    </>
                )}
            </div>

            {/* Skip */}
            <button
                onClick={() => onComplete(null)}
                style={{
                    background: 'transparent', border: 'none', color: 'rgba(255,255,255,0.4)',
                    fontSize: '0.8rem', cursor: 'pointer', marginTop: '0.5rem',
                    textDecoration: 'underline', fontFamily: 'inherit', padding: '0.5rem 1rem'
                }}
            >
                Skip — show all India jobs
            </button>
        </div>
    );
}

function StateCard({ state, onSelect, isHovered, onHover }) {
    return (
        <button
            onClick={() => onSelect(state.name)}
            onMouseEnter={() => onHover(state.name)}
            onMouseLeave={() => onHover(null)}
            style={{
                background: isHovered ? 'rgba(255,153,51,0.25)' : 'rgba(255,255,255,0.08)',
                border: isHovered ? '1.5px solid #FF9933' : '1.5px solid rgba(255,255,255,0.12)',
                borderRadius: 10, padding: '0.65rem 0.85rem',
                display: 'flex', alignItems: 'center', gap: '0.6rem',
                color: 'white', cursor: 'pointer', fontFamily: 'inherit',
                fontSize: '0.82rem', fontWeight: isHovered ? 600 : 400,
                transition: 'all 0.15s', textAlign: 'left', width: '100%',
                transform: isHovered ? 'scale(1.02)' : 'scale(1)',
            }}
        >
            <span style={{ fontSize: '1.2rem', flexShrink: 0 }}>{state.flag}</span>
            <span style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{state.name}</span>
        </button>
    );
}

// Hook to manage the onboarding state
export function useStateOnboarding() {
    const [selectedState, setSelectedState] = useState(() => {
        return localStorage.getItem(STATE_ONBOARDING_KEY) || null;
    });
    const [showOnboarding, setShowOnboarding] = useState(() => {
        return !localStorage.getItem(STATE_ONBOARDING_KEY);
    });

    const completeOnboarding = (stateName) => {
        if (stateName) {
            localStorage.setItem(STATE_ONBOARDING_KEY, stateName);
        }
        setSelectedState(stateName);
        setShowOnboarding(false);
    };

    const resetState = () => {
        localStorage.removeItem(STATE_ONBOARDING_KEY);
        setSelectedState(null);
        setShowOnboarding(true);
    };

    return { selectedState, showOnboarding, completeOnboarding, resetState };
}

export { INDIAN_STATES };
