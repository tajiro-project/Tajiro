<template>
  <div class="pinfra">
    <PageHeader title="주변 생활 인프라" />

    <div class="scroll-area">
      <!-- 기존 SVG 더미 지도를 KakaoMap 컴포넌트로 교체 -->
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

      <h1 class="title">{{ propertyName }} 기준 도보 거리예요</h1>

      <!-- 인프라 리스트 -->
      <section class="card">
        <div
          v-for="(item, i) in rows"
          :key="i"
          class="row"
          :class="{
            bordered: i > 0,
            active: activeDotKey === `${item.lat},${item.lng}`,
          }"
          @mouseenter="onRowHover(item)"
          @mouseleave="onRowHover(null)"
        >
          <span
            class="r-icon"
            v-html="item.icon"
          />
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
      </section>
    </div>

    <AppTabBar active="home" />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import PageHeader from '@/components/PageHeader.vue';
import AppTabBar from '@/components/AppTabBar.vue';
import KakaoMap from '@/components/KakaoMap.vue'; // 생성해두신 KakaoMap 위치
import { propertyApi } from '@/api/services';

const route = useRoute();

// 상태 관리
const infras = ref([]);
const propertyDetail = ref(null);
const propertyName = ref('상남동 오피스텔');
const activeDotKey = ref(null);

// 지도 중심 좌표 (초기값: 서울역 또는 API 조회 후 매물 위치)
const mapCenter = ref({ lat: 37.5563, lng: 126.9723 });

// API 호출
onMounted(async () => {
  const propertyId = route.params.id;
  try {
    // 1. 매물 정보 불러오기 (매물 위도/경도 필요)
    if (propertyApi.detail) {
      const res = await propertyApi.detail(propertyId);
      propertyDetail.value = res;
      if (res?.lat && res?.lng) {
        mapCenter.value = { lat: res.lat, lng: res.lng };
      }
      if (res?.name) {
        propertyName.value = res.name;
      }
    }

    // 2. 주변 인프라 목록 불러오기
    infras.value = (await propertyApi.infrastructures(propertyId)) ?? [];
  } catch (e) {
    console.error('데이터 로드 실패:', e);
  }
});

// KakaoMap markers 계산 (현재 매물 위치 핀)
const markers = computed(() => {
  if (!propertyDetail.value?.lat) return [];
  return [
    {
      lat: propertyDetail.value.lat,
      lng: propertyDetail.value.lng,
      selected: true,
      count: 1,
    },
  ];
});

// KakaoMap dots 계산 (주변 인프라 핀들)
const dots = computed(() => {
  return infras.value
    .filter((i) => i.lat != null && i.lng != null)
    .map((i) => ({
      lat: i.lat,
      lng: i.lng,
      category: i.category,
      name: i.name,
    }));
});

// SVG 및 레이아웃 관련 상수
const ICONS = {
  HOSPITAL:
    '<svg width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M8 3v10M3 8h10" stroke="#a8842c" stroke-width="1.8" stroke-linecap="round"/></svg>',
  CONVENIENCE:
    '<svg width="16" height="16" viewBox="0 0 16 16" fill="none"><rect x="2.5" y="5" width="11" height="8.5" rx="1.5" stroke="#a8842c" stroke-width="1.3"/><path d="M5.5 5V3.5A1.5 1.5 0 017 2h2a1.5 1.5 0 011.5 1.5V5" stroke="#a8842c" stroke-width="1.3"/></svg>',
  MART: '<svg width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M3 5.5h10l-1 8H4l-1-8z" stroke="#a8842c" stroke-width="1.3" stroke-linejoin="round"/><path d="M6 5.5V4a2 2 0 014 0v1.5" stroke="#a8842c" stroke-width="1.3"/></svg>',
  BUS: '<svg width="16" height="16" viewBox="0 0 16 16" fill="none"><rect x="3" y="2.5" width="10" height="9.5" rx="2" stroke="#a8842c" stroke-width="1.3"/><path d="M3 8h10M5 14l.8-2M11 14l-.8-2" stroke="#a8842c" stroke-width="1.3" stroke-linecap="round"/><circle cx="5.8" cy="10" r=".8" fill="#a8842c"/><circle cx="10.2" cy="10" r=".8" fill="#a8842c"/></svg>',
  SUBWAY:
    '<svg width="16" height="16" viewBox="0 0 16 16" fill="none"><rect x="3.5" y="2.5" width="9" height="9" rx="2.5" stroke="#a8842c" stroke-width="1.3"/><path d="M3.5 8h9M5.5 14l1-2M10.5 14l-1-2" stroke="#a8842c" stroke-width="1.3" stroke-linecap="round"/></svg>',
  PARK: '<svg width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M8 2l3.5 5h-2L12 11H4l2.5-4h-2L8 2z" stroke="#a8842c" stroke-width="1.3" stroke-linejoin="round"/><path d="M8 11v3" stroke="#a8842c" stroke-width="1.3" stroke-linecap="round"/></svg>',
  GYM: '<svg width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M2 8h12M4 5.5v5M12 5.5v5M6 4v8M10 4v8" stroke="#a8842c" stroke-width="1.3" stroke-linecap="round"/></svg>',
  CAFE: '<svg width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M3 6h8v4a3 3 0 01-3 3H6a3 3 0 01-3-3V6z" stroke="#a8842c" stroke-width="1.3"/><path d="M11 7h1.5a1.5 1.5 0 010 3H11" stroke="#a8842c" stroke-width="1.3"/></svg>',
};

