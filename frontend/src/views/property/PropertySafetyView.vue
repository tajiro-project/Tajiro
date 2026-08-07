<template>
  <div class="safety-page">
    <!-- 1. 상단 지도 영역 -->
    <div class="map-section">
      <div class="map-container">
        <KakaoMap
          :center="propertyCenter"
          :markers="mapMarkers"
          :dots="activeDots"
          :level="4"
          :fixed-center="true"
        />
      </div>
    </div>

    <!-- 2. 하단 스크롤 영역 -->
    <SimpleBar class="scroll-area">
      <div class="scroll-wrapper">
        <div class="scroll-content">
          <!-- 타이틀 -->
          <div class="title-area">
            <h1 class="main-title">매물 기준 안전 지표예요</h1>
            <p class="sub-title">반경 500m 공공데이터 기준 · 2026.07 갱신</p>
          </div>

          <!-- 세그먼트 탭 -->
          <div class="tab-bar">
            <button
              v-for="tab in tabs"
              :key="tab.key"
              class="tab-item"
              :class="{ active: currentTab === tab.key }"
              @click="changeTab(tab.key)"
            >
              {{ tab.label }} ({{ getTabItemCount(tab.key) }})
            </button>
          </div>

          <!-- 리스트 카드 영역 -->
          <div class="info-card">
            <div
              v-for="item in activeTabItems"
              :key="item.key"
              class="list-item"
              :class="{ selected: selectedItemKey === item.key }"
              @click="toggleItemSelection(item.key)"
            >
              <div class="item-left">
                <div class="icon-badge">
                  <component
                    :is="item.icon"
                    class="item-icon"
                  />
                </div>
                <span class="item-label">{{ item.label }}</span>
              </div>
              <span
                class="item-value"
                :class="{ 'text-warning': item.status === 'warning' }"
              >
                {{ item.value }}
              </span>
            </div>
          </div>
        </div>

        <!-- 하단 출처 -->
        <div class="data-source">
          <p class="source-title">데이터 출처</p>
          <p class="source-desc">
            도로교통공단 · 경찰청 · 여성가족부 · 지자체 CCTV 공공데이터. 정보는
            공표 시점 기준이며 실제와 다를 수 있어요.
          </p>
        </div>
      </div>
    </SimpleBar>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { SAFETY_CATEGORIES } from '@/constants/preferenceOptions';
import SimpleBar from 'simplebar-vue';
import 'simplebar-vue/dist/simplebar.min.css';
import KakaoMap from '@/components/KakaoMap.vue';

// 내 매물 중심 좌표
const propertyCenter = ref({ lat: 35.223, lng: 128.682 });
const mapMarkers = ref([
  { lat: 35.223, lng: 128.682, selected: true, count: 1 },
]);

// 실제 지도 위에 뿌려질 안전 시설물 데이터 (categoryKey 기반으로 지도 색상/아이콘 적용)
const apiMapDots = ref([
  { id: 1, lat: 35.2245, lng: 128.6805, categoryKey: 'POLICE' },
  { id: 2, lat: 35.2235, lng: 128.6812, categoryKey: 'CCTV' },
  { id: 3, lat: 35.222, lng: 128.683, categoryKey: 'CCTV' },
  { id: 4, lat: 35.2215, lng: 128.6825, categoryKey: 'EMERGENCY_BELL' },
  { id: 5, lat: 35.224, lng: 128.6835, categoryKey: 'STREET_LIGHT' },
  { id: 6, lat: 35.225, lng: 128.684, categoryKey: 'CHILD_PROTECTION' },
  { id: 7, lat: 35.221, lng: 128.685, categoryKey: 'CHILD_ACCIDENT' },
]);

const currentTab = ref('crime');
const selectedItemKey = ref(null);

const tabs = ref([
  {
    key: 'crime',
    label: '범죄안전',
    categoryKeys: [
      'POLICE',
      'CCTV',
      'EMERGENCY_BELL',
      'STREET_LIGHT',
      'CHILD_PROTECTION',
    ],
  },
  {
    key: 'traffic',
    label: '교통안전',
    categoryKeys: ['SCHOOL_ZONE', 'CHILD_ACCIDENT', 'PEDESTRIAN_ACCIDENT'],
  },
]);

const mockValues = {
  POLICE: { value: '상남지구대 350m', status: 'safe' },
  CCTV: { value: '반경 내 34대', status: 'safe' },
  EMERGENCY_BELL: { value: '반경 내 3곳', status: 'safe' },
  STREET_LIGHT: { value: '반경 내 15곳', status: 'safe' },
  CHILD_PROTECTION: { value: '반경 내 2곳', status: 'safe' },
  SCHOOL_ZONE: { value: '2곳 지정', status: 'safe' },
  CHILD_ACCIDENT: { value: '반경 내 3곳', status: 'warning' },
  PEDESTRIAN_ACCIDENT: { value: '반경 내 4곳', status: 'warning' },
};

