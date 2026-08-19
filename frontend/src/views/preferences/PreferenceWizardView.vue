<template>
  <div class="pref-wizard">
    <p v-if="isDemoMode" class="demo-banner">
      예시 화면이에요 · 실제 내 가치관 설정이 아니에요
    </p>
    <button
      v-if="isDemoMode"
      class="demo-corner-exit"
      type="button"
      @click="goToMypage"
    >
      마이페이지로
    </button>
    <!-- STEP 표시 + 진행 바 -->
    <div class="step-head" data-tour="preferences-wizard">
      <p class="step-line">
        <span class="step-no"
          >STEP {{ step }} / {{ PREFERENCE_STEP_COUNT }}</span
        >
        <span class="dot">·</span>
        <span class="step-label">{{ PREFERENCE_STEP_LABELS[step - 1] }}</span>
      </p>
      <div class="progress">
        <div
          v-for="n in PREFERENCE_STEP_COUNT"
          :key="n"
          class="seg"
          :class="{ on: n <= step }"
        />
      </div>
    </div>

    <!-- STEP 1 — 이주·통근 정보 -->
    <p v-if="isLoading" class="status-message" role="status">
      저장된 설정을 불러오는 중입니다.
    </p>
    <p v-else-if="errorMessage" class="status-message error" role="alert">
      {{ errorMessage }}
    </p>

    <!-- TODO: 바깥 .content와 안쪽 simplebar .content 중첩으로 본문 높이가 0이 될 수 있음. 담당자 확인 필요. -->
    <!-- <div
      v-if="step === 1"
      class="content"
    > -->
    <simplebar v-if="step === 1" class="content">
      <div class="field">
        <label class="section-title"
          >선호 위치(직장 / 학교 등)<span class="req">*</span></label
        >
        <input
          class="field-input"
          type="text"
          readonly
          :value="pref.workplace?.name ?? ''"
          placeholder="예) 창원시 성산구 상남동"
          @click="goLocationSelect"
          @keydown.enter.prevent="goLocationSelect"
        />
      </div>
      <div class="field">
        <label class="section-title">
          희망 통학·통근 거리
          <span class="range-value">{{ commuteDistanceLabel }}</span>
        </label>
        <SingleSlider
          v-model="pref.maxCommuteDistanceMeters"
          :min="PREFERENCE_SLIDER_CONFIG.COMMUTE_DISTANCE.min"
          :max="PREFERENCE_SLIDER_CONFIG.COMMUTE_DISTANCE.max"
          :step="PREFERENCE_SLIDER_CONFIG.COMMUTE_DISTANCE.step"
          :marks="PREFERENCE_SLIDER_CONFIG.COMMUTE_DISTANCE.marks"
          aria-label="희망 통학·통근 거리"
        />
      </div>
      <div class="field">
        <label class="section-title">자차 보유 여부</label>
        <div class="check-row">
          <label class="check-item" @click="pref.hasCar = true">
            <span class="checkbox" :class="{ on: pref.hasCar }">
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
          <label class="check-item" @click="pref.hasCar = false">
            <span class="checkbox" :class="{ on: !pref.hasCar }">
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
    </simplebar>

    <!-- STEP 2 — 희망 주거 조건 -->
    <simplebar v-else-if="step === 2" class="content">
      <div class="group">
        <p class="section-title">매물 유형</p>
        <div class="chips">
          <button
            v-for="t in HOUSING_OPTIONS"
            :key="t"
            class="chip"
            :class="{ on: pref.housingTypes.includes(t) }"
            @click="toggle(pref.housingTypes, t)"
          >
            {{ t }}
          </button>
        </div>
      </div>
      <div class="group">
        <p class="section-title">희망 주거 형태</p>
        <div class="chips">
          <button
            v-for="t in TRADE_OPTIONS"
            :key="t"
            class="chip"
            :class="{ on: pref.tradeTypes.includes(t) }"
            @click="toggle(pref.tradeTypes, t)"
          >
            {{ t }}
          </button>
        </div>
      </div>

      <div v-if="pref.tradeTypes.length" class="range-card">
        <div
          v-if="
            pref.tradeTypes.includes('월세') || pref.tradeTypes.includes('전세')
          "
          class="range-group"
        >
          <p class="range-title">
            보증금/전세금
            <span class="range-value">{{ depositJeonseLabel }}</span>
          </p>
          <DualSlider
            v-model="pref.depositJeonseRange"
            :min="PREFERENCE_SLIDER_CONFIG.DEPOSIT_JEONSE.min"
            :max="PREFERENCE_SLIDER_CONFIG.DEPOSIT_JEONSE.max"
            :step="PREFERENCE_SLIDER_CONFIG.DEPOSIT_JEONSE.step"
            :marks="PREFERENCE_SLIDER_CONFIG.DEPOSIT_JEONSE.marks"
          />
        </div>

        <div v-if="pref.tradeTypes.includes('월세')" class="range-group">
          <p class="range-title">
            월세 <span class="range-value">{{ monthlyRentLabel }}</span>
          </p>
          <DualSlider
            v-model="pref.monthlyRentRange"
            :min="PREFERENCE_SLIDER_CONFIG.MONTHLY_RENT.min"
            :max="PREFERENCE_SLIDER_CONFIG.MONTHLY_RENT.max"
            :step="PREFERENCE_SLIDER_CONFIG.MONTHLY_RENT.step"
            :marks="PREFERENCE_SLIDER_CONFIG.MONTHLY_RENT.marks"
          />
        </div>

        <div v-if="pref.tradeTypes.includes('매매')" class="range-group">
          <p class="range-title">
            매매가 <span class="range-value">{{ salePriceLabel }}</span>
          </p>
          <DualSlider
            v-model="pref.salePriceRange"
            :min="PREFERENCE_SLIDER_CONFIG.SALE_PRICE.min"
            :max="PREFERENCE_SLIDER_CONFIG.SALE_PRICE.max"
            :step="PREFERENCE_SLIDER_CONFIG.SALE_PRICE.step"
            :marks="PREFERENCE_SLIDER_CONFIG.SALE_PRICE.marks"
          />
        </div>
      </div>

      <div class="area-group">
        <p class="range-title">
          매물 면적
          <span class="range-value">{{ areaLabel }}</span>
        </p>
        <DualSlider
          v-model="pref.areaRange"
          :min="AREA_MIN_M2"
          :max="AREA_MAX_M2"
          :step="AREA_STEP_M2"
          :marks="AREA_MARKS"
        />
      </div>

      <div class="group">
        <p class="section-title">매물 층수</p>
        <div class="chips">
          <button
            v-for="f in FLOOR_OPTIONS"
            :key="f"
            class="chip"
            :class="{ on: pref.floorPreference.includes(f) }"
            @click="toggle(pref.floorPreference, f)"
          >
            {{ f }}
          </button>
        </div>
      </div>
    </simplebar>

    <!-- STEP 3 — 인프라·편의시설 -->
    <simplebar v-else-if="step === 3" class="content">
      <div class="group">
        <p class="section-title">희망 인프라</p>
        <p class="caption">반경 2km 이내만 표시</p>
        <div class="chips wrap">
          <button
            v-for="category in INFRA_CATEGORIES"
            :key="category.key"
            class="chip"
            :class="{
              on: pref.desiredInfraCategories.includes(category.key),
            }"
            @click="toggle(pref.desiredInfraCategories, category.key)"
          >
            {{ category.label }}
          </button>
        </div>
      </div>
      <div class="group">
        <p class="section-title">희망 편의시설</p>
        <p class="caption">반경 2km 이내만 표시</p>
        <div class="chips wrap">
          <button
            v-for="category in AMENITY_CATEGORIES"
            :key="category.key"
            class="chip"
            :class="{
              on: pref.desiredAmenityCategories.includes(category.key),
            }"
            @click="toggle(pref.desiredAmenityCategories, category.key)"
          >
            {{ category.label }}
          </button>
        </div>
      </div>
    </simplebar>

    <!-- STEP 4 — 가치관 우선순위 (0~3개 선택) -->
    <simplebar v-else class="content">
      <div class="priority-head">
        <p class="section-title big">주거 가치관 우선순위</p>
        <p class="caption">
          최대 3개까지 선택하세요. 선택하지 않으면 모두 동일하게 반영돼요.
        </p>
      </div>
      <div class="priority-list">
        <button
          v-for="opt in PRIORITY_OPTIONS"
          :key="opt.criterion"
          class="priority-card"
          :class="{ on: priorityOrder(opt.criterion) }"
          @click="onTogglePriority(opt.criterion)"
        >
          <span class="p-icon" v-html="opt.icon" />
          <span class="p-texts">
            <span class="p-title">{{ opt.title }}</span>
            <span class="p-sub">{{ opt.sub }}</span>
          </span>
          <span v-if="priorityOrder(opt.criterion)" class="p-badge">{{
            priorityOrder(opt.criterion)
          }}</span>
        </button>
      </div>
    </simplebar>

    <div class="fixed-footer">
      <!-- 하단 버튼 -->
      <div class="bottom-bar">
        <button
          v-if="step === 1"
          class="btn-quick-setup"
          type="button"
          data-tour="preferences-quick-setup"
          :disabled="isLoading || isSaving || !pref.workplace"
          @click="openQuickSetupModal"
        >
          빠른 이동
        </button>
        <button
          v-if="step > 1"
          class="btn-prev"
          :disabled="isSaving"
          @click="go(step - 1)"
        >
          이전
        </button>
        <button
          class="btn-cta"
          :disabled="isLoading || isSaving || (step === 1 && !pref.workplace)"
          @click="onNext"
        >
          {{
            isSaving
              ? '저장 중...'
              : step === PREFERENCE_STEP_COUNT
                ? '설정 완료'
                : '다음'
          }}
        </button>
      </div>
    </div>

    <KakaoLocation
      v-if="step === 1"
      :open="isLocationPickerOpen"
      :initial-location="pref.workplace"
      @close="isLocationPickerOpen = false"
      @select="selectWorkplace"
    />

    <Teleport to="body">
      <div
        v-if="isQuickSetupModalOpen"
        class="quick-modal-overlay"
        @click.self="closeQuickSetupModal"
      >
        <div
          class="quick-modal"
          role="dialog"
          aria-modal="true"
          aria-labelledby="quick-setup-title"
          aria-describedby="quick-setup-description"
        >
          <div class="quick-modal-head">
            <div>
              <p id="quick-setup-title" class="quick-modal-title">
                어떤 조건으로 찾아볼까요?
              </p>
              <p id="quick-setup-description" class="quick-modal-description">
                선택한 유형에 맞춰 나머지 가치관을 자동으로 설정해드려요.
              </p>
            </div>
            <button
              class="quick-modal-close"
              type="button"
              aria-label="간편 설정 닫기"
              :disabled="isSaving"
              @click="closeQuickSetupModal"
            >
              ×
            </button>
          </div>

          <div class="quick-preset-list">
            <button
              v-for="preset in QUICK_SETUP_PRESETS"
              :key="preset.key"
              class="quick-preset-card"
              type="button"
              :disabled="isSaving"
              @click="applyQuickSetup(preset.key)"
            >
              <span class="quick-preset-texts">
                <span class="quick-preset-title">{{ preset.title }}</span>
                <span class="quick-preset-description">
                  {{ preset.description }}
                </span>
              </span>
              <span class="quick-preset-action" aria-hidden="true">
                {{ activeQuickPreset === preset.key ? '적용 중' : '›' }}
              </span>
            </button>
          </div>

          <p v-if="quickSetupError" class="quick-modal-error" role="alert">
            {{ quickSetupError }}
          </p>
        </div>
      </div>
    </Teleport>
    <!-- </div> -->

    <Teleport to="body">
      <Transition name="demo-pop">
        <div
          v-if="showDemoCompletePrompt"
          class="demo-complete-overlay"
          @click.self="stayOnDemo"
        >
          <div class="demo-complete-card">
            <p class="demo-complete-title">다시보기를 완료했어요</p>
            <p class="demo-complete-text">마이페이지로 돌아가시겠어요?</p>
            <div class="demo-complete-actions">
              <button
                class="demo-complete-stay"
                type="button"
                @click="stayOnDemo"
              >
                더 둘러보기
              </button>
              <button
                class="demo-complete-go"
                type="button"
                @click="goToMypage"
              >
                마이페이지로
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <OnboardingSpotlight
      group-name="preferences"
      :steps="ONBOARDING_STEPS.preferences"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getApiErrorMessage } from '@/api/client';
