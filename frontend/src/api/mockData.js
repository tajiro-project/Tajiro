// 와이어프레임 예시 데이터 기반 목데이터 (백엔드 미기동 시 폴백)

export const mockProperties = [
  {
    propertyId: "P01",
    title: "상남동 오피스텔",
    propertyType: "오피스텔",
    buildingName: "에스하임",
    tradeType: "월세",
    deposit: 500,
    monthlyRent: 45,
    maintenanceFee: 7,
    areaM2: 26.4,
    areaPyeong: 8,
    floorInfo: "21 / 34층",
    thumbnailUrl: null,
    latitude: 35.2223,
    longitude: 128.6812,
    recommendScore: 92,
    updatedDate: "2026-07-20T09:00:00",
  },
  {
    propertyId: "P02",
    title: "답십리동 원룸",
    propertyType: "원룸",
    buildingName: "",
    tradeType: "월세",
    deposit: 500,
    monthlyRent: 45,
    maintenanceFee: 7,
    areaM2: 26.4,
    areaPyeong: 8,
    floorInfo: "6 / 34층",
    thumbnailUrl: null,
    latitude: 37.5714,
    longitude: 127.0521,
    recommendScore: 88,
    updatedDate: "2026-07-20T13:20:00",
  },
  {
    propertyId: "P03",
    title: "왕십리동 원룸",
    propertyType: "원룸",
    buildingName: "",
    tradeType: "전세",
    deposit: 8000,
    maintenanceFee: 7,
    areaM2: 26.4,
    areaPyeong: 8,
    floorInfo: "6 / 34층",
    thumbnailUrl: null,
    latitude: 37.5613,
    longitude: 127.0374,
    recommendScore: 85,
    updatedDate: "2026-07-20T16:10:00",
  },
  {
    propertyId: "P04",
    title: "홍은동 투룸",
    propertyType: "투룸",
    buildingName: "",
    tradeType: "월세",
    deposit: 1000,
    monthlyRent: 55,
    maintenanceFee: 5,
    areaM2: 39.6,
    areaPyeong: 12,
    floorInfo: "6 / 34층",
    thumbnailUrl: null,
    latitude: 37.5893,
    longitude: 126.9422,
    recommendScore: 81,
    updatedDate: "2026-07-10T11:00:00",
  },
  {
    propertyId: "P05",
    title: "석수아파트",
    propertyType: "아파트",
    buildingName: "석수아파트",
    tradeType: "매매",
    deposit: 35500,
    monthlyRent: 55,
    maintenanceFee: 5,
    areaM2: 39.6,
    areaPyeong: 12,
    floorInfo: "6 / 34층",
    thumbnailUrl: null,
    latitude: 37.5893,
    longitude: 126.9422,
    recommendScore: 81,
  },
];

export const mockPropertyDetail = (id) => {
  const base =
    mockProperties.find((p) => p.propertyId === id) ?? mockProperties[0];
  return {
    id: base.propertyId,
    images: [],
    title: base.title,
    propertyType: base.propertyType,
    tradeType: base.tradeType,
    deposit: base.deposit,
    monthlyRent: base.monthlyRent,
    maintenanceFee: base.maintenanceFee,
    areaM2: base.areaM2,
    floorInfo: base.floorInfo,
    roomNumber: "302호",
    roomNum: 1,
    bathroomNum: 1,
    parkAvailability: true,
    address: "경남 창원시 성산구 상남동 12-3",
    dong: "상남동",
    propertyDescription:
      "역세권 도보 5분, 채광 좋은 남향 매물입니다. 풀옵션(에어컨·세탁기·냉장고) 포함이며 즉시 입주 가능합니다.",
    availableDate: "2026-08-15",
    moveInDate: "즉시 입주",
    discussionStatus: "협의 가능",
    latitude: base.latitude,
    longitude: base.longitude,
    marketEvaluation: "적정",
    recommendScore: base.recommendScore,
    workplaceDistanceMeters: 2400,
    commuteMinutes: 24,
    commuteMode: "TRANSIT",
    infraSummary: [
      { category: "CONVENIENCE", count: 3 },
      { category: "HOSPITAL", count: 2 },
      { category: "SUBWAY", count: 1 },
    ],
    realtorPreview: {
      name: "KB 공인중개사 상남점",
      address: "경남 창원시 성산구 상남로 89",
      phone: "055-123-4567",
      open: true,
    },
    matchScore: 92,
  };
};

