<template>
  <div class="favorite-list">
    <PageHeader title="스크랩한 매물" />

    <simplebar class="content">
      <p class="count-label">찜한 매물 {{ items.length }}건</p>
            <ul v-if="items.length" class="cards">
                <li v-for="item in items" :key="item.propertyId">
                    <div class="card" @click="goDetail(item)">
                        <span class="thumb">
                            <svg width="30" height="30" viewBox="0 0 30 30" fill="none">
                                <path d="M4 13.5L15 5l11 8.5" stroke="#8A8477" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
                                <path d="M7 12v12h16V12" stroke="#8A8477" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
                            </svg>
                        </span>

                        <div class="card-texts">
                            <p class="card-price">{{ priceLabel(item) }}</p>
                            <p class="card-meta">{{ item.propertyType }} · {{ pyeong(item.areaM2) }}평 · {{ floorLabel(item.floorInfo) }}</p>
                            <p class="card-address">{{ item.address }}</p>
                        </div>

                        <button
                            class="heart-btn"
                            type="button"
                            :disabled="removingId === item.propertyId"
                            aria-label="찜 해제"
                            @click.stop="removeFavorite(item)"
                        >
                            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                                <path
                                    d="M10 17.2s-6.6-4-6.6-8.7c0-2.3 1.8-4 4-4 1.2 0 2.2.6 2.6 1.4.4-.8 1.4-1.4 2.6-1.4 2.2 0 4 1.7 4 4 0 4.7-6.6 8.7-6.6 8.7z"
                                    fill="#FFBC00"
                                />
                            </svg>
                        </button>
                    </div>
                </li>
            </ul
      <div
        v-else
        class="empty-state"
      >
        <p class="empty-title">아직 찜한 매물이 없어요</p>
        <p class="empty-sub">
          마음에 드는 매물을 찜해두면 여기서 모아볼 수 있어요
        </p>
      </div>
    </simplebar>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { favoriteApi } from '@/api/services';
import simplebar from 'simplebar-vue';
import PageHeader from '@/components/PageHeader.vue';

const PYEONG = 3.3058;

const router = useRouter();
const items = ref([]);
const removingId = ref('');

onMounted(loadFavorites);

async function loadFavorites() {
  const data = await favoriteApi.list();
  items.value = Array.isArray(data) ? data : [];
}

function priceLabel(item) {
  return item.tradeType === '월세'
    ? `월세 ${item.deposit}/${item.monthlyRent}`
    : `${item.tradeType} ${item.deposit}`;
}

function pyeong(areaM2) {
  return Math.floor(areaM2 / PYEONG);
}

function floorLabel(floorInfo) {
  const head = floorInfo.split('/')[0].trim();
  return /^\d+$/.test(head) ? `${head}층` : head;
}

function goDetail(item) {
  router.push(`/properties/${item.propertyId}`);
}

async function removeFavorite(item) {
  removingId.value = item.propertyId;
  try {
    await favoriteApi.remove(item.propertyId);
    items.value = items.value.filter((i) => i.propertyId !== item.propertyId);
  } finally {
    removingId.value = '';
  }
}
</script>

<style scoped>
.favorite-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  background: var(--bg);
}

.content {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 12px 16px 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.content :deep(.simplebar-content) {
  display: flex;
  flex-direction: column;
  gap: 12px; /* 카드 간 세로 간격 복구 */
  padding: 0 16px;
}

.count-label {
  font-size: 12px;
  font-weight: 500;
  color: var(--kb-silver);
}

.cards {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.card {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: var(--white);
  border: 1px solid var(--border);
  border-radius: var(--radius-card);
  cursor: pointer;
}

.thumb {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 76px;
  height: 76px;
  flex-shrink: 0;
  background: var(--yellow-tint);
  border-radius: 10px;
}

.card-texts {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.card-price {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
}

.card-meta {
  font-size: 12px;
  color: var(--kb-gray);
}

.card-address {
  font-size: 11px;
  color: var(--kb-silver);
}

.heart-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.heart-btn:disabled {
  opacity: 0.5;
}

.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 40px 20px;
  text-align: center;
}

.empty-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-primary);
}

.empty-sub {
  font-size: 12px;
  color: var(--kb-silver);
}
</style>
