<script setup>
import { computed, provide, reactive } from 'vue';
import { useRoute } from 'vue-router';
import AppTabBar from '@/components/AppTabBar.vue';
import PageHeader from '@/components/PageHeader.vue';
import { ChevronDown, MapPin } from 'lucide-vue-next';

const route = useRoute();
const propertyListHeader = reactive({
  locationLabel: '매물 검색 결과',
  locationPickerRequestId: 0,
  setLocationLabel(label) {
    this.locationLabel = label || '매물 검색 결과';
  },
  requestLocationPicker() {
    this.locationPickerRequestId += 1;
  },
});

provide('propertyListHeader', propertyListHeader);

const headerTitle = computed(() => route.meta.headerTitle ?? '');
const showHeader = computed(() => Boolean(headerTitle.value));
const headerBack = computed(() => {
  const value = route.meta.headerBack;
  return typeof value === 'function' ? value(route) : (value ?? true);
});
const headerBackTo = computed(() => route.meta.headerBackTo ?? null);

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
  return (
    !['/', '/login', '/register'].includes(route.path) &&
    !route.path.startsWith('/seller')
  );
});

const KEEP_ALIVE_VIEWS = [
  'PropertyListView',
  'BenefitMatchView',
  'PropertyDetailView',
];
</script>

<template>
  <div class="app-frame">
    <PageHeader
      v-if="showHeader"
      :title="headerTitle"
      :back="headerBack"
      :back-to="headerBackTo"
    >
      <template
        v-if="route.name === 'property-list'"
        #title
      >
        <button
          class="header-location"
          @click="propertyListHeader.requestLocationPicker"
        >
          <MapPin
            :size="18"
            :stroke-width="2"
          />
          <span>{{ propertyListHeader.locationLabel }}</span>
          <ChevronDown
            :size="14"
            :stroke-width="2"
          />
        </button>
      </template>
      <template
        v-if="route.meta.headerAction === 'edit-profile'"
        #right
      >
        <router-link
          class="header-action"
          to="/profile-setup"
        >
          내 정보 수정
        </router-link>
      </template>
    </PageHeader>

    <main class="route-content">
      <router-view v-slot="{ Component }">
        <KeepAlive :include="KEEP_ALIVE_VIEWS">
          <component :is="Component" />
        </KeepAlive>
      </router-view>
    </main>

    <AppTabBar
      v-if="showTabBar"
      :active="activeTab"
      class="global-tab-bar"
    />
  </div>
</template>

<style scoped>
.header-location {
  display: flex;
  align-items: center;
  gap: 7px;
  min-width: 0;
  max-width: min(280px, calc(100vw - 76px));
  padding: 0;
  border: 0;
  background: transparent;
  color: var(--text-primary);
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
}

.header-location svg {
  flex-shrink: 0;
}

.header-location svg:first-child {
  color: #fe7b00;
}

.header-location svg:last-child {
  color: #545045;
}

.header-location span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.header-action {
  padding: 5px 10px;
  border-radius: 999px;
  background: var(--kb-yellow-header);
  color: var(--text-primary);
  font-size: 12px;
  font-weight: 700;
  text-decoration: none;
}
</style>
