<template>
  <div class="cmp">
    <simplebar class="scroll-area">
      <div v-if="loading && !isReportMode" class="loading-state">
        <div class="loading-spinner" aria-hidden="true"></div>
        <p class="loading-title">AI 비교 리포트를 생성 중입니다</p>
        <p class="loading-text">가치관 기준으로 매물을 분석하고 있어요</p>
        <div class="loading-dots" aria-hidden="true">
          <span></span>
          <span></span>
          <span></span>
        </div>
      </div>
      <div v-else-if="loading" class="state">
        저장된 리포트를 불러오는 중이에요.
      </div>
      <div v-else-if="errorMessage" class="state error">
        {{ errorMessage }}
      </div>
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
            <p class="t-price">{{ formatTradePrice(item) }}</p>
            <span
              v-if="hasAiRecommendation && recommendedId === item.propertyId"
              class="t-badge"
              >AI 추천</span
            >
          </div>
        </div>

        <section class="ai-card">
          <p class="ai-head">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path
                d="M8 1L9.9 6.1L15 8L9.9 9.9L8 15L6.1 9.9L1 8L6.1 6.1L8 1Z"
                fill="#ffbc00"
              />
            </svg>
            AI 의사결정 코치
          </p>

          <p v-if="aiRecommendationLine" class="ai-recommendation-line">
            {{ aiRecommendationLine }}
          </p>
          <p
            v-if="aiPrimaryText"
            class="ai-p"
            :class="{ error: coachingError }"
          >
            {{ aiPrimaryText }}
          </p>
          <p v-if="aiSecondaryText" class="ai-p">
            {{ aiSecondaryText }}
          </p>
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
                  stroke="#85714D"
                  stroke-width="1.6"
                  stroke-linecap="round"
                />
              </svg>
              종합 비교
            </span>
            <span class="ph-right">
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
            <div class="score-tabs" role="tablist" aria-label="비교 지표 선택">
              <button
                v-for="spec in allScoreSpecs"
                :key="spec.key"
                class="score-tab"
                :class="{ active: selectedScoreSpec?.key === spec.key }"
                type="button"
                role="tab"
                :aria-selected="selectedScoreSpec?.key === spec.key"
                :disabled="!spec.available"
                @click="selectScore(spec.key)"
              >
                {{ spec.tabLabel }}
              </button>
            </div>
            <!-- 세로 막대그래프 템플릿 -->
            <div v-if="selectedScoreSpec" class="metric-chart">
              <div class="score-section-head">
                <strong>{{ selectedScoreTitle }}</strong>
                <span>높을수록 좋아요</span>
              </div>
              <div class="metric-chart-navigation">
                <!-- 터치 스와이프 처리 -->
                <div
                  class="metric-slide-viewport"
                  @touchstart.passive="handleMetricTouchStart"
                  @touchend.passive="handleMetricTouchEnd"
                >
                  <!-- 지표 전환 애니메이션 -->
                  <Transition :name="slideTransitionName">
                    <div :key="selectedScoreSpec.key" class="metric-bars">
                      <div
                        v-for="bar in selectedMetricBars"
                        :key="bar.propertyId"
                        class="metric-bar-column"
                      >
                        <div class="metric-bar-visual">
                          <div
                            class="metric-bar-fill"
                            :style="{
                              height: `${bar.score}%`,
                              background: selectedScoreSpec.color,
                            }"
                          >
                            <span class="metric-bar-score">{{
                              bar.score
                            }}</span>
                          </div>
                        </div>
                        <span class="metric-raw-label">
                          {{ bar.letter }} · {{ bar.rawLabel }}
                        </span>
                      </div>
                    </div>
                  </Transition>
                </div>
              </div>
            </div>

            <div class="cumulative-chart">
              <div class="score-section-head cumulative-head">
                <strong>5개 지표 누적 점수</strong>
                <span>총 500점</span>
              </div>
              <div
                v-for="(scores, propertyIndex) in seriesScores"
                :key="items[propertyIndex]?.propertyId ?? propertyIndex"
                class="cumulative-row"
              >
                <strong class="cumulative-letter">{{
                  letters[propertyIndex]
                }}</strong>
                <div class="cumulative-track">
                  <span
                    v-for="(score, scoreIndex) in scores"
                    :key="scoreSpecs[scoreIndex].key"
                    class="cumulative-segment"
                    :style="{
                      width: `${score / 5}%`,
                      background: scoreSpecs[scoreIndex].color,
                    }"
                  />
                </div>
                <strong class="cumulative-total">
                  {{ seriesTotals[propertyIndex] }}점
                </strong>
              </div>
            </div>

            <p v-if="showRecommendationMismatch" class="score-notice">
              <!-- {{ recommendationMismatchText }} -->
              AI 코칭 결과와 종합 비교 점수는 일치하지 않을 수 있습니다.
            </p>
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
                  stroke="#85714D"
                  stroke-width="1.3"
                  stroke-linejoin="round"
                />
                <path
                  d="M5.4 7.4l1.5 1.5 2.8-3"
                  stroke="#85714D"
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
                  <th v-for="(item, i) in items" :key="i">
                    {{ letters[i] }}
                  </th>
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
          </div>
        </section>

        <button
          v-if="!isReportMode"
          class="report-btn"
          type="button"
          :disabled="saving || !coaching?.reportId"
          @click="saveReport"
        >
          리포트 보관
        </button>
        <p v-if="savedMsg" class="saved-msg" :class="{ error: savedMsgError }">
          {{ savedMsg }}
        </p>
      </template>
    </simplebar>

    <Teleport to="body">
      <div v-if="showAiRefreshModal" class="modal-overlay">
        <div
          class="modal"
          role="dialog"
          aria-modal="true"
          aria-labelledby="ai-refresh-title"
        >
          <span class="m-icon">
            <svg width="22" height="22" viewBox="0 0 22 22" fill="none">
              <rect
                x="5"
                y="2.5"
                width="12"
                height="17"
                rx="2"
                stroke="#a8842c"
                stroke-width="1.6"
              />
              <path
                d="M8.5 7h5M8.5 10.5h5M8.5 14h3"
                stroke="#a8842c"
                stroke-width="1.4"
                stroke-linecap="round"
              />
            </svg>
          </span>
          <p id="ai-refresh-title" class="m-title">
            AI 코칭 업데이트가 필요해요
          </p>
          <p class="m-text">
            매물 정보가 저장 이후 변경됐어요.<br />
            현재 정보에 맞게 AI 코칭을<br />
            업데이트하시겠어요?
          </p>
          <div class="m-actions">
            <button
              class="m-later"
              type="button"
              :disabled="refreshingCoaching"
              @click="showAiRefreshModal = false"
            >
              다음에
            </button>
            <button
              class="m-go"
              type="button"
              :disabled="refreshingCoaching"
              @click="refreshAiCoaching"
            >
              {{ refreshingCoaching ? '업데이트 중...' : '업데이트하기' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import client, { getApiErrorMessage } from '@/api/client';
import { comparisonApi } from '@/api/services';
import simplebar from 'simplebar-vue';

const route = useRoute();
const router = useRouter();

const letters = ['A', 'B', 'C'];
const PRIORITY_KEYS = ['COMMUTE', 'COST', 'INFRA', 'AMENITY', 'AREA'];
const SCORE_KEYS = ['COMMUTE', 'COST', 'INFRA', 'MARKET', 'AMENITY'];
const AI_COACHING_UNAVAILABLE_MESSAGE =
  'AI 코칭을 불러오지 못했어요. 잠시 후 다시 시도해주세요.';
const CONVERSION_RATE = 0.053;
const items = ref([]);
const metrics = ref([]);

// AI 코칭 정보
const coaching = ref(null);
const coachingError = ref('');

const loading = ref(false);
const saving = ref(false);
const errorMessage = ref('');
const showOverall = ref(true);
const showSafety = ref(true);
const selectedScoreKey = ref('COMMUTE');
const slideDirection = ref(1);
const savedMsg = ref('');
const savedMsgError = ref(false);
const currentPropertyIds = ref([]);
const activeWorkplace = ref(null);
const activePriorities = ref([]);
const showAiRefreshModal = ref(false);
const refreshingCoaching = ref(false);
let metricTouchStartX = null;

const selectedIds = computed(() =>
  []
    .concat(route.query.propertyIds ?? route.query.ids ?? [])
    .flatMap((id) => String(id).split(','))
    .map((id) => id.trim())
    .filter(Boolean)
    .map(Number)
    .sort((a, b) => a - b)
    .slice(0, 3),
);
const comparisonWorkplace = computed(() => {
  const lat = Number(firstQueryValue(route.query.workplaceLat));
  const lng = Number(firstQueryValue(route.query.workplaceLng));

  if (
    !Number.isFinite(lat) ||
    !Number.isFinite(lng) ||
    lat < -90 ||
    lat > 90 ||
    lng < -180 ||
    lng > 180
  ) {
    return null;
  }

  return {
    lat,
    lng,
    name: firstQueryValue(route.query.workplaceName),
  };
});
const comparisonPriorities = computed(() => {
  return normalizePriorityKeys(
    String(firstQueryValue(route.query.priorities) ?? '').split(','),
  );
});
const reportId = computed(() =>
  String(route.params.reportId ?? route.query.reportId ?? ''),
);
const isReportMode = computed(() => Boolean(reportId.value));
// AI가 추천한 매물 객체
const aiRecommendedItem = computed(() => {
  const aiRecommendedPropertyId = coaching.value?.aiRecommendedPropertyId;
  if (aiRecommendedPropertyId == null) return null;
  return (
    items.value.find(
      (item) => String(item.propertyId) === String(aiRecommendedPropertyId),
    ) ?? null
  );
});
// AI 추천 매물이 있는지 여부
const hasAiRecommendation = computed(() => Boolean(aiRecommendedItem.value));
// AI가 추천한 매물 ID
const recommendedId = computed(() => aiRecommendedItem.value?.propertyId ?? '');
// AI 추천 매물 첫 줄 안내 문구
const aiRecommendationLine = computed(() => {
  if (coachingError.value || !aiRecommendedItem.value) return '';
  const recommendedIndex = items.value.findIndex(
    (item) => String(item.propertyId) === String(recommendedId.value),
  );
  const recommendedLetter = letters[recommendedIndex] ?? '';
  const title = aiRecommendedItem.value.title
    ? ` · ${aiRecommendedItem.value.title}`
    : '';
  return recommendedLetter
    ? `AI 추천 매물은 ${recommendedLetter}${title}입니다.`
    : '';
});
const aiPrimaryText = computed(() => {
  if (coachingError.value) return coachingError.value;
  return (
    coaching.value?.aiPropertySummaryText || AI_COACHING_UNAVAILABLE_MESSAGE
  );
});

const aiSecondaryText = computed(() => {
  if (coachingError.value) return '';
  const summary = coaching.value?.aiSummary ?? '';
  return summary === aiPrimaryText.value ? '' : summary;
});

const warningText = computed(() => {
  const worst = [...metrics.value]
    .filter(
      (metric) =>
        hasNumber(metric.evaluationScore) &&
        Math.abs(metric.evaluationScore) >= 10,
    )
    .sort(
      (a, b) => Math.abs(b.evaluationScore) - Math.abs(a.evaluationScore),
    )[0];
  if (!worst) return '';
  const index = Math.max(0, metrics.value.indexOf(worst));
  const score = worst.evaluationScore;
  const pct = Math.abs(Math.round(score));
  const direction = score > 0 ? '높아요' : '낮아요';
  return `${letters[index]} 매물은 주변 시세보다 ${pct}% ${direction}. 계약 전 시세와 관리비 항목을 확인하세요.`;
});

const allScoreSpecs = computed(() => {
  if (!metrics.value.length) return [];

  return [
    {
      key: 'COMMUTE',
      tabLabel: '직주',
      label: '직주근접',
      color: '#f2926d',
      available: metrics.value.every((m) => hasNumber(m.commuteMinutes)),
      values: metrics.value.map((m) => Number(m.commuteMinutes)),
      invert: true,
      formatRaw: (value) => `${formatCompactNumber(value)}분`,
    },
    {
      key: 'COST',
      tabLabel: '가격',
      label: '가격',
      color: '#ffca43',
      available: metrics.value.every(
        (m) => hasNumber(m.deposit) && hasNumber(m.monthlyRent),
      ),
      values: metrics.value.map((m) => monthlyCostValue(m)),
      invert: true,
      formatRaw: (value) => `${formatCompactNumber(value)}만원`,
    },
    {
      key: 'INFRA',
      tabLabel: '인프라',
      label: '인프라',
      color: '#78b58c',
      available: metrics.value.every((m) => hasNumber(m.infraCount)),
      values: metrics.value.map((m) => Number(m.infraCount)),
      invert: false,
      formatRaw: (value) => `${formatCompactNumber(value)}점`,
    },
    {
      key: 'MARKET',
      tabLabel: '시세',
      label: '시세안정',
      color: '#68b2c7',
      available: metrics.value.every((m) => hasNumber(m.evaluationScore)),
      values: metrics.value.map((m) => Math.abs(Number(m.evaluationScore))),
      invert: true,
      formatRaw: (value) => `${formatCompactNumber(value)}%`,
    },
    {
      key: 'AMENITY',
      tabLabel: '편의',
      label: '편의시설',
      color: '#8d82cc',
      available: metrics.value.every((m) => hasNumber(m.amenityCount)),
      values: metrics.value.map((m) => Number(m.amenityCount)),
      invert: false,
      formatRaw: (value) => `${formatCompactNumber(value)}점`,
    },
  ];
});

const scoreSpecs = computed(() =>
  allScoreSpecs.value.filter((spec) => spec.available),
);

// 차트 점수를 정규화하여 35~95점 사이로 변환
const seriesScores = computed(() => {
  if (!metrics.value.length || !scoreSpecs.value.length) return [];

  const normalizedByAxis = scoreSpecs.value.map((spec) =>
    normalize(spec.values, spec.invert),
  );

  return metrics.value.map((_, propertyIndex) =>
    normalizedByAxis.map((axisScores) => axisScores[propertyIndex]),
  );
});
//현재 선택된 지표 객체
const selectedScoreSpec = computed(
  () =>
    scoreSpecs.value.find((spec) => spec.key === selectedScoreKey.value) ??
    scoreSpecs.value[0] ??
    null,
);
const slideTransitionName = computed(() =>
  slideDirection.value > 0 ? 'metric-slide-next' : 'metric-slide-prev',
);
//우선순위 번호를 제목에 표시
const selectedScoreTitle = computed(() => {
  if (!selectedScoreSpec.value) return '';
  const priorityIndex = activePriorities.value.indexOf(
    selectedScoreSpec.value.key,
  );
  const priorityPrefix =
    priorityIndex >= 0 ? `${priorityIndex + 1}순위 · ` : '';
  return `${priorityPrefix}${selectedScoreSpec.value.label} 점수`;
});
const selectedMetricBars = computed(() => {
  if (!selectedScoreSpec.value) return [];
  const scoreIndex = scoreSpecs.value.findIndex(
    (spec) => spec.key === selectedScoreSpec.value.key,
  );

  return metrics.value.map((metric, propertyIndex) => ({
    propertyId: metric.propertyId,
    letter: letters[propertyIndex],
    score: seriesScores.value[propertyIndex]?.[scoreIndex] ?? 0,
    rawLabel: selectedScoreSpec.value.formatRaw(
      selectedScoreSpec.value.values[propertyIndex],
    ),
  }));
});
const seriesTotals = computed(() =>
  seriesScores.value.map((scores) =>
    scores.reduce((total, score) => total + score, 0),
  ),
);
const topCumulativePropertyIds = computed(() => {
  if (!seriesTotals.value.length) return [];
  const highestScore = Math.max(...seriesTotals.value);
  return items.value
    .filter((_, index) => seriesTotals.value[index] === highestScore)
    .map((item) => String(item.propertyId));
});
const showRecommendationMismatch = computed(
  () =>
    hasAiRecommendation.value &&
    !topCumulativePropertyIds.value.includes(String(recommendedId.value)),
);
// const recommendationMismatchText = computed(() => {
//   if (!showRecommendationMismatch.value) return '';

//   const recommendedIndex = items.value.findIndex(
//     (item) => String(item.propertyId) === String(recommendedId.value),
//   );
//   const topIndex = items.value.findIndex((item) =>
//     topCumulativePropertyIds.value.includes(String(item.propertyId)),
//   );
//   if (recommendedIndex < 0 || topIndex < 0) return '';

//   const recommendedLetter = letters[recommendedIndex];
//   const topLetter = letters[topIndex];
//   const priorityOrder = new Map(
//     activePriorities.value.map((key, index) => [key, index]),
//   );
//   const strongerMetrics = scoreSpecs.value
//     .map((spec, scoreIndex) => ({
//       key: spec.key,
//       label: spec.label,
//       difference:
//         (seriesScores.value[recommendedIndex]?.[scoreIndex] ?? 0) -
//         (seriesScores.value[topIndex]?.[scoreIndex] ?? 0),
//     }))
//     .filter((metric) => metric.difference > 0)
//     .sort((a, b) => {
//       const aPriority = priorityOrder.get(a.key) ?? Number.MAX_SAFE_INTEGER;
//       const bPriority = priorityOrder.get(b.key) ?? Number.MAX_SAFE_INTEGER;
//       return aPriority - bPriority || b.difference - a.difference;
//     })
//     .slice(0, 2)
//     .map((metric) => metric.label);

//   if (!strongerMetrics.length) {
//     return `AI 추천 ${recommendedLetter}는 사용자 우선순위와 코칭 내용을 반영한 결과이며, 5개 지표를 동일하게 합산한 누적 점수는 ${topLetter}가 가장 높습니다.`;
//   }

//   return `AI 추천 ${recommendedLetter}는 ${strongerMetrics.join('·')} 지표에서 ${topLetter}보다 앞섰지만, 5개 지표를 동일하게 합산한 누적 점수는 ${topLetter}가 가장 높습니다.`;
// });

function selectInitialScore(priorities) {
  const firstVisiblePriority = priorities.find((criterion) =>
    SCORE_KEYS.includes(criterion),
  );
  selectedScoreKey.value = firstVisiblePriority ?? 'COMMUTE';
}

function normalizePriorityKeys(priorities) {
  const allowed = new Set(PRIORITY_KEYS);
  const seen = new Set();

  return (priorities ?? [])
    .map((priority) => String(priority ?? '').trim())
    .filter((criterion) => {
      if (!allowed.has(criterion) || seen.has(criterion)) return false;
      seen.add(criterion);
      return true;
    })
    .slice(0, 3);
}

function selectScore(scoreKey) {
  const currentIndex = scoreSpecs.value.findIndex(
    (spec) => spec.key === selectedScoreSpec.value?.key,
  );
  const targetIndex = scoreSpecs.value.findIndex(
    (spec) => spec.key === scoreKey,
  );
  if (targetIndex < 0 || targetIndex === currentIndex) return;
  slideDirection.value = targetIndex > currentIndex ? 1 : -1;
  selectedScoreKey.value = scoreKey;
}
// 선택된 지표를 기준으로 이전/다음 지표로 전환
function selectAdjacentScore(direction) {
  if (scoreSpecs.value.length < 2 || !selectedScoreSpec.value) return;
  const currentIndex = scoreSpecs.value.findIndex(
    (spec) => spec.key === selectedScoreSpec.value.key,
  );
  const nextIndex =
    (currentIndex + direction + scoreSpecs.value.length) %
    scoreSpecs.value.length;
  slideDirection.value = direction;
  selectedScoreKey.value = scoreSpecs.value[nextIndex].key;
}

function handleMetricTouchStart(event) {
  metricTouchStartX = event.touches[0]?.clientX ?? null;
}

function handleMetricTouchEnd(event) {
  const touchEndX = event.changedTouches[0]?.clientX;
  if (metricTouchStartX == null || touchEndX == null) return;
  const distance = touchEndX - metricTouchStartX;
  metricTouchStartX = null;
  if (Math.abs(distance) < 40) return;
  selectAdjacentScore(distance < 0 ? 1 : -1);
}

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
      values: metrics.value.map((m) => m.policeNearestDistanceMeters ?? null),
      fmt: (v) => `${v.toLocaleString()}m`,
      better: 'min',
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
    const sorted = row.values
      .filter(Number.isFinite)
      .sort((a, b) => (row.better === 'max' ? b - a : a - b));
    const best = sorted[0];
    const second = sorted[1];
    return {
      label: row.label,
      cells: row.values.map((value) => ({
        text: Number.isFinite(value) ? row.fmt(value) : '정보 없음',
        tone: !Number.isFinite(value)
          ? 'plain'
          : value === best
            ? 'best'
            : value === second && row.values.length > 2
              ? 'mid'
              : 'plain',
      })),
    };
  });
});

