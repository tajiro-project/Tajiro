<template>
  <div class="pinfra">
    <!-- 1. 지도는 상단 고정 -->
    <div class="map-area">
      <KakaoMap
        :markers="markers"
        :dots="dots"
        :center="mapCenter"
        :active-dot-key="activeDotKey"
        :draggable="false"
        mode="infra"
        @marker-click="onMarkerClick"
        @dot-click="onDotClick"
        @dot-hover="onDotHover"
        @bounds-change="onBoundsChange"
      />
    </div>

    <!-- 2. 제목 및 탭 고정 -->
    <div class="title-area">
      <h1 class="main-title">{{ buildingName }} 기준 도보 거리예요</h1>
      <p class="sub-title">반경 2km 공공데이터 기준</p>
    </div>
    <div class="tab-wrap">
      <button
        class="tab-btn"
        :class="{ active: currentTab === 'infra' }"
        @click="currentTab = 'infra'"
      >
        인프라 ({{ infraRows.length }})
      </button>
      <button
        class="tab-btn"
        :class="{ active: currentTab === 'amenity' }"
        @click="currentTab = 'amenity'"
      >
        편의시설 ({{ amenityRows.length }})
      </button>
    </div>

    <!-- 3. 하단 카드 리스트 스크롤 영역 (Simplebar) -->
    <simplebar class="scroll-area">
      <section class="card">
        <template v-if="activeRows.length > 0">
          <div
            v-for="(item, i) in activeRows"
            :key="i"
            class="row"
            :class="{
              bordered: i > 0,
              active: activeDotKey === `${item.lat},${item.lng}`,
            }"
            @mouseenter="onRowHover(item)"
            @mouseleave="onRowHover(null)"
          >
            <!-- 좌측: 아이콘 + 카테고리 라벨 영역 -->
            <div class="r-icon-col">
              <span class="r-icon">
                <component
                  :is="item.icon"
                  :size="18"
                />
              </span>
              <span class="r-cat-label">{{ item.categoryLabel }}</span>
            </div>

            <!-- 우측: 이름 + 거리 정보 + 게이지 바 -->
            <div class="r-main">
              <div class="r-header">
                <p class="r-name">{{ item.name }}</p>
                <p class="r-dist">{{ item.dist }} · 도보 {{ item.walk }}분</p>
              </div>

              <!-- 게이지 바 + 500m 단위 눈금 영역 -->
              <!-- 게이지 바 + 500m 단위 눈금 영역 -->
              <div class="r-bar-wrapper">
                <div class="r-track">
                  <!-- 500m 단위 눈금선 (트랙 바깥으로 살짝 돌출) -->
                  <div class="r-ticks">
                    <span
                      class="r-tick"
                      style="left: 0%"
                    ></span>
                    <span
                      class="r-tick"
                      style="left: 25%"
                    ></span>
                    <span
                      class="r-tick"
                      style="left: 50%"
                    ></span>
                    <span
                      class="r-tick"
                      style="left: 75%"
                    ></span>
                    <span
                      class="r-tick"
                      style="left: 100%"
                    ></span>
                  </div>
                  <div
                    class="r-fill"
                    :style="{ width: item.pct + '%' }"
                  ></div>
                  <div
                    class="r-pin"
                    :style="{ left: item.pct + '%' }"
                  ></div>
                </div>

                <div class="r-scale">
                  <span>0m</span>
                  <span>500m</span>
                  <span>1km</span>
                  <span>1.5km</span>
                  <span>2km</span>
                </div>
              </div>
            </div>
          </div>
        </template>
        <div
          v-else
          class="empty-msg"
        >
          해당 정보가 없습니다.
        </div>
      </section>
    </simplebar>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import simplebar from 'simplebar-vue';
import KakaoMap from '@/components/KakaoMap.vue';
import { propertyApi } from '@/api/services';
import {
  INFRA_CATEGORIES,
  AMENITY_CATEGORIES,
} from '@/constants/preferenceOptions';
import { MapPin } from 'lucide-vue-next';

const route = useRoute();

// 💡 1. 쿼리 스트링으로 전달받은 buildingName 우선 사용
const buildingName = ref(route.query.buildingName || '');

// 상태 관리
const infras = ref([]);
const activeDotKey = ref(null);

// 탭 상태 ('infra' | 'amenity')
const currentTab = ref('infra');

// 지도 중심 좌표
const mapCenter = ref({ lat: 36.3320194, lng: 127.4570694 });

// 카테고리 구분용 Set 생성
const infraKeySet = new Set(INFRA_CATEGORIES.map((c) => c.key));
const amenityKeySet = new Set(AMENITY_CATEGORIES.map((c) => c.key));

