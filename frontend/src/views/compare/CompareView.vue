<template>
  <div class="cmp">
    <header class="local-header">
      <button class="back" type="button" aria-label="뒤로 가기" @click="goBack">
        ‹
      </button>
      <span>매물 비교</span>
      <button class="refresh" type="button" @click="loadComparison">
        새로고침
      </button>
    </header>

    <div class="scroll-area">
      <div v-if="loading" class="state">비교 결과를 불러오는 중이에요.</div>
      <div v-else-if="errorMessage" class="state error">{{ errorMessage }}</div>
      <div v-else-if="items.length < 2" class="state">
        비교할 매물이 부족해요. 비교함에서 2개 이상 선택해주세요.
      </div>

      <template v-else>
        <div class="target-row">
          <div
            v-for="(item, i) in items"
            :key="item.propertyId"
            class="target-card"
            :class="{ on: recommendedId === item.propertyId }"
          >
            <span
              class="t-circle"
              :class="{ on: recommendedId === item.propertyId }"
              >{{ letters[i] }}</span
            >
            <p class="t-name">{{ item.title }}</p>
            <p class="t-price">{{ item.deposit }}/{{ item.monthlyRent }}</p>
            <span v-if="recommendedId === item.propertyId" class="t-badge"
              >AI 추천</span
            >
          </div>
        </div>

        <section class="ai-card">
          <p class="ai-head">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path
                d="M8 1l1.4 4L14 6l-4 2.2L9.2 13 8 9.5 4 12l1.8-4L2 6l4.6-1L8 1z"
                fill="#ffbc00"
              />
            </svg>
            AI 의사결정 코치
          </p>
          <p class="ai-p">{{ coachIntro }}</p>
          <p class="ai-p">
            {{
              coaching?.aiPropertySummaryText ??
              '비교 코칭을 불러오는 중이에요...'
            }}
          </p>
          <p class="ai-p">{{ coaching?.aiSummary }}</p>
        </section>

        <p v-if="warningText" class="warn">
          <svg width="15" height="15" viewBox="0 0 15 15" fill="none">
            <path
              d="M7.5 1.8L14 13H1L7.5 1.8z"
              stroke="#8a7a55"
              stroke-width="1.2"
              stroke-linejoin="round"
            />
            <path
              d="M7.5 6v3.2M7.5 11v.2"
              stroke="#8a7a55"
              stroke-width="1.3"
              stroke-linecap="round"
            />
          </svg>
          {{ warningText }}
        </p>

        <section class="panel">
          <button
            class="panel-head"
            type="button"
            @click="showOverall = !showOverall"
          >
            <span class="ph-left">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path
                  d="M2 14V7M6 14V3M10 14V9M14 14V5"
                  stroke="#545045"
                  stroke-width="1.6"
                  stroke-linecap="round"
                />
              </svg>
              종합 비교
            </span>
            <span class="ph-right">
              가치관 우선순위
              <svg
                class="chev"
                :class="{ open: showOverall }"
                width="14"
                height="14"
                viewBox="0 0 14 14"
                fill="none"
              >
                <path
                  d="M3.5 5.5L7 9l3.5-3.5"
                  stroke="#545045"
                  stroke-width="1.5"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
            </span>
          </button>
          <div v-show="showOverall" class="panel-body">
            <div class="radar-wrap">
              <canvas ref="radarEl" class="radar-canvas" />
            </div>
            <div class="legend">
              <span v-for="(item, i) in items" :key="i" class="lg">
                <i class="dot" :style="{ background: colors[i].dot }" />
                {{ letters[i] }} {{ shortName(item.title) }}
              </span>
            </div>
            <div class="best-bar">
              <span class="bb-label">가장 높은 영역</span>
              <b class="bb-value">{{ bestAreaText }}</b>
            </div>
          </div>
        </section>

        <section class="panel">
          <button
            class="panel-head"
            type="button"
            @click="showSafety = !showSafety"
          >
            <span class="ph-left">
              <svg width="15" height="15" viewBox="0 0 15 15" fill="none">
                <path
                  d="M7.5 1.5l5 2.2v3.6c0 3.2-2.1 5-5 6.2-2.9-1.2-5-3-5-6.2V3.7l5-2.2z"
                  stroke="#545045"
                  stroke-width="1.3"
                  stroke-linejoin="round"
                />
                <path
                  d="M5.4 7.4l1.5 1.5 2.8-3"
                  stroke="#545045"
                  stroke-width="1.3"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
              안전 비교
            </span>
            <span class="ph-right">
              반경 500m 기준
              <svg
                class="chev"
                :class="{ open: showSafety }"
                width="14"
                height="14"
                viewBox="0 0 14 14"
                fill="none"
              >
                <path
                  d="M3.5 5.5L7 9l3.5-3.5"
                  stroke="#545045"
                  stroke-width="1.5"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
            </span>
          </button>
          <div v-show="showSafety" class="panel-body">
            <table class="safety-table">
              <thead>
                <tr>
                  <th></th>
                  <th v-for="(item, i) in items" :key="i">{{ letters[i] }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in safetyRows" :key="row.label">
                  <td class="row-label">{{ row.label }}</td>
                  <td v-for="(cell, i) in row.cells" :key="i">
                    <span class="cell" :class="cell.tone">{{ cell.text }}</span>
                  </td>
                </tr>
              </tbody>
            </table>
            <div class="win-bar">
              <span class="bb-label">항목 우위</span>
              <span class="win-chips">
                <span
                  v-for="(item, i) in items"
                  :key="i"
                  class="win-chip"
                  :class="{
                    top:
                      winCounts[i] === Math.max(...winCounts) &&
                      winCounts[i] > 0,
                  }"
                >
                  <b>{{ letters[i] }}</b> {{ winCounts[i] }}개
                </span>
              </span>
            </div>
          </div>
        </section>

        <button
          class="report-btn"
          type="button"
          :disabled="saving"
          @click="saveReport"
        >
          리포트 보관
        </button>
        <p v-if="savedMsg" class="saved-msg">{{ savedMsg }}</p>
      </template>
    </div>

    <AppTabBar active="compare" />
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import AppTabBar from '@/components/AppTabBar.vue';
import {
  Chart,
  Filler,
  LineElement,
  PointElement,
  RadarController,
  RadialLinearScale,
} from 'chart.js';
import client, { withMock } from '@/api/client';
import {
  mockAiCoaching,
  mockCompareBox,
  mockComparisonMetrics,
} from '@/api/mockData';

