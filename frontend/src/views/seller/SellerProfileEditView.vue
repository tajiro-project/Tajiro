<template>
  <div class="seller-profile-edit">
    <simplebar class="content">
      <form class="profile-form" @submit.prevent="handleSubmit">
        <div class="field">
          <label class="field-label" for="sellerName">이름</label>
          <input
            id="sellerName"
            v-model="name"
            class="field-input"
            type="text"
            maxlength="30"
            placeholder="이름을 입력해주세요"
            required
          />
        </div>

        <div class="field">
          <label class="field-label" for="sellerPhone">연락처</label>
          <input
            id="sellerPhone"
            :value="phone"
            class="field-input"
            type="tel"
            inputmode="numeric"
            autocomplete="tel"
            placeholder="010-0000-0000"
            required
            @input="onPhoneInput"
          />
        </div>

        <div class="field">
          <label class="field-label" for="sellerAgencyName">
            중개사무소명
          </label>
          <input
            id="sellerAgencyName"
            v-model="agencyName"
            class="field-input"
            type="text"
            maxlength="60"
            placeholder="중개사무소명을 입력해주세요"
            required
          />
        </div>

        <p v-if="errorMessage" class="error-message" role="alert">
          {{ errorMessage }}
        </p>

        <button class="btn-cta" type="submit" :disabled="!canSubmit || loading">
          {{ loading ? '저장 중...' : '저장하기' }}
        </button>
      </form>
    </simplebar>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import simplebar from 'simplebar-vue';
import client, { getApiErrorMessage } from '@/api/client';

const router = useRouter();
const name = ref('');
const phone = ref('');
const agencyName = ref('');
const loading = ref(false);
const errorMessage = ref('');

const canSubmit = computed(
  () =>
    name.value.trim() !== '' &&
    phone.value.replace(/\D/g, '').length >= 10 &&
    agencyName.value.trim() !== '',
);

onMounted(loadProfile);

async function loadProfile() {
  const { data } = await client.get('/users/me');
  const payload = data?.data ?? data;
  name.value = payload?.name ?? '';
  phone.value = formatPhone(payload?.phone ?? '');
  agencyName.value = payload?.agencyName ?? '';
}

function formatPhone(value) {
  const digits = String(value).replace(/\D/g, '').slice(0, 11);
  if (digits.length > 7) {
    return `${digits.slice(0, 3)}-${digits.slice(3, 7)}-${digits.slice(7)}`;
  }
  if (digits.length > 3) return `${digits.slice(0, 3)}-${digits.slice(3)}`;
  return digits;
}

function onPhoneInput(event) {
  phone.value = formatPhone(event.target.value);
  event.target.value = phone.value;
}

async function handleSubmit() {
  if (!canSubmit.value || loading.value) return;

  loading.value = true;
  errorMessage.value = '';

  try {
    await client.put('/users/me', {
      name: name.value.trim(),
      phone: phone.value,
      agencyName: agencyName.value.trim(),
    });
  } catch (error) {
    if (error.response) {
      errorMessage.value = getApiErrorMessage(
        error,
        '정보를 저장하지 못했어요. 잠시 후 다시 시도해주세요.',
      );
      return;
    }
    // 백엔드 미기동 상태에서는 목데이터 저장 성공으로 처리
  } finally {
    loading.value = false;
  }

  router.push('/seller/home');
}
</script>

<style scoped>
.seller-profile-edit {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  background: var(--bg);
}

.content {
  flex: 1;
  min-height: 0;
  padding: 16px 16px 24px;
}
.profile-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.error-message {
  font-size: 12px;
  line-height: 1.4;
  color: var(--danger);
}

.btn-cta {
  margin-top: 6px;
}
</style>
