<template>
  <PageHeader title="매물 검색 결과" />
  <div class="property-list">
    <div class="fixed-top">
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
          <span
            class="dot-info-swatch"
            :style="{ background: activeDotColor }"
          />
          <span class="dot-info-text">{{ activeDotText }}</span>
          <button v-if="pinnedDot" class="dot-info-close" @click="closePanel">
            ×
          </button>
        </div>

        <button
          v-if="selectedBuildingId"
          class="reset-btn"
          @click="clearSelection"
        >
          전체 보기
        </button>
      </div>

      <div class="filter-chips" @wheel="onWheelX">
        <button
          class="fchip"
          :class="{ on: filter.tradeTypes.length > 0 }"
          @click="openSheet('trade')"
        >
          {{ tradeChipLabel }}
        </button>
        <button
          class="fchip"
          :class="{
            on: filter.maxDeposit != null || filter.maxMonthlyRent != null,
          }"
          @click="openSheet('price')"
        >
          {{ priceChipLabel }}
        </button>
        <button
          class="fchip"
          :class="{ on: filter.maxWorkplaceDistanceMeters != null }"
          @click="openSheet('commute')"
        >
          {{ commuteChipLabel }}
        </button>
        <button
          class="fchip"
          :class="{ on: filterChipOn }"
          @click="openSheet('filter')"
        >
          필터
        </button>
      </div>

      <div class="result-row">
        <p class="result-count">
          조건에 맞는 매물 <b>{{ totalCount }}건</b>
        </p>
        <button class="sort-btn" @click="openSheet('sort')">
          {{ sortLabel }}
          <svg width="10" height="10" viewBox="0 0 10 10" fill="none">
            <path
              d="M2 3.5L5 6.5L8 3.5"
              stroke="#545045"
              stroke-width="1.4"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
        </button>
      </div>

      <div
        class="priority-row"
        @click="openSheet('priority')"
        v-if="filter.sort === 'recommend'"
        @wheel="onWheelX"
      >
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
          <path
            d="M2 4h9M2 8h6M2 12h4M13 6v7M13 13l-2-2M13 13l2-2"
            stroke="#545045"
            stroke-width="1.3"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
        <span v-for="p in priorityChips" :key="p.criterion" class="pchip">
          <b class="pnum">{{ p.priorityOrder }}</b>
          {{ criterionLabel(p.criterion) }}
        </span>
        <svg width="13" height="13" viewBox="0 0 12 12" fill="none">
          <path
            d="M8.5 1.5l2 2L4 10H2V8l6.5-6.5z"
            stroke="#8a8d8f"
            stroke-width="1.1"
          />
        </svg>
      </div>
    </div>
    <div ref="scrollArea" class="scroll-area">
      <ul v-if="listItems.length" class="cards">
        <li v-for="p in listItems" :key="p.propertyId">
          <div
            class="card"
            :class="{ selected: p.selected }"
            @click="onCardClick(p)"
          >
            <span class="thumb">
              <svg width="30" height="30" viewBox="0 0 30 30" fill="none">
                <path
                  d="M4 13.5L15 5l11 8.5"
                  stroke="#8a8477"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
                <path
                  d="M7 12v12h16V12"
                  stroke="#8a8477"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
            </span>

            <div class="card-texts">
              <p class="card-title">{{ p.title }}</p>
              <p class="card-sub">
                {{ p.propertyType
                }}<template v-if="p.buildingName?.trim()">
                  · {{ p.buildingName }}</template
                >
              </p>
              <p class="card-price">{{ priceLabel(p) }}</p>
              <p class="card-meta">
                {{ pyeong(p.areaM2) }}평 · {{ floorLabel(p.floorInfo) }} ·
                관리비 {{ p.maintenanceFee }}만
              </p>
            </div>

            <button
              class="card-go"
              :class="{ on: p.selected }"
              aria-label="상세 보기"
              @click.stop="goDetail(p)"
            >
              <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                <path
                  d="M5 3l4 4-4 4"
                  :stroke="p.selected ? '#fff' : '#8a8d8f'"
                  stroke-width="1.7"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
            </button>
          </div>
          <div v-if="p.dividerAfter" class="list-divider">
            <span>그 외 매물</span>
          </div>
        </li>
      </ul>

      <div v-else class="empty">
        <svg width="40" height="40" viewBox="0 0 40 40" fill="none">
          <circle cx="18" cy="18" r="11" stroke="#c9c5bd" stroke-width="2.2" />
          <path
            d="M26 26l7 7"
            stroke="#c9c5bd"
            stroke-width="2.2"
            stroke-linecap="round"
          />
        </svg>
        <p class="empty-title">조건에 맞는 매물이 없어요</p>
        <p class="empty-sub">필터를 조금 넓혀보세요</p>
      </div>
    </div>
  </div>
  <AppTabBar active="property" />
