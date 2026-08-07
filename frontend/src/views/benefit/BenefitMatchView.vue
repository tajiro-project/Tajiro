<template>
  <div class="benefit">
    <simplebar class="scroll-area" :class="{ dimmed: needProfile }">
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
            <path d="M11 11L14.5 14.5" stroke="#8a8d8f" stroke-width="1.5" stroke-linecap="round" />
          </svg>
          <input
            v-model="keyword"
            type="search"
            :placeholder="tab === 'policy' ? '정책·혜택명 검색' : 'KB 금융 상품명 검색'"
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
            class="item-card"
            @click="$router.push(`/financial-products/${f.id}`)"
          >
            <p class="item-title">{{ f.productName }}</p>
            <!-- <p class="item-amount">
              한도 {{ f.maxLimitAmount.toLocaleString() }}만원 · 연
              {{ f.minRate.toFixed(1) }}%
            </p> -->
            <div class="item-meta">
              <!-- <p class="item-sub">무보증 월세 자금 · 만 19~34세</p> -->

              <span class="rate-range">
                연 {{ Number(f.minRate).toFixed(1) }}% ~
                {{ Number(f.maxRate).toFixed(1) }}%
              </span>
            </div>
          </button>
        </li>
      </ul>
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
        aria-label="정책 대상 필터"
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
      <div v-if="needProfile" class="modal-overlay">
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
            <button class="m-later" @click="needProfile = false">다음에</button>
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
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import simplebar from 'simplebar-vue';
import { financeApi, policyApi, userApi } from '@/api/services';

const props = defineProps({
  initialTab: { type: String, default: 'policy' },
});

const tab = ref(props.initialTab);
const keyword = ref('');
const selectedTargetCodes = ref([]);
const filtersExpanded = ref(false);
const filterDropdownRef = ref(null);
const filterToggleRef = ref(null);
const targetFiltersRef = ref(null);
const filterDropdownStyle = ref({});
const policies = ref([]);
const products = ref([]);
const needProfile = ref(false);
const currentPage = ref(1);
const pageSize = 5;
const targetFilters = [
  { code: '0014001', label: '중소기업' },
  { code: '0014002', label: '여성' },
  { code: '0014003', label: '기초생활수급자' },
  { code: '0014004', label: '한부모가정' },
  { code: '0014005', label: '장애인' },
  { code: '0014006', label: '농업인' },
  { code: '0014007', label: '군인' },
  { code: '0014008', label: '지역인재' },
  { code: '0014010', label: '제한없음' },
];
const selectedTargetSummary = computed(() => {
  const count = selectedTargetCodes.value.length;
  if (!count) return '대상';
  if (count === 1) {
    return targetFilters.find(
      (filter) => filter.code === selectedTargetCodes.value[0],
    )?.label ?? '대상';
  }
  return `${count}개 선택`;
});

onMounted(async () => {
  document.addEventListener('pointerdown', closeFilterOnOutsideClick);
  window.addEventListener('resize', updateFilterDropdownPosition);
  const profile = await userApi.getProfile();
  // 프로필(지역·생년월일) 미입력 시 12-1 모달 노출
  if (!profile || !profile.targetRegion || !profile.birthDate)
    needProfile.value = true;
  policies.value = (await policyApi.matches()) ?? [];
  products.value = (await financeApi.matches()) ?? [];
});

onBeforeUnmount(() => {
  document.removeEventListener('pointerdown', closeFilterOnOutsideClick);
  window.removeEventListener('resize', updateFilterDropdownPosition);
});

// 키워드 프론트 필터링 (기능명세 43)
const filteredPolicies = computed(() =>
  policies.value.filter(
    (p) =>
      (!keyword.value || p.title.includes(keyword.value)) &&
      matchesTargetCodes(p, selectedTargetCodes.value),
  ),
);
const filteredProducts = computed(() =>
  products.value.filter(
    (f) => !keyword.value || f.productName.includes(keyword.value),
  ),
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

function movePage(page) {
  if (page < 1 || page > totalPages.value) return;
  currentPage.value = page;
}

function toggleTargetFilter(code) {
  selectedTargetCodes.value = selectedTargetCodes.value.includes(code)
    ? []
    : [code];
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

function matchesTargetCodes(policy, codes) {
  if (!codes.length) return true;

  const value =
    policy.sbizCd ??
    policy.targetCode ??
    policy.target_code ??
    policy.targetCodes ??
    policy.target_codes ??
    policy.targetTypeCode ??
    policy.target_type_code;

  if (Array.isArray(value))
    return value.some((item) => codes.includes(normalizeTargetCode(item)));
  if (value == null) return false;

  const policyCodes = String(value)
    .split(',')
    .map(normalizeTargetCode);
  return codes.some((code) => policyCodes.includes(code));
}

function normalizeTargetCode(value) {
  const normalized = String(value).trim();
  return /^\d{1,7}$/.test(normalized)
    ? normalized.padStart(7, '0')
    : normalized;
}

watch([tab, keyword, selectedTargetCodes], () => {
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
  padding: 4px 8px;
  font-size: 11px;
  font-weight: 700;
  line-height: 1;
  color: #6b5300;
  background: var(--yellow-tint);
  border-radius: 999px;
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