import { preferenceApi } from '@/api/services';
import DualSlider from '@/components/DualSlider.vue';
import KakaoLocation from '@/components/KakaoLocation.vue';
import simplebar from 'simplebar-vue';
import SingleSlider from '@/components/SingleSlider.vue';
import OnboardingSpotlight from '@/components/OnboardingSpotlight.vue';
import { ONBOARDING_STEPS } from '@/constants/onboardingSteps';
import {
  AMENITY_CATEGORIES,
  FLOOR_OPTIONS,
  HOUSING_OPTIONS,
  INFRA_CATEGORIES,
  MAX_PRIORITY_SELECTIONS,
  PREFERENCE_SLIDER_CONFIG,
  PREFERENCE_STEP_COUNT,
  PREFERENCE_STEP_LABELS,
  PRIORITY_OPTIONS,
  TRADE_OPTIONS,
} from '@/constants/preferenceOptions';

const route = useRoute();
const router = useRouter();
const AREA_MIN_M2 = 0;
const AREA_MAX_M2 = 200;
const AREA_STEP_M2 = 5;
const AREA_MARKS = ['0', '50m²', '100m²', '150m²', '200m²'];
const PYEONG_M2 = 3.3058;
const PREFERENCE_DRAFT_KEY = 'tajiro-preferences';
const BASEMENT_UI_VALUE = '지하/반지하';
const BASEMENT_API_VALUE = '지하/반지하';

