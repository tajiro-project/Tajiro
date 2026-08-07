<template>
  <div class="pinfra">
    <PageHeader title="주변 생활 인프라" />

    <!-- 1. 지도는 상단 고정 -->
    <div class="map-area">
      <KakaoMap
        :markers="markers"
        :dots="dots"
        :center="mapCenter"
        :active-dot-key="activeDotKey"
        :draggable="false"
        @marker-click="onMarkerClick"
        @dot-click="onDotClick"
        @dot-hover="onDotHover"
        @bounds-change="onBoundsChange"
      />
    </div>

    <!-- 2. 제목 및 탭 고정 -->
    <h1 class="title">{{ propertyName }} 기준 도보 거리예요</h1>

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
            <span class="r-icon">
              <component
                :is="item.icon"
                :size="18"
              />
            </span>

            <div class="r-main">
              <p class="r-name">{{ item.name }}</p>
              <div class="r-bar">
                <div
                  class="r-fill"
                  :style="{ width: item.pct + '%' }"
                />
              </div>
            </div>
            <p class="r-dist">{{ item.dist }} · 도보 {{ item.walk }}분</p>
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
import PageHeader from '@/components/PageHeader.vue';
import KakaoMap from '@/components/KakaoMap.vue';
import { propertyApi } from '@/api/services';
import {
  INFRA_CATEGORIES,
  AMENITY_CATEGORIES,
} from '@/constants/preferenceOptions';
import { MapPin } from 'lucide-vue-next';

const route = useRoute();

// 상태 관리
const infras = ref([]);
const propertyDetail = ref(null);
const propertyName = ref('매물');
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
  try {
    if (propertyApi.detail) {
      const res = await propertyApi.detail(propertyId);
      propertyDetail.value = res;
      if (res?.buildingName || res?.title || res?.name) {
        propertyName.value = res.buildingName || res.title || res.name;
      }
    }

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
    console.error('데이터 로드 실패:', e);
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

  const MAX_DISTANCE = 2000;
  const distMeters = item.distanceMeters ?? 0;

  const calculatedPct =
    distMeters >= MAX_DISTANCE
      ? 5
      : Math.round(((MAX_DISTANCE - distMeters) / MAX_DISTANCE) * 100);

  return {
    icon: categoryIcon,
    name: `${categoryLabel} (${item.name})`,
    dist:
      distMeters >= 1000
        ? (distMeters / 1000).toFixed(1) + 'km'
        : distMeters + 'm',
    walk: item.walkMinutes,
    pct: Math.max(5, Math.min(100, calculatedPct)),
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

.title {
  padding: 14px 16px 10px;
  font-size: 15px;
  font-weight: 900;
  flex-shrink: 0;
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
  padding: 4px 16px;
}

.empty-msg {
  padding: 20px 0;
  text-align: center;
  font-size: 13px;
  color: var(--kb-gray);
}

.row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 13px 0;
  transition: background-color 0.2s;
}

.row.bordered {
  border-top: 1px solid var(--bg);
}

.row.active {
  background-color: rgba(240, 168, 0, 0.08);
  border-radius: 8px;
}

.r-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 100px;
  background: var(--yellow-tint);
  flex-shrink: 0;
  color: #a8842c;
}

.r-main {
  flex: 1;
  min-width: 0;
}

.r-name {
  font-size: 13px;
  font-weight: 700;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.r-bar {
  margin-top: 5px;
  height: 4px;
  border-radius: 2px;
  background: var(--border);
  overflow: hidden;
}

.r-fill {
  height: 100%;
  border-radius: 2px;
  background: var(--kb-yellow);
}

.r-dist {
  font-size: 11.5px;
  color: var(--kb-gray);
  flex-shrink: 0;
}
</style>