// 전체 카테고리 Map 생성
const ALL_CATEGORIES = [...INFRA_CATEGORIES, ...AMENITY_CATEGORIES];
const categoryMap = computed(() => {
  const map = {};
  ALL_CATEGORIES.forEach((cat) => {
    map[cat.key] = cat;
  });
  return map;
});

// API 데이터 로드
onMounted(async () => {
  const propertyId = route.params.id;
  if (!propertyId) return;

  // 💡 2. buildingName 쿼리가 없는 경우(직접 URL 진입 등) 매물 상세 API로 예외 처리
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

  // 3. 인프라 데이터 로드
  try {
    const infraRes = await propertyApi.infrastructures(propertyId);
    const rawData = infraRes?.data || infraRes || {};

    if (rawData.propertyLatitude && rawData.propertyLongitude) {
      mapCenter.value = {
        lat: Number(rawData.propertyLatitude),
        lng: Number(rawData.propertyLongitude),
      };
    }

    if (Array.isArray(rawData.infrastructures)) {
      infras.value = rawData.infrastructures.map((infra) => ({
        category: infra.infraCategory,
        name: infra.infraName,
        distanceMeters: infra.distanceM ?? infra.distanceMeters,
        walkMinutes: infra.walkMinutes,
        lat: Number(infra.latitude),
        lng: Number(infra.longitude),
      }));
    }
  } catch (e) {
    console.error('인프라 데이터 로드 실패:', e);
  }
});

// KakaoMap 중심 마커
const markers = computed(() => {
  if (!mapCenter.value?.lat) return [];
  return [
    {
      lat: mapCenter.value.lat,
      lng: mapCenter.value.lng,
      selected: true,
      count: 1,
    },
  ];
});

// 공통 데이터 포맷팅 함수
function formatRowItem(item) {
  const catConfig = categoryMap.value[item.category];
  const categoryLabel = catConfig?.label || item.category;
  const categoryIcon = catConfig?.icon || MapPin;

  const MAX_DISTANCE = 2000; // 기준 최대 거리 (2km = 2000m)
  const distMeters = item.distanceMeters ?? 0;

  const rawPct = (distMeters / MAX_DISTANCE) * 100;
  const calculatedPct = Math.min(100, Math.max(0, Math.round(rawPct)));

  return {
    icon: categoryIcon,
    categoryLabel: categoryLabel,
    name: item.name,
    dist:
      distMeters >= 1000
        ? (distMeters / 1000).toFixed(1) + 'km'
        : distMeters + 'm',
    walk: item.walkMinutes,
    pct: calculatedPct,
    lat: item.lat,
    lng: item.lng,
    category: item.category,
  };
}

// INFRA_CATEGORIES
const infraRows = computed(() => {
  return infras.value
    .filter((i) => infraKeySet.has(i.category))
    .sort((a, b) => a.distanceMeters - b.distanceMeters)
    .map(formatRowItem);
});

// AMENITY_CATEGORIES
const amenityRows = computed(() => {
  return infras.value
    .filter((i) => amenityKeySet.has(i.category))
    .sort((a, b) => a.distanceMeters - b.distanceMeters)
    .map(formatRowItem);
});

// 현재 선택된 탭에 따른 리스트
const activeRows = computed(() => {
  return currentTab.value === 'infra' ? infraRows.value : amenityRows.value;
});

// KakaoMap 핀(dot) 목록
const dots = computed(() => {
  const validInfras = infras.value.filter(
    (i) => i.lat != null && i.lng != null,
  );

  const filteredInfras = validInfras.filter((i) => {
    if (currentTab.value === 'infra') {
      return infraKeySet.has(i.category);
    } else {
      return amenityKeySet.has(i.category);
    }
  });

  if (!filteredInfras.length) return [];

  const rawDots = filteredInfras.map((i) => ({
    lat: i.lat,
    lng: i.lng,
    category: i.category,
    name: i.name,
  }));

  const centerLat = mapCenter.value?.lat;
  const centerLng = mapCenter.value?.lng;
  if (centerLat == null || centerLng == null) return rawDots;

  let maxLatDiff = 0;
  let maxLngDiff = 0;

  filteredInfras.forEach((i) => {
    const latDiff = Math.abs(i.lat - centerLat);
    const lngDiff = Math.abs(i.lng - centerLng);
    if (latDiff > maxLatDiff) maxLatDiff = latDiff;
    if (lngDiff > maxLngDiff) maxLngDiff = lngDiff;
  });

  const dummyDots = [
    {
      lat: centerLat + maxLatDiff,
      lng: centerLng + maxLngDiff,
      category: '_dummy',
    },
    {
      lat: centerLat - maxLatDiff,
      lng: centerLng - maxLngDiff,
      category: '_dummy',
    },
  ];

  return [...rawDots, ...dummyDots];
});

