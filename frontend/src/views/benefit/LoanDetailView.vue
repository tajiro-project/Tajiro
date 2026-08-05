<template>
  <div v-if="product" class="loan-detail">
    <PageHeader title="대출상품 상세" />

    <div class="scroll-area">
      <!-- 상품 요약 (다크 카드) -->
      <div class="hero-card">
        <h1 class="hero-title">{{ product.productName }}</h1>
        <div class="hero-row">
          <div class="hero-col">
            <p class="hero-label">금리 (연)</p>
            <p class="hero-rate">{{ rateRange }}</p>
          </div>
          <div class="hero-col right">
            <p class="hero-label">금리 유형</p>
            <p class="hero-type">{{ rateInfo.base_type || '정보 없음' }}</p>
          </div>
        </div>
        <div class="hero-foot">
          한도 · {{ limitText }}
        </div>
      </div>

      <!-- 금리 안내 -->
      <section class="card">
        <div class="rate-head">
          <p class="card-head">금리 안내</p>
          <span class="rate-badge">{{ rateBadge }}</span>
        </div>
        <ul class="rate-list">
          <li v-for="item in rateItems" :key="item.label" class="rate-row">
            <div class="rate-item-main">
              <div>
                <p class="rate-name">{{ item.label }}</p>
                <p v-if="item.meta?.length" class="rate-subline">
                  <span v-for="meta in item.meta" :key="meta.label">
                    {{ meta.label }} {{ meta.value }}
                  </span>
                </p>
              </div>
              <strong v-if="item.highlight" class="rate-highlight">{{ item.highlight }}</strong>
            </div>
            <p v-if="item.value" class="rate-description">{{ item.value }}</p>
          </li>
        </ul>
      </section>

      <!-- 신청 대상 -->
      <section class="card">
        <p class="card-head">신청 대상</p>
        <ul class="targets">
          <li v-for="t in targets" :key="t" class="target">
            <span class="t-check"
              ><svg width="10" height="10" viewBox="0 0 12 12" fill="none">
                <path
                  d="M2 6.5L4.7 9L10 3.5"
                  stroke="#545045"
                  stroke-width="1.8"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                /></svg
            ></span>
            <span class="t-text">{{ t }}</span>
          </li>
        </ul>
      </section>

      <!-- 상품 정보 -->
      <section class="card">
        <p class="card-head">상품 정보</p>
        <dl class="info-rows">
          <div class="info-row">
            <dt>기준일</dt>
            <dd>{{ formatDate(product.referenceDate) }}</dd>
          </div>
          <div class="info-row">
            <dt>유효기간</dt>
            <dd>{{ formatDate(product.validEndDate, true) }}</dd>
          </div>
          <div class="info-row">
            <dt>판매상태</dt>
            <dd class="strong">{{ product.isActive ? '판매 중' : '판매 종료' }}</dd>
          </div>
        </dl>
      </section>

      <!-- 필요 서류 -->
      <section class="docs-card">
        <p class="docs-head">필요 서류</p>
        <template v-for="group in documentGroups" :key="group.category">
          <p class="docs-sub">{{ group.category }}</p>
          <ul class="docs">
            <li v-for="d in group.items" :key="d">{{ d }}</li>
          </ul>
        </template>
      </section>

      <p class="note">
        <svg width="13" height="13" viewBox="0 0 13 13" fill="none">
          <circle
            cx="6.5"
            cy="6.5"
            r="5.2"
            stroke="#8a8d8f"
            stroke-width="1.1"
          />
          <path
            d="M6.5 6v3M6.5 4v.2"
            stroke="#8a8d8f"
            stroke-width="1.2"
            stroke-linecap="round"
          />
        </svg>
        {{ rateInfo.note || '금리·조건은 기준일 시점 기준이며 실제 심사 결과와 다를 수 있어요.' }}
      </p>

      <button v-if="product.applicationUrl" class="btn-cta go-btn" @click="openApplication">바로가기</button>
    </div>

    <AppTabBar active="home" />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import PageHeader from '@/components/PageHeader.vue';
import AppTabBar from '@/components/AppTabBar.vue';
import { financeApi } from '@/api/services';

const route = useRoute();
const product = ref(null);

onMounted(async () => {
  product.value = await financeApi.detail(route.params.id);
});

function parseJson(value, fallback) {
  if (value == null || value === '') return fallback;
  if (typeof value !== 'string') return value;
  try {
    return JSON.parse(value);
  } catch {
    return fallback;
  }
}

