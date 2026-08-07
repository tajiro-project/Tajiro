<template>
  <div class="safety-page">
    <div class="map-section">
      <div class="map-container">
        <KakaoMap
          :center="propertyCenter"
          :markers="mapMarkers"
          :dots="activeDots"
          :polygons="activePolygons"
          :fixed-center="true"
        />
      </div>
    </div>

    <SimpleBar class="scroll-area">
      <div class="scroll-wrapper">
        <div class="scroll-content">
          <div class="title-area">
            <h1 class="main-title">매물 기준 안전 지표예요</h1>
            <p
              v-if="isLoading"
              class="sub-title"
            >
              데이터를 불러오는 중입니다...
            </p>
            <p
              v-else
              class="sub-title"
            >
              반경 500m 공공데이터 기준
            </p>
          </div>

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

          <div class="info-card">
            <template v-if="activeTabItems.length > 0">
              <div
                v-for="item in activeTabItems"
                :key="item.key"
                class="list-item"
                :class="{
                  selected: selectedItemKey === item.key,
                  disabled: item.count === 0,
                }"
                @click="toggleItemSelection(item)"
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
                  :class="{ 'text-muted': item.count === 0 }"
                >
                  {{ item.value }}
                </span>
              </div>
            </template>
            <div
              v-else-if="!isLoading"
              class="empty-msg"
            >
              안전 정보 데이터가 없습니다.
            </div>
          </div>
        </div>

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
import { ref, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import SimpleBar from 'simplebar-vue';
import KakaoMap from '@/components/KakaoMap.vue';
import { propertyApi } from '@/api/services';
import { SAFETY_CATEGORIES } from '@/constants/preferenceOptions';

const route = useRoute();

const CATEGORY_KEY_MAP = {
  POLICE_CENTER: 'POLICE',
  CCTV: 'CCTV',
  SAFETY_BELL: 'EMERGENCY_BELL',
  SECURITY_LIGHT: 'STREET_LIGHT',
  CHILD_SAFE_ZONE: 'SCHOOL_ZONE',
  CHILD_GUARD_HOUSE: 'CHILD_PROTECTION',
  CHILD_ACCIDENT_ZONE: 'CHILD_ACCIDENT',
  PEDESTRIAN_ACCIDENT_ZONE: 'PEDESTRIAN_ACCIDENT',
};

const isLoading = ref(true);
const currentTab = ref('crime');
const selectedItemKey = ref(null);
const rawSafetyData = ref(null);

const propertyCenter = ref({ lat: 36.3273128, lng: 127.4647872 });

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

onMounted(async () => {
  const propertyId = route.params.id;
  try {
    isLoading.value = true;
    const res = await propertyApi.safety(propertyId);
    const actualData = res?.data || res;

    if (actualData) {
      rawSafetyData.value = actualData;

      const lat = Number(actualData.latitude);
      const lng = Number(actualData.longitude);
      if (!isNaN(lat) && !isNaN(lng) && lat !== 0 && lng !== 0) {
        propertyCenter.value = { lat, lng };
      }
    }
  } catch (e) {
    console.error('안전 정보 불러오기 실패:', e);
  } finally {
    isLoading.value = false;
  }
});

const mapMarkers = computed(() => {
  if (!propertyCenter.value?.lat) return [];
  return [
    {
      lat: propertyCenter.value.lat,
      lng: propertyCenter.value.lng,
      selected: true,
      count: 1,
    },
  ];
});

const getTabItemCount = (tabKey) => {
  if (!rawSafetyData.value) return 0;
  return tabKey === 'crime'
    ? rawSafetyData.value.crimeSafetyCount || 0
    : rawSafetyData.value.trafficSafetyCount || 0;
};

const activeTabItems = computed(() => {
  if (!rawSafetyData.value?.safetyList) return [];

  const currentCategoryKeys =
    tabs.value.find((t) => t.key === currentTab.value)?.categoryKeys || [];

  return currentCategoryKeys.map((key) => {
    const categoryMeta = SAFETY_CATEGORIES.find((c) => c.key === key) || {};
    const apiItem = rawSafetyData.value.safetyList.find((s) => {
      const mappedKey = CATEGORY_KEY_MAP[s.safeCategory] || s.safeCategory;
      return mappedKey === key;
    });

    const count = apiItem?.countWithin500m || 0;
    const nearestDist = apiItem?.nearestDistanceMeters;
    const nearestName = apiItem?.nearestSafeName;

    const unit = key === 'CCTV' ? '대' : '곳';
    let displayValue = `반경 내 ${count}${unit}`;

    if (key === 'POLICE' && nearestName && nearestDist && count > 0) {
      displayValue = `${nearestName} ${nearestDist}m`;
    }

    return {
      key,
      label: categoryMeta.label || key,
      icon: categoryMeta.icon,
      color: categoryMeta.color,
      value: displayValue,
      count,
      apiCategoryKey: apiItem?.safeCategory,
      details: apiItem?.details || [],
    };
  });
});

const activeDots = computed(() => {
  if (!rawSafetyData.value?.safetyList) return [];

  const extractDots = (categoryKeys) => {
    const rawDots = [];

    rawSafetyData.value.safetyList.forEach((s) => {
      const mappedKey = CATEGORY_KEY_MAP[s.safeCategory] || s.safeCategory;
      if (categoryKeys.includes(mappedKey) && s.details) {
        s.details.forEach((detail) => {
          const lat = Number(detail.latitude);
          const lng = Number(detail.longitude);
          if (!isNaN(lat) && !isNaN(lng)) {
            rawDots.push({
              id: detail.safeDetailId,
              lat,
              lng,
              categoryKey: mappedKey,
              name: detail.safeName,
            });
          }
        });
      }
    });

    const coordCountMap = new Map();

    return rawDots.map((dot) => {
      const coordKey = `${dot.lat.toFixed(6)},${dot.lng.toFixed(6)}`;
      const count = coordCountMap.get(coordKey) || 0;
      coordCountMap.set(coordKey, count + 1);

      if (count > 0) {
        const offset = count * 0.00015;
        return {
          ...dot,
          lat: dot.lat + offset,
          lng: dot.lng + offset,
        };
      }

      return dot;
    });
  };

  if (selectedItemKey.value) {
    return extractDots([selectedItemKey.value]);
  }

  const currentCategoryKeys =
    tabs.value.find((t) => t.key === currentTab.value)?.categoryKeys || [];

  return extractDots(currentCategoryKeys);
});

// 안전 구역 다각형(Polygon) 파싱 및 옵션 생성
const activePolygons = computed(() => {
  if (!rawSafetyData.value?.safetyList) return [];

  const polygons = [];
  const currentCategoryKeys = selectedItemKey.value
    ? [selectedItemKey.value]
    : tabs.value.find((t) => t.key === currentTab.value)?.categoryKeys || [];

  rawSafetyData.value.safetyList.forEach((s) => {
    const mappedKey = CATEGORY_KEY_MAP[s.safeCategory] || s.safeCategory;

    if (currentCategoryKeys.includes(mappedKey) && s.details) {
      // 해당 카테고리의 고유 색상 추출 (없으면 기본값 사용)
      const categoryMeta =
        SAFETY_CATEGORIES.find((c) => c.key === mappedKey) || {};
      const categoryColor = categoryMeta.color || '#10B981';

      s.details.forEach((detail) => {
        if (detail.polygon) {
          try {
            let parsed = detail.polygon;

            // 문자열인 경우 객체가 될 때까지 완전 파싱 (이중 이스케이프 처리)
            while (typeof parsed === 'string') {
              parsed = JSON.parse(parsed);
            }

            if (parsed?.type === 'Feature') {
              parsed = parsed.geometry;
            }

            if (
              parsed?.type === 'Polygon' &&
              Array.isArray(parsed?.coordinates?.[0])
            ) {
              polygons.push({
                id: detail.safeDetailId,
                path: parsed.coordinates[0], // [[127.x, 36.x], ...]
                strokeColor: categoryColor,
                strokeWeight: 2.5,
                strokeOpacity: 0.8,
                fillColor: categoryColor,
                fillOpacity: 0.2, // 연한 구역 색상
              });
            }
          } catch (e) {
            console.warn('Polygon 파싱 오류 스킵:', e);
          }
        }
      });
    }
  });

  return polygons;
});

const changeTab = (tabKey) => {
  currentTab.value = tabKey;
  selectedItemKey.value = null;
};

const toggleItemSelection = (item) => {
  if (item.count === 0) return;
  selectedItemKey.value = selectedItemKey.value === item.key ? null : item.key;
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
  height: 220px;
  position: relative;
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

.list-item.disabled {
  opacity: 0.55;
  cursor: default;
}

.item-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.icon-badge {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background-color: #fff8e1;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.item-icon {
  width: 18px;
  height: 18px;
  color: #d97706;
}

.item-label {
  font-size: 14px;
  font-weight: 600;
  color: #222222;
}
.item-value {
  font-size: 13px;
  font-weight: 600;
  color: #222222;
}
.item-value.text-muted {
  color: #888888;
  font-weight: 500;
}

.empty-msg {
  padding: 24px;
  text-align: center;
  font-size: 13px;
  color: #888888;
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
