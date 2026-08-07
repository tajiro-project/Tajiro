<template>
  <div class="cbox">

    <simplebar class="scroll-area">
      <h1 class="title">비교할 매물을 골라주세요 (최대 3개)</h1>
      <p class="sub">
        {{ checkedIds.length }}개 선택됨
        <template v-if="items.length < 3">
          · {{ 3 - items.length }}개 더 담을 수 있어요</template
        >
      </p>

      <div
        v-if="loading"
        class="state"
      >
        비교함을 불러오는 중이에요.
      </div>
      <div
        v-else-if="errorMessage"
        class="state error"
      >
        {{ errorMessage }}
      </div>
      <div
        v-else-if="items.length === 0"
        class="state"
      >
        아직 비교함에 담긴 매물이 없어요.
      </div>

      <ul
        v-else
        class="items"
      >
        <li
          v-for="item in items"
          :key="item.propertyId"
          class="item"
        >
          <button
            class="check"
            :class="{ on: checkedIds.includes(item.propertyId) }"
            type="button"
            @click="toggleCheck(item.propertyId)"
          >
            <svg
              width="12"
              height="12"
              viewBox="0 0 12 12"
              fill="none"
            >
              <path
                d="M2 6.5L4.7 9L10 3.5"
                stroke="#545045"
                stroke-width="1.8"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
          </button>

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
                {{ item.propertyType || '매물' }}<template v-if="item.buildingName?.trim()">
                  · {{ item.buildingName.trim() }}</template>
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
              @click="removeItem(item.propertyId)"
            >
              <svg
                width="14"
                height="14"
                viewBox="0 0 14 14"
                fill="none"
              >
                <path
                  d="M3.5 3.5L10.5 10.5M10.5 3.5L3.5 10.5"
                  stroke="#8a8d8f"
                  stroke-width="1.4"
                  stroke-linecap="round"
                />
              </svg>
            </button>
          </div>
        </li>
      </ul>

      <button
        class="add-btn"
        type="button"
        @click="goPropertyListForAdd"
      >
        <svg
          width="14"
          height="14"
          viewBox="0 0 14 14"
          fill="none"
        >
          <path
            d="M7 2v10M2 7h10"
            stroke="var(--kb-gold)"
            stroke-width="1.5"
            stroke-linecap="round"
          />
        </svg>
        매물 리스트에서 추가하기
      </button>
      <p
        v-if="toastMessage"
        class="toast-msg"
      >
        {{ toastMessage }}
      </p>

      <p class="tip">
        <svg
          width="14"
          height="14"
          viewBox="0 0 14 14"
          fill="none"
        >
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
        3개를 모두 담으면 AI가 가치관 기준으로 비교 코칭을 해드려요.
      </p>

      <button
        class="btn-cta"
        type="button"
        :disabled="checkedIds.length < 2"
        @click="startCompare"
      >
        비교 시작 ({{ checkedIds.length }}개)
      </button>
    </simplebar>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import simplebar from 'simplebar-vue';
import { getApiErrorMessage } from '@/api/client';
import { comparisonApi } from '@/api/services';

const router = useRouter();

const loading = ref(false);
const deletingId = ref('');
const errorMessage = ref('');
const items = ref([]);
const checkedIds = ref([]);
const imageErrorIds = ref([]);
const toastMessage = ref('');
let toastTimer = null;

onMounted(loadCompareBox);

async function loadCompareBox() {
  loading.value = true;
  errorMessage.value = '';

  try {
    const data = await comparisonApi.box();
    const payload = data?.data ?? data;
    const nextItems = Array.isArray(payload) ? payload : (payload?.items ?? []);
    items.value = nextItems.slice(0, 3);
    checkedIds.value = items.value.slice(0, 3).map((item) => item.propertyId);
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

function toggleCheck(id) {
  const index = checkedIds.value.indexOf(id);
  if (index >= 0) checkedIds.value.splice(index, 1);
  else if (checkedIds.value.length < 3) checkedIds.value.push(id);
}

async function removeItem(propertyId) {
  deletingId.value = propertyId;

  try {
    await comparisonApi.removeFromBox(propertyId);
    items.value = items.value.filter((item) => item.propertyId !== propertyId);
    checkedIds.value = checkedIds.value.filter((id) => id !== propertyId);
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

function startCompare() {
  if (checkedIds.value.length < 2) return;
  router.push({ path: '/compare', query: { propertyIds: checkedIds.value } });
}

function goPropertyListForAdd() {
  if (items.value.length >= 3) {
    showToast('비교함에는 최대 3개까지만 담을 수 있어요.');
    return;
  }

  router.push('/properties');
}

function showToast(message) {
  toastMessage.value = message;
  if (toastTimer) clearTimeout(toastTimer);
  toastTimer = setTimeout(() => {
    toastMessage.value = '';
    toastTimer = null;
  }, 2200);
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
  gap: 12px;
  margin-top: 16px;
}
.item {
  display: flex;
  align-items: center;
  gap: 10px;
}
.check {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  border-radius: 6px;
  border: 1.5px solid #d8d5cf;
  background: var(--white);
  flex-shrink: 0;
}
.check svg {
  opacity: 0;
}
.check.on {
  background: var(--kb-yellow);
  border-color: var(--kb-yellow);
}
.check.on svg {
  opacity: 1;
}
.item-card {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  background: #ffffff;
  border: 1px solid var(--border);
  border-radius: 14px;
  min-width: 0;
}
.thumb {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 12px;
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
  font-size: 14px;
  font-weight: 800;
}
.item-sub {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 11.5px;
  color: var(--kb-gray);
}
.item-price {
  font-size: 12.5px;
  font-weight: 700;
}
.item-meta {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 11px;
  color: var(--kb-silver);
}
.remove {
  display: flex;
  flex-shrink: 0;
  padding: 4px;
}
.remove:disabled {
  opacity: 0.45;
}
.toast-msg {
  position: fixed;
  left: 50%;
  bottom: 92px;
  z-index: 80;
  width: max-content;
  max-width: min(300px, calc(100% - 72px));
  transform: translateX(-50%);
  padding: 9px 12px;
  border-radius: 10px;
  background: rgba(33, 30, 24, 0.92);
  color: #fff;
  font-size: 11.5px;
  font-weight: 700;
  line-height: 1.4;
  text-align: center;
  white-space: pre-line;
  box-shadow: 0 8px 18px rgba(0, 0, 0, 0.16);
}
.add-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  height: 48px;
  margin-top: 16px;
  border: 1.5px dashed var(--kb-gold);
  border-radius: 14px;
  background: var(--white);
  font-size: 13.5px;
  font-weight: 700;
  color: var(--kb-gold);
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
