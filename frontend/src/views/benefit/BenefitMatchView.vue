<template>
  <div class="benefit">
    <simplebar
      ref="scrollAreaRef"
      class="scroll-area"
      :class="{ dimmed: needProfile }"
    >
      <!-- 탭 -->
      <div class="tabs">
        <button
          class="tab"
          :class="{ on: tab === 'policy' }"
          @click="tab = 'policy'"
        >
          청년 정책
        </button>
        <button class="tab" :class="{ on: tab === 'kb' }" @click="tab = 'kb'">
          KB 금융 상품
        </button>
      </div>

      <!-- 검색창 및 대상 필터 -->
      <div class="search-row">
        <div class="search-box">
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
            <circle cx="7" cy="7" r="5" stroke="#8a8d8f" stroke-width="1.5" />
            <path
              d="M11 11L14.5 14.5"
              stroke="#8a8d8f"
              stroke-width="1.5"
              stroke-linecap="round"
            />
          </svg>
          <input
            v-model="keyword"
            type="search"
            :placeholder="
              tab === 'policy' ? '정책·혜택명 검색' : 'KB 금융 상품명 검색'
            "
          />
        </div>

        <div
          v-if="tab === 'policy'"
          ref="filterDropdownRef"
          class="filter-dropdown"
        >
          <button
            ref="filterToggleRef"
            type="button"
            class="filter-toggle"
            :aria-expanded="filtersExpanded"
            aria-controls="target-filters"
            @click="toggleFilterDropdown"
          >
            <span>{{ selectedTargetSummary }}</span>
            <span class="filter-toggle-arrow" aria-hidden="true">▾</span>
          </button>
        </div>
      </div>

      <div v-if="tab === 'kb'" class="finance-filter-wrap">
        <button
          type="button"
          class="finance-filter-toggle"
          :class="{ open: financeFiltersExpanded }"
          :aria-expanded="financeFiltersExpanded"
          aria-controls="finance-product-filters"
          @click="financeFiltersExpanded = !financeFiltersExpanded"
        >
          <span class="finance-filter-toggle-title">
            <svg width="15" height="15" viewBox="0 0 16 16" fill="none" aria-hidden="true">
              <path d="M2 4h12M4 8h8M6 12h4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" />
            </svg>
            필터
            <span v-if="activeFinanceFilterCount" class="filter-count">
              {{ activeFinanceFilterCount }}
            </span>
          </span>
          <span class="finance-filter-chevron" aria-hidden="true">▾</span>
        </button>

        <section
          v-show="financeFiltersExpanded"
          id="finance-product-filters"
          class="finance-filters"
          aria-label="금융상품 필터"
        >
          <div
            v-for="group in financeFilterGroups"
            :key="group.key"
            class="finance-filter-row"
          >
            <span class="finance-filter-label">{{ group.label }}</span>
            <div class="finance-filter-options">
              <button
                v-for="option in group.options"
                :key="option.value"
                type="button"
                class="finance-filter-chip"
                :class="{ on: selectedFinanceFilters[group.key] === option.value }"
                :aria-pressed="selectedFinanceFilters[group.key] === option.value"
                @click="toggleFinanceFilter(group.key, option.value)"
              >
                <span class="chip-check" aria-hidden="true">✓</span>
                {{ option.label }}
              </button>
            </div>
          </div>
        </section>
      </div>

      <!-- 청년 정책 리스트 -->
      <ul v-if="tab === 'policy'" class="list">
        <!-- <li v-for="p in filteredPolicies" :key="p.id"> -->
        <li v-for="p in paginatedPolicies" :key="p.id">
          <button class="item-card" @click="$router.push(`/policies/${p.id}`)">
            <p class="item-title">{{ p.title }}</p>
            <p class="item-amount">{{ shortAmount(p.sumDescription) }}</p>
          </button>
        </li>
      </ul>

      <!-- KB 금융 상품 리스트 -->
      <ul v-else class="list">
        <li v-for="f in paginatedProducts" :key="f.id">
          <!-- <li v-for="f in filteredProducts" :key="f.id"> -->
          <button
            class="item-card finance-card"
            @click="$router.push(`/financial-products/${f.id}`)"
          >
            <p class="item-title">{{ f.productName }}</p>
            <p class="finance-summary">
              <span v-if="formatRateRange(f)" class="rate-range">
                연 {{ formatRateRange(f) }}
              </span>
              <span
                v-if="formatLoanLimit(f.loanLimit)"
                class="loan-limit"
              >
                {{ formatLoanLimit(f.loanLimit) }}
              </span>
            </p>
          </button>
        </li>
      </ul>
      <div v-if="activeItems.length === 0" class="empty-result">
        <span class="empty-result-icon" aria-hidden="true">⌕</span>
        <p>선택한 조건에 맞는 상품이 없어요</p>
        <button v-if="tab === 'kb'" type="button" @click="resetFinanceFilters">
          필터 초기화
        </button>
      </div>
      <nav
        v-if="activeItems.length > 0"
        class="pagination"
        aria-label="목록 페이지 이동"
      >
        <button
          class="page-arrow"
          type="button"
          :disabled="currentPage === 1"
          aria-label="이전 페이지"
          @click="movePage(currentPage - 1)"
        >
          ‹
        </button>

        <button
          v-for="page in visiblePages"
          :key="page"
          type="button"
          class="page-number"
          :class="{ active: currentPage === page }"
          :aria-current="currentPage === page ? 'page' : undefined"
          @click="movePage(page)"
        >
          {{ page }}
        </button>

        <button
          class="page-arrow"
          type="button"
          :disabled="currentPage === totalPages"
          aria-label="다음 페이지"
          @click="movePage(currentPage + 1)"
        >
          ›
        </button>
      </nav>
    </simplebar>

    <Teleport to="body">
      <div
        v-if="filtersExpanded"
        id="target-filters"
        ref="targetFiltersRef"
        class="target-filters"
        :style="filterDropdownStyle"
        aria-label="정책 카테고리 필터"
      >
        <button
          v-for="filter in targetFilters"
          :key="filter.code"
          type="button"
          class="target-filter"
          :class="{ on: selectedTargetCodes.includes(filter.code) }"
          :aria-pressed="selectedTargetCodes.includes(filter.code)"
          @click="toggleTargetFilter(filter.code)"
        >
          <span>{{ filter.label }}</span>
        </button>
      </div>
    </Teleport>

    <!-- 12-1 내 정보 입력 필요 모달 -->
    <Teleport to="body">
      <div v-if="isActive && needProfile" class="modal-overlay">
        <div class="modal">
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
          <p class="m-title">내 정보 입력이 필요해요</p>
          <p class="m-text">
            맞춤 청년 정책/금융 상품을 찾으려면<br />소득·나이·지역 정보가
            필요해요.<br />지금 입력하시겠어요?
          </p>
          <div class="m-actions">
            <button class="m-later" @click="$router.push('/home')">
              다음에
            </button>
            <button class="m-go" @click="$router.push('/profile-setup')">
              입력하러 가기
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import {
  computed,
  nextTick,
  onActivated,
  onBeforeUnmount,
  onDeactivated,
  onMounted,
  ref,
  watch,
} from 'vue';
import simplebar from 'simplebar-vue';
import { financeApi, policyApi, userApi } from '@/api/services';
import { onBeforeRouteLeave } from 'vue-router';