</template>

<script setup>
import PageHeader from '@/components/PageHeader.vue';
import AppTabBar from '@/components/AppTabBar.vue';
import KakaoMap from '@/components/KakaoMap.vue';
import { computed, ref, reactive, watch } from 'vue';
import { useRouter } from 'vue-router';
import { infraColor, INFRA_ICON_PATHS } from '@/constants/infraIcons';
import {
  safetyColor,
  SAFETY_ICON_PATHS,
  safetyLabel,
} from '@/constants/safetyIcons';

// mock data
const RAW_PROPERTIES = [
  // B01 용운마젤란21 — 3건
  {
    propertyId: 'P01',
    buildingId: 'B01',
    buildingName: '용운마젤란21',
    title: '용운마젤란21 1302호',
    propertyType: '아파트',
    tradeType: '월세',
    deposit: 3700,
    monthlyRent: 69,
    maintenanceFee: 13,
    areaM2: 84.9,
    floorInfo: '13 / 15층',
    address: '대전광역시 동구 대학로 102-7',
    latitude: 36.3272,
    longitude: 127.4541,
    thumbnailUrl: 'https://example.com/properties/P01-1.jpg',
    desiredInfraCount: 3,
    desiredAmenityCount: 2,
    recommendScore: 84,
    workplaceDistanceMeters: 1070,
  },

  {
    propertyId: 'P02',
    buildingId: 'B01',
    buildingName: '용운마젤란21',
    title: '용운마젤란21 1103호',
    propertyType: '아파트',
    tradeType: '매매',
    deposit: 25300,
    monthlyRent: 0,
    maintenanceFee: 12,
    areaM2: 84.9,
    floorInfo: '11 / 15층',
    address: '대전광역시 동구 대학로 102-7',
    latitude: 36.3272,
    longitude: 127.4541,
    thumbnailUrl: 'https://example.com/properties/P02-1.jpg',
    desiredInfraCount: 3,
    desiredAmenityCount: 2,
    recommendScore: null,
    workplaceDistanceMeters: 1070,
  },

  {
    propertyId: 'P03',
    buildingId: 'B01',
    buildingName: '용운마젤란21',
    title: '용운마젤란21 1401호',
    propertyType: '아파트',
    tradeType: '월세',
    deposit: 700,
    monthlyRent: 105,
    maintenanceFee: 12,
    areaM2: 111.2,
    floorInfo: '14 / 15층',
    address: '대전광역시 동구 대학로 102-7',
    latitude: 36.3272,
    longitude: 127.4541,
    thumbnailUrl: null,
    desiredInfraCount: 3,
    desiredAmenityCount: 2,
    recommendScore: 72,
    workplaceDistanceMeters: 1070,
  },

  // B02 한화꿈에그린 — 2건
  {
    propertyId: 'P04',
    buildingId: 'B02',
    buildingName: '한화꿈에그린',
    title: '한화꿈에그린 1504호',
    propertyType: '아파트',
    tradeType: '전세',
    deposit: 15200,
    monthlyRent: 0,
    maintenanceFee: 10,
    areaM2: 84.49,
    floorInfo: '15 / 15층',
    address: '대전광역시 동구 대학로50번길 53',
    latitude: 36.3305,
    longitude: 127.4589,
    thumbnailUrl: 'https://example.com/properties/P04-1.jpg',
    desiredInfraCount: 2,
    desiredAmenityCount: 3,
    recommendScore: 91,
    workplaceDistanceMeters: 570,
  },

  {
    propertyId: 'P05',
    buildingId: 'B02',
    buildingName: '한화꿈에그린',
    title: '한화꿈에그린 1004호',
    propertyType: '아파트',
    tradeType: '월세',
    deposit: 1300,
    monthlyRent: 115,
    maintenanceFee: 15,
    areaM2: 112.96,
    floorInfo: '10 / 15층',
    address: '대전광역시 동구 대학로50번길 53',
    latitude: 36.3305,
    longitude: 127.4589,
    thumbnailUrl: 'https://example.com/properties/P05-1.jpg',
    desiredInfraCount: 2,
    desiredAmenityCount: 3,
    recommendScore: 66,
    workplaceDistanceMeters: 570,
  },

  // B03 용방마을아파트 — 1건
  {
    propertyId: 'P06',
    buildingId: 'B03',
    buildingName: '용방마을아파트',
    title: '용방마을주공3단지 1202호',
    propertyType: '아파트',
    tradeType: '전세',
    deposit: 9500,
    monthlyRent: 0,
    maintenanceFee: 10,
    areaM2: 59.94,
    floorInfo: '12 / 15층',
    address: '대전광역시 동구 용운동 460',
    latitude: 36.3251,
    longitude: 127.4608,
    thumbnailUrl: 'https://example.com/properties/P06-1.jpg',
    desiredInfraCount: 1,
    desiredAmenityCount: 1,
    recommendScore: 87,
    workplaceDistanceMeters: 1170,
  },

  // B04 에코포레 — 4건
  {
    propertyId: 'P07',
    buildingId: 'B04',
    buildingName: '에코포레',
    title: 'e편한세상대전에코포레 2101호',
    propertyType: '아파트',
    tradeType: '전세',
    deposit: 35400,
    monthlyRent: 0,
    maintenanceFee: 13,
    areaM2: 84.97,
    floorInfo: '21 / 34층',
    address: '대전광역시 동구 용운로 203',
    latitude: 36.333,
    longitude: 127.453,
    thumbnailUrl: 'https://example.com/properties/P07-1.jpg',
    desiredInfraCount: 2,
    desiredAmenityCount: 0,
    recommendScore: 79,
    workplaceDistanceMeters: 680,
  },

  {
    propertyId: 'P08',
    buildingId: 'B04',
    buildingName: '에코포레',
    title: 'e편한세상대전에코포레 601호',
    propertyType: '아파트',
    tradeType: '매매',
    deposit: 44000,
    monthlyRent: 0,
    maintenanceFee: 12,
    areaM2: 84.97,
    floorInfo: '6 / 34층',
    address: '대전광역시 동구 용운로 203',
    latitude: 36.333,
    longitude: 127.453,
    thumbnailUrl: 'https://example.com/properties/P08-1.jpg',
    desiredInfraCount: 2,
    desiredAmenityCount: 0,
    recommendScore: 58,
    workplaceDistanceMeters: 680,
  },

  {
    propertyId: 'P09',
    buildingId: 'B04',
    buildingName: '에코포레',
    title: 'e편한세상대전에코포레 3201호',
    propertyType: '아파트',
    tradeType: '월세',
    deposit: 9000,
    monthlyRent: 77,
    maintenanceFee: 15,
    areaM2: 75.34,
    floorInfo: '32 / 34층',
    address: '대전광역시 동구 용운로 203',
    latitude: 36.333,
    longitude: 127.453,
    thumbnailUrl: 'https://example.com/properties/P09-1.jpg',
    desiredInfraCount: 2,
    desiredAmenityCount: 0,
    recommendScore: 83,
    workplaceDistanceMeters: 680,
  },

  {
    propertyId: 'P10',
    buildingId: 'B04',
    buildingName: '에코포레',
    title: 'e편한세상대전에코포레 903호',
    propertyType: '아파트',
    tradeType: '월세',
    deposit: 5000,
    monthlyRent: 92,
    maintenanceFee: 12,
    areaM2: 59.88,
    floorInfo: '9 / 34층',
    address: '대전광역시 동구 용운로 203',
    latitude: 36.333,
    longitude: 127.453,
    thumbnailUrl: 'https://example.com/properties/P10-1.jpg',
    desiredInfraCount: 2,
    desiredAmenityCount: 0,
    recommendScore: 75,
    workplaceDistanceMeters: 680,
  },

  // B05 — 건물명 없음 (오피스텔)
  {
    propertyId: 'P11',
    buildingId: 'B05',
    buildingName: '',
    title: '대학로62-67 202호',
    propertyType: '오피스텔',
    tradeType: '월세',
    deposit: 1300,
    monthlyRent: 36,
    maintenanceFee: 7,
    areaM2: 26.4,
    floorInfo: '2 / 7층',
    address: '대전광역시 동구 대학로 62-67',
    latitude: 36.324,
    longitude: 127.452,
    thumbnailUrl: 'https://example.com/properties/P11-1.jpg',
    desiredInfraCount: 0,
    desiredAmenityCount: 2,
    recommendScore: 69,
    workplaceDistanceMeters: 1470,
  },

  // B06 — 건물명 없음 (원룸). 지하·옥탑 표기 확인용
  {
    propertyId: 'P12',
    buildingId: 'B06',
    buildingName: '',
    title: '277-22 102호',
    propertyType: '원룸',
    tradeType: '월세',
    deposit: 1300,
    monthlyRent: 20,
    maintenanceFee: 5,
    areaM2: 18.0,
    floorInfo: '지하1 / 3층',
    address: '대전광역시 동구 용운동 277-22',
    latitude: 36.332,
    longitude: 127.464,
    thumbnailUrl: null,
    desiredInfraCount: 1,
    desiredAmenityCount: 1,
    recommendScore: null,
    workplaceDistanceMeters: 540,
  },

  {
    propertyId: 'P13',
    buildingId: 'B06',
    buildingName: '',
    title: '277-22 옥탑',
    propertyType: '원룸',
    tradeType: '월세',
    deposit: 300,
    monthlyRent: 34,
    maintenanceFee: 4,
    areaM2: 20.0,
    floorInfo: '옥탑 / 3층',
    address: '대전광역시 동구 용운동 277-22',
    latitude: 36.332,
    longitude: 127.464,
    thumbnailUrl: 'https://example.com/properties/P13-1.jpg',
    desiredInfraCount: 1,
    desiredAmenityCount: 1,
    recommendScore: 62,
    workplaceDistanceMeters: 540,
  },
];

