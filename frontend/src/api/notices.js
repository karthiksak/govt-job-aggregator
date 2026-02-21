import axios from 'axios';

const BASE = '/api';

export const fetchNotices = (params) =>
    axios.get(`${BASE}/notices`, { params }).then(r => r.data.data);

export const fetchNoticeById = (id) =>
    axios.get(`${BASE}/notices/${id}`).then(r => r.data.data);

export const fetchCategories = () =>
    axios.get(`${BASE}/categories`).then(r => r.data.data);

export const fetchStates = () =>
    axios.get(`${BASE}/states`).then(r => r.data.data);

export const triggerRefresh = () =>
    axios.post(`${BASE}/admin/refresh`).then(r => r.data.data);
