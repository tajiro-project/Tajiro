<template>
  <div class="cbox">
    <simplebar class="scroll-area">
      <h1 class="title">비교할 매물을 확인해주세요 (최대 3개)</h1>
      <p class="sub">
        {{ items.length }}개 담겨 있어요
        <template v-if="items.length < 3">
          · {{ 3 - items.length }}개 더 담을 수 있어요</template
        >
      </p>

      <section class="comparison-settings-card">
        <button class="location-row" type="button" @click="openLocationPicker">
          <span class="location-marker" aria-hidden="true">
            <span class="location-marker-dot" />
          </span>
          <span class="location-texts">
            <span class="location-label">선호 위치</span>
            <span
              class="location-address"
              :class="{ placeholder: !comparisonWorkplace }"
            >
              {{
                comparisonWorkplace?.name ||
                comparisonWorkplace?.address ||
                '선호 위치를 선택해주세요'
              }}
            </span>
          </span>
          <ChevronRight
            class="location-arrow"
            :size="22"
            :stroke-width="2.2"
            aria-hidden="true"
          />
        </button>

        <button class="priority-row" type="button" @click="openPrioritySheet">
          <svg
            class="priority-row-icon"
            width="18"
            height="18"
            viewBox="0 0 18 18"
            fill="none"
            aria-hidden="true"
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

          <template v-if="comparisonPriorities.length">
            <span
              v-for="priority in comparisonPriorities"
              :key="priority.criterion"
              class="priority-chip"
            >
              <b class="priority-number">{{ priority.priorityOrder }}</b>
              {{ criterionLabel(priority.criterion) }}
            </span>
          </template>
          <span v-else class="priority-placeholder">가치관 미선택 시 동일 가중치로 비교해요</span>
        </button>
      </section>

      <p v-if="locationMessage" class="location-message">
        {{ locationMessage }}
      </p>
      <p v-if="priorityMessage" class="priority-message">
        {{ priorityMessage }}
      </p>

      <div v-if="loading" class="state">비교함을 불러오는 중이에요.</div>
      <div v-else-if="errorMessage" class="state error">
        {{ errorMessage }}
      </div>
      <div v-else-if="items.length === 0" class="state">
        아직 비교함에 담긴 매물이 없어요.
      </div>

      <ul v-else class="items">
        <li
          v-for="item in items"
          :key="item.propertyId"
          class="item"
        >
          <div class="item-card">
            <span class="thumb">
              <img
                v-if="
                  item.thumbnailUrl && !imageErrorIds.includes(item.propertyId)
                "
                :src="item.thumbnailUrl"
                alt=""
                @error="markImageError(item.propertyId)"
              />
              <svg
                v-else
                width="26"
                height="26"
                viewBox="0 0 30 30"
                fill="none"
              >
                <path
                  d="M4 13.5L15 5l11 8.5"
                  stroke="var(--kb-gold)"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
                <path
                  d="M7 12v12h16V12"
                  stroke="var(--kb-gold)"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
            </span>

            <div class="item-texts">
              <p class="item-title">{{ item.title }}</p>
              <p class="item-sub">
                {{ item.propertyType || '매물'
                }}<template v-if="item.buildingName?.trim()">
                  · {{ item.buildingName.trim() }}</template
                >
              </p>
              <p class="item-price">{{ formatTrade(item) }}</p>
              <p class="item-meta">
                {{ formatArea(item.areaM2) }} ·
                {{ formatFloorInfo(item.floorInfo) }} · 관리비
                {{ formatFee(item) }}
              </p>
            </div>

            <button
              class="remove"
              type="button"
              aria-label="삭제"
              :disabled="deletingId === item.propertyId"
              @click.stop="removeItem(item.propertyId)"
            >
              <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                <path
                  d="M3.5 3.5L10.5 10.5M10.5 3.5L3.5 10.5"
                  stroke="currentColor"
                  stroke-width="1.4"
                  stroke-linecap="round"
                />
              </svg>
            </button>
          </div>
        </li>
      </ul>

      <button
        v-if="items.length < 3"
        class="add-btn"
        type="button"
        @click="goPropertyListForAdd"
      >
        <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
          <path
            d="M7 2v10M2 7h10"
            stroke="var(--kb-gold)"
            stroke-width="1.5"
            stroke-linecap="round"
          />
        </svg>
        매물 리스트에서 추가하기
      </button>

      <p class="tip">
        <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
          <path
            d="M7 1.5a4.2 4.2 0 00-2.4 7.6c.5.4.9 1 .9 1.6v.3h3v-.3c0-.6.4-1.2.9-1.6A4.2 4.2 0 007 1.5z"
            stroke="#8a8477"
            stroke-width="1.2"
          />
          <path
            d="M5.8 12.5h2.4"
            stroke="#8a8477"
            stroke-width="1.2"
            stroke-linecap="round"
          />
        </svg>
        2~3개를 담으면 선택한 가치관 기준으로 비교 코칭을 해드려요.
      </p>

      <button
        class="btn-cta"
        type="button"
        :disabled="items.length < 2 || startingComparison"
        @click="startCompare"
      >
        {{
          startingComparison
            ? '비교 준비 중...'
            : `비교 시작 (${items.length}개)`
        }}
      </button>
    </simplebar>

    <KakaoLocation
      :open="isLocationPickerOpen"
      :initial-location="comparisonWorkplace"
      @close="isLocationPickerOpen = false"
      @select="selectWorkplace"
    />

    <BottomSheet
      :model-value="isPrioritySheetOpen"
      title="비교 가치관 설정"
      @update:model-value="closePrioritySheet"
    >
      <p class="sheet-note">
        중요한 순서대로 최대 3개까지 선택하세요.<br />
        이 값은 비교 리포트에만 적용되며 내 가치관에는 저장되지 않아요.
      </p>
      <div class="priority-list">
        <button
          v-for="option in PRIORITY_OPTIONS"
          :key="option.criterion"
          class="priority-card"
          :class="{ on: priorityRank(option.criterion) != null }"
          type="button"
          @click="togglePriority(option.criterion)"
        >
          <span class="priority-option-icon" v-html="option.icon" />
          <span class="priority-texts">
            <span class="priority-title">{{ option.title }}</span>
            <span class="priority-sub">{{ option.sub }}</span>
          </span>
          <span v-if="priorityRank(option.criterion)" class="priority-badge">
            {{ priorityRank(option.criterion) }}
          </span>
        </button>
      </div>
      <div class="sheet-actions">
        <button class="btn-ghost" type="button" @click="draftPriorities = []">
          초기화
        </button>
        <button
          class="btn-primary"
          type="button"
          @click="applyPriorities"
        >
          이 순서로 적용
        </button>
      </div>
    </BottomSheet>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import simplebar from 'simplebar-vue';