export const mockSafety = {
  radiusMeters: 500,
  updatedAt: "2026-07-01T00:00:00",
  policeStation: { name: "상남지구대", distanceMeters: 350 },
  safetyCategories: [
    { category: "CCTV", categoryName: "CCTV", count: 34 },
    { category: "BELL", categoryName: "안전 비상벨", count: 6 },
    { category: "LIGHT", categoryName: "보안등", count: 41 },
    { category: "CHILD_ZONE", categoryName: "어린이 보호구역", count: 3 },
    { category: "WOMEN_ZONE", categoryName: "여성 안심 귀갓길", count: 2 },
    { category: "ACCIDENT", categoryName: "교통사고 다발지", count: 1 },
  ],
};

export const mockInfras = [
  {
    category: "SUBWAY",
    name: "상남역 2호선",
    distanceMeters: 380,
    walkMinutes: 5,
    latitude: 35.2231,
    longitude: 128.6825,
  },
  {
    category: "BUS",
    name: "상남시장 정류장",
    distanceMeters: 120,
    walkMinutes: 2,
    latitude: 35.2225,
    longitude: 128.6809,
  },
  {
    category: "CONVENIENCE",
    name: "GS25 상남점",
    distanceMeters: 90,
    walkMinutes: 1,
    latitude: 35.2221,
    longitude: 128.6816,
  },
  {
    category: "MART",
    name: "홈플러스 상남점",
    distanceMeters: 650,
    walkMinutes: 9,
    latitude: 35.2205,
    longitude: 128.6841,
  },
  {
    category: "HOSPITAL",
    name: "상남연세의원",
    distanceMeters: 210,
    walkMinutes: 3,
    latitude: 35.2228,
    longitude: 128.6803,
  },
  {
    category: "PARK",
    name: "상남공원",
    distanceMeters: 480,
    walkMinutes: 7,
    latitude: 35.2211,
    longitude: 128.6795,
  },
  {
    category: "GYM",
    name: "스포애니 상남점",
    distanceMeters: 300,
    walkMinutes: 4,
    latitude: 35.2219,
    longitude: 128.683,
  },
  {
    category: "CAFE",
    name: "스타벅스 상남점",
    distanceMeters: 150,
    walkMinutes: 2,
    latitude: 35.2224,
    longitude: 128.682,
  },
];

export const mockComparables = [
  {
    propertyId: "C01",
    title: "상남동 오피스텔 A",
    dealType: "월세",
    price: "500/43",
    areaM2: 25.2,
  },
  {
    propertyId: "C02",
    title: "상남동 오피스텔 B",
    dealType: "월세",
    price: "500/47",
    areaM2: 27.1,
  },
  {
    propertyId: "C03",
    title: "가음동 오피스텔",
    dealType: "월세",
    price: "300/50",
    areaM2: 24.8,
  },
];

export const mockMarketEvaluation = { medianPrice: 46, diffPercent: -2.2 };

export const mockCommute = {
  distanceMeters: 2400,
  minutes: 24,
  mode: "TRANSIT",
};

export const mockCompareBox = mockProperties.slice(0, 3).map((p) => ({
  propertyId: p.propertyId,
  title: p.title,
  propertyType: p.propertyType,
  tradeType: p.tradeType,
  deposit: p.deposit,
  monthlyRent: p.monthlyRent,
  areaM2: p.areaM2,
  maintenanceFee: p.maintenanceFee,
  floorInfo: p.floorInfo,
}));