Chart.register(
  RadarController,
  RadialLinearScale,
  PointElement,
  LineElement,
  Filler,
);

const route = useRoute();
const router = useRouter();

const letters = ['A', 'B', 'C'];
const colors = [
  { dot: '#ffbc00', fill: 'rgba(255, 205, 60, 0.45)', line: '#f0b400' },
  { dot: '#88a860', fill: 'rgba(136, 168, 96, 0.45)', line: '#6f9048' },
  { dot: '#7aa8e8', fill: 'rgba(122, 168, 232, 0.30)', line: '#5c8fd6' },
];
const axes = ['직주근접', '가성비', '인프라', '안전', '시세안정'];

const items = ref([]);
const metrics = ref([]);

// AI 코칭 정보
const coaching = ref(null);

const loading = ref(false);
const saving = ref(false);
const errorMessage = ref('');
const showOverall = ref(true);
const showSafety = ref(true);
const radarEl = ref(null);
const savedMsg = ref('');
let chart = null;

const selectedIds = computed(() =>
  []
    .concat(route.query.propertyIds ?? route.query.ids ?? [])
    .map((id) => String(id).trim())
    .filter(Boolean)
    .slice(0, 3),
);
// AI가 추천한 매물 ID를 결정
const recommendedId = computed(() => {
  // AI 추천 매물이 비교 대상에 포함되어 있으면 그 ID를 반환
  const aiRecommendedId = coaching.value?.aiRecommendedPropertyId;

  if (items.value.some((item) => item.propertyId === aiRecommendedId)) {
    return aiRecommendedId;
  }

  // AI 추천 매물이 비교 대상에 없으면, 점수가 가장 높은 매물 ID를 반환
  return getBestScoredPropertyId() ?? items.value[0]?.propertyId ?? '';
});

