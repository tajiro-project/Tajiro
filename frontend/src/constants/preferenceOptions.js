import {
  TrainFront, // 지하철
  Bus, // 버스터미널
  TrainTrack, // 기차역
  Plus, // 병원 (십자가)
  Pill, // 약국
  GraduationCap, // 학교
  Baby, // 유치원
  BookOpen, // 학원
  Library, // 도서관
  Trees, // 공원
  ShieldCheck, // 경찰서
  Flame, // 소방서
  Building2, // 행정복지센터
  Building, // 관공서
  Mail, // 우체국
  Landmark, // 은행
  Store, // 편의점
  ShoppingCart, // 마트
  Coffee, // 카페
  Utensils, // 음식점
  Film, // 문화시설
  Dumbbell, // 체육시설
  Waves, // 수영장
  SquareParking, // 주차장
  Fuel, // 주유소
} from 'lucide-vue-next';

export const PREFERENCE_STEP_LABELS = [
  '이주·통근 정보',
  '희망 주거 조건',
  '인프라·편의시설',
  '가치관 우선순위',
];

export const PREFERENCE_STEP_COUNT = PREFERENCE_STEP_LABELS.length;

export const MAX_PRIORITY_SELECTIONS = 3;

export const HOUSING_OPTIONS = ['원룸', '아파트', '주택/빌라', '오피스텔'];

export const TRADE_OPTIONS = ['월세', '전세', '매매'];

export const FLOOR_OPTIONS = ['지하/반지하', '1층', '2층 이상', '옥탑'];

export const INFRA_CATEGORIES = [
  // 🚆 교통
  { key: 'SUBWAY', label: '지하철', icon: TrainFront, color: '#2563EB' }, // 파랑 (지하철 대표색)
  { key: 'BUS_TERMINAL', label: '버스터미널', icon: Bus, color: '#0284C7' }, // 스카이블루
  { key: 'TRAIN', label: '기차역', icon: TrainTrack, color: '#1D4ED8' }, // 다크 블루

  // 🏥 의료/건강
  { key: 'HOSPITAL', label: '병원', icon: Plus, color: '#EF4444' }, // 적십자 빨강
  { key: 'PHARMACY', label: '약국', icon: Pill, color: '#EC4899' }, // 핑크/장미색

  // 🎓 교육
  { key: 'SCHOOL', label: '학교', icon: GraduationCap, color: '#7C3AED' }, // 보라
  { key: 'KINDERGARTEN', label: '유치원', icon: Baby, color: '#F43F5E' }, // 코랄/다홍 (아동 테마)
  { key: 'ACADEMY', label: '학원', icon: BookOpen, color: '#8B5CF6' }, // 바이올렛
  { key: 'LIBRARY', label: '도서관', icon: Library, color: '#6D28D9' }, // 딥 퍼플

  // 🌲 자연/안전/공공
  { key: 'PARK', label: '공원', icon: Trees, color: '#059669' }, // 딥 그린
  { key: 'POLICE', label: '경찰서', icon: ShieldCheck, color: '#1E3A8A' }, // 경찰 제복/마크 남색
  { key: 'FIRE', label: '소방서', icon: Flame, color: '#DC2626' }, // 소방차 빨강
  {
    key: 'GOV_OFFICE',
    label: '행정복지센터',
    icon: Building2,
    color: '#4B5563',
  }, // 슬레이트 그리
  { key: 'PUBLIC', label: '관공서', icon: Building, color: '#374151' }, // 다크 차콜
  { key: 'POST_OFFICE', label: '우체국', icon: Mail, color: '#EA580C' }, // 우체국 주황
  { key: 'BANK', label: '은행', icon: Landmark, color: '#0D9488' }, // 틸(Teal)/금융 대표색
];

export const AMENITY_CATEGORIES = [
  // 🛒 쇼핑/음식
  { key: 'CONVENIENCE', label: '편의점', icon: Store, color: '#10B981' }, // 초록 (CU/GS25 브랜드 톤)
  { key: 'MART', label: '마트', icon: ShoppingCart, color: '#F59E0B' }, // 골드/앰버
  { key: 'CAFE', label: '카페', icon: Coffee, color: '#B45309' }, // 커피 브라운
  { key: 'FOOD', label: '음식점', icon: Utensils, color: '#F97316' }, // 식욕을 돋우는 주황

  // 🎨 여가/체육/기타
  { key: 'CULTURE', label: '문화시설', icon: Film, color: '#6366F1' }, // 인디고
  { key: 'SPORTS', label: '체육시설', icon: Dumbbell, color: '#84CC16' }, // 라임 그린 (스포츠 톤)
  { key: 'SWIMMING', label: '수영장', icon: Waves, color: '#06B6D4' }, // 시원한 민트/아쿠아
  { key: 'PARKING', label: '주차장', icon: SquareParking, color: '#64748B' }, // 주차 표지판 블루-그레이
  { key: 'GAS', label: '주유소', icon: Fuel, color: '#D97706' }, // 옐로우 브라운
];

export const PREFERENCE_SLIDER_CONFIG = {
  COMMUTE_DISTANCE: {
    min: 0,
    max: 10000,
    step: 100,
    defaultValue: 1500,
    marks: ['0', '2km', '4km', '6km', '8km', '10km'],
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
  AREA: {
    min: 0,
    max: 200,
    step: 1,
    marks: ['0', '50m²', '100m²', '150m²', '200m²'],
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
    icon: '<svg width="20" height="20" viewBox="0 0 20 20" fill="none"><path d="M4 7h12l-1 9H5L4 7z" stroke="#545045" stroke-width="1.5" stroke-linejoin="round"/><path d="M7.5 7V5.5a2.5 2.5 0 015 0V7" stroke="#545045" stroke-width="1.5"/></svg>',
  },
  {
    criterion: 'AREA',
    title: '매물 면적',
    sub: '매물의 면적이 가장 중요해요',
    icon: '<svg width="20" height="20" viewBox="0 0 20 20" fill="none"><circle cx="10" cy="10" r="7.2" stroke="#545045" stroke-width="1.5"/><path d="M10 6.2v4.6" stroke="#545045" stroke-width="1.6" stroke-linecap="round"/><circle cx="10" cy="13.6" r="0.9" fill="#545045"/></svg>',
  },
];
