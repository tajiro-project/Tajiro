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
      <p
        v-if="route.query.demo === '1'"
        class="demo-banner"
      >
        예시 화면이에요 · 실제 인프라 데이터가 아니에요
      </p>
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
              <span
                class="r-icon"
                :style="{
                  backgroundColor: item.color + '1f',
                  color: item.color,
                  borderColor: item.color + '40',
                }"
              >
                <component
                  :is="item.icon"
                  :size="18"
                />
              </span>
              <span
                class="r-cat-label"
                :style="{ color: item.color }"
              >
                {{ item.categoryLabel }}
              </span>
            </div>

            <!-- 우측: 이름 + 거리 정보 + 게이지 바 -->
            <div class="r-main">
              <div class="r-header">
                <p class="r-name">{{ item.name }}</p>
                <!-- item.walkText에 '약 X분' 또는 '정보 없음' 등이 들어가도록 연동 -->
                <p class="r-dist">{{ item.dist }} · 도보 {{ item.walkText }}</p>
              </div>

              <!-- 게이지 바 + 500m 단위 눈금 영역 -->
              <div class="r-bar-wrapper">
                <div class="r-track">
                  <!-- 500m 단위 눈금선 -->
                  <div
                    class="r-tick"
                    style="left: 0%"
                  ></div>
                  <div
                    class="r-tick"
                    style="left: 25%"
                  ></div>
                  <div
                    class="r-tick"
                    style="left: 50%"
                  ></div>
                  <div
                    class="r-tick"
                    style="left: 75%"
                  ></div>
                  <div
                    class="r-tick"
                    style="left: 100%"
                  ></div>
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

const buildingName = ref(route.query.buildingName || '');
const propertyType = ref(route.query.propertyType || '');

const infras = ref([]);
const activeDotKey = ref(null);

const currentTab = ref('infra');

const mapCenter = ref({ lat: 36.3320194, lng: 127.4570694 });

const infraKeySet = new Set(INFRA_CATEGORIES.map((c) => c.key));
const amenityKeySet = new Set(AMENITY_CATEGORIES.map((c) => c.key));

const ALL_CATEGORIES = [...INFRA_CATEGORIES, ...AMENITY_CATEGORIES];
const categoryMap = computed(() => {
  const map = {};
  ALL_CATEGORIES.forEach((cat) => {
    map[cat.key] = cat;
  });
  return map;
});

// 도보 계산 함수
const formatCommuteTime = (data) => {
  // 값이 null이나 undefined인 경우 예외 처리
  if (data === null || data === undefined) return '정보 없음';

  // 평균 도보 분속
  const SPEED_PER_MINUTE = 85;

  // 굴곡도 1.25배 적용
  const actualDistance = data * 1.25;

  // 도보 분속 계산 (올림 처리하여 최소 1분 이상 표시)
  const minutes = Math.ceil(actualDistance / SPEED_PER_MINUTE);

  if (minutes === 0) return '1분 미만';

  return `${minutes}분`;
};

// 온보딩 다시보기(?demo=1) — 예시 데이터에도 distanceM 추가
const DEMO_CENTER = { lat: 36.3273128, lng: 127.4647872 };
const DEMO_INFRAS = [
  {
    category: 'SUBWAY',
    name: '용운역',
    distanceM: 420,
    lat: 36.3283,
    lng: 127.4652,
  },
  {
    category: 'HOSPITAL',
    name: '대전대학교병원',
    distanceM: 680,
    lat: 36.3265,
    lng: 127.4638,
  },
  {
    category: 'SCHOOL',
    name: '용운초등학교',
    distanceM: 350,
    lat: 36.3278,
    lng: 127.4655,
  },
  {
    category: 'CONVENIENCE',
    name: 'GS25 용운점',
    distanceM: 150,
    lat: 36.3274,
    lng: 127.4649,
  },
  {
    category: 'MART',
    name: '하나로마트 대전동구점',
    distanceM: 520,
    lat: 36.3268,
    lng: 127.4644,
  },
  {
    category: 'CAFE',
    name: '스타벅스 대전대점',
    distanceM: 280,
    lat: 36.3276,
    lng: 127.4651,
  },
];

