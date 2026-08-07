<template>
  <div
    class="policy-detail"
    v-if="policy"
  >
    <PageHeader title="정책 상세" />

    <simplebar class="scroll-area">
      <div class="head">
        <h1 class="title">{{ policy.title }}</h1>
        <p class="sub">{{ summary }}</p>
      </div>

      <!-- 지원 금액
      <div class="amount-bar">
        <span class="amount-label">지원 금액</span>
        <b class="amount-value">{{ formattedBenefitAmount }}</b>
      </div> -->

      <!-- 신청 정보 -->
      <section class="card">
        <p class="card-head">신청 정보</p>
        <dl class="rows">
          <div class="row">
            <dt>신청 기간</dt>
            <dd>{{ displayValue(policy.applicationPeriod) }}</dd>
          </div>
          <div class="row">
            <dt>신청 기관</dt>
            <dd>{{ displayValue(policy.agency) }}</dd>
          </div>
          <div class="row">
            <dt>신청 방법</dt>
            <dd>{{ displayValue(policy.applyMethod) }}</dd>
          </div>
        </dl>
      </section>

      <!-- 자격 조건 -->
      <section class="card">
        <ul class="conds">
          <li class="cond">
            <span class="c-check"
              ><svg
                width="11"
                height="11"
                viewBox="0 0 12 12"
                fill="none"
              >
                <path
                  d="M2 6.5L4.7 9L10 3.5"
                  stroke="#545045"
                  stroke-width="1.8"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                /></svg
            ></span>
            <span class="c-label">지원 지역</span>
            <b class="c-value">{{ displayValue(policy.region) }}</b>
          </li>
          <li class="cond">
            <span class="c-check"
              ><svg
                width="11"
                height="11"
                viewBox="0 0 12 12"
                fill="none"
              >
                <path
                  d="M2 6.5L4.7 9L10 3.5"
                  stroke="#545045"
                  stroke-width="1.8"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                /></svg
            ></span>
            <span class="c-label">나이 조건</span>
            <b class="c-value">{{ ageRange }}</b>
          </li>
        </ul>
      </section>

      <section
        v-if="policy.description"
        class="card"
      >
        <p class="card-head">정책 설명</p>
        <p class="description">{{ policy.description.trim() }}</p>
      </section>

      <!-- 필요 서류 -->
      <section class="docs-card">
        <p class="docs-head">필요 서류(정확한 정보는 신청 기관 참고)</p>
        <ul class="docs">
          <li
            v-for="d in docs"
            :key="d"
          >
            {{ d }}
          </li>
        </ul>
      </section>
    </simplebar>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import PageHeader from '@/components/PageHeader.vue';
import simplebar from 'simplebar-vue';
import { policyApi } from '@/api/services';

const route = useRoute();
const policy = ref(null);

onMounted(async () => {
  policy.value = await policyApi.detail(route.params.id);
});

const displayValue = (value) => {
  if (
    value === null ||
    value === undefined ||
    String(value).trim() === '' ||
    value === '-'
  ) {
    return '정보 없음';
  }
  return value;
};

const summary = computed(() =>
  displayValue(policy.value?.sumDescription || policy.value?.description),
);

const formattedBenefitAmount = computed(() => {
  const amount = policy.value?.benefitAmount;
  if (!amount) return '정보 없음';
  return amount.replace(/\s*\((\d+)개월\)/, ' × $1개월');
});

const ageRange = computed(() => {
  const min = policy.value?.minAge;
  const max = policy.value?.maxAge;
  if (min == null && max == null) return '정보 없음';
  if (min == null) return `만 ${max}세 이하`;
  if (max == null) return `만 ${min}세 이상`;
  return `만 ${min}세 ~ ${max}세`;
});

const docs = computed(() => {
  const raw = policy.value?.requiredDocuments;
  if (!raw || raw.trim() === '-') return ['정보 없음'];
  return raw
    .split(',')
    .map((s) => s.trim())
    .filter(Boolean);
});
</script>

<style scoped>
.policy-detail {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--bg);
}
.scroll-area {
  flex: 1;
  overflow-y: auto;
  padding-bottom: 20px;
}
.head {
  padding: 16px 16px 0;
}
.title {
  font-size: 18px;
  font-weight: 900;
}
.sub {
  margin-top: 6px;
  font-size: 12px;
  color: var(--kb-silver);
  white-space: pre-line;
  line-height: 1.5;
}
.description {
  font-size: 12.5px;
  line-height: 1.7;
  white-space: pre-line;
}
.amount-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 14px 16px 0;
  padding: 14px 16px;
  background: var(--kb-yellow);
  border-radius: 12px;
}
.amount-label {
  font-size: 13px;
  font-weight: 700;
  color: #6b5410;
}
.amount-value {
  font-size: 15px;
  font-weight: 900;
}
.card {
  margin: 14px 16px 0;
  padding: 16px;
  background: var(--white);
  border: 1px solid var(--border);
  border-radius: 16px;
}
.card-head {
  font-size: 14px;
  font-weight: 800;
  margin-bottom: 10px;
}
.rows {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.row {
  display: flex;
  gap: 12px;
}
.row dt {
  flex: 0 0 66px;
  font-size: 12.5px;
  color: var(--kb-silver);
}
.row dd {
  flex: 1;
  font-size: 12.5px;
  line-height: 1.5;
}
.conds {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.cond {
  display: flex;
  align-items: flex-start;
  gap: 9px;
}
.c-check {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  border-radius: 5px;
  background: var(--kb-yellow);
  flex-shrink: 0;
  margin-top: 1px;
}
.c-label {
  flex: 0 0 62px;
  font-size: 12.5px;
  color: var(--kb-gray);
}
.c-value {
  flex: 1;
  font-size: 12.5px;
  font-weight: 700;
  line-height: 1.5;
}
.docs-card {
  margin: 14px 16px 0;
  padding: 16px;
  background: var(--yellow-tint);
  border-radius: 16px;
}
.docs-head {
  font-size: 13px;
  font-weight: 800;
  color: #8a6d1c;
}
.docs {
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 7px;
}
.docs li {
  position: relative;
  padding-left: 12px;
  font-size: 12.5px;
  color: var(--text-primary);
}
.docs li::before {
  content: '•';
  position: absolute;
  left: 0;
  color: #d9a800;
}
</style>
