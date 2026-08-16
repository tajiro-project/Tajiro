<template>
  <div class="seller-home">
    <simplebar class="content">
      <section class="greeting">
        <div class="greeting-texts">
          <p class="greeting-title">{{ greeting }}</p>
          <p class="greeting-sub">등록한 매물을 관리해 보세요</p>
        </div>
        <div class="account-btns">
          <button class="chip-btn danger" type="button" @click="handleLogout">
            로그아웃
          </button>
          <button
            class="chip-btn"
            type="button"
            @click="router.push('/seller/profile')"
          >
            내 정보 수정
          </button>
        </div>
      </section>

      <div class="property-actions">
        <section class="stats">
          <div class="stat">
          <p class="stat-value">{{ propertyCount }}</p>
            <p class="stat-label">등록 매물</p>
          </div>
          <span class="stat-divider" />
          <div class="stat">
          <p class="stat-value">{{ favoriteCount }}</p>
            <p class="stat-label">찜</p>
          </div>
        </section>

        <button
          class="action action-primary"
          type="button"
          @click="router.push('/seller/register/1')"
        >
          <span class="action-icon">+</span>
          <span class="action-texts">
            <span class="action-title">매물 등록하기</span>
            <span class="action-sub">사진·조건 입력 후 바로 노출</span>
          </span>
          <span class="action-arrow">›</span>
        </button>

        <button
          class="action"
          type="button"
          @click="router.push('/seller/properties')"
        >
          <span class="action-icon tint">☰</span>
          <span class="action-texts">
            <span class="action-title">내 매물 관리</span>
            <span class="action-sub muted">수정 및 삭제</span>
          </span>
          <span class="action-arrow muted">›</span>
        </button>
      </div>

      <section class="recent-section">
        <div class="section-head">
          <p class="section-title">최근 등록 매물</p>
          <button
            class="section-more"
            type="button"
            @click="router.push('/seller/properties')"
          >
            전체 보기 ›
          </button>
        </div>

        <ul v-if="recent.length" class="recent">
          <li v-for="item in recent" :key="item.propertyId">
          <div class="recent-card">
              <span class="thumb">
                <img
                  v-if="item.imageUrl"
                  :src="item.imageUrl"
                  :alt="titleOf(item)"
                  class="thumb-img"
                />
                <svg
                  v-else
                  width="25"
                  height="25"
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
              <span class="recent-texts">
                <span
                  class="status-badge"
                  :class="{ done: !item.transactionStatus }"
                >
                  {{ statusLabel(item) }}
                </span>
                <span class="recent-title">{{ titleOf(item) }}</span>
                <span class="recent-price">{{ priceLabel(item) }}</span>
                <span class="recent-favorite"
                  >찜 {{ item.favoriteCount ?? 0 }}</span
                >
              </span>
          </div>
          </li>
        </ul>
        <p v-else class="empty">아직 등록한 매물이 없어요</p>
      </section>
    </simplebar>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import simplebar from 'simplebar-vue';
import client, { withMock } from '@/api/client';
import { sellerApi } from '@/api/services';
import { priceLabel, statusLabel, titleOf } from '@/utils/sellerProperty';

const router = useRouter();
const userName = ref('');
const items = ref([]);
const propertyCount = ref(0);
const favoriteCount = ref(0);

const greeting = computed(() =>
  userName.value ? `안녕하세요, ${userName.value}님` : '안녕하세요',
);

const recent = computed(() => items.value.slice(0, 3));

onMounted(() => {
  loadUser();
  loadProperties();
});

async function loadUser() {
  try {
    const response = await client.get('/users/me/dashboard');
    userName.value = (response.data?.data ?? response.data)?.name ?? '';
  } catch {
    userName.value = '';
  }
}

