<template>
    <div class="register">
        <PageHeader title="회원가입" />

        <div class="scroll-area">
            <h1 class="headline">타지로와 함께<br />새 도시 정착을 시작해요</h1>

            <form class="register-form" @submit.prevent="handleRegister">
                <div class="field">
                    <label class="field-label" for="name">이름</label>
                    <input id="name" v-model="name" class="field-input" type="text" placeholder="실명을 입력해주세요" required />
                </div>
                <div class="field">
                    <label class="field-label" for="email">이메일</label>
                    <input id="email" v-model="email" class="field-input" type="email" placeholder="example@email.com" required />
                </div>
                <div class="field">
                    <label class="field-label" for="password">비밀번호</label>
                    <input id="password" v-model="password" class="field-input" type="password" placeholder="8자 이상, 영문+숫자 조합" required />
                </div>
                <div class="field">
                    <label class="field-label" for="passwordConfirm">비밀번호 확인</label>
                    <input id="passwordConfirm" v-model="passwordConfirm" class="field-input" type="password" placeholder="비밀번호를 다시 입력해주세요" required />
                </div>

                <div class="terms-box">
                    <button class="terms-all" type="button" @click="toggleAll">
                        <span class="checkbox" :class="{ on: isAllAgreed }">
                            <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
                                <path d="M2 6.5L4.7 9L10 3.5" stroke="#545045" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" />
                            </svg>
                        </span>
                        전체 동의
                    </button>

                    <ul class="terms-list">
                        <li v-for="term in termsList" :key="term.id" class="terms-row">
                            <button class="checkbox-btn" type="button" @click="toggleTerm(term.id)">
                                <span class="checkbox" :class="{ on: isAgreed(term.id) }">
                                    <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
                                        <path d="M2 6.5L4.7 9L10 3.5" stroke="#545045" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" />
                                    </svg>
                                </span>
                            </button>
                            <button class="terms-label" type="button" @click="openTermsSheet(term.id)">
                                <span>{{ term.label }}</span>
                                <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                                    <path d="M5.5 3.5l4 3.5-4 3.5" stroke="#8a8d8f" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" />
                                </svg>
                            </button>
                        </li>
                    </ul>
                </div>

                <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>

                <button class="btn-cta register-btn" type="submit" :disabled="!canSubmit || loading">가입하고 시작하기</button>
            </form>

            <p class="flow-hint">가입 후 내 정보 입력(1/2) → 가치관 설정(2/2)으로 이어져요</p>
        </div>

        <div v-if="activeTerm" class="sheet-overlay" @click="closeTermsSheet">
            <div class="sheet" @click.stop>
                <div class="sheet-handle"></div>
                <div class="sheet-header">
                    <p class="sheet-title">{{ activeTerm.label.replace('[필수] ', '') }}</p>
                    <button class="sheet-close" type="button" @click="closeTermsSheet">
                        <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
                            <path d="M4.5 4.5l9 9M13.5 4.5l-9 9" stroke="#60584c" stroke-width="1.6" stroke-linecap="round" />
                        </svg>
                    </button>
                </div>
                <div class="sheet-content">{{ activeTermContent }}</div>
                <button class="btn-cta" type="button" @click="agreeAndClose">확인했어요 · 동의</button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import client, { withMock } from '@/api/client';
import { mockTerms } from '@/api/mockData';
import PageHeader from '@/components/PageHeader.vue';

const termsList = [
    { id: 1, label: '[필수] 서비스 이용약관 동의', required: true },
    { id: 2, label: '[필수] 개인정보 수집 · 이용 동의', required: true },
    { id: 4, label: '[필수] 맞춤 추천을 위한 금융정보 활용', required: true },
];

const router = useRouter();
const name = ref('');
const email = ref('');
const password = ref('');
const passwordConfirm = ref('');
const loading = ref(false);
const errorMessage = ref('');
const agreedIds = ref([1, 2]);
const activeTermId = ref(null);

const activeTerm = computed(() => termsList.find((term) => term.id === activeTermId.value) ?? null);
const activeTermContent = computed(() => mockTerms[activeTermId.value]?.content ?? '');

const isAllAgreed = computed(() => termsList.every((term) => agreedIds.value.includes(term.id)));

const requiredAgreed = computed(() => {
    return termsList.filter((term) => term.required).every((term) => agreedIds.value.includes(term.id));
});

