<template>
  <div
    ref="mapElement"
    class="kakao-map"
  ></div>
</template>

<script setup>
import { onMounted, onUnmounted, ref, watch, computed, h, render } from 'vue';
import { loadKakaoSdk } from '@/utils/kakaoSdk';
import {
  INFRA_CATEGORIES,
  AMENITY_CATEGORIES,
} from '@/constants/preferenceOptions';
import { MapPin } from 'lucide-vue-next';

const props = defineProps({
  markers: { type: Array, default: () => [] },
  dots: { type: Array, default: () => [] },
  center: { type: Object, default: () => ({ lat: 37.5563, lng: 126.9723 }) },
  activeDotKey: { type: String, default: null },
  level: { type: Number, default: 5 },
});

const emit = defineEmits([
  'marker-click',
  'bounds-change',
  'dot-click',
  'dot-hover',
]);

const mapElement = ref(null);

let map = null;
let overlays = [];
let dotElements = new Map();

// 카테고리별 Lucide 아이콘 및 색상 매핑
const ALL_CATEGORIES = [...INFRA_CATEGORIES, ...AMENITY_CATEGORIES];
const categoryIconMap = {};
const categoryColorMap = {};

ALL_CATEGORIES.forEach((cat) => {
  categoryIconMap[cat.key] = cat.icon;
  if (cat.color) {
    categoryColorMap[cat.key] = cat.color;
  }
});

// 💡 카테고리별 고유 색상 정의 (하나로 통일되지 않도록 구별되는 고유 파스텔/비비드 톤 적용)
const DEFAULT_CATEGORY_COLORS = {
  // 교통 / 공공기관
  subway: '#3B82F6', // 지하철 - 파랑
  bus: '#06B6D4', // 버스 - 스카이블루
  police: '#1E40AF', // 경찰서 - 남색
  fire: '#EF4444', // 소방서 - 강렬한 빨강
  post: '#F43F5E', // 우체국 - 로즈/핑크
  bank: '#2563EB', // 은행 - 로열 블루

  // 교육 / 학군
  school: '#8B5CF6', // 학교 - 보라
  academy: '#A855F7', // 학원 - 라이트 바이올렛

  // 의료 / 건강
  hospital: '#E11D48', // 병원 - 다크 레드
  pharmacy: '#EC4899', // 약국 - 핫핑크

  // 편의 / 쇼핑
  mart: '#F59E0B', // 마트 - 골드/옐로우
  convenience: '#10B981', // 편의점 - 에메랄드 초록
  cafe: '#D97706', // 카페 - 브라운/앰버
  restaurant: '#FB923C', // 음식점 - 연주황

  // 자연 / 휴식
  park: '#059669', // 공원/산책로 - 딥 그린
  culture: '#6366F1', // 문화시설 - 인디고
};

function pinSvg(selected, count) {
  const label =
    count > 1
      ? `<text x="13" y="16" text-anchor="middle" font-size="10" font-weight="700" fill="#33302a">${count}</text>`
      : '';

  return `<svg width="30" height="37" viewBox="0 0 26 32" fill="none">
    <path d="M13 0C5.8 0 0 5.7 0 12.8 0 22.4 13 32 13 32s13-9.6 13-19.2C26 5.7 20.2 0 13 0z"
          fill="${selected ? '#fe7b00' : '#ffbc00'}"/>
    <circle cx="13" cy="12.5" r="7" fill="#fff"/>
    ${label}
  </svg>`;
}

function createPinElement(marker) {
  const element = document.createElement('div');
  element.className = 'property-pin';
  element.innerHTML = pinSvg(marker.selected, marker.count);
  element.addEventListener('click', (e) => {
    e.stopPropagation();
    emit('marker-click', marker);
  });
  return element;
}

function createDotElement(dot) {
  const element = document.createElement('div');
  element.className = 'infra-dot-wrap';

  const dotSpan = document.createElement('span');
  dotSpan.className = 'infra-dot';

  // 💡 카테고리별 고유 색상(DEFAULT_CATEGORY_COLORS)을 우선 적용하여 아이콘별로 색이 다르게 설정
  const backgroundColor =
    DEFAULT_CATEGORY_COLORS[dot.category] ||
    dot.color ||
    categoryColorMap[dot.category] ||
    '#6B7280'; // 정의되지 않은 경우 기본 회색

  dotSpan.style.background = backgroundColor;

  const IconComponent = categoryIconMap[dot.category] || MapPin;

  const vnode = h(IconComponent, {
    size: 14,
    color: '#ffffff',
    strokeWidth: 2.5,
  });
  render(vnode, dotSpan);

  element.appendChild(dotSpan);

  element.addEventListener('click', (e) => {
    e.stopPropagation();
    emit('dot-click', dot);
  });
  element.addEventListener('mouseenter', () => {
    emit('dot-hover', dot);
  });
  element.addEventListener('mouseleave', () => {
    emit('dot-hover', null);
  });
  return element;
}

function dotKey(dot) {
  return `${dot.lat},${dot.lng}`;
}

function fitToPoints(rawPoints) {
  const points = rawPoints.filter((p) => p.lat != null && p.lng != null);
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

  map.setBounds(bounds, 0, 0, 0, 0);
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
    if (marker.lat == null || marker.lng == null) return;
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
    if (dot.lat == null || dot.lng == null) return;
    if (dot.category === '_dummy') return;

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
      content: element,
      zIndex: 3,
    });

    overlay.setMap(map);
    overlays.push(overlay);
  });

  if (props.markers.length > 0 || props.dots.length > 0) {
    fitToPoints([...props.markers, ...props.dots]);
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

onMounted(async () => {
  try {
    await loadKakaoSdk();
    map = new window.kakao.maps.Map(mapElement.value, {
      center: new window.kakao.maps.LatLng(props.center.lat, props.center.lng),
      level: props.level,
    });
    window.kakao.maps.event.addListener(map, 'idle', handleIdle);
    redraw();
  } catch (e) {
    console.warn('[KakaoMap]', e.message);
  }
});

onUnmounted(() => {
  overlays.forEach((o) => {
    const dotSpan = o.getContent()?.querySelector?.('.infra-dot');
    if (dotSpan) render(null, dotSpan);
    o.setMap(null);
  });
  overlays = [];
  dotElements.clear();
  if (map) {
    window.kakao.maps.event.removeListener(map, 'idle', handleIdle);
    map = null;
  }
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
  cursor: pointer;
}

:deep(.infra-dot svg) {
  display: block;
}

:deep(.infra-dot--active) {
  box-shadow:
    0 0 0 3px rgba(136, 136, 136, 0.75),
    0 1px 4px rgba(0, 0, 0, 0.3);
}
</style>