export const mockComparisonMetrics = {
  items: [
    {
      propertyId: "P01",
      commuteMinutes: 48,
      monthlyRent: 45,
      maintenanceFee: 7,
      infraCount: 12,
      amenityCount: 14,
      areaM2: 26.4,
      evaluationScore: -2.2,
      cctvCountWithin500m: 34,
      policeNearestDistanceMeters: 350,
      childrenCountWithin500m: 3,
      bellCountWithin500m: 6,
      safetyLightCountWithin500m: 41,
    },
    {
      propertyId: "P02",
      commuteMinutes: 62,
      monthlyRent: 45,
      maintenanceFee: 7,
      infraCount: 9,
      amenityCount: 11,
      areaM2: 26.4,
      evaluationScore: 1.8,
      cctvCountWithin500m: 21,
      policeNearestDistanceMeters: 620,
      childrenCountWithin500m: 2,
      bellCountWithin500m: 3,
      safetyLightCountWithin500m: 32,
    },
    {
      propertyId: "P03",
      commuteMinutes: 55,
      monthlyRent: 45,
      maintenanceFee: 7,
      infraCount: 11,
      amenityCount: 13,
      areaM2: 26.4,
      evaluationScore: 4.1,
      cctvCountWithin500m: 27,
      policeNearestDistanceMeters: 480,
      childrenCountWithin500m: 1,
      bellCountWithin500m: 4,
      safetyLightCountWithin500m: 36,
    },
  ],
};

export const mockAiCoaching = {
  aiPropertySummaryText:
    "사용자 가치관 1순위인 직주근접 기준으로 비교했어요. P01 상남동 오피스텔: 직주근접·인프라 우수, 월세 대비 적정 시세. P02 답십리동 원룸: 통근 시간이 가장 길지만 조용한 주거지. P03 왕십리동 원룸: 균형형이나 시세가 다소 높음.",
  aiSummary:
    "직주근접과 가격 낮은 순을 중시하는 가치관 기준으로 상남동 오피스텔이 가장 적합해요. 통근 왕복 48분으로 가장 짧고, 시세도 주변 중앙값 대비 2.2% 낮아요.",
  aiRecommendedPropertyId: "P01",
  aiAtp:
    "계약 전 관리비 세부 항목(수도·인터넷 포함 여부)과 주차 가능 대수를 꼭 확인하세요.",
};

export const mockFavorites = [
  {
    propertyId: "P01",
    title: "상남동 오피스텔",
    propertyType: "오피스텔",
    tradeType: "월세",
    deposit: 500,
    monthlyRent: 45,
    areaM2: 27,
    floorInfo: "3층",
    address: "성산구 상남동",
    commuteLabel: "직장 15분",
    evaluationScore: -2.2,
    imageUrl: null,
  },
  {
    propertyId: "P02",
    title: "팔용동 원룸",
    propertyType: "원룸",
    tradeType: "월세",
    deposit: 300,
    monthlyRent: 38,
    areaM2: 24,
    floorInfo: "5층",
    address: "의창구 팔용동",
    commuteLabel: "버스 20분",
    evaluationScore: 1.8,
    imageUrl: null,
  },
  {
    propertyId: "P03",
    title: "반림동 오피스텔",
    propertyType: "오피스텔",
    tradeType: "전세",
    deposit: 9500,
    monthlyRent: 0,
    areaM2: 30,
    floorInfo: "6층",
    address: "성산구 반림동",
    commuteLabel: "버스 18분",
    evaluationScore: 4.1,
    imageUrl: null,
  },
  {
    propertyId: "P04",
    title: "상남동 투룸",
    propertyType: "투룸",
    tradeType: "월세",
    deposit: 1000,
    monthlyRent: 40,
    areaM2: 40,
    floorInfo: "2층",
    address: "상남동",
    commuteLabel: "도보 18분",
    evaluationScore: 8.4,
    imageUrl: null,
  },
  {
    propertyId: "P05",
    title: "용호동 원룸",
    propertyType: "원룸",
    tradeType: "전세",
    deposit: 8000,
    monthlyRent: 0,
    areaM2: 27,
    floorInfo: "4층",
    address: "의창구 용호동",
    commuteLabel: "버스 22분",
    evaluationScore: -0.5,
    imageUrl: null,
  },
];