const RAW_INFRA = [
  // B01
  {
    buildingId: 'B01',
    category: 'SUBWAY',
    name: '판암역',
    latitude: 36.3285,
    longitude: 127.4541,
  },
  {
    buildingId: 'B01',
    category: 'HOSPITAL',
    name: '대전한국병원',
    latitude: 36.3281,
    longitude: 127.4553,
  },
  {
    buildingId: 'B01',
    category: 'CAFE',
    name: '스타벅스 대전대점',
    latitude: 36.3263,
    longitude: 127.4553,
  },
  {
    buildingId: 'B01',
    category: 'SCHOOL',
    name: '가양초등학교',
    latitude: 36.3259,
    longitude: 127.4541,
  },
  {
    buildingId: 'B01',
    category: 'PARK',
    name: '용운근린공원',
    latitude: 36.3263,
    longitude: 127.4529,
  },

  // B02
  {
    buildingId: 'B02',
    category: 'BUS_TERMINAL',
    name: '대전복합터미널',
    latitude: 36.3318,
    longitude: 127.4589,
  },
  {
    buildingId: 'B02',
    category: 'PHARMACY',
    name: '온누리약국',
    latitude: 36.3314,
    longitude: 127.4601,
  },
  {
    buildingId: 'B02',
    category: 'MART',
    name: '홈플러스 가오점',
    latitude: 36.3296,
    longitude: 127.4601,
  },
  {
    buildingId: 'B02',
    category: 'ACADEMY',
    name: '이룸수학학원',
    latitude: 36.3292,
    longitude: 127.4589,
  },
  {
    buildingId: 'B02',
    category: 'CULTURE',
    name: '동구문화체육센터',
    latitude: 36.3296,
    longitude: 127.4577,
  },

  // B03
  {
    buildingId: 'B03',
    category: 'TRAIN',
    name: '대전역',
    latitude: 36.3264,
    longitude: 127.4608,
  },
  {
    buildingId: 'B03',
    category: 'CONVENIENCE',
    name: 'GS25 용운점',
    latitude: 36.326,
    longitude: 127.462,
  },
  {
    buildingId: 'B03',
    category: 'KINDERGARTEN',
    name: '햇살유치원',
    latitude: 36.3242,
    longitude: 127.462,
  },
  {
    buildingId: 'B03',
    category: 'SPORTS',
    name: '동구실내체육관',
    latitude: 36.3238,
    longitude: 127.4608,
  },

  // B04
  {
    buildingId: 'B04',
    category: 'PARKING',
    name: '용운공영주차장',
    latitude: 36.3343,
    longitude: 127.453,
  },
  {
    buildingId: 'B04',
    category: 'FOOD',
    name: '용운칼국수',
    latitude: 36.3339,
    longitude: 127.4542,
  },
  {
    buildingId: 'B04',
    category: 'LIBRARY',
    name: '한밭도서관',
    latitude: 36.3321,
    longitude: 127.4542,
  },
  {
    buildingId: 'B04',
    category: 'SWIMMING',
    name: '용운국제수영장',
    latitude: 36.3317,
    longitude: 127.453,
  },

  // B05
  {
    buildingId: 'B05',
    category: 'GAS',
    name: 'GS칼텍스 용운주유소',
    latitude: 36.3253,
    longitude: 127.452,
  },
  {
    buildingId: 'B05',
    category: 'BANK',
    name: '하나은행 대전대점',
    latitude: 36.3249,
    longitude: 127.4532,
  },
  {
    buildingId: 'B05',
    category: 'GOV_OFFICE',
    name: '용운동 행정복지센터',
    latitude: 36.3231,
    longitude: 127.4532,
  },
  {
    buildingId: 'B05',
    category: 'POST_OFFICE',
    name: '대전용운우체국',
    latitude: 36.3227,
    longitude: 127.452,
  },

  // B06
  {
    buildingId: 'B06',
    category: 'PUBLIC',
    name: '대전동구청',
    latitude: 36.3333,
    longitude: 127.464,
  },
  {
    buildingId: 'B06',
    category: 'POLICE',
    name: '대전동부경찰서',
    latitude: 36.3329,
    longitude: 127.4652,
  },
  {
    buildingId: 'B06',
    category: 'FIRE',
    name: '동부소방서',
    latitude: 36.3311,
    longitude: 127.4652,
  },
  {
    buildingId: 'B06',
    category: 'FIRE',
    name: '동부119안전센터',
    latitude: 36.335,
    longitude: 127.468,
  },
];

