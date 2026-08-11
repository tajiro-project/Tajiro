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
      />

      <div v-if="activeDot" class="dot-info">
        <span class="dot-info-swatch" :style="{ background: activeDotColor }" />
        <span class="dot-info-text">{{ activeDotText }}</span>
        <button v-if="pinnedDot" class="dot-info-close" @click="closePanel">
          ×
        </button>
      </div>

      <div v-if="selectedBuildingId" class="map-overlay">
        <InfraTogglePanel
          v-model="mapLayers"
          :categories="layerCategories"
          @open-settings="openSheet('infra')"
        />
      </div>

      <button
        v-if="selectedBuildingId"
        class="reset-btn"
        @click="clearSelection"
      >
        전체 보기
      </button>
    </div>
    <div
      class="sheet-top"
      :class="{ dragging: isSheetDragging }"
      :style="{ marginTop: `-${sheetOffset}px` }"
    >
      <button
        class="sheet-handle"
        :aria-label="isSheetUp ? '지도 보기' : '목록 넓게 보기'"
        @pointerdown="startSheetDrag"
        @click="toggleSheet"
      >
        <span class="handle-bar" />
        <svg
          class="handle-chevron"
          :class="{ down: isSheetUp }"
          width="16"
          height="16"
          viewBox="0 0 16 16"
          fill="none"
        >
          <path
            d="M3.5 10L8 5.5L12.5 10"
            stroke="#8a8477"
            stroke-width="1.6"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
      </button>
      <div v-if="!isRegionSearch" class="filter-chips" @wheel="onWheelX">
        <button
          class="fchip"
          :class="{ on: commuteChipOn }"
          @click="openSheet('commute')"
        >
          {{ commuteChipLabel }}
        </button>
        <button
          class="fchip"
          :class="{ on: housingChipOn }"
          @click="openSheet('housing')"
        >
          {{ housingChipLabel }}
        </button>
        <button
          class="fchip"
          :class="{ on: infraChipOn }"
          @click="openSheet('infra')"
        >
          {{ infraChipLabel }}
        </button>
      </div>

      <div class="result-row">
        <p class="result-count">
          <template v-if="isLoading">매물을 불러오는 중</template>
          <template v-else>
            조건에 맞는 매물 <b>{{ totalCount }}건</b>
          </template>
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
        v-if="!isRegionSearch && filter.sort === 'recommend'"
        class="priority-row"
        @click="openSheet('priority')"
        @wheel="onWheelX"
      >
        <svg
          class="prow-icon"
          width="18"
          height="18"
          viewBox="0 0 18 18"
          fill="none"
        >
          <path
            d="M2 5.5h3M8.5 5.5H16M2 12.5h7.5M13 12.5H16"
            stroke="#33302a"
            stroke-width="1.6"
            stroke-linecap="round"
          />
          <circle
            cx="6.75"
            cy="5.5"
            r="1.9"
            stroke="#33302a"
            stroke-width="1.6"
          />
          <circle
            cx="11.25"
            cy="12.5"
            r="1.9"
            stroke="#33302a"
            stroke-width="1.6"
          />
        </svg>

        <span v-for="p in priorityChips" :key="p.criterion" class="pchip">
          <b class="pnum">{{ p.priorityOrder }}</b>
          {{ criterionLabel(p.criterion) }}
        </span>
      </div>
    </div>
    <sidebar ref="scrollArea" class="scroll-area">
      <div v-if="isLoading" class="loading">
        <span class="spinner" />
        <p class="loading-text">매물을 불러오는 중이에요</p>
      </div>
      <div v-else-if="loadError" class="empty">
        <p class="empty-title">불러오지 못했어요</p>
        <p class="empty-sub">{{ loadError }}</p>
      </div>
      <ul v-else-if="listItems.length" class="cards">
        <li v-for="p in listItems" :key="p.propertyId">
          <div
            class="card"
            :class="{ selected: p.selected }"
            @click="onCardClick(p)"
          >
            <span class="thumb">
              <img v-if="p.rank" :src="MEDALS[p.rank]" :alt="`추천 ${p.rank}위`"" class="medal"/>
              <img
                v-if="p.thumbnailUrl && !p.thumbFailed"
                :src="p.thumbnailUrl"
                :alt="p.title"
                class="thumb-img"
                @error="p.thumbFailed = true"
              />
              <svg
                v-else
                width="30"
                height="30"
                viewBox="0 0 30 30"
                fill="none"
              >
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
              <div class="card-title-row">
                <p class="card-title">{{ p.title }}</p>
                <span v-if="p.recommendScore != null" class="score-chip">
                  {{ p.recommendScore }}점
                </span>
              </div>
              <p class="card-sub">
                {{ p.propertyType
                }}<template v-if="p.buildingName?.trim()">
                  · {{ p.buildingName.trim() }}</template
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
    </sidebar>
  </div>

  <!-- 희망 주거 조건 -->
  <BottomSheet
    :model-value="openedSheet === 'housing'"
    title="희망 주거 조건"
    @update:model-value="closeSheet"
  >
    <div class="field">
      <p class="field-name">매물 유형</p>
      <div class="opt-grid">
        <button
          v-for="t in PROPERTY_TYPES"
          :key="t"
          class="opt"
          :class="{ on: draft.propertyTypes.includes(t) }"
          @click="toggleIn(draft.propertyTypes, t)"
        >
          {{ t }}
        </button>
      </div>
    </div>

    <div class="field">
      <p class="field-name">거래 유형</p>
      <div class="opt-grid">
        <button
          v-for="t in TRADE_TYPES"
          :key="t"
          class="opt"
          :class="{ on: draft.tradeTypes.includes(t) }"
          @click="toggleDraftTrade(t)"
        >
          {{ t }}
        </button>
      </div>
    </div>

    <div v-if="draft.tradeTypes.length" class="range-card">
      <div
        v-if="
          draft.tradeTypes.includes('월세') || draft.tradeTypes.includes('전세')
        "
        class="range-group"
      >
        <p class="range-title">
          보증금/전세금
          <span class="range-value">{{ depositJeonseLabel }}</span>
        </p>
        <DualSlider
          v-model="draft.depositJeonse"
          :min="DEPOSIT_JEONSE.min"
          :max="DEPOSIT_JEONSE.max"
          :step="DEPOSIT_JEONSE.step"
          :marks="DEPOSIT_JEONSE.marks"
        />
      </div>

      <div v-if="draft.tradeTypes.includes('월세')" class="range-group">
        <p class="range-title">
          월세
          <span class="range-value">{{ rentValueLabel }}</span>
        </p>
        <DualSlider
          v-model="draft.rent"
          :min="MONTHLY_RENT.min"
          :max="MONTHLY_RENT.max"
          :step="MONTHLY_RENT.step"
          :marks="MONTHLY_RENT.marks"
        />
      </div>

      <div v-if="draft.tradeTypes.includes('매매')" class="range-group">
        <p class="range-title">
          매매가
          <span class="range-value">{{ salePriceLabel }}</span>
        </p>
        <DualSlider
          v-model="draft.salePrice"
          :min="SALE_PRICE.min"
          :max="SALE_PRICE.max"
          :step="SALE_PRICE.step"
          :marks="SALE_PRICE.marks"
        />
      </div>
    </div>

    <div class="field field-gap-top">
      <div class="field-head">
        <span class="field-name">매물 면적</span>
        <span class="range-value">{{ areaLabel }}</span>
      </div>
      <DualSlider
        v-model="draft.areaRange"
        :min="AREA.min"
        :max="AREA.max"
        :step="AREA.step"
        :marks="AREA.marks"
      />
    </div>

    <div class="field">
      <p class="field-name">매물 층수</p>
      <div class="opt-grid">
        <button
          v-for="f in FLOOR_OPTIONS"
          :key="f"
          class="opt"
          :class="{ on: draft.floorPreference.includes(f) }"
          @click="toggleIn(draft.floorPreference, f)"
        >
          {{ f }}
        </button>
      </div>
    </div>

    <div class="sheet-actions">
      <button class="btn-ghost" @click="resetHousing">초기화</button>
      <button class="btn-primary" @click="applyHousing">
        이 조건으로 적용
      </button>
    </div>
  </BottomSheet>

  <!-- 이주/통근 정보 -->
  <BottomSheet
    :model-value="openedSheet === 'commute'"
    title="이주/통근 정보"
    @update:model-value="closeSheet"
  >
    <div class="field">
      <p class="field-name">선호 위치 (직장 / 학교 등)</p>
      <input
        class="location-input"
        type="text"
        readonly
        :value="draft.workplace?.name || draft.workplace?.address || ''"
        placeholder="예) 창원시 성산구 상남동"
        @click="goLocationSelect"
        @keydown.enter.prevent="goLocationSelect"
      />
    </div>

    <div class="field">
      <div class="field-head">
        <span class="field-name">희망 통근 거리</span>
        <span class="range-value">{{ distanceLabel }}</span>
      </div>
      <SingleSlider
        v-model="draft.distance"
        :min="PREFERENCE_SLIDER_CONFIG.COMMUTE_DISTANCE.min"
        :max="PREFERENCE_SLIDER_CONFIG.COMMUTE_DISTANCE.max"
        :step="PREFERENCE_SLIDER_CONFIG.COMMUTE_DISTANCE.step"
        :marks="PREFERENCE_SLIDER_CONFIG.COMMUTE_DISTANCE.marks"
        aria-label="희망 통근 거리"
      />
    </div>

    <div class="field">
      <p class="field-name">자차 보유 여부</p>
      <div class="check-row">
        <label class="check-item" @click="draft.hasCar = true">
          <span class="checkbox" :class="{ on: draft.hasCar }">
            <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
              <path
                d="M2 6.5L4.7 9L10 3.5"
                stroke="#545045"
                stroke-width="1.8"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
          </span>
          자차 보유 O
        </label>
        <label class="check-item" @click="draft.hasCar = false">
          <span class="checkbox" :class="{ on: !draft.hasCar }">
            <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
              <path
                d="M2 6.5L4.7 9L10 3.5"
                stroke="#545045"
                stroke-width="1.8"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
          </span>
          자차 보유 X
        </label>
      </div>
    </div>

    <div class="sheet-actions">
      <button class="btn-ghost" @click="resetCommute">초기화</button>
      <button class="btn-primary" @click="applyCommute">
        이 조건으로 적용
      </button>
    </div>
  </BottomSheet>

  <!-- 인프라 · 편의시설 -->
  <BottomSheet
    :model-value="openedSheet === 'infra'"
    title="인프라 · 편의시설"
    @update:model-value="closeSheet"
  >
    <div class="field">
      <p class="field-name">희망 인프라</p>
      <p class="field-caption">반경 2km 이내만 표시</p>
      <div class="opt-grid">
        <button
          v-for="c in INFRA_CATEGORIES"
          :key="c.key"
          class="opt"
          :class="{ on: draft.infra.includes(c.key) }"
          @click="toggleIn(draft.infra, c.key)"
        >
          {{ c.label }}
        </button>
      </div>
    </div>

    <div class="field">
      <p class="field-name">희망 편의시설</p>
      <p class="field-caption">반경 2km 이내만 표시</p>
      <div class="opt-grid">
        <button
          v-for="c in AMENITY_CATEGORIES"
          :key="c.key"
          class="opt"
          :class="{ on: draft.amenity.includes(c.key) }"
          @click="toggleIn(draft.amenity, c.key)"
        >
          {{ c.label }}
        </button>
      </div>
    </div>

    <div class="sheet-actions">
      <button class="btn-ghost" @click="resetInfra">초기화</button>
      <button class="btn-primary" @click="applyInfra">이 조건으로 적용</button>
    </div>
  </BottomSheet>

  <!-- 정렬 -->
  <BottomSheet
    :model-value="openedSheet === 'sort'"
    title="정렬"
    @update:model-value="closeSheet"
  >
    <ul class="sort-list">
      <li v-for="o in sortOptions" :key="o.key">
        <button
          class="sort-item"
          :class="{ on: filter.sort === o.key }"
          @click="applySort(o.key)"
        >
          {{ o.label }}
          <svg
            v-if="filter.sort === o.key"
            width="14"
            height="14"
            viewBox="0 0 14 14"
            fill="none"
          >
            <path
              d="M3 7.5l3 3 5-6"
              stroke="#fe7b00"
              stroke-width="1.8"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
        </button>
      </li>
    </ul>
  </BottomSheet>

  <!-- 우선순위 -->
  <BottomSheet
    :model-value="openedSheet === 'priority'"
    title="가치관 우선순위 수정"
    @update:model-value="closeSheet"
  >
    <p class="sheet-note">중요한 순서대로 최대 3개까지 선택하세요.</p>
    <div class="priority-list">
      <button
        v-for="opt in PRIORITY_OPTIONS"
        :key="opt.criterion"
        class="priority-card"
        :class="{ on: priorityRank(opt.criterion) != null }"
        @click="togglePriority(opt.criterion)"
      >
        <span class="p-icon" v-html="opt.icon" />
        <span class="p-texts">
          <span class="p-title">{{ opt.title }}</span>
          <span class="p-sub">{{ opt.sub }}</span>
        </span>
        <span v-if="priorityRank(opt.criterion)" class="p-badge">
          {{ priorityRank(opt.criterion) }}
        </span>
      </button>
    </div>
    <div class="sheet-actions">
      <button class="btn-ghost" @click="draft.priorities = []">초기화</button>
      <button
        class="btn-primary"
        :disabled="draft.priorities.length === 0"
        @click="applyPriority"
      >
        이 순서로 적용
      </button>
    </div>
  </BottomSheet>

  <KakaoLocation
    :open="isLocationPickerOpen"
    :initial-location="draft.workplace"
    @close="isLocationPickerOpen = false"
    @select="selectWorkplace"
  />
