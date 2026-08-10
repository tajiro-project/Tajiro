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
  list: (keyword) =>
    withMock(
      () => client.get('/financial-products', { params: { keyword } }),
      mock.mockFinancialProducts,
    ),
  matches: (keyword) =>
    withMock(
      () => client.get('/financial-products/matches', { params: { keyword } }),
      mock.mockFinancialProducts,
    ),
  detail: (id) =>
    withMock(
      () => client.get(`/financial-products/${id}`),
      mock.mockFinancialProductDetail,
    ),
};

// ---------- policy ----------
export const policyApi = {
  list: (regionCode, keyword) =>
    withMock(
      () => client.get('/policies', { params: { regionCode, keyword } }),
      mock.mockPolicies,
    ),
  matches: (keyword) =>
    withMock(
      () => client.get('/policies/matches', { params: { keyword } }),
      mock.mockPolicies,
    ),
  detail: (policyId) =>
    withMock(() => client.get(`/policies/${policyId}`), mock.mockPolicyDetail),
};

// ---------- property ----------
export const propertyApi = {
  getPropertyDetail: async (propertyId) =>
    (await client.get(`/properties/${propertyId}`)).data,
  infrastructures: async (id) =>
    (await client.get(`/properties/${id}/infrastructures`)).data,
  safety: async (id) => (await client.get(`/properties/${id}/safety`)).data,

  recommended: () =>
    withMock(() => client.get('/properties/recommended'), mock.mockProperties),
  detail: (id) =>
    withMock(
      () => client.get(`/properties/${id}`),
      () => mock.mockPropertyDetail(id),
    ),
  comparables: (id) =>
    withMock(
      () => client.get(`/properties/${id}/comparables`),
      mock.mockComparables,
    ),
  marketEvaluation: (id) =>
    withMock(
      () => client.get(`/properties/${id}/market-evaluation`),
      mock.mockMarketEvaluation,
    ),
  commute: (id) =>
    withMock(() => client.get(`/properties/${id}/commute`), mock.mockCommute),
  infraMapLayer: (id, categories) =>
    withMock(
      () =>
        client.get(`/infrastructures/${id}/map`, {
          params: { categories: categories.join(',') },
        }),
      () =>
        mock.mockInfras
          .filter((i) => categories.includes(i.category))
          .map((i) => ({
            lat: i.latitude,
            lng: i.longitude,
            category: i.category,
            name: i.name,
          })),
    ),
  safeMap: (bounds) =>
    withMock(
      () => client.get('/safe/map', { params: bounds }),
      () => [
        { lat: 35.2229, lng: 128.6819, type: 'CCTV', label: 'CCTV' },
        { lat: 35.2217, lng: 128.6801, type: 'POLICE', label: '상남지구대' },
        {
          lat: 35.2234,
          lng: 128.6832,
          type: 'WOMEN_ZONE',
          label: '여성안심귀갓길',
        },
      ],
    ),
  getList: async ({ centerLat, centerLng } = {}) => {
    const params = {};
    if (centerLat != null && centerLng != null) {
      params.centerLat = centerLat;
      params.centerLng = centerLng;
    }
    const res = await client.get('/properties', { params });
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
  metrics: async (propertyIds, workplace) => {
    const sortedIds = [...(propertyIds ?? [])]
      .map(Number)
      .sort((a, b) => a - b);
    return (
      await client.get('/comparisons/metrics', {
        params: {
          propertyIds: sortedIds.join(','),
          workplaceLat: workplace?.lat,
          workplaceLng: workplace?.lng,
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
