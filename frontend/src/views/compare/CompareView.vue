<template>
  <div class="cmp">
    <simplebar class="scroll-area">
      <div
        v-if="loading && !isReportMode"
        class="loading-state"
        role="status"
        aria-live="polite"
      >
        <div
          class="loading-progress"
          :style="{ '--loading-progress': `${loadingProgress * 3.6}deg` }"
          aria-hidden="true"
        >
          <strong>{{ loadingProgress }}%</strong>
        </div>
        <p class="loading-title">{{ currentLoadingStep.title }}</p>
        <p class="loading-text">데이터 양에 따라 잠시 시간이 걸릴 수 있어요</p>

        <ol
          class="loading-steps"
          aria-label="AI 비교 리포트 생성 단계"
        >
          <li
            v-for="(step, index) in LOADING_STEPS"
            :key="step.title"
            :class="{
              complete: index < loadingStage,
              current: index === loadingStage,
            }"
          >
            <span
              class="loading-step-icon"
              aria-hidden="true"
            >
              <span v-if="index < loadingStage">✓</span>
            </span>
            <span>{{ loadingStepText(step, index) }}</span>
          </li>
        </ol>
      </div>
      <div
        v-else-if="loading"
        class="state"
      >
        저장된 리포트를 불러오는 중이에요.
      </div>
      <div
        v-else-if="errorMessage"
        class="state error"
      >
        {{ errorMessage }}
      </div>
      <div
        v-else-if="items.length < 2"
        class="state"
      >
        비교할 매물이 부족해요. 비교함에서 2개 이상 선택해주세요.
      </div>

      <template v-else>
        <p
          v-if="isDemoMode"
          class="demo-banner"
        >
          예시 화면이에요 · 실제 내 비교 데이터가 아니에요
        </p>
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

        <section
          class="ai-card"
          data-tour="compare-ai-card"
        >
          <p class="ai-head">
            <svg
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
            AI 의사결정 코치
          </p>

          <p
            v-if="aiRecommendationLine"
            class="ai-recommendation-line"
          >
            {{ aiRecommendationLine }}
          </p>
          <p
            v-if="aiPrimaryText"
            class="ai-p"
            :class="{ error: coachingError }"
          >
            {{ aiPrimaryText }}
          </p>
          <div
            v-if="aiSecondaryText"
            class="ai-summary-box"
          >
            <span class="ai-summary-label">코칭 요약</span>
            <p>{{ aiSecondaryText }}</p>
          </div>
        </section>

        <p
          v-if="warningText"
          class="warn"
        >
          <svg
            width="15"
            height="15"
            viewBox="0 0 15 15"
            fill="none"
          >
            <path
              d="M7.5 1.8L14 13H1L7.5 1.8z"
              stroke="currentColor"
              stroke-width="1.2"
              stroke-linejoin="round"
            />
            <path
              d="M7.5 6v3.2M7.5 11v.2"
              stroke="currentColor"
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
              <svg
                width="16"
                height="16"
                viewBox="0 0 16 16"
                fill="none"
              >
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
          <div
            v-show="showOverall"
            class="panel-body"
          >
            <div
              class="score-tabs"
              role="tablist"
              aria-label="비교 지표 선택"
            >
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
            <ul
              v-if="marketStatusItems.length"
              class="score-notice market-status-notice"
              aria-live="polite"
            >
              <li
                v-for="item in marketStatusItems"
                :key="item"
              >
                {{ item }}
              </li>
            </ul>
            <!-- 세로 막대그래프 템플릿 -->
            <div
              v-if="selectedScoreSpec"
              class="metric-chart"
            >
              <div class="score-section-head">
                <strong>{{ selectedScoreTitle }}</strong>
                <span>높을수록 좋아요</span>
              </div>
              <p
                v-if="selectedScoreDescription"
                class="score-guidance"
              >
                {{ selectedScoreDescription }}
              </p>
              <div
                class="metric-chart-navigation"
                data-tour="compare-metric-tabs"
              >
                <!-- 터치 스와이프 처리 -->
                <div
                  class="metric-slide-viewport"
                  @touchstart.passive="handleMetricTouchStart"
                  @touchend.passive="handleMetricTouchEnd"
                >
                  <!-- 지표 전환 애니메이션 -->
                  <Transition :name="slideTransitionName">
                    <div
                      :key="selectedScoreSpec.key"
                      class="metric-bars"
                    >
                      <div
                        v-for="bar in selectedMetricBars"
                        :key="bar.propertyId"
                        class="metric-bar-column"
                      >
                        <div class="metric-bar-visual">
                          <div
                            class="metric-bar-fill"
                            :style="{
                              height:
                                bar.score === null ? '0%' : `${bar.score}%`,
                              background: selectedScoreSpec.color,
                            }"
                          >
                            <span
                              class="metric-bar-score"
                              :class="{ unevaluated: bar.score === null }"
                            >
                              {{ bar.score === null ? '미평가' : bar.score }}
                            </span>
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
                <strong
                  >{{ cumulativeScoreSpecs.length }}개 지표 누적 점수</strong
                >
                <span>총 {{ cumulativeTotalMax }}점</span>
              </div>
              <div
                v-for="(scores, propertyIndex) in cumulativeSeriesScores"
                :key="items[propertyIndex]?.propertyId ?? propertyIndex"
                class="cumulative-row"
              >
                <strong class="cumulative-letter">{{
                  letters[propertyIndex]
                }}</strong>
                <div class="cumulative-track">
                  <span
                    v-for="(score, scoreIndex) in scores"
                    :key="cumulativeScoreSpecs[scoreIndex].key"
                    class="cumulative-segment"
                    :title="getCumulativeSegmentTitle(score, scoreIndex)"
                    :style="{
                      width: getCumulativeSegmentWidth(score),
                      background: cumulativeScoreSpecs[scoreIndex].color,
                    }"
                  />
                </div>
                <strong class="cumulative-total">
                  {{ seriesTotals[propertyIndex] }}점
                </strong>
              </div>
            </div>

            <p
              v-if="showRecommendationMismatch"
              class="score-notice"
            >
              AI 추천이 종합 점수와 다를 수 있으며, 그 이유는 코칭 요약에 함께
              설명돼요.
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
              <svg
                width="15"
                height="15"
                viewBox="0 0 15 15"
                fill="none"
              >
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
          <div
            v-show="showSafety"
            class="panel-body"
          >
            <table
              class="safety-table"
              data-tour="compare-safety-table"
            >
              <thead>
                <tr>
                  <th></th>
                  <th
                    v-for="(item, i) in items"
                    :key="i"
                  >
                    {{ letters[i] }}
                  </th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="row in safetyRows"
                  :key="row.label"
                >
                  <td class="row-label">{{ row.label }}</td>
                  <td
                    v-for="(cell, i) in row.cells"
                    :key="i"
                  >
                    <span
                      class="cell"
                      :class="cell.tone"
                      >{{ cell.text }}</span
                    >
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
        <p
          v-if="savedMsg"
          class="saved-msg"
          :class="{ error: savedMsgError }"
        >
          {{ savedMsg }}
        </p>
      </template>
    </simplebar>

    <Teleport to="body">
      <div
        v-if="showAiRefreshModal"
        class="modal-overlay"
      >
        <div
          class="modal"
          role="dialog"
          aria-modal="true"
          aria-labelledby="ai-refresh-title"
        >
          <span class="m-icon">
            <svg
              width="22"
              height="22"
              viewBox="0 0 22 22"
              fill="none"
            >
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
          <p
            id="ai-refresh-title"
            class="m-title"
          >
            AI 코칭 업데이트가 필요해요
          </p>
          <p class="m-text">
            매물 또는 시세 정보가 저장 이후 변경됐어요.<br />
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

    <OnboardingSpotlight
      group-name="compare"
      :steps="ONBOARDING_STEPS.compare"
      return-to="/mypage"
    />
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import client, { getApiErrorMessage } from '@/api/client';
import { comparisonApi, preferenceApi } from '@/api/services';
import simplebar from 'simplebar-vue';
import OnboardingSpotlight from '@/components/OnboardingSpotlight.vue';
import { ONBOARDING_STEPS } from '@/constants/onboardingSteps';

