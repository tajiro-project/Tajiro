<template>
  <div class="home">
    <header class="home-header">
      <p class="logo">
        <span class="logo-badge">
          <img :src="logoImg" alt="타지로" class="logo-img" />
        </span>
        타지로
      </p>
      <p class="headline">어디로 이주하세요?</p>
      <p class="headline-sub">그 지역 주변 매물을 바로 찾아드려요</p>

      <hr class="header-divider" />

      <!-- 핵심 기능: 가치관 입력 / 매물 검색 (헤더 내 최상단, 원준수 멘토님 피드백 #1) -->
      <button
        class="menu-card primary"
        type="button"
        @click="router.push('/preferences/1')"
      >
        <span class="menu-icon primary">
          <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
            <path
              d="M10 2c-3 0-5.5 2.4-5.5 5.5 0 4 5.5 10.5 5.5 10.5s5.5-6.5 5.5-10.5C15.5 4.4 13 2 10 2z"
              stroke="#60584c"
              stroke-width="1.8"
              stroke-linejoin="round"
            />
            <circle
              cx="10"
              cy="7.5"
              r="1.8"
              stroke="#60584c"
              stroke-width="1.8"
            />
          </svg>
        </span>
        <span class="menu-texts">
          <span class="menu-title">매물 검색</span>
          <span class="menu-sub">가치관 입력하고 맞춤 매물 찾기</span>
        </span>
        <svg
          class="menu-chevron"
          width="16"
          height="16"
          viewBox="0 0 16 16"
          fill="none"
        >
          <path
            d="M6 3.5l5 4.5-5 4.5"
            stroke="#545045"
            stroke-width="1.6"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
      </button>
    </header>

    <simplebar class="scroll-area">
      <section class="quick-menu">
        <p class="section-title">무엇을 도와드릴까요?</p>

        <div class="menu-grid">
          <!-- 정책 · 대출 -->
          <button
            class="menu-card"
            type="button"
            @click="router.push('/benefits/policies')"
          >
            <span class="menu-icon">
              <svg
                width="18"
                height="18"
                viewBox="0 0 18 18"
                fill="none"
                aria-hidden="true"
              >
                <path
                  d="M2.5 6.5L9 3l6.5 3.5-6.5 3.5-6.5-3.5z"
                  stroke="#60584c"
                  stroke-width="1.5"
                  stroke-linejoin="round"
                />
                <path
                  d="M2.5 6.5V12L9 15.5l6.5-3.5V6.5"
                  stroke="#60584c"
                  stroke-width="1.5"
                  stroke-linejoin="round"
                />
              </svg>
            </span>

            <span class="menu-title small">정책 · 대출</span>
            <span class="menu-sub">청년 혜택 · KB 상품</span>
          </button>

          <!-- 안전 거래 가이드 -->
          <button
            class="menu-card"
            type="button"
            @click="router.push('/safety-guide')"
          >
            <span class="menu-icon">
              <svg
                width="18"
                height="18"
                viewBox="0 0 18 18"
                fill="none"
                aria-hidden="true"
              >
                <path
                  d="M9 2.2l5.5 2v3.9c0 3.6-2.3 6.4-5.5 7.7-3.2-1.3-5.5-4.1-5.5-7.7V4.2L9 2.2z"
                  stroke="#60584c"
                  stroke-width="1.5"
                  stroke-linejoin="round"
                />
                <path
                  d="M6.3 8.8l1.7 1.7 3.7-4"
                  stroke="#60584c"
                  stroke-width="1.5"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
            </span>

            <span class="menu-title small">안전 거래 가이드</span>
            <span class="menu-sub">계약 전 필수 가이드</span>
          </button>
        </div>
      </section>

      <!-- 매물별 맞춤 정책 · 금융 추천 미리보기 (mock, 원준수 멘토님 피드백 #2) -->
      <section class="benefit-preview">
        <div class="benefit-title-row">
          <p class="section-title">나에게 맞는 정책·금융 혜택을 추천해드려요</p>
          <span class="benefit-example-tag">예시</span>
        </div>
        <div class="benefit-list">
          <div
            v-for="item in mockBenefitPreview"
            :key="item.title"
            class="benefit-item"
          >
            <span class="benefit-badge" :class="item.type">{{
              item.type === 'policy' ? '정책' : '금융'
            }}</span>
            <div class="benefit-texts">
              <span class="benefit-title">{{ item.title }}</span>
              <span class="benefit-sub">{{ item.sub }}</span>
            </div>
          </div>
        </div>
        <p class="benefit-note">
          가치관을 입력하면 내 조건에 맞는 혜택만 골라서 보여드려요
        </p>
      </section>

      <section class="safety-card">
        <p class="safety-title">
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
            <path
              d="M8 1.5l5.5 2v3.8c0 3.5-2.3 6.2-5.5 7.2-3.2-1-5.5-3.7-5.5-7.2V3.5L8 1.5z"
              stroke="#545045"
              stroke-width="1.4"
              stroke-linejoin="round"
            />
            <path
              d="M5.7 8l1.6 1.6L10.5 6"
              stroke="#545045"
              stroke-width="1.4"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
          안전까지 확인하고 이사하세요
        </p>

        <ul class="safety-list">
          <li class="safety-item">
            <span class="safety-icon">
              <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
                <rect
                  x="2"
                  y="6"
                  width="10"
                  height="7"
                  rx="1.5"
                  stroke="#60584c"
                  stroke-width="1.4"
                />
                <path
                  d="M12 8.5l4-2v6l-4-2"
                  stroke="#60584c"
                  stroke-width="1.4"
                  stroke-linejoin="round"
                />
              </svg>
            </span>
            <span class="safety-texts">
              <span class="safety-item-title">집 근처 치안</span>
              <span class="safety-item-sub">주변 CCTV 개수를 확인해요</span>
            </span>
          </li>
          <li class="safety-item">
            <span class="safety-icon">
              <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
                <circle
                  cx="9"
                  cy="9"
                  r="6.5"
                  stroke="#60584c"
                  stroke-width="1.4"
                />
                <path
                  d="M9 5.5v7M5.5 9h7"
                  stroke="#60584c"
                  stroke-width="1.4"
                  stroke-linecap="round"
                />
              </svg>
            </span>
            <span class="safety-texts">
              <span class="safety-item-title">여성 안심</span>
              <span class="safety-item-sub"
                >안전 비상벨 · 보안등을 확인해요</span
              >
            </span>
          </li>
          <li class="safety-item">
            <span class="safety-icon">
              <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
                <circle
                  cx="9"
                  cy="5.5"
                  r="2.5"
                  stroke="#60584c"
                  stroke-width="1.4"
                />
                <path
                  d="M4 15v-2a5 5 0 0110 0v2"
                  stroke="#60584c"
                  stroke-width="1.4"
                  stroke-linecap="round"
                />
              </svg>
            </span>
            <span class="safety-texts">
              <span class="safety-item-title">자녀 안전</span>
              <span class="safety-item-sub"
                >어린이 보호구역을 함께 보여줘요</span
              >
            </span>
          </li>
        </ul>
      </section>
    </simplebar>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';
import simplebar from 'simplebar-vue';
import logoImg from '@/assets/img/logo.png';

const router = useRouter();

const mockBenefitPreview = [
  {
    type: 'policy',
    title: '청년 전세자금 대출',
    sub: '최대 2억원 · 저금리 지원',
  },
  {
    type: 'finance',
    title: 'KB 청년 전월세보증금 대출',
    sub: '우대금리 최대 0.3%p',
  },
];
</script>

<style scoped>
.home {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--bg);
}

.home-header {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 13px 16px 20px;
  background: var(--kb-yellow-header);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 22px;
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.logo-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 8px;
  overflow: hidden;
}

.logo-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.headline {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
}

.headline-sub {
  font-size: 11.5px;
  color: var(--kb-gray);
}

.header-divider {
  width: 100%;
  height: 0;
  margin: 6px 0 2px;
  border: none;
  border-top: 1px solid rgba(0, 0, 0, 0.08);
}

.scroll-area {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.scroll-area :deep(.simplebar-content) {
  display: flex;
  flex-direction: column;
  gap: 12px; /* 카드 간 세로 간격 복구 */
  padding: 0 16px;
}

.safety-card {
  display: flex;
  flex-direction: column;
  padding: 15px 16px 16px;
  background: var(--white);
  border: 1px solid var(--border);
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-card);
}

.safety-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 12px;
}

