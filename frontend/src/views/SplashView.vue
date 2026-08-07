<template>
  <simplebar class="splash">
    <div class="top-group">
      <div class="logo-badge">
        <img
          :src="logoImg"
          alt="타지로"
          class="logo-img"
        />
      </div>
      <div class="headline-group">
        <p class="headline">낯선 도시, 익숙한 시작</p>
        <p class="headline-sub">2030 지방 정착 부동산 도우미</p>
      </div>
    </div>

    <ul class="checklist">
      <li
        v-for="item in checklistItems"
        :key="item"
        class="checklist-item"
      >
        <Check
          :size="13"
          color="#545045"
          :stroke-width="2.5"
        />
        <span>{{ item }}</span>
      </li>
    </ul>

    <div class="bottom-group">
      <button
        class="start-btn"
        type="button"
        @click="router.push('/login')"
      >
        시작하기
      </button>
      <button
        class="seller-link"
        type="button"
        @click="router.push('/seller/properties/new')"
      >
        집주인이신가요? 내 매물 등록하기
      </button>
    </div>
  </simplebar>
</template>

<script setup>
import { useRouter } from 'vue-router';
import { Check } from 'lucide-vue-next';
import logoImg from '@/assets/img/logo.png';
import simplebar from 'simplebar-vue';

const checklistItems = [
  '매물 추천부터 AI 매물 비교까지',
  '주변 인프라와 안전 확보',
  '정책 매칭 및 KB 금융상품 연계',
];

const router = useRouter();
</script>

<style scoped>
.splash {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 75px 24px;
  background: #ffdd80;
  min-height: 100%;
}

.top-group {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 18px;
  margin-bottom: 32px;
}

.logo-badge {
  width: 128px;
  height: 128px;
}

.logo-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 30px;
  box-shadow: 0 10px 24px 0 rgba(89, 66, 0, 0.22);
}

.headline-group {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  text-align: center;
}

.headline {
  font-size: 21px;
  font-weight: 700;
  color: var(--text-primary);
}

.headline-sub {
  font-size: 13px;
  color: var(--kb-gray);
}

.checklist {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 9px;
  margin-bottom: 32px;
}

.checklist-item {
  display: flex;
  align-items: center;
  gap: 7px;
  padding: 9px 16px;
  background: rgba(255, 255, 255, 0.62);
  border-radius: 100px;
  font-size: 12.5px;
  font-weight: 500;
  color: var(--kb-dark-gray);
}

.bottom-group {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  margin-top: auto;
}

.start-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  padding: 13px 20px;
  background: var(--kb-gray);
  border-radius: 12px;
  color: var(--white);
  font-size: 15px;
  font-weight: 700;
}

.seller-link {
  margin-top: 16px;
  font-size: 13px;
  color: var(--kb-gray);
}
</style>