onMounted(loadComparison);

// 매물 비교 화면에 필요한 데이터를 서버에서 불러오는 함수
async function loadComparison() {
  loading.value = true;
  errorMessage.value = '';
  currentPropertyIds.value = [];
  showAiRefreshModal.value = false;
  try {
    const savedReport = isReportMode.value
      ? await getReportDetail(reportId.value)
      : null;
    const workplace =
      comparisonWorkplace.value ?? getSavedReportWorkplace(savedReport);
    const priorities = isReportMode.value
      ? getSavedReportPriorities(savedReport)
      : comparisonPriorities.value;
    const propertyIds = (
      savedReport?.comparedPropertyIds ?? selectedIds.value
    ).slice(0, 3);

    currentPropertyIds.value = propertyIds;

    if (!propertyIds.length) {
      router.replace('/compare-box');
      return;
    }

    if (!isReportMode.value && (!workplace || !priorities.length)) {
      router.replace('/compare-box');
      return;
    }

    activeWorkplace.value = workplace;
    activePriorities.value = priorities;
    selectInitialScore(priorities);
    const metricsResult = await getMetrics(propertyIds, workplace);
    let coachingDto = null;
    coachingError.value = '';

    if (savedReport) {
      coachingDto = createReportCoaching(savedReport);
      if (!coachingDto.aiPropertySummaryText && !coachingDto.aiSummary) {
        coachingError.value = AI_COACHING_UNAVAILABLE_MESSAGE;
      }
    } else {
      try {
        coachingDto = await getCoaching(propertyIds, workplace, priorities);
      } catch (error) {
        coachingError.value = error.message;
      }
    }

    applyComparisonResult({
      metrics: metricsResult,
      coaching: coachingDto,
    });

    if (savedReport && shouldShowAiRefreshModal(savedReport)) {
      showAiRefreshModal.value = true;
    }
  } catch (error) {
    errorMessage.value = getApiErrorMessage(
      error,
      error?.message ??
        '비교 서버와 연결하지 못했습니다. 잠시 후 다시 시도해주세요.',
    );
  } finally {
    loading.value = false;
  }
}
function unwrapApiData(payload) {
  return payload?.data && payload?.statusCode ? payload.data : payload;
}

