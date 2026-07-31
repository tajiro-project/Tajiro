<template>
  <div class="property-list">
    <div class="map-area">
      <KakaoMap
        :markers="markers"
        :dots="dots"
        :center="center"
        :active-dot-key="activeDotKey"
        @marker-click="onMarkerClick"
        @dot-click="onDotClick"
        @dot-hover="onDotHover"
        @bounds-change="onBoundsChange"
      />

      <div v-if="activeDot" class="dot-info">
        <span class="dot-info-swatch" :style="{ background: activeDotColor }" />
        <span class="dot-info-text">{{ activeDotText }}</span>
        <button v-if="pinnedDot" class="dot-info-close" @click="closePanel">
          ×
        </button>
      </div>

      <button
        v-if="selectedBuildingId"
        class="reset-btn"
        @click="selectedBuildingId = null"
      >
        전체 보기
      </button>
    </div>

    <ul class="list">
      <li v-for="p in listItems" :key="p.propertyId">
        <button :class="{ on: p.selected }" @click="onCardClick(p)">
          {{ p.propertyId }}
        </button>
      </li>
    </ul>
  </div>
</template>

<script setup>
import KakaoMap from '@/components/KakaoMap.vue';
import { computed, ref, watch } from 'vue';
import { infraColor, INFRA_ICON_PATHS } from '@/constants/infraIcons';
import {
  safetyColor,
  SAFETY_ICON_PATHS,
  safetyLabel,
} from '@/constants/safetyIcons';

// mock data

// GET /api/properties 응답 형태 — 건물 정보가 매물마다 붙어서 온다
const RAW_PROPERTIES = [
  {
    propertyId: 'P01',
    buildingId: 'B01',
    buildingName: '용운마젤란21',
    lat: 36.3272,
    lng: 127.4541,
  },
  {
    propertyId: 'P02',
    buildingId: 'B01',
    buildingName: '용운마젤란21',
    lat: 36.3272,
    lng: 127.4541,
  },
  {
    propertyId: 'P03',
    buildingId: 'B01',
    buildingName: '용운마젤란21',
    lat: 36.3272,
    lng: 127.4541,
  },

  {
    propertyId: 'P04',
    buildingId: 'B02',
    buildingName: '한화꿈에그린',
    lat: 36.3305,
    lng: 127.4589,
  },
  {
    propertyId: 'P05',
    buildingId: 'B02',
    buildingName: '한화꿈에그린',
    lat: 36.3305,
    lng: 127.4589,
  },

  {
    propertyId: 'P06',
    buildingId: 'B03',
    buildingName: '용방마을아파트',
    lat: 36.3251,
    lng: 127.4608,
  },

  {
    propertyId: 'P07',
    buildingId: 'B04',
    buildingName: '에코포레',
    lat: 36.333,
    lng: 127.453,
  },
  {
    propertyId: 'P08',
    buildingId: 'B04',
    buildingName: '에코포레',
    lat: 36.333,
    lng: 127.453,
  },
  {
    propertyId: 'P09',
    buildingId: 'B04',
    buildingName: '에코포레',
    lat: 36.333,
    lng: 127.453,
  },
  {
    propertyId: 'P10',
    buildingId: 'B04',
    buildingName: '에코포레',
    lat: 36.333,
    lng: 127.453,
  },

  {
    propertyId: 'P11',
    buildingId: 'B05',
    buildingName: '대학로62-67',
    lat: 36.324,
    lng: 127.452,
  },

  {
    propertyId: 'P12',
    buildingId: 'B06',
    buildingName: '새울로109번길',
    lat: 36.332,
    lng: 127.464,
  },
  {
    propertyId: 'P13',
    buildingId: 'B06',
    buildingName: '새울로109번길',
    lat: 36.332,
    lng: 127.464,
  },
];

