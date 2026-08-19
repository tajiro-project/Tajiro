<script setup>
import { computed, provide, reactive, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import AppTabBar from '@/components/AppTabBar.vue';
import PageHeader from '@/components/PageHeader.vue';
import KakaoLocation from '@/components/KakaoLocation.vue';
import BirthDatePicker from '@/components/BirthDatePicker.vue';
import client, { getApiErrorMessage } from '@/api/client';
import { ChevronDown, MapPin } from 'lucide-vue-next';

const route = useRoute();

// 예전 가입자 중 지역/생년월일이 없는 사람은 로그인 상태로 앱을 켤 때마다
// 이 정보를 다 채울 때까지 화면 위에 모달을 띄운다. 새 가입자는 회원가입 때
// 이미 채워지므로 이 모달을 볼 일이 없다.
const showProfileGate = ref(false);
const profileConfirmedComplete = ref(false);
const gateRegion = ref('');
const gateSggCode = ref('');
const gateBirthDate = ref('');
const gateLocationPickerOpen = ref(false);
const gateSaving = ref(false);
const gateError = ref('');

// onMounted은 앱이 처음 로드될 때(보통 로그인 전) 한 번만 실행되고 로그인
// 후 라우트만 바뀌는 SPA 특성상 다시 실행되지 않으므로, 라우트 변화를 지켜보며
// 토큰이 생긴 시점(로그인 직후)에 다시 확인한다. 이미 완성된 걸 확인했으면
// 매 이동마다 다시 조회하지 않도록 플래그로 막는다.
watch(() => route.fullPath, checkProfileCompletion, { immediate: true });

async function checkProfileCompletion() {
  if (profileConfirmedComplete.value || showProfileGate.value) return;
  if (!localStorage.getItem('accessToken')) return;

  try {
    const res = await client.get('/users/me/profile');
    const profile = res?.data?.data;
    if (!profile?.targetRegion || !profile?.birthDate) {
      showProfileGate.value = true;
    } else {
      profileConfirmedComplete.value = true;
    }
  } catch {
    // 401 등으로 실패하면 로그인 화면 쪽에서 처리되므로 여기서는 무시
  }
}

function selectGateRegion(location) {
  gateRegion.value = location.name || location.address;
  gateSggCode.value = location.sggCode ?? '';
  gateLocationPickerOpen.value = false;
}

async function submitProfileGate() {
  if (!gateRegion.value.trim() || !gateBirthDate.value) return;

  gateError.value = '';
  gateSaving.value = true;

  try {
    await client.put('/users/me/profile', {
      targetRegion: gateRegion.value,
      target_sgg_code: gateSggCode.value,
      birthDate: gateBirthDate.value,
    });
    showProfileGate.value = false;
    profileConfirmedComplete.value = true;
  } catch (error) {
    gateError.value = getApiErrorMessage(
      error,
      '저장에 실패했어요. 잠시 후 다시 시도해주세요.',
    );
  } finally {
    gateSaving.value = false;
  }
}
const propertyListHeader = reactive({
  locationLabel: '매물 검색 결과',
  locationPickerRequestId: 0,
  setLocationLabel(label) {
    this.locationLabel = label || '매물 검색 결과';
  },
  requestLocationPicker() {
    this.locationPickerRequestId += 1;
  },
});

provide('propertyListHeader', propertyListHeader);

const headerTitle = computed(() => route.meta.headerTitle ?? '');
const showHeader = computed(() => Boolean(headerTitle.value));
const headerBack = computed(() => {
  const value = route.meta.headerBack;
  return typeof value === 'function' ? value(route) : (value ?? true);
});
const headerBackTo = computed(() => {
  const value = route.meta.headerBackTo;
  return typeof value === 'function' ? value(route) : (value ?? null);
});

const activeTab = computed(() => {
  if (route.path === '/favorites') return 'scrap';

  if (
    route.path.startsWith('/properties') ||
    route.path.startsWith('/preferences')
  ) {
    return 'property';
  }

  if (route.path.startsWith('/compare') || route.path.startsWith('/reports')) {
    return 'compare';
  }

  if (route.path === '/mypage') return 'my';

  return 'home';
});

const showTabBar = computed(() => {
  return (
    !['/', '/login', '/register'].includes(route.path) &&
    !route.path.startsWith('/seller')
  );
});

const KEEP_ALIVE_VIEWS = [
  'PropertyListView',
  'BenefitMatchView',
  'PropertyDetailView',
];
</script>

<template>
  <div class="app-frame">
    <PageHeader
      v-if="showHeader"
      :title="headerTitle"
      :back="headerBack"
      :back-to="headerBackTo"
    >
      <template
        v-if="route.name === 'property-list'"
        #title
      >
        <button
          class="header-location"
          @click="propertyListHeader.requestLocationPicker"
        >
          <MapPin
            :size="18"
            :stroke-width="2"
          />
          <span>{{ propertyListHeader.locationLabel }}</span>
          <ChevronDown
            :size="14"
            :stroke-width="2"
          />
        </button>
      </template>
      <template
        v-if="route.meta.headerAction === 'edit-profile'"
        #right
      >
        <button
          class="header-action"
          type="button"
          @click="showProfileGate = true"
        >
          내 정보 수정
        </button>
      </template>
    </PageHeader>

    <main class="route-content">
      <router-view v-slot="{ Component }">
        <KeepAlive :include="KEEP_ALIVE_VIEWS">
          <component :is="Component" />
        </KeepAlive>
      </router-view>
    </main>

    <AppTabBar
      v-if="showTabBar"
      :active="activeTab"
      class="global-tab-bar"
    />

    <div
      v-if="showProfileGate"
      class="profile-gate-overlay"
    >
      <div class="profile-gate-sheet">
        <p class="profile-gate-title">맞춤 추천을 위해<br />지역과 생년월일을 알려주세요</p>

        <div class="field">
          <label class="field-label">선호 지역</label>
          <input
            class="field-input"
            type="text"
            readonly
            :value="gateRegion"
            placeholder="예) 서울특별시 강남구"
            @click="gateLocationPickerOpen = true"
          />
        </div>
        <div class="field">
          <label class="field-label">생년월일</label>
          <BirthDatePicker v-model="gateBirthDate" />
        </div>

        <p
          v-if="gateError"
          class="profile-gate-error"
        >
          {{ gateError }}
        </p>

        <button
          class="btn-cta"
          type="button"
          :disabled="!gateRegion.trim() || !gateBirthDate || gateSaving"
          @click="submitProfileGate"
        >
          확인
        </button>
      </div>
    </div>

    <KakaoLocation
      :open="gateLocationPickerOpen"
      @close="gateLocationPickerOpen = false"
      @select="selectGateRegion"
    />
  </div>
</template>

<style scoped>
.profile-gate-overlay {
  position: fixed;
  inset: 0;
  z-index: 60;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  background: rgba(51, 48, 42, 0.5);
}

.profile-gate-sheet {
  width: 100%;
  max-width: 375px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 24px 20px 28px;
  background: var(--white);
  border-radius: 20px 20px 0 0;
}

.profile-gate-title {
  font-size: 16px;
  font-weight: 700;
  line-height: 1.4;
  color: var(--text-primary);
}

.profile-gate-error {
  font-size: 12px;
  color: var(--danger);
}

.header-location {
  display: flex;
  align-items: center;
  gap: 7px;
  min-width: 0;
  max-width: min(280px, calc(100vw - 76px));
  padding: 0;
  border: 0;
  background: transparent;
  color: var(--text-primary);
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
}

.header-location svg {
  flex-shrink: 0;
}

.header-location svg:first-child {
  color: #fe7b00;
}

.header-location svg:last-child {
  color: #545045;
}

.header-location span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.header-action {
  padding: 5px 10px;
  border-radius: 999px;
  background: var(--kb-yellow-header);
  color: var(--text-primary);
  font-size: 12px;
  font-weight: 700;
  text-decoration: none;
}
</style>
