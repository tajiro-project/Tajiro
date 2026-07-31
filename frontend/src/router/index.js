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
];

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 };
  },
});

export default router;
