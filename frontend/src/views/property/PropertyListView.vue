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

      <button v-if="selectedId" class="reset-btn" @click="selectedId = null">
        전체 매물 보기
      </button>
    </div>

    <ul class="list">
      <li v-for="m in listItems" :key="m.id">
        <button :class="{ on: m.selected }" @click="onMarkerClick(m)">
          {{ m.id }}
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
const RAW_MARKERS = [
  { id: 'P01', lat: 36.3272, lng: 127.4541 },
  { id: 'P02', lat: 36.3305, lng: 127.4589 },
  { id: 'P03', lat: 36.3251, lng: 127.4608 },
  { id: 'P04', lat: 36.333, lng: 127.453 },
  { id: 'P05', lat: 36.324, lng: 127.452 },
  { id: 'P06', lat: 36.332, lng: 127.464 },
];

// GET /api/infrastructures/{propertyId}/map  응답 형태
const RAW_INFRA = [
  // P01
  {
    propertyId: 'P01',
    category: 'SUBWAY',
    name: '판암역',
    lat: 36.3285,
    lng: 127.4541,
  },
  {
    propertyId: 'P01',
    category: 'HOSPITAL',
    name: '대전한국병원',
    lat: 36.3281,
    lng: 127.4553,
  },
  {
    propertyId: 'P01',
    category: 'CAFE',
    name: '스타벅스 대전대점',
    lat: 36.3263,
    lng: 127.4553,
  },
  {
    propertyId: 'P01',
    category: 'SCHOOL',
    name: '가양초등학교',
    lat: 36.3259,
    lng: 127.4541,
  },
  {
    propertyId: 'P01',
    category: 'PARK',
    name: '용운근린공원',
    lat: 36.3263,
    lng: 127.4529,
  },

  // P02
  {
    propertyId: 'P02',
    category: 'BUS_TERMINAL',
    name: '대전복합터미널',
    lat: 36.3318,
    lng: 127.4589,
  },
  {
    propertyId: 'P02',
    category: 'PHARMACY',
    name: '온누리약국',
    lat: 36.3314,
    lng: 127.4601,
  },
  {
    propertyId: 'P02',
    category: 'MART',
    name: '홈플러스 가오점',
    lat: 36.3296,
    lng: 127.4601,
  },
  {
    propertyId: 'P02',
    category: 'ACADEMY',
    name: '이룸수학학원',
    lat: 36.3292,
    lng: 127.4589,
  },
  {
    propertyId: 'P02',
    category: 'CULTURE',
    name: '동구문화체육센터',
    lat: 36.3296,
    lng: 127.4577,
  },

  // P03
  {
    propertyId: 'P03',
    category: 'TRAIN',
    name: '대전역',
    lat: 36.3264,
    lng: 127.4608,
  },
  {
    propertyId: 'P03',
    category: 'CONVENIENCE',
    name: 'GS25 용운점',
    lat: 36.326,
    lng: 127.462,
  },
  {
    propertyId: 'P03',
    category: 'KINDERGARTEN',
    name: '햇살유치원',
    lat: 36.3242,
    lng: 127.462,
  },
  {
    propertyId: 'P03',
    category: 'SPORTS',
    name: '동구실내체육관',
    lat: 36.3238,
    lng: 127.4608,
  },

  // P04
  {
    propertyId: 'P04',
    category: 'PARKING',
    name: '용운공영주차장',
    lat: 36.3343,
    lng: 127.453,
  },
  {
    propertyId: 'P04',
    category: 'FOOD',
    name: '용운칼국수',
    lat: 36.3339,
    lng: 127.4542,
  },
  {
    propertyId: 'P04',
    category: 'LIBRARY',
    name: '한밭도서관',
    lat: 36.3321,
    lng: 127.4542,
  },
  {
    propertyId: 'P04',
    category: 'SWIMMING',
    name: '용운국제수영장',
    lat: 36.3317,
    lng: 127.453,
  },

  // P05
  {
    propertyId: 'P05',
    category: 'GAS',
    name: 'GS칼텍스 용운주유소',
    lat: 36.3253,
    lng: 127.452,
  },
  {
    propertyId: 'P05',
    category: 'BANK',
    name: '하나은행 대전대점',
    lat: 36.3249,
    lng: 127.4532,
  },
  {
    propertyId: 'P05',
    category: 'GOV_OFFICE',
    name: '용운동 행정복지센터',
    lat: 36.3231,
    lng: 127.4532,
  },
  {
    propertyId: 'P05',
    category: 'POST_OFFICE',
    name: '대전용운우체국',
    lat: 36.3227,
    lng: 127.452,
  },

  // P06
  {
    propertyId: 'P06',
    category: 'PUBLIC',
    name: '대전동구청',
    lat: 36.3333,
    lng: 127.464,
  },
  {
    propertyId: 'P06',
    category: 'POLICE',
    name: '대전동부경찰서',
    lat: 36.3329,
    lng: 127.4652,
  },
  {
    propertyId: 'P06',
    category: 'FIRE',
    name: '동부소방서',
    lat: 36.3311,
    lng: 127.4652,
  },
  {
    propertyId: 'P06',
    category: 'FIRE',
    name: '동부소방서',
    lat: 36.335,
    lng: 127.468,
  },
];

const RAW_SAFETY = [
  {
    propertyId: 'P01',
    type: 'CCTV',
    lat: 36.3281,
    lng: 127.4529,
  },
  {
    propertyId: 'P03',
    type: 'POLICE_CENTER',
    lat: 36.3242,
    lng: 127.4596,
  },
  {
    propertyId: 'P04',
    type: 'SAFETY_BELL',
    lat: 36.3321,
    lng: 127.4518,
  },
  {
    propertyId: 'P05',
    type: 'SECURITY_LIGHT',
    lat: 36.3231,
    lng: 127.4508,
  },
  {
    propertyId: 'P06',
    type: 'CHILD_SAFE_ZONE',
    lat: 36.3307,
    lng: 127.464,
  },
  {
    propertyId: 'P06',
    type: 'CHILD_GUARD_HOUSE',
    lat: 36.3311,
    lng: 127.4628,
  },
];

const center = { lat: 36.3366, lng: 127.459 };

const selectedId = ref(null);
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
  RAW_MARKERS.map((m) => ({ ...m, selected: m.id === selectedId.value })),
);

const markers = computed(() =>
  listItems.value.filter((m) => !selectedId.value || m.selected),
);

const dots = computed(() => {
  if (!selectedId.value) return [];

  // 인프라/편의시설 관련
  const infra = RAW_INFRA.filter((i) => i.propertyId === selectedId.value).map(
    (i) => ({ lat: i.lat, lng: i.lng, category: i.category, name: i.name }),
  );

  // 안전 지표 관련
  const safety = RAW_SAFETY.filter(
    (s) => s.propertyId === selectedId.value,
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
  selectedId.value = selectedId.value === marker.id ? null : marker.id;
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

watch(selectedId, closePanel);
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