const RAW_SAFETY = [
  { buildingId: 'B01', type: 'CCTV', latitude: 36.3281, longitude: 127.4529 },
  {
    buildingId: 'B03',
    type: 'POLICE_CENTER',
    latitude: 36.3242,
    longitude: 127.4596,
  },
  {
    buildingId: 'B04',
    type: 'SAFETY_BELL',
    latitude: 36.3321,
    longitude: 127.4518,
  },
  {
    buildingId: 'B05',
    type: 'SECURITY_LIGHT',
    latitude: 36.3231,
    longitude: 127.4508,
  },
  {
    buildingId: 'B06',
    type: 'CHILD_SAFE_ZONE',
    latitude: 36.3307,
    longitude: 127.464,
  },
  {
    buildingId: 'B06',
    type: 'CHILD_GUARD_HOUSE',
    latitude: 36.3311,
    longitude: 127.4628,
  },
];

const router = useRouter();
const center = { lat: 36.3366, lng: 127.459 };

const scrollArea = ref(null);
const selectedBuildingId = ref(null);
const selectedPropertyId = ref(null);
const selectionSource = ref(null);
const bounds = ref(null);
const pinnedDot = ref(null);
const hoveredDot = ref(null);

const activeDot = computed(() => hoveredDot.value ?? pinnedDot.value);
const items = ref(RAW_PROPERTIES);