async function getReportDetail(id) {
  const response = await client.get(`/users/me/comparison-reports/${id}`);
  return unwrapApiData(response.data);
}

function createReportCoaching(report) {
  const propertySummary = report.aiPropertySummaryText ?? '';
  const summary = report.aiSummary ?? '';

  return {
    reportId: report.reportId,
    aiPropertySummaryText: propertySummary || summary,
    aiSummary: summary === propertySummary ? '' : summary,
    aiRecommendedPropertyId: report.aiRecommendedPropertyId,
    aiAtp: report.aiAtp,
  };
}

function getSavedReportWorkplace(report) {
  if (report?.workplaceLat == null || report?.workplaceLng == null) return null;
  const lat = Number(report?.workplaceLat);
  const lng = Number(report?.workplaceLng);
  if (!Number.isFinite(lat) || !Number.isFinite(lng)) return null;
  return { lat, lng };
}

function getSavedReportPriorities(report) {
  return normalizePriorityKeys(report?.priorities);
}

function shouldShowAiRefreshModal(report) {
  return route.query.aiRefresh === '1' || hasUpdatedReportProperty(report);
}

function hasUpdatedReportProperty(report) {
  const savedAt = new Date(report?.createdAt);
  if (Number.isNaN(savedAt.getTime())) return false;

  return (report?.comparedProperties ?? []).some((property) => {
    const updatedAt = new Date(property.updatedDate);
    return !Number.isNaN(updatedAt.getTime()) && updatedAt > savedAt;
  });
}