</template>

<script setup>
import KakaoMap from '@/components/KakaoMap.vue';
import Sidebar from 'simplebar-vue';
import BottomSheet from '@/components/BottomSheet.vue';
import DualSlider from '@/components/DualSlider.vue';
import SingleSlider from '@/components/SingleSlider.vue';
import KakaoLocation from '@/components/KakaoLocation.vue';
import InfraTogglePanel from '@/components/InfraTogglePanel.vue';
import medalGold from '@/assets/img/medals/medal_gold_ribbon.svg';
import medalSilver from '@/assets/img/medals/medal_silver_ribbon.svg';
import medalBronze from '@/assets/img/medals/medal_bronze_ribbon.svg';

import {
  computed,
  ref,
  reactive,
  watch,
  onMounted,
  onBeforeUnmount,
} from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { infraColor } from '@/constants/infraIcons';

import { propertyApi, buildingApi } from '@/api/services';
import client, { getApiErrorMessage } from '@/api/client';

import {
  TRADE_OPTIONS as TRADE_TYPES,
  HOUSING_OPTIONS as PROPERTY_TYPES,
  FLOOR_OPTIONS,
  INFRA_CATEGORIES,
  AMENITY_CATEGORIES,
  PRIORITY_OPTIONS,
  MAX_PRIORITY_SELECTIONS,
  PREFERENCE_SLIDER_CONFIG,
} from '@/constants/preferenceOptions';

