<template>
    <div class="home">
        <header class="home-header">
            <p class="logo">
                <span class="logo-badge">🏠</span>
                타지로
            </p>
            <p class="headline">어디로 이주하세요?</p>
            <p class="headline-sub">그 지역 주변 매물을 바로 찾아드려요</p>

            <button class="search-bar" type="button" @click="goRegionSearch">
                <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
                    <circle cx="8" cy="8" r="5.5" stroke="#8a8d8f" stroke-width="1.6" />
                    <path d="M12.2 12.2L16 16" stroke="#8a8d8f" stroke-width="1.6" stroke-linecap="round" />
                </svg>
                <span class="search-text">{{ targetRegion || '이주할 지역 · 동네 검색' }}</span>
                <span class="search-submit">
                    <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                        <path d="M4 8h8M8 4l4 4-4 4" stroke="#545045" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" />
                    </svg>
                </span>
            </button>
        </header>

        <div class="scroll-area">
            <section class="safety-card">
                <p class="safety-title">
                    <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                        <path d="M8 1.5l5.5 2v3.8c0 3.5-2.3 6.2-5.5 7.2-3.2-1-5.5-3.7-5.5-7.2V3.5L8 1.5z" stroke="#545045" stroke-width="1.4" stroke-linejoin="round" />
                        <path d="M5.7 8l1.6 1.6L10.5 6" stroke="#545045" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round" />
                    </svg>
                    안전까지 확인하고 이사하세요
                </p>

                <ul class="safety-list">
                    <li class="safety-item">
                        <span class="safety-icon">
                            <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
                                <rect x="2" y="6" width="10" height="7" rx="1.5" stroke="#60584c" stroke-width="1.4" />
                                <path d="M12 8.5l4-2v6l-4-2" stroke="#60584c" stroke-width="1.4" stroke-linejoin="round" />
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
                                <circle cx="9" cy="9" r="6.5" stroke="#60584c" stroke-width="1.4" />
                                <path d="M9 5.5v7M5.5 9h7" stroke="#60584c" stroke-width="1.4" stroke-linecap="round" />
                            </svg>
                        </span>
                        <span class="safety-texts">
                            <span class="safety-item-title">여성 안심</span>
                            <span class="safety-item-sub">안전 비상벨 · 보안등을 확인해요</span>
                        </span>
                    </li>
                    <li class="safety-item">
                        <span class="safety-icon">
                            <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
                                <circle cx="9" cy="5.5" r="2.5" stroke="#60584c" stroke-width="1.4" />
                                <path d="M4 15v-2a5 5 0 0110 0v2" stroke="#60584c" stroke-width="1.4" stroke-linecap="round" />
                            </svg>
                        </span>
                        <span class="safety-texts">
                            <span class="safety-item-title">자녀 안전</span>
                            <span class="safety-item-sub">어린이 보호구역을 함께 보여줘요</span>
                        </span>
                    </li>
                </ul>
            </section>

            <section class="quick-menu">
                <p class="section-title">무엇을 도와드릴까요?</p>

                <button class="menu-card primary" type="button" @click="router.push('/properties')">
                    <span class="menu-icon primary">
                        <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                            <path d="M10 2c-3 0-5.5 2.4-5.5 5.5 0 4 5.5 10.5 5.5 10.5s5.5-6.5 5.5-10.5C15.5 4.4 13 2 10 2z" stroke="#545045" stroke-width="1.6" stroke-linejoin="round" />
                            <circle cx="10" cy="7.5" r="1.8" stroke="#545045" stroke-width="1.6" />
                        </svg>
                    </span>
                    <span class="menu-texts">
                        <span class="menu-title">매물 검색</span>
                        <span class="menu-sub">가치관 입력하고 맞춤 매물 찾기</span>
                    </span>
                    <svg class="menu-chevron" width="16" height="16" viewBox="0 0 16 16" fill="none">
                        <path d="M6 3.5l5 4.5-5 4.5" stroke="#545045" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" />
                    </svg>
                </button>

                <div class="menu-grid">
                    <button class="menu-card" type="button" @click="router.push('/compare-box')">
                        <span class="menu-icon">
                            <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
                                <path d="M9 2v14M9 2L5.5 5.5M9 2l3.5 3.5M9 16l-3.5-3.5M9 16l3.5-3.5" stroke="#60584c" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" />
                            </svg>
                        </span>
                        <span class="menu-title small">매물 비교</span>
                        <span class="menu-sub">최대 3개 AI 코칭</span>
                    </button>
                    <button class="menu-card" type="button" @click="router.push('/benefits/policies')">
                        <span class="menu-icon">
                            <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
                                <path d="M2.5 6.5L9 3l6.5 3.5-6.5 3.5-6.5-3.5z" stroke="#60584c" stroke-width="1.5" stroke-linejoin="round" />
                                <path d="M2.5 6.5V12L9 15.5l6.5-3.5V6.5" stroke="#60584c" stroke-width="1.5" stroke-linejoin="round" />
                            </svg>
                        </span>
                        <span class="menu-title small">정책 · 대출</span>
                        <span class="menu-sub">청년 혜택 · KB 상품</span>
                    </button>
                </div>
            </section>
        </div>

        <AppTabBar active="home" />
    </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import client, { withMock } from '@/api/client';
import { mockDashboard } from '@/api/mockData';
import AppTabBar from '@/components/AppTabBar.vue';

const router = useRouter();
const targetRegion = ref('');

onMounted(loadTargetRegion);

async function loadTargetRegion() {
    try {
        const data = await withMock(() => client.get('/users/me/dashboard'), mockDashboard);
        targetRegion.value = data?.targetRegion ?? '';
    } catch {
        targetRegion.value = '';
    }
}

function goRegionSearch() {
    router.push('/location-search');
}
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
    gap: 6px;
    font-size: 17px;
    font-weight: 900;
    color: var(--text-primary);
    margin-bottom: 8px;
}

.logo-badge {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 22px;
    height: 22px;
    font-size: 13px;
    background: var(--white);
    border-radius: 8px;
}

.headline {
    font-size: 16px;
    font-weight: 700;
    color: var(--text-primary);
}

.headline-sub {
    font-size: 11.5px;
    color: var(--kb-gray);
    margin-bottom: 10px;
}

.search-bar {
    display: flex;
    align-items: center;
    gap: 10px;
    width: 100%;
    padding: 8px 8px 8px 16px;
    background: var(--white);
    border-radius: var(--radius-input);
    box-shadow: var(--shadow-card);
}

.search-text {
    flex: 1;
    text-align: left;
    font-size: 13px;
    color: var(--kb-silver);
}

.search-submit {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    background: var(--kb-yellow);
    border-radius: 100px;
    flex-shrink: 0;
}

.scroll-area {
    flex: 1;
    overflow-y: auto;
    padding: 16px;
    display: flex;
    flex-direction: column;
    gap: 16px;
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
    border-color: var(--kb-yellow);
    margin-bottom: 10px;
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
    background: var(--kb-yellow);
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
