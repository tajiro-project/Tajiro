<template>
  <Teleport to="body">
    <Transition name="sheet">
      <div v-if="modelValue" class="sheet-overlay" @click.self="close">
        <div class="sheet" :class="{ dragging }" :style="sheetStyle">
          <div
            class="sheet-grab"
            @pointerdown="onDown"
            @pointermove="onMove"
            @pointerup="onUp"
            @pointercancel="onUp"
          >
            <div class="sheet-handle" />
            <div v-if="title" class="sheet-head">
              <p class="sheet-title">{{ title }}</p>
              <button class="sheet-close" aria-label="닫기" @click="close">
                <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
                  <path
                    d="M4 4L14 14M14 4L4 14"
                    stroke="currentColor"
                    stroke-width="1.6"
                    stroke-linecap="round"
                  />
                </svg>
              </button>
            </div>
          </div>
          <div class="sheet-body">
            <slot />
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { computed, ref, watch, onUnmounted } from 'vue';

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  title: { type: String, default: '' },
});
const emit = defineEmits(['update:modelValue']);

const dragY = ref(0);
const dragging = ref(false);
let startY = 0;
let startAt = 0;

const sheetStyle = computed(() => ({
  transform: dragY.value ? `translateY(${dragY.value}px)` : '',
}));

function onDown(e) {
  if (e.target.closest('.sheet-close')) return;
  dragging.value = true;
  startY = e.clientY;
  startAt = Date.now();
  e.currentTarget.setPointerCapture(e.pointerId);
}

function onMove(e) {
  if (!dragging.value) return;
  dragY.value = Math.max(0, e.clientY - startY);
}

function onUp() {
  if (!dragging.value) return;
  const flick = Date.now() - startAt < 250 && dragY.value > 30;
  dragging.value = false;
  if (dragY.value > 90 || flick) close();
  dragY.value = 0;
}

function close() {
  emit('update:modelValue', false);
}

// 뒤로가기로 바텀시트 닫기
let pushedState = false;

function onPopState() {
  pushedState = false;
  window.removeEventListener('popstate', onPopState);
  emit('update:modelValue', false);
}

watch(
  () => props.modelValue,
  (v) => {
    if (v) {
      history.pushState({ sheetOpen: true }, '');
      pushedState = true;
      window.addEventListener('popstate', onPopState);
    } else {
      dragY.value = 0;
      if (pushedState) {
        pushedState = false;
        window.removeEventListener('popstate', onPopState);
        history.back();
      }
    }
  },
);

onUnmounted(() => {
  if (pushedState) {
    pushedState = false;
    window.removeEventListener('popstate', onPopState);
  }
});
</script>

<style scoped>
.sheet-overlay {
  position: fixed;
  inset: 0;
  z-index: 100;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  background: rgba(33, 30, 24, 0.45);
}
.sheet {
  width: 100%;
  max-width: 400px;
  background: var(--white);
  border-radius: 20px 20px 0 0;
  padding: 8px 0 0;
  max-height: 82dvh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: transform 0.22s ease;
}
.sheet.dragging {
  transition: none;
}
.sheet-grab {
  touch-action: none;
  cursor: grab;
  user-select: none;
  flex-shrink: 0;
  padding: 0 16px;
}
.sheet-grab:active {
  cursor: grabbing;
}
.sheet-handle {
  width: 40px;
  height: 4px;
  border-radius: 2px;
  background: var(--border);
  margin: 4px auto 10px;
}
.sheet-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 14px;
  border-bottom: 1px solid #e9e7e2;
}
.sheet-title {
  font-size: 15px;
  font-weight: 700;
}
.sheet-close {
  color: var(--kb-silver);
  display: flex;
}
.sheet-body {
  overflow-y: auto;
  flex: 1;
  padding: 0 16px 20px;
  -webkit-overflow-scrolling: touch;
}
.sheet-enter-active,
.sheet-leave-active {
  transition: opacity 0.22s ease;
}
.sheet-enter-from,
.sheet-leave-to {
  opacity: 0;
}
.sheet-enter-from .sheet,
.sheet-leave-to .sheet {
  transform: translateY(100%);
}
</style>
