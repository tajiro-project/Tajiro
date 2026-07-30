import { createRouter, createWebHistory } from 'vue-router';
import PropertyListView from '@/views/property/PropertyListView.vue';

const routes = [
  {
    path: '/',
    name: 'placeholder',
    component: () => import('@/views/PlaceholderView.vue'),
  },
  {
    path: '/properties',
    name: 'property-list',
    component: PropertyListView,
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