defineOptions({ name: 'BenefitMatchView' });

const props = defineProps({
  initialTab: { type: String, default: 'policy' },
});

const tab = ref(props.initialTab);
const keyword = ref('');
const selectedTargetCodes = ref([]);
const selectedFinanceFilters = ref({ transaction: '', product: '' });
const financeFiltersExpanded = ref(false);
const filtersExpanded = ref(false);
const filterDropdownRef = ref(null);
const filterToggleRef = ref(null);
const targetFiltersRef = ref(null);
const scrollAreaRef = ref(null);
const filterDropdownStyle = ref({});
const policies = ref([]);
const products = ref([]);
const needProfile = ref(false);
const isActive = ref(false);
const currentPage = ref(1);
const savedScrollTop = ref(0);
const reloadOnActivate = ref(false);
const pageSize = 5;
const financeFilterGroups = [
  {
    key: 'transaction',
    label: '거래 유형',
    options: [
      { value: '매매', label: '매매' },
      { value: '전세', label: '전세' },
      { value: '월세', label: '월세' },
    ],
  },
  {
    key: 'product',
    label: '상품 유형',
    options: [
      { value: '보증', label: '보증' },
      { value: '담보', label: '담보' },
      { value: '기타', label: '기타' },
    ],
  },
];
const activeFinanceFilterCount = computed(() =>
  Object.values(selectedFinanceFilters.value).filter(Boolean).length,
);
const targetFilters = [
  { code: 'HOUSING', label: '주거' },
  { code: 'EMPLOYMENT', label: '취업·일자리' },
  { code: 'STARTUP', label: '창업·사업' },
  { code: 'FINANCE_ASSET', label: '금융·자산' },
  { code: 'EDUCATION', label: '교육·역량' },
  { code: 'WELFARE', label: '생활·복지' },
  { code: 'CULTURE', label: '문화·여가' },
  { code: 'FAMILY', label: '결혼·가족' },
  { code: 'AGRI_SETTLEMENT', label: '농업·지역정착' },
];
const selectedTargetSummary = computed(() => {
  const count = selectedTargetCodes.value.length;
  if (!count) return '카테고리';
  if (count === 1) {
    return (
      targetFilters.find(
        (filter) => filter.code === selectedTargetCodes.value[0],
      )?.label ?? '카테고리'
    );
  }
  return `${count}개 선택`;
});