const filter = reactive({
  tradeTypes: ['월세', '전세', '매매'],
  propertyTypes: [],
  minDeposit: null,
  maxDeposit: 1000,
  minMonthlyRent: null,
  maxMonthlyRent: null,
  minAreaM2: null,
  maxAreaM2: null,
  floorPreference: [],
  maxWorkplaceDistanceMeters: 2000,
  sort: 'recommend',
});

const SORT_OPTIONS = [
  { key: 'recommend', label: '추천순' },
  { key: 'distance', label: '거리순' },
  { key: 'price', label: '가격순' },
  { key: 'infra', label: '인프라 많은순' },
  { key: 'amenity', label: '편의시설 많은순' },
  { key: 'area', label: '면적순' },
];

const SORT_SPECS = {
  recommend: { value: (p) => p.recommendScore, dir: 'desc' },
  distance: { value: (p) => p.workplaceDistanceMeters, dir: 'asc' },
  price: { value: (p) => p.monthlyRent + p.deposit / 100, dir: 'asc' },
  infra: { value: (p) => p.desiredInfraCount, dir: 'desc' },
  amenity: { value: (p) => p.desiredAmenityCount, dir: 'desc' },
  area: { value: (p) => p.areaM2, dir: 'desc' },
};

const priorityChips = ref([
  { criterion: '직주근접', priorityOrder: 1 },
  { criterion: '가성비', priorityOrder: 2 },
  { criterion: '편의시설', priorityOrder: 3 },
]);