const RAW_INFRA = [
  // B01 용운마젤란21
  {
    buildingId: 'B01',
    category: 'SUBWAY',
    name: '판암역',
    lat: 36.3285,
    lng: 127.4541,
  },
  {
    buildingId: 'B01',
    category: 'HOSPITAL',
    name: '대전한국병원',
    lat: 36.3281,
    lng: 127.4553,
  },
  {
    buildingId: 'B01',
    category: 'CAFE',
    name: '스타벅스 대전대점',
    lat: 36.3263,
    lng: 127.4553,
  },
  {
    buildingId: 'B01',
    category: 'SCHOOL',
    name: '가양초등학교',
    lat: 36.3259,
    lng: 127.4541,
  },
  {
    buildingId: 'B01',
    category: 'PARK',
    name: '용운근린공원',
    lat: 36.3263,
    lng: 127.4529,
  },

  // B02 한화꿈에그린
  {
    buildingId: 'B02',
    category: 'BUS_TERMINAL',
    name: '대전복합터미널',
    lat: 36.3318,
    lng: 127.4589,
  },
  {
    buildingId: 'B02',
    category: 'PHARMACY',
    name: '온누리약국',
    lat: 36.3314,
    lng: 127.4601,
  },
  {
    buildingId: 'B02',
    category: 'MART',
    name: '홈플러스 가오점',
    lat: 36.3296,
    lng: 127.4601,
  },
  {
    buildingId: 'B02',
    category: 'ACADEMY',
    name: '이룸수학학원',
    lat: 36.3292,
    lng: 127.4589,
  },
  {
    buildingId: 'B02',
    category: 'CULTURE',
    name: '동구문화체육센터',
    lat: 36.3296,
    lng: 127.4577,
  },

  // B03 용방마을아파트
  {
    buildingId: 'B03',
    category: 'TRAIN',
    name: '대전역',
    lat: 36.3264,
    lng: 127.4608,
  },
  {
    buildingId: 'B03',
    category: 'CONVENIENCE',
    name: 'GS25 용운점',
    lat: 36.326,
    lng: 127.462,
  },
  {
    buildingId: 'B03',
    category: 'KINDERGARTEN',
    name: '햇살유치원',
    lat: 36.3242,
    lng: 127.462,
  },
  {
    buildingId: 'B03',
    category: 'SPORTS',
    name: '동구실내체육관',
    lat: 36.3238,
    lng: 127.4608,
  },

  // B04 에코포레
  {
    buildingId: 'B04',
    category: 'PARKING',
    name: '용운공영주차장',
    lat: 36.3343,
    lng: 127.453,
  },
  {
    buildingId: 'B04',
    category: 'FOOD',
    name: '용운칼국수',
    lat: 36.3339,
    lng: 127.4542,
  },
  {
    buildingId: 'B04',
    category: 'LIBRARY',
    name: '한밭도서관',
    lat: 36.3321,
    lng: 127.4542,
  },
  {
    buildingId: 'B04',
    category: 'SWIMMING',
    name: '용운국제수영장',
    lat: 36.3317,
    lng: 127.453,
  },

  // B05 대학로62-67
  {
    buildingId: 'B05',
    category: 'GAS',
    name: 'GS칼텍스 용운주유소',
    lat: 36.3253,
    lng: 127.452,
  },
  {
    buildingId: 'B05',
    category: 'BANK',
    name: '하나은행 대전대점',
    lat: 36.3249,
    lng: 127.4532,
  },
  {
    buildingId: 'B05',
    category: 'GOV_OFFICE',
    name: '용운동 행정복지센터',
    lat: 36.3231,
    lng: 127.4532,
  },
  {
    buildingId: 'B05',
    category: 'POST_OFFICE',
    name: '대전용운우체국',
    lat: 36.3227,
    lng: 127.452,
  },

  // B06 새울로109번길
  {
    buildingId: 'B06',
    category: 'PUBLIC',
    name: '대전동구청',
    lat: 36.3333,
    lng: 127.464,
  },
  {
    buildingId: 'B06',
    category: 'POLICE',
    name: '대전동부경찰서',
    lat: 36.3329,
    lng: 127.4652,
  },
  {
    buildingId: 'B06',
    category: 'FIRE',
    name: '동부소방서',
    lat: 36.3311,
    lng: 127.4652,
  },
  {
    buildingId: 'B06',
    category: 'FIRE',
    name: '동부119안전센터',
    lat: 36.335,
    lng: 127.468,
  },
];

const RAW_SAFETY = [
  { buildingId: 'B01', type: 'CCTV', lat: 36.3281, lng: 127.4529 },
  { buildingId: 'B03', type: 'POLICE_CENTER', lat: 36.3242, lng: 127.4596 },
  { buildingId: 'B04', type: 'SAFETY_BELL', lat: 36.3321, lng: 127.4518 },
  { buildingId: 'B05', type: 'SECURITY_LIGHT', lat: 36.3231, lng: 127.4508 },
  { buildingId: 'B06', type: 'CHILD_SAFE_ZONE', lat: 36.3307, lng: 127.464 },
  { buildingId: 'B06', type: 'CHILD_GUARD_HOUSE', lat: 36.3311, lng: 127.4628 },
];

const center = { lat: 36.3366, lng: 127.459 };

const selectedBuildingId = ref(null);
const selectedPropertyId = ref(null);
const bounds = ref(null);
const pinnedDot = ref(null);
const hoveredDot = ref(null);

