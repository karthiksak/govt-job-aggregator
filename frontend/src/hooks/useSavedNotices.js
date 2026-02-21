import { useState, useEffect } from 'react';

const SAVED_KEY = 'govtjobs_saved_ids';
const SAVED_DATA_KEY = 'govtjobs_saved_data';

export default function useSavedNotices() {
    const [savedIds, setSavedIds] = useState(() => {
        try {
            return JSON.parse(localStorage.getItem(SAVED_KEY) || '[]');
        } catch { return []; }
    });

    const [savedNotices, setSavedNotices] = useState(() => {
        try {
            return JSON.parse(localStorage.getItem(SAVED_DATA_KEY) || '[]');
        } catch { return []; }
    });

    useEffect(() => {
        localStorage.setItem(SAVED_KEY, JSON.stringify(savedIds));
    }, [savedIds]);

    useEffect(() => {
        localStorage.setItem(SAVED_DATA_KEY, JSON.stringify(savedNotices));
    }, [savedNotices]);

    const isSaved = (id) => savedIds.includes(String(id));

    const toggleSave = (notice) => {
        const idStr = String(notice.id);
        if (savedIds.includes(idStr)) {
            setSavedIds(prev => prev.filter(i => i !== idStr));
            setSavedNotices(prev => prev.filter(n => String(n.id) !== idStr));
        } else {
            setSavedIds(prev => [...prev, idStr]);
            setSavedNotices(prev => [...prev, notice]);
        }
    };

    return { savedIds, savedNotices, isSaved, toggleSave };
}