const createComparedProperties = (propertyIds) =>
  propertyIds
    .map((id) => mockProperties.find((property) => property.propertyId === id))
    .filter(Boolean)
    .map((property) => ({
      propertyId: property.propertyId,
      title: property.title,
      deposit: property.deposit,
      monthlyRent: property.monthlyRent,
      maintenanceFee: property.maintenanceFee,
      areaM2: property.areaM2,
      floorInfo: property.floorInfo,
      updatedDate: property.updatedDate,
    }));
export const mockReports = [
  {
    reportId: "R01",
    title: "상남동 오피스텔 외 2건 비교",
    comparedPropertyIds: ["P01", "P02", "P03"],
    comparedProperties: createComparedProperties(["P01", "P02", "P03"]),
    aiPropertySummaryText: mockAiCoaching.aiPropertySummaryText,
    aiSummary: mockAiCoaching.aiSummary,
    aiRecommendedPropertyId: "P01",
    aiAtp: mockAiCoaching.aiAtp,
    createdAt: "2026-07-21T10:22:00",
  },
  {
    reportId: "R02",
    title: "답십리동 원룸 외 1건 비교",
    comparedPropertyIds: ["P02", "P03"],
    comparedProperties: createComparedProperties(["P02", "P03"]),
    aiPropertySummaryText:
      "P02 답십리동 원룸과 P03 왕십리동 원룸을 직주근접과 주거비 기준으로 비교했어요.",
    aiSummary:
      "월세와 관리비 부담은 두 매물이 비슷하지만, 출퇴근 시간을 우선하면 P02를 먼저 검토하는 편이 합리적이에요.",
    aiRecommendedPropertyId: "P02",
    aiAtp: "계약 전 관리비 포함 항목과 실제 통근 경로를 다시 확인하세요.",
    createdAt: "2026-07-14T18:03:00",
  },
];
export const mockReportDetail = (reportId) =>
  mockReports.find((report) => report.reportId === reportId) ?? mockReports[0];

export const mockPolicies = [
  {
    id: "PL01",
    title: "청년 월세 한시 특별지원",
    benefitAmount: "월 최대 20만 원 (12개월)",
  },
  {
    id: "PL02",
    title: "창원시 청년 전입 지원금",
    benefitAmount: "최대 100만 원",
  },
  { id: "PL03", title: "청년 이사비 지원 사업", benefitAmount: "최대 40만 원" },
];

export const mockPolicyDetail = {
  title: "청년 월세 한시 특별지원",
  region: "경남 창원시",
  targetMinAge: 19,
  targetMaxAge: 34,
  benefitAmount: "월 최대 20만 원 (12개월)",
  applicationPeriod: "2026.01.01 ~ 2026.12.31",
  agency: "창원시청 청년정책과",
  applyMethod: "복지로 온라인 신청 또는 주소지 행정복지센터 방문",
  requiredDocuments:
    "임대차계약서 사본, 월세 이체 증빙, 가족관계증명서, 소득 증빙 서류",
};

export const mockFinancialProducts = [
  {
    id: "F01",
    provider: "KB국민은행",
    productName: "KB 청년 전세자금대출",
    tradeType: "전세",
    productType: "보증",
    minRate: 3.2,
    maxRate: 4.1,
    maxLimitAmount: 20000,
    loanLimit: "보증금의 90% 이내",
  },
  {
    id: "F02",
    provider: "KB국민은행",
    productName: "KB 버팀목 전세자금",
    tradeType: "전세",
    productType: "기타",
    minRate: 2.1,
    maxRate: 2.9,
    maxLimitAmount: 12000,
    loanLimit: "보증금의 80% 이내",
  },
  {
    id: "F03",
    provider: "KB국민은행",
    productName: "KB 청년 월세대출",
    tradeType: "월세",
    productType: "보증",
    minRate: 3.0,
    maxRate: 3.6,
    maxLimitAmount: 1200,
    loanLimit: "월 60만 원 이내",
  },
];

