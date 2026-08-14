<template>
  <Teleport to="body">
    <div v-show="open" class="sheet-overlay" @mousedown.self="emit('close')">
      <div class="addr-search">
        <header class="as-header">
          <p class="as-title">도로명 주소</p>
          <button class="as-close" aria-label="닫기" @click="emit('close')">
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

        <form class="search-row" @submit.prevent="onSearch">
          <input
            v-model="keyword"
            type="search"
            placeholder="예) 서울 강남구 테헤란로 152"
          />
          <button type="submit">검색</button>
        </form>

        <div class="body">
          <p v-if="isLoading" class="state">검색 중이에요</p>

          <ul v-else-if="results.length" class="results">
            <li v-for="(r, i) in results" :key="i">
              <button class="result" @click="select(r)">
                <p v-if="r.buildingName" class="r-name">{{ r.buildingName }}</p>
                <p class="r-line">
                  <span class="r-tag">도로명</span>
                  <span class="r-text">
                    {{ r.roadAddress || r.jibunAddress }}
                  </span>
                </p>
                <p class="r-line">
                  <span class="r-tag">지번</span>
                  <span class="r-text">{{ r.jibunAddress }}</span>
                </p>
              </button>
            </li>
          </ul>

          <p v-else-if="searched" class="state">검색 결과가 없어요</p>

          <div v-else class="tip">
            <p class="tip-head">tip</p>
            <p class="tip-desc">
              아래와 같은 조합으로 검색을 하시면 더욱 정확한 결과가 검색됩니다.
            </p>
            <div v-for="t in tips" :key="t.label" class="tip-group">
              <p class="tip-label">{{ t.label }}</p>
              <p class="tip-example">예) {{ t.example }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue';
import { locationApi } from '@/api/services';

const keyword = ref('');
const results = ref([]);
const isLoading = ref(false);
const searched = ref(false);

const props = defineProps({
  open: { type: Boolean, default: false },
});

const emit = defineEmits(['close', 'select']);

let previousBodyOverflow = '';

watch(
  () => props.open,
  (isOpen) => {
    if (isOpen) {
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

const tips = [
  { label: '도로명 + 건물번호', example: '백룡로57번길 126' },
  { label: '지역명(동/리) + 번지', example: '자양동 51-5' },
  { label: '지역명(동/리) + 건물명(아파트명)', example: '자양동 우송에이스빌' },
];

async function onSearch() {
  const k = keyword.value.trim();
  if (!k) return;

  isLoading.value = true;
  searched.value = true;
  try {
    const res = await locationApi.searchAddress(k);
    results.value = res.data ?? [];
  } catch {
    results.value = [];
  } finally {
    isLoading.value = false;
  }
}

function select(addr) {
  emit('select', { ...addr });
}
</script>

<style scoped>
.sheet-overlay {
  position: fixed;
  inset: 0;
  z-index: 200;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: rgba(33, 30, 24, 0.55);
}
.addr-search {
  width: min(100%, 480px);
  height: min(560px, calc(100dvh - 40px));
  display: flex;
  flex-direction: column;
  background: var(--white);
  border-radius: 20px;
  box-shadow: 0 16px 40px rgba(33, 30, 24, 0.22);
  overflow: hidden;
}

@media (max-width: 600px) {
  .sheet-overlay {
    align-items: flex-end;
    padding: 0;
  }
  .addr-search {
    width: 100%;
    height: 60dvh;
    border-radius: 22px 22px 0 0;
  }
}
.as-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
}
.as-title {
  font-size: 16px;
  font-weight: 800;
}
.as-close {
  display: flex;
  border: none;
  background: none;
  cursor: pointer;
}
.search-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 16px;
  border-bottom: 1px solid var(--border);
}
.search-row input {
  flex: 1;
  height: 46px;
  padding: 0 14px;
  border: 1px solid var(--border);
  border-radius: 12px;
  font-size: 13.5px;
  color: var(--text-primary);
  background: var(--white);
}
.search-row input::placeholder {
  color: #b6b3ad;
}
.search-row input:focus {
  outline: none;
  border-color: var(--kb-yellow);
}
.search-row button {
  flex: 0 0 60px;
  height: 46px;
  border-radius: 12px;
  background: #ffdd80;
  color: var(--text-primary);
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
}
.body {
  flex: 1;
  overflow-y: auto;
}
.results {
  display: flex;
  flex-direction: column;
  list-style: none;
  margin: 0;
  padding: 0;
}
.result {
  display: block;
  width: 100%;
  padding: 14px 16px;
  text-align: left;
  border: none;
  border-bottom: 1px solid var(--border);
  background: none;
  cursor: pointer;
  transition: background 0.15s;
}
.result:hover {
  background: var(--yellow-tint);
}

.r-jibun {
  margin-top: 4px;
}
.tip {
  padding: 24px 20px;
}
.tip-head {
  font-size: 18px;
  font-weight: 800;
  color: #444;
}
.tip-desc {
  margin-top: 8px;
  font-size: 13px;
  line-height: 1.6;
  color: #666;
}
.tip-group {
  margin-top: 16px;
}
.tip-label {
  font-size: 13px;
  font-weight: 600;
  color: #444;
}
.tip-example {
  margin-top: 4px;
  font-size: 13px;
  color: #3d6bd6;
}

.state {
  padding: 40px 20px;
  text-align: center;
  font-size: 13px;
  color: var(--kb-silver);
}

.r-name {
  margin-bottom: 6px;
  font-size: 14px;
  font-weight: 800;
  color: var(--text-primary);
}
.r-line {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-top: 5px;
}
.r-tag {
  flex-shrink: 0;
  min-width: 42px;
  padding: 2px 5px;
  border-radius: 4px;
  background: var(--bg);
  color: var(--kb-gray);
  font-size: 11px;
  font-weight: 600;
  text-align: center;
  line-height: 1.5;
}
.r-text {
  flex: 1;
  min-width: 0;
  font-size: 13.5px;
  font-weight: 500;
  color: var(--text-primary);
  line-height: 1.5;
  word-break: keep-all;
}

.addr-label {
  flex-shrink: 0;
  width: 3em;
  font-size: 13px;
  color: var(--kb-gray);
  text-align: center;
  text-align-last: right;
}
</style>
