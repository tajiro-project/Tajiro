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
        v-if="filter.sort === 'recommend'"
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
          {{ p.criterion }}
        </span>
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
      <div class="range-group">
        <p class="range-title">
          {{ depositTitle }}
          <span class="range-value">{{ depositValueLabel }}</span>
        </p>
        <DualSlider
          v-model="draft.deposit"
          :min="0"
          :max="depositMax"
          :step="depositStep"
          :marks="depositMarks"
        />
      </div>

      <div v-if="draft.tradeTypes.includes('월세')" class="range-group">
        <p class="range-title">
          월세
          <span class="range-value">{{ rentValueLabel }}</span>
        </p>
        <DualSlider
          v-model="draft.rent"
          :min="0"
          :max="200"
          :step="5"
          :marks="['0', '40', '80', '120', '160', '최대']"
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
        :min="0"
        :max="AREA_MAX_M2"
        :step="5"
        :marks="['0', '50m²', '100m²', '150m²', '200m²']"
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
      <button class="btn-primary" @click="applyHousing">이 조건으로 적용</button>
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
        :min="500"
        :max="DISTANCE_MAX"
        :step="500"
        :marks="['500m', '2.5km', '5km', '7.5km', '10km']"
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
      <button class="btn-primary" @click="applyCommute">이 조건으로 적용</button>
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
      <li v-for="o in SORT_OPTIONS" :key="o.key">
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
  <!-- 08-2 · 가치관 우선순위 -->
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
          <span class="p-title">{{ opt.criterion }}</span>
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
import PageHeader from '@/components/PageHeader.vue';
import AppTabBar from '@/components/AppTabBar.vue';
import KakaoMap from '@/components/KakaoMap.vue';
import BottomSheet from '@/components/BottomSheet.vue';
import DualSlider from '@/components/DualSlider.vue';
import SingleSlider from '@/components/SingleSlider.vue';
import KakaoLocation from '@/components/KakaoLocation.vue';

import { computed, ref, reactive, watch } from 'vue';
import { useRouter } from 'vue-router';
import { infraColor } from '@/constants/infraIcons';