async function refreshAiCoaching() {
  if (!currentPropertyIds.value.length) return;

  refreshingCoaching.value = true;
  coachingError.value = '';
  try {
    coaching.value = await getCoaching(
      currentPropertyIds.value,
      activeWorkplace.value,
      activePriorities.value,
    );
    showAiRefreshModal.value = false;
  } catch (error) {
    coaching.value = null;
    coachingError.value = error.message;
    showAiRefreshModal.value = false;
  } finally {
    refreshingCoaching.value = false;
  }
}

async function getMetrics(propertyIds, workplace) {
  try {
    return unwrapApiData(await comparisonApi.metrics(propertyIds, workplace));
  } catch (error) {
    throw new Error(
      getApiErrorMessage(
        error,
        '비교 서버와 연결하지 못했습니다. 잠시 후 다시 시도해주세요.',
      ),
    );
  }
}

async function getCoaching(propertyIds, workplace, priorities) {
  try {
    return unwrapApiData(
      await comparisonApi.analyze(propertyIds, workplace, priorities),
    );
  } catch (error) {
    const message =
      error.response?.data?.message ?? AI_COACHING_UNAVAILABLE_MESSAGE;
    throw new Error(message);
  }
}

function firstQueryValue(value) {
  return Array.isArray(value) ? value[0] : value;
}

