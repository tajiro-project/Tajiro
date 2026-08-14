<template>
  <Teleport to="body">
    <div
      v-show="open"
      class="location-picker-overlay"
      role="presentation"
      @mousedown.self="close"
    >
      <section
        class="location-picker"
        role="dialog"
        aria-modal="true"
        aria-labelledby="location-picker-title"
      >
        <header class="picker-header">
          <div>
            <h2 id="location-picker-title">지도에서 위치 선택</h2>
            <p>장소를 검색하거나 지도를 눌러 위치를 지정하세요.</p>
          </div>
          <button
            type="button"
            class="close-button"
            aria-label="닫기"
            @click="close"
          >
            ×
          </button>
        </header>

        <form class="map-search" @submit.prevent="searchLocation">
          <input
            ref="searchInput"
            v-model.trim="query"
            type="search"
            placeholder="장소 또는 주소 검색 (예: 강남역)"
            aria-label="장소 또는 주소"
          />
          <button type="submit" :disabled="loading || !query">검색</button>
        </form>

        <div class="map-wrap">
          <div ref="mapElement" class="map" aria-label="위치 선택 지도" />
          <div v-if="loading" class="map-state">지도를 불러오는 중이에요.</div>
          <div v-else-if="errorMessage" class="map-state map-error">
            {{ errorMessage }}
          </div>
        </div>

        <div class="selected-address" aria-live="polite">
          <span class="pin" aria-hidden="true">●</span>
          <div>
            <strong>선택한 위치</strong>
            <p>
              {{ selectedAddress || '지도를 클릭해 위치를 선택해 주세요.' }}
            </p>
          </div>
        </div>

        <footer class="picker-footer">
          <button type="button" class="cancel-button" @click="close">
            취소
          </button>
          <button
            type="button"
            class="confirm-button"
            :disabled="!hasSelection || loading"
            @click="confirm"
          >
            이 위치 선택
          </button>
        </footer>
      </section>
    </div>
  </Teleport>
</template>

<script setup>
import {
  computed,
  nextTick,
  onDeactivated,
  onMounted,
  onUnmounted,
  reactive,
  ref,
  watch,
} from 'vue';
import { loadKakaoSdk } from '@/utils/kakaoSdk';

const DEFAULT_LOCATION = {
  lat: 37.5665,
  lng: 126.978,
};

const props = defineProps({
  open: {
    type: Boolean,
    default: false,
  },
  initialLocation: {
    type: Object,
    default: null,
  },
});

const emit = defineEmits(['close', 'select']);

const mapElement = ref(null);
const searchInput = ref(null);
const query = ref('');
const loading = ref(false);
const errorMessage = ref('');
const selectedAddress = ref('');
const selected = reactive({
  name: '',
  address: '',
  lat: null,
  lng: null,
  sggCode: null,
});

const hasSelection = computed(
  () => Number.isFinite(selected.lat) && Number.isFinite(selected.lng),
);

let map = null;
let marker = null;
let geocoder = null;
let places = null;
let addressRequestId = 0;
let previousBodyOverflow = '';

function handleMapClick(mouseEvent) {
  updatePosition(mouseEvent.latLng.getLat(), mouseEvent.latLng.getLng());
}

function normalizedInitialLocation() {
  const lat = Number(props.initialLocation?.lat);
  const lng = Number(props.initialLocation?.lng);

  if (Number.isFinite(lat) && Number.isFinite(lng)) {
    return { lat, lng };
  }

  return DEFAULT_LOCATION;
}

async function initializeMap() {
  loading.value = true;
  errorMessage.value = '';

  try {
    await nextTick();
    await loadKakaoSdk();

    if (!window.kakao?.maps?.services) {
      throw new Error('카카오 지도 검색 서비스를 불러오지 못했습니다.');
    }

    const initial = normalizedInitialLocation();
    const center = new window.kakao.maps.LatLng(initial.lat, initial.lng);

    if (!map) {
      map = new window.kakao.maps.Map(mapElement.value, {
        center,
        level: 3,
      });
      marker = new window.kakao.maps.Marker({ map, position: center });
      geocoder = new window.kakao.maps.services.Geocoder();
      places = new window.kakao.maps.services.Places();

      window.kakao.maps.event.addListener(map, 'click', handleMapClick);
    } else {
      map.relayout();
      map.setCenter(center);
    }

    updatePosition(initial.lat, initial.lng, props.initialLocation?.name ?? '');
    await nextTick();
    map.relayout();
    map.setCenter(center);
    searchInput.value?.focus();
  } catch (error) {
    errorMessage.value = error?.message ?? '지도를 불러오지 못했습니다.';
  } finally {
    loading.value = false;
  }
}

function updatePosition(lat, lng, preferredName = '') {
  if (!map || !marker || !geocoder) return;

  errorMessage.value = '';
  const position = new window.kakao.maps.LatLng(lat, lng);
  marker.setPosition(position);
  selected.lat = lat;
  selected.lng = lng;
  selected.name = preferredName;
  selected.address = '';
  selected.sggCode = null;
  selectedAddress.value = '주소를 확인하는 중이에요.';

  const requestId = ++addressRequestId;
  geocoder.coord2Address(lng, lat, (result, status) => {
    if (requestId !== addressRequestId) return;

    if (status === window.kakao.maps.services.Status.OK && result[0]) {
      const roadAddress = result[0].road_address?.address_name;
      const parcelAddress = result[0].address?.address_name;
      const address =
        roadAddress || parcelAddress || `${lat.toFixed(6)}, ${lng.toFixed(6)}`;

      selected.address = address;
      selected.name = preferredName || address;
      selectedAddress.value = address;
      return;
    }

    const coordinates = `${lat.toFixed(6)}, ${lng.toFixed(6)}`;
    selected.address = coordinates;
    selected.name = preferredName || coordinates;
    selectedAddress.value = coordinates;
  });

  geocoder.coord2RegionCode(lng, lat, (result, status) => {
    if (requestId !== addressRequestId) return;
    if (status !== window.kakao.maps.services.Status.OK) return;

    const legalDong = result.find((r) => r.region_type === 'B') ?? result[0];
    selected.sggCode = legalDong?.code ? legalDong.code.slice(0, 5) : null;
  });
}