import { ChevronRight } from 'lucide-vue-next';
import KakaoLocation from '@/components/KakaoLocation.vue';
import BottomSheet from '@/components/BottomSheet.vue';
import { getApiErrorMessage } from '@/api/client';
import { comparisonApi, preferenceApi } from '@/api/services';
import {
  MAX_PRIORITY_SELECTIONS,
  PRIORITY_OPTIONS,
} from '@/constants/preferenceOptions';

const router = useRouter();
const COMPARISON_DRAFT_KEY = 'tajiro:compare-box:draft';

const loading = ref(false);
const deletingId = ref('');
const errorMessage = ref('');
const items = ref([]);
const imageErrorIds = ref([]);
const startingComparison = ref(false);
const comparisonWorkplace = ref(null);
const isLocationPickerOpen = ref(false);
const locationMessage = ref('');
const comparisonPriorities = ref([]);
const draftPriorities = ref([]);
const isPrioritySheetOpen = ref(false);
const priorityMessage = ref('');
const hasEditedPriorities = ref(false);

onMounted(() => {
  loadCompareBox();
  const restoredDraft = restoreComparisonDraft();
  loadDefaultComparisonSettings(restoredDraft);
});

async function loadCompareBox() {
  loading.value = true;
  errorMessage.value = '';

  try {
    const data = await comparisonApi.box();
    const payload = data?.data ?? data;
    const nextItems = Array.isArray(payload) ? payload : (payload?.items ?? []);
    items.value = nextItems.slice(0, 3);
    imageErrorIds.value = [];
  } catch (error) {
    errorMessage.value = getApiErrorMessage(
      error,
      '비교함 서버와 연결하지 못했습니다. 잠시 후 다시 시도해주세요.',
    );
  } finally {
    loading.value = false;
  }
}

async function removeItem(propertyId) {
  deletingId.value = propertyId;

  try {
    await comparisonApi.removeFromBox(propertyId);
    items.value = items.value.filter((item) => item.propertyId !== propertyId);
  } catch (error) {
    errorMessage.value = getApiErrorMessage(
      error,
      '비교함 서버와 연결하지 못해 삭제하지 못했습니다.',
    );
  } finally {
    deletingId.value = '';
  }
}

function markImageError(propertyId) {
  if (!imageErrorIds.value.includes(propertyId)) {
    imageErrorIds.value.push(propertyId);
  }
}