// 서버에서 받아온 비교 결과를 화면에 적용하는 함수
function applyComparisonResult(dto) {
  const rawMetrics = Array.isArray(dto?.metrics?.items)
    ? dto.metrics.items
    : [];
  const rawItems = Array.isArray(dto?.items) ? dto.items : [];

  metrics.value = rawMetrics.slice(0, 3);
  items.value = metrics.value
    .map((metric) =>
      createComparisonItem(
        metric,
        rawItems.find((item) => item.propertyId === metric.propertyId),
      ),
    )
    .filter((item) => item.propertyId)
    .slice(0, 3);

  coaching.value = dto?.coaching ?? null;
}

function createComparisonItem(metric, fallbackItem = {}) {
  return {
    ...fallbackItem,
    propertyId: metric.propertyId ?? fallbackItem.propertyId,
    title: metric.title ?? fallbackItem.title ?? metric.propertyId,
    propertyType: metric.propertyType ?? fallbackItem.propertyType,
    tradeType: metric.tradeType ?? fallbackItem.tradeType,
    deposit: metric.deposit ?? fallbackItem.deposit ?? 0,
    monthlyRent: metric.monthlyRent ?? fallbackItem.monthlyRent ?? 0,
    maintenanceFee: metric.maintenanceFee ?? fallbackItem.maintenanceFee,
    areaM2: metric.areaM2 ?? fallbackItem.areaM2,
    floorInfo: metric.floorInfo ?? fallbackItem.floorInfo,
  };
}

