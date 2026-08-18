<template>
  <div class="mypage">

    <simplebar class="scroll-area">
      <section class="profile-card">
        <span class="avatar">
          <svg
            width="22"
            height="22"
            viewBox="0 0 22 22"
            fill="none"
          >
            <circle
              cx="11"
              cy="7.5"
              r="3.5"
              stroke="#60584c"
              stroke-width="1.6"
            />
            <path
              d="M4 18c0-3.3 3.1-6 7-6s7 2.7 7 6"
              stroke="#60584c"
              stroke-width="1.6"
              stroke-linecap="round"
            />
          </svg>
        </span>
        <span class="profile-texts">
          <span class="profile-name">{{ dashboard.name }}</span>
          <span class="profile-sub">{{ regionSummary }}</span>
        </span>
      </section>

      <section class="stat-row">
        <button
          class="stat-box"
          type="button"
          @click="router.push('/favorites')"
        >
          <p class="stat-value">{{ dashboard.favoriteCount }}</p>
          <p class="stat-label">찜한 매물</p>
        </button>
        <button
          class="stat-box"
          type="button"
          @click="router.push('/reports')"
        >
          <p class="stat-value">{{ dashboard.reportCount }}</p>
          <p class="stat-label">비교 리포트</p>
        </button>
      </section>

      <section class="priority-card">
        <p class="section-title">내 가치관 우선순위</p>
        <p
          v-if="sortedPriorities.length === 0"
          class="priority-empty"
        >
          가치관 우선순위를 설정해주세요
        </p>
        <div
          v-else
          class="priority-chips"
        >
          <span
            class="priority-chip"
            v-for="priority in sortedPriorities"
            :key="priority.priorityOrder"
          >
            {{ priority.priorityOrder }}
            {{ criterionLabel(priority.criterion) }}
          </span>
        </div>
      </section>

      <section class="replay-card">
        <p class="section-title">온보딩 다시보기</p>
        <div class="replay-grid">
          <button
            v-for="item in TOUR_REPLAY_ITEMS"
            :key="item.group"
            class="replay-btn"
            type="button"
            @click="replayTour(item)"
          >
            {{ item.label }}
          </button>
        </div>
      </section>

      <nav class="menu-list">
        <button
          class="menu-row"
          type="button"
          @click="router.push('/profile-setup')"
        >
          <span>내 정보 관리 (소득 · 자산 · 직장)</span>
          <svg
            width="16"
            height="16"
            viewBox="0 0 16 16"
            fill="none"
          >
            <path
              d="M6 3.5l5 4.5-5 4.5"
              stroke="#8a8d8f"
              stroke-width="1.6"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
        </button>
      </nav>

      <p
        v-if="errorMessage"
        class="error-message"
      >
        {{ errorMessage }}
      </p>

      <button
        class="btn-cta logout-btn"
        type="button"
        @click="handleLogout"
      >
        로그아웃
      </button>
      <button
        class="withdraw-link"
        type="button"
        @click="handleWithdraw"
      >
        탈퇴하기
      </button>
    </simplebar>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import simplebar from 'simplebar-vue';
import client, { getApiErrorMessage, withMock } from '@/api/client';
import { mockDashboard } from '@/api/mockData';
import { useOnboardingTour } from '@/composables/useOnboardingTour';

const { startAt } = useOnboardingTour();

// 데이터 없이 언제든 단독으로 들어갈 수 있는 화면(홈)은 실제 경로로 바로 이동.
// 가치관설정·매물리스트·매물상세·비교함·비교(AI코칭)는 실제 내 데이터를 보여주거나(수정·저장
// 위험) 실제 데이터 상태에 따라 화면이 비거나(매물리스트: 가치관 미설정 시 /preferences로
// 튕겨나감) 깨질 수 있어서 ?demo=1로 들어가면 항상 예시 데이터로 보여준다 — 실제 API 호출·저장
// 없음. 이 다섯은 투어가 끝나면(완료/건너뛰기) 자동으로 마이페이지로 돌아온다.
const TOUR_REPLAY_ITEMS = [
  { group: 'home', label: '홈', path: '/home' },
  { group: 'preferences', label: '가치관 설정', path: '/preferences/1?demo=1' },
  { group: 'property-list', label: '매물 리스트', path: '/properties?demo=1' },
  { group: 'compare-box', label: '비교함', path: '/compare-box?demo=1' },
  { group: 'property-detail', label: '매물 상세', path: '/properties/demo?demo=1' },
  { group: 'compare', label: '비교 · AI코칭', path: '/compare?demo=1' },
];

const CRITERION_LABELS = {
  COMMUTE: '직주근접',
  COST: '가격 낮은 순',
  AMENITY: '편의',
  INFRA: '인프라',
  AREA: '매물면적',
};