const coachIntro = computed(
  () => "사용자 가치관 1순위인 '직주근접' 기준으로 비교했어요.",
);

const warningText = computed(() => {
  const worst = [...metrics.value]
    .filter((metric) => Math.abs(metric.evaluationScore ?? 0) >= 10)
    .sort(
      (a, b) =>
        Math.abs(b.evaluationScore ?? 0) - Math.abs(a.evaluationScore ?? 0),
    )[0];
  if (!worst) return '';
  const index = Math.max(0, metrics.value.indexOf(worst));
  const score = worst.evaluationScore ?? 0;
  const pct = Math.abs(Math.round(score));
  const direction = score > 0 ? '높아요' : '낮아요';
  return `${letters[index]} 매물은 주변 시세보다 ${pct}% ${direction}. 계약 전 시세와 관리비 항목을 확인하세요.`;
});

// 차트 점수를 정규화하여 35~95점 사이로 변환
const seriesScores = computed(() => {
  if (!metrics.value.length) return [];
  const commute = normalize(
    metrics.value.map((m) => m.commuteMinutes ?? 0),
    true,
  );
  const cost = normalize(
    metrics.value.map((m) => (m.monthlyRent ?? 0) + feeValue(m)),
    true,
  );
  const infra = normalize(metrics.value.map((m) => m.infraCount ?? 0));
  const safety = normalize(
    metrics.value.map(
      (m) => (m.cctvCountWithin500m ?? 0) + (m.bellCountWithin500m ?? 0),
    ),
  );
  const market = normalize(
    metrics.value.map((m) => Math.abs(m.evaluationScore ?? 0)),
    true,
  );
  return metrics.value.map((_, i) => [
    commute[i],
    cost[i],
    infra[i],
    safety[i],
    market[i],
  ]);
});

const bestAreaText = computed(() => {
  if (!seriesScores.value.length) return '';
  // 각 매물의 총점 계산
  const sums = seriesScores.value.map((scores) =>
    scores.reduce((acc, score) => acc + score, 0),
  );
  // 총점이 가장 높은 매물 찾기
  const best = sums.indexOf(Math.max(...sums));
  // 총점이 가장 높은 매물의 상위 2개 영역 찾기
  const top2 = axes
    .map((axis, i) => ({ axis, value: seriesScores.value[best][i] }))
    .sort((a, b) => b.value - a.value)
    .slice(0, 2)
    .map((item) => item.axis.slice(0, 2));
  return `${letters[best]} 매물 (${top2.join('·')} 우위)`;
});

const safetyRows = computed(() => {
  if (!metrics.value.length) return [];
  const rows = [
    {
      label: 'CCTV',
      values: metrics.value.map((m) => m.cctvCountWithin500m ?? 0),
      fmt: (v) => `${v}대`,
      better: 'max',
    },
    {
      label: '경찰·지구대',
      values: metrics.value.map((m) => m.policeCountWithin500m ?? 0),
      fmt: (v) => `${v}곳`,
      better: 'max',
    },
    {
      label: '보안등',
      values: metrics.value.map((m) => m.safetyLightCountWithin500m ?? 0),
      fmt: (v) => `${v}곳`,
      better: 'max',
    },
    {
      label: '안전 비상벨',
      values: metrics.value.map((m) => m.bellCountWithin500m ?? 0),
      fmt: (v) => `${v}개`,
      better: 'max',
    },
    {
      label: '아동안전지킴이집',
      values: metrics.value.map((m) => m.childrenCountWithin500m ?? 0),
      fmt: (v) => `${v}곳`,
      better: 'max',
    },
  ];
  return rows.map((row) => {
    // min, max에 따라 순서 정렬
    const sorted = [...row.values].sort((a, b) =>
      row.better === 'max' ? b - a : a - b,
    );
    // 순서 정렬 후 가장 높은 값과 두 번째로 높은 값을 가져옴
    const best = sorted[0];
    const second = sorted[1];
    return {
      label: row.label,
      cells: row.values.map((value) => ({
        text: row.fmt(value),
        // tone 결정: best, second, plain
        tone:
          value === best
            ? 'best'
            : value === second && row.values.length > 2
              ? 'mid'
              : 'plain',
      })),
    };
  });
});