function formatTradePrice(item) {
  const tradeType = item.tradeType;
  const deposit = moneyLabel(item.deposit);
  const monthlyRent = moneyLabel(item.monthlyRent);

  if (tradeType === '월세') return `월세 ${deposit}/${monthlyRent}`;
  if (tradeType === '전세') return `전세 ${deposit}`;
  if (tradeType === '매매') return `매매 ${deposit}`;

  if (item.monthlyRent) {
    return `${tradeType ?? ''} ${deposit}/${monthlyRent}`.trim();
  }
  return `${tradeType ?? ''} ${deposit}`.trim();
}

function moneyLabel(value) {
  if (value === null || value === undefined || value === '') return '-';

  const manwon = Number(value);
  if (!Number.isFinite(manwon)) return '-';

  if (manwon >= 10000) {
    const eok = Math.floor(manwon / 10000);
    const rest = manwon % 10000;
    return rest === 0 ? `${eok}억` : `${eok}억 ${rest}`;
  }

  return String(manwon);
}

function normalize(values, invert = false) {
  if (!values.length) return [];
  const min = Math.min(...values);
  const max = Math.max(...values);
  if (min === max) return values.map(() => 70);
  return values.map((value) => {
    const ratio = (value - min) / (max - min);
    return Math.round(35 + (invert ? 1 - ratio : ratio) * 60);
  });
}