async function startCompare() {
  if (items.value.length < 2 || startingComparison.value) return;

  if (!hasDesiredLocation(comparisonWorkplace.value)) {
    locationMessage.value = '직주근접 비교 기준이 될 위치를 선택해주세요.';
    isLocationPickerOpen.value = true;
    return;
  }

  startingComparison.value = true;
  errorMessage.value = '';

  try {
    saveComparisonDraft();

    await router.push({
      path: '/compare',
      query: {
        propertyIds: items.value.map((item) => item.propertyId),
        workplaceLat: comparisonWorkplace.value.lat,
        workplaceLng: comparisonWorkplace.value.lng,
        workplaceName:
          comparisonWorkplace.value.name || comparisonWorkplace.value.address,
        priorities: comparisonPriorities.value
          .map((priority) => priority.criterion)
          .join(','),
      },
    });
  } catch (error) {
    errorMessage.value = getApiErrorMessage(
      error,
      '비교 화면을 열지 못했습니다. 잠시 후 다시 시도해주세요.',
    );
  } finally {
    startingComparison.value = false;
  }
}

async function loadDefaultComparisonSettings(restoredDraft = {}) {
  try {
    const preference = await preferenceApi.get();
    if (
      !restoredDraft.hasWorkplace &&
      !comparisonWorkplace.value &&
      hasDesiredLocation(preference?.workplace)
    ) {
      comparisonWorkplace.value = { ...preference.workplace };
    }

    if (
      !restoredDraft.hasPriorities &&
      !hasEditedPriorities.value &&
      Array.isArray(preference?.priorities)
    ) {
      comparisonPriorities.value = normalizePriorities(preference.priorities);
    }
  } catch (error) {
    if (error.response?.status !== 404 && import.meta.env.DEV) {
      console.warn('[api] comparison settings default failed:', error);
    }
  }
}

function normalizePriorities(priorities) {
  const validCriteria = new Set(
    PRIORITY_OPTIONS.map((option) => option.criterion),
  );
  const seen = new Set();

  return [...priorities]
    .sort((a, b) => Number(a?.priorityOrder) - Number(b?.priorityOrder))
    .filter((priority) => {
      const criterion = priority?.criterion;
      if (!validCriteria.has(criterion) || seen.has(criterion)) return false;
      seen.add(criterion);
      return true;
    })
    .slice(0, MAX_PRIORITY_SELECTIONS)
    .map((priority, index) => ({
      criterion: priority.criterion,
      priorityOrder: index + 1,
    }));
}

function restoreComparisonDraft() {
  if (typeof sessionStorage === 'undefined') {
    return { hasWorkplace: false, hasPriorities: false };
  }

  try {
    const rawDraft = sessionStorage.getItem(COMPARISON_DRAFT_KEY);
    if (!rawDraft) {
      return { hasWorkplace: false, hasPriorities: false };
    }

    const draft = JSON.parse(rawDraft);
    const restored = { hasWorkplace: false, hasPriorities: false };

    if (hasDesiredLocation(draft?.workplace)) {
      comparisonWorkplace.value = { ...draft.workplace };
      restored.hasWorkplace = true;
    }

    if (Array.isArray(draft?.priorities)) {
      comparisonPriorities.value = normalizePriorities(draft.priorities);
      hasEditedPriorities.value = true;
      restored.hasPriorities = true;
    }

    return restored;
  } catch (error) {
    sessionStorage.removeItem(COMPARISON_DRAFT_KEY);
    return { hasWorkplace: false, hasPriorities: false };
  }
}

function saveComparisonDraft() {
  if (typeof sessionStorage === 'undefined') return;

  sessionStorage.setItem(
    COMPARISON_DRAFT_KEY,
    JSON.stringify({
      workplace: comparisonWorkplace.value,
      priorities: comparisonPriorities.value,
    }),
  );
}

function criterionLabel(criterion) {
  return (
    PRIORITY_OPTIONS.find((option) => option.criterion === criterion)?.title ??
    criterion
  );
}

function openPrioritySheet() {
  draftPriorities.value = comparisonPriorities.value.map(
    (priority) => priority.criterion,
  );
  isPrioritySheetOpen.value = true;
}

function closePrioritySheet() {
  isPrioritySheetOpen.value = false;
}

function togglePriority(criterion) {
  const index = draftPriorities.value.indexOf(criterion);
  if (index >= 0) {
    draftPriorities.value.splice(index, 1);
  } else if (draftPriorities.value.length < MAX_PRIORITY_SELECTIONS) {
    draftPriorities.value.push(criterion);
  }
}

function priorityRank(criterion) {
  const index = draftPriorities.value.indexOf(criterion);
  return index < 0 ? null : index + 1;
}

