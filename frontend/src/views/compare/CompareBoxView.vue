<template>
  <div class="cbox">
    <simplebar class="scroll-area">
      <p v-if="usingDemoItems" class="demo-banner">
        예시 화면이에요 · 실제 내 비교함이 아니에요
      </p>
      <h1 class="title">비교할 매물을 확인해주세요 (최대 3개)</h1>
      <p class="sub">
        {{ items.length }}개 담겨 있어요
        <template v-if="items.length < 3">
          · {{ 3 - items.length }}개 더 담을 수 있어요</template
        >
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
              v-if="!usingDemoItems"
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
        담긴 매물은 내 가치관 기준으로 비교 코칭해드려요.
      </p>

    </simplebar>

    <div class="compare-actions">
      <button
        class="btn-cta preference-edit-btn"
        type="button"
        data-tour="comparebox-settings"
        @click="goPreferenceEdit"
      >
        가치관 설정 수정
      </button>
      <button
        class="btn-cta"
        type="button"
        data-tour="comparebox-start"
        :disabled="items.length < 2 || startingComparison"
        @click="startCompare"
      >
        {{
          startingComparison
            ? '비교 준비 중...'
            : `비교 시작 (${items.length}개)`
        }}
      </button>
    </div>

    <OnboardingSpotlight group-name="compare-box" :steps="ONBOARDING_STEPS['compare-box']" return-to="/mypage" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import simplebar from 'simplebar-vue';
import OnboardingSpotlight from '@/components/OnboardingSpotlight.vue';
import { ONBOARDING_STEPS } from '@/constants/onboardingSteps';
import { getApiErrorMessage } from '@/api/client';
import { comparisonApi } from '@/api/services';

const route = useRoute();
const router = useRouter();

const DEMO_ITEMS = [
  {
    propertyId: 'demo-1',
    title: 'e편한세상대전에코포레 2101호',
    propertyType: '아파트',
    tradeType: '전세',
    deposit: 35400,
    monthlyRent: 0,
    maintenanceFee: 13,
    areaM2: 84.97,
    floorInfo: '21/34층',
  },
  {
    propertyId: 'demo-2',
    title: '한밭리버파크 1502호',
    propertyType: '아파트',
    tradeType: '월세',
    deposit: 5000,
    monthlyRent: 65,
    maintenanceFee: 15,
    areaM2: 59.8,
    floorInfo: '15/25층',
  },
];

const loading = ref(false);
const deletingId = ref('');
const errorMessage = ref('');
const items = ref([]);
const usingDemoItems = ref(false);
const imageErrorIds = ref([]);
const startingComparison = ref(false);

onMounted(loadCompareBox);

async function loadCompareBox() {
  // 온보딩 다시보기(?demo=1)는 실제 상태와 무관하게 항상 예시로 보여준다 —
  // 실제 API를 아예 안 타서 백엔드/DB 상태에도 영향을 안 받는다.
  if (route.query.demo === '1') {
    items.value = DEMO_ITEMS;
    usingDemoItems.value = true;
    imageErrorIds.value = [];
    return;
  }

  loading.value = true;
  errorMessage.value = '';

  try {
    const data = await comparisonApi.box();
    const payload = data?.data ?? data;
    const nextItems = Array.isArray(payload) ? payload : (payload?.items ?? []);
    items.value = nextItems.slice(0, 3);
    usingDemoItems.value = false;
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

  if (usingDemoItems.value) {
    router.push('/compare?demo=1');
    return;
  }

  startingComparison.value = true;
  errorMessage.value = '';

  try {
    await router.push({
      path: '/compare',
      query: {
        propertyIds: items.value.map((item) => item.propertyId),
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

function goPreferenceEdit() {
  router.push({
    path: '/preferences/1',
    query: { returnTo: 'compare-box' },
  });
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

  const manwon = Number(value);
  if (!Number.isFinite(manwon)) return '-';

  if (manwon >= 10000) {
    const eok = Math.floor(manwon / 10000);
    const rest = manwon % 10000;
    return rest === 0 ? `${eok}억` : `${eok}억 ${rest}만`;
  }

  return `${manwon}만`;
}


function formatArea(areaM2) {
  if (!areaM2) return '면적 정보 없음';
  return `${Math.round(areaM2 / 3.3)}평`;
}

function formatFee(item) {
  const fee = item.maintenanceFee;
  if (fee === null || fee === undefined || fee === '') return '-';
  return `${Number(fee)}만`;
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
  padding: 16px 12px;
}
.scroll-area :deep(.simplebar-content) {
  display: flex;
  flex-direction: column;
  padding: 0;
}
.demo-banner {
  margin-bottom: 12px;
  padding: 9px 12px;
  background: #f1efea;
  border: 1px dashed var(--kb-silver);
  border-radius: 10px;
  font-size: 11.5px;
  font-weight: 700;
  color: var(--kb-gray);
  text-align: center;
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
.state + .add-btn {
  height: 64px;
  margin-top: 12px;
}
.add-btn svg {
  width: 18px;
  height: 18px;
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
.preference-edit-btn {
  border: 1px solid var(--border);
  background: var(--white);
}
.btn-cta {
  color: var(--kb-dark-gray);
}
.compare-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex-shrink: 0;
  padding: 12px 16px 16px;
  background: transparent;
}
</style>
