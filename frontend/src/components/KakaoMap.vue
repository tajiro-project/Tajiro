<template>
  <div ref="mapElement" class="kakao-map"></div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue';

const props = defineProps({
  markers: { type: Array, default: () => [] },
  center: { type: Object, default: () => ({ lat: 37.5563, lng: 126.9723 }) }, // default : 서울역
});

const emit = defineEmits(['marker-click']);

const mapElement = ref(null);
const KAKAO_API_KEY = import.meta.env.VITE_KAKAO_MAP_KEY;

let map = null;
let overlays = [];
let boundsFit = false;

function pinSvg(selected) {
  return `<svg width="30" height="37" viewBox="0 0 26 32" fill="none">
    <path d="M13 0C5.8 0 0 5.7 0 12.8 0 22.4 13 32 13 32s13-9.6 13-19.2C26 5.7 20.2 0 13 0z"
          fill="${selected ? '#fe7b00' : '#ffbc00'}"/>
    <circle cx="13" cy="12.5" r="5" fill="#fff"/>
  </svg>`;
}

function createPinElement(marker) {
  const element = document.createElement('div');
  element.className = 'property-pin';
  element.innerHTML = pinSvg(marker.selected);
  element.addEventListener('click', (e) => {
    e.stopPropagation();
    emit('marker-click', marker);
  });
  return element;
}

function redraw() {
  if (!map) return;

  overlays.forEach((o) => {
    o.setMap(null);
  });
  overlays = [];

  const bounds = new window.kakao.maps.LatLngBounds();

  props.markers.forEach((marker) => {
    const latlng = new window.kakao.maps.LatLng(marker.lat, marker.lng);
    const overlay = new window.kakao.maps.CustomOverlay({
      position: latlng,
      content: createPinElement(marker),
      yAnchor: 1,
      zIndex: 5,
    });
    overlay.setMap(map);
    overlays.push(overlay);
    bounds.extend(latlng);
  });

  if (!boundsFit && props.markers.length > 0) {
    map.setBounds(bounds);
    boundsFit = true;
  }
}

watch(() => props.markers, redraw, { deep: true });

onMounted(() => {
  const script = document.createElement('script');
  script.src = `https://dapi.kakao.com/v2/maps/sdk.js?appkey=${KAKAO_API_KEY}&autoload=false`;
  script.onload = () => {
    window.kakao.maps.load(() => {
      map = new window.kakao.maps.Map(mapElement.value, {
        center: new window.kakao.maps.LatLng(
          props.center.lat,
          props.center.lng,
        ),
        level: 5,
      });
      redraw();
    });
  };
  document.head.appendChild(script);
});
</script>

<style scoped>
.kakao-map {
  width: 100%;
  height: 100%;
  min-height: 160px;
  background-color: #f5efdb;
}

:deep(.property-pin) {
  display: block;
  cursor: pointer;
  filter: drop-shadow(0 2px 3px rgba(102, 77, 0, 0.35));
}

:deep(.property-pin--selected) {
  z-index: 10;
}

:deep(.property-pin svg) {
  display: block;
}
</style>
