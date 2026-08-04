export const PREFERENCE_STEP_LABELS = [
  '이주·통근 정보',
  '희망 주거 조건',
  '인프라·편의시설',
  '가치관 우선순위',
];

export const PREFERENCE_STEP_COUNT = PREFERENCE_STEP_LABELS.length;

export const MAX_PRIORITY_SELECTIONS = 3;

export const HOUSING_OPTIONS = ['원/투룸', '아파트', '주택/빌라', '오피스텔'];

export const TRADE_OPTIONS = ['월세', '전세', '매매'];

export const FLOOR_OPTIONS = ['지하/반지하', '1층', '2층 이상', '옥탑'];

export const INFRA_CATEGORIES = [
  { key: 'SUBWAY', label: '지하철' },
  { key: 'BUS_TERMINAL', label: '버스터미널' },
  { key: 'TRAIN', label: '기차역' },
  { key: 'HOSPITAL', label: '병원' },
  { key: 'PHARMACY', label: '약국' },
  { key: 'SCHOOL', label: '학교' },
  { key: 'KINDERGARTEN', label: '유치원' },
  { key: 'ACADEMY', label: '학원' },
  { key: 'LIBRARY', label: '도서관' },
  { key: 'PARK', label: '공원' },
  { key: 'POLICE', label: '경찰서' },
  { key: 'FIRE', label: '소방서' },
  { key: 'GOV_OFFICE', label: '행정복지센터' },
  { key: 'PUBLIC', label: '관공서' },
  { key: 'POST_OFFICE', label: '우체국' },
  { key: 'BANK', label: '은행' },
];

export const AMENITY_CATEGORIES = [
  { key: 'CONVENIENCE', label: '편의점' },
  { key: 'MART', label: '마트' },
  { key: 'CAFE', label: '카페' },
  { key: 'FOOD', label: '음식점' },
  { key: 'CULTURE', label: '문화시설' },
  { key: 'SPORTS', label: '체육시설' },
  { key: 'SWIMMING', label: '수영장' },
  { key: 'PARKING', label: '주차장' },
  { key: 'GAS', label: '주유소' },
];

export const PREFERENCE_SLIDER_CONFIG = {
  COMMUTE_DISTANCE: {
    min: 500,
    max: 10000,
    step: 100,
    defaultValue: 1500,
    marks: ['500m', '2.5km', '5km', '7.5km', '10km'],
  },
  DEPOSIT_MONTHLY: {
    min: 0,
    max: 5000,
    step: 100,
    marks: ['최소', '1,000', '2,000', '3,000', '4,000', '최대'],
  },
  DEPOSIT_JEONSE: {
    min: 0,
    max: 50000,
    step: 500,
    marks: ['최소', '1억', '2억', '3억', '4억', '최대'],
  },
  MONTHLY_RENT: {
    min: 0,
    max: 250,
    step: 5,
    marks: ['최소', '50만', '100만', '150만', '200만', '최대'],
  },
  SALE_PRICE: {
    min: 0,
    max: 400000,
    step: 5000,
    marks: ['최소', '10억', '20억', '30억', '최대'],
  },
};

export const PRIORITY_OPTIONS = [
  {
    criterion: 'COMMUTE',
    title: '직주근접',
    sub: '출퇴근 시간이 가장 중요해요',
    icon: '<svg width="20" height="20" viewBox="0 0 20 20" fill="none"><circle cx="10" cy="10" r="7.2" stroke="#545045" stroke-width="1.5"/><path d="M10 6v4l2.6 1.6" stroke="#545045" stroke-width="1.5" stroke-linecap="round"/></svg>',
  },
  {
    criterion: 'COST',
    title: '가성비',
    sub: '월세·관리비 등 주거비 절약',
    icon: '<svg width="20" height="20" viewBox="0 0 20 20" fill="none"><circle cx="10" cy="10" r="7.2" stroke="#545045" stroke-width="1.5"/><path d="M7 8h6M7 10.5h6M9 6.5l2 7" stroke="#545045" stroke-width="1.3" stroke-linecap="round"/></svg>',
  },
  {
    criterion: 'INFRA',
    title: '인프라',
    sub: '교육시설, 의료시설, 교통시설, 공공기관 등',
    icon: '<svg width="20" height="20" viewBox="0 0 20 20" fill="none"><rect x="4" y="3.5" width="8" height="13" stroke="#545045" stroke-width="1.5"/><path d="M12 8h4v8.5h-4M6.5 7h1.2M6.5 10h1.2M6.5 13h1.2M9.5 7h1.2M9.5 10h1.2M9.5 13h1.2" stroke="#545045" stroke-width="1.2"/></svg>',
  },
  {
    criterion: 'AMENITY',
    title: '편의시설',
    sub: '마트, 편의점, 카페 등',
    icon: '<svg width="20" height="20" viewBox="0 0 20 20" fill="none"><path d="M10 2.5l6.5 3v5c0 4-2.8 6.4-6.5 7.5-3.7-1.1-6.5-3.5-6.5-7.5v-5l6.5-3z" stroke="#545045" stroke-width="1.5" stroke-linejoin="round"/><path d="M7.2 10l2 2 3.6-3.8" stroke="#545045" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>',
  },
  {
    criterion: 'AREA',
    title: '매물 면적',
    sub: '매물의 면적이 가장 중요해요',
    icon: '<svg width="20" height="20" viewBox="0 0 20 20" fill="none"><circle cx="10" cy="10" r="7.2" stroke="#545045" stroke-width="1.5"/><path d="M10 6.2v4.6" stroke="#545045" stroke-width="1.6" stroke-linecap="round"/><circle cx="10" cy="13.6" r="0.9" fill="#545045"/></svg>',
  },
];
