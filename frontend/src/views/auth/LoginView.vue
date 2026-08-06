<template>
    <div class="login">
        <div class="login-body">
            <div class="logo-badge">
                <img :src="logoImg" alt="타지로" class="logo-img" />
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
import logoImg from '@/assets/img/logo.png';

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
        const payload = data.data ?? data;
        localStorage.setItem('accessToken', payload.accessToken);
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
    width: 88px;
    height: 88px;
}

.logo-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    border-radius: 22px;
    box-shadow: 0 8px 20px 0 rgba(89, 66, 0, 0.2);
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