const router = useRouter();
const route = useRoute();
const center = { lat: 36.3366, lng: 127.459 };

const scrollArea = ref(null);
const selectedBuildingId = ref(null);
const selectedPropertyId = ref(null);
const selectionSource = ref(null);
const pinnedDot = ref(null);
const hoveredDot = ref(null);
const MEDALS = { 1: medalGold, 2: medalSilver, 3: medalBronze };

const activeDot = computed(() => hoveredDot.value ?? pinnedDot.value);
const items = ref([]);
const infraItems = ref([]);
const preference = ref(null);
const isLoading = ref(false);
const loadError = ref('');

const nullIfMin = (v, min) => (v <= min ? null : v);
const nullIfMax = (v, max) => (v >= max ? null : v);

async function fetchProperties() {
  isLoading.value = true;
  loadError.value = '';
  items.value = [];
  try {
    const { centerLat, centerLng } = route.query;
    items.value = await propertyApi.getList({ centerLat, centerLng });
  } catch (e) {
    if (e.response?.status === 404) {
      router.replace('/preferences/1');
      return;
    }
    loadError.value = getApiErrorMessage(e);
  } finally {
    isLoading.value = false;
  }
}

onMounted(async () => {
  if (!isRegionSearch.value) await loadPreference();
  await fetchProperties();
});

async function loadPreference() {
  try {
    const res = await client.get('/users/me/preferences');
    preference.value = res.data.data ?? res.data;
    applyPreferenceToFilter(preference.value);
  } catch {
    // 가치관이 없으면 fetchProperties 가 404 를 받아 PreferenceWizardView로 보낸다
  }
}

