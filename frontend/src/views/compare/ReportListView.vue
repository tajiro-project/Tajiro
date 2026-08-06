<template>
  <div class="reports">
    <PageHeader title="비교 리포트 보관함" />

    <div class="scroll-area">
      <div class="top-row">
        <p class="count">
          저장된 비교 리포트 <b>{{ reports.length }}건</b>
        </p>
        <span class="sort-label">최신순</span>
      </div>

      <p v-if="deleteError" class="action-error">{{ deleteError }}</p>
      <div v-if="loading" class="state">리포트를 불러오는 중이에요.</div>
      <div v-else-if="errorMessage" class="state error">{{ errorMessage }}</div>
      <div v-else-if="reports.length === 0" class="state">
        저장된 비교 리포트가 없어요.
      </div>

      <div v-else v-for="r in reports" :key="r.reportId" class="report-card">
        <div class="rc-head">
          <span class="rc-icon">
            <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
              <path
                d="M5.5 3v9M5.5 12l-2.2-2.2M5.5 12l2.2-2.2M12.5 15V6M12.5 6l-2.2 2.2M12.5 6l2.2 2.2"
                stroke="#545045"
                stroke-width="1.5"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
          </span>
          <div class="rc-titles">
            <p class="rc-title">{{ r.title }}</p>
            <p class="rc-date">{{ fmtDate(r.createdAt) }} 저장</p>
          </div>
        </div>

        <div class="rc-props">
          <div
            v-for="(pid, i) in r.comparedPropertyIds"
            :key="pid"
            class="rc-prop"
            :class="{ on: recommendedOf(r) === pid }"
          >
            <span class="rp-circle" :class="{ on: recommendedOf(r) === pid }">{{
              letters[i]
            }}</span>
            <p class="rp-name">{{ propName(r, pid) }}</p>
            <p class="rp-price">{{ propPrice(r, pid) }}</p>
          </div>
        </div>

        <p v-if="hasUpdatedProperty(r)" class="update-notice">
          매물 정보가 저장 이후 업데이트됐어요.
        </p>

        <p class="rc-summary">
          <svg
            class="summary-icon"
            width="16"
            height="16"
            viewBox="0 0 16 16"
            fill="none"
          >
            <path
              d="M8 1L9.9 6.1L15 8L9.9 9.9L8 15L6.1 9.9L1 8L6.1 6.1L8 1Z"
              fill="#ffbc00"
            />
          </svg>
          {{ summaryOf(r) }}
        </p>

        <div class="rc-actions">
          <button
            class="rc-del"
            :disabled="deletingId === r.reportId"
            @click="removeReport(r)"
          >
            삭제
          </button>
          <button class="rc-open" @click="openReport(r)">상세 보기 →</button>
        </div>
      </div>

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
        매물 정보는 현재 시점 기준으로 표시돼요. 매물이 계약 완료되면 카드에
        표시될 수 있어요.
      </p>

      <button class="btn-cta" @click="$router.push('/compare-box')">
        새 비교 시작하기
      </button>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import PageHeader from '@/components/PageHeader.vue';
import client, { getApiErrorMessage } from '@/api/client';

const router = useRouter();
const letters = ['A', 'B', 'C'];
const reports = ref([]);
const loading = ref(false);
const deletingId = ref('');
const errorMessage = ref('');
const deleteError = ref('');

onMounted(loadReports);

async function loadReports() {
  loading.value = true;
  errorMessage.value = '';
  deleteError.value = '';

  try {
    const response = await client.get('/users/me/comparison-reports');
    const payload = response.data;
    const nextReports = Array.isArray(payload)
      ? payload
      : (payload?.items ?? []);
    reports.value = nextReports.map((report) => ({
      ...report,
      comparedPropertyIds: [...(report.comparedPropertyIds ?? [])]
        .map(Number)
        .sort((a, b) => a - b),
    }));
  } catch (error) {
    errorMessage.value = getApiErrorMessage(
      error,
      '리포트 보관함 서버와 연결하지 못했습니다.',
    );
  } finally {
    loading.value = false;
  }
}

