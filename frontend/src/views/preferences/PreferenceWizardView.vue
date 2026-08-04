<template>
  <div class="pref-wizard">
    <PageHeader title="가치관 설정" />

    <!-- STEP 표시 + 진행 바 -->
    <div class="step-head">
      <p class="step-line">
        <span class="step-no">STEP {{ step }} / 4</span>
        <span class="dot">·</span>
        <span class="step-label">{{ stepLabels[step - 1] }}</span>
      </p>
      <div class="progress">
        <div v-for="n in 4" :key="n" class="seg" :class="{ on: n <= step }" />
      </div>
    </div>

    <!-- STEP 1 — 이주·통근 정보 -->
    <div v-if="step === 1" class="content">
      <div class="field">
        <label class="section-title"
          >선호위치(직장 / 학교 등)<span class="req">*</span></label
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
          :min="0"
          :max="10000"
          :step="100"
          :marks="['0', '2km', '4km', '6km', '8km', '10km']"
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
    </div>

    <!-- STEP 2 — 희망 주거 조건 -->
    <div v-else-if="step === 2" class="content">
      <div class="group">
        <p class="section-title">매물 유형</p>
        <div class="chips">
          <button
            v-for="t in housingOptions"
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
            v-for="t in tradeOptions"
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
            pref.tradeTypes.includes('월세') ||
            pref.tradeTypes.includes('전세')
          "
          class="range-group"
        >
          <p class="range-title">
            보증금/전세금
            <span class="range-value">{{ depositJeonseLabel }}</span>
          </p>
          <DualSlider
            v-model="pref.depositJeonseRange"
            :min="0"
            :max="50000"
            :step="500"
            :marks="['최소', '1억', '2억', '3억', '4억', '최대']"
          />
        </div>

        <div v-if="pref.tradeTypes.includes('월세')" class="range-group">
          <p class="range-title">
            월세 <span class="range-value">{{ monthlyRentLabel }}</span>
          </p>
          <DualSlider
            v-model="pref.monthlyRentRange"
            :min="0"
            :max="250"
            :step="5"
            :marks="['최소', '50만', '100만', '150만', '200만', '최대']"
          />
        </div>

        <div v-if="pref.tradeTypes.includes('매매')" class="range-group">
          <p class="range-title">
            매매가 <span class="range-value">{{ salePriceLabel }}</span>
          </p>
          <DualSlider
            v-model="pref.salePriceRange"
            :min="0"
            :max="400000"
            :step="5000"
            :marks="['최소', '10억', '20억', '30억', '최대']"
          />
        </div>
      </div>

      <div class="group">
        <p class="section-title">매물 층수</p>
        <div class="chips">
          <button
            v-for="f in floorOptions"
            :key="f"
            class="chip"
            :class="{ on: pref.floorPreference.includes(f) }"
            @click="toggle(pref.floorPreference, f)"
          >
            {{ f }}
          </button>
        </div>
      </div>
    </div>

    <!-- STEP 3 — 인프라·편의시설 -->
    <div v-else-if="step === 3" class="content">
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
              on: pref.desireAmenityCategories.includes(category.key),
            }"
            @click="toggle(pref.desireAmenityCategories, category.key)"
          >
            {{ category.label }}
          </button>
        </div>
      </div>
    </div>

    <!-- STEP 4 — 가치관 우선순위 (1~3개 선택) -->
    <div v-else class="content">
      <p class="section-title big">주거 가치관 우선순위</p>
      <div class="priority-list">
        <button
          v-for="opt in priorityOptions"
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
      <p v-if="maxWarning" class="max-warning">
        우선순위는 최대 3개까지 선택할 수 있어요.
      </p>
    </div>

    <div class="fixed-footer">
      <!-- 하단 버튼 -->
      <div class="bottom-bar">
        <button v-if="step > 1" class="btn-prev" @click="go(step - 1)">
          이전
        </button>
        <button
          class="btn-cta"
          :disabled="
            (step === 1 && !pref.workplace) ||
            (step === 4 && pref.priorities.length < 1)
          "
          @click="onNext"
        >
          {{ step === 4 ? '설정 완료' : '다음' }}
        </button>
      </div>

      <AppTabBar active="property" />
    </div>

    <KakaoLocation
      :open="isLocationPickerOpen"
      :initial-location="pref.workplace"
      @close="isLocationPickerOpen = false"
      @select="selectWorkplace"
    />
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import AppTabBar from '@/components/AppTabBar.vue';
import DualSlider from '@/components/DualSlider.vue';
import KakaoLocation from '@/components/KakaoLocation.vue';
import PageHeader from '@/components/PageHeader.vue';
import SingleSlider from '@/components/SingleSlider.vue';

const route = useRoute();
const router = useRouter();
const pref = reactive({
  workplace: null,
  hasCar: false,
  maxCommuteDistanceMeters: 1500,
  housingTypes: [],
  tradeTypes: [],
  depositJeonseRange: [0, 50000],
  monthlyRentRange: [0, 250],
  salePriceRange: [0, 400000],
  floorPreference: [],
  desiredInfraCategories: [],
  desireAmenityCategories: [],
  priorities: [],
});

const step = computed(() => Number(route.params.step) || 1);
const stepLabels = [
  '이주·통근 정보',
  '희망 주거 조건',
  '인프라·편의시설',
  '가치관 우선순위',
];

const housingOptions = ['원/투룸', '아파트', '주택/빌라', '오피스텔'];
const tradeOptions = ['월세', '전세', '매매'];
const floorOptions = ['저층', '1층', '2층 이상', '옥탑'];
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