// mock data
const RAW_PROPERTIES = [
  // B01 용운마젤란21 — 3건
  {
    propertyId: 1,
    buildingId: 1,
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
    propertyId: 2,
    buildingId: 1,
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
    propertyId: 3,
    buildingId: 1,
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
    propertyId: 4,
    buildingId: 2,
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
    propertyId: 5,
    buildingId: 2,
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
    propertyId: 6,
    buildingId: 3,
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
    propertyId: 7,
    buildingId: 4,
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
    propertyId: 8,
    buildingId: 4,
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
    propertyId: 9,
    buildingId: 4,
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
    propertyId: 10,
    buildingId: 4,
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
    propertyId: 11,
    buildingId: 5,
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
    propertyId: 12,
    buildingId: 6,
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
    propertyId: 13,
    buildingId: 6,
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
    buildingId: 1,
    category: 'SUBWAY',
    name: '판암역',
    latitude: 36.3285,
    longitude: 127.4541,
  },
  {
    buildingId: 1,
    category: 'HOSPITAL',
    name: '대전한국병원',
    latitude: 36.3281,
    longitude: 127.4553,
  },
  {
    buildingId: 1,
    category: 'CAFE',
    name: '스타벅스 대전대점',
    latitude: 36.3263,
    longitude: 127.4553,
  },
  {
    buildingId: 1,
    category: 'SCHOOL',
    name: '가양초등학교',
    latitude: 36.3259,
    longitude: 127.4541,
  },
  {
    buildingId: 1,
    category: 'PARK',
    name: '용운근린공원',
    latitude: 36.3263,
    longitude: 127.4529,
  },

  // B02
  {
    buildingId: 2,
    category: 'BUS_TERMINAL',
    name: '대전복합터미널',
    latitude: 36.3318,
    longitude: 127.4589,
  },
  {
    buildingId: 2,
    category: 'PHARMACY',
    name: '온누리약국',
    latitude: 36.3314,
    longitude: 127.4601,
  },
  {
    buildingId: 2,
    category: 'MART',
    name: '홈플러스 가오점',
    latitude: 36.3296,
    longitude: 127.4601,
  },
  {
    buildingId: 2,
    category: 'ACADEMY',
    name: '이룸수학학원',
    latitude: 36.3292,
    longitude: 127.4589,
  },
  {
    buildingId: 2,
    category: 'CULTURE',
    name: '동구문화체육센터',
    latitude: 36.3296,
    longitude: 127.4577,
  },

  // B03
  {
    buildingId: 3,
    category: 'TRAIN',
    name: '대전역',
    latitude: 36.3264,
    longitude: 127.4608,
  },
  {
    buildingId: 3,
    category: 'CONVENIENCE',
    name: 'GS25 용운점',
    latitude: 36.326,
    longitude: 127.462,
  },
  {
    buildingId: 3,
    category: 'KINDERGARTEN',
    name: '햇살유치원',
    latitude: 36.3242,
    longitude: 127.462,
  },
  {
    buildingId: 3,
    category: 'SPORTS',
    name: '동구실내체육관',
    latitude: 36.3238,
    longitude: 127.4608,
  },

  // B04
  {
    buildingId: 4,
    category: 'PARKING',
    name: '용운공영주차장',
    latitude: 36.3343,
    longitude: 127.453,
  },
  {
    buildingId: 4,
    category: 'FOOD',
    name: '용운칼국수',
    latitude: 36.3339,
    longitude: 127.4542,
  },
  {
    buildingId: 4,
    category: 'LIBRARY',
    name: '한밭도서관',
    latitude: 36.3321,
    longitude: 127.4542,
  },
  {
    buildingId: 4,
    category: 'SWIMMING',
    name: '용운국제수영장',
    latitude: 36.3317,
    longitude: 127.453,
  },

  // B05
  {
    buildingId: 5,
    category: 'GAS',
    name: 'GS칼텍스 용운주유소',
    latitude: 36.3253,
    longitude: 127.452,
  },
  {
    buildingId: 5,
    category: 'BANK',
    name: '하나은행 대전대점',
    latitude: 36.3249,
    longitude: 127.4532,
  },
  {
    buildingId: 5,
    category: 'GOV_OFFICE',
    name: '용운동 행정복지센터',
    latitude: 36.3231,
    longitude: 127.4532,
  },
  {
    buildingId: 5,
    category: 'POST_OFFICE',
    name: '대전용운우체국',
    latitude: 36.3227,
    longitude: 127.452,
  },

  // B06
  {
    buildingId: 6,
    category: 'PUBLIC',
    name: '대전동구청',
    latitude: 36.3333,
    longitude: 127.464,
  },
  {
    buildingId: 6,
    category: 'POLICE',
    name: '대전동부경찰서',
    latitude: 36.3329,
    longitude: 127.4652,
  },
  {
    buildingId: 6,
    category: 'FIRE',
    name: '동부소방서',
    latitude: 36.3311,
    longitude: 127.4652,
  },
  {
    buildingId: 6,
    category: 'FIRE',
    name: '동부119안전센터',
    latitude: 36.335,
    longitude: 127.468,
  },
];

const router = useRouter();
const center = { lat: 36.3366, lng: 127.459 };

const scrollArea = ref(null);
const selectedBuildingId = ref(null);
const selectedPropertyId = ref(null);
const selectionSource = ref(null);
const pinnedDot = ref(null);
const hoveredDot = ref(null);

const activeDot = computed(() => hoveredDot.value ?? pinnedDot.value);
const items = ref(RAW_PROPERTIES);

const TRADE_TYPES = ['월세', '전세', '매매'];
const PROPERTY_TYPES = ['원룸', '오피스텔', '아파트'];
const FLOOR_OPTIONS = ['지하/반지하', '1층', '2층 이상', '옥탑'];

const PRICE_LIMITS = {
  월세: { deposit: 5000, rent: 200 },
  전세: { deposit: 50000 },
  매매: { deposit: 100000 },
};