onMounted(async () => {
  if (route.query.demo === '1') {
    buildingName.value = 'e편한세상대전에코포레';
    propertyType.value = '아파트';
    mapCenter.value = DEMO_CENTER;
    infras.value = DEMO_INFRAS;
    return;
  }

  const propertyId = route.params.id;
  if (!propertyId) return;

  if (!buildingName.value || !propertyType.value) {
    try {
      const pRes = await propertyApi.getPropertyDetail(propertyId);
      const pData = pRes?.data || pRes;
      const rawTitle = pData?.title || pData?.buildingName || pData?.name || '';

      if (!buildingName.value) {
        buildingName.value =
          rawTitle.replace(/\s*\d+호$/, '').trim() || '건물명 정보 없음';
      }
      if (!propertyType.value) {
        propertyType.value = pData?.propertyType || pData?.realEstateType || '';
      }
    } catch (err) {
      console.error('건물 정보 조회 실패:', err);
    }
  }

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
        // distanceM을 먼저 확인하고 없으면 distanceMeters, 그래도 없으면 null
        distanceM: infra.distanceM ?? infra.distanceMeters ?? null,
        lat: Number(infra.latitude),
        lng: Number(infra.longitude),
      }));
    }
  } catch (e) {
    console.error('인프라 데이터 로드 실패:', e);
  }
});

const markers = computed(() => {
  if (!mapCenter.value?.lat) return [];
  return [
    {
      lat: mapCenter.value.lat,
      lng: mapCenter.value.lng,
      selected: true,
      count: 1,
      propertyType: propertyType.value,
    },
  ];
});

function formatRowItem(item) {
  const catConfig = categoryMap.value[item.category];
  const categoryLabel = catConfig?.label || item.category;
  const categoryIcon = catConfig?.icon || MapPin;
  const categoryColor = catConfig?.color || '#475569';

  const MAX_DISTANCE = 2000;
  const distMeters = item.distanceM;

  // distanceM 값이 없는 경우 예외 처리
  const isInvalidDist = distMeters === null || distMeters === undefined;
  const rawPct = isInvalidDist ? 0 : (distMeters / MAX_DISTANCE) * 100;
  const calculatedPct = Math.min(100, Math.max(0, Math.round(rawPct)));

  // formatCommuteTime 활용하여 도보 시간 문자열 생성
  const walkText = formatCommuteTime(distMeters);

  return {
    icon: categoryIcon,
    categoryLabel: categoryLabel,
    color: categoryColor,
    name: item.name,
    dist: isInvalidDist
      ? '거리 정보 없음'
      : distMeters >= 1000
        ? (distMeters / 1000).toFixed(1) + 'km'
        : distMeters + 'm',
    walkText: walkText,
    pct: calculatedPct,
    lat: item.lat,
    lng: item.lng,
    category: item.category,
  };
}

const infraRows = computed(() => {
  return infras.value
    .filter((i) => infraKeySet.has(i.category))
    .sort((a, b) => (a.distanceM ?? 0) - (b.distanceM ?? 0))
    .map(formatRowItem);
});

const amenityRows = computed(() => {
  return infras.value
    .filter((i) => amenityKeySet.has(i.category))
    .sort((a, b) => (a.distanceM ?? 0) - (b.distanceM ?? 0))
    .map(formatRowItem);
});

const activeRows = computed(() => {
  return currentTab.value === 'infra' ? infraRows.value : amenityRows.value;
});

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
  border: 1px solid transparent;
  transition: all 0.2s ease;
}

.r-cat-label {
  font-size: 10.5px;
  font-weight: 700;
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
  height: 8px;
  background-color: #e2e8f0;
  border-radius: 4px;
  overflow: visible;
}

.r-tick {
  position: absolute;
  top: -2px;
  bottom: -2px;
  width: 2px;
  height: calc(100% + 4px);
  background-color: #cbd5e1;
  transform: translateX(-50%);
  z-index: 1;
}

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

.r-pin {
  position: absolute;
  top: 50%;
  transform: translate(-50%, -50%);
  width: 10px;
  height: 10px;
  background-color: var(--kb-yellow, #ffb703);
  border: 2.5px solid #ffffff;
  border-radius: 50%;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.35);
  transition: left 0.3s ease;
  z-index: 3;
}

.r-scale {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  font-weight: 600;
  color: #475569;
  line-height: 1;
}
</style>