const winCounts = computed(() => {
  const counts = items.value.map(() => 0);
  safetyRows.value.forEach((row) => {
    row.cells.forEach((cell, i) => {
      if (cell.tone === 'best') counts[i] += 1;
    });
  });
  return counts;
});

function getBestScoredPropertyId() {
  if (!items.value.length || !seriesScores.value.length) return '';
  const sums = seriesScores.value.map((scores) =>
    scores.reduce((acc, score) => acc + score, 0),
  );
  const bestIndex = sums.indexOf(Math.max(...sums));
  return items.value[bestIndex]?.propertyId ?? '';
}

onMounted(loadComparison);
watch(seriesScores, () => nextTick(scheduleRenderChart), { deep: true });
watch(showOverall, (open) => {
  if (open) nextTick(scheduleRenderChart);
});
watch(loading, (isLoading) => {
  if (!isLoading) nextTick(scheduleRenderChart);
});

// 매물 비교 화면에 필요한 데이터를 서버(또는 Mock)에서 불러오는 함수
async function loadComparison() {
  loading.value = true;
  errorMessage.value = '';
  try {
    // 선택된 매물 ID가 있으면 그 ID를 사용하고, 없으면 Mock 데이터에서 3개의 매물 ID를 가져옴
    const requestedIds = selectedIds.value.length
      ? selectedIds.value
      : mockCompareBox.slice(0, 3).map((item) => item.propertyId);
    const propertyIds = requestedIds.slice(0, 3);
    const metricsDto = unwrapApiData(
      await withMock(
        () =>
          client.get('/comparisons/metrics', {
            params: toPropertyIdParams(propertyIds),
          }),
        () => createMockComparisonResultDto(propertyIds).metrics,
      ),
    );
    const coachingDto = unwrapApiData(
      await withMock(
        () => client.post('/comparisons/analyze', { propertyIds }),
        () => createMockComparisonResultDto(propertyIds).coaching,
      ),
    );

    applyComparisonResult(
      { metrics: metricsDto, coaching: coachingDto },
      propertyIds,
    );
  } catch (error) {
    errorMessage.value = error?.message ?? '비교 결과를 불러오지 못했어요.';
  } finally {
    loading.value = false;
  }
}

function unwrapApiData(payload) {
  return payload?.data && payload?.statusCode ? payload.data : payload;
}
function toPropertyIdParams(propertyIds) {
  const params = new URLSearchParams();
  propertyIds.forEach((id) => params.append('propertyIds', id));
  return params;
}

function createMockComparisonResultDto(propertyIds) {
  const selectedItems = mockCompareBox
    .filter((item) => propertyIds.includes(item.propertyId))
    .slice(0, 3);
  const fallbackItems = selectedItems.length
    ? selectedItems
    : mockCompareBox.slice(0, 3);
  const fallbackIds = fallbackItems.map((item) => item.propertyId);
  const metricItems = (mockComparisonMetrics.items ?? []).filter((metric) =>
    fallbackIds.includes(metric.propertyId),
  );

  const recommendedPropertyId = fallbackIds.includes(
    mockAiCoaching.aiRecommendedPropertyId,
  )
    ? mockAiCoaching.aiRecommendedPropertyId
    : fallbackIds[0];

  return {
    items: fallbackItems,
    metrics: {
      items: metricItems,
    },
    coaching: {
      ...mockAiCoaching,
      aiRecommendedPropertyId: recommendedPropertyId,
    },
  };
}

