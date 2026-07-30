<template>
  <header class="page-header" :class="{ yellow }">
    <button v-if="back" class="back-btn" aria-label="뒤로" @click="onBack">
      <svg width="22" height="22" viewBox="0 0 22 22" fill="none">
        <path d="M13.5 4.5L7 11L13.5 17.5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" />
      </svg>
    </button>
    <p class="title">{{ title }}</p>
    <div class="right">
      <slot name="right" />
    </div>
  </header>
</template>

<script setup>
import { useRouter } from 'vue-router'

const props = defineProps({
  title: { type: String, default: '' },
  back: { type: Boolean, default: true },
  yellow: { type: Boolean, default: false },
  backTo: { type: String, default: null },
})

const router = useRouter()
function onBack() {
  if (props.backTo) router.push(props.backTo)
  else router.back()
}
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 46px;
  padding: 12px;
  background: var(--white);
  border-bottom: 1px solid var(--border);
  position: sticky;
  top: 0;
  z-index: 20;
}
.page-header.yellow {
  background: var(--kb-yellow-header);
  border-bottom: none;
}
.back-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  color: var(--text-primary);
}
.title {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
}
.right {
  margin-left: auto;
  display: flex;
  align-items: center;
}
</style>
