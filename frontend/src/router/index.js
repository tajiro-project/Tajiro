import { createRouter, createWebHistory } from 'vue-router';

const routes = [
  {
    path: '/',
    name: 'placeholder',
    component: () => import('@/views/PlaceholderView.vue'),
  },
  {
    path: '/properties',
    name: 'property-list',
    component: () => import('@/views/property/PropertyListView.vue'),
  },
  {
    path: '/compare-box',
    name: 'compare-box',
    component: () => import('@/views/compare/CompareBoxView.vue'),
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
  },
  {
    path: '/benefits/policies',
    name: 'policy-match',
    component: () => import('@/views/benefit/BenefitMatchView.vue'),
  }, // 12-1 / 12-2
  {
    path: '/benefits/kb',
    name: 'kb-match',
    component: () => import('@/views/benefit/BenefitMatchView.vue'),
    props: { initialTab: 'kb' },
  }, // 12-2 KB
  {
    path: '/preferences',
    name: 'preferences',
    component: () => import('@/views/preferences/PreferenceWizardView.vue'),
  },
  {
    path: '/mypage',
    name: 'mypage',
    component: () => import('@/views/mypage/MyPageView.vue'),
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('@/views/auth/RegisterView.vue'),
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 };
  },
});

export default router;