const canSubmit = computed(() => {
    return (
        name.value.trim() !== '' &&
        email.value.trim() !== '' &&
        password.value !== '' &&
        password.value === passwordConfirm.value &&
        requiredAgreed.value
    );
});

function isAgreed(termId) {
    return agreedIds.value.includes(termId);
}

function toggleTerm(termId) {
    if (isAgreed(termId)) {
        agreedIds.value = agreedIds.value.filter((id) => id !== termId);
    } else {
        agreedIds.value = [...agreedIds.value, termId];
    }
}

function toggleAll() {
    agreedIds.value = isAllAgreed.value ? [] : termsList.map((term) => term.id);
}

function openTermsSheet(termId) {
    activeTermId.value = termId;
}

function closeTermsSheet() {
    activeTermId.value = null;
}

function agreeAndClose() {
    if (activeTermId.value !== null && !isAgreed(activeTermId.value)) {
        agreedIds.value = [...agreedIds.value, activeTermId.value];
    }
    closeTermsSheet();
}

async function handleRegister() {
    if (!canSubmit.value) return;

    errorMessage.value = '';
    loading.value = true;

    try {
        const data = await withMock(
            () =>
                client.post('/auth/register', {
                    name: name.value,
                    email: email.value,
                    password: password.value,
                    agreements: termsList.map((term) => ({ termsId: term.id, agreed: isAgreed(term.id) })),
                }),
            { userId: 1, accessToken: 'mock-access-token' },
        );
        localStorage.setItem('accessToken', data.accessToken);
        router.push('/profile-setup');
    } catch {
        errorMessage.value = '회원가입에 실패했어요. 잠시 후 다시 시도해주세요.';
    } finally {
        loading.value = false;
    }
}
</script>

<style scoped>
.register {
    flex: 1;
    display: flex;
    flex-direction: column;
    background: var(--bg);
}

.scroll-area {
    flex: 1;
    overflow-y: auto;
    padding: 16px;
}

.headline {
    font-size: 18px;
    font-weight: 700;
    line-height: 1.4;
    color: var(--text-primary);
}

.register-form {
    display: flex;
    flex-direction: column;
    gap: 16px;
    margin-top: 24px;
}

.terms-box {
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding: 16px;
    background: var(--yellow-tint);
    border-radius: var(--radius-card);
}

.terms-all {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    font-weight: 700;
    color: var(--text-primary);
}

.terms-list {
    display: flex;
    flex-direction: column;
    gap: 10px;
}

.terms-row {
    display: flex;
    align-items: center;
    gap: 8px;
}

.checkbox-btn {
    display: flex;
}

.checkbox {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 20px;
    height: 20px;
    border-radius: 6px;
    border: 1.5px solid #d8d5cf;
    background: var(--white);
    flex-shrink: 0;
}

.checkbox svg {
    opacity: 0;
}

.checkbox.on {
    background: var(--kb-yellow);
    border-color: var(--kb-yellow);
}

.checkbox.on svg {
    opacity: 1;
}

.terms-label {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
    font-size: 12.5px;
    color: var(--kb-gray);
    text-align: left;
}

.error-message {
    font-size: 12px;
    color: var(--danger);
}

.flow-hint {
    margin-top: 16px;
    font-size: 11px;
    color: var(--kb-silver);
    text-align: center;
}

.sheet-overlay {
    position: fixed;
    inset: 0;
    display: flex;
    align-items: flex-end;
    justify-content: center;
    background: rgba(51, 48, 42, 0.5);
    z-index: 40;
}

.sheet {
    width: 100%;
    max-width: 375px;
    max-height: 80vh;
    display: flex;
    flex-direction: column;
    gap: 14px;
    padding: 10px 20px 24px;
    background: var(--white);
    border-radius: 20px 20px 0 0;
}

.sheet-handle {
    align-self: center;
    width: 36px;
    height: 4px;
    border-radius: 100px;
    background: var(--border);
}

.sheet-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
}

.sheet-title {
    font-size: 16px;
    font-weight: 700;
    color: var(--text-primary);
}

.sheet-content {
    flex: 1;
    overflow-y: auto;
    white-space: pre-line;
    font-size: 12.5px;
    line-height: 1.6;
    color: var(--kb-gray);
}
</style>
