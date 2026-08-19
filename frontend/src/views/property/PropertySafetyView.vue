<template>
  <div class="safety-page">
    <div class="map-section">
      <div class="map-container">
        <!-- fixed-center를 제거하여 모든 좌표가 지도 안에 들어오도록 자동 조절 -->
        <KakaoMap
          :center="propertyCenter"
          :markers="mapMarkers"
          :dots="activeDots"
          :polygons="activePolygons"
          mode="safety"
        />
      </div>
    </div>

    <SimpleBar class="scroll-area">
      <div class="scroll-wrapper">
        <div class="scroll-content">
          <div class="title-area">
            <p v-if="route.query.demo === '1'" class="demo-banner">
              예시 화면이에요 · 실제 안전 데이터가 아니에요
            </p>
            <h1 class="main-title">{{ buildingName }} 기준 안전 지표예요</h1>
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
              반경 500m 공공데이터 기준 · {{ formatDate(updateDate) }}
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
                  <!-- 💡 동적 색상을 반영한 아이콘 뱃지 -->
                  <div
                    class="icon-badge"
                    :style="{
                      backgroundColor: item.color
                        ? item.color + '1f'
                        : '#fff8e1',
                      borderColor: item.color
                        ? item.color + '40'
                        : 'transparent',
                    }"
                  >
                    <component
                      :is="item.icon"
                      class="item-icon"
                      :style="{ color: item.color || '#d97706' }"
                    />
                  </div>
                  <!-- 💡 동적 색상을 반영한 라벨 -->
                  <span
                    class="item-label"
                    :style="{ color: item.color || '#222222' }"
                  >
                    {{ item.label }}
                  </span>
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

// 💡 쿼리 스트링으로 전달받은 buildingName 우선 사용
const buildingName = ref(route.query.buildingName || '');

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
const updateDate = ref(null);

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

// 온보딩 다시보기(?demo=1) — 실제 API를 안 타고 바로 예시 데이터로 채운다.
const DEMO_CENTER = { lat: 36.3273128, lng: 127.4647872 };
const DEMO_SAFETY_DATA = {
  crimeSafetyCount: 4,
  trafficSafetyCount: 1,
  updatedAt: '2026-06-01T00:00:00',
  latitude: DEMO_CENTER.lat,
  longitude: DEMO_CENTER.lng,
  safetyList: [
    {
      safeCategory: 'POLICE_CENTER',
      countWithin500m: 1,
      details: [
        { safeDetailId: 'demo-police-1', latitude: 36.3281, longitude: 127.4655, safeName: '용운지구대' },
      ],
    },
    {
      safeCategory: 'CCTV',
      countWithin500m: 6,
      details: [
        { safeDetailId: 'demo-cctv-1', latitude: 36.3276, longitude: 127.4650, safeName: '방범 CCTV' },
        { safeDetailId: 'demo-cctv-2', latitude: 36.3270, longitude: 127.4645, safeName: '방범 CCTV' },
      ],
    },
    {
      safeCategory: 'SAFETY_BELL',
      countWithin500m: 2,
      details: [
        { safeDetailId: 'demo-bell-1', latitude: 36.3274, longitude: 127.4652, safeName: '안전 비상벨' },
      ],
    },
    {
      safeCategory: 'SECURITY_LIGHT',
      countWithin500m: 9,
      details: [
        { safeDetailId: 'demo-light-1', latitude: 36.3272, longitude: 127.4648, safeName: '보안등' },
      ],
    },
    {
      safeCategory: 'CHILD_ACCIDENT_ZONE',
      countWithin500m: 1,
      details: [
        { safeDetailId: 'demo-accident-1', latitude: 36.3279, longitude: 127.4642, safeName: '어린이 보호구역' },
      ],
    },
  ],
};

onMounted(async () => {
  if (route.query.demo === '1') {
    buildingName.value = 'e편한세상대전에코포레';
    propertyCenter.value = DEMO_CENTER;
    rawSafetyData.value = DEMO_SAFETY_DATA;
    updateDate.value = DEMO_SAFETY_DATA.updatedAt;
    isLoading.value = false;
    return;
  }

  const propertyId = route.params.id;
  if (!propertyId) return;

  if (!buildingName.value) {
    try {
      const pRes = await propertyApi.getPropertyDetail(propertyId);
      const pData = pRes?.data || pRes;
      const rawTitle = pData?.title || pData?.buildingName || pData?.name || '';
      buildingName.value =
        rawTitle.replace(/\s*\d+호$/, '').trim() || '건물명 정보 없음';
    } catch (err) {
      console.error('건물명 조회 실패:', err);
      buildingName.value = '건물명 정보 없음';
    }
  }

  try {
    isLoading.value = true;
    const res = await propertyApi.safety(propertyId);
    const actualData = res?.data || res;

    if (actualData) {
      rawSafetyData.value = actualData;

      updateDate.value = actualData.updatedAt;

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

const formatDate = (dateStr) => {
  if (!dateStr) return '';

  return dateStr.slice(0, 10).replaceAll('-', '.');
};

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
    const unit = key === 'CCTV' ? '대' : '곳';
    const displayValue = `반경 내 ${count}${unit}`;

    return {
      key,
      label: categoryMeta.label || key,
      icon: categoryMeta.icon,
      color: categoryMeta.color, // 💡 SAFETY_CATEGORIES에서 정의된 색상 주입
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

const activePolygons = computed(() => {
  if (!rawSafetyData.value?.safetyList) return [];

  const polygons = [];
  const currentCategoryKeys = selectedItemKey.value
    ? [selectedItemKey.value]
    : tabs.value.find((t) => t.key === currentTab.value)?.categoryKeys || [];

  rawSafetyData.value.safetyList.forEach((s) => {
    const mappedKey = CATEGORY_KEY_MAP[s.safeCategory] || s.safeCategory;

    if (currentCategoryKeys.includes(mappedKey) && s.details) {
      const categoryMeta =
        SAFETY_CATEGORIES.find((c) => c.key === mappedKey) || {};
      const categoryColor = categoryMeta.color || '#10B981';

      s.details.forEach((detail) => {
        if (detail.polygon) {
          try {
            let parsed = detail.polygon;

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
                path: parsed.coordinates[0],
                strokeColor: categoryColor,
                strokeWeight: 2.5,
                strokeOpacity: 0.8,
                fillColor: categoryColor,
                fillOpacity: 0.2,
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
.demo-banner {
  margin: 0 0 10px;
  padding: 9px 12px;
  background: #f1efea;
  border: 1px dashed var(--kb-silver);
  border-radius: 10px;
  font-size: 11.5px;
  font-weight: 700;
  color: var(--kb-gray);
  text-align: center;
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

/* 💡 동적 입체감을 주는 테두리와 부드러운 전환 효과 추가 */
.icon-badge {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  border: 1px solid transparent;
  transition: all 0.2s ease;
}
.item-icon {
  width: 18px;
  height: 18px;
  transition: color 0.2s ease;
}

.item-label {
  font-size: 14px;
  font-weight: 700;
  transition: color 0.2s ease;
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
