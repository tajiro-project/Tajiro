import client, { withMock } from './client';
import * as mock from './mockData';

// ---------- finance ----------
export const financeApi = {
  list: (keyword) =>
    withMock(
      () => client.get('/financial-products', { params: { keyword } }),
      mock.mockFinancialProducts,
    ),
  matches: async (keyword) => {
    const response = await client.get('/financial-products/matches', {
      params: { keyword },
    });
    return response.data;
  },
  detail: (id) =>
    withMock(
      () => client.get(`/financial-products/${id}`),
      mock.mockFinancialProductDetail,
    ),
};