const openedSheet = ref(null);

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

const sortedItems = computed(() => {
  const spec = SORT_SPECS[filter.sort];
  if (!spec) return [...items.value];

  const missing = spec.dir === 'asc' ? Infinity : -Infinity;

  return items.value
    .map((item, index) => {
      const v = spec.value(item);
      return { item, index, v: Number.isFinite(v) ? v : missing };
    })
    .sort((a, b) =>
      a.v === b.v
        ? a.index - b.index
        : spec.dir === 'asc'
          ? a.v - b.v
          : b.v - a.v,
    )
    .map((e) => e.item);
});

const listItems = computed(() => {
  const flagged = sortedItems.value.map((p) => ({
    ...p,
    selected: selectedPropertyId.value
      ? p.propertyId === selectedPropertyId.value
      : p.buildingId === selectedBuildingId.value,
  }));
  if (!selectedBuildingId.value || selectionSource.value !== 'pin') {
    return flagged;
  }

  const picked = [];
  const rest = [];
  for (const p of flagged) {
    (p.buildingId === selectedBuildingId.value ? picked : rest).push(p);
  }

  if (picked.length && rest.length) {
    picked[picked.length - 1] = {
      ...picked[picked.length - 1],
      dividerAfter: true,
    };
  }

  return [...picked, ...rest];
});