async function loadProperties() {
  const data = await sellerApi.myProperties({ page: 0, size: 3, status: 'ALL' });
  const payload = data?.data ?? data;
  const content = Array.isArray(payload) ? payload : (payload?.content ?? []);
  items.value = content.map((item) => ({
    ...item,
    propertyId: item.propertyId ?? item.id,
  }));
  propertyCount.value = payload?.counts?.all ?? content.length;
  favoriteCount.value =
    payload?.counts?.favorites ??
    content.reduce((sum, item) => sum + (item.favoriteCount ?? 0), 0);
}

async function handleLogout() {
  await withMock(() => client.post('/auth/logout'), {});
  localStorage.removeItem('accessToken');
  router.push('/login');
}
</script>

<style scoped>
.seller-home {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  background: var(--bg);
}

.content {
  flex: 1;
  min-height: 0;
  padding: 0 16px 22px;
}

.content :deep(.simplebar-content) {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.property-actions,
.recent-section {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.greeting {
  display: flex;
  align-items: center;
  gap: 12px;
}

.greeting-texts {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.greeting-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}

.greeting-sub {
  font-size: 13px;
  color: var(--kb-silver);
}

.account-btns {
  display: flex;
  flex-direction: column;
  gap: 5px;
  width: 95px;
  flex-shrink: 0;
}

.chip-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 20px;
  width: 95px;
  padding: 0 11px;
  background: var(--white);
  border: 1px solid var(--border);
  border-radius: 14px;
  font-size: 11px;
  font-weight: 500;
  color: var(--kb-gray);
}

.chip-btn.danger {
  border-color: var(--danger);
  color: var(--danger);
}

.stats {
  display: flex;
  align-items: center;
  padding: 16px 0;
  background: var(--white);
  border: 1px solid var(--border);
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-card);
}

.stat {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-value {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}

.stat-label {
  font-size: 11px;
  font-weight: 500;
  color: var(--kb-silver);
}

.stat-divider {
  width: 1px;
  height: 26px;
  background: var(--border);
}

.action {
  display: flex;
  align-items: center;
  gap: 13px;
  padding: 16px 14px 16px 16px;
  background: var(--white);
  border: 1px solid var(--border);
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-card);
  text-align: left;
}

.action-primary {
  background: var(--kb-yellow-header);
  border-color: transparent;
  box-shadow: 0 4px 12px rgba(255, 189, 0, 0.3);
}

.action-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  flex-shrink: 0;
  background: var(--white);
  border-radius: 13px;
  font-size: 19px;
  font-weight: 700;
  color: var(--kb-yellow);
}

.action-icon.tint {
  background: #fff6db;
  border-radius: 50px;
  font-size: 14px;
  color: #b88c0d;
}

.action-texts {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.action-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
}

.action-primary .action-title {
  color: #211a00;
}

.action-sub {
  font-size: 12px;
  color: #5c4705;
}

.action-sub.muted {
  color: var(--kb-silver);
}

.action-arrow {
  flex-shrink: 0;
  font-size: 20px;
  color: #664f05;
}

.action-arrow.muted {
  color: var(--kb-silver);
}

.section-head {
  display: flex;
  align-items: center;
}

.section-title {
  flex: 1;
  min-width: 0;
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
}

.section-more {
  font-size: 12px;
  font-weight: 500;
  color: var(--kb-silver);
}

.recent {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.recent-card {
  display: flex;
  align-items: center;
  gap: 11px;
  width: 100%;
  padding: 12px;
  background: var(--white);
  border: 1px solid var(--border);
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-card);
  text-align: left;
}

.thumb {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 60px;
  flex-shrink: 0;
  background: var(--yellow-tint);
  border-radius: 10px;
  overflow: hidden;
}

.thumb-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.recent-texts {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
}

.status-badge {
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

.recent-title {
  width: 100%;
  font-size: 14px;
  font-weight: 700;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recent-price {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-primary);
}

.recent-favorite {
  font-size: 11px;
  color: var(--kb-silver);
}

.empty {
  padding: 32px 0;
  font-size: 12.5px;
  color: var(--kb-silver);
  text-align: center;
}
</style>