const QUICK_SETUP_PRESETS = [
  {
    key: 'DEFAULT',
    title: '선호 위치로 바로보기',
    description: '조건을 넓게 설정해 다양한 매물을 확인해요.',
  },
  {
    key: 'YOUTH',
    title: '청년 맞춤으로 보기',
    description: '원룸·오피스텔과 비용 부담이 적은 매물을 우선해요.',
  },
  {
    key: 'WORKER',
    title: '직장인 맞춤으로 보기',
    description: '통근 거리와 생활 인프라가 좋은 매물을 우선해요.',
  },
];

function createDefaultPreference() {
  return {
    workplace: null,
    hasCar: false,
    maxCommuteDistanceMeters:
      PREFERENCE_SLIDER_CONFIG.COMMUTE_DISTANCE.defaultValue,
    housingTypes: [],
    tradeTypes: [],
    depositJeonseRange: [
      PREFERENCE_SLIDER_CONFIG.DEPOSIT_JEONSE.min,
      PREFERENCE_SLIDER_CONFIG.DEPOSIT_JEONSE.max,
    ],
    monthlyRentRange: [
      PREFERENCE_SLIDER_CONFIG.MONTHLY_RENT.min,
      PREFERENCE_SLIDER_CONFIG.MONTHLY_RENT.max,
    ],
    salePriceRange: [
      PREFERENCE_SLIDER_CONFIG.SALE_PRICE.min,
      PREFERENCE_SLIDER_CONFIG.SALE_PRICE.max,
    ],
    areaRange: [AREA_MIN_M2, AREA_MAX_M2],
    floorPreference: [],
    desiredInfraCategories: [],
    desiredAmenityCategories: [],
    priorities: [],
  };
}

