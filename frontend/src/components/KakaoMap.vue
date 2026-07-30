<template>
  <div ref="mapElement" class="kakao-map"></div>
</template>

<script setup>
import { onMounted, ref, watch, computed } from 'vue';
import {
  infraColor,
  infraIconSvg,
  INFRA_ICON_PATHS,
} from '@/constants/infraIcons';
import {
  safetyColor,
  safetyIconSvg,
  SAFETY_ICON_PATHS,
} from '@/constants/safetyIcons';

const props = defineProps({
  markers: { type: Array, default: () => [] },
  dots: { type: Array, default: () => [] },
  center: { type: Object, default: () => ({ lat: 37.5563, lng: 126.9723 }) }, // default : 서울역
  activeDotKey: { type: String, default: null },
});

const emit = defineEmits([
  'marker-click',
  'bounds-change',
  'dot-click',
  'dot-hover',
]);

const mapElement = ref(null);
const KAKAO_API_KEY = import.meta.env.VITE_KAKAO_MAP_KEY;

const DEFAULT_DOT_ICON =
  '<svg width="14" height="14" viewBox="0 0 16 16" fill="none" stroke="#fff" stroke-width="1.5"><circle cx="8" cy="8" r="2.5"/></svg>';

let map = null;
let overlays = [];
let boundsFit = false;
let dotElements = new Map();

function pinSvg(selected) {
  return `<svg width="30" height="37" viewBox="0 0 26 32" fill="none">
    <path d="M13 0C5.8 0 0 5.7 0 12.8 0 22.4 13 32 13 32s13-9.6 13-19.2C26 5.7 20.2 0 13 0z"
          fill="${selected ? '#fe7b00' : '#ffbc00'}"/>
    <circle cx="13" cy="12.5" r="5" fill="#fff"/>
  </svg>`;
}

function dotSvg(category) {
  return infraIconSvg(category) || safetyIconSvg(category) || DEFAULT_DOT_ICON;
}

function dotColor(category) {
  if (INFRA_ICON_PATHS[category]) return infraColor(category);
  if (SAFETY_ICON_PATHS[category]) return safetyColor();
  return '#8a8d8f';
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

function createDotElement(dot) {
  const element = document.createElement('div');
  element.className = 'infra-dot-wrap';
  const bg = dot.color || dotColor(dot.category);
  element.innerHTML = `<span class="infra-dot" style="background:${bg}">${dotSvg(dot.category)}</span>`;
  element.addEventListener('click', (e) => {
    e.stopPropagation();
    emit('dot-click', dot);
    // console.log('click');
  });
  element.addEventListener('mouseenter', () => {
    emit('dot-hover', dot);
    // console.log('hover on');
  });
  element.addEventListener('mouseleave', () => {
    emit('dot-hover', null);
    // console.log('hover leave');
  });
  return element;
}

function dotKey(dot) {
  return `${dot.lat},${dot.lng}`;
}

function fitToPoints(points) {
  if (!map || points.length === 0) return;

  if (points.length === 1) {
    const p = points[0];
    map.setCenter(new window.kakao.maps.LatLng(p.lat, p.lng));
    return;
  }

  const bounds = new window.kakao.maps.LatLngBounds();
  points.forEach((p) => {
    bounds.extend(new window.kakao.maps.LatLng(p.lat, p.lng));
  });

  map.setBounds(bounds, 52, 20, 48, 20);
}

function fitAllMarkers() {
  fitToPoints(props.markers);
}

function fitSelected() {
  fitToPoints([...props.markers, ...props.dots]);
}

function redraw() {
  if (!map) return;

  overlays.forEach((o) => {
    o.setMap(null);
  });
  overlays = [];
  dotElements = new Map();

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
  });

  props.dots.forEach((dot) => {
    const latlng = new window.kakao.maps.LatLng(dot.lat, dot.lng);
    const element = createDotElement(dot);
    const dotElement = element.querySelector('.infra-dot');
    const key = dotKey(dot);

    if (dotElement) {
      dotElements.set(key, dotElement);
      if (key === props.activeDotKey) {
        dotElement.classList.add('infra-dot--active');
      }
    }

    const overlay = new window.kakao.maps.CustomOverlay({
      position: latlng,
      content: createDotElement(dot),
      zIndex: 3,
    });

    overlay.setMap(map);
    overlays.push(overlay);
  });

  if (!boundsFit && props.markers.length > 0) {
    fitAllMarkers();
    boundsFit = true;
  }
}

function handleIdle() {
  if (!map) return;

  const bounds = map.getBounds();
  const sw = bounds.getSouthWest();
  const ne = bounds.getNorthEast();

  emit('bounds-change', {
    swLat: sw.getLat(),
    swLng: sw.getLng(),
    neLat: ne.getLat(),
    neLng: ne.getLng(),
  });
}

watch(() => [props.markers, props.dots], redraw, { deep: true });

const selectedPos = computed(() => {
  const m = props.markers.find((x) => x.selected);
  return m ? `${m.lat},${m.lng}` : null;
});

watch(selectedPos, (pos) => {
  if (!map) return;
  if (pos) {
    fitSelected();
  } else {
    fitAllMarkers();
  }
});

watch(
  () => props.activeDotKey,
  (key, previousKey) => {
    if (previousKey) {
      dotElements.get(previousKey)?.classList.remove('infra-dot--active');
    }

    if (key) {
      dotElements.get(key)?.classList.add('infra-dot--active');
    }
  },
);

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
      window.kakao.maps.event.addListener(map, 'idle', handleIdle);
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
  z-index: 10;
}

:deep(.property-pin svg) {
  display: block;
}

:deep(.infra-dot) {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: 2px solid #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.3);
}

:deep(.infra-dot svg) {
  display: block;
}

:deep(.infra-dot--active) {
  box-shadow:
    0 0 0 3px rgba(255, 188, 0, 0.75),
    0 1px 4px rgba(0, 0, 0, 0.3);
}

:deep(.infra-dot) {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: 2px solid #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.3);
  cursor: pointer;
}
</style>