const activeDot = computed(() => hoveredDot.value ?? pinnedDot.value);

function keyOf(dot) {
  return dot ? `${dot.lat},${dot.lng}` : null;
}
const activeDotKey = computed(() => keyOf(activeDot.value));
const activeDotText = computed(() => activeDot.value?.name ?? '');

const activeDotColor = computed(() => {
  const c = activeDot.value?.category;
  if (!c) return '#8a8d8f';
  if (INFRA_ICON_PATHS[c]) return infraColor(c);
  if (SAFETY_ICON_PATHS[c]) return safetyColor();
  return '#8a8d8f';
});

const listItems = computed(() =>
  RAW_PROPERTIES.map((p) => ({
    ...p,
    selected: selectedPropertyId.value
      ? p.propertyId === selectedPropertyId.value
      : p.buildingId === selectedBuildingId.value,
  })),
);

const markers = computed(() => {
  const grouped = new Map();

  for (const p of RAW_PROPERTIES) {
    if (p.lat == null || p.lng == null) continue;

    if (!grouped.has(p.buildingId)) {
      grouped.set(p.buildingId, {
        id: p.buildingId,
        lat: p.lat,
        lng: p.lng,
        name: p.buildingName,
        count: 0,
      });
    }
    grouped.get(p.buildingId).count += 1;
  }

  return [...grouped.values()]
    .map((m) => ({ ...m, selected: m.id === selectedBuildingId.value }))
    .filter((m) => !selectedBuildingId.value || m.selected);
});

const dots = computed(() => {
  if (!selectedBuildingId.value) return [];

  // 인프라/편의시설 관련
  const infra = RAW_INFRA.filter(
    (i) => i.buildingId === selectedBuildingId.value,
  ).map((i) => ({
    lat: i.lat,
    lng: i.lng,
    category: i.category,
    name: i.name,
  }));

  // 안전 지표 관련
  const safety = RAW_SAFETY.filter(
    (s) => s.buildingId === selectedBuildingId.value,
  ).map((s) => ({
    lat: s.lat,
    lng: s.lng,
    category: s.type,
    name: safetyLabel(s.type),
  }));

  return [...infra, ...safety];
});

function onMarkerClick(marker) {
  // console.log('marker-click: ' + marker);
  if (selectedBuildingId.value === marker.id) {
    selectedBuildingId.value = null;
    selectedPropertyId.value = null;
    return;
  }
  selectedBuildingId.value = marker.id;
  selectedPropertyId.value = null;
}

function onCardClick(property) {
  if (selectedPropertyId.value === property.propertyId) {
    selectedPropertyId.value = null;
    selectedBuildingId.value = null;
    return;
  }
  selectedPropertyId.value = property.propertyId;
  selectedBuildingId.value = property.buildingId;
}

function onBoundsChange(b) {
  bounds.value = b;
}

function onDotClick(dot) {
  // activeDot.value = dot;
  pinnedDot.value = keyOf(pinnedDot.value) === keyOf(dot) ? null : dot;
}

function onDotHover(dot) {
  hoveredDot.value = dot;
}

function closePanel() {
  pinnedDot.value = null;
  hoveredDot.value = null;
}

watch(selectedBuildingId, closePanel);
</script>

<style scoped>
.property-list {
  flex: 1;
}

.map-area {
  position: relative;
  height: 250px;
}

.list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 0 16px;
}

.list button {
  padding: 8px 14px;
  border: 1px solid #e9e7e2;
  border-radius: 8px;
  background-color: #ffffff;
}

.list button.on {
  background-color: #fff6dc;
  border-color: #ffbc00;
  font-weight: 700;
}

.reset-btn {
  position: absolute;
  bottom: 12px;
  right: 12px;
  z-index: 100;
  padding: 7px 12px;
  border: 1px solid #e9e7e2;
  border-radius: 100px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(51, 48, 42, 0.18);
  font-size: 12px;
  font-weight: 700;
  color: #33302a;
  cursor: pointer;
}

.dot-info {
  position: absolute;
  top: 12px;
  left: 12px;
  z-index: 100;
  display: flex;
  align-items: center;
  gap: 7px;
  max-width: calc(100% - 24px);
  padding: 7px 8px 7px 10px;
  background: #fff;
  border: 1px solid #e9e7e2;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(51, 48, 42, 0.18);
}

.dot-info-swatch {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.dot-info-text {
  font-size: 12px;
  font-weight: 700;
  color: #33302a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dot-info-close {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  border: none;
  background: none;
  color: #8a8d8f;
  font-size: 15px;
  line-height: 1;
  cursor: pointer;
  flex-shrink: 0;
}
</style>