const markers = computed(() => {
  const grouped = new Map();

  for (const p of items.value) {
    if (p.latitude == null || p.longitude == null) continue;

    if (!grouped.has(p.buildingId)) {
      grouped.set(p.buildingId, {
        id: p.buildingId,
        lat: Number(p.latitude),
        lng: Number(p.longitude),
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
    lat: Number(i.latitude),
    lng: Number(i.longitude),
    category: i.category,
    name: i.name,
  }));

  // 안전 지표 관련
  const safety = RAW_SAFETY.filter(
    (s) => s.buildingId === selectedBuildingId.value,
  ).map((s) => ({
    lat: Number(s.latitude),
    lng: Number(s.longitude),
    category: s.type,
    name: safetyLabel(s.type),
  }));

  return [...infra, ...safety];
});

const tradeChipLabel = computed(() => {
  const v = filter.tradeTypes;
  if (v.length === 0) return '거래유형';
  return v.length === 1 ? v[0] : `${v[0]} 외 ${v.length - 1}`;
});

const priceChipLabel = computed(() => {
  const parts = [];
  if (filter.maxDeposit != null)
    parts.push(`보증금 ${moneyLabel(filter.maxDeposit)} 이하`);
  if (filter.maxMonthlyRent != null)
    parts.push(`월세 ${filter.maxMonthlyRent} 이하`);
  return parts.length ? parts.join(' · ') : '금액';
});

const commuteChipLabel = computed(() => {
  const m = filter.maxWorkplaceDistanceMeters;
  if (m == null) return '통근';
  return m >= 1000 ? `직장 ${(m / 1000).toFixed(1)}km 이내` : `직장 ${m}m 이내`;
});

const sortLabel = computed(
  () => SORT_OPTIONS.find((o) => o.key === filter.sort)?.label ?? '추천순',
);

const totalCount = computed(() => listItems.value.length);

const filterChipOn = computed(
  () =>
    filter.propertyTypes.length > 0 ||
    filter.floorPreference.length > 0 ||
    filter.minAreaM2 != null ||
    filter.maxAreaM2 != null,
);

function criterionLabel(criterion) {
  return criterion;
}

function openSheet(name) {
  openedSheet.value = name;
  console.log('시트 열기:', name); // 바텀시트는 다음 이슈
}

function clearSelection() {
  selectedBuildingId.value = null;
  selectedPropertyId.value = null;
  selectionSource.value = null;
}

function onMarkerClick(marker) {
  if (selectedBuildingId.value === marker.id) {
    clearSelection();
    return;
  }
  selectedBuildingId.value = marker.id;
  selectedPropertyId.value = null;
  selectionSource.value = 'pin';
}

function onCardClick(property) {
  const inHoistedGroup =
    selectionSource.value === 'pin' &&
    selectedBuildingId.value === property.buildingId;

  if (selectedPropertyId.value === property.propertyId) {
    selectedPropertyId.value = null;
    if (!inHoistedGroup) clearSelection();
    return;
  }
  selectedPropertyId.value = property.propertyId;
  if (inHoistedGroup) return;

  selectedBuildingId.value = property.buildingId;
  selectionSource.value = 'card';
}

function onBoundsChange(b) {
  bounds.value = b;
}

function onDotClick(dot) {
  pinnedDot.value = keyOf(pinnedDot.value) === keyOf(dot) ? null : dot;
}

function onDotHover(dot) {
  hoveredDot.value = dot;
}

function closePanel() {
  pinnedDot.value = null;
  hoveredDot.value = null;
}

function onWheelX(e) {
  const el = e.currentTarget;
  if (el.scrollWidth <= el.clientWidth) return;
  if (Math.abs(e.deltaY) < Math.abs(e.deltaX)) return;
  e.preventDefault();
  el.scrollLeft += e.deltaY;
}

function pyeong(areaM2) {
  return Math.round(areaM2 / 3.3058);
}

function floorLabel(floorInfo) {
  const head = floorInfo.split('/')[0].trim();
  return /^\d+$/.test(head) ? `${head}층` : head;
}

function moneyLabel(manwon) {
  if (manwon >= 10000) {
    const eok = Math.floor(manwon / 10000);
    const rest = manwon % 10000;
    return rest === 0 ? `${eok}억` : `${eok}억 ${rest}`;
  }
  return String(manwon);
}

function priceLabel(p) {
  return p.tradeType === '월세'
    ? `월세 ${moneyLabel(p.deposit)}/${p.monthlyRent}`
    : `${p.tradeType} ${moneyLabel(p.deposit)}`;
}

function goDetail(p) {
  router.push(`/properties/${p.propertyId}`);
}

watch(selectedBuildingId, (id) => {
  closePanel();
  if (id && selectionSource.value === 'pin') {
    scrollArea.value?.scrollTo({ top: 0 });
  }
});

watch(filter, () => {
  scrollArea.value?.scrollTo({ top: 0 });
});
</script>

<style scoped>
.property-list {
  height: calc(100dvh - 52px - 60px);
  display: flex;
  flex-direction: column;
  background: var(--bg);
}

.fixed-top {
  flex-shrink: 0;
  background: var(--bg);
}

.scroll-area {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

.map-area {
  position: relative;
  height: 250px;
}

.cards {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 5px 16px 15px;
}

.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 50px 20px;
}

.empty-title {
  margin-top: 6px;
  font-size: 14px;
  font-weight: 700;
  color: var(--kb-gray);
}

.empty-sub {
  font-size: 12px;
  color: var(--kb-silver);
}

.card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #fff;
  border: 1px solid #e9e7e2;
  border-radius: 14px;
  cursor: pointer;
}

