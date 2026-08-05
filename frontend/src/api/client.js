import axios from 'axios';

const client = axios.create({
  baseURL: '/api',
  timeout: 8000,
});

client.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

export async function withMock(request, mock) {
  try {
    const res = await request();
    return res.data;
  } catch (e) {
    if (import.meta.env.DEV) {
      console.warn('[api] mock fallback:', e.config?.url ?? e.message);
    }
    return typeof mock === 'function' ? mock() : mock;
  }
}

export function getApiErrorMessage(
  error,
  fallback = '서버와 연결하지 못했습니다. 잠시 후 다시 시도해주세요.',
) {
  return error?.response?.data?.message ?? error?.response?.data?.error ?? fallback;
}

export default client;