const NAME_FMT = {
  HOSPITAL: (n) => `병원 (${n})`,
  CONVENIENCE: (n) => `편의점 (${n})`,
  MART: (n) => `마트 (${n})`,
  BUS: () => '버스정류장',
  SUBWAY: () => '지하철 (예정 노선)',
  PARK: (n) => `공원·문화시설 (${n})`,
  GYM: (n) => `헬스장 (${n})`,
  CAFE: (n) => `카페 (${n})`,
};

const ORDER = ['HOSPITAL', 'CONVENIENCE', 'MART', 'BUS', 'SUBWAY', 'PARK'];

// 하단 리스트 계산
const rows = computed(() =>
  ORDER.map((cat) => {
    const found = infras.value.find((i) => i.category === cat);
    if (!found && cat === 'SUBWAY') {
      return {
        icon: ICONS[cat],
        name: NAME_FMT[cat](),
        dist: '1.2km',
        walk: 18,
        pct: 100,
        lat: null,
        lng: null,
      };
    }
    if (!found) return null;
    const shortName = found.name
      ? found.name.replace(/(역|정류장).*$/, '$1')
      : '';
    return {
      icon: ICONS[cat] ?? ICONS.PARK,
      name: NAME_FMT[cat] ? NAME_FMT[cat](shortName) : found.name,
      dist:
        found.distanceMeters >= 1000
          ? (found.distanceMeters / 1000).toFixed(1) + 'km'
          : found.distanceMeters + 'm',
      walk: found.walkMinutes,
      pct: Math.min(100, Math.round((found.distanceMeters / 1200) * 100)),
      lat: found.lat,
      lng: found.lng,
    };
  }).filter(Boolean),
);

// 카카오맵 이벤트 핸들러
const onMarkerClick = (marker) => {
  console.log('매물 마커 클릭됨:', marker);
};

const onDotClick = (dot) => {
  activeDotKey.value = `${dot.lat},${dot.lng}`;
};

const onDotHover = (dot) => {
  activeDotKey.value = dot ? `${dot.lat},${dot.lng}` : null;
};

const onRowHover = (item) => {
  if (item && item.lat && item.lng) {
    activeDotKey.value = `${item.lat},${item.lng}`;
  } else {
    activeDotKey.value = null;
  }
};

const onBoundsChange = (bounds) => {
  // 지도가 움직일 때 필요한 추가 처리 (필요시)
};
</script>

<style scoped>
.pinfra {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--bg);
}
.scroll-area {
  flex: 1;
  overflow-y: auto;
}
.map-area {
  height: 220px; /* 카카오 지도 영역 높이 조정 */
  width: 100%;
}
.title {
  padding: 16px 16px 0;
  font-size: 15px;
  font-weight: 900;
}
.card {
  margin: 12px 16px 20px;
  background: var(--white);
  border: 1px solid var(--border);
  border-radius: 16px;
  padding: 4px 16px;
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
/* 리스트 항목과 지도 인프라 dot이 연결(Hover)될 때의 스타일 */
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