const pref = reactive(createDefaultPreference());

const step = computed(() => Number(route.params.step) || 1);
// 마이페이지 "다시보기"(?demo=1) 전용 — 실제 저장된 가치관을 불러오지 않고 항상 빈 기본값 폼으로
// 시작하며, 끝까지 진행해도 실제 API 호출·저장이 일어나지 않는다.
const isDemoMode = computed(() => route.query.demo === '1');
const isLocationPickerOpen = ref(false);
const isLoading = ref(false);
const isSaving = ref(false);
const hasSavedPreference = ref(false);
const errorMessage = ref('');
const isQuickSetupModalOpen = ref(false);
const activeQuickPreset = ref(null);
const quickSetupError = ref('');
// 목데이터 다시보기에서 4단계까지 실제로 다 넘겨본 뒤 '설정 완료'를 누른 시점에만 띄우는 안내.
const showDemoCompletePrompt = ref(false);

function stayOnDemo() {
  showDemoCompletePrompt.value = false;
}

function goToMypage() {
  showDemoCompletePrompt.value = false;
  router.push('/mypage');
}

onMounted(loadPreference);

watch(step, (currentStep) => {
  if (currentStep !== 1) {
    isLocationPickerOpen.value = false;
  }
});

const commuteDistanceLabel = computed(() => {
  const kilometers = pref.maxCommuteDistanceMeters / 1000;
  return `${Number(kilometers.toFixed(1))}km 이내`;
});

const formatPrice = (value, max) => {
  if (value <= 0) return '최소';
  if (value >= max) return '최대';
  return value >= 10000
    ? `${(value / 10000).toFixed(2).replace(/\.?0+$/, '')}억`
    : `${value.toLocaleString()}만`;
};

const formatRange = (range, max) =>
  range[0] <= 0 && range[1] >= max
    ? '전체'
    : `${formatPrice(range[0], max)} ~ ${formatPrice(range[1], max)}`;

const depositJeonseLabel = computed(() =>
  formatRange(
    pref.depositJeonseRange,
    PREFERENCE_SLIDER_CONFIG.DEPOSIT_JEONSE.max,
  ),
);
const monthlyRentLabel = computed(() =>
  formatRange(pref.monthlyRentRange, PREFERENCE_SLIDER_CONFIG.MONTHLY_RENT.max),
);
const salePriceLabel = computed(() =>
  formatRange(pref.salePriceRange, PREFERENCE_SLIDER_CONFIG.SALE_PRICE.max),
);
const areaLabel = computed(() => {
  const [low, high] = pref.areaRange;
  const lowLabel =
    low <= AREA_MIN_M2 ? '최소' : `${low}m² (${Math.floor(low / PYEONG_M2)}평)`;
  const highLabel =
    high >= AREA_MAX_M2
      ? '최대'
      : `${high}m² (${Math.floor(high / PYEONG_M2)}평)`;

  return `${lowLabel} ~ ${highLabel}`;
});

