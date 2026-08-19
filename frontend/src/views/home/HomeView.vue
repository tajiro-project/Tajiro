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
        data-tour="home-property-search"
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
            data-tour="home-benefit"
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
            data-tour="home-safety-guide"
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

      <section class="banner-carousel">
        <p class="section-title">안전과 혜택도 함께 확인하세요</p>
        <div class="banner-viewport">
        <div
          ref="trackRef"
          class="banner-track"
          @scroll.passive="onScroll"
          @pointerdown="pauseAuto"
          @pointerup="resumeAuto"
          @pointercancel="resumeAuto"
        >
          <div class="banner">
            <section class="safety-card">
              <p class="safety-title">
              <ShieldCheck :size="16" :stroke-width="1.8" color="#545045" />
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
          </div>
          <!-- 정책 · 금융 매칭 기준 안내 (원준수 멘토님 피드백 #2) -->
          <div class="banner">
          <section class="benefit-preview">
            <p class="section-title">
              <Landmark :size="16" :stroke-width="1.8" color="#545045" />
              나에게 맞는 정책·금융 혜택을 추천해드려요
            </p>
            <div class="benefit-list">
              <div
                v-for="item in benefitMatchCriteria"
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
            <button
              type="button"
              class="banner-cta"
              @click="router.push('/benefits/policies')"
            >
              내 선호 지역 맞춤 혜택 보기
            </button>
          </section>
          </div>
          <!-- 비교함 AI 리포트 안내 -->
          <div class="banner">
          <section class="safety-card">
            <p class="safety-title">
              <Bot :size="16" :stroke-width="1.8" color="#545045" />
              매물 2~3개, AI 리포트로 비교해요
            </p>

            <ul class="safety-list">
              <li class="safety-item">
                <span class="safety-icon">
                  <svg width="18" height="18" viewBox="0 0 16 16" fill="none">
                    <path
                      d="M2 14V7M6 14V3M10 14V9M14 14V5"
                      stroke="#60584c"
                      stroke-width="1.4"
                      stroke-linecap="round"
                    />
                  </svg>
                </span>
                <span class="safety-texts">
                  <span class="safety-item-title">가치관 우선순위로 점수 비교</span>
                  <span class="safety-item-sub"
                    >통근·비용·인프라 등을 내가 고른 기준으로 비교해요</span
                  >
                </span>
              </li>
              <li class="safety-item">
                <span class="safety-icon ai-badge">AI</span>
                <span class="safety-texts">
                  <span class="safety-item-title">AI 추천 매물까지 확인</span>
                  <span class="safety-item-sub"
                    >가장 적합한 매물과 이유를 코칭 요약으로 알려드려요</span
                  >
                </span>
              </li>
            </ul>

            <button
              type="button"
              class="banner-cta"
              @click="router.push('/compare-box')"
            >
              비교함 담으러 가기
            </button>
          </section>
          </div>
        </div>
        <button
          type="button"
          class="banner-nav prev"
          aria-label="이전 배너"
          @click="goPrev"
        >
          <ChevronLeft :size="18" :stroke-width="2.2" />
        </button>
        <button
          type="button"
          class="banner-nav next"
          aria-label="다음 배너"
          @click="goNext"
        >
          <ChevronRight :size="18" :stroke-width="2.2" />
        </button>
        </div>
        <div class="dots">
          <button
            v-for="(banner, index) in BANNERS"
            :key="banner"
            class="dot"
            :class="{ on: index === activeIndex }"
            type="button"
            :aria-label="`${index + 1}번 배너 보기`"
            @click="goBanner(index)"
          />
        </div>
      </section>
    </simplebar>

    <OnboardingSpotlight group-name="home" :steps="ONBOARDING_STEPS.home" />
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import simplebar from 'simplebar-vue';
import {
  Bot,
  ChevronLeft,
  ChevronRight,
  Landmark,
  ShieldCheck,
} from 'lucide-vue-next';
import logoImg from '@/assets/img/logo.png';
import OnboardingSpotlight from '@/components/OnboardingSpotlight.vue';
import { ONBOARDING_STEPS } from '@/constants/onboardingSteps';

const router = useRouter();

const AUTO_INTERVAL = 5000;
const BANNERS = ['safety', 'benefit', 'compare'];

const trackRef = ref(null);
const activeIndex = ref(0);

let autoTimer = null;

onMounted(startAuto);
onBeforeUnmount(stopAuto);

function bannerStep() {
  const track = trackRef.value;
  if (!track) return 0;

  const banner = track.querySelector('.banner');
  if (!banner) return 0;

  const gap = parseFloat(getComputedStyle(track).columnGap) || 0;
  return banner.offsetWidth + gap;
}

function onScroll() {
  const track = trackRef.value;
  const step = bannerStep();
  if (!track || !step) return;

  activeIndex.value = Math.round(track.scrollLeft / step);
}

function goBanner(index) {
  const track = trackRef.value;
  const step = bannerStep();
  if (!track || !step) return;

  track.scrollTo({ left: step * index, behavior: 'smooth' });
}

function goPrev() {
  goBanner((activeIndex.value - 1 + BANNERS.length) % BANNERS.length);
}

function goNext() {
  goBanner((activeIndex.value + 1) % BANNERS.length);
}

function startAuto() {
  stopAuto();
  if (BANNERS.length < 2) return;
  if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) return;

  autoTimer = setInterval(() => {
    goBanner((activeIndex.value + 1) % BANNERS.length);
  }, AUTO_INTERVAL);
}

