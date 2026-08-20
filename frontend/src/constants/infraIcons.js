/* 인프라 카테고리 아이콘·색 */

// 카테고리가 많아 개별 색은 구분이 불가능하므로 그룹으로 묶는다
export const INFRA_GROUP_COLORS = {
  TRANSPORT: '#3b82f6', // 교통
  MEDICAL: '#ef4444', // 의료
  EDUCATION: '#8b5cf6', // 교육
  LIVING: '#f0a800', // 생활·쇼핑
  PUBLIC_SERVICE: '#6b7280', // 공공·행정
  LEISURE: '#14b8a6', // 여가
};

export const INFRA_CATEGORY_GROUP = {
  BUS_TERMINAL: 'TRANSPORT',
  SUBWAY: 'TRANSPORT',
  TRAIN: 'TRANSPORT',
  PARKING: 'TRANSPORT',
  GAS: 'TRANSPORT',

  HOSPITAL: 'MEDICAL',
  PHARMACY: 'MEDICAL',

  SCHOOL: 'EDUCATION',
  ACADEMY: 'EDUCATION',
  KINDERGARTEN: 'EDUCATION',
  LIBRARY: 'EDUCATION',

  MART: 'LIVING',
  CONVENIENCE: 'LIVING',
  CAFE: 'LIVING',
  FOOD: 'LIVING',
  BANK: 'LIVING',

  GOV_OFFICE: 'PUBLIC_SERVICE',
  POST_OFFICE: 'PUBLIC_SERVICE',
  PUBLIC: 'PUBLIC_SERVICE',
  POLICE: 'PUBLIC_SERVICE',
  FIRE: 'PUBLIC_SERVICE',

  PARK: 'LEISURE',
  CULTURE: 'LEISURE',
  SPORTS: 'LEISURE',
  SWIMMING: 'LEISURE',
};

export const INFRA_ICON_PATHS = {
  // 교통
  BUS_TERMINAL:
    '<rect x="3.5" y="3" width="9" height="7.5" rx="1"/><path d="M3.5 7.5h9M5.5 13l.7-2.5M10.5 13l-.7-2.5"/>',
  SUBWAY:
    '<rect x="4" y="3" width="8" height="6.5" rx="2"/><path d="M4 7h8M6 12.5l1-3M10 12.5l-1-3"/>',
  TRAIN:
    '<rect x="4.5" y="2.5" width="7" height="8" rx="1.5"/><path d="M4.5 7h7M6.5 13l1-2.5M9.5 13l-1-2.5"/>',
  PARKING:
    '<circle cx="8" cy="8" r="5.5"/><path d="M6.5 11V5h2a1.8 1.8 0 0 1 0 3.6h-2"/>',
  GAS: '<path d="M4 13V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v9"/><path d="M4 8h6M10 6h1.6a1 1 0 0 1 1 1v3.4a1 1 0 0 0 1 1"/>',

  // 의료
  HOSPITAL: '<path d="M8 4v8M4 8h8"/>',
  PHARMACY: '<circle cx="8" cy="8" r="5.5"/><path d="M8 5.5v5M5.5 8h5"/>',

  // 교육
  SCHOOL:
    '<path d="M8 2.5l6 3v1H2v-1l6-3z"/><path d="M4 6.5v6M12 6.5v6M2.5 12.5h11"/>',
  ACADEMY:
    '<path d="M2 5.5L8 3l6 2.5L8 8 2 5.5z"/><path d="M4.5 6.6V10c0 1 1.6 1.8 3.5 1.8s3.5-.8 3.5-1.8V6.6"/>',
  KINDERGARTEN:
    '<circle cx="8" cy="5.5" r="2"/><path d="M4 13c0-2.2 1.8-4 4-4s4 1.8 4 4"/>',
  LIBRARY: '<path d="M3 3.5h4.5v9H3zM8.5 3.5H13v9H8.5z"/>',

  // 생활·쇼핑
  MART: '<path d="M3 5h10l-1 7.5H4L3 5z"/><path d="M6 8h4"/>',
  CONVENIENCE:
    '<path d="M4 6h8l-.8 6.5H4.8L4 6z"/><path d="M6.2 6V4.8a1.8 1.8 0 0 1 3.6 0V6"/>',
  CAFE: '<path d="M4 6h6v4a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6z"/><path d="M10 7h1.5a1.5 1.5 0 0 1 0 3H10"/>',
  FOOD: '<path d="M5 3v4.5a1.5 1.5 0 0 0 3 0V3M6.5 7.5V13"/><path d="M11 13V3c1 0 1.6.9 1.6 2.2s-.6 2.2-1.6 2.2"/>',
  BANK: '<path d="M8 3l5.5 3H2.5L8 3z"/><path d="M4.5 6.5v5M11.5 6.5v5M2.5 12h11"/>',

  // 공공·행정
  GOV_OFFICE:
    '<path d="M8 2v1.5"/><path d="M3 5.5h10v7H3z"/><path d="M6 12.5V8.5h4v4"/>',
  POST_OFFICE: '<path d="M2.5 4.5h11v7h-11z"/><path d="M2.5 4.5L8 9l5.5-4.5"/>',
  PUBLIC: '<circle cx="8" cy="8" r="5.5"/><path d="M8 2.5v11M2.5 8h11"/>',
  POLICE:
    '<path d="M8 2.5l5 2v3.7c0 3-2 5-5 5.8-3-.8-5-2.8-5-5.8V4.5l5-2z"/><path d="M8 6v3.5M6.3 7.7h3.4"/>',
  FIRE: '<path d="M8 2.5S11 5.5 11 8a3 3 0 0 1-6 0c0-2.5 3-5.5 3-5.5z"/>',

  // 여가
  PARK: '<path d="M8 2.5l3.5 5h-2L12 11H4l2.5-3.5h-2L8 2.5z"/><path d="M8 11v3"/>',
  CULTURE:
    '<circle cx="5.5" cy="10.5" r="2.2"/><circle cx="10.5" cy="9.5" r="2.2"/><path d="M7.7 10.5V4l5 1.2v4.3"/>',
  SPORTS:
    '<circle cx="8" cy="8" r="5.5"/><path d="M8 2.5c-2.2 2-2.2 9 0 11M2.5 8h11"/>',
  SWIMMING:
    '<circle cx="5.5" cy="4.5" r="1.5"/><path d="M7 6l3.5-1.5"/><path d="M2 10.5c1.5-1.2 2.5-1.2 4 0s2.5 1.2 4 0 2.5-1.2 4 0"/>',
};

export function infraColor(category) {
  const group = INFRA_CATEGORY_GROUP[category];
  return INFRA_GROUP_COLORS[group] ?? '#8a8d8f';
}

export function infraIconSvg(category, size = 14) {
  const path = INFRA_ICON_PATHS[category];
  if (!path) return '';
  return `<svg width="${size}" height="${size}" viewBox="0 0 16 16" fill="none"
    stroke="#fff" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">${path}</svg>`;
}