.safety-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.safety-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.safety-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  flex-shrink: 0;
  background: var(--yellow-tint);
  border-radius: 100px;
}

.safety-texts {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.safety-item-title {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-primary);
}

.safety-item-sub {
  font-size: 11px;
  color: var(--kb-silver);
}

.section-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 10px;
}

/* ---- 정책·금융 맞춤 추천 미리보기 ---- */
.benefit-preview {
  padding: 15px 16px 16px;
  background: var(--white);
  border: 1px solid var(--border);
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-card);
}

.benefit-title-row {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 12px;
}

.benefit-title-row .section-title {
  margin-bottom: 0;
  white-space: nowrap;
}

.benefit-example-tag {
  flex-shrink: 0;
  font-size: 10.5px;
  font-weight: 700;
  padding: 4px 8px;
  border-radius: 100px;
  background: var(--bg);
  color: var(--kb-gray);
}

.benefit-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 12px;
}

.benefit-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  background: var(--bg);
  border-radius: 10px;
}

.benefit-badge {
  flex-shrink: 0;
  font-size: 10.5px;
  font-weight: 700;
  padding: 4px 8px;
  border-radius: 100px;
}

.benefit-badge.policy {
  background: var(--yellow-tint);
  color: var(--kb-dark-gray);
}

.benefit-badge.finance {
  background: #e6f0ff;
  color: var(--blue);
}

.benefit-texts {
  display: flex;
  flex-direction: column;
  gap: 1px;
  min-width: 0;
}

.benefit-title {
  font-size: 12.5px;
  font-weight: 700;
  color: var(--text-primary);
}

.benefit-sub {
  font-size: 10.5px;
  color: var(--kb-silver);
}

.benefit-note {
  margin-top: 10px;
  font-size: 10.5px;
  color: var(--kb-silver);
  text-align: center;
}

.menu-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: var(--white);
  border: 1px solid var(--border);
  border-radius: var(--radius-card);
  text-align: left;
}

.menu-card.primary {
  width: 100%;
  background: var(--yellow-tint);
  box-shadow: var(--shadow-card);
}

.menu-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  flex-shrink: 0;
  background: var(--bg);
  border-radius: 100px;
}

.menu-icon.primary {
  width: 36px;
  height: 36px;
  background: var(--kb-yellow-header);
}

.menu-texts {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.menu-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
}

.menu-title.small {
  font-size: 13px;
}

.menu-sub {
  font-size: 11px;
  color: var(--kb-silver);
}

.menu-chevron {
  flex-shrink: 0;
}

.menu-grid {
  display: flex;
  gap: 10px;
  margin-top: 12px;
}

.menu-grid .menu-card {
  flex: 1;
  flex-direction: column;
  align-items: flex-start;
  gap: 10px;
}

.menu-grid .menu-icon {
  width: 36px;
  height: 36px;
  background: var(--bg);
}
</style>
