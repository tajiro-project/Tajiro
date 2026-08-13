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
            placeholder="예) 서울특별시 강남구 테헤란로 152"
          />
          <button type="submit">검색</button>
        </form>

        <div class="body">
          <!-- 검색 결과 -->
          <ul v-if="results.length" class="results">
            <li v-for="(r, i) in results" :key="i">
              <button class="result" @click="select(r)">
                <p class="r-road">{{ r.roadAddress }}</p>
                <p class="r-jibun">지번 &nbsp;{{ r.jibunAddress }}</p>
              </button>
            </li>
          </ul>

          <!-- tip -->
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

const keyword = ref('');
const results = ref([]);

const tips = [
  { label: '도로명 + 건물번호', example: '백룡로57번길 126' },
  { label: '지역명(동/리) + 번지', example: '자양동 51-5' },
  { label: '지역명(동/리) + 건물명(아파트명)', example: '자양동 우송에이스빌' },
  { label: '사서함명 + 번호', example: '대전동구우체국사서함 1~100' },
];

const MOCK_ADDRESSES = [
  {
    roadAddress: '대전광역시 동구 백룡로57번길 126',
    jibunAddress: '대전광역시 동구 자양동 1-1',
    buildingName: '우송에이스빌',
    dongName: '자양동',
    isApartment: false,
    dongs: [],
    lat: 36.3387162,
    lng: 127.4507735,
  },
  {
    roadAddress: '대전광역시 동구 백룡로5번길 59',
    jibunAddress: '대전광역시 동구 자양동 51-5',
    buildingName: '',
    dongName: '자양동',
    isApartment: false,
    dongs: [],
    lat: 36.338653,
    lng: 127.4491249,
  },
  {
    roadAddress: '대전광역시 동구 자양동 89-16',
    jibunAddress: '대전광역시 동구 자양동 89-16',
    buildingName: '',
    dongName: '자양동',
    isApartment: false,
    dongs: [],
    lat: 36.3377551,
    lng: 127.4533973,
  },
  {
    roadAddress: '대전광역시 동구 자양동 200-13',
    jibunAddress: '대전광역시 동구 자양동 200-13',
    buildingName: '자양그린아파트',
    dongName: '자양동',
    isApartment: true,
    dongs: ['101동', '102동', '103동', '104동', '105동', '106동'],
    lat: 36.3411405,
    lng: 127.4507628,
  },
  {
    roadAddress: '대전광역시 동구 자양동 200-13',
    jibunAddress: '대전광역시 동구 자양동 200-13',
    buildingName: '자양그린아파트',
    dongName: '자양동',
    isApartment: true,
    dongs: ['101동', '102동', '103동', '104동', '105동', '106동'],
    lat: 36.3411405,
    lng: 127.4507628,
  },
  {
    roadAddress: '대전광역시 동구 자양동 200-13',
    jibunAddress: '대전광역시 동구 자양동 200-13',
    buildingName: '자양그린아파트',
    dongName: '자양동',
    isApartment: true,
    dongs: ['101동', '102동', '103동', '104동', '105동', '106동'],
    lat: 36.3411405,
    lng: 127.4507628,
  },
  {
    roadAddress: '대전광역시 동구 자양동 200-13',
    jibunAddress: '대전광역시 동구 자양동 200-13',
    buildingName: '자양그린아파트',
    dongName: '자양동',
    isApartment: true,
    dongs: ['101동', '102동', '103동', '104동', '105동', '106동'],
    lat: 36.3411405,
    lng: 127.4507628,
  },
  {
    roadAddress: '대전광역시 동구 자양동 200-13',
    jibunAddress: '대전광역시 동구 자양동 200-13',
    buildingName: '자양그린아파트',
    dongName: '자양동',
    isApartment: true,
    dongs: ['101동', '102동', '103동', '104동', '105동', '106동'],
    lat: 36.3411405,
    lng: 127.4507628,
  },
  {
    roadAddress: '대전광역시 동구 자양동 200-13',
    jibunAddress: '대전광역시 동구 자양동 200-13',
    buildingName: '자양그린아파트',
    dongName: '자양동',
    isApartment: true,
    dongs: ['101동', '102동', '103동', '104동', '105동', '106동'],
    lat: 36.3411405,
    lng: 127.4507628,
  },
  {
    roadAddress: '대전광역시 동구 자양동 200-13',
    jibunAddress: '대전광역시 동구 자양동 200-13',
    buildingName: '자양그린아파트',
    dongName: '자양동',
    isApartment: true,
    dongs: ['101동', '102동', '103동', '104동', '105동', '106동'],
    lat: 36.3411405,
    lng: 127.4507628,
  },
  {
    roadAddress: '대전광역시 동구 자양동 200-13',
    jibunAddress: '대전광역시 동구 자양동 200-13',
    buildingName: '자양그린아파트',
    dongName: '자양동',
    isApartment: true,
    dongs: ['101동', '102동', '103동', '104동', '105동', '106동'],
    lat: 36.3411405,
    lng: 127.4507628,
  },
  {
    roadAddress: '대전광역시 동구 자양동 200-13',
    jibunAddress: '대전광역시 동구 자양동 200-13',
    buildingName: '자양그린아파트',
    dongName: '자양동',
    isApartment: true,
    dongs: ['101동', '102동', '103동', '104동', '105동', '106동'],
    lat: 36.3411405,
    lng: 127.4507628,
  },
  {
    roadAddress: '대전광역시 동구 자양동 200-13',
    jibunAddress: '대전광역시 동구 자양동 200-13',
    buildingName: '자양그린아파트',
    dongName: '자양동',
    isApartment: true,
    dongs: ['101동', '102동', '103동', '104동', '105동', '106동'],
    lat: 36.3411405,
    lng: 127.4507628,
  },
];

function onSearch() {
  const k = keyword.value.trim();
  results.value = k
    ? MOCK_ADDRESSES.filter(
        (a) =>
          a.roadAddress.includes(k) ||
          a.jibunAddress.includes(k) ||
          a.buildingName.includes(k),
      )
    : [];
  if (k && !results.value.length) results.value = MOCK_ADDRESSES;
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
  background: var(--kb-yellow);
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
.r-road {
  font-size: 13.5px;
  font-weight: 700;
}
.r-jibun {
  margin-top: 4px;
  font-size: 12px;
  color: var(--kb-silver);
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
.as-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 14px;
  border-top: 1px solid var(--border);
  font-size: 12px;
  color: #8b8b8b;
}
.as-footer b {
  color: #333;
}
.as-divider {
  color: var(--border);
}
</style>
