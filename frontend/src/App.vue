<script setup>
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import AppTabBar from '@/components/AppTabBar.vue';

const route = useRoute();

const activeTab = computed(() => {
  if (route.path === '/favorites') return 'scrap';

  if (
    route.path.startsWith('/properties') ||
    route.path.startsWith('/preferences')
  ) {
    return 'property';
  }

  if (route.path.startsWith('/compare') || route.path.startsWith('/reports')) {
    return 'compare';
  }

  if (route.path === '/mypage') return 'my';

  return 'home';
});

const showTabBar = computed(() => {
  return !['/', '/login', '/register'].includes(route.path);
});
</script>

<template>
  <div class="app-frame">
    <main class="route-content">
      <router-view />
    </main>

    <AppTabBar
      v-if="showTabBar"
      :active="activeTab"
      class="global-tab-bar"
    />
  </div>
</template>