const rateInfo = computed(() => parseJson(product.value?.rateDescription, {}));
const targets = computed(() => {
  const parsed = parseJson(product.value?.eligibility, []);
  return Array.isArray(parsed) ? parsed : [];
});
const documentGroups = computed(() => {
  const parsed = parseJson(product.value?.requiredDocs, []);
  if (!Array.isArray(parsed) || parsed.length === 0) {
    return [{ category: '공통 제출서류', items: ['정보 없음'] }];
  }
  return parsed.map((group, index) => ({
    category: group.category || `제출서류 ${index + 1}`,
    items: Array.isArray(group.items) && group.items.length ? group.items : ['정보 없음'],
  }));
});
const rateItems = computed(() => {
  const info = rateInfo.value;
  const detailItems = [];

  if (Array.isArray(info.details)) {
    info.details.forEach((detail, index) => {
      const parsedDetail = parseJson(detail, detail);
      if (parsedDetail && typeof parsedDetail === 'object') {
        detailItems.push({
          label: parsedDetail.category || parsedDetail.name || `세부 금리 ${index + 1}`,
          highlight: parsedDetail.final_rate || parsedDetail.finalRate || parsedDetail.rate,
          value: parsedDetail.description || parsedDetail.value,
          meta: [
            { label: '기준금리', value: parsedDetail.base_rate || parsedDetail.baseRate },
            { label: '가산금리', value: parsedDetail.spread_rate || parsedDetail.spreadRate },
          ].filter((meta) => meta.value),
        });
      } else if (parsedDetail) {
        detailItems.push({ label: `세부 금리 ${index + 1}`, value: parsedDetail });
      }
    });
  }

  if (detailItems.length) return detailItems;

  return [
    { label: '기준금리 유형', value: info.base_type },
    { label: '우대금리', value: info.preferential_rate },
    { label: '연체이자율', value: info.late_fee_rate },
    { label: '금리 안내', value: info.note },
  ].filter((item) => item.value);
});
const rateBadge = computed(() =>
  rateInfo.value.base_type?.includes('COFIX') ? 'COFIX 연동 변동금리' : '금리 정보',
);
const rateRange = computed(() => {
  const min = Number(product.value?.minRate);
  const max = Number(product.value?.maxRate);
  if (!min && !max) return '상담 후 결정';
  if (min === max) return `${min}%`;
  return `${min} ~ ${max}%`;
});
const limitText = computed(() => {
  const amount = product.value?.maxLimitAmount;
  if (amount != null && Number(amount) > 0) return `최고 ${Number(amount).toLocaleString()}만원`;
  return product.value?.loanLimit || '정보 없음';
});

function formatDate(value, prefix = false) {
  if (!value) return '정보 없음';
  const date = String(value).slice(0, 10).replaceAll('-', '.');
  return prefix ? `~ ${date}` : date;
}

function openApplication() {
  window.open(product.value.applicationUrl, '_blank', 'noopener,noreferrer');
}
</script>

<style scoped>
.loan-detail {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--bg);
}
.scroll-area {
  flex: 1;
  overflow-y: auto;
  padding: 14px 16px 20px;
}
.hero-card {
  background: #5d564a;
  border-radius: 18px;
  padding: 20px 18px 0;
  overflow: hidden;
}
.hero-title {
  font-size: 16px;
  font-weight: 800;
  line-height: 1.45;
  color: #fff;
}
.hero-row {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 14px;
  margin-top: 16px;
}
.hero-label {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.55);
}
.hero-rate {
  margin-top: 4px;
  font-size: 22px;
  font-weight: 900;
  color: var(--kb-yellow);
}
.hero-type {
  margin-top: 4px;
  font-size: 15px;
  font-weight: 800;
  color: #fff;
}
.hero-col.right {
  text-align: left;
}
.hero-foot {
  margin: 16px -18px 0;
  padding: 11px 18px;
  background: rgba(255, 255, 255, 0.1);
  font-size: 11.5px;
  color: rgba(255, 255, 255, 0.85);
}
.card {
  margin-top: 14px;
  padding: 16px;
  background: var(--white);
  border: 1px solid var(--border);
  border-radius: 16px;
}
.card-head {
  font-size: 14.5px;
  font-weight: 800;
}
.rate-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.rate-badge {
  padding: 5px 11px;
  border-radius: 100px;
  background: var(--yellow-tint);
  font-size: 11px;
  font-weight: 700;
  color: #8a6d1c;
}
.rate-list {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border-radius: 10px;
  background: var(--bg);
}
.rate-row {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
  padding: 10px 12px;
  border-bottom: 1px solid var(--border);
}
.rate-row:last-child {
  border-bottom: 0;
}
.rate-name {
  font-size: 12px;
  font-weight: 700;
  color: var(--text-primary);
}
.rate-item-main {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.rate-highlight {
  flex-shrink: 0;
  font-size: 11.5px;
  color: #9a7410;
}
.rate-subline {
  display: flex;
  gap: 10px;
  margin-top: 3px;
  font-size: 10.5px;
  color: var(--kb-silver);
}
.rate-description {
  font-size: 12.5px;
  font-weight: 600;
  line-height: 1.55;
  color: var(--text-primary);
}
.targets {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 12px;
}
.target {
  display: flex;
  align-items: flex-start;
  gap: 9px;
}
.t-check {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 17px;
  height: 17px;
  border-radius: 5px;
  background: var(--kb-yellow);
  flex-shrink: 0;
  margin-top: 2px;
}
.t-text {
  font-size: 12.5px;
  line-height: 1.6;
}
.info-rows {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.info-row {
  display: flex;
  justify-content: space-between;
}
.info-row dt {
  font-size: 12.5px;
  color: var(--kb-silver);
}
.info-row dd {
  font-size: 12.5px;
  font-weight: 500;
}
.info-row dd.strong {
  font-weight: 800;
}
.docs-card {
  margin-top: 14px;
  padding: 16px;
  background: var(--yellow-tint);
  border-radius: 16px;
}
.docs-head {
  font-size: 13.5px;
  font-weight: 800;
  color: #8a6d1c;
}
.docs-sub {
  margin-top: 10px;
  font-size: 12px;
  color: var(--kb-gray);
}
.docs {
  margin-top: 8px;
  display: flex;
  flex-direction: column;
  gap: 7px;
}
.docs li {
  position: relative;
  padding-left: 12px;
  font-size: 12.5px;
}
.docs li::before {
  content: '•';
  position: absolute;
  left: 0;
  color: #d9a800;
}
.note {
  display: flex;
  align-items: flex-start;
  gap: 7px;
  margin-top: 14px;
  padding: 12px 14px;
  background: #f1efea;
  border-radius: 12px;
  font-size: 11.5px;
  line-height: 1.55;
  color: var(--kb-gray);
}
.go-btn {
  margin-top: 14px;
  background: var(--kb-yellow);
}
</style>
