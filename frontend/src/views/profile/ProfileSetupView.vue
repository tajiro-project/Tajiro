<template>
    <div class="profile-setup">
        <PageHeader title="내 정보 입력" :back="isEdit" />

        <div class="scroll-area">
            <form class="profile-form" @submit.prevent="handleSubmit">
                <div class="field">
                    <label class="field-label" for="targetRegion">지역 선택*</label>
                    <input id="targetRegion" v-model="targetRegion" class="field-input" type="text" placeholder="예) 서울특별시, 부산광역시" required />
                </div>
                <div class="field">
                    <label class="field-label" for="birthDate">생년월일*</label>
                    <input id="birthDate" v-model="birthDate" class="field-input" type="date" required />
                </div>
                <div class="field">
                    <label class="field-label" for="monthlyIncome">월 소득/용돈</label>
                    <input id="monthlyIncome" v-model="monthlyIncome" class="field-input" type="number" placeholder="예) 280 (만원)" />
                </div>
                <div class="field">
                    <label class="field-label" for="assetAmount">보유 자산</label>
                    <input id="assetAmount" v-model="assetAmount" class="field-input" type="number" placeholder="예) 3000 (만원)" />
                </div>
                <div class="field">
                    <label class="field-label" for="jobStatus">직업 / 재직·구직·학생 등 경제 상태</label>
                    <input id="jobStatus" v-model="jobStatus" class="field-input" type="text" placeholder="예) 회사원" />
                </div>

                <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>

                <button class="btn-cta" type="submit" :disabled="!canSubmit || loading">확인</button>
            </form>
        </div>
    </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import client, { getApiErrorMessage, withMock } from '@/api/client';
import { mockProfile } from '@/api/mockData';
import PageHeader from '@/components/PageHeader.vue';

const route = useRoute();
const router = useRouter();
const isEdit = computed(() => route.query.mode === 'edit');

const targetRegion = ref('');
const birthDate = ref('');
const monthlyIncome = ref('');
const assetAmount = ref('');
const jobStatus = ref('');
const loading = ref(false);
const errorMessage = ref('');

const canSubmit = computed(() => targetRegion.value.trim() !== '' && birthDate.value !== '');

onMounted(() => {
    if (isEdit.value) loadProfile();
});

async function loadProfile() {
    const data = await withMock(() => client.get('/users/me/profile'), mockProfile);
    const payload = data?.data ?? data;
    targetRegion.value = payload?.targetRegion ?? '';
    birthDate.value = payload?.birthDate ?? '';
    monthlyIncome.value = payload?.monthlyIncome ?? '';
    assetAmount.value = payload?.assetAmount ?? '';
    jobStatus.value = payload?.jobStatus ?? '';
}

async function handleSubmit() {
    if (!canSubmit.value) return;

    errorMessage.value = '';
    loading.value = true;

    try {
        await client.put('/users/me/profile', {
            targetRegion: targetRegion.value,
            birthDate: birthDate.value,
            monthlyIncome: monthlyIncome.value === '' ? null : Number(monthlyIncome.value),
            assetAmount: assetAmount.value === '' ? null : Number(assetAmount.value),
            jobStatus: jobStatus.value,
        });
    } catch (error) {
        if (error.response) {
            errorMessage.value = getApiErrorMessage(error);
            loading.value = false;
            return;
        }
        // 백엔드 자체가 없을 때만 mock으로 간주하고 진행
    }

    router.push(isEdit.value ? '/mypage' : '/preferences/1');
}
</script>

<style scoped>
.profile-setup {
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

.profile-form {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.error-message {
    font-size: 12px;
    color: var(--danger);
}
</style>
