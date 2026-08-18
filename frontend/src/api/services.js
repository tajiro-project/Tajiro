import client, { withMock } from './client';
import * as mock from './mockData';

// ---------- user ----------
export const userApi = {
  getProfile: async () => {
    const response = await client.get('/users/me/profile');
    return response.data?.data ?? response.data;
  },
};

// ---------- preference ----------
export const preferenceApi = {
  get: async () => {
    const response = await client.get('/users/me/preferences');
    return response.data?.data ?? response.data;
  },
  create: async (preference) => {
    const response = await client.post('/users/me/preferences', preference);
    return response.data?.data ?? response.data;
  },
  update: async (preference) => {
    const response = await client.put('/users/me/preferences', preference);
    return response.data?.data ?? response.data;
  },
};

// ---------- finance ----------
export const financeApi = {
  list: async (keyword) =>
    (await client.get('/financial-products', { params: { keyword } })).data,
  matches: async (keyword) =>
    (await client.get('/financial-products/matches', { params: { keyword } }))
      .data,
  detail: async (id) => (await client.get(`/financial-products/${id}`)).data,
  getFinanceRecommendations: async (propertyId) =>
    (
      await client.get('/financial-products/recommendations', {
        params: { propertyId },
      })
    ).data,
};

// ---------- policy ----------
export const policyApi = {
  list: async (regionCode, keyword) =>
    (await client.get('/policies', { params: { regionCode, keyword } })).data,
  matches: async (keyword, categoryCode) =>
    (
      await client.get('/policies/matches', {
        params: { keyword, categoryCode },
      })
    ).data,
  detail: async (policyId) => (await client.get(`/policies/${policyId}`)).data,
  getPolicyRecommendations: async (propertyId) =>
    (
      await client.get('/policies/recommendations', {
        params: { propertyId },
      })
    ).data,
};

// ---------- property ----------
export const propertyApi = {
  getPropertyDetail: async (propertyId) =>
    (await client.get(`/properties/${propertyId}`)).data,
  infrastructures: async (id) =>
    (await client.get(`/properties/${id}/infrastructures`)).data,
  safety: async (id) => (await client.get(`/properties/${id}/safety`)).data,
  getList: async () => {
    const res = await client.get('/properties');
    return (res.data.data ?? []).map((p) => ({ ...p, propertyId: p.id }));
  },
};

// ---------- building ----------
export const buildingApi = {
  infraPoints: async (buildingId) =>
    (await client.get(`/buildings/${buildingId}/infrastructures`)).data,
};

// ---------- favorite ----------
export const favoriteApi = {
  list: () =>
    withMock(() => client.get('/users/me/favorites'), mock.mockFavorites),
  add: (propertyId) =>
    withMock(
      () => client.post(`/users/me/favorites/${propertyId}`),
      () => ({
        favoriteId: 'FV-' + propertyId,
        createdAt: new Date().toISOString(),
      }),
    ),
  remove: (propertyId) =>
    withMock(
      () => client.delete(`/users/me/favorites/${propertyId}`),
      () => ({}),
    ),
};

// ---------- comparison ----------
export const comparisonApi = {
  box: async () => (await client.get('/users/me/compare')).data,
  addToBox: async (propertyId) =>
    (await client.post(`/users/me/compare/${propertyId}`)).data,
  removeFromBox: async (propertyId) =>
    (await client.delete(`/users/me/compare/${propertyId}`)).data,
  metrics: async (propertyIds, workplace, refreshMarketScore = true) => {
    const sortedIds = [...(propertyIds ?? [])]
      .map(Number)
      .sort((a, b) => a - b);
    return (
      await client.get('/comparisons/metrics', {
        params: {
          propertyIds: sortedIds.join(','),
          workplaceLat: workplace?.lat,
          workplaceLng: workplace?.lng,
          refreshMarketScore,
        },
      })
    ).data;
  },
  analyze: async (propertyIds, workplace, priorities) => {
    const sortedIds = [...(propertyIds ?? [])]
      .map(Number)
      .sort((a, b) => a - b);
    return (
      await client.post(
        '/comparisons/analyze',
        {
          propertyIds: sortedIds,
          workplaceLat: workplace?.lat,
          workplaceLng: workplace?.lng,
          priorities,
        },
        { timeout: 35000 },
      )
    ).data;
  },
};

// ---------- seller ----------
export const sellerApi = {
  create: async (body) => (await client.post('/seller/properties', body)).data,
  myProperties: async (params = {}) =>
    (await client.get('/seller/properties', { params })).data,
  myProperty: async (id) => (await client.get(`/seller/properties/${id}`)).data,
  changeStatus: async (id, transactionStatus) =>
    (
      await client.patch(`/seller/properties/${id}/status`, {
        transactionStatus,
      })
    ).data,
  remove: async (id) => (await client.delete(`/seller/properties/${id}`)).data,
};

// ---------- location ----------
export const locationApi = {
  searchAddress: async (query) =>
    (await client.get('/locations/addresses', { params: { query } })).data,
  searchDongs: async (params) =>
    (await client.get('/locations/buildings/dongs', { params })).data,
};
