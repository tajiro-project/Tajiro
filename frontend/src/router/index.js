import { createRouter, createWebHistory } from 'vue-router';

const routes = [
  {
    path: '/',
    name: 'splash',
    component: () => import('@/views/SplashView.vue'),
  },
  {
    path: '/properties',
    name: 'property-list',
    component: () => import('@/views/property/PropertyListView.vue'),
    meta: { headerTitle: '매물 검색 결과' },
  },
  {
    path: '/compare-box',
    name: 'compare-box',
    component: () => import('@/views/compare/CompareBoxView.vue'),
    meta: { headerTitle: '비교함' },
  },
  {
    path: '/home',
    name: 'home',
    component: () => import('@/views/home/HomeView.vue'),
  },
  {
    path: '/compare',
    name: 'compare',
    component: () => import('@/views/compare/CompareView.vue'),
    meta: { headerTitle: '매물 비교', headerBackTo: '/compare-box' },
  },
  {
    path: '/reports',
    name: 'reports',
    component: () => import('@/views/compare/ReportListView.vue'),
    meta: { headerTitle: '비교 리포트 보관함' },
  },
  {
    path: '/reports/:reportId',
    name: 'report-detail',
    component: () => import('@/views/compare/CompareView.vue'),
    meta: { headerTitle: '비교 리포트 상세', headerBackTo: '/reports' },
  },
  {
    path: '/comparison-reports/:reportId',
    redirect: (to) => `/reports/${to.params.reportId}`,
  },
  {
    path: '/comparison-reports',
    redirect: '/reports',
  },
  {
    path: '/benefits/policies',
    name: 'policy-match',
    component: () => import('@/views/benefit/BenefitMatchView.vue'),
    meta: { headerTitle: '정책 · 금융 추천', headerAction: 'edit-profile' },
  }, // 12-1 / 12-2
  {
    path: '/benefits/kb',
    name: 'kb-match',
    component: () => import('@/views/benefit/BenefitMatchView.vue'),
    props: { initialTab: 'kb' },
    meta: { headerTitle: '정책 · 금융 추천', headerAction: 'edit-profile' },
  }, // 12-2 KB
  {
    path: '/properties/:id',
    component: () => import('@/views/property/PropertyLayout.vue'), // 부모 레이아웃
    children: [
      {
        path: '', // /properties/158 접속 시
        name: 'property-detail',
        component: () => import('@/views/property/PropertyDetailView.vue'),
        meta: { headerTitle: '매물 상세' },
      },
      {
        path: 'infra', // /properties/158/infra 접속 시
        name: 'property-infra',
        component: () => import('@/views/property/PropertyInfraView.vue'),
        meta: { headerTitle: '주변 생활 인프라' },
      },
      {
        path: 'safety', // /properties/158/safety 접속 시
        name: 'property-safety',
        component: () => import('@/views/property/PropertySafetyView.vue'),
        meta: { headerTitle: '안전 정보' },
      },
    ],
  },
  {
    path: '/preferences',
    name: 'preferences',
    component: () => import('@/views/preferences/PreferenceWizardView.vue'),
    meta: { headerTitle: '가치관 설정' },
  },
  {
    path: '/preferences/:step',
    name: 'preferences',
    component: () => import('@/views/preferences/PreferenceWizardView.vue'),
    meta: { headerTitle: '가치관 설정' },
  },
  {
    path: '/mypage',
    name: 'mypage',
    component: () => import('@/views/mypage/MyPageView.vue'),
    meta: { headerTitle: '마이페이지' },
  },
  {
    path: '/favorites',
    name: 'favorites',
    component: () => import('@/views/favorite/FavoriteListView.vue'),
    meta: { headerTitle: '스크랩한 매물' },
  },
  {
    path: '/profile-setup',
    name: 'profile-setup',
    component: () => import('@/views/profile/ProfileSetupView.vue'),
    meta: { headerTitle: '내 정보 입력', headerBack: true },
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('@/views/auth/RegisterView.vue'),
    meta: { headerTitle: '회원가입' },
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/auth/LoginView.vue'),
  },
  {
    path: '/policies/:id',
    name: 'policy-detail',
    component: () => import('@/views/benefit/PolicyDetailView.vue'),
    meta: { headerTitle: '정책 상세' },
  },
  {
    path: '/financial-products/:id',
    name: 'financial-product-detail',
    component: () => import('@/views/benefit/LoanDetailView.vue'),
    meta: { headerTitle: '대출상품 상세' },
  },
  {
    path: '/safety-guide',
    name: 'safety-guide',
    component: () => import('@/views/safety/SafetyGuideView.vue'),
    meta: { headerTitle: '안전 거래 가이드' },
  },
  {
    path: '/seller/home',
    name: 'seller-home',
    component: () => import('@/views/seller/SellerHomeView.vue'),
    meta: { headerTitle: '홈' },
  },
  {
    path: '/seller/properties',
    name: 'seller-properties',
    component: () => import('@/views/seller/SellerPropertyListView.vue'),
    meta: { headerTitle: '내 매물 관리', headerBackTo: '/seller/home' },
  },
  {
    path: '/seller/profile',
    name: 'seller-profile',
    component: () => import('@/views/seller/SellerProfileEditView.vue'),
    meta: { headerTitle: '내 정보 수정', headerBackTo: '/seller/home' },
  },
  {
    path: '/seller/register/:step',
    name: 'seller-register',
    component: () => import('@/views/seller/SellerRegisterView.vue'),
    meta: { headerTitle: '매물 등록' },
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 };
  },
});

const PUBLIC_PATHS = ['/', '/login', '/register'];

router.beforeEach((to) => {
  const isLoggedIn = !!localStorage.getItem('accessToken');

  if (!PUBLIC_PATHS.includes(to.path) && !isLoggedIn) {
    return { path: '/login', query: { redirect: to.fullPath } };
  }

  if (to.path.startsWith('/seller') && isLoggedIn) {
    const role = localStorage.getItem('userRole');
    if (role !== 'SELLER') {
      return { path: '/home' };
    }
  }
});

export default router;