function toggle(list, value) {
  const i = list.indexOf(value);
  if (i >= 0) list.splice(i, 1);
  else list.push(value);
}

function onTogglePriority(criterion) {
  const index = pref.priorities.findIndex(
    (priority) => priority.criterion === criterion,
  );

  if (index >= 0) {
    pref.priorities.splice(index, 1);
    pref.priorities.forEach((priority, priorityIndex) => {
      priority.priorityOrder = priorityIndex + 1;
    });
    return;
  }

  if (pref.priorities.length >= MAX_PRIORITY_SELECTIONS) return;

  pref.priorities.push({
    criterion,
    priorityOrder: pref.priorities.length + 1,
  });
}
function priorityOrder(criterion) {
  return (
    pref.priorities.find((p) => p.criterion === criterion)?.priorityOrder ?? 0
  );
}

function goLocationSelect() {
  isLocationPickerOpen.value = true;
}

function selectWorkplace(location) {
  pref.workplace = location;
  isLocationPickerOpen.value = false;
}

function openQuickSetupModal() {
  if (!pref.workplace || isSaving.value) return;
  quickSetupError.value = '';
  isQuickSetupModalOpen.value = true;
}

function closeQuickSetupModal() {
  if (isSaving.value) return;
  isQuickSetupModalOpen.value = false;
  activeQuickPreset.value = null;
  quickSetupError.value = '';
}

function createQuickSetupPreference(presetKey) {
  const defaults = createDefaultPreference();
  const stepOnePreference = {
    workplace: pref.workplace ? { ...pref.workplace } : null,
    hasCar: pref.hasCar,
    maxCommuteDistanceMeters: pref.maxCommuteDistanceMeters,
  };

  if (presetKey === 'YOUTH') {
    return {
      ...defaults,
      ...stepOnePreference,
      housingTypes: ['원룸', '오피스텔'],
      tradeTypes: ['월세', '전세'],
      depositJeonseRange: [0, 20000],
      monthlyRentRange: [0, 100],
      areaRange: [0, 60],
      floorPreference: ['1층', '2층 이상'],
      desiredInfraCategories: ['SUBWAY', 'BUS_TERMINAL'],
      desiredAmenityCategories: ['CONVENIENCE', 'MART', 'CAFE'],
      priorities: [
        { criterion: 'COST', priorityOrder: 1 },
        { criterion: 'COMMUTE', priorityOrder: 2 },
        { criterion: 'AMENITY', priorityOrder: 3 },
      ],
    };
  }

  if (presetKey === 'WORKER') {
    return {
      ...defaults,
      ...stepOnePreference,
      housingTypes: ['아파트', '오피스텔'],
      tradeTypes: ['월세', '전세', '매매'],
      areaRange: [20, 120],
      floorPreference: ['2층 이상'],
      desiredInfraCategories: ['SUBWAY', 'BUS_TERMINAL', 'TRAIN', 'HOSPITAL'],
      desiredAmenityCategories: ['CONVENIENCE', 'MART', 'PARKING'],
      priorities: [
        { criterion: 'COMMUTE', priorityOrder: 1 },
        { criterion: 'INFRA', priorityOrder: 2 },
        { criterion: 'COST', priorityOrder: 3 },
      ],
    };
  }

  return { ...defaults, ...stepOnePreference };
}

function copyList(value, fallback) {
  return Array.isArray(value) ? [...value] : [...fallback];
}