const DEPOSIT_MARKS = {
  5000: ['0', '1,000', '2,000', '3,000', '4,000', '최대'],
  50000: ['0', '1억', '2억', '3억', '4억', '최대'],
  100000: ['0', '2억', '4억', '6억', '8억', '최대'],
};

const DISTANCE_MAX = 10000;
const DEFAULT_DISTANCE = 2000;
const PYEONG = 3.3058;
const AREA_MAX_M2 = 200;

const INFRA_CATEGORIES = [
  { key: 'SUBWAY', label: '지하철' },
  { key: 'BUS_TERMINAL', label: '버스터미널' },
  { key: 'TRAIN', label: '기차역' },
  { key: 'HOSPITAL', label: '병원' },
  { key: 'PHARMACY', label: '약국' },
  { key: 'SCHOOL', label: '학교' },
  { key: 'KINDERGARTEN', label: '유치원' },
  { key: 'ACADEMY', label: '학원' },
  { key: 'LIBRARY', label: '도서관' },
  { key: 'PARK', label: '공원' },
  { key: 'POLICE', label: '경찰서' },
  { key: 'FIRE', label: '소방서' },
  { key: 'GOV_OFFICE', label: '행정복지센터' },
  { key: 'PUBLIC', label: '관공서' },
  { key: 'POST_OFFICE', label: '우체국' },
  { key: 'BANK', label: '은행' },
];

const AMENITY_CATEGORIES = [
  { key: 'CONVENIENCE', label: '편의점' },
  { key: 'MART', label: '마트' },
  { key: 'CAFE', label: '카페' },
  { key: 'FOOD', label: '음식점' },
  { key: 'CULTURE', label: '문화시설' },
  { key: 'SPORTS', label: '체육시설' },
  { key: 'SWIMMING', label: '수영장' },
  { key: 'PARKING', label: '주차장' },
  { key: 'GAS', label: '주유소' },
];

const filter = reactive({
  tradeTypes: [...TRADE_TYPES],
  propertyTypes: [],
  minDeposit: null,
  maxDeposit: null,
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

const CONVERSION_RATE = 0.053; // 전월세 전활율(전국)
function monthlyCost(p) {
  return p.monthlyRent + (p.deposit * CONVERSION_RATE) / 12 + p.maintenanceFee;
}

const SORT_SPECS = {
  recommend: { value: (p) => p.recommendScore, dir: 'desc' },
  distance: { value: (p) => p.workplaceDistanceMeters, dir: 'asc' },
  price: { value: monthlyCost, dir: 'asc' },
  infra: { value: (p) => p.desiredInfraCount, dir: 'desc' },
  amenity: { value: (p) => p.desiredAmenityCount, dir: 'desc' },
  area: { value: (p) => p.areaM2, dir: 'desc' },
};

const priorityChips = ref([
  { criterion: '직주근접', priorityOrder: 1 },
  { criterion: '가성비', priorityOrder: 2 },
  { criterion: '편의시설', priorityOrder: 3 },
]);

const PRIORITY_OPTIONS = [
  {
    criterion: '직주근접',
    sub: '출퇴근 시간이 가장 중요해요',
    icon: '<svg width="20" height="20" viewBox="0 0 20 20" fill="none"><circle cx="10" cy="10" r="7.2" stroke="#545045" stroke-width="1.5"/><path d="M10 6v4l2.6 1.6" stroke="#545045" stroke-width="1.5" stroke-linecap="round"/></svg>',
  },
  {
    criterion: '가성비',
    sub: '월세·관리비 등 주거비 절약',
    icon: '<svg width="20" height="20" viewBox="0 0 20 20" fill="none"><circle cx="10" cy="10" r="7.2" stroke="#545045" stroke-width="1.5"/><path d="M7 8h6M7 10.5h6M9 6.5l2 7" stroke="#545045" stroke-width="1.3" stroke-linecap="round"/></svg>',
  },
  {
    criterion: '편의시설',
    sub: '카페, 헬스장 등 편의시설',
    icon: '<svg width="20" height="20" viewBox="0 0 20 20" fill="none"><rect x="3.5" y="6" width="13" height="10" rx="1.5" stroke="#545045" stroke-width="1.5"/><path d="M7 6V4.5A1.5 1.5 0 018.5 3h3A1.5 1.5 0 0113 4.5V6" stroke="#545045" stroke-width="1.5"/></svg>',
  },
  {
    criterion: '인프라',
    sub: '교육·의료·교통시설',
    icon: '<svg width="20" height="20" viewBox="0 0 20 20" fill="none"><rect x="4" y="3.5" width="8" height="13" stroke="#545045" stroke-width="1.5"/><path d="M12 8h4v8.5h-4M6.5 7h1.2M6.5 10h1.2M6.5 13h1.2M9.5 7h1.2M9.5 10h1.2M9.5 13h1.2" stroke="#545045" stroke-width="1.2"/></svg>',
  },
  {
    criterion: '매물 면적',
    sub: '매물의 면적이 가장 중요해요',
    icon: '<svg width="20" height="20" viewBox="0 0 20 20" fill="none"><rect x="3.5" y="3.5" width="13" height="13" rx="1.5" stroke="#545045" stroke-width="1.5"/><path d="M6.5 12.5v-5h5" stroke="#545045" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/><path d="M13.5 7.5v5h-5" stroke="#545045" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/></svg>',
  },
];

const openedSheet = ref(null);
const isLocationPickerOpen = ref(false);

const draft = reactive({
  tradeTypes: [],
  deposit: [0, 5000],
  rent: [0, 200],
  propertyTypes: [],
  floorPreference: [],
  areaRange: [0, AREA_MAX_M2],
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

  return [...infra];
});

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
  if (m !== DEFAULT_DISTANCE) parts.push(m >= 1000 ? `${(m / 1000).toFixed(1)}km 이내` : `${m}m 이내`);
  if (filter.hasCar) parts.push('자차 O');
  return parts.slice(0, 2).join(' · ') || '이주/통근';
});