function stopAuto() {
  if (autoTimer) clearInterval(autoTimer);
  autoTimer = null;
}

function pauseAuto() {
  stopAuto();
}

function resumeAuto() {
  startAuto();
}

const benefitMatchCriteria = [
  {
    type: 'policy',
    title: '나이와 지역에 맞는 정책 매칭',
    sub: '주거·취업 등 9개 분야로 분류해요',
  },
  {
    type: 'finance',
    title: '거래유형(월세·전세·매매)에 맞는 상품 매칭',
    sub: '보증·담보·기타 유형으로 다시 나눠요',
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
  padding: 13px 16px 14px;
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
  margin: 6px 0 8px;
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
  justify-content: center;
  height: 100%;
  margin: 0 6px;
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
  margin-bottom: 18px;
}

.safety-list {
  display: flex;
  flex-direction: column;
  gap: 22px;
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

.ai-badge {
  font-size: 11px;
  font-weight: 800;
  color: #545045;
  letter-spacing: -0.2px;
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

.banner-cta {
  width: 100%;
  margin-top: 18px;
  padding: 11px 0;
  border: none;
  border-radius: 10px;
  background: var(--yellow-tint);
  color: var(--kb-dark-gray);
  font-size: 12.5px;
  font-weight: 800;
  text-align: center;
}

.section-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 10px;
}

/* ---- 정책·금융 맞춤 추천 미리보기 ---- */
.benefit-preview {
  display: flex;
  flex-direction: column;
  justify-content: center;
  height: 100%;
  margin: 0 6px;
  padding: 15px 16px 16px;
  background: var(--white);
  border: 1px solid var(--border);
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-card);
}

.benefit-preview .section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  margin-bottom: 12px;
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
  flex: 1;
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

/* --- 배너 캐러셀 --- */
.banner-carousel .section-title {
  margin-bottom: 0;
}

.banner-carousel {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 10px;
}

.banner-track {
  display: flex;
  gap: 12px;
  padding: 8px 0;
  overflow-x: auto;
  scroll-snap-type: x mandatory;
  scrollbar-width: none;
}

.banner-track::-webkit-scrollbar {
  display: none;
}

.banner-viewport {
  position: relative;
}

.banner-nav {
  position: absolute;
  top: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 2px 8px rgba(33, 30, 24, 0.18);
  color: #545045;
  opacity: 0;
  transform: translateY(-50%);
  transition: opacity 0.2s ease;
}

.banner-viewport:hover .banner-nav,
.banner-nav:focus-visible {
  opacity: 1;
}

.banner-nav.prev {
  left: 4px;
}

.banner-nav.next {
  right: 4px;
}

.banner {
  flex: 0 0 100%;
  min-width: 0;
  box-sizing: border-box;
  scroll-snap-align: start;
}

.dots {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
}

.dot {
  width: 5px;
  height: 5px;
  padding: 0;
  border: 0;
  border-radius: 3px;
  background: #d9d5cc;
  transition: width 0.2s ease;
}

.dot.on {
  width: 16px;
  background: var(--kb-yellow);
}
</style>