onMounted(async () => {
  await checkProfile();
  await loadMatches();
});

onActivated(async () => {
  isActive.value = true;
  document.addEventListener('pointerdown', closeFilterOnOutsideClick);
  window.addEventListener('resize', updateFilterDropdownPosition);

  if (reloadOnActivate.value) {
    reloadOnActivate.value = false;
    await checkProfile();
    await loadMatches();
  } else if (needProfile.value) {
    await checkProfile();
  }

  await nextTick();
  scrollTo(savedScrollTop.value);
});

onDeactivated(() => {
  isActive.value = false;
  document.removeEventListener('pointerdown', closeFilterOnOutsideClick);
  window.removeEventListener('resize', updateFilterDropdownPosition);
  filtersExpanded.value = false;
  financeFiltersExpanded.value = false;
});

onBeforeRouteLeave((to) => {
  const isDetailRoute = [
    'policy-detail',
    'financial-product-detail',
  ].includes(to.name);

  if (isDetailRoute) {
    savedScrollTop.value = getScrollElement()?.scrollTop ?? 0;
    return;
  }

  resetListState();
  reloadOnActivate.value = true;
});

onBeforeUnmount(() => {
  document.removeEventListener('pointerdown', closeFilterOnOutsideClick);
  window.removeEventListener('resize', updateFilterDropdownPosition);
});

async function checkProfile() {
  const profile = await userApi.getProfile();
  needProfile.value = !profile || !profile.targetRegion || !profile.birthDate;
}

async function loadMatches() {
  policies.value = (await policyApi.matches()) ?? [];
  products.value = (await financeApi.matches()) ?? [];
}

function getScrollElement() {
  return scrollAreaRef.value?.scrollElement ?? null;
}

function scrollTo(top) {
  getScrollElement()?.scrollTo({ top, behavior: 'auto' });
}

function resetListState() {
  tab.value = props.initialTab;
  keyword.value = '';
  selectedTargetCodes.value = [];
  selectedFinanceFilters.value = { transaction: '', product: '' };
  currentPage.value = 1;
  savedScrollTop.value = 0;
}

// 키워드 프론트 필터링 (기능명세 43)
const filteredPolicies = computed(() =>
  policies.value.filter(
    (p) => !keyword.value || p.title.includes(keyword.value),
  ),
);
const filteredProducts = computed(() =>
  products.value
    .filter((product) => {
      const matchesKeyword =
        !keyword.value || product.productName.includes(keyword.value);
      const matchesTransaction = matchesFinanceFilter(
        product,
        'transaction',
        selectedFinanceFilters.value.transaction,
      );
      const matchesProduct = matchesFinanceFilter(
        product,
        'product',
        selectedFinanceFilters.value.product,
      );
      return matchesKeyword && matchesTransaction && matchesProduct;
    })
    .sort(compareSimilarityDescending),
);