const housingChipOn = computed(
  () =>
    filter.tradeTypes.length < TRADE_TYPES.length ||
    filter.minDeposit != null ||
    filter.maxDeposit != null ||
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
  if (filter.tradeTypes.length < TRADE_TYPES.length) {
    parts.push(
      filter.tradeTypes.length === 1
        ? filter.tradeTypes[0]
        : `${filter.tradeTypes[0]} 외 ${filter.tradeTypes.length - 1}`,
    );
  }
  if (filter.maxDeposit != null) parts.push(`${moneyLabel(filter.maxDeposit)} 이하`);
  if (filter.maxMonthlyRent != null) parts.push(`월세 ${filter.maxMonthlyRent}만 이하`);
  if (filter.propertyTypes.length > 0) {
    parts.push(
      filter.propertyTypes.length === 1
        ? filter.propertyTypes[0]
        : `${filter.propertyTypes[0]} 외 ${filter.propertyTypes.length - 1}`,
    );
  }
  if (filter.minAreaM2 != null || filter.maxAreaM2 != null) parts.push('면적 조건');
  if (filter.floorPreference.length > 0) parts.push(`층수 ${filter.floorPreference.length}개`);
  return parts.slice(0, 2).join(' · ') || '주거 조건';
});

const selectedTrades = computed(() =>
  TRADE_TYPES.filter((t) => draft.tradeTypes.includes(t)),
);

const depositTitle = computed(() =>
  selectedTrades.value
    .map((t) => (t === '매매' ? '매매가' : t === '전세' ? '전세금' : '보증금'))
    .join(' · '),
);

const depositMax = computed(() => depositMaxOf(draft.tradeTypes));
const depositStep = computed(() => (depositMax.value <= 5000 ? 100 : 1000));
const depositMarks = computed(() => DEPOSIT_MARKS[depositMax.value]);

const depositValueLabel = computed(() => {
  const [lo, hi] = draft.deposit;
  return `${moneyLabel(lo)} ~ ${hi >= depositMax.value ? '최대' : moneyLabel(hi)}`;
});

const rentValueLabel = computed(() => {
  const [lo, hi] = draft.rent;
  const max = PRICE_LIMITS['월세'].rent;
  return `${lo}만 ~ ${hi >= max ? '최대' : `${hi}만`}`;
});

