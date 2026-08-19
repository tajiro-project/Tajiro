export const TOUR_GROUPS = [
  'home',
  'preferences',
  'property-list',
  'property-detail',
  'compare-box',
  'compare',
];

export const ONBOARDING_STEPS = {
  home: [
    {
      target: 'home-property-search',
      title: '매물 검색',
      text: '가치관을 입력하면 나에게 맞는 매물을 찾아드려요.',
    },
    {
      target: 'home-benefit',
      title: '정책 · 대출',
      text: '청년 정책과 KB 금융상품을 매칭해드려요.',
    },
    {
      target: 'home-safety-guide',
      title: '안전 거래 가이드',
      text: '계약 전 확인해야 할 안전 정보를 알려드려요.',
    },
  ],
  preferences: [
    {
      target: 'preferences-wizard',
      title: '가치관 설정',
      text: '총 4단계로 위치·조건·인프라·우선순위를 입력해요. 여기서 고른 값이 매물 추천, 비교함, 비교 점수에 계속 쓰이니 정확히 입력해주세요.',
    },
    {
      target: 'preferences-quick-setup',
      title: '빠른 이동',
      text: '위치만 정하면, 유형별 추천 조건으로 나머지 단계를 한 번에 채워서 바로 완료할 수 있어요.',
    },
  ],
  'property-list': [
    {
      target: 'property-filter-chips',
      title: '필터',
      text: '조건을 세밀하게 좁힐 수 있어요.',
    },
    {
      target: 'property-sort',
      title: '정렬',
      text: '추천순 · 거리순 · 가격순 등으로 바꿀 수 있어요.',
    },
    {
      target: 'property-priority',
      title: '가치관 우선순위',
      text: '가치관 설정에서 정한 우선순위를 여기서도 바로 바꿀 수 있어요.',
    },
    {
      target: 'property-map',
      title: '지도 연동',
      text: '지도에서 매물을 눌러도 리스트가 같이 움직여요. 매물을 선택하면 주변 인프라를 켜고 끄는 패널도 나타나요.',
    },
  ],
  'property-detail': [
    {
      target: 'detail-score',
      title: '가치관 반영 점수',
      text: '내 가치관 기준으로 이 매물이 몇 점인지 보여줘요.',
    },
    {
      target: 'detail-banners',
      title: '주변 정보',
      text: '매물 주변 인프라 · 안전 정보를 더 자세히 볼 수 있어요.',
    },
    {
      target: 'detail-compare-btn',
      title: '비교함 담기',
      text: '마음에 드는 매물을 담아두고, 아래 탭바의 비교함에서 2~3개를 한번에 비교해요.',
    },
  ],
  'compare-box': [
    {
      target: 'comparebox-settings',
      title: '비교 기준',
      text: '눌러서 바꿀 수 있어요. 선호 위치는 통근 시간의 기준점이 되고, 우선순위는 이 비교에만 적용돼요.',
    },
    {
      target: 'comparebox-start',
      title: '비교 시작',
      text: '2~3개를 담으면 AI가 비교 코칭까지 해드려요.',
    },
  ],
  compare: [
    {
      target: 'compare-ai-card',
      title: 'AI 의사결정 코치',
      text: '가치관 기준으로 추천 매물과 이유를 알려줘요.',
    },
    {
      target: 'compare-metric-tabs',
      title: '종합 비교',
      text: '탭을 눌러서 직주근접·가격·인프라 등 지표를 바꿔볼 수 있어요.',
    },
    {
      target: 'compare-safety-table',
      title: '안전 비교',
      text: 'CCTV · 경찰거리 등 안전 지표를 매물별로 비교해요.',
    },
  ],
};