.card.selected {
  background: #fff6dc;
  border: 2px solid #ffbc00;
  padding: 11px;
}

.thumb {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 62px;
  height: 62px;
  flex-shrink: 0;
  background: #f5efdb;
  border-radius: 10px;
}

.card.selected .thumb {
  background: #fff;
}

.card-texts {
  flex: 1;
  min-width: 0;
}

.card-title {
  font-size: 16px;
  font-weight: bold;
  color: #33302a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-sub,
.card-price {
  color: #60584c;
  margin-top: 2px;
  font-size: 12px;
  font-weight: 500;
}

.card-meta {
  font-size: 12px;
  font-weight: 400;
  color: #8a8d8f;
  margin-top: 3px;
}

.card-go {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  flex-shrink: 0;
  border: 1px solid #e9e7e2;
  border-radius: 50%;
  background: #fff;
  cursor: pointer;
}

.card-go.on {
  background: #ffbc00;
  border-color: #ffbc00;
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

.filter-chips {
  display: flex;
  gap: 8px;
  padding: 12px 16px 0;
  overflow-x: auto;
}

.fchip {
  flex-shrink: 0;
  padding: 8px 14px;
  border: 1px solid #e9e7e2;
  border-radius: 100px;
  background: #fff;
  font-size: 12.5px;
  color: #33302a;
  white-space: nowrap;
  cursor: pointer;
}

.fchip.on {
  border-color: #ffbc00;
  background: #fff6dc;
  font-weight: 700;
}

.result-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 16px 0;
}

.result-count {
  font-size: 12.5px;
  color: #60584c;
}

.result-count b {
  color: #33302a;
  font-weight: 700;
}

.sort-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  border: none;
  background: none;
  font-size: 12.5px;
  color: #545045;
  cursor: pointer;
}

.priority-row {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  overflow-x: auto;
  cursor: pointer;
}

.filter-chips,
.priority-row {
  scrollbar-width: none;
}

.filter-chips::-webkit-scrollbar,
.priority-row::-webkit-scrollbar {
  display: none;
}

.pchip {
  display: flex;
  align-items: center;
  gap: 5px;
  flex-shrink: 0;
  padding: 5px 10px 5px 5px;
  border: 1px solid #e9e7e2;
  border-radius: 100px;
  background: #fff;
  font-size: 11.5px;
  color: #33302a;
  white-space: nowrap;
}

.pnum {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 16px;
  height: 16px;
  flex-shrink: 0;
  border-radius: 50%;
  background: #ffbc00;
  font-size: 10px;
  font-weight: 700;
}

.list-divider {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 16px 2px 4px;
  font-size: 11.5px;
  color: var(--kb-silver);
  white-space: nowrap;
}

.list-divider::before,
.list-divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: var(--border);
}

@media (hover: hover) {
  .filter-chips {
    scrollbar-width: thin;
    scrollbar-color: #eceae5 transparent;
  }
  .filter-chips::-webkit-scrollbar {
    display: block;
    height: 4px;
  }
  .filter-chips::-webkit-scrollbar-track {
    background: transparent;
  }
  .filter-chips::-webkit-scrollbar-thumb {
    background: #eceae5;
    border-radius: 2px;
  }
}
</style>
