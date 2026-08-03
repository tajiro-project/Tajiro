<template>
    <div class="login">
        <div class="login-body">
            <div class="logo-badge">
                <svg width="40" height="40" viewBox="0 0 40 40" fill="none">
                    <path d="M8 21l12-9 12 9v13a2 2 0 01-2 2h-6v-9h-8v9h-6a2 2 0 01-2-2V21z" stroke="#545045" stroke-width="2.2" stroke-linejoin="round" />
                </svg>
                <span class="logo-text">타지로</span>
            </div>

            <h1 class="headline">다시 만나서 반가워요!</h1>
            <p class="headline-sub">타지로와 함께 정착을 이어가요</p>

            <form class="login-form" @submit.prevent="handleLogin">
                <div class="field">
                    <label class="field-label" for="email">이메일</label>
                    <input id="email" v-model="email" class="field-input" type="email" placeholder="example@email.com" required />
                </div>
                <div class="field">
                    <label class="field-label" for="password">비밀번호</label>
                    <input id="password" v-model="password" class="field-input" type="password" placeholder="********" required />
                </div>

                <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>

                <button class="btn-cta login-btn" type="submit" :disabled="loading">로그인</button>
            </form>

            <p class="signup-link">
                아직 회원이 아니신가요?
                <button type="button" @click="router.push('/register')">회원가입</button>
            </p>
        </div>
    </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import client, { withMock } from '@/api/client';

const MOCK_EMAIL = 'test@tajiro.com';
const MOCK_PASSWORD = 'test1234';

const router = useRouter();
const email = ref('');
const password = ref('');
const loading = ref(false);
const errorMessage = ref('');

async function handleLogin() {
    errorMessage.value = '';
    loading.value = true;

    try {
        const data = await withMock(
            () => client.post('/auth/login', { email: email.value, password: password.value }),
            mockLogin,
        );
        localStorage.setItem('accessToken', data.accessToken);
        router.push('/home');
    } catch {
        errorMessage.value = '이메일 또는 비밀번호가 일치하지 않습니다.';
    } finally {
        loading.value = false;
    }
}

function mockLogin() {
    if (email.value === MOCK_EMAIL && password.value === MOCK_PASSWORD) {
        return { accessToken: 'mock-access-token', user: { id: 1, name: '김민주', email: email.value } };
    }

    throw new Error('MOCK_LOGIN_FAILED');
}
</script>

<style scoped>
.login {
    flex: 1;
    display: flex;
    flex-direction: column;
    background: var(--bg);
}

.login-body {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 70px 24px 24px;
}

.logo-badge {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 4px;
    width: 88px;
    height: 88px;
    background: var(--kb-yellow);
    border-radius: 20px;
}

.logo-text {
    font-size: 12px;
    font-weight: 900;
    color: var(--sub-kb-dark-gray, #545045);
}

.headline {
    margin-top: 20px;
    font-size: 18px;
    font-weight: 700;
    color: var(--text-primary);
    text-align: center;
}

.headline-sub {
    margin-top: 4px;
    font-size: 12px;
    color: var(--kb-gray);
    text-align: center;
}

.login-form {
    width: 100%;
    display: flex;
    flex-direction: column;
    gap: 16px;
    margin-top: 32px;
}

.error-message {
    margin-top: -8px;
    font-size: 12px;
    color: var(--danger);
}

.login-btn {
    margin-top: 4px;
}

.signup-link {
    margin-top: 20px;
    font-size: 12.5px;
    color: var(--kb-gray);
    text-align: center;
}

.signup-link button {
    font-weight: 700;
    color: var(--text-primary);
    text-decoration: underline;
}
</style>