const areaLabel = computed(() => {
  const [lo, hi] = draft.areaRange;
  const loStr = lo <= 0 ? '최소' : `${lo}m² (${Math.floor(lo / PYEONG)}평)`;
  const hiStr = hi >= AREA_MAX_M2 ? '최대' : `${hi}m² (${Math.floor(hi / PYEONG)}평)`;
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
    draft.deposit = [
      filter.minDeposit ?? 0,
      filter.maxDeposit ?? depositMaxOf(draft.tradeTypes),
    ];
    draft.rent = [filter.minMonthlyRent ?? 0, filter.maxMonthlyRent ?? 200];
    draft.propertyTypes = [...filter.propertyTypes];
    draft.floorPreference = [...filter.floorPreference];
    draft.areaRange = [filter.minAreaM2 ?? 0, filter.maxAreaM2 ?? AREA_MAX_M2];
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

const nullIfMin = (v, min) => (v <= min ? null : v);
const nullIfMax = (v, max) => (v >= max ? null : v);


function depositMaxOf(types) {
  return Math.max(...types.map((t) => PRICE_LIMITS[t].deposit));
}

function toggleDraftTrade(t) {
  const before = depositMaxOf(draft.tradeTypes);
  const i = draft.tradeTypes.indexOf(t);

  if (i !== -1) {
    if (draft.tradeTypes.length === 1) return;
    draft.tradeTypes.splice(i, 1);
  } else {
    draft.tradeTypes.push(t);
  }

  const after = depositMaxOf(draft.tradeTypes);
  if (after !== before) {
    const [lo, hi] = draft.deposit;
    draft.deposit = [
      Math.min(lo, after),
      hi >= before ? after : Math.min(hi, after),
    ];
  }
}

function applyCommute() {
  filter.maxWorkplaceDistanceMeters = draft.distance;
  filter.workplace = draft.workplace;
  filter.hasCar = draft.hasCar;
  closeSheet();
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

function applyHousing() {
  const max = depositMaxOf(draft.tradeTypes);
  filter.tradeTypes = [...selectedTrades.value];
  filter.minDeposit = nullIfMin(draft.deposit[0], 0);
  filter.maxDeposit = nullIfMax(draft.deposit[1], max);

  if (draft.tradeTypes.includes('월세')) {
    filter.minMonthlyRent = nullIfMin(draft.rent[0], 0);
    filter.maxMonthlyRent = nullIfMax(draft.rent[1], PRICE_LIMITS['월세'].rent);
  } else {
    filter.minMonthlyRent = null;
    filter.maxMonthlyRent = null;
  }
  filter.propertyTypes = [...draft.propertyTypes];
  filter.floorPreference = [...draft.floorPreference];
  filter.minAreaM2 = draft.areaRange[0] > 0 ? draft.areaRange[0] : null;
  filter.maxAreaM2 = draft.areaRange[1] < AREA_MAX_M2 ? draft.areaRange[1] : null;
  closeSheet();
}

function resetHousing() {
  draft.tradeTypes = [...TRADE_TYPES];
  draft.deposit = [0, depositMaxOf(TRADE_TYPES)];
  draft.rent = [0, PRICE_LIMITS['월세'].rent];
  draft.propertyTypes = [];
  draft.floorPreference = [];
  draft.areaRange = [0, AREA_MAX_M2];
}

function applyInfra() {
  filter.desiredInfraCategories = [...draft.infra];
  filter.desiredAmenityCategories = [...draft.amenity];
  closeSheet();
}

function applySort(key) {
  filter.sort = key;
  closeSheet();
}

function applyPriority() {
  priorityChips.value = draft.priorities.map((c, i) => ({
    criterion: c,
    priorityOrder: i + 1,
  }));
  closeSheet();
}

function resetInfra() {
  draft.infra = [];
  draft.amenity = [];
}

function togglePriority(c) {
  const i = draft.priorities.indexOf(c);
  if (i !== -1) draft.priorities.splice(i, 1);
  else if (draft.priorities.length < 3) draft.priorities.push(c);
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
</style>