function searchLocation() {
  if (!query.value || !places || !map) return;

  errorMessage.value = '';
  places.keywordSearch(query.value, (results, status) => {
    if (status !== window.kakao.maps.services.Status.OK || !results.length) {
      errorMessage.value = '검색 결과를 찾을 수 없습니다.';
      return;
    }

    const place = results[0];
    const lat = Number(place.y);
    const lng = Number(place.x);
    const position = new window.kakao.maps.LatLng(lat, lng);

    map.setCenter(position);
    updatePosition(lat, lng, place.place_name);
  });
}

function confirm() {
  if (!hasSelection.value) return;

  emit('select', {
    name: selected.name || selected.address,
    address: selected.address,
    lat: selected.lat,
    lng: selected.lng,
    sggCode: selected.sggCode,
  });
  close();
}

function close() {
  emit('close');
}

function handleKeydown(event) {
  if (props.open && event.key === 'Escape') close();
}

watch(
  () => props.open,
  (isOpen) => {
    if (isOpen) {
      previousBodyOverflow = document.body.style.overflow;
      document.body.style.overflow = 'hidden';
      initializeMap();
    } else {
      document.body.style.overflow = previousBodyOverflow;
    }
  },
);

onMounted(() => document.addEventListener('keydown', handleKeydown));

onDeactivated(() => {
  if (!props.open) return;

  addressRequestId += 1;
  document.body.style.overflow = previousBodyOverflow;
  emit('close');
});

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown);
  document.body.style.overflow = previousBodyOverflow;
  addressRequestId += 1;

  if (map) {
    window.kakao?.maps?.event?.removeListener(map, 'click', handleMapClick);
  }

  marker?.setMap(null);
  marker = null;
  map = null;
});
</script>

<style scoped>
.location-picker-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: rgba(33, 30, 24, 0.55);
}

.location-picker {
  width: min(100%, 560px);
  max-height: calc(100dvh - 40px);
  padding: 20px;
  overflow-y: auto;
  border-radius: 20px;
  background: var(--white);
  box-shadow: 0 16px 40px rgba(33, 30, 24, 0.22);
}

.picker-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.picker-header h2 {
  font-size: 18px;
  color: var(--text-primary);
}

.picker-header p {
  margin-top: 5px;
  color: var(--kb-silver);
  font-size: 12px;
}

.close-button {
  flex: 0 0 34px;
  height: 34px;
  border-radius: 50%;
  background: var(--bg);
  color: var(--kb-gray);
  font-size: 25px;
  line-height: 1;
}

.map-search {
  display: flex;
  gap: 8px;
  margin-top: 18px;
}

.map-search input {
  min-width: 0;
  flex: 1;
  height: 44px;
  padding: 0 13px;
  border: 1px solid var(--border);
  border-radius: 12px;
  font-size: 13px;
}

.map-search input:focus {
  border-color: var(--kb-yellow);
}

.map-search button {
  flex: 0 0 72px;
  border-radius: 12px;
  background: var(--kb-gray);
  color: var(--white);
  font-size: 13px;
  font-weight: 700;
}

.map-search button:disabled {
  opacity: 0.45;
  cursor: default;
}

.map-wrap {
  position: relative;
  height: 340px;
  margin-top: 12px;
  overflow: hidden;
  border: 1px solid var(--border);
  border-radius: 14px;
  background: #f5efdb;
}

.map {
  width: 100%;
  height: 100%;
}

.map-state {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(247, 246, 243, 0.92);
  color: var(--kb-gray);
  font-size: 13px;
  text-align: center;
  pointer-events: none;
}

.map-error {
  color: var(--danger);
}

.selected-address {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  min-height: 68px;
  margin-top: 12px;
  padding: 13px 14px;
  border-radius: 12px;
  background: var(--yellow-tint);
}

.selected-address .pin {
  margin-top: 2px;
  color: var(--kb-yellow);
  font-size: 13px;
}

.selected-address strong {
  color: var(--text-primary);
  font-size: 12px;
}

.selected-address p {
  margin-top: 4px;
  color: var(--kb-gray);
  font-size: 13px;
  line-height: 1.45;
}

.picker-footer {
  display: flex;
  gap: 8px;
  margin-top: 16px;
}

.picker-footer button {
  height: 44px;
  border-radius: 12px;
  font-size: 13.5px;
  font-weight: 700;
}

.cancel-button {
  flex: 0 0 88px;
  border: 1px solid var(--border);
  background: var(--white);
}

.confirm-button {
  flex: 1;
  background: var(--kb-yellow-header);
}

.confirm-button:disabled {
  opacity: 0.45;
  cursor: default;
}

@media (max-width: 600px) {
  .location-picker-overlay {
    align-items: flex-end;
    padding: 0;
  }

  .location-picker {
    max-height: 92dvh;
    padding: 18px 16px calc(16px + env(safe-area-inset-bottom));
    border-radius: 22px 22px 0 0;
  }

  .map-wrap {
    height: min(42dvh, 330px);
  }
}
</style>