// 서버에서 받아온 비교 결과를 화면에 적용하는 함수
function applyComparisonResult(dto, requestedIds) {
  const nextItems = Array.isArray(dto?.items) ? dto.items.slice(0, 3) : [];
  const fallbackDto = createMockComparisonResultDto(requestedIds);
  items.value = nextItems.length ? nextItems : fallbackDto.items;

  const propertyIds = items.value.map((item) => item.propertyId);
  const rawMetrics = Array.isArray(dto?.metrics?.items)
    ? dto.metrics.items
    : fallbackDto.metrics.items;

  metrics.value = propertyIds.map(
    (id) =>
      rawMetrics.find((metric) => metric.propertyId === id) ?? {
        propertyId: id,
      },
  );
  coaching.value = dto?.coaching ?? fallbackDto.coaching;
}

function normalize(values, invert = false) {
  const min = Math.min(...values);
  const max = Math.max(...values);
  if (min === max) return values.map(() => 70);
  return values.map((value) => {
    const ratio = (value - min) / (max - min);
    return Math.round(35 + (invert ? 1 - ratio : ratio) * 60);
  });
}

function scheduleRenderChart() {
  requestAnimationFrame(() => {
    renderChart();
  });
}

function renderChart() {
  if (!radarEl.value || !seriesScores.value.length) return;
  if (chart) chart.destroy();
  const ctx = radarEl.value.getContext('2d');
  if (!ctx) return;
  chart = new Chart(ctx, {
    type: 'radar',
    data: {
      labels: axes,
      datasets: seriesScores.value.map((scores, i) => ({
        data: scores,
        backgroundColor: colors[i].fill,
        borderColor: colors[i].line,
        borderWidth: 1.5,
        pointRadius: 2,
        pointBackgroundColor: colors[i].line,
      })),
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: { legend: { display: false }, tooltip: { enabled: false } },
      scales: {
        r: {
          min: 0,
          max: 100,
          ticks: { display: false, stepSize: 25 },
          grid: { color: '#e9e7e2' },
          angleLines: { color: '#e9e7e2' },
          pointLabels: {
            font: {
              size: 11,
              weight: 600,
              family: "'Noto Sans KR', sans-serif",
            },
            color: '#33302a',
          },
        },
      },
    },
  });
}

async function saveReport() {
  saving.value = true;
  savedMsg.value = '';
  try {
    const propertyIds = items.value.map((item) => item.propertyId);
    await withMock(
      () =>
        client.post('/comparison-reports', {
          title: `${items.value[0]?.title ?? '비교 리포트'} 외 ${Math.max(items.value.length - 1, 0)}건`,
          comparedPropertyIds: propertyIds,
          aiPropertySummaryText: coaching.value?.aiPropertySummaryText,
          aiSummary: coaching.value?.aiSummary,
          aiRecommendedPropertyId: coaching.value?.aiRecommendedPropertyId,
          saved: true,
          aiAtp: coaching.value?.aiAtp,
        }),
      { reportId: `R-${Date.now()}`, createdAt: new Date().toISOString() },
    );
    savedMsg.value = '리포트를 보관함에 저장했어요.';
  } catch (error) {
    savedMsg.value = error?.message ?? '리포트 저장 중 오류가 발생했어요.';
  } finally {
    saving.value = false;
  }
}

function shortName(title) {
  return String(title ?? '').split(' ')[0];
}

function feeValue(item) {
  return Number(
    item.maintenanceFee ?? item.maintainanceFee ?? item.maintenaceFee ?? 0,
  );
}

function goBack() {
  router.push('/compare-box');
}
</script>