const activeItems = computed(() =>
  tab.value === 'policy' ? filteredPolicies.value : filteredProducts.value,
);

const totalPages = computed(() =>
  Math.max(1, Math.ceil(activeItems.value.length / pageSize)),
);

const visiblePages = computed(() => {
  const total = totalPages.value;
  const current = currentPage.value;
  const start = Math.max(1, Math.min(current - 2, total - 4));
  const end = Math.min(total, start + 4);

  return Array.from({ length: end - start + 1 }, (_, index) => start + index);
});

const paginatedPolicies = computed(() => {
  const start = (currentPage.value - 1) * pageSize;
  return filteredPolicies.value.slice(start, start + pageSize);
});

const paginatedProducts = computed(() => {
  const start = (currentPage.value - 1) * pageSize;
  return filteredProducts.value.slice(start, start + pageSize);
});

async function movePage(page) {
  if (page < 1 || page > totalPages.value) return;
  currentPage.value = page;
  await nextTick();
  scrollTo(0);
}

async function toggleTargetFilter(code) {
  selectedTargetCodes.value = selectedTargetCodes.value.includes(code)
    ? []
    : [code];
  policies.value =
    (await policyApi.matches(undefined, selectedTargetCodes.value[0])) ?? [];
}

function toggleFinanceFilter(group, value) {
  selectedFinanceFilters.value[group] =
    selectedFinanceFilters.value[group] === value ? '' : value;
}

function resetFinanceFilters() {
  selectedFinanceFilters.value = { transaction: '', product: '' };
}

function matchesFinanceFilter(product, group, selected) {
  if (!selected) return true;

  const explicitValue = group === 'transaction'
    ? product.tradeType
    : product.productType;

  return String(explicitValue ?? '').trim() === selected;
}

function compareSimilarityDescending(a, b) {
  const aValue = Number(a.categorySimilarity);
  const bValue = Number(b.categorySimilarity);
  const aHasValue = Number.isFinite(aValue);
  const bHasValue = Number.isFinite(bValue);

  if (!aHasValue && !bHasValue) return 0;
  if (!aHasValue) return 1;
  if (!bHasValue) return -1;
  return bValue - aValue;
}

function formatRate(value) {
  const rate = Number(value);
  if (!Number.isFinite(rate) || rate <= 0) return '';
  return `${rate.toFixed(2).replace(/\.?0+$/, '')}%`;
}

function formatRateRange(product) {
  const minRate = formatRate(product.minRate);
  const maxRate = formatRate(product.maxRate);

  if (!minRate) return maxRate;
  if (!maxRate || minRate === maxRate) return minRate;
  return `${minRate} ~ ${maxRate}`;
}

function formatLoanLimit(value) {
  const loanLimit = String(value ?? '');
  const parenthesisIndex = loanLimit.indexOf('(');
  return (parenthesisIndex === -1
    ? loanLimit
    : loanLimit.slice(0, parenthesisIndex)
  ).trim();
}

async function toggleFilterDropdown() {
  filtersExpanded.value = !filtersExpanded.value;
  if (filtersExpanded.value) {
    await nextTick();
    updateFilterDropdownPosition();
  }
}

function updateFilterDropdownPosition() {
  if (!filtersExpanded.value || !filterToggleRef.value) return;
  const rect = filterToggleRef.value.getBoundingClientRect();
  const width = 158;
  filterDropdownStyle.value = {
    top: `${rect.bottom + 5}px`,
    left: `${Math.max(8, Math.min(rect.right - width, window.innerWidth - width - 8))}px`,
  };
}

function closeFilterOnOutsideClick(event) {
  const clickedTrigger = filterDropdownRef.value?.contains(event.target);
  const clickedMenu = targetFiltersRef.value?.contains(event.target);
  if (!clickedTrigger && !clickedMenu) {
    filtersExpanded.value = false;
  }
}

watch(
  () => props.initialTab,
  (value) => {
    tab.value = value;
  },
);

watch([tab, keyword, selectedTargetCodes, selectedFinanceFilters], () => {
  currentPage.value = 1;
}, { deep: true });

watch(totalPages, (total) => {
  if (currentPage.value > total) {
    currentPage.value = total;
  }
});