function hasNumber(value) {
  return (
    value !== null && value !== undefined && Number.isFinite(Number(value))
  );
}

async function saveReport() {
  saving.value = true;
  savedMsg.value = '';
  savedMsgError.value = false;
  try {
    await client.post('/users/me/comparison-reports', {
      reportId: coaching.value?.reportId,
    });
    savedMsg.value = '리포트를 보관함에 저장했어요.';
    router.push('/reports');
  } catch (error) {
    savedMsgError.value = true;
    savedMsg.value = getApiErrorMessage(
      error,
      '리포트 저장 중 오류가 발생했습니다.',
    );
    if (import.meta.env.DEV) {
      console.warn('[api] comparison report save failed:', error);
    }
  } finally {
    saving.value = false;
  }
}

function getReportPropertySummaryText() {
  return coaching.value?.aiPropertySummaryText ?? '';
}

function getReportSummaryText() {
  return coaching.value?.aiSummary ?? '';
}

function monthlyCostValue(item) {
  return (
    Number(item.monthlyRent) +
    (Number(item.deposit) * CONVERSION_RATE) / 12 +
    Number(item.maintenanceFee ?? 0)
  );
}
// 숫자를 천 단위로 구분하여 문자열로 반환
function formatCompactNumber(value) {
  const numericValue = Number(value);
  if (!Number.isFinite(numericValue)) return '-';
  return Number.isInteger(numericValue)
    ? numericValue.toLocaleString()
    : numericValue.toLocaleString(undefined, { maximumFractionDigits: 1 });
}

