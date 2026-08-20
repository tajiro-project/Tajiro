<template>
  <nav class="tab-bar">
    <router-link
      v-for="tab in tabs"
      :key="tab.key"
      :to="tab.to"
      class="tab-item"
      :class="{ active: active === tab.key }"
    >
      <component
        :is="tab.icon"
        :size="22"
        class="tab-icon"
      />
      <span class="tab-label">{{ tab.label }}</span>
    </router-link>
  </nav>
</template>

<script setup>
import { Home, Heart, Building2, ArrowUpDown, User } from 'lucide-vue-next';

defineProps({
  active: { type: String, default: 'home' }, // home | scrap | property | compare | my
});

const tabs = [
  { key: 'home', label: '홈', to: '/home', icon: Home },
  { key: 'scrap', label: '찜', to: '/favorites', icon: Heart },
  { key: 'property', label: '매물', to: '/properties', icon: Building2 },
  { key: 'compare', label: '비교함', to: '/compare-box', icon: ArrowUpDown },
  { key: 'my', label: '마이', to: '/mypage', icon: User },
];
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
  box-sizing: border-box;
}

.tab-item {
  display: flex;
  flex-direction: column;
  gap: 3px;
  align-items: center;
  justify-content: center;
  padding: 8px 6px;
  text-decoration: none; /* router-link 기본 밑줄 제거 */
  color: var(--kb-silver); /* 아이콘과 텍스트의 기본 비활성 색상 */
  transition:
    color 0.15s ease,
    opacity 0.15s ease;
}

.tab-icon {
  width: 22px;
  height: 22px;
  stroke-width: 1.5; /* SVG 선 두께 */
  opacity: 0.65;
  transition:
    transform 0.15s ease,
    opacity 0.15s ease;
}

.tab-label {
  font-size: 10px;
  color: inherit; /* 부모(.tab-item) 색상 상속 */
  line-height: 1;
}

/* 활성화(Active) 상태 */
.tab-item.active {
  color: var(--kb-gray);
}

.tab-item.active .tab-icon {
  opacity: 1;
  stroke-width: 2.2; /* 활성화 시 아이콘을 살짝 더 선명하게 */
}

.tab-item.active .tab-label {
  font-weight: 600;
}
</style>