function applyPreference(value) {
  if (!value || typeof value !== 'object') return;

  const defaults = createDefaultPreference();
  Object.assign(pref, {
    workplace: value.workplace ? { ...value.workplace } : defaults.workplace,
    hasCar: value.hasCar ?? defaults.hasCar,
    maxCommuteDistanceMeters:
      value.maxCommuteDistanceMeters ?? defaults.maxCommuteDistanceMeters,
    housingTypes: copyList(value.housingTypes, defaults.housingTypes),
    tradeTypes: copyList(value.tradeTypes, defaults.tradeTypes),
    depositJeonseRange: copyList(
      value.depositJeonseRange,
      defaults.depositJeonseRange,
    ),
    monthlyRentRange: copyList(
      value.monthlyRentRange,
      defaults.monthlyRentRange,
    ),
    salePriceRange: copyList(value.salePriceRange, defaults.salePriceRange),
    areaRange: copyList(value.areaRange, defaults.areaRange),
    floorPreference: copyList(
      value.floorPreference,
      defaults.floorPreference,
    ).map((floor) =>
      floor === BASEMENT_API_VALUE ? BASEMENT_UI_VALUE : floor,
    ),
    desiredInfraCategories: copyList(
      value.desiredInfraCategories,
      defaults.desiredInfraCategories,
    ),
    desiredAmenityCategories: copyList(
      value.desiredAmenityCategories,
      defaults.desiredAmenityCategories,
    ),
    priorities: copyList(value.priorities, defaults.priorities).map(
      (priority) => ({ ...priority }),
    ),
  });
}

function toRequestPayload() {
  return {
    workplace: pref.workplace
      ? {
          name: pref.workplace.name,
          address: pref.workplace.address,
          lat: pref.workplace.lat,
          lng: pref.workplace.lng,
        }
      : null,
    hasCar: pref.hasCar,
    maxCommuteDistanceMeters: pref.maxCommuteDistanceMeters,
    housingTypes: [...pref.housingTypes],
    tradeTypes: [...pref.tradeTypes],
    depositJeonseRange: [...pref.depositJeonseRange],
    monthlyRentRange: [...pref.monthlyRentRange],
    salePriceRange: [...pref.salePriceRange],
    areaRange: [...pref.areaRange],
    floorPreference: pref.floorPreference.map((floor) =>
      floor === BASEMENT_UI_VALUE ? BASEMENT_API_VALUE : floor,
    ),
    desiredInfraCategories: [...pref.desiredInfraCategories],
    desiredAmenityCategories: [...pref.desiredAmenityCategories],
    priorities: pref.priorities.map((priority) => ({ ...priority })),
  };
}

async function savePreference() {
  if (isDemoMode.value) return;

  const requestPayload = toRequestPayload();
  const savedPreference = hasSavedPreference.value
    ? await preferenceApi.update(requestPayload)
    : await preferenceApi.create(requestPayload);

  applyPreference(savedPreference);
  hasSavedPreference.value = true;
  localStorage.removeItem(PREFERENCE_DRAFT_KEY);
  await router.push(
    route.query.returnTo === 'compare-box' ? '/compare-box' : '/properties',
  );
}

async function applyQuickSetup(presetKey) {
  if (!pref.workplace || isSaving.value) return;

  isSaving.value = true;
  activeQuickPreset.value = presetKey;
  quickSetupError.value = '';
  errorMessage.value = '';

  try {
    applyPreference(createQuickSetupPreference(presetKey));
    await savePreference();
    if (isDemoMode.value) {
      isQuickSetupModalOpen.value = false;
      showDemoCompletePrompt.value = true;
    }
  } catch (error) {
    const message = getApiErrorMessage(
      error,
      '간편 설정을 저장하지 못했습니다. 잠시 후 다시 시도해주세요.',
    );
    quickSetupError.value = message;
    errorMessage.value = message;
  } finally {
    isSaving.value = false;
    activeQuickPreset.value = null;
  }
}

function saveDraft() {
  if (isDemoMode.value) return;
  localStorage.setItem(PREFERENCE_DRAFT_KEY, JSON.stringify(pref));
}

function restoreDraft() {
  if (isDemoMode.value) return;
  const draft = localStorage.getItem(PREFERENCE_DRAFT_KEY);
  if (!draft) return;

  try {
    applyPreference(JSON.parse(draft));
  } catch {
    localStorage.removeItem(PREFERENCE_DRAFT_KEY);
  }
}

function isPreferenceNotFound(error) {
  return (
    error?.response?.status === 404 ||
    error?.response?.data?.code === 'PREF_404'
  );
}

async function loadPreference() {
  if (isDemoMode.value) return;

  isLoading.value = true;
  errorMessage.value = '';

  try {
    const savedPreference = await preferenceApi.get();
    applyPreference(savedPreference);
    hasSavedPreference.value = true;
    restoreDraft();
  } catch (error) {
    restoreDraft();
    if (!isPreferenceNotFound(error)) {
      errorMessage.value = getApiErrorMessage(
        error,
        '저장된 주거 선호 조건을 불러오지 못했습니다.',
      );
    }
  } finally {
    isLoading.value = false;
  }
}

function go(n) {
  saveDraft();
  return router.push({ path: `/preferences/${n}`, query: route.query });
}

