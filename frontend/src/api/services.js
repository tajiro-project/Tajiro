import client, { withMock } from "./client";
import * as mock from "./mockData";

// ---------- finance ----------
export const financeApi = {
  list: (keyword) =>
    withMock(
      () => client.get("/financial-products", { params: { keyword } }),
      mock.mockFinancialProducts,
    ),
  matches: (keyword) =>
    withMock(
      () => client.get("/financial-products/matches", { params: { keyword } }),
      mock.mockFinancialProducts,
    ),
  detail: (id) =>
    withMock(
      () => client.get(`/financial-products/${id}`),
      mock.mockFinancialProductDetail,
    ),
};

// ---------- property ----------
export const propertyApi = {
  getPropertyDetail: (propertyId) => {
    return client.get(`/properties/${propertyId}`);
  },
  recommended: () =>
    withMock(() => client.get("/properties/recommended"), mock.mockProperties),
  detail: (id) =>
    withMock(
      () => client.get(`/properties/${id}`),
      () => mock.mockPropertyDetail(id),
    ),
  safety: (id) =>
    withMock(() => client.get(`/properties/${id}/safety`), mock.mockSafety),
  infrastructures: (id) =>
    withMock(
      () => client.get(`/properties/${id}/infrastructures`),
      mock.mockInfras,
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
          params: { categories: categories.join(",") },
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
      () => client.get("/safe/map", { params: bounds }),
      () => [
        { lat: 35.2229, lng: 128.6819, type: "CCTV", label: "CCTV" },
        { lat: 35.2217, lng: 128.6801, type: "POLICE", label: "상남지구대" },
        {
          lat: 35.2234,
          lng: 128.6832,
          type: "WOMEN_ZONE",
          label: "여성안심귀갓길",
        },
      ],
    ),
};

// ---------- favorite ----------
export const favoriteApi = {
  list: () =>
    withMock(() => client.get("/users/me/favorites"), mock.mockFavorites),
  add: (propertyId) =>
    withMock(
      () => client.post(`/users/me/favorites/${propertyId}`),
      () => ({
        favoriteId: "FV-" + propertyId,
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
  box: () =>
    withMock(() => client.get("/users/me/compare"), mock.mockCompareBox),
  addToBox: (propertyId) => {
    return client.post(`/users/me/compare/${propertyId}`);
  },
  removeFromBox: (propertyId) =>
    withMock(
      () => client.delete(`/users/me/compare/${propertyId}`),
      () => ({}),
    ),
  metrics: (propertyIds) =>
    withMock(
      () =>
        client.get("/comparisons/metrics", {
          params: { propertyIds: [...propertyIds].sort().join(",") },
        }),
      mock.mockComparisonMetrics,
    ),
  analyze: (propertyIds) =>
    withMock(
      () => client.post("/comparisons/analyze", { propertyIds }),
      mock.mockAiCoaching,
    ),
};
