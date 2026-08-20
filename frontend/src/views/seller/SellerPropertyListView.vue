<template>
  <div class="seller-properties">
    <div class="filters">
      <button
        v-for="option in FILTERS"
        :key="option.value"
        class="filter-chip"
        :class="{ active: filter === option.value }"
        type="button"
        @click="selectFilter(option.value)"
      >
        {{ option.label }} {{ countOf(option.value) }}
      </button>
    </div>

    <simplebar class="content">
      <ul v-if="items.length" class="cards">
        <li
          v-for="item in items"
          :key="item.propertyId"
          class="card"
          :class="{ sold: !item.transactionStatus }"
        >
          <div
            class="card-body"
            @click="
              router.push({
                path: `/seller/properties/${item.propertyId}`,
                query: { from: 'list' },
              })
            "
          >
            <span class="thumb">
              <span v-if="!item.transactionStatus" class="sold-overlay">
                거래 완료
              </span>
              <img
                v-if="item.imageUrl"
                :src="item.imageUrl"
                :alt="titleOf(item)"
                class="thumb-img"
              />
              <svg
                v-else
                width="30"
                height="30"
                viewBox="0 0 30 30"
                fill="none"
              >
                <path
                  d="M4 13.5L15 5l11 8.5"
                  stroke="#8A8477"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
                <path
                  d="M7 12v12h16V12"
                  stroke="#8A8477"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
            </span>
            <div class="card-texts">
              <span
                class="status-badge"
                :class="{ done: !item.transactionStatus }"
              >
                {{ statusLabel(item) }}
              </span>
              <p class="card-title">{{ titleOf(item) }}</p>
              <p class="card-sub">{{ typeLabel(item) }}</p>
              <p class="card-price">{{ priceLabel(item) }}</p>
              <p class="card-meta">{{ subMeta(item) }}</p>
            </div>
            <ChevronRight class="detail-chevron" :size="20" aria-hidden="true" />
          </div>

          <div class="card-actions">
            <button class="card-action" type="button" @click="goEdit(item)">
              수정
            </button>
            <span class="action-divider" />
            <button
              class="card-action"
              :class="item.transactionStatus ? 'to-done' : 'to-open'"
              type="button"
              :disabled="busyId === item.propertyId"
              @click="toggleStatus(item)"
            >
              {{ item.transactionStatus ? '거래완료' : '게시' }}
            </button>
            <span class="action-divider" />
            <button
              class="card-action danger"
              type="button"
              :disabled="busyId === item.propertyId"
              @click="removeProperty(item)"
            >
              삭제
            </button>
          </div>
        </li>
      </ul>

      <p v-if="!items.length" class="empty">
        {{ emptyMessage }}
      </p>
    </simplebar>

    <div class="pagination-footer">
      <nav
        v-show="items.length && totalPages > 1"
        class="pagination"
        aria-label="매물 목록 페이지 이동"
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
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import simplebar from 'simplebar-vue';
import { ChevronRight } from 'lucide-vue-next';
import { sellerApi } from '@/api/services';
import { getApiErrorMessage } from '@/api/client';
import {
  priceLabel,
  statusLabel,
  subMeta,
  titleOf,
  typeLabel,
} from '@/utils/sellerProperty';

const FILTERS = [
  { value: 'all', label: '전체' },
  { value: 'open', label: '게시중' },
  { value: 'done', label: '거래완료' },
];

const router = useRouter();
const items = ref([]);
const filter = ref('all');
const busyId = ref(null);
const currentPage = ref(1);
const totalPages = ref(1);
const counts = ref({ all: 0, open: 0, done: 0 });
const PAGE_SIZE = 3;

const visiblePages = computed(() => {
  const start = Math.max(
    1,
    Math.min(currentPage.value - 2, totalPages.value - 4),
  );
  const end = Math.min(totalPages.value, start + 4);
  return Array.from({ length: end - start + 1 }, (_, index) => start + index);
});

const emptyMessage = computed(() =>
  filter.value === 'all'
    ? '아직 등록한 매물이 없어요'
    : '조건에 맞는 매물이 없어요',
);

onMounted(loadProperties);

async function loadProperties() {
  const data = await sellerApi.myProperties({
    page: currentPage.value - 1,
    size: PAGE_SIZE,
    status: filter.value.toUpperCase(),
  });
  const payload = data?.data ?? data;
  const content = Array.isArray(payload) ? payload : (payload?.content ?? []);
  const nextTotalPages = payload?.totalPages ?? 1;
  if (currentPage.value > nextTotalPages) {
    currentPage.value = nextTotalPages;
    await loadProperties();
    return;
  }
  items.value = content.map((item) => ({
    ...item,
    propertyId: item.propertyId ?? item.id,
  }));
  totalPages.value = nextTotalPages;
  counts.value = payload?.counts ?? {
    all: content.length,
    open: content.filter((item) => item.transactionStatus).length,
    done: content.filter((item) => !item.transactionStatus).length,
  };
}

