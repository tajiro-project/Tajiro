import {
  TrainFront,
  Bus,
  TrainTrack,
  Plus,
  Pill,
  GraduationCap,
  Baby,
  BookOpen,
  Library,
  Trees,
  ShieldCheck,
  Flame,
  Building2,
  Building,
  Mail,
  Landmark,
  Store,
  ShoppingCart,
  Coffee,
  Utensils,
  Film,
  Dumbbell,
  Waves,
  SquareParking,
  Fuel,
  Video,
  Bell,
  Lightbulb,
  UserCheck,
  Siren,
  AlertTriangle,
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

// 🏢 인프라 시설 카테고리
export const INFRA_CATEGORIES = [
  { key: 'SUBWAY', label: '지하철', icon: TrainFront, color: '#2563EB' },
  { key: 'BUS_TERMINAL', label: '버스터미널', icon: Bus, color: '#0284C7' },
  { key: 'TRAIN', label: '기차역', icon: TrainTrack, color: '#1D4ED8' },
  { key: 'HOSPITAL', label: '병원', icon: Plus, color: '#EF4444' },
  { key: 'PHARMACY', label: '약국', icon: Pill, color: '#EC4899' },
  { key: 'SCHOOL', label: '학교', icon: GraduationCap, color: '#7C3AED' },
  { key: 'KINDERGARTEN', label: '유치원', icon: Baby, color: '#F43F5E' },
  { key: 'ACADEMY', label: '학원', icon: BookOpen, color: '#8B5CF6' },
  { key: 'LIBRARY', label: '도서관', icon: Library, color: '#6D28D9' },
  { key: 'PARK', label: '공원', icon: Trees, color: '#059669' },
  { key: 'POLICE', label: '경찰서', icon: ShieldCheck, color: '#1E3A8A' },
  { key: 'FIRE', label: '소방서', icon: Flame, color: '#DC2626' },
  {
    key: 'GOV_OFFICE',
    label: '행정복지센터',
    icon: Building2,
    color: '#4B5563',
  },
  { key: 'PUBLIC', label: '관공서', icon: Building, color: '#374151' },
  { key: 'POST_OFFICE', label: '우체국', icon: Mail, color: '#EA580C' },
  { key: 'BANK', label: '은행', icon: Landmark, color: '#0D9488' },
];

// 🛒 편의시설 카테고리
export const AMENITY_CATEGORIES = [
  { key: 'CONVENIENCE', label: '편의점', icon: Store, color: '#10B981' },
  { key: 'MART', label: '마트', icon: ShoppingCart, color: '#F59E0B' },
  { key: 'CAFE', label: '카페', icon: Coffee, color: '#B45309' },
  { key: 'FOOD', label: '음식점', icon: Utensils, color: '#F97316' },
  { key: 'CULTURE', label: '문화시설', icon: Film, color: '#6366F1' },
  { key: 'SPORTS', label: '체육시설', icon: Dumbbell, color: '#84CC16' },
  { key: 'SWIMMING', label: '수영장', icon: Waves, color: '#06B6D4' },
  { key: 'PARKING', label: '주차장', icon: SquareParking, color: '#64748B' },
  { key: 'GAS', label: '주유소', icon: Fuel, color: '#D97706' },
];

// 🛡️ 안전 시설 및 지표 카테고리
export const SAFETY_CATEGORIES = [
  // 범죄 안전
  {
    key: 'POLICE',
    label: '경찰서·지구대',
    icon: ShieldCheck,
    color: '#1E3A8A',
  }, // 남색
  { key: 'CCTV', label: 'CCTV', icon: Video, color: '#10B981' }, // 초록
  { key: 'EMERGENCY_BELL', label: '안전 비상벨', icon: Bell, color: '#F43F5E' }, // 코랄 핑크
  { key: 'STREET_LIGHT', label: '보안등', icon: Lightbulb, color: '#F59E0B' }, // 주황
  {
    key: 'CHILD_PROTECTION',
    label: '아동안전지킴이집',
    icon: UserCheck,
    color: '#8B5CF6',
  }, // 보라

  // 교통 안전 및 주의
  {
    key: 'SCHOOL_ZONE',
    label: '어린이보호구역',
    icon: Siren,
    color: '#059669',
  }, // 딥 그린
  {
    key: 'CHILD_ACCIDENT',
    label: '보행어린이 사고다발지역',
    icon: AlertTriangle,
    color: '#DC2626',
  }, // 경고 빨강
  {
    key: 'PEDESTRIAN_ACCIDENT',
    label: '보행자 사고다발지역',
    icon: AlertTriangle,
    color: '#EA580C',
  }, // 진한 주황
];

export const PREFERENCE_SLIDER_CONFIG = {
  COMMUTE_DISTANCE: {
    min: 0,
    max: 10000,
    step: 100,
    defaultValue: 1500,
    marks: ['0km', '2.5km', '5km', '7.5km', '10km'],
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
    title: '예산 적합',
    sub: '설정한 예산 안에서 부담이 적은 매물',
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
