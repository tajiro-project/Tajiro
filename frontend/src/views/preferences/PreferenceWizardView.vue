<template>
  <div class="preference-wizard">
    <PageHeader title="가치관 설정" />

    <main class="wizard-content">
      <!-- 선호 조건 입력 단계 -->
    </main>

    <AppTabBar />
  </div>
</template>

<script setup>
import AppTabBar from '@/components/AppTabBar.vue';
import PageHeader from '@/components/PageHeader.vue';
</script>

<style scoped>
.preference-wizard {
  display: flex;
  flex: 1;
  min-height: 0;
  flex-direction: column;
  background: var(--bg);
}

.wizard-content {
  flex: 1;
  overflow-y: auto;
}
</style>
