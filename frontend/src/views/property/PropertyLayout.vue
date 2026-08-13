<!-- src/views/property/PropertyLayout.vue -->
<template>
  <div class="property-layout">
    <router-view v-if="!isLoading" />
    <div
      v-else
      class="loading-state"
    >
      <span>매물 정보를 불러오는 중...</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, provide, watch } from 'vue';
import { useRoute } from 'vue-router';
import { propertyApi } from '@/api/services';

const route = useRoute();
const propertyDetail = ref(null);
const isLoading = ref(true);

// 💡 호수('202호' 등)를 정제한 건물명 Computed
const buildingName = computed(() => {
  const rawTitle =
    propertyDetail.value?.title ||
    propertyDetail.value?.buildingName ||
    propertyDetail.value?.name;

  if (!rawTitle) return '건물명 정보 없음';

  // 정규식으로 끝자리의 '호수' 제거 및 트림 처리
  return rawTitle.replace(/\s*\d+호$/, '').trim();
});

// 하위 자식 컴포넌트에 전체 객체와 필터링된 건물명을 모두 전달
provide('propertyDetail', propertyDetail);
provide('buildingName', buildingName);

const fetchDetail = async (id) => {
  if (!id) return;
  isLoading.value = true;
  try {
    const res = await propertyApi.getPropertyDetail(id);

    propertyDetail.value = res?.data || res;
  } catch (e) {
    console.error('매물 정보 로드 실패:', e);
  } finally {
    isLoading.value = false;
  }
};

onMounted(() => {
  fetchDetail(route.params.id);
});

watch(
  () => route.params.id,
  (newId) => {
    if (newId) fetchDetail(newId);
  },
);
</script>

<style scoped>
.property-layout {
  width: 100%;
  height: 100%;
}

.loading-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
  font-size: 14px;
  color: #888888;
}
</style>