function shortAmount(v) {
  const m = String(v).match(/(월\s*)?(최대\s*)?([\d,.]+만\s*원)/);
  return m ? m[3].replace(/\s/g, '') : v;
}
</script>

<style scoped>
.benefit {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--bg);
}
.edit-badge {
  padding: 6px 12px;
  border-radius: 8px;
  background: #454138;
  color: #fff;
  font-size: 11.5px;
  font-weight: 700;
}
.scroll-area {
  flex: 1;
  overflow-y: auto;
  padding: 14px 16px 20px;
}
.tabs {
  display: flex;
  gap: 8px;
}
.tab {
  padding: 8px 14px;
  border-radius: 100px;
  border: 1px solid var(--border);
  background: var(--white);
  font-size: 13px;
  color: var(--kb-silver);
}
.tab.on {
  background: var(--yellow-tint);
  border-color: var(--kb-yellow);
  color: var(--text-primary);
  font-weight: 700;
}
.search-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 14px;
}
.search-box {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
  height: 44px;
  padding: 0 14px;
  background: var(--white);
  border: 1px solid var(--border);
  border-radius: 12px;
}
.search-box input {
  flex: 1;
  min-width: 0;
  border: none;
  font-size: 13px;
  background: transparent;
}
.filter-dropdown {
  position: relative;
  flex: 0 0 auto;
}
.filter-toggle {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 4px;
  width: 88px;
  height: 44px;
  padding: 0 10px;
  border: 1px solid var(--border);
  border-radius: 10px;
  background: var(--white);
  color: var(--text-primary);
  font-size: 12px;
  font-weight: 600;
}
.filter-toggle-arrow {
  font-size: 10px;
  line-height: 1;
}
.filter-toggle:focus-visible {
  outline: 2px solid var(--kb-yellow);
  outline-offset: 2px;
}
.finance-filter-wrap {
  margin-top: 10px;
}
.finance-filter-toggle {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  height: 40px;
  padding: 0 13px;
  border: 1px solid var(--border);
  border-radius: 12px;
  background: var(--white);
  color: var(--kb-gray);
  font-size: 12.5px;
  font-weight: 800;
}
.finance-filter-toggle-title {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.filter-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 19px;
  height: 19px;
  padding: 0 6px;
  border-radius: 99px;
  border: 1px solid var(--kb-yellow);
  background: var(--yellow-tint);
  color: var(--text-primary);
  font-size: 10.5px;
}
.finance-filter-chevron {
  font-size: 10px;
  line-height: 1;
  transition: transform 0.2s ease;
}
.finance-filter-toggle.open .finance-filter-chevron {
  transform: rotate(180deg);
}
.finance-filter-toggle:focus-visible {
  outline: 2px solid var(--kb-yellow);
  outline-offset: 2px;
}
.finance-filters {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 8px;
  padding: 14px;
  border: 1px solid var(--border);
  border-radius: 16px;
  background: var(--white);
  box-shadow: 0 3px 12px rgba(102, 77, 0, 0.06);
}
.finance-filter-row {
  display: flex;
  align-items: center;
  gap: 12px;
}
.finance-filter-label {
  width: 58px;
  flex-shrink: 0;
  color: var(--kb-gray);
  font-size: 11.5px;
  font-weight: 800;
}
.finance-filter-options {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 6px;
  flex: 1;
}
.finance-filter-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 3px;
  min-width: 0;
  height: 34px;
  padding: 0 8px;
  border: 1px solid var(--border);
  border-radius: 10px;
  background: #faf9f7;
  color: var(--kb-silver);
  font-size: 12px;
  font-weight: 700;
  transition: 0.18s ease;
}
.finance-filter-chip:hover {
  border-color: #e4bd45;
  background: var(--yellow-tint);
  color: var(--text-primary);
}
.finance-filter-chip.on {
  border-color: var(--kb-yellow);
  background: var(--yellow-tint);
  color: var(--text-primary);
  box-shadow: none;
}
.chip-check {
  width: 0;
  overflow: hidden;
  opacity: 0;
  transition: 0.18s ease;
}
.finance-filter-chip.on .chip-check {
  width: 12px;
  opacity: 1;
}
.finance-filter-chip:focus-visible {
  outline: 2px solid var(--kb-yellow);
  outline-offset: 2px;
}
.target-filters {
  position: fixed;
  z-index: 150;
  display: flex;
  flex-direction: column;
  gap: 3px;
  width: 158px;
  max-height: 244px;
  padding: 7px;
  overflow-y: auto;
  scrollbar-width: none;
  border: 1px solid var(--border);
  border-radius: 10px;
  background: var(--white);
  box-shadow: 0 8px 24px rgba(33, 30, 24, 0.18);
}
.target-filters::-webkit-scrollbar {
  display: none;
}
.target-filter {
  width: 100%;
  padding: 8px 10px;
  border-radius: 7px;
  background: transparent;
  color: var(--text-primary);
  font-size: 12px;
  text-align: left;
  white-space: nowrap;
}
.target-filter.on {
  background: var(--yellow-tint);
  color: var(--text-primary);
  font-weight: 700;
}
.target-filter:focus-visible {
  outline: 2px solid var(--kb-yellow);
  outline-offset: 2px;
}
.list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 16px;
}
.item-card {
  display: flex;
  flex-direction: column;
  gap: 4px;
  width: 100%;
  padding: 16px;
  text-align: left;
  background: var(--white);
  border: 1px solid var(--border);
  border-radius: 14px;
}
.finance-card {
  gap: 8px;
  min-height: 88px;
  padding: 18px 20px;
  border-radius: 15px;
  box-shadow: 0 2px 5px rgba(33, 30, 24, 0.04);
  transition:
    border-color 0.18s ease,
    box-shadow 0.18s ease;
}
.finance-card:hover,
.finance-card:focus-visible {
  border-color: #efc754;
  box-shadow: 0 3px 8px rgba(139, 104, 0, 0.08);
}
.finance-card:focus-visible {
  outline: none;
}
.finance-summary {
  display: flex;
  align-items: baseline;
  flex-wrap: wrap;
  gap: 5px 10px;
  min-width: 0;
  font-size: 12.5px;
  line-height: 1.35;
}
.item-title {
  font-size: 14.5px;
  font-weight: 800;
}
.item-amount {
  font-size: 11.5px;
  font-weight: 400;
  color: var(--kb-silver);
}
.item-sub {
  font-size: 11.5px;
  color: var(--kb-silver);
}
.item-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  width: 100%;
  margin-top: 3px;
}
.rate-range {
  flex-shrink: 0;
  color: #df762d;
  font-weight: 800;
}
.loan-limit {
  flex: 0 0 auto;
  max-width: 100%;
  color: var(--kb-silver);
  font-weight: 500;
  overflow-wrap: anywhere;
}
.empty-result {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 38px 16px;
  color: var(--kb-silver);
  font-size: 12.5px;
}
.empty-result-icon {
  font-size: 28px;
  color: #bbb5aa;
}
.empty-result button {
  margin-top: 3px;
  padding: 7px 12px;
  border-radius: 9px;
  background: var(--yellow-tint);
  color: #6b5300;
  font-size: 11.5px;
  font-weight: 800;
}
/* 12-1 모달 */
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
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin: 24px 0 8px;
}

.page-number,
.page-arrow {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border: 1px solid var(--border);
  border-radius: 10px;
  background: var(--white);
  color: var(--kb-gray);
  font-size: 13px;
  font-weight: 700;
  transition:
    background 0.15s ease,
    border-color 0.15s ease,
    transform 0.15s ease;
}

.page-number:hover,
.page-arrow:not(:disabled):hover {
  border-color: var(--kb-yellow);
  background: var(--yellow-tint);
  transform: translateY(-1px);
}

.page-number.active {
  border-color: var(--kb-yellow);
  background: var(--kb-yellow);
  color: #302b22;
  box-shadow: 0 4px 10px rgba(255, 188, 0, 0.25);
}

.page-arrow {
  font-size: 21px;
  font-weight: 500;
}

.page-arrow:disabled {
  cursor: default;
  opacity: 0.35;
}

.page-number:focus-visible,
.page-arrow:focus-visible {
  outline: 2px solid var(--kb-yellow);
  outline-offset: 2px;
}
</style>