function fmtDate(d) {
  const dt = new Date(d);
  if (Number.isNaN(dt.getTime())) return '-';
  return `${dt.getFullYear()}.${String(dt.getMonth() + 1).padStart(2, '0')}.${String(dt.getDate()).padStart(2, '0')}`;
}
function getProperty(report, pid) {
  return report.comparedProperties?.find((p) => p.propertyId === pid);
}
function propName(report, pid) {
  return getProperty(report, pid)?.title ?? pid;
}
function propPrice(report, pid) {
  const p = getProperty(report, pid);
  if (!p) return '';
  if (p.monthlyRent === null || p.monthlyRent === undefined || p.monthlyRent === '') {
    return `${p.tradeType ?? '전세'} ${p.deposit}`;
  }
  return `${p.deposit}/${p.monthlyRent}`;
}
// 리포트 저장 날짜 이후 매물 정보가 업데이트됐는지 확인
function hasUpdatedProperty(report) {
  const savedAt = new Date(report.createdAt);
  if (Number.isNaN(savedAt.getTime())) return false;

  return (report.comparedProperties ?? []).some((property) => {
    const updatedAt = new Date(property.updatedDate);
    return !Number.isNaN(updatedAt.getTime()) && updatedAt > savedAt;
  });
}
function recommendedOf(r) {
  return r.aiRecommendedPropertyId ?? '';
}
function summaryOf(r) {
  return firstText(
    r.aiSummary,
    r.aiPropertySummaryText,
    'AI 코칭을 아직 불러오지 못했어요.',
  );
}

function firstText(...values) {
  return values.find((value) => String(value ?? '').trim()) ?? '';
}

async function removeReport(r) {
  deletingId.value = r.reportId;
  deleteError.value = '';

  try {
    await client.delete(`/users/me/comparison-reports/${r.reportId}`);
    reports.value = reports.value.filter((x) => x.reportId !== r.reportId);
  } catch (error) {
    deleteError.value = getApiErrorMessage(
      error,
      '리포트 삭제 중 오류가 발생했어요.',
    );
  } finally {
    deletingId.value = '';
  }
}
//상세 보기 버튼 눌렀을 때 리포트 상세 페이지로 이동
function openReport(r) {
  router.push({
    path: '/reports/' + r.reportId,
    query: hasUpdatedProperty(r) ? { aiRefresh: '1' } : {},
  });
}
</script>

<style scoped>
.reports {
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
.top-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.count {
  font-size: 12.5px;
  color: var(--kb-gray);
}
.count b {
  color: var(--text-primary);
}
.sort-label {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12.5px;
  font-weight: 700;
}
.action-error {
  margin-top: 12px;
  padding: 10px 12px;
  background: #fff4f4;
  border: 1px solid #ffd0d0;
  border-radius: 10px;
  color: var(--danger);
  font-size: 12px;
  line-height: 1.45;
}
.state {
  margin-top: 12px;
  padding: 16px 14px;
  background: var(--white);
  border: 1px solid var(--border);
  border-radius: var(--radius-card);
  font-size: 13px;
  color: var(--kb-gray);
  line-height: 1.5;
}
.state.error {
  color: var(--danger);
}
.report-card {
  margin-top: 12px;
  padding: 16px;
  background: var(--white);
  border: 1px solid var(--border);
  border-radius: 16px;
}
.rc-head {
  display: flex;
  align-items: center;
  gap: 10px;
}
.rc-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: var(--yellow-tint);
  flex-shrink: 0;
}
.rc-title {
  font-size: 14px;
  font-weight: 800;
}
.rc-date {
  margin-top: 2px;
  font-size: 11.5px;
  color: var(--kb-silver);
}
.rc-props {
  display: flex;
  gap: 8px;
  margin-top: 14px;
}
.rc-prop {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3px;
  padding: 10px 4px;
  background: #f1efea;
  border: 1px solid transparent;
  border-radius: 12px;
  min-width: 0;
}
.rc-prop.on {
  background: var(--yellow-tint);
  border: 1.5px solid var(--kb-yellow);
}
.rp-circle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: #e0ddd6;
  color: var(--kb-silver);
  font-size: 12px;
  font-weight: 800;
}
.rp-circle.on {
  background: var(--kb-yellow);
  color: var(--text-primary);
}
.rp-name {
  color: #8a8d8f;
  font-size: 10.5px;
  font-weight: 700;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
}
.rp-price {
  color: #8a8d8f;
  font-size: 11.5px;
  font-weight: 800;
}
.rc-prop.on .rp-name,
.rc-prop.on .rp-price {
  color: var(--text-primary);
}
.update-notice {
  margin-top: 10px;
  padding: 8px 10px;
  border-radius: 8px;
  background: #f1efea;
  color: #d64545;
  font-size: 11.5px;
  font-weight: 700;
}
.rc-summary {
  display: flex;
  align-items: flex-start;
  gap: 7px;
  margin-top: 12px;
  padding: 11px 12px;
  background: var(--yellow-tint);
  border-radius: 10px;
  font-size: 12px;
  line-height: 1.55;
}
.summary-icon {
  flex-shrink: 0;
  margin-top: 1px;
}
.rc-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 16px;
  margin-top: 12px;
}
.rc-del {
  font-size: 12.5px;
  color: var(--kb-silver);
}
.rc-del:disabled {
  opacity: 0.45;
  cursor: default;
}
.rc-open {
  font-size: 12.5px;
  font-weight: 800;
  color: #85714d;
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
.btn-cta {
  margin-top: 14px;
}
</style>