// 현재 탭 항목 리스트
const activeTabItems = computed(() => {
  const currentCategoryKeys =
    tabs.value.find((t) => t.key === currentTab.value)?.categoryKeys || [];

  return currentCategoryKeys.map((key) => {
    const categoryInfo = SAFETY_CATEGORIES.find((c) => c.key === key);
    const apiData = mockValues[key] || { value: '-', status: 'safe' };
    return {
      ...categoryInfo,
      value: apiData.value,
      status: apiData.status,
    };
  });
});

const getTabItemCount = (tabKey) => {
  return tabs.value.find((t) => t.key === tabKey)?.categoryKeys.length || 0;
};

// 지도 위 핀(도트) 필터링 (항목 클릭 시 해당 카테고리 핀만 지도에 표시)
const activeDots = computed(() => {
  const currentCategoryKeys =
    tabs.value.find((t) => t.key === currentTab.value)?.categoryKeys || [];

  return apiMapDots.value.filter((dot) => {
    if (selectedItemKey.value) {
      return dot.categoryKey === selectedItemKey.value;
    }
    return currentCategoryKeys.includes(dot.categoryKey);
  });
});

const changeTab = (tabKey) => {
  currentTab.value = tabKey;
  selectedItemKey.value = null;
};

const toggleItemSelection = (itemKey) => {
  selectedItemKey.value = selectedItemKey.value === itemKey ? null : itemKey;
};
</script>

<style scoped>
.safety-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
  background-color: #f7f7f5;
  color: #2b2b2b;
}

.map-section {
  flex-shrink: 0;
  width: 100%;
  background-color: #f7f7f5;
}

.map-container {
  width: 100%;
  height: 190px;
  position: relative;
}

.map-legend {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 16px;
  padding: 10px 18px 4px 18px;
  font-size: 12px;
  color: #666666;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
}
.legend-item .dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}
.dot.my-property {
  background-color: #ffb703;
}
.dot.safe-facility {
  background-color: #10b981;
}
.dot.accident-zone {
  background-color: #dc2626;
}

.scroll-area {
  flex: 1;
  height: 100%;
}
:deep(.simplebar-content) {
  min-height: 100%;
  display: flex;
  flex-direction: column;
}
.scroll-wrapper {
  display: flex;
  flex-direction: column;
  flex: 1;
  padding: 16px 16px 24px 16px;
  box-sizing: border-box;
}
.scroll-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.title-area {
  margin-bottom: 2px;
}
.main-title {
  font-size: 18px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 4px;
}
.sub-title {
  font-size: 12px;
  color: #787878;
}

.tab-bar {
  display: flex;
  background-color: #e9ece9;
  border-radius: 12px;
  padding: 3px;
  gap: 3px;
}
.tab-item {
  flex: 1;
  padding: 10px 0;
  border: none;
  background: transparent;
  font-size: 14px;
  font-weight: 700;
  color: #666666;
  border-radius: 9px;
  cursor: pointer;
  transition: all 0.2s ease;
}
.tab-item.active {
  background-color: #ffffff;
  color: #1a1a1a;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.info-card {
  background-color: #ffffff;
  border-radius: 16px;
  border: 1px solid #eef0eb;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
}
.list-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid #f3f5f2;
  cursor: pointer;
  transition: all 0.15s ease;
  box-sizing: border-box;
}
.list-item:first-child {
  border-top-left-radius: 15px;
  border-top-right-radius: 15px;
}
.list-item:last-child {
  border-bottom: none;
  border-bottom-left-radius: 15px;
  border-bottom-right-radius: 15px;
}
.list-item.selected {
  background-color: #fffdf5;
  box-shadow: inset 0 0 0 2px #ffb703;
}

.item-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 💡 2번째 사진처럼 리스트 아이콘 뱃지 스타일 통일 (연노랑 배경 + 브라운/주황 아이콘) */
.icon-badge {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background-color: #fff8e1; /* 연한 크림/옐로우 톤 */
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.item-icon {
  width: 18px;
  height: 18px;
  color: #d97706; /* 따뜻한 주황/브라운 톤 통일 */
}

.item-label {
  font-size: 14px;
  font-weight: 600;
  color: #222222;
}
.item-value {
  font-size: 13px;
  font-weight: 600;
  color: var(--kb-gray);
}
.item-value.text-warning {
  color: #dc2626;
}

.data-source {
  margin-top: auto;
  padding-top: 24px;
}
.source-title {
  font-size: 12px;
  font-weight: 700;
  color: #555555;
  margin-bottom: 4px;
}
.source-desc {
  font-size: 11px;
  color: #777777;
  line-height: 1.5;
}
</style>