async function onNext() {
  if (step.value < PREFERENCE_STEP_COUNT) {
    await go(step.value + 1);
    return;
  }

  if (isDemoMode.value) {
    showDemoCompletePrompt.value = true;
    return;
  }

  isSaving.value = true;
  errorMessage.value = '';
  saveDraft();

  try {
    await savePreference();
  } catch (error) {
    errorMessage.value = getApiErrorMessage(
      error,
      '주거 선호 조건을 저장하지 못했습니다. 잠시 후 다시 시도해주세요.',
    );
  } finally {
    isSaving.value = false;
  }
}
</script>

<style scoped>
/* 1. 최상위 레이아웃 - 화면 전체 높이를 딱 맞춤 */
.pref-wizard {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  min-height: 0;
  overflow: hidden;
  background: var(--white);
}

.demo-banner {
  flex-shrink: 0;
  margin: 12px 16px 0;
  padding: 9px 12px;
  background: #f1efea;
  border: 1px dashed var(--kb-silver);
  border-radius: 10px;
  font-size: 11.5px;
  font-weight: 700;
  color: var(--kb-gray);
  text-align: center;
}

.demo-corner-exit {
  position: absolute;
  top: 10px;
  right: 12px;
  z-index: 50;
  padding: 6px 12px;
  border-radius: 100px;
  background: rgba(33, 30, 21, 0.72);
  color: #fff;
  font-size: 11px;
  font-weight: 700;
}