function applyPriorities() {
  comparisonPriorities.value = draftPriorities.value.map(
    (criterion, index) => ({
      criterion,
      priorityOrder: index + 1,
    }),
  );
  hasEditedPriorities.value = true;
  priorityMessage.value = '';
  saveComparisonDraft();
  closePrioritySheet();
}

function hasDesiredLocation(workplace) {
  if (!workplace) return false;

  const hasLatitude =
    workplace.lat !== null &&
    workplace.lat !== undefined &&
    Number.isFinite(Number(workplace.lat));
  const hasLongitude =
    workplace.lng !== null &&
    workplace.lng !== undefined &&
    Number.isFinite(Number(workplace.lng));

  return hasLatitude && hasLongitude;
}

function openLocationPicker() {
  locationMessage.value = '';
  isLocationPickerOpen.value = true;
}

function selectWorkplace(location) {
  comparisonWorkplace.value = location;
  locationMessage.value = '';
  saveComparisonDraft();
}

function goPropertyListForAdd() {
  if (items.value.length >= 3) return;

  router.push({
    path: '/properties',
    query: { returnTo: 'compare-box' },
  });
}

function goBack() {
  if (history.length > 1) router.back();
  else router.push('/');
}

function formatTrade(item) {
  const deposit = formatMoney(item.deposit);
  const rent = formatMoney(item.monthlyRent);

  if (item.tradeType === '전세') return `전세 ${deposit}`;
  if (item.tradeType === '매매') return `매매 ${deposit}`;
  return `월세 ${deposit}/${rent}`;
}

function formatMoney(value) {
  if (value === null || value === undefined || value === '') return '-';
  return `${Number(value).toLocaleString('ko-KR')}만`;
}

function formatArea(areaM2) {
  if (!areaM2) return '면적 정보 없음';
  return `${Math.round(areaM2 / 3.3)}평`;
}

function formatFee(item) {
  const fee = item.maintenanceFee;
  if (fee === null || fee === undefined || fee === '') return '-';
  return `${Number(fee).toLocaleString('ko-KR')}만`;
}

function formatFloorInfo(floorInfo) {
  if (!floorInfo) return '층수 정보 없음';
  const head = String(floorInfo).split('/')[0].trim();
  if (!head) return '층수 정보 없음';
  if (head.endsWith('층') || head === '옥탑') return head;
  return `${head}층`;
}
</script>