<style scoped>
.cmp {
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
  padding: 16px 16px 20px;
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
.target-row {
  display: flex;
  gap: 8px;
}
.target-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 12px 6px;
  background: var(--white);
  border: 1px solid var(--border);
  border-radius: 14px;
  min-width: 0;
}
.target-card.on {
  background: var(--yellow-tint);
  border: 1.5px solid var(--kb-yellow);
}
.t-circle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: #eceae4;
  color: var(--kb-silver);
  font-size: 13px;
  font-weight: 800;
}
.t-circle.on {
  background: var(--kb-yellow);
  color: var(--text-primary);
}
.t-name {
  font-size: 11.5px;
  font-weight: 700;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
}
.t-price {
  font-size: 12px;
  font-weight: 800;
}
.t-badge {
  font-size: 10px;
  font-weight: 700;
  color: #a8842c;
}
.ai-card {
  margin-top: 14px;
  padding: 16px;
  background: var(--white);
  border: 1.5px solid var(--kb-yellow);
  border-radius: 16px;
}
.ai-head {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 14.5px;
  font-weight: 800;
  margin-bottom: 10px;
}
.ai-p {
  font-size: 13px;
  line-height: 1.65;
  margin-bottom: 10px;
}
.ai-p:last-child {
  margin-bottom: 0;
}
.warn {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-top: 12px;
  padding: 12px 14px;
  background: var(--yellow-tint);
  border-radius: 12px;
  font-size: 12px;
  line-height: 1.55;
  color: var(--kb-gray);
}
.panel {
  margin-top: 14px;
  background: var(--white);
  border: 1px solid var(--border);
  border-radius: 16px;
  overflow: hidden;
}
.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 14px 16px;
}
.ph-left {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14.5px;
  font-weight: 800;
}
.ph-right {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: var(--kb-gray);
}
.chev {
  transition: transform 0.15s;
}
.chev.open {
  transform: rotate(180deg);
}
.panel-body {
  padding: 0 16px 16px;
}
.radar-wrap {
  position: relative;
  height: 230px;
}
.legend {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-top: 8px;
  flex-wrap: wrap;
}
.lg {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 11px;
  color: var(--kb-gray);
}
.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}
.best-bar,
.win-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;
  padding: 10px 14px;
  background: var(--yellow-tint);
  border-radius: 10px;
}
.bb-label {
  font-size: 12px;
  color: var(--kb-gray);
}
.bb-value {
  font-size: 12.5px;
  font-weight: 800;
}
.safety-table {
  width: 100%;
  border-collapse: collapse;
}
.safety-table th {
  padding: 6px 4px;
  font-size: 12px;
  color: var(--kb-gray);
  font-weight: 700;
  text-align: center;
}
.safety-table td {
  padding: 7px 4px;
  text-align: center;
}
.row-label {
  text-align: left !important;
  font-size: 12.5px;
  color: var(--text-primary);
  white-space: nowrap;
}
.cell {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 52px;
  padding: 7px 10px;
  border-radius: 9px;
  font-size: 12px;
  font-weight: 700;
  background: #f1efea;
  color: var(--kb-silver);
}
.cell.best {
  background: var(--kb-yellow);
  color: var(--text-primary);
}
.cell.mid {
  background: var(--yellow-tint);
  color: var(--text-primary);
}
.win-chips {
  display: flex;
  gap: 6px;
}
.win-chip {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 100px;
  background: var(--white);
  border: 1px solid var(--border);
  font-size: 11.5px;
}
.win-chip.top {
  background: var(--kb-yellow);
  border-color: var(--kb-yellow);
  font-weight: 700;
}
.report-btn {
  width: 100%;
  height: 48px;
  margin-top: 16px;
  border: 1px solid var(--border);
  border-radius: 14px;
  background: var(--white);
  font-size: 14.5px;
  font-weight: 800;
}
.report-btn:disabled {
  opacity: 0.45;
  cursor: default;
}
.saved-msg {
  margin-top: 10px;
  text-align: center;
  font-size: 12.5px;
  color: #2f9e69;
}
</style>