function applyPreferenceToFilter(p) {
  filter.tradeTypes = [...p.tradeTypes];
  filter.propertyTypes = [...p.housingTypes];
  filter.minDepositJeonse = nullIfMin(p.depositJeonseRange[0], 0);
  filter.maxDepositJeonse = nullIfMax(
    p.depositJeonseRange[1],
    DEPOSIT_JEONSE.max,
  );
  filter.minMonthlyRent = nullIfMin(p.monthlyRentRange[0], 0);
  filter.maxMonthlyRent = nullIfMax(p.monthlyRentRange[1], MONTHLY_RENT.max);
  filter.minSalePrice = nullIfMin(p.salePriceRange[0], SALE_PRICE.min);
  filter.maxSalePrice = nullIfMax(p.salePriceRange[1], SALE_PRICE.max);
  filter.minAreaM2 = nullIfMin(Number(p.areaRange[0]), 0);
  filter.maxAreaM2 = nullIfMax(Number(p.areaRange[1]), AREA.max);
  filter.floorPreference = [...p.floorPreference];
  filter.desiredInfraCategories = [...p.desiredInfraCategories];
  filter.desiredAmenityCategories = [...p.desiredAmenityCategories];
  filter.maxWorkplaceDistanceMeters = p.maxCommuteDistanceMeters;
  filter.workplace = p.workplace;
  filter.hasCar = p.hasCar;

  priorityChips.value = [...p.priorities].sort(
    (a, b) => a.priorityOrder - b.priorityOrder,
  );
}

async function commitFilter() {
  if (!preference.value) return;

  const workplace = filter.workplace ?? preference.value.workplace;

  const body = {
    ...preference.value,
    workplace: workplace
      ? {
          name: workplace.name,
          address: workplace.address,
          lat: workplace.lat,
          lng: workplace.lng,
        }
      : null,
    hasCar: filter.hasCar,
    maxCommuteDistanceMeters: filter.maxWorkplaceDistanceMeters,
    housingTypes: [...filter.propertyTypes],
    tradeTypes: [...filter.tradeTypes],
    depositJeonseRange: [
      filter.minDepositJeonse ?? 0,
      filter.maxDepositJeonse ?? DEPOSIT_JEONSE.max,
    ],
    monthlyRentRange: [
      filter.minMonthlyRent ?? 0,
      filter.maxMonthlyRent ?? MONTHLY_RENT.max,
    ],
    salePriceRange: [
      filter.minSalePrice ?? SALE_PRICE.min,
      filter.maxSalePrice ?? SALE_PRICE.max,
    ],
    areaRange: [filter.minAreaM2 ?? 0, filter.maxAreaM2 ?? AREA.max],
    floorPreference: [...filter.floorPreference],
    desiredInfraCategories: [...filter.desiredInfraCategories],
    desiredAmenityCategories: [...filter.desiredAmenityCategories],
    priorities: priorityChips.value.map((p) => ({
      criterion: p.criterion,
      priorityOrder: p.priorityOrder,
    })),
  };

  isLoading.value = true;
  try {
    const res = await client.put('/users/me/preferences', body);
    preference.value = res.data.data ?? res.data;
    await fetchProperties();
  } catch (e) {
    loadError.value = getApiErrorMessage(e);
  } finally {
    isLoading.value = false;
  }
}

watch(selectedBuildingId, async (id) => {
  if (!id) {
    infraItems.value = [];
    return;
  }

  try {
    const res = await buildingApi.infraPoints(id);
    infraItems.value = res?.data ?? [];
  } catch {
    infraItems.value = [];
  }
});

const { DEPOSIT_JEONSE, SALE_PRICE, MONTHLY_RENT, AREA } =
  PREFERENCE_SLIDER_CONFIG;

const DEFAULT_DISTANCE = PREFERENCE_SLIDER_CONFIG.COMMUTE_DISTANCE.defaultValue;
const PYEONG = 3.3058;

const filter = reactive({
  tradeTypes: [],
  propertyTypes: [],
  minDepositJeonse: null,
  maxDepositJeonse: null,
  minSalePrice: null,
  maxSalePrice: null,
  minMonthlyRent: null,
  maxMonthlyRent: null,
  minAreaM2: null,
  maxAreaM2: null,
  floorPreference: [],
  desiredInfraCategories: [],
  desiredAmenityCategories: [],
  maxWorkplaceDistanceMeters: DEFAULT_DISTANCE,
  workplace: null,
  hasCar: false,
  sort: route.query.centerLat != null ? 'distance' : 'recommend',
});

const SORT_OPTIONS = [
  { key: 'recommend', label: '추천순' },
  { key: 'distance', label: '거리순' },
  { key: 'price', label: '가격 낮은 순' },
  { key: 'infra', label: '인프라 많은 순' },
  { key: 'amenity', label: '편의시설 많은 순' },
  { key: 'area', label: '면적순' },
];

const CONVERSION_RATE = 0.053; // 전월세 전활율(전국)
function monthlyCost(p) {
  return p.monthlyRent + (p.deposit * CONVERSION_RATE) / 12 + p.maintenanceFee;
}

const SORT_SPECS = {
  recommend: { value: (p) => p.recommendScore, dir: 'desc' },
  distance: { value: (p) => p.distanceMeters, dir: 'asc' },
  price: { value: monthlyCost, dir: 'asc' },
  infra: { value: (p) => p.desiredInfraCount, dir: 'desc' },
  amenity: { value: (p) => p.desiredAmenityCount, dir: 'desc' },
  area: { value: (p) => p.areaM2, dir: 'desc' },
};

const priorityChips = ref([
  { criterion: 'COMMUTE', priorityOrder: 1 },
  { criterion: 'COST', priorityOrder: 2 },
  { criterion: 'AMENITY', priorityOrder: 3 },
]);

const isRegionSearch = computed(
  () => route.query.centerLat != null && route.query.centerLng != null,
);

const REGION_HIDDEN_SORTS = ['recommend', 'infra', 'amenity'];

const sortOptions = computed(() =>
  isRegionSearch.value
    ? SORT_OPTIONS.filter((o) => !REGION_HIDDEN_SORTS.includes(o.key))
    : SORT_OPTIONS,
);

const criterionLabel = (c) =>
  PRIORITY_OPTIONS.find((o) => o.criterion === c)?.title ?? c;

const openedSheet = ref(null);
const isLocationPickerOpen = ref(false);