const priorityOptions = [
  {
    criterion: 'COMMUTE',
    title: '직주근접',
    sub: '출퇴근 시간이 가장 중요해요',
    icon: '<svg width="20" height="20" viewBox="0 0 20 20" fill="none"><circle cx="10" cy="10" r="7.2" stroke="#545045" stroke-width="1.5"/><path d="M10 6v4l2.6 1.6" stroke="#545045" stroke-width="1.5" stroke-linecap="round"/></svg>',
  },
  {
    criterion: 'COST',
    title: '가성비',
    sub: '월세·관리비 등 주거비 절약',
    icon: '<svg width="20" height="20" viewBox="0 0 20 20" fill="none"><circle cx="10" cy="10" r="7.2" stroke="#545045" stroke-width="1.5"/><path d="M7 8h6M7 10.5h6M9 6.5l2 7" stroke="#545045" stroke-width="1.3" stroke-linecap="round"/></svg>',
  },
  {
    criterion: 'INFRA',
    title: '인프라',
    sub: '교육시설, 의료시설, 교통시설, 공공기관 등',
    icon: '<svg width="20" height="20" viewBox="0 0 20 20" fill="none"><rect x="4" y="3.5" width="8" height="13" stroke="#545045" stroke-width="1.5"/><path d="M12 8h4v8.5h-4M6.5 7h1.2M6.5 10h1.2M6.5 13h1.2M9.5 7h1.2M9.5 10h1.2M9.5 13h1.2" stroke="#545045" stroke-width="1.2"/></svg>',
  },
  {
    criterion: 'AMENITY',
    title: '편의시설',
    sub: '마트, 편의점, 카페 등',
    icon: '<svg width="20" height="20" viewBox="0 0 20 20" fill="none"><path d="M10 2.5l6.5 3v5c0 4-2.8 6.4-6.5 7.5-3.7-1.1-6.5-3.5-6.5-7.5v-5l6.5-3z" stroke="#545045" stroke-width="1.5" stroke-linejoin="round"/><path d="M7.2 10l2 2 3.6-3.8" stroke="#545045" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>',
  },
  {
    criterion: 'AREA',
    title: '매물 면적',
    sub: '매물의 면적이 가장 중요해요',
    icon: '<svg width="20" height="20" viewBox="0 0 20 20" fill="none"><circle cx="10" cy="10" r="7.2" stroke="#545045" stroke-width="1.5"/><path d="M10 6.2v4.6" stroke="#545045" stroke-width="1.6" stroke-linecap="round"/><circle cx="10" cy="13.6" r="0.9" fill="#545045"/></svg>',
  },
];

const isLocationPickerOpen = ref(false);

const commuteDistanceLabel = computed(
  () => `${pref.maxCommuteDistanceMeters.toLocaleString()}m 이내`,
);

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
  formatRange(pref.depositJeonseRange, 50000),
);
const monthlyRentLabel = computed(() =>
  formatRange(pref.monthlyRentRange, 250),
);
const salePriceLabel = computed(() =>
  formatRange(pref.salePriceRange, 400000),
);

function toggle(list, value) {
  const i = list.indexOf(value);
  if (i >= 0) list.splice(i, 1);
  else list.push(value);
}

const maxWarning = ref(false);
function onTogglePriority(criterion) {
  const index = pref.priorities.findIndex(
    (priority) => priority.criterion === criterion,
  );

  if (index >= 0) {
    pref.priorities.splice(index, 1);
    pref.priorities.forEach((priority, priorityIndex) => {
      priority.priorityOrder = priorityIndex + 1;
    });
    maxWarning.value = false;
    return;
  }

  const ok = pref.priorities.length < 3;
  if (ok) {
    pref.priorities.push({
      criterion,
      priorityOrder: pref.priorities.length + 1,
    });
  }

  maxWarning.value = !ok;
  if (ok) setTimeout(() => (maxWarning.value = false), 0);
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

function go(n) {
  router.push(`/preferences/${n}`);
}

function onNext() {
  if (step.value < 4) {
    go(step.value + 1);
  } else {
    localStorage.setItem('tajiro-preferences', JSON.stringify(pref));
    router.push('/properties');
  }
}
</script>

<style scoped>
.pref-wizard {
  position: relative;
  flex: 0 0 100dvh;
  display: flex;
  flex-direction: column;
  height: 100dvh;
  min-height: 0;
  overflow: hidden;
  background: var(--white);
}
.step-head {
  flex-shrink: 0;
  padding: 14px 16px 12px;
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
.content {
  flex: 1;
  min-height: 0;
  padding: 10px 16px 24px;
  display: flex;
  flex-direction: column;
  gap: 22px;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
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
.range-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13.5px;
  font-weight: 800;
}
.range-value {
  color: var(--kb-gray);
  font-weight: 500;
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
.max-warning {
  margin-top: 10px;
  font-size: 12px;
  color: var(--danger);
}
.fixed-footer {
  flex-shrink: 0;
  width: 100%;
  background: var(--white);
}
.fixed-footer :deep(.tab-bar) {
  position: static;
}
.bottom-bar {
  display: flex;
  gap: 10px;
  padding: 12px 16px 16px;
  border-top: 1px solid var(--border);
  background: var(--white);
}
.btn-prev {
  flex: 0 0 88px;
  height: 46px;
  border-radius: 14px;
  border: 1px solid var(--border);
  background: var(--white);
  font-size: 14px;
  font-weight: 700;
}

.field-input[readonly] {
  cursor: pointer;
}
</style>