// 핸들러
const onMarkerClick = (marker) => console.log('매물 마커 클릭:', marker);
const onDotClick = (dot) => {
  activeDotKey.value = `${dot.lat},${dot.lng}`;
};
const onDotHover = (dot) => {
  activeDotKey.value = dot ? `${dot.lat},${dot.lng}` : null;
};
const onRowHover = (item) => {
  activeDotKey.value =
    item && item.lat && item.lng ? `${item.lat},${item.lng}` : null;
};
const onBoundsChange = () => {};
</script>

<style scoped>
.pinfra {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--bg);
  overflow: hidden;
}

.scroll-area {
  padding: 0 16px 16px;
}

.map-area {
  height: 200px;
  width: 100%;
  flex-shrink: 0;
}
.title-area {
  margin-bottom: 2px;
  padding: 16px;
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
.tab-wrap {
  display: flex;
  margin: 0 16px 8px;
  background-color: #e9ecef;
  border-radius: 10px;
  padding: 3px;
  flex-shrink: 0;
}

.tab-btn {
  flex: 1;
  padding: 8px 0;
  border: none;
  background: transparent;
  font-size: 13px;
  font-weight: 700;
  color: #6c757d;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.tab-btn.active {
  background: var(--white);
  color: #212529;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
}

.card {
  background: var(--white);
  border: 1px solid var(--border);
  border-radius: 16px;
}

.empty-msg {
  padding: 20px 0;
  text-align: center;
  font-size: 13px;
  color: var(--kb-gray);
}

.row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 12px;
  border-radius: 12px;
  transition: all 0.15s ease;
  box-sizing: border-box;
}

.row.bordered {
  border-top: 1px solid var(--border);
}

.row.active {
  background-color: #fffdf5;
  box-shadow: inset 0 0 0 2px #ffb703;
}

/* 좌측 아이콘 + 카테고리 레이아웃 */
.r-icon-col {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  width: 52px;
  flex-shrink: 0;
}

.r-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 100px;
  background: var(--yellow-tint);
  color: #a8842c;
}

.r-cat-label {
  font-size: 10.5px;
  font-weight: 600;
  color: #666666;
  text-align: center;
  line-height: 1.1;
  word-break: keep-all;
}

.r-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.r-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.r-name {
  font-size: 13.5px;
  font-weight: 700;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: #222222;
}

.r-dist {
  font-size: 11.5px;
  color: var(--kb-gray);
  flex-shrink: 0;
}

/* 게이지 트랙 및 눈금 스타일 */
/* 게이지 트랙 및 눈금 고대비(High-Contrast) 스타일 */
.r-bar-wrapper {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 6px;
  padding: 0 2px;
}

.r-track {
  position: relative;
  width: 100%;
  height: 8px; /* 트랙 두께 확장 (6px -> 8px) */
  background-color: #e2e8f0; /* 더 또렷한 배경색 */
  border-radius: 4px;
  overflow: visible;
}

/* 선명한 눈금선 (트랙 위아래로 2px씩 돌출) */
.r-ticks {
  position: absolute;
  top: -2px;
  bottom: -2px;
  left: 0;
  right: 0;
  pointer-events: none;
}

.r-tick {
  position: absolute;
  top: 0;
  width: 2px;
  height: 100%;
  background-color: #cbd5e1; /* 진한 눈금선 */
  transform: translateX(-50%);
  z-index: 1;
}

/* 게이지 채움 바 (선명한 개나리색 / 필요 시 #f59e0b 로 변경 가능) */
.r-fill {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  background-color: var(--kb-yellow, #ffb703);
  border-radius: 4px;
  transition: width 0.3s ease;
  z-index: 2;
}

/* 핀 마커 (크기 확대 + 입체 그림자 추가) */
.r-pin {
  position: absolute;
  top: 50%;
  transform: translate(-50%, -50%);
  width: 10px;
  height: 10px;
  background-color: var(--kb-yellow, #ffb703);
  border: 2.5px solid #ffffff;
  border-radius: 50%;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.35); /* 그림자 강조 */
  transition: left 0.3s ease;
  z-index: 3;
}

/* 하단 거리 텍스트 가독성 보정 */
.r-scale {
  display: flex;
  justify-content: space-between;
  font-size: 11px; /* 글자 크기 확대 (9px -> 11px) */
  font-weight: 600;
  color: #475569; /* 진한 슬레이트 그레이로 변경 */
  line-height: 1;
}
</style>