const draft = reactive({
  tradeTypes: [],
  depositJeonse: [0, DEPOSIT_JEONSE.max],
  salePrice: [SALE_PRICE.min, SALE_PRICE.max],
  rent: [0, MONTHLY_RENT.max],
  propertyTypes: [],
  floorPreference: [],
  areaRange: [0, AREA.max],
  infra: [],
  amenity: [],
  distance: DEFAULT_DISTANCE,
  workplace: null,
  hasCar: false,
  priorities: [],
});

function keyOf(dot) {
  return dot ? `${dot.lat},${dot.lng}` : null;
}

const activeDotKey = computed(() => keyOf(activeDot.value));
const activeDotText = computed(() => activeDot.value?.name ?? '');

const activeDotColor = computed(() => {
  const c = activeDot.value?.category;
  if (!c) return '#8a8d8f';
  return infraColor(c);
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

const rankByPropertyId = computed(() => {
  if (filter.sort !== 'recommend') return {};

  const scored = items.value
    .filter((p) => p.recommendScore != null)
    .sort((a, b) => b.recommendScore - a.recommendScore);

  const map = {};
  let rank = 0;
  let prevScore = null;

  for (let i = 0; i < scored.length; i++) {
    const p = scored[i];

    if (p.recommendScore !== prevScore) {
      rank = i + 1;
      prevScore = p.recommendScore;
      if (rank > 3) break;
    }

    map[p.propertyId] = rank;
  }

  return map;
});

const listItems = computed(() => {
  const flagged = sortedItems.value.map((p) => ({
    ...p,
    rank: rankByPropertyId.value[p.propertyId] ?? null,
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

const ALL_CATEGORY_KEYS = [
  ...INFRA_CATEGORIES.map((c) => c.key),
  ...AMENITY_CATEGORIES.map((c) => c.key),
];

const layerCategories = computed(() =>
  isRegionSearch.value
    ? ALL_CATEGORY_KEYS
    : [...filter.desiredInfraCategories, ...filter.desiredAmenityCategories],
);
const mapLayers = ref([...layerCategories.value]);

watch(
  layerCategories,
  (v) => {
    mapLayers.value = isRegionSearch.value ? [] : [...v];
  },
  { immediate: true },
);

const dots = computed(() =>
  infraItems.value
    .filter((i) => mapLayers.value.includes(i.infraCategory))
    .map((i) => ({
      lat: Number(i.latitude),
      lng: Number(i.longitude),
      category: i.infraCategory,
      name: i.infraName,
    })),
);

const commuteChipOn = computed(
  () =>
    filter.workplace != null ||
    filter.hasCar ||
    filter.maxWorkplaceDistanceMeters !== DEFAULT_DISTANCE,
);

const commuteChipLabel = computed(() => {
  if (!commuteChipOn.value) return '이주/통근';
  const parts = [];
  const placeName = filter.workplace?.name || filter.workplace?.address;
  if (placeName) parts.push(placeName);
  const m = filter.maxWorkplaceDistanceMeters;
  if (m !== DEFAULT_DISTANCE)
    parts.push(m >= 1000 ? `${(m / 1000).toFixed(1)}km 이내` : `${m}m 이내`);
  if (filter.hasCar) parts.push('자차 O');
  return parts.slice(0, 2).join(' · ') || '이주/통근';
});

const housingChipOn = computed(
  () =>
    (filter.tradeTypes.length > 0 &&
      filter.tradeTypes.length < TRADE_TYPES.length) ||
    filter.minDepositJeonse != null ||
    filter.maxDepositJeonse != null ||
    filter.minSalePrice != null ||
    filter.maxSalePrice != null ||
    filter.minMonthlyRent != null ||
    filter.maxMonthlyRent != null ||
    filter.propertyTypes.length > 0 ||
    filter.floorPreference.length > 0 ||
    filter.minAreaM2 != null ||
    filter.maxAreaM2 != null,
);

const housingChipLabel = computed(() => {
  if (!housingChipOn.value) return '주거 조건';
  const parts = [];
  if (
    filter.tradeTypes.length > 0 &&
    filter.tradeTypes.length < TRADE_TYPES.length
  ) {
    parts.push(
      filter.tradeTypes.length === 1
        ? filter.tradeTypes[0]
        : `${filter.tradeTypes[0]} 외 ${filter.tradeTypes.length - 1}`,
    );
  }
  if (filter.maxDepositJeonse != null)
    parts.push(`${moneyLabel(filter.maxDepositJeonse)} 이하`);
  if (filter.maxSalePrice != null)
    parts.push(`매매가 ${moneyLabel(filter.maxSalePrice)} 이하`);
  if (filter.maxMonthlyRent != null)
    parts.push(`월세 ${filter.maxMonthlyRent}만 이하`);
  if (filter.propertyTypes.length > 0) {
    parts.push(
      filter.propertyTypes.length === 1
        ? filter.propertyTypes[0]
        : `${filter.propertyTypes[0]} 외 ${filter.propertyTypes.length - 1}`,
    );
  }
  if (filter.minAreaM2 != null || filter.maxAreaM2 != null)
    parts.push('면적 조건');
  if (filter.floorPreference.length > 0)
    parts.push(`층수 ${filter.floorPreference.length}개`);
  return parts.slice(0, 2).join(' · ') || '주거 조건';
});

const selectedTrades = computed(() =>
  TRADE_TYPES.filter((t) => draft.tradeTypes.includes(t)),
);

const depositJeonseLabel = computed(() => {
  const [lo, hi] = draft.depositJeonse;
  if (lo <= 0 && hi >= DEPOSIT_JEONSE.max) return '전체';
  return `${moneyLabel(lo)} ~ ${hi >= DEPOSIT_JEONSE.max ? '최대' : moneyLabel(hi)}`;
});

const salePriceLabel = computed(() => {
  const [lo, hi] = draft.salePrice;
  if (lo <= SALE_PRICE.min && hi >= SALE_PRICE.max) return '전체';
  return `${moneyLabel(lo)} ~ ${hi >= SALE_PRICE.max ? '최대' : moneyLabel(hi)}`;
});

const rentValueLabel = computed(() => {
  const [lo, hi] = draft.rent;
  if (lo <= 0 && hi >= MONTHLY_RENT.max) return '전체';
  return `${lo}만 ~ ${hi >= MONTHLY_RENT.max ? '최대' : `${hi}만`}`;
});

const areaLabel = computed(() => {
  const [lo, hi] = draft.areaRange;
  const loStr = lo <= 0 ? '최소' : `${lo}m² (${Math.floor(lo / PYEONG)}평)`;
  const hiStr =
    hi >= AREA.max ? '최대' : `${hi}m² (${Math.floor(hi / PYEONG)}평)`;
  return `${loStr} ~ ${hiStr}`;
});

const distanceLabel = computed(() => {
  const m = draft.distance;
  return m >= 1000 ? `${(m / 1000).toFixed(1)}km 이내` : `${m}m 이내`;
});

const totalCount = computed(() => listItems.value.length);

const infraChipOn = computed(
  () =>
    filter.desiredInfraCategories.length > 0 ||
    filter.desiredAmenityCategories.length > 0,
);

const infraChipLabel = computed(() => {
  if (!infraChipOn.value) return '인프라/편의';
  const allLabels = [
    ...filter.desiredInfraCategories.map(
      (k) => INFRA_CATEGORIES.find((c) => c.key === k)?.label ?? k,
    ),
    ...filter.desiredAmenityCategories.map(
      (k) => AMENITY_CATEGORIES.find((c) => c.key === k)?.label ?? k,
    ),
  ];
  return allLabels.length === 1
    ? allLabels[0]
    : `${allLabels[0]} 외 ${allLabels.length - 1}개`;
});

const sortLabel = computed(
  () => SORT_OPTIONS.find((o) => o.key === filter.sort)?.label ?? '추천순',
);

function openSheet(name) {
  if (name === 'commute') {
    draft.distance = filter.maxWorkplaceDistanceMeters ?? DEFAULT_DISTANCE;
    draft.workplace = filter.workplace;
    draft.hasCar = filter.hasCar;
  } else if (name === 'housing') {
    draft.tradeTypes = [...filter.tradeTypes];
    draft.depositJeonse = [
      filter.minDepositJeonse ?? 0,
      filter.maxDepositJeonse ?? DEPOSIT_JEONSE.max,
    ];
    draft.salePrice = [
      filter.minSalePrice ?? SALE_PRICE.min,
      filter.maxSalePrice ?? SALE_PRICE.max,
    ];
    draft.rent = [
      filter.minMonthlyRent ?? 0,
      filter.maxMonthlyRent ?? MONTHLY_RENT.max,
    ];
    draft.propertyTypes = [...filter.propertyTypes];
    draft.floorPreference = [...filter.floorPreference];
    draft.areaRange = [filter.minAreaM2 ?? 0, filter.maxAreaM2 ?? AREA.max];
  } else if (name === 'infra') {
    draft.infra = [...filter.desiredInfraCategories];
    draft.amenity = [...filter.desiredAmenityCategories];
  } else if (name === 'priority') {
    draft.priorities = priorityChips.value.map((p) => p.criterion);
  }
  openedSheet.value = name;
}

function closeSheet() {
  openedSheet.value = null;
}

function toggleIn(list, value) {
  const i = list.indexOf(value);
  if (i === -1) list.push(value);
  else list.splice(i, 1);
}

function toggleDraftTrade(t) {
  const i = draft.tradeTypes.indexOf(t);
  if (i !== -1) {
    draft.tradeTypes.splice(i, 1);
  } else {
    draft.tradeTypes.push(t);
  }
}

async function applyCommute() {
  filter.maxWorkplaceDistanceMeters = draft.distance;
  filter.workplace = draft.workplace;
  filter.hasCar = draft.hasCar;
  closeSheet();
  await commitFilter();
}

function resetCommute() {
  draft.distance = DEFAULT_DISTANCE;
  draft.workplace = null;
  draft.hasCar = false;
}

function goLocationSelect() {
  isLocationPickerOpen.value = true;
}

function selectWorkplace(location) {
  draft.workplace = location;
  isLocationPickerOpen.value = false;
}

async function applyHousing() {
  filter.tradeTypes = [...selectedTrades.value];

  if (draft.tradeTypes.includes('월세') || draft.tradeTypes.includes('전세')) {
    filter.minDepositJeonse = nullIfMin(draft.depositJeonse[0], 0);
    filter.maxDepositJeonse = nullIfMax(
      draft.depositJeonse[1],
      DEPOSIT_JEONSE.max,
    );
  } else {
    filter.minDepositJeonse = null;
    filter.maxDepositJeonse = null;
  }

  if (draft.tradeTypes.includes('매매')) {
    filter.minSalePrice = nullIfMin(draft.salePrice[0], SALE_PRICE.min);
    filter.maxSalePrice = nullIfMax(draft.salePrice[1], SALE_PRICE.max);
  } else {
    filter.minSalePrice = null;
    filter.maxSalePrice = null;
  }

  if (draft.tradeTypes.includes('월세')) {
    filter.minMonthlyRent = nullIfMin(draft.rent[0], 0);
    filter.maxMonthlyRent = nullIfMax(draft.rent[1], MONTHLY_RENT.max);
  } else {
    filter.minMonthlyRent = null;
    filter.maxMonthlyRent = null;
  }
  filter.propertyTypes = [...draft.propertyTypes];
  filter.floorPreference = [...draft.floorPreference];
  filter.minAreaM2 = draft.areaRange[0] > 0 ? draft.areaRange[0] : null;
  filter.maxAreaM2 = draft.areaRange[1] < AREA.max ? draft.areaRange[1] : null;
  closeSheet();
  await commitFilter();
}

function resetHousing() {
  draft.tradeTypes = [];
  draft.depositJeonse = [0, DEPOSIT_JEONSE.max];
  draft.salePrice = [SALE_PRICE.min, SALE_PRICE.max];
  draft.rent = [0, MONTHLY_RENT.max];
  draft.propertyTypes = [];
  draft.floorPreference = [];
  draft.areaRange = [0, AREA.max];
}

async function applyInfra() {
  filter.desiredInfraCategories = [...draft.infra];
  filter.desiredAmenityCategories = [...draft.amenity];
  closeSheet();
  await commitFilter();
}

function applySort(key) {
  filter.sort = key;
  closeSheet();
}

async function applyPriority() {
  priorityChips.value = draft.priorities.map((c, i) => ({
    criterion: c,
    priorityOrder: i + 1,
  }));
  closeSheet();
  await commitFilter();
}

function resetInfra() {
  draft.infra = [];
  draft.amenity = [];
}

function togglePriority(c) {
  const i = draft.priorities.indexOf(c);
  if (i !== -1) draft.priorities.splice(i, 1);
  else if (draft.priorities.length < MAX_PRIORITY_SELECTIONS)
    draft.priorities.push(c);
}

function priorityRank(c) {
  const i = draft.priorities.indexOf(c);
  return i === -1 ? null : i + 1;
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
  return Math.floor(areaM2 / PYEONG);
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

function scrollToTop() {
  const el = scrollArea.value?.scrollElement ?? scrollArea.value?.$el;
  el?.scrollTo?.({ top: 0 });
}

const MAP_HEIGHT = 250;
const DRAG_THRESHOLD = 5;

const sheetOffset = ref(0);
const isSheetDragging = ref(false);
const isSheetUp = computed(() => sheetOffset.value >= MAP_HEIGHT / 2);

let dragStartY = 0;
let dragStartOffset = 0;
let movedBy = 0;
let wasDragged = false;

function startSheetDrag(e) {
  isSheetDragging.value = true;
  dragStartY = e.clientY;
  dragStartOffset = sheetOffset.value;
  movedBy = 0;
  window.addEventListener('pointermove', onSheetDrag);
  window.addEventListener('pointerup', endSheetDrag);
}

function onSheetDrag(e) {
  const delta = dragStartY - e.clientY;
  movedBy = Math.max(movedBy, Math.abs(delta));
  if (movedBy < DRAG_THRESHOLD) return;
  sheetOffset.value = Math.min(
    MAP_HEIGHT,
    Math.max(0, dragStartOffset + delta),
  );
}

function endSheetDrag() {
  isSheetDragging.value = false;
  stopDragListeners();

  if (movedBy < DRAG_THRESHOLD) return;

  wasDragged = true;
  sheetOffset.value = sheetOffset.value > MAP_HEIGHT / 2 ? MAP_HEIGHT : 0;
}

function toggleSheet() {
  if (wasDragged) {
    wasDragged = false;
    return;
  }
  sheetOffset.value = sheetOffset.value > 0 ? 0 : MAP_HEIGHT;
}

function stopDragListeners() {
  window.removeEventListener('pointermove', onSheetDrag);
  window.removeEventListener('pointerup', endSheetDrag);
}

onBeforeUnmount(stopDragListeners);

watch(selectedBuildingId, (id) => {
  closePanel();
  if (id && selectionSource.value === 'pin') scrollToTop();
});

watch(filter, scrollToTop);
</script>

<style scoped>
.property-list {
  height: calc(100dvh - 52px - 60px);
  display: flex;
  flex-direction: column;
  background: var(--bg);
  overflow: hidden;
}

.sheet-top {
  position: relative;
  z-index: 1;
  flex-shrink: 0;
  background: var(--bg);
  border-radius: 16px 16px 0 0;
  box-shadow: 0 -6px 16px rgba(0, 0, 0, 0.08);
  transition: margin-top 0.22s ease;
}

.sheet-top.dragging {
  transition: none;
}

.sheet-handle {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  width: 100%;
  min-height: 40px;
  padding: 8px 0 4px;
  border: 0;
  background: transparent;
  cursor: pointer;
  touch-action: none;
}

.handle-bar {
  width: 40px;
  height: 4px;
  border-radius: 2px;
  background: #d8d2c4;
}

.handle-chevron {
  display: none;
  transition: transform 0.22s ease;
}

.handle-chevron.down {
  transform: rotate(180deg);
}

@media (hover: hover) and (pointer: fine) {
  .sheet-handle {
    min-height: 30px;
  }

  .handle-bar {
    display: none;
  }

  .handle-chevron {
    display: block;
  }
}

.scroll-area {
  position: relative;
  z-index: 1;
  background: var(--bg);
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

.map-area {
  position: relative;
  height: 250px;
  flex-shrink: 0;
  z-index: 0;
}

.cards {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 0px 16px 16px;
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
  overflow: hidden;
  cursor: pointer;
}

.card.selected {
  background: #fff6dc;
  border: 2px solid #ffbc00;
  padding: 11px;
}

.thumb {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 62px;
  height: 62px;
  flex-shrink: 0;
  background: #f5efdb;
  border-radius: 10px;
}

.medal {
  position: absolute;
  top: -9px;
  left: -9px;
  z-index: 1;
  width: 32px;
  height: 32px;
  pointer-events: none;
  filter: drop-shadow(0 1px 2px rgba(0,0,0,0.25));
}

.card.selected .thumb {
  background: #fff;
}

.card-texts {
  flex: 1;
  min-width: 0;
}

.card-title-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.score-chip {
  margin-left: auto;
  flex-shrink: 0;
  padding: 2px 8px;
  border-radius: 100px;
  background: #f0eeea;
  font-size: 10.5px;
  font-weight: 800;
  color: #60584c;
  white-space: nowrap;
}

.card-title {
  min-width: 0;
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
  position: relative;
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

.card-go::after {
  content: '';
  position: absolute;
  top: -35px;
  right: -12px;
  bottom: -35px;
  left: -14px;
}

.card-go.on {
  background: #ffbc00;
  border-color: #ffbc00;
}

.map-overlay {
  position: absolute;
  top: 12px;
  right: 12px;
  bottom: 12px;
  z-index: 100;
  pointer-events: none;
}

.reset-btn {
  position: absolute;
  bottom: 12px;
  right: 12px;
  z-index: 90;
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
  padding: 0px 16px 0;
  overflow-x: auto;
}

.fchip {
  flex-shrink: 0;
  padding: 8px 14px;
  border: 1.5px solid #e9e7e2;
  border-radius: 100px;
  background: #fff;
  font-size: 12.5px;
  color: #33302a;
  white-space: nowrap;
  cursor: pointer;
}

.fchip.on {
  border-color: #ffdd80;
  background: #fff6dc;
  font-weight: 700;
}

.result-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 16px;
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
  gap: 10px;
  margin: 0px 16px 16px;
  padding: 9px 16px;
  border-radius: 100px;
  background: #fdf7e6;
  border: 1.5px solid #ffdd80;
  overflow-x: auto;
  cursor: pointer;
  height: 42px;
}

.filter-chips,
.priority-row {
  scrollbar-width: none;
}

.filter-chips::-webkit-scrollbar,
.priority-row::-webkit-scrollbar {
  display: none;
}

.prow-icon {
  flex-shrink: 0;
}

.pchip {
  height: 24px;
  display: flex;
  align-items: center;
  gap: 7px;
  flex-shrink: 0;
  padding: 6px 15px 6px 6px;
  border-radius: 100px;
  background: #fff;
  font-size: 11.5px;
  font-weight: 700;
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
  background: #f0c33c;
  font-size: 9px;
  font-weight: 800;
  color: #545045;
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

.opt-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.opt {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 9px 15px;
  border: 1px solid #e9e7e2;
  border-radius: 100px;
  background: #fff;
  font-size: 13px;
  color: #33302a;
  cursor: pointer;
}

.opt.on {
  border-color: #ffbc00;
  background: #fff6dc;
  font-weight: 700;
}

.field {
  padding: 4px 0 18px;
}

.field-gap-top {
  margin-top: 10px;
}

.field-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 10px;
}

.field-head .field-name {
  margin-bottom: 0;
}

.range-card {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 16px 14px;
  border-radius: 14px;
  background: var(--bg);
}

.range-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.range-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13.5px;
  font-weight: 800;
  color: #33302a;
}

.range-value {
  font-size: 12.5px;
  color: var(--kb-gray);
  font-weight: 500;
}

.field-name {
  font-size: 15px;
  font-weight: 800;
  color: #33302a;
  margin-bottom: 10px;
}

.field-caption {
  margin-top: -4px;
  margin-bottom: 10px;
  font-size: 11.5px;
  color: var(--kb-silver);
}

.location-input {
  width: 100%;
  padding: 11px 12px;
  border: 1px solid #e9e7e2;
  border-radius: 10px;
  background: #fff;
  font-size: 14px;
  color: #33302a;
  cursor: pointer;
  box-sizing: border-box;
}

.location-input::placeholder {
  color: #b4b0a8;
}

.check-row {
  display: flex;
  gap: 28px;
  margin-top: 2px;
}

.check-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13.5px;
  cursor: pointer;
}

.checkbox {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 6px;
  border: 1.5px solid #d8d5cf;
  background: #fff;
  flex-shrink: 0;
}

.checkbox svg {
  opacity: 0;
}

.checkbox.on {
  background: var(--kb-yellow);
  border-color: var(--kb-yellow);
}

.checkbox.on svg {
  opacity: 1;
}

.sheet-note {
  font-size: 11.5px;
  color: #8a8d8f;
  margin-bottom: 12px;
}

.sheet-actions {
  display: flex;
  gap: 8px;
  padding-top: 18px;
}

.btn-ghost {
  flex-shrink: 0;
  padding: 13px 18px;
  border: 1px solid #e9e7e2;
  border-radius: 12px;
  background: #fff;
  font-size: 13.5px;
  color: #60584c;
  cursor: pointer;
}

.btn-primary {
  flex: 1;
  padding: 13px;
  border: none;
  border-radius: 12px;
  background: #ffdd80;
  font-size: 14px;
  font-weight: 700;
  color: #33302a;
  cursor: pointer;
}

.btn-primary:disabled {
  background: #eceae5;
  color: #b4b0a8;
  cursor: default;
}

.sort-list {
  display: flex;
  flex-direction: column;
}

.sort-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 14px 2px;
  border: none;
  background: none;
  font-size: 14px;
  color: #33302a;
  cursor: pointer;
}

.sort-item.on {
  font-weight: 700;
  color: #fe7b00;
}

.priority-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.priority-card {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 12px 14px;
  text-align: left;
  border: 1px solid var(--border);
  border-radius: 14px;
  background: var(--white);
  cursor: pointer;
}

.priority-card.on {
  background: var(--yellow-tint);
  border: 2px solid #ffdd80;
  padding: 11px 13px;
}

.p-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 100px;
  background: rgba(255, 188, 0, 0.14);
  flex-shrink: 0;
}

.p-texts {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
  min-width: 0;
}

.p-title {
  font-size: 14px;
  font-weight: 800;
}

.p-sub {
  font-size: 11.5px;
  color: var(--kb-silver);
}

.p-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #ffdd80;
  font-size: 12.5px;
  font-weight: 800;
  flex-shrink: 0;
}

/* loading */
.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 50px 20px;
}

.spinner {
  width: 28px;
  height: 28px;
  border: 3px solid #eceae5;
  border-top-color: var(--kb-yellow);
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.loading-text {
  font-size: 12.5px;
  color: var(--kb-silver);
}

@media (prefers-reduced-motion: reduce) {
  .spinner {
    animation-duration: 2s;
  }
}

.thumb-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 10px;
}
</style>