function goBack() {
  router.push(isReportMode.value ? '/reports' : '/compare-box');
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
.loading-state {
  display: flex;
  min-height: calc(100vh - 112px);
  padding-bottom: 64px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
}
.loading-spinner {
  width: 56px;
  height: 56px;
  margin-bottom: 24px;
  border: 5px solid #efefef;
  border-top-color: var(--kb-yellow);
  border-right-color: var(--kb-yellow);
  border-radius: 50%;
  animation: compare-spin 0.9s linear infinite;
}
.loading-title {
  margin: 0 0 8px;
  color: #24211d;
  font-size: 16px;
  font-weight: 800;
  line-height: 1.4;
}
.loading-text {
  margin: 0;
  color: #a8a29a;
  font-size: 13px;
  line-height: 1.45;
}
.loading-dots {
  display: flex;
  gap: 6px;
  justify-content: center;
  margin-top: 26px;
}
.loading-dots span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--kb-yellow);
  animation: compare-dot 1.2s ease-in-out infinite;
}
.loading-dots span:nth-child(2) {
  animation-delay: 0.16s;
}
.loading-dots span:nth-child(3) {
  animation-delay: 0.32s;
}
@keyframes compare-spin {
  to {
    transform: rotate(360deg);
  }
}
@keyframes compare-dot {
  0%,
  80%,
  100% {
    opacity: 0.35;
    transform: scale(0.8);
  }
  40% {
    opacity: 1;
    transform: scale(1);
  }
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
  background: #e9e7e2;
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
  color: var(--kb-gold);
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
  color: var(--kb-gold);
  margin-bottom: 10px;
}
.ai-p {
  font-size: 13px;
  line-height: 1.65;
  color: var(--kb-gray);
  margin-bottom: 10px;
}
.ai-recommendation-line {
  margin-bottom: 8px;
  color: #3f3b34;
  font-size: 13.5px;
  font-weight: 800;
  line-height: 1.45;
}
.ai-p:last-child {
  margin-bottom: 0;
}
.ai-p.error {
  color: var(--danger);
  font-weight: 700;
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
  color: #8a8d8f;
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
.score-tabs {
  display: flex;
  align-items: center;
  gap: 5px;
}
.score-tab {
  flex: 1;
  min-width: 0;
  height: 26px;
  padding: 0 4px;
  border: 1px solid transparent;
  border-radius: 999px;
  background: #f1eee7;
  color: #6f6a61;
  font-size: 10.5px;
  font-weight: 700;
}
.score-tab.active {
  border-color: #f2bd2f;
  background: #fff7d9;
  color: #80682b;
}
.score-tab:disabled {
  opacity: 0.42;
  cursor: default;
}
.score-section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 14px;
}
.score-section-head strong {
  font-size: 12px;
  font-weight: 800;
  color: #3f3b34;
}
.score-section-head span {
  font-size: 9.5px;
  color: #9b978f;
}
.metric-chart-navigation {
  margin-top: 16px;
}
.metric-slide-viewport {
  position: relative;
  min-width: 0;
  min-height: 182px;
  overflow: hidden;
  touch-action: pan-y;
}
.metric-bars {
  position: relative;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
  width: 100%;
  padding: 22px 4px 0;
}
.metric-bars::before {
  position: absolute;
  inset: 22px 0 22px;
  z-index: 0;
  background: repeating-linear-gradient(
    to bottom,
    #e9e6df 0,
    #e9e6df 1px,
    transparent 1px,
    transparent 44px
  );
  content: '';
  pointer-events: none;
}
.metric-bar-column {
  position: relative;
  z-index: 1;
  min-width: 0;
  text-align: center;
}
.metric-bar-visual {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  width: 100%;
  height: 132px;
  flex-direction: column;
}
.metric-bar-fill {
  position: relative;
  width: 36px;
  min-height: 2px;
  border-radius: 8px 8px 0 0;
  transition: height 0.2s ease;
}
.metric-bar-score {
  position: absolute;
  top: -20px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 11px;
  font-weight: 800;
  color: #36322c;
}
.metric-raw-label {
  display: block;
  margin-top: 6px;
  overflow: hidden;
  color: #777269;
  font-size: 10.5px;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.metric-slide-next-enter-active,
.metric-slide-next-leave-active,
.metric-slide-prev-enter-active,
.metric-slide-prev-leave-active {
  transition:
    transform 0.22s ease,
    opacity 0.22s ease;
}
.metric-slide-next-leave-active,
.metric-slide-prev-leave-active {
  position: absolute;
  inset: 0;
}
.metric-slide-next-enter-from {
  opacity: 0;
  transform: translateX(36px);
}
.metric-slide-next-leave-to {
  opacity: 0;
  transform: translateX(-36px);
}
.metric-slide-prev-enter-from {
  opacity: 0;
  transform: translateX(-36px);
}
.metric-slide-prev-leave-to {
  opacity: 0;
  transform: translateX(36px);
}
.cumulative-chart {
  margin-top: 20px;
  padding-top: 2px;
}
.cumulative-head {
  margin-bottom: 13px;
}
.cumulative-row {
  display: grid;
  grid-template-columns: 18px minmax(0, 1fr) 45px;
  gap: 7px;
  align-items: center;
  margin-top: 13px;
}
.cumulative-letter {
  font-size: 11px;
  color: #4b4740;
}
.cumulative-track {
  display: flex;
  width: 100%;
  height: 15px;
  overflow: hidden;
  background: #f0eee9;
}
.cumulative-segment {
  flex: 0 0 auto;
  height: 100%;
}
.cumulative-total {
  color: #8b7441;
  font-size: 10.5px;
  text-align: right;
}
.score-notice {
  margin-top: 18px;
  padding: 9px 11px;
  border-radius: 10px;
  background: #fff6d9;
  color: #786f5e;
  font-size: 10.5px;
  line-height: 1.45;
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
  border-radius: 99px;
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
  color: var(--kb-dark-gray);
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
.saved-msg.error {
  color: #d64545;
}
.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 120;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(33, 30, 24, 0.5);
  padding: 24px;
}
.modal {
  width: 100%;
  max-width: 300px;
  background: var(--white);
  border-radius: 20px;
  padding: 28px 20px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}
.m-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: var(--yellow-tint);
}
.m-title {
  margin-top: 16px;
  font-size: 16px;
  font-weight: 900;
}
.m-text {
  margin-top: 10px;
  font-size: 12.5px;
  line-height: 1.6;
  color: var(--kb-gray);
}
.m-actions {
  display: flex;
  gap: 8px;
  width: 100%;
  margin-top: 20px;
}
.m-later {
  flex: 0 0 84px;
  height: 44px;
  border: 1px solid var(--border);
  border-radius: 12px;
  background: var(--white);
  font-size: 13.5px;
  font-weight: 700;
}
.m-go {
  flex: 1;
  height: 44px;
  border-radius: 12px;
  background: var(--kb-yellow-header);
  font-size: 13.5px;
  font-weight: 800;
}
.m-go:disabled {
  opacity: 0.55;
  cursor: default;
}
</style>
