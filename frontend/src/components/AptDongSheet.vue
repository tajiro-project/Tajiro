<template>
  <Teleport to="body">
    <div
      v-show="open"
      class="sheet-overlay"
      @mousedown.self="emit('close')"
    >
    <div class="dong-select">
      <header class="ds-header">
        <p class="ds-title">동 선택</p>
        <button class="ds-close" aria-label="닫기" @click="emit('close')">
          <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
            <path
              d="M5 5L15 15M15 5L5 15"
              stroke="#33302a"
              stroke-width="1.6"
              stroke-linecap="round"
            />
          </svg>
        </button>
      </header>

      <div class="body">
        <!-- 선택한 주소 -->
        <div class="addr-card">
          <span class="a-icon">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path
                d="M2 7.5L8 2.5L14 7.5"
                stroke="#545045"
                stroke-width="1.5"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
              <path
                d="M3.8 6.8V13.5h8.4V6.8"
                stroke="#545045"
                stroke-width="1.5"
                stroke-linejoin="round"
              />
            </svg>
          </span>
          <div class="a-texts">
            <p class="a-name">선택한 주소 · {{ address?.buildingName || '—' }}</p>
            <p class="a-addr">{{ address?.roadAddress ?? '' }}</p>
          </div>
          <button class="a-change" @click="emit('change-address')">변경</button>
        </div>

        <p class="guide">이 단지에서 매물이 위치한 동을 선택하세요</p>

        <ul v-if="dongs.length" class="dongs">
          <li v-for="d in dongs" :key="d">
            <button class="dong" :class="{ on: selected === d }" @click="selected = d">
              <span class="grip">
                <svg width="10" height="14" viewBox="0 0 10 14" fill="none">
                  <circle cx="3" cy="3" r="1" fill="#c9c5bc" />
                  <circle cx="7" cy="3" r="1" fill="#c9c5bc" />
                  <circle cx="3" cy="7" r="1" fill="#c9c5bc" />
                  <circle cx="7" cy="7" r="1" fill="#c9c5bc" />
                  <circle cx="3" cy="11" r="1" fill="#c9c5bc" />
                  <circle cx="7" cy="11" r="1" fill="#c9c5bc" />
                </svg>
              </span>
              <span class="dong-name">{{ d }}</span>
              <span v-if="selected === d" class="dong-check">
                <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
                  <path
                    d="M2 6.5L4.7 9L10 3.5"
                    stroke="#fff"
                    stroke-width="1.8"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                </svg>
              </span>
              <svg v-else class="dong-chev" width="14" height="14" viewBox="0 0 14 14" fill="none">
                <path
                  d="M5 3l4 4-4 4"
                  stroke="#c9c5bc"
                  stroke-width="1.5"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
            </button>
          </li>
        </ul>

        <p v-else class="empty">
          동 정보를 불러오지 못했어요. 주소를 다시 선택해주세요.
        </p>
      </div>

      <div class="bottom-bar">
        <button class="btn-cta" :disabled="!selected" @click="confirm">등록하기</button>
      </div>
    </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue';

const props = defineProps({
  open: { type: Boolean, default: false },
  address: { type: Object, default: null },
  dongs: { type: Array, default: () => [] },
});

const emit = defineEmits(['close', 'select', 'change-address']);

const selected = ref(null);

let previousBodyOverflow = '';

watch(
  () => props.open,
  (isOpen) => {
    if (isOpen) {
      selected.value = props.dongs[0] ?? null;
      previousBodyOverflow = document.body.style.overflow;
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = previousBodyOverflow;
    }
  },
);

function handleKeydown(e) {
  if (props.open && e.key === 'Escape') emit('close');
}

onMounted(() => document.addEventListener('keydown', handleKeydown));
onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown);
  document.body.style.overflow = previousBodyOverflow;
});

function confirm() {
  emit('select', selected.value);
}
</script>

<style scoped>
.sheet-overlay {
  position: fixed;
  inset: 0;
  z-index: 210;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: rgba(33, 30, 24, 0.55);
}
.dong-select {
  width: min(100%, 480px);
  max-height: calc(100dvh - 40px);
  display: flex;
  flex-direction: column;
  background: var(--bg);
  border-radius: 20px;
  box-shadow: 0 16px 40px rgba(33, 30, 24, 0.22);
  overflow: hidden;
}

@media (max-width: 600px) {
  .sheet-overlay {
    align-items: flex-end;
    padding: 0;
  }
  .dong-select {
    width: 100%;
    max-height: 92dvh;
    border-radius: 22px 22px 0 0;
  }
}
.ds-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  background: var(--white);
}
.ds-title {
  font-size: 16px;
  font-weight: 800;
}
.ds-close {
  display: flex;
  border: none;
  background: none;
  cursor: pointer;
}
.body {
  flex: 1;
  overflow-y: auto;
  padding: 8px 16px 16px;
}
.addr-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 13px 14px;
  background: var(--yellow-tint);
  border-radius: 12px;
}
.a-icon {
  display: flex;
  flex-shrink: 0;
}
.a-texts {
  flex: 1;
  min-width: 0;
}
.a-name {
  font-size: 13px;
  font-weight: 800;
}
.a-addr {
  margin-top: 3px;
  font-size: 11.5px;
  color: var(--kb-gray);
}
.a-change {
  padding: 6px 12px;
  border-radius: 8px;
  background: var(--white);
  border: 1px solid rgba(255, 188, 0, 0.5);
  font-size: 11.5px;
  font-weight: 700;
  flex-shrink: 0;
  cursor: pointer;
}
.guide {
  margin: 16px 0 10px;
  font-size: 13.5px;
  font-weight: 800;
}
.dongs {
  display: flex;
  flex-direction: column;
  gap: 10px;
  list-style: none;
  margin: 0;
  padding: 0;
}
.dong {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 15px 14px;
  background: var(--white);
  border: 1px solid var(--border);
  border-radius: 12px;
  text-align: left;
  cursor: pointer;
}
.dong.on {
  background: var(--yellow-tint);
  border: 1.5px solid var(--kb-yellow);
}
.grip {
  display: flex;
  flex-shrink: 0;
}
.dong-name {
  flex: 1;
  font-size: 14.5px;
  font-weight: 800;
}
.dong-check {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: var(--kb-yellow);
  flex-shrink: 0;
}
.dong-chev {
  flex-shrink: 0;
}
.empty {
  margin-top: 40px;
  text-align: center;
  font-size: 13px;
  color: var(--kb-silver);
}
.bottom-bar {
  padding: 12px 16px 16px;
  background: var(--bg);
}
</style>