.demo-complete-overlay {
  position: fixed;
  inset: 0;
  z-index: 320;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(20, 16, 8, 0.5);
}
.demo-complete-card {
  width: 100%;
  max-width: 300px;
  padding: 22px 20px 20px;
  background: var(--white, #fff);
  border-radius: 20px;
  text-align: center;
}
.demo-complete-title {
  font-size: 15.5px;
  font-weight: 800;
  color: var(--text-primary, #33302a);
}
.demo-complete-text {
  margin-top: 8px;
  font-size: 12.5px;
  line-height: 1.55;
  color: var(--kb-gray, #60584c);
}
.demo-complete-actions {
  display: flex;
  gap: 8px;
  margin-top: 18px;
}
.demo-complete-stay {
  flex: 1;
  padding: 12px;
  border: 1px solid var(--border, #e9e7e2);
  border-radius: 12px;
  background: var(--white, #fff);
  font-size: 13px;
  font-weight: 700;
  color: var(--kb-gray, #60584c);
}
.demo-complete-go {
  flex: 1;
  padding: 12px;
  border-radius: 12px;
  background: var(--kb-yellow, #ffbc00);
  font-size: 13px;
  font-weight: 800;
  color: var(--text-primary, #33302a);
}
.demo-pop-enter-active {
  transition: opacity 0.18s ease;
}
.demo-pop-enter-from {
  opacity: 0;
}
.demo-pop-leave-active {
  transition: opacity 0.12s ease;
}
.demo-pop-leave-to {
  opacity: 0;
}

/* 2. 상단 스텝 헤더 고정 */
.step-head {
  flex-shrink: 0;
  padding: 14px 16px 12px;
}
.status-message {
  flex-shrink: 0;
  margin: 0 16px 8px;
  padding: 10px 12px;
  border-radius: 10px;
  background: var(--bg);
  color: var(--kb-gray);
  font-size: 12px;
}
.status-message.error {
  background: #fff3f3;
  color: var(--danger);
}
.step-line {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
}
.step-no {
  color: #d9a800;
  font-weight: 700;
  letter-spacing: 0.4px;
}
.dot {
  color: var(--kb-silver);
}
.step-label {
  color: var(--text-primary);
  font-weight: 500;
}
.progress {
  display: flex;
  gap: 6px;
  margin-top: 10px;
}
.seg {
  flex: 1;
  height: 4px;
  border-radius: 2px;
  background: var(--border);
}
.seg.on {
  background: var(--kb-yellow);
}

/* 3. 중간 스크롤 영역 - 남은 높이만 차지하여 독립 스크롤 생성 */
.content {
  flex: 1 1 0%;
  height: 0;
  padding: 0 16px;
  min-height: 0;
}

/* Simplebar 내부 콘텐츠 배치 및 패딩 */
.content :deep(.simplebar-content) {
  display: flex;
  flex-direction: column;
  gap: 22px;
  padding: 10px 16px 24px;
}

.section-title {
  font-size: 15px;
  font-weight: 800;
  color: var(--text-primary);
}
.section-title.big {
  font-size: 16px;
}
.req {
  color: var(--danger);
}
.caption {
  font-size: 11.5px;
  color: var(--kb-silver);
  margin-top: 4px;
}
.check-row {
  display: flex;
  gap: 28px;
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
  background: var(--white);
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
.group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.chips {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.chip {
  padding: 8px 14px;
  border-radius: 100px;
  border: 1px solid var(--border);
  background: var(--white);
  font-size: 13px;
  color: var(--text-primary);
}
.chip.on {
  background: var(--yellow-tint);
  border-color: var(--kb-yellow);
  font-weight: 700;
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
.area-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 4px 0 18px;
}
.range-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  font-size: 13.5px;
  font-weight: 800;
}
.range-value {
  margin-left: auto;
  text-align: right;
  color: var(--kb-gray);
  font-weight: 500;
}
.priority-head {
  display: flex;
  flex-direction: column;
}
.priority-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.priority-card {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 14px;
  text-align: left;
  border: 1px solid var(--border);
  border-radius: 14px;
  background: var(--white);
}
.priority-card.on {
  background: var(--yellow-tint);
  border-color: var(--kb-yellow);
}
.p-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 100px;
  background: rgba(255, 188, 0, 0.14);
  flex-shrink: 0;
}
.priority-card.on .p-icon {
  background: rgba(255, 188, 0, 0.28);
}
.p-texts {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
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
  background: var(--kb-yellow);
  color: var(--text-primary);
  font-size: 12.5px;
  font-weight: 800;
  flex-shrink: 0;
}

/* 4. 하단 버튼 영역 고정 */
.fixed-footer {
  flex-shrink: 0;
  width: 100%;
  border-top: 1px solid var(--border);
  background: var(--white);
}
.fixed-footer :deep(.tab-bar) {
  position: static;
}
.bottom-bar {
  display: flex;
  gap: 10px;
  padding: 12px 16px 16px;
  background: var(--white);
}
.bottom-bar .btn-cta {
  flex: 3 1 0;
  width: auto;
}
.btn-quick-setup {
  flex: 1 1 0;
  height: 46px;
  border: 1px solid var(--border);
  border-radius: 14px;
  background: var(--white);
  font-size: 14px;
  font-weight: 700;
}
.btn-prev {
  flex: 1 1 0;
  height: 46px;
  border-radius: 14px;
  border: 1px solid var(--border);
  background: var(--white);
  font-size: 14px;
  font-weight: 700;
}
.btn-prev:disabled,
.btn-quick-setup:disabled,
.btn-cta:disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

.field-input[readonly] {
  cursor: pointer;
}

.quick-modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 130;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(33, 30, 24, 0.5);
}
.quick-modal {
  width: 100%;
  max-width: 340px;
  padding: 22px 18px 18px;
  border-radius: 20px;
  background: var(--white);
}
.quick-modal-head {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}
.quick-modal-title {
  font-size: 17px;
  font-weight: 900;
  color: var(--text-primary);
}
.quick-modal-description {
  margin-top: 6px;
  color: var(--kb-gray);
  font-size: 12.5px;
  line-height: 1.5;
}
.quick-modal-close {
  flex-shrink: 0;
  width: 30px;
  height: 30px;
  margin: -4px -4px 0 auto;
  border-radius: 50%;
  background: var(--bg);
  color: var(--kb-gray);
  font-size: 22px;
  line-height: 1;
}
.quick-preset-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 20px;
}
.quick-preset-card {
  display: flex;
  align-items: center;
  width: 100%;
  min-height: 72px;
  gap: 10px;
  padding: 13px 14px;
  border: 1px solid var(--border);
  border-radius: 14px;
  background: var(--white);
  text-align: left;
}
.quick-preset-card:hover:not(:disabled),
.quick-preset-card:focus-visible {
  border-color: var(--kb-yellow);
  background: var(--yellow-tint);
}
.quick-preset-card:disabled {
  cursor: default;
  opacity: 0.6;
}
.quick-preset-texts {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 4px;
}
.quick-preset-title {
  color: var(--text-primary);
  font-size: 14px;
  font-weight: 800;
}
.quick-preset-description {
  color: var(--kb-silver);
  font-size: 11.5px;
  line-height: 1.45;
}
.quick-preset-action {
  flex-shrink: 0;
  color: #a8842c;
  font-size: 24px;
  font-weight: 700;
  white-space: nowrap;
}
.quick-preset-card:disabled .quick-preset-action {
  font-size: 11px;
}
.quick-modal-error {
  margin-top: 12px;
  color: var(--danger);
  font-size: 12px;
  line-height: 1.45;
}
</style>