const route = useRoute();
const router = useRouter();

const letters = ['A', 'B', 'C'];
const PRIORITY_KEYS = ['COMMUTE', 'COST', 'INFRA', 'AMENITY', 'AREA'];
const SCORE_KEYS = ['COMMUTE', 'COST', 'INFRA', 'MARKET', 'AMENITY'];
const LOADING_STEPS = [
  {
    title: '비교할 매물을 확인하고 있어요',
    label: '매물과 비교 조건 확인',
    done: '매물과 비교 조건을 확인했어요',
  },
  {
    title: '기존 리포트와 최신 데이터를 확인하고 있어요',
    label: '기존 리포트와 최신 데이터 확인',
    done: '기존 리포트와 최신 데이터를 확인했어요',
  },
  {
    title: '실거래가·시세 지표를 분석하고 있어요',
    label: '실거래가·시세 지표 분석',
    done: '실거래가·시세 지표를 분석했어요',
  },
  {
    title: '가치관 기준 AI 리포트를 작성하고 있어요',
    label: '가치관 기준 AI 리포트 작성',
    done: '가치관 기준 AI 리포트를 작성했어요',
  },
];
const LOADING_STAGE_DELAYS = [600, 1800, 3500];
const LOADING_PROGRESS_VALUES = [18, 42, 68, 92];
const AI_COACHING_UNAVAILABLE_MESSAGE =
  'AI 코칭을 불러오지 못했어요. 잠시 후 다시 시도해주세요.';
