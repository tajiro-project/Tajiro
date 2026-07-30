<template>
  <nav class="tab-bar">
    <router-link v-for="tab in tabs" :key="tab.key" :to="tab.to" class="tab-item" :class="{ active: active === tab.key }">
      <img :src="tab.icon" alt="" class="tab-icon" />
      <span class="tab-label">{{ tab.label }}</span>
    </router-link>
  </nav>
</template>

<script setup>
import iconHome from '@/assets/img/tab-home-active.svg'
import iconScrap from '@/assets/img/tab-scrap.svg'
import iconBuilding from '@/assets/img/tab-building.svg'
import iconCompare from '@/assets/img/tab-compare.svg'
import iconMy from '@/assets/img/tab-my.svg'

defineProps({
  active: { type: String, default: 'home' }, // home | scrap | property | compare | my
})

const tabs = [
  { key: 'home', label: '홈', to: '/home', icon: iconHome },
  { key: 'scrap', label: '스크랩', to: '/favorites', icon: iconScrap },
  { key: 'property', label: '매물', to: '/properties', icon: iconBuilding },
  { key: 'compare', label: '비교함', to: '/compare-box', icon: iconCompare },
  { key: 'my', label: '마이', to: '/mypage', icon: iconMy },
]
</script>

<style scoped>
.tab-bar {
  position: sticky;
  bottom: 0;
  z-index: 30;
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 2px 12px 6px;
  background: var(--white);
  border-top: 1px solid var(--border);
  margin-top: auto;
}
.tab-item {
  display: flex;
  flex-direction: column;
  gap: 3px;
  align-items: center;
  justify-content: center;
  padding: 8px 6px;
}
.tab-icon {
  width: 22px;
  height: 22px;
  opacity: 0.55;
}
.tab-label {
  font-size: 10px;
  color: var(--kb-silver);
}
.tab-item.active .tab-icon {
  opacity: 1;
}
.tab-item.active .tab-label {
  color: var(--kb-gray);
  font-weight: 500;
}
</style>