const router = useRouter();
const errorMessage = ref('');
const dashboard = ref({
  name: '',
  targetRegion: '',
  birthDate: '',
  favoriteCount: 0,
  reportCount: 0,
  priorities: [],
});

const sortedPriorities = computed(() => {
  return [...dashboard.value.priorities].sort(
    (a, b) => a.priorityOrder - b.priorityOrder,
  );
});

const regionSummary = computed(() => {
  if (!dashboard.value.targetRegion) return '내 정보를 입력해주세요';

  const age = calcAge(dashboard.value.birthDate);
  const ageText = age === null ? '' : ` · 만 ${age}세`;
  return `${dashboard.value.targetRegion} 이주 준비 중${ageText}`;
});

onMounted(loadDashboard);

async function loadDashboard() {
  try {
    const data = await withMock(
      () => client.get('/users/me/dashboard'),
      mockDashboard,
    );
    const payload = data?.data ?? data;
    dashboard.value = { ...dashboard.value, ...payload };
  } catch {
    dashboard.value = { ...mockDashboard };
  }
}

function criterionLabel(criterion) {
  return CRITERION_LABELS[criterion] ?? criterion;
}

function replayTour(item) {
  startAt(item.group);
  router.push(item.path);
}

function calcAge(birthDate) {
  if (!birthDate) return null;

  const birth = new Date(birthDate);
  const today = new Date();
  let age = today.getFullYear() - birth.getFullYear();
  const hasHadBirthdayThisYear =
    today.getMonth() > birth.getMonth() ||
    (today.getMonth() === birth.getMonth() &&
      today.getDate() >= birth.getDate());

  if (!hasHadBirthdayThisYear) age -= 1;

  return age;
}

async function handleLogout() {
  await withMock(() => client.post('/auth/logout'), {});
  localStorage.removeItem('accessToken');
  router.push('/login');
}

async function handleWithdraw() {
  if (!confirm('정말 탈퇴하시겠어요? 탈퇴 시 계정 정보가 삭제됩니다.')) return;

  errorMessage.value = '';

  try {
    await client.delete('/users/me');
  } catch (error) {
    if (error.response) {
      errorMessage.value = getApiErrorMessage(
        error,
        '탈퇴에 실패했어요. 잠시 후 다시 시도해주세요.',
      );
      return;
    }
    // 백엔드 자체가 없을 때만 mock으로 간주하고 진행
  }

  localStorage.removeItem('accessToken');
  router.push('/login');
}
</script>

<style scoped>
.mypage {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--bg);
}

.scroll-area {
  padding: 16px;
}

.scroll-area :deep(.simplebar-content) {
  display: flex;
  flex-direction: column;
  gap: 12px; /* 카드 간 세로 간격 복구 */
  padding: 0 16px;
}

.profile-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: var(--white);
  border: 1px solid var(--border);
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-card);
}

.avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  flex-shrink: 0;
  background: var(--yellow-tint);
  border-radius: 100px;
}

.profile-texts {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.profile-name {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
}

.profile-sub {
  font-size: 11.5px;
  color: var(--kb-gray);
}

.stat-row {
  display: flex;
  gap: 10px;
}

.stat-box {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 14px 12px;
  background: var(--white);
  border: 1px solid var(--border);
  border-radius: var(--radius-card);
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
}

.stat-label {
  font-size: 11px;
  color: var(--kb-silver);
}

.priority-card {
  padding: 16px;
  background: var(--white);
  border: 1px solid var(--border);
  border-radius: var(--radius-card);
}

.section-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 10px;
}

.priority-chips {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.priority-empty {
  font-size: 12.5px;
  color: var(--kb-silver);
}

.priority-chip {
  padding: 7px 12px;
  background: var(--yellow-tint);
  border-radius: 100px;
  font-size: 11.5px;
  font-weight: 500;
  color: var(--kb-gray);
}

.replay-card {
  padding: 16px;
  background: var(--white);
  border: 1px solid var(--border);
  border-radius: var(--radius-card);
}

.replay-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}

.replay-btn {
  padding: 12px 8px;
  background: var(--bg);
  border: 1px solid var(--border);
  border-radius: 12px;
  font-size: 12.5px;
  font-weight: 600;
  color: var(--text-primary);
  text-align: center;
}

.replay-btn:last-child:nth-child(odd) {
  grid-column: 1 / -1;
}

.menu-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.menu-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  background: var(--white);
  border: 1px solid var(--border);
  border-radius: var(--radius-card);
  font-size: 13.5px;
  font-weight: 500;
  color: var(--text-primary);
  text-align: left;
}

.error-message {
  font-size: 12px;
  color: var(--danger);
  text-align: center;
}

.logout-btn {
  margin-top: 8px;
}

.withdraw-link {
  padding: 4px;
  font-size: 12px;
  color: var(--kb-silver);
  text-align: center;
}
</style>