const CONVERSION_RATE = 0.053;
const items = ref([]);
const metrics = ref([]);

// AI 코칭 정보
const coaching = ref(null);
const coachingError = ref('');

const loading = ref(false);
const loadingStage = ref(0);
const loadingProgress = computed(
  () => LOADING_PROGRESS_VALUES[loadingStage.value] ?? 92,
);
const currentLoadingStep = computed(
  () => LOADING_STEPS[loadingStage.value] ?? LOADING_STEPS[0],
);
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
let loadingStageTimers = [];

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
const reportId = computed(() =>
  String(route.params.reportId ?? route.query.reportId ?? ''),
);
const isReportMode = computed(() => Boolean(reportId.value));
const isDemoMode = computed(() => route.query.demo === '1');
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
  return formatKoreanMoneyText(
    coaching.value?.aiPropertySummaryText || AI_COACHING_UNAVAILABLE_MESSAGE,
  );
});

const aiSecondaryText = computed(() => {
  if (coachingError.value) return '';
  const summary = formatKoreanMoneyText(coaching.value?.aiSummary ?? '');
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
const MARKET_STATUS_MESSAGES = {
  DATA_INSUFFICIENT: '최근 비교 가능한 실거래가가 3건 미만이에요.',
  NOT_SYNCED: '실거래가 수집이 아직 완료되지 않았어요.',
  SYNC_ERROR:
    '실거래가 수집 중 오류가 발생했어요. 다음 동기화에서 다시 시도해요.',
  SOURCE_UNMAPPED: '건물 유형을 확인해야 시세를 계산할 수 있어요.',
  REGION_CODE_MISSING: '지역 코드가 없어 시세를 계산할 수 없어요.',
};

const PRESERVED_MARKET_STATUSES = new Set(['NOT_SYNCED', 'SYNC_ERROR']);

const marketStatusItems = computed(() =>
  metrics.value
    .map((metric, index) => {
      const status = metric.marketStatus;
      const message = MARKET_STATUS_MESSAGES[status];
      if (message) {
        const preservedSuffix =
          hasNumber(metric.evaluationScore) &&
          PRESERVED_MARKET_STATUSES.has(status)
            ? ' 기존 계산값을 표시하고 있어요.'
            : '';
        return `${letters[index]} 매물: ${message}${preservedSuffix}`;
      }
      return hasNumber(metric.evaluationScore)
        ? ''
        : `${letters[index]} 매물: 시세 데이터를 확인할 수 없어요.`;
    })
    .filter(Boolean),
);

const allScoreSpecs = computed(() => {
  if (!metrics.value.length) return [];

  return [
    {
      key: 'COMMUTE',
      scoreKey: 'commuteScore',
      tabLabel: '직주',
      label: '직주근접',
      color: '#f2926d',
      available: metrics.value.every((m) => hasNumber(m.commuteScore)),
      values: metrics.value,
      description: commuteDescription,
      formatRaw: formatCommuteTime,
    },
    {
      key: 'COST',
      scoreKey: 'costScore',
      tabLabel: '가격',
      label: '가격',
      color: '#ffca43',
      available: metrics.value.every((m) => hasNumber(m.costScore)),
      values: metrics.value,
      description: '타지로 내부 기준으로 가격 조건 적합도를 계산한 점수예요.',
      formatRaw: formatTradePrice,
    },
    {
      key: 'INFRA',
      scoreKey: 'infraScore',
      tabLabel: '인프라',
      label: '인프라',
      color: '#78b58c',
      available: metrics.value.every((m) => hasNumber(m.infraScore)),
      values: metrics.value.map((m) => Number(m.infraCount)),
      description: '선택한 인프라 항목 중 반경 2km 안에 있는 항목 수예요.',
      formatRaw: (value) => `${formatCompactNumber(value)}개 충족`,
    },
    {
      key: 'MARKET',
      tabLabel: '시세',
      label: '시세안정',
      color: '#68b2c7',
      available: metrics.value.some((m) => hasNumber(m.evaluationScore)),
      includeInTotal: metrics.value.every((m) => hasNumber(m.evaluationScore)),
      values: metrics.value.map((m) =>
        hasNumber(m.evaluationScore) ? Number(m.evaluationScore) : null,
      ),
      scores: metrics.value.map((m) =>
        hasNumber(m.evaluationScore)
          ? calculateMarketStabilityScore(m.evaluationScore)
          : null,
      ),
      description: '주변 시세와 차이가 작을수록 높은 점수예요.',
      formatRaw: (value) =>
        hasNumber(value) ? formatSignedPercent(value) : '데이터 부족',
    },
    {
      key: 'AMENITY',
      scoreKey: 'amenityScore',
      tabLabel: '편의',
      label: '편의시설',
      color: '#8d82cc',
      available: metrics.value.every((m) => hasNumber(m.amenityScore)),
      values: metrics.value.map((m) => Number(m.amenityCount)),
      description: '선택한 편의시설 항목 중 반경 2km 안에 있는 항목 수예요.',
      formatRaw: (value) => `${formatCompactNumber(value)}개 충족`,
    },
  ];
});
const scoreSpecs = computed(() =>
  allScoreSpecs.value.filter((spec) => spec.available),
);

const cumulativeScoreSpecs = computed(() =>
  scoreSpecs.value.filter((spec) => spec.includeInTotal !== false),
);

const cumulativeTotalMax = computed(
  () => cumulativeScoreSpecs.value.length * 100,
);

// 맞춤 기준은 백엔드 점수를 사용하고, 시세는 주변 시세 대비 절대 편차로 계산
const seriesScores = computed(() => {
  if (!metrics.value.length || !scoreSpecs.value.length) return [];

  const scoresByAxis = scoreSpecs.value.map((spec) =>
    spec.scoreKey
      ? metrics.value.map((metric) =>
          hasNumber(metric[spec.scoreKey])
            ? Number(metric[spec.scoreKey])
            : null,
        )
      : (spec.scores ?? normalize(spec.values, spec.invert)),
  );

  return metrics.value.map((_, propertyIndex) =>
    scoresByAxis.map((axisScores) => axisScores[propertyIndex]),
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
const selectedScoreDescription = computed(() => {
  const description = selectedScoreSpec.value?.description;
  return typeof description === 'function'
    ? description()
    : (description ?? '');
});
const selectedMetricBars = computed(() => {
  if (!selectedScoreSpec.value) return [];
  const scoreIndex = scoreSpecs.value.findIndex(
    (spec) => spec.key === selectedScoreSpec.value.key,
  );

  return metrics.value.map((metric, propertyIndex) => ({
    propertyId: metric.propertyId,
    letter: letters[propertyIndex],
    score: seriesScores.value[propertyIndex]?.[scoreIndex] ?? null,
    rawLabel: selectedScoreSpec.value.formatRaw(
      selectedScoreSpec.value.values[propertyIndex],
    ),
  }));
});
const cumulativeSeriesScores = computed(() => {
  if (!metrics.value.length || !cumulativeScoreSpecs.value.length) return [];

  const scoresByAxis = cumulativeScoreSpecs.value.map((spec) =>
    spec.scoreKey
      ? metrics.value.map((metric) =>
          hasNumber(metric[spec.scoreKey])
            ? Number(metric[spec.scoreKey])
            : null,
        )
      : (spec.scores ?? normalize(spec.values, spec.invert)),
  );

  return metrics.value.map((_, propertyIndex) =>
    scoresByAxis.map((axisScores) => axisScores[propertyIndex]),
  );
});

const seriesTotals = computed(() =>
  cumulativeSeriesScores.value.map((scores) =>
    scores.reduce((total, score) => total + (score ?? 0), 0),
  ),
);
function getCumulativeSegmentWidth(score) {
  const axisCount = cumulativeScoreSpecs.value.length;
  return axisCount && score !== null ? `${score / axisCount}%` : '0%';
}

function getCumulativeSegmentTitle(score, scoreIndex) {
  const spec = cumulativeScoreSpecs.value[scoreIndex];
  const label = spec?.label ?? '지표';
  return score === null
    ? `${label} · 미평가`
    : `${label} · ${Math.round(score)}점`;
}
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
      zeroIsLowest: true,
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
      zeroIsLowest: true,
    },
    {
      label: '안전 비상벨',
      values: metrics.value.map((m) => m.bellCountWithin500m ?? 0),
      fmt: (v) => `${v}개`,
      better: 'max',
      zeroIsLowest: true,
    },
    {
      label: '아동안전지킴이집',
      values: metrics.value.map((m) => m.childrenCountWithin500m ?? 0),
      fmt: (v) => `${v}곳`,
      better: 'max',
      zeroIsLowest: true,
    },
  ];
  return rows.map((row) => {
    const rankedValues = [
      ...new Set(
        row.values
          .filter(Number.isFinite)
          .filter((value) => !(row.zeroIsLowest && value === 0)),
      ),
    ].sort((a, b) => (row.better === 'max' ? b - a : a - b));

    const getTone = (value) => {
      if (!Number.isFinite(value)) return 'plain';
      if (row.zeroIsLowest && value === 0) return 'plain';

      const denseRank = rankedValues.indexOf(value) + 1;
      if (denseRank === 1) return 'best';
      if (denseRank === 2) return 'mid';
      return 'plain';
    };

    return {
      label: row.label,
      cells: row.values.map((value) => ({
        text: Number.isFinite(value) ? row.fmt(value) : '정보 없음',
        tone: getTone(value),
      })),
    };
  });
});
onMounted(loadComparison);
onBeforeUnmount(stopLoadingStages);

function startLoadingStages() {
  stopLoadingStages();
  loadingStage.value = 0;
  loadingStageTimers = LOADING_STAGE_DELAYS.map((delay, index) =>
    window.setTimeout(() => {
      if (loading.value && !isReportMode.value) {
        loadingStage.value = Math.min(index + 1, LOADING_STEPS.length - 1);
      }
    }, delay),
  );
}

function stopLoadingStages() {
  loadingStageTimers.forEach((timerId) => window.clearTimeout(timerId));
  loadingStageTimers = [];
}

function loadingStepText(step, index) {
  if (index < loadingStage.value) return step.done;
  if (index === loadingStage.value) return step.title;
  return step.label;
}

// 온보딩 다시보기 전용 목데이터 — 비교함에 실제로 매물을 안 담아도 화면을 볼 수 있게 함.
// 실제 API를 하나도 안 타서, 백엔드/DB 상태와 무관하게 항상 정상 렌더링된다.
function loadDemoComparison() {
  items.value = [
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
  metrics.value = [
    {
      propertyId: 'demo-1',
      commuteMinutes: 18,
      deposit: 35400,
      monthlyRent: 0,
      maintenanceFee: 13,
      infraCount: 12,
      amenityCount: 9,
      evaluationScore: -4.2,
      marketStatus: null,
      commuteScore: 82,
      costScore: 58,
      infraScore: 71,
      amenityScore: 65,
      cctvCountWithin500m: 6,
      policeNearestDistanceMeters: 320,
      safetyLightCountWithin500m: 14,
      bellCountWithin500m: 3,
      childrenCountWithin500m: 1,
    },
    {
      propertyId: 'demo-2',
      commuteMinutes: 27,
      deposit: 5000,
      monthlyRent: 65,
      maintenanceFee: 15,
      infraCount: 8,
      amenityCount: 11,
      evaluationScore: 2.1,
      marketStatus: null,
      commuteScore: 54,
      costScore: 76,
      infraScore: 60,
      amenityScore: 73,
      cctvCountWithin500m: 3,
      policeNearestDistanceMeters: 540,
      safetyLightCountWithin500m: 7,
      bellCountWithin500m: 1,
      childrenCountWithin500m: 0,
    },
  ];
  coaching.value = {
    reportId: null,
    aiRecommendedPropertyId: 'demo-1',
    aiPropertySummaryText:
      'A 매물은 통근 시간과 안전 지표에서 앞서 있어, 출퇴근 부담을 줄이고 싶다면 더 적합해요.',
    aiSummary: 'A 매물이 직주근접·안전 지표에서 우세해요.',
    aiAtp: null,
  };
  activeWorkplace.value = { lat: 36.35, lng: 127.38 };
  activePriorities.value = ['COMMUTE', 'COST'];
  selectInitialScore(activePriorities.value);
  currentPropertyIds.value = ['demo-1', 'demo-2'];
}

// 매물 비교 화면에 필요한 데이터를 서버에서 불러오는 함수
async function loadComparison() {
  if (route.query.demo === '1') {
    loadDemoComparison();
    return;
  }

  loading.value = true;
  if (!isReportMode.value) startLoadingStages();
  errorMessage.value = '';
  currentPropertyIds.value = [];
  showAiRefreshModal.value = false;
  try {
    const savedReport = isReportMode.value
      ? await getReportDetail(reportId.value)
      : null;
    const savedPreference = isReportMode.value
      ? null
      : await preferenceApi.get();
    const workplace = isReportMode.value
      ? getSavedReportWorkplace(savedReport)
      : getPreferenceWorkplace(savedPreference);
    const priorities = isReportMode.value
      ? getSavedReportPriorities(savedReport)
      : getPreferencePriorities(savedPreference);
    const propertyIds = (
      savedReport?.comparedPropertyIds ?? selectedIds.value
    ).slice(0, 3);

    currentPropertyIds.value = propertyIds;

    if (!propertyIds.length) {
      router.replace('/compare-box');
      return;
    }

    if (!isReportMode.value && !workplace) {
      router.replace('/compare-box');
      return;
    }

    activeWorkplace.value = workplace;
    activePriorities.value = priorities;
    selectInitialScore(priorities);
    let metricsResult = null;
    let coachingDto = null;
    coachingError.value = '';

    if (savedReport) {
      metricsResult = await getMetrics(propertyIds, workplace, false);
      coachingDto = createReportCoaching(savedReport);
      if (!coachingDto.aiPropertySummaryText && !coachingDto.aiSummary) {
        coachingError.value = AI_COACHING_UNAVAILABLE_MESSAGE;
      }
    } else {
      try {
        coachingDto = await getCoaching(propertyIds);
      } catch (error) {
        coachingError.value = error.message;
      }
      loadingStage.value = LOADING_STEPS.length - 1;
      metricsResult = await getMetrics(propertyIds, null, false);
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
    stopLoadingStages();
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

function getPreferenceWorkplace(preference) {
  const lat = Number(preference?.workplace?.lat);
  const lng = Number(preference?.workplace?.lng);
  if (!Number.isFinite(lat) || !Number.isFinite(lng)) return null;
  return {
    lat,
    lng,
    name: preference?.workplace?.name ?? preference?.workplace?.address,
  };
}

function getPreferencePriorities(preference) {
  return normalizePriorityKeys(
    [...(preference?.priorities ?? [])]
      .sort((a, b) => Number(a?.priorityOrder) - Number(b?.priorityOrder))
      .map((priority) => priority?.criterion),
  );
}

function shouldShowAiRefreshModal(report) {
  return route.query.aiRefresh === '1' || hasUpdatedReportData(report);
}

function hasUpdatedReportData(report) {
  return hasUpdatedReportProperty(report) || hasUpdatedMarketData(report);
}

function hasUpdatedReportProperty(report) {
  const savedAt = new Date(report?.createdAt);
  if (Number.isNaN(savedAt.getTime())) return false;

  return (report?.comparedProperties ?? []).some((property) => {
    const updatedAt = new Date(property.updatedDate);
    return !Number.isNaN(updatedAt.getTime()) && updatedAt > savedAt;
  });
}

function hasUpdatedMarketData(report) {
  const savedAt = new Date(report?.createdAt);
  const calculatedAt = new Date(report?.latestMarketCalculatedAt);
  return (
    !Number.isNaN(savedAt.getTime()) &&
    !Number.isNaN(calculatedAt.getTime()) &&
    calculatedAt > savedAt
  );
}

async function refreshAiCoaching() {
  if (!currentPropertyIds.value.length) return;

  refreshingCoaching.value = true;
  coachingError.value = '';
  try {
    const updatedCoaching = await getCoaching(currentPropertyIds.value);
    const updatedMetrics = await getMetrics(
      currentPropertyIds.value,
      activeWorkplace.value,
      false,
    );
    applyComparisonResult({
      metrics: updatedMetrics,
      coaching: updatedCoaching,
    });
    showAiRefreshModal.value = false;
  } catch (error) {
    coaching.value = null;
    coachingError.value = error.message;
    showAiRefreshModal.value = false;
  } finally {
    refreshingCoaching.value = false;
  }
}

async function getMetrics(propertyIds, workplace, refreshMarketScore = true) {
  try {
    return unwrapApiData(
      await comparisonApi.metrics(propertyIds, workplace, refreshMarketScore),
    );
  } catch (error) {
    throw new Error(
      getApiErrorMessage(
        error,
        '비교 서버와 연결하지 못했습니다. 잠시 후 다시 시도해주세요.',
      ),
    );
  }
}

async function getCoaching(propertyIds) {
  try {
    return unwrapApiData(await comparisonApi.analyze(propertyIds));
  } catch (error) {
    const message =
      error.response?.data?.message ?? AI_COACHING_UNAVAILABLE_MESSAGE;
    throw new Error(message);
  }
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
function formatCommuteTime(item) {
  return `${formatCompactNumber(item?.commuteMinutes)}분`;
}
function formatSignedPercent(value) {
  const numericValue = Number(value);
  const sign = numericValue > 0 ? '+' : numericValue < 0 ? '-' : '';
  return `${sign}${formatCompactNumber(Math.abs(numericValue))}%`;
}
function commuteDescription() {
  const hasCar = metrics.value.some((metric) => metric.hasCar === true);
  return hasCar
    ? '출근지까지 차량 편도 기준으로 계산한 시간이에요.'
    : '출근지까지 도보 편도 기준으로 계산한 시간이에요.';
}

function moneyLabel(value) {
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
function formatKoreanMoneyText(text) {
  if (!text) return text;

  return String(text).replace(
    /(\d{1,3}(?:,\d{3})+|\d+)\uB9CC/g,
    (match, amountText, offset, source) => {
      const prefix = source.slice(Math.max(0, offset - 3), offset);
      if (prefix.includes('\uC5B5')) return match;

      const manwon = Number(String(amountText).replaceAll(',', ''));
      return manwon >= 10000 ? moneyLabel(manwon) : match;
    },
  );
}

function normalize(values, invert = false) {
  if (!values.length) return [];
  const numericValues = values.filter((value) => hasNumber(value)).map(Number);
  if (!numericValues.length) return values.map(() => null);

  const min = Math.min(...numericValues);
  const max = Math.max(...numericValues);
  if (min === max) {
    return values.map((value) => (hasNumber(value) ? 50 : null));
  }
  return values.map((value) => {
    if (!hasNumber(value)) return null;
    const ratio = (Number(value) - min) / (max - min);
    return Math.round((invert ? 1 - ratio : ratio) * 100);
  });
}

function calculateMarketStabilityScore(evaluationScore) {
  const difference = Math.abs(Number(evaluationScore));
  return Math.max(0, Math.round(100 - difference * (100 / 30)));
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
  padding: 44px 4px 64px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
}
@property --loading-progress {
  syntax: '<angle>';
  inherits: false;
  initial-value: 0deg;
}
.loading-progress {
  --loading-progress: 0deg;
  position: relative;
  display: grid;
  width: 92px;
  height: 92px;
  margin-bottom: 24px;
  place-items: center;
  border-radius: 50%;
  background: conic-gradient(
    var(--kb-yellow) 0deg var(--loading-progress),
    #e9e8e5 var(--loading-progress) 360deg
  );
  transition: --loading-progress 0.8s ease;
}
.loading-progress::before {
  position: absolute;
  inset: 7px;
  border-radius: 50%;
  background: var(--bg);
  content: '';
}
.loading-progress strong {
  position: relative;
  color: #24211d;
  font-size: 22px;
  font-weight: 900;
  letter-spacing: -0.5px;
}
.loading-title {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 50px;
  max-width: 310px;
  margin: 0 0 7px;
  color: #24211d;
  font-size: 17px;
  font-weight: 800;
  line-height: 1.45;
  word-break: keep-all;
}
.loading-text {
  margin: 0;
  color: #a09b93;
  font-size: 12.5px;
  line-height: 1.45;
}
.loading-steps {
  width: 100%;
  max-width: 340px;
  margin-top: 24px;
  padding: 16px 16px;
  border: 1px solid var(--border);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.78);
  box-shadow: 0 3px 10px rgba(50, 43, 31, 0.06);
  text-align: left;
}
.loading-steps li {
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 30px;
  color: #aaa69f;
  font-size: 12.5px;
  line-height: 1.4;
  transition: color 0.2s ease;
}
.loading-steps li.complete {
  color: #777168;
}
.loading-steps li.current {
  color: #2f2b25;
  font-weight: 800;
}
.loading-step-icon {
  display: grid;
  width: 16px;
  height: 16px;
  flex-shrink: 0;
  place-items: center;
  border: 1.5px solid #deddd9;
  border-radius: 50%;
  color: var(--white);
  font-size: 11px;
  font-weight: 900;
  line-height: 1;
}
.loading-steps li.complete .loading-step-icon {
  border-color: var(--kb-yellow);
  background: var(--kb-yellow);
}
.loading-steps li.current .loading-step-icon {
  border: 2px solid #e3e1dc;
  border-top-color: var(--kb-yellow);
  animation: compare-spin 0.85s linear infinite;
}
@keyframes compare-spin {
  to {
    transform: rotate(360deg);
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
.ai-summary-box {
  margin-top: 12px;
  padding: 12px 13px;
  background: #fff8df;
  border: 1px solid #f1dfa7;
  border-radius: 10px;
}
.ai-summary-label {
  display: block;
  margin-bottom: 5px;
  color: var(--kb-gold);
  font-size: 11px;
  font-weight: 800;
}
.ai-summary-box p {
  margin: 0;
  color: #3f3b34;
  font-size: 13px;
  line-height: 1.6;
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
  background: #fff1f2;
  border: 1px solid #ffe4e6;
  border-radius: 12px;
  font-size: 12px;
  line-height: 1.55;
  color: #9f1239;
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
.score-guidance {
  margin: 6px 0 0;
  color: #9b978f;
  font-size: 10.5px;
  line-height: 1.45;
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
.metric-bar-score.unevaluated {
  top: -18px;
  width: max-content;
  color: #9b978f;
  font-size: 9px;
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
.market-status-notice {
  margin-top: 10px;
  padding-left: 18px;
  background: #f4f2ed;
}
.market-status-notice li + li {
  margin-top: 4px;
}
</style>