<style scoped>
.cbox {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--bg);
}
.local-header {
  display: grid;
  grid-template-columns: 44px 1fr 68px;
  align-items: center;
  height: 52px;
  padding: 0 12px;
  background: var(--white);
  border-bottom: 1px solid var(--border);
  font-size: 16px;
  font-weight: 800;
  text-align: center;
}
.back {
  width: 36px;
  height: 36px;
  font-size: 28px;
  line-height: 1;
  color: var(--kb-dark-gray);
}
.refresh {
  font-size: 12px;
  font-weight: 700;
  color: var(--kb-gray);
}
.scroll-area {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}
.scroll-area :deep(.simplebar-content) {
  display: flex;
  flex-direction: column;
  padding: 0 16px;
}
.title {
  font-size: 16px;
  font-weight: 900;
}
.sub {
  margin-top: 6px;
  font-size: 12px;
  color: var(--kb-silver);
}
.state {
  margin-top: 16px;
  padding: 16px 14px;
  background: var(--white);
  border: 1px solid var(--border);
  border-radius: 14px;
  font-size: 13px;
  color: var(--kb-gray);
  line-height: 1.5;
}
.state.error {
  color: var(--danger);
}
.items {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-top: 16px;
}
.item {
  display: flex;
}
.item-card {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 15px;
  min-height: 126px;
  padding: 14px;
  background: #ffffff;
  border: 1px solid var(--border);
  border-radius: 18px;
  min-width: 0;
}
.thumb {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 96px;
  height: 96px;
  border-radius: 13px;
  background: var(--yellow-tint);
  border: 1px solid var(--border);
  overflow: hidden;
  flex-shrink: 0;
}
.thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.thumb > svg {
  width: 36px;
  height: 36px;
}
.item-texts {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.item-title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 18px;
  font-weight: 800;
  line-height: 1.25;
}
.item-sub {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
  line-height: 1.35;
  color: var(--kb-gray);
}
.item-price {
  font-size: 14px;
  font-weight: 700;
  line-height: 1.35;
}
.item-meta {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13px;
  line-height: 1.35;
  color: var(--kb-silver);
}
.remove {
  display: flex;
  flex-shrink: 0;
  padding: 4px;
  color: var(--kb-silver);
}
.remove svg {
  width: 18px;
  height: 18px;
}
.remove:disabled {
  opacity: 0.45;
}
.add-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  width: 100%;
  height: 78px;
  margin-top: 16px;
  border: 1.5px dashed var(--kb-gold);
  border-radius: 14px;
  background: var(--white);
  font-size: 16px;
  font-weight: 700;
  color: var(--kb-gold);
}
.add-btn svg {
  width: 18px;
  height: 18px;
}
.comparison-settings-card {
  overflow: hidden;
  margin-top: 16px;
  border: 1px solid #e5e5e5;
  border-radius: 18px;
  background: var(--white);
  box-shadow: 0 3px 12px rgba(0, 0, 0, 0.1);
}
.location-row {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  min-height: 76px;
  padding: 12px 16px;
  text-align: left;
}
.location-marker {
  position: relative;
  width: 20px;
  height: 20px;
  border-radius: 50% 50% 50% 0;
  background: #ffbc00;
  flex-shrink: 0;
  transform: rotate(-45deg);
}
.location-marker-dot {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--white);
  transform: translate(-50%, -50%);
}
.location-texts {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}
.location-label {
  font-size: 12px;
  font-weight: 500;
  color: var(--kb-silver);
}
.location-address {
  overflow: hidden;
  font-size: 16px;
  font-weight: 800;
  line-height: 1.35;
  color: #1a1a1a;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.location-address.placeholder {
  font-size: 15px;
  font-weight: 600;
  color: #b4b0a8;
}
.location-arrow {
  flex-shrink: 0;
  width: 18px;
  height: 18px;
  color: #9b9b9b;
}
.location-message {
  margin-top: 8px;
  font-size: 11.5px;
  color: var(--danger);
}
.priority-row {
  display: flex;
  align-items: center;
  gap: 7px;
  width: 100%;
  min-height: 46px;
  padding: 8px 16px;
  border-top: 1px solid #e5e5e5;
  overflow-x: auto;
  text-align: left;
  scrollbar-width: none;
}
.priority-row::-webkit-scrollbar {
  display: none;
}
.priority-row-icon {
  flex-shrink: 0;
  width: 14px;
  height: 14px;
}
.priority-row-icon path,
.priority-row-icon circle {
  stroke: #8c7950;
}
.priority-chip {
  display: flex;
  align-items: center;
  gap: 5px;
  height: 27px;
  padding: 4px 9px 4px 5px;
  border-radius: 100px;
  background: #fff6dc;
  flex-shrink: 0;
  font-size: 12.5px;
  font-weight: 700;
  color: #5e5547;
  white-space: nowrap;
}
.priority-number {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 19px;
  height: 19px;
  border-radius: 50%;
  background: var(--kb-yellow);
  flex-shrink: 0;
  font-size: 11px;
  font-weight: 800;
  color: #3f392f;
}
.priority-placeholder {
  font-size: 13px;
  color: var(--kb-silver);
  white-space: nowrap;
}
.priority-message {
  margin-top: 6px;
  font-size: 11.5px;
  color: var(--danger);
}
.sheet-note {
  margin-bottom: 12px;
  font-size: 11.5px;
  color: #8a8d8f;
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
  border: 1px solid var(--border);
  border-radius: 14px;
  background: var(--white);
  text-align: left;
}
.priority-card.on {
  padding: 11px 13px;
  border: 2px solid #ffdd80;
  background: var(--yellow-tint);
}
.priority-option-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 100px;
  background: rgba(255, 188, 0, 0.14);
  flex-shrink: 0;
}
.priority-texts {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}
.priority-title {
  font-size: 14px;
  font-weight: 800;
}
.priority-sub {
  font-size: 11.5px;
  color: var(--kb-silver);
}
.priority-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #ffdd80;
  flex-shrink: 0;
  font-size: 12.5px;
  font-weight: 800;
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
}
.btn-primary {
  flex: 1;
  padding: 13px;
  border: 0;
  border-radius: 12px;
  background: #ffdd80;
  font-size: 14px;
  font-weight: 700;
  color: #33302a;
}
.btn-primary:disabled {
  background: #eceae5;
  color: #b4b0a8;
}
.tip {
  display: flex;
  align-items: flex-start;
  gap: 7px;
  margin-top: 16px;
  padding: 12px 14px;
  background: var(--yellow-tint);
  border-radius: 12px;
  font-size: 12px;
  color: var(--kb-gray);
  line-height: 1.5;
}
.tip svg {
  flex-shrink: 0;
  margin-top: 2px;
}
.btn-cta {
  margin-top: 16px;
  color: var(--kb-dark-gray);
}
</style>