function countOf(value) {
  return counts.value[value] ?? 0;
}

function selectFilter(value) {
  if (filter.value === value) return;
  filter.value = value;
  currentPage.value = 1;
  loadProperties();
}

function movePage(page) {
  if (page < 1 || page > totalPages.value || page === currentPage.value) return;
  currentPage.value = page;
  loadProperties();
}

function goEdit(item) {
  router.push(`/seller/register/1?propertyId=${item.propertyId}`);
}

async function toggleStatus(item) {
  const next = !item.transactionStatus;
  busyId.value = item.propertyId;
  try {
    await sellerApi.changeStatus(item.propertyId, next);
    await loadProperties();
  } catch (error) {
    alert(getApiErrorMessage(error, '매물 상태를 변경하지 못했어요.'));
  } finally {
    busyId.value = null;
  }
}

async function removeProperty(item) {
  if (!confirm('이 매물을 삭제할까요? 삭제하면 목록에서 사라집니다.')) return;

  busyId.value = item.propertyId;
  try {
    await sellerApi.remove(item.propertyId);
    await loadProperties();
  } catch (error) {
    alert(getApiErrorMessage(error, '매물을 삭제하지 못했어요.'));
  } finally {
    busyId.value = null;
  }
}
</script>

<style scoped>
.seller-properties {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  background: var(--bg);
}

.content {
  flex: 1;
  min-height: 0;
  padding: 0 16px;
}

.content :deep(.simplebar-content) {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.filters {
  display: flex;
  gap: 7px;
  flex-shrink: 0;
  padding: 16px 16px 8px;
}

.filter-chip {
  padding: 7px 13px;
  background: var(--white);
  border: 1px solid var(--border);
  border-radius: 16px;
  font-size: 12px;
  font-weight: 500;
  color: var(--kb-gray);
}

.filter-chip.active {
  background: var(--kb-yellow-header);
  border-color: transparent;
  font-weight: 700;
  color: #211a00;
}

.cards {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* 카드 모양·글자는 매물 목록(PropertyListView)과 같은 값을 쓴다 */
.card {
  display: flex;
  flex-direction: column;
  background: #fff;
  border: 1px solid var(--border);
  border-radius: 14px;
  overflow: hidden;
}

.card.sold {
  background: #f2f1ee;
  border-color: #e2e0da;
}

.card.sold .card-title,
.card.sold .card-price {
  color: #8a8477;
}

.card.sold .card-sub,
.card.sold .card-meta {
  color: #a8a49b;
}

.card-body {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  cursor: pointer;
}

.thumb {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 62px;
  height: 62px;
  flex-shrink: 0;
  background: #f5efdb;
  border-radius: 10px;
  overflow: hidden;
}

.thumb-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.sold-overlay {
  position: absolute;
  inset: 0;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(33, 30, 24, 0.55);
  border-radius: inherit;
  font-size: 11px;
  font-weight: 700;
  color: #fff;
}

.card-texts {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.detail-chevron {
  flex-shrink: 0;
  color: #b4b0a8;
}

.status-badge {
  margin-bottom: 4px;
  padding: 3px 7px;
  background: #e5f6ea;
  border-radius: 6px;
  font-size: 10px;
  font-weight: 700;
  color: #21874a;
}

.status-badge.done {
  background: #efefef;
  color: #737373;
}

.card-title {
  width: 100%;
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-sub,
.card-price {
  margin-top: 2px;
  font-size: 12px;
  font-weight: 500;
  color: var(--kb-gray);
}

.card-meta {
  margin-top: 3px;
  font-size: 12px;
  font-weight: 400;
  color: var(--kb-silver);
}

.card-actions {
  display: flex;
  align-items: stretch;
  border-top: 1px solid #eee;
}

.card-action {
  flex: 1;
  min-width: 0;
  padding: 12px 0;
  font-size: 13px;
  font-weight: 700;
  color: var(--kb-gray);
}

.card-action:disabled {
  opacity: 0.5;
}

.card-action.to-done {
  color: #3351ca;
}

.card-action.to-open {
  color: #21874a;
}

.card-action.danger {
  color: var(--danger);
}

.action-divider {
  width: 1px;
  background: #eee;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin: 0;
}

.pagination-footer {
  height: 58px;
  flex-shrink: 0;
  padding: 8px 16px 16px;
  box-sizing: border-box;
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

.empty {
  padding: 40px 0;
  font-size: 12.5px;
  color: var(--kb-silver);
  text-align: center;
}
</style>