export const mockFinancialProductDetail = {
  id: "F01",
  provider: "KB국민은행",
  productName: "KB 청년 전세자금대출",
  tradeType: "전세",
  productType: "보증",
  minRate: 3.2,
  maxRate: 4.1,
  maxLimitAmount: 20000,
  loanLimit: "보증금의 90% 이내",
  rateDescription:
    "기준금리(COFIX) + 가산금리, 우대금리 최대 0.9%p (급여이체 0.3%p, 카드실적 0.3%p, 자동이체 0.3%p)",
  eligibility:
    "만 19세~34세 무주택 세대주, 연 소득 5천만 원 이하, 임차보증금 3억 원 이하 주택",
  requiredDocs:
    "신분증, 확정일자부 임대차계약서, 주민등록등본, 소득 증빙 서류, 보증금 5% 이상 납입 영수증",
  applicationUrl: "https://obank.kbstar.com",
  isActive: true,
  referenceDate: "2026-07-01",
  validEndDate: "2026-12-31",
};

export const mockRegions = [
  {
    regionName: "경남 창원시 성산구",
    legalDongCode: "4812310100",
    sggCode: "48123",
    lowerRegionName: "상남동",
    parentRegionCode: "48",
  },
  {
    regionName: "서울 동대문구",
    legalDongCode: "1123010100",
    sggCode: "11230",
    lowerRegionName: "답십리동",
    parentRegionCode: "11",
  },
  {
    regionName: "서울 성동구",
    legalDongCode: "1120010100",
    sggCode: "11200",
    lowerRegionName: "왕십리동",
    parentRegionCode: "11",
  },
];

export const mockDashboard = {
  name: "김민주",
  targetRegion: "경남 창원시",
  birthDate: "2000-01-06",
  favoriteCount: 3,
  reportCount: 2,
  priorities: [
    { criterion: "COMMUTE", priorityOrder: 1 },
    { criterion: "COST", priorityOrder: 2 },
    { criterion: "AMENITY", priorityOrder: 3 },
  ],
};

export const mockProfile = {
  targetRegion: "경남 창원시",
  birthDate: "2000-01-06",
  monthlyIncome: 250,
  assetAmount: 1800,
  jobStatus: "재직 중",
  target_sgg_code: "48123",
};

export const mockTerms = {
  1: {
    id: 1,
    type: "SERVICE",
    title: "서비스 이용약관",
    version: "1.2",
    content:
      '제1조(목적)\n이 약관은 타지로(이하 "회사")가 제공하는 서비스의 이용 조건 및 절차, 회사와 회원 간의 권리·의무를 규정함을 목적으로 합니다.\n\n제2조(정의)\n"서비스"란 회사가 제공하는 정착 추천, 매물 비교, 정책·금융 매칭 등 일체의 서비스를 말합니다.\n\n제3조(약관의 효력)\n본 약관은 서비스 화면에 게시하거나 기타 방법으로 공지함으로써 효력이 발생합니다.',
  },
  2: {
    id: 2,
    type: "PRIVACY",
    title: "개인정보 수집·이용 동의",
    version: "1.1",
    content:
      "1. 수집 항목: 이름, 이메일, 생년월일, 소득·자산 정보(선택)\n2. 수집 목적: 회원 관리, 맞춤 매물 추천, 정책·금융상품 매칭\n3. 보유 기간: 회원 탈퇴 시까지(관계 법령에 따라 일정 기간 보관될 수 있음)",
  },
  3: {
    id: 3,
    type: "MARKETING",
    title: "마케팅 정보 수신 동의(선택)",
    version: "1.0",
    content:
      "이벤트, 혜택 등 마케팅 정보를 이메일·푸시로 받아보실 수 있습니다. 동의하지 않아도 서비스 이용에 제한이 없습니다.",
  },
};

// 안전 거래 가이드 (프론트 목업 — 기능명세 61)
export const safetyGuideData = {
  intro: {
    title: "전세사기·계약 실수, 미리 막아요",
    subtitle:
      "청년 첫 계약을 위한 단계별 체크리스트예요. 하나씩 확인하며 안전하게 진행하세요.",
    total: 16,
  },
  tabs: ["계약", "전세보증보험"],
};
