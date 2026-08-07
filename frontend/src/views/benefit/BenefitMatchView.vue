<template>
  <div class="benefit">
    <simplebar
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
        <button
          class="tab"
          :class="{ on: tab === 'kb' }"
          @click="tab = 'kb'"
        >
          KB 금융 상품
        </button>
      </div>

      <!-- 검색창 -->
      <div class="search-box">
        <svg
          width="16"
          height="16"
          viewBox="0 0 16 16"
          fill="none"
        >
          <circle
            cx="7"
            cy="7"
            r="5"
            stroke="#8a8d8f"
            stroke-width="1.5"
          />
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
            tab === 'policy'
              ? '정책·혜택명으로 검색 (예: 월세지원)'
              : 'KB 금융 상품명으로 검색'
          "
        />
      </div>

      <!-- 청년 정책 리스트 -->
      <ul
        v-if="tab === 'policy'"
        class="list"
      >
        <!-- <li v-for="p in filteredPolicies" :key="p.id"> -->
        <li
          v-for="p in paginatedPolicies"
          :key="p.id"
        >
          <button
            class="item-card"
            @click="$router.push(`/policies/${p.id}`)"
          >
            <p class="item-title">{{ p.title }}</p>
            <p class="item-amount">{{ shortAmount(p.sumDescription) }}</p>
          </button>
        </li>
      </ul>

      <!-- KB 금융 상품 리스트 -->
      <ul
        v-else
        class="list"
      >
        <li
          v-for="f in paginatedProducts"
          :key="f.id"
        >
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

    <!-- 12-1 내 정보 입력 필요 모달 -->
    <Teleport to="body">
      <div
        v-if="needProfile"
        class="modal-overlay"
      >
        <div class="modal">
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
          <p class="m-title">내 정보 입력이 필요해요</p>
          <p class="m-text">
            맞춤 청년 정책/금융 상품을 찾으려면<br />소득·나이·지역 정보가
            필요해요.<br />지금 입력하시겠어요?
          </p>
          <div class="m-actions">
            <button
              class="m-later"
              @click="needProfile = false"
            >
              다음에
            </button>
            <button
              class="m-go"
              @click="$router.push('/profile-setup')"
            >
              입력하러 가기
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue';
import simplebar from 'simplebar-vue';
import { financeApi, policyApi } from '@/api/services';

const props = defineProps({
  initialTab: { type: String, default: 'policy' },
});

const tab = ref(props.initialTab);
const keyword = ref('');
const policies = ref([]);
const products = ref([]);
const needProfile = ref(false);
const currentPage = ref(1);
const pageSize = 5;

onMounted(async () => {
  // const profile = await userApi.getProfile();
  // 프로필(지역·생년월일) 미입력 시 12-1 모달 노출
  // if (!profile || !profile.targetRegion || !profile.birthDate)
  //   needProfile.value = true;
  policies.value = (await policyApi.matches()) ?? [];
  products.value = (await financeApi.matches()) ?? [];
});

// 키워드 프론트 필터링 (기능명세 43)
const filteredPolicies = computed(() =>
  policies.value.filter(
    (p) => !keyword.value || p.title.includes(keyword.value),
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

watch([tab, keyword], () => {
  currentPage.value = 1;
});

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
.search-box {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 44px;
  padding: 0 14px;
  margin-top: 14px;
  background: var(--white);
  border: 1px solid var(--border);
  border-radius: 12px;
}
.search-box input {
  flex: 1;
  border: none;
  font-size: 13px;
  background: transparent;
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
