<template>
  <div ref="mapElement" class="kakao-map"></div>
</template>

<script setup>
import {
  onMounted,
  onUnmounted,
  onActivated,
  nextTick,
  ref,
  watch,
  h,
  render,
} from 'vue';
import { loadKakaoSdk } from '@/utils/kakaoSdk';
import {
  INFRA_CATEGORIES,
  AMENITY_CATEGORIES,
  SAFETY_CATEGORIES,
} from '@/constants/preferenceOptions';
import { MapPin } from 'lucide-vue-next';

const props = defineProps({
  mode: {
    type: String,
    default: 'list',
    validator: (val) => ['list', 'infra', 'safety'].includes(val),
  },
  markers: { type: Array, default: () => [] },
  referenceLocation: { type: Object, default: null },
  dots: { type: Array, default: () => [] },
  polygons: { type: Array, default: () => [] },
  center: { type: Object, default: () => ({ lat: 37.5563, lng: 126.9723 }) },
  activeDotKey: { type: String, default: null },
  level: { type: Number, default: 3 },
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
let polygonOverlays = [];
let dotElements = new Map();
let hasFittedListView = false;

// --- 카테고리 매핑 ---
const ALL_CATEGORIES = [
  ...INFRA_CATEGORIES,
  ...AMENITY_CATEGORIES,
  ...SAFETY_CATEGORIES,
];
const categoryIconMap = {};
const categoryColorMap = {};

ALL_CATEGORIES.forEach((cat) => {
  if (cat.key) {
    categoryIconMap[cat.key] = cat.icon;
    categoryColorMap[cat.key] = cat.color;
    categoryIconMap[cat.key.toLowerCase()] = cat.icon;
    categoryColorMap[cat.key.toLowerCase()] = cat.color;
  }
});

function pinSvg(count, hasMedal) {
  const showCount = props.mode === 'list' && count > 1 && !hasMedal;

  const label = showCount
    ? `
      <text
        x="9"
        y="16"
        text-anchor="middle"
        font-size="7"
        font-weight="500"
        fill="#8a8477"
      >+</text>
      <text
        x="15"
        y="16"
        text-anchor="middle"
        font-size="10"
        font-weight="700"
        fill="#545045"
      >${count}</text>
    `
    : '';

  const fill = hasMedal ? '#fe7b00' : '#ffbc00';

  return `<svg width="30" height="37" viewBox="0 0 26 32" fill="none">
    <path
      d="M13 0C5.8 0 0 5.7 0 12.8C0 22.4 13 32 13 32s13-9.6 13-19.2C26 5.7 20.2 0 13 0z"
      fill="${fill}"
    />
    <circle cx="13" cy="12.5" r="7" fill="#fff"/>
    ${label}
  </svg>`;
}

function createPinElement(marker) {
  const element = document.createElement('div');
  element.className = 'property-pin';
  element.innerHTML =
    pinSvg(marker.count, !!marker.medalUrl) +
    (marker.medalUrl
      ? `<img class="pin-medal" src="${marker.medalUrl}" alt="">`
      : '');
  element.addEventListener('click', (e) => {
    e.stopPropagation();
    emit('marker-click', marker);
  });
  return element;
}

function createReferenceLocationElement() {
  const element = document.createElement('div');
  element.className = 'reference-location-marker';
  element.setAttribute('aria-hidden', 'true');

  const pole = document.createElement('span');
  pole.className = 'reference-location-pole';

  const flag = document.createElement('span');
  flag.className = 'reference-location-flag';

  element.append(pole, flag);

  return element;
}

function createDotElement(dot) {
  const element = document.createElement('div');
  element.className = 'infra-dot-wrap';

  const dotSpan = document.createElement('span');
  dotSpan.className = 'infra-dot';

  const catKey = dot.category || dot.categoryKey || '';
  const backgroundColor =
    dot.color ||
    categoryColorMap[catKey] ||
    categoryColorMap[catKey.toLowerCase()] ||
    '#1E3A8A';

  dotSpan.style.background = backgroundColor;

  const IconComponent =
    categoryIconMap[catKey] || categoryIconMap[catKey.toLowerCase()] || MapPin;

  const vnode = h(IconComponent, {
    size: 13,
    color: '#ffffff',
    strokeWidth: 2.5,
  });
  render(vnode, dotSpan);

  element.appendChild(dotSpan);

  element.addEventListener('click', (e) => {
    e.stopPropagation();
    emit('dot-click', dot);
  });
  element.addEventListener('mouseenter', () => emit('dot-hover', dot));
  element.addEventListener('mouseleave', () => emit('dot-hover', null));

  return element;
}

function dotKey(dot) {
  return `${dot.lat},${dot.lng}`;
}

function convertPathToKakao(path) {
  if (!Array.isArray(path)) return [];
  return path
    .map((coord) => {
      if (Array.isArray(coord) && coord.length >= 2) {
        return new window.kakao.maps.LatLng(Number(coord[1]), Number(coord[0]));
      } else if (coord && coord.lat != null && coord.lng != null) {
        return new window.kakao.maps.LatLng(
          Number(coord.lat),
          Number(coord.lng),
        );
      }
      return null;
    })
    .filter(Boolean);
}

// 1. [Mode: list] 영역에 맞춘 자동 시점 계산
function fitAllElements() {
  if (!map) return;
  const bounds = new window.kakao.maps.LatLngBounds();
  let hasValidCoords = false;

  props.markers.forEach((m) => {
    if (m.lat != null && m.lng != null) {
      bounds.extend(new window.kakao.maps.LatLng(m.lat, m.lng));
      hasValidCoords = true;
    }
  });

  if (
    props.mode === 'list' &&
    props.referenceLocation?.lat != null &&
    props.referenceLocation?.lng != null
  ) {
    bounds.extend(
      new window.kakao.maps.LatLng(
        Number(props.referenceLocation.lat),
        Number(props.referenceLocation.lng),
      ),
    );
    hasValidCoords = true;
  }

  props.dots.forEach((d) => {
    if (d.lat != null && d.lng != null) {
      bounds.extend(new window.kakao.maps.LatLng(d.lat, d.lng));
      hasValidCoords = true;
    }
  });

  props.polygons.forEach((poly) => {
    if (!poly.path || !Array.isArray(poly.path)) return;
    const addPathToBounds = (pathArr) => {
      pathArr.forEach((coord) => {
        if (Array.isArray(coord) && coord.length >= 2) {
          bounds.extend(
            new window.kakao.maps.LatLng(Number(coord[1]), Number(coord[0])),
          );
          hasValidCoords = true;
        } else if (coord && coord.lat != null && coord.lng != null) {
          bounds.extend(
            new window.kakao.maps.LatLng(Number(coord.lat), Number(coord.lng)),
          );
          hasValidCoords = true;
        } else if (Array.isArray(coord)) {
          addPathToBounds(coord);
        }
      });
    };
    addPathToBounds(poly.path);
  });

  if (!hasValidCoords) {
    if (props.center?.lat && props.center?.lng) {
      map.setCenter(
        new window.kakao.maps.LatLng(props.center.lat, props.center.lng),
      );
      if (props.level) map.setLevel(props.level);
    }
    return;
  }

  if (!hasFittedListView) {
    map.setBounds(bounds, 40, 40, 40, 40);
    hasFittedListView = true;
    return;
  }

  const previousCenter = map.getCenter();
  const previousLevel = map.getLevel();

  map.setBounds(bounds, 40, 40, 40, 40);

  const targetCenter = map.getCenter();
  const targetLevel = map.getLevel();
  const centerChanged =
    Math.abs(previousCenter.getLat() - targetCenter.getLat()) > 0.000001 ||
    Math.abs(previousCenter.getLng() - targetCenter.getLng()) > 0.000001;

  if (!centerChanged && previousLevel === targetLevel) return;

  map.jump(previousCenter, previousLevel);
  map.jump(targetCenter, targetLevel, {
    animate: { duration: 180 },
  });
}

// 2. [Mode: infra & safety] 매물 정중앙 고정 + 가독성 극대화 스마트 줌
function fitCenterWithAllElements() {
  if (!map || !props.center?.lat || !props.center?.lng) return;

  const centerLat = Number(props.center.lat);
  const centerLng = Number(props.center.lng);
  if (isNaN(centerLat) || isNaN(centerLng)) return;

  let maxLatDiff = 0;
  let maxLngDiff = 0;

  const checkPoint = (lat, lng) => {
    const nLat = Number(lat);
    const nLng = Number(lng);
    if (!isNaN(nLat) && !isNaN(nLng)) {
      const latDiff = Math.abs(nLat - centerLat);
      const lngDiff = Math.abs(nLng - centerLng);
      if (latDiff > maxLatDiff) maxLatDiff = latDiff;
      if (lngDiff > maxLngDiff) maxLngDiff = lngDiff;
    }
  };

  props.dots.forEach((d) => checkPoint(d.lat, d.lng));
  props.polygons.forEach((poly) => {
    if (!poly.path || !Array.isArray(poly.path)) return;
    const processPath = (pathArr) => {
      pathArr.forEach((coord) => {
        if (Array.isArray(coord) && coord.length >= 2) {
          checkPoint(coord[1], coord[0]);
        } else if (coord && coord.lat != null && coord.lng != null) {
          checkPoint(coord.lat, coord.lng);
        } else if (Array.isArray(coord)) {
          processPath(coord);
        }
      });
    };
    processPath(poly.path);
  });

  if (maxLatDiff === 0 && maxLngDiff === 0) {
    map.setCenter(new window.kakao.maps.LatLng(centerLat, centerLng));
    if (props.level) map.setLevel(props.level);
    return;
  }

  // 여백을 포함한 대칭 경계 계산
  const paddingMultiplier = 1.05;
  const paddedLatDiff = maxLatDiff * paddingMultiplier;
  const paddedLngDiff = maxLngDiff * paddingMultiplier;

  const sw = new window.kakao.maps.LatLng(
    centerLat - paddedLatDiff,
    centerLng - paddedLngDiff,
  );
  const ne = new window.kakao.maps.LatLng(
    centerLat + paddedLatDiff,
    centerLng + paddedLngDiff,
  );
  const bounds = new window.kakao.maps.LatLngBounds(sw, ne);

  // 1차 적용: 카카오 맵 보수적 bounds 계산
  map.setBounds(bounds, 10, 10, 10, 10);
  map.setCenter(new window.kakao.maps.LatLng(centerLat, centerLng));

  // 2차 스마트 보정: 1단계 더 확대해도 모든 도트가 화면 안에 잘 들어오는지 테스트
  const currentLevel = map.getLevel();
  if (currentLevel > 1) {
    const testLevel = currentLevel - 1;
    map.setLevel(testLevel, { animate: false });
    map.setCenter(new window.kakao.maps.LatLng(centerLat, centerLng));

    const testBounds = map.getBounds();
    let allDotsFit = true;

    for (const d of props.dots) {
      if (d.lat != null && d.lng != null) {
        const pt = new window.kakao.maps.LatLng(d.lat, d.lng);
        if (!testBounds.contain(pt)) {
          allDotsFit = false;
          break;
        }
      }
    }

    // 1단계 확대했을 때 도트가 하나라도 화면 밖으로 잘리면 안전하게 이전 레벨로 원복
    if (!allDotsFit) {
      map.setLevel(currentLevel, { animate: false });
      map.setCenter(new window.kakao.maps.LatLng(centerLat, centerLng));
    }
  }
}

function applyViewMode() {
  if (props.mode === 'infra' || props.mode === 'safety') {
    fitCenterWithAllElements();
  } else {
    fitAllElements();
  }
}

function redraw() {
  if (!map) return;

  overlays.forEach((o) => {
    const dotSpan = o.getContent()?.querySelector?.('.infra-dot');
    if (dotSpan) render(null, dotSpan);
    o.setMap(null);
  });
  overlays = [];
  dotElements.clear();

  polygonOverlays.forEach((p) => p.setMap(null));
  polygonOverlays = [];

  // 매물 핀
  props.markers.forEach((marker) => {
    if (marker.lat == null || marker.lng == null) return;
    const latlng = new window.kakao.maps.LatLng(marker.lat, marker.lng);
    const overlay = new window.kakao.maps.CustomOverlay({
      position: latlng,
      content: createPinElement(marker),
      yAnchor: 1,
      zIndex: marker.rank ? 10 - marker.rank : 5,
    });
    overlay.setMap(map);
    overlays.push(overlay);
  });

  if (
    props.mode === 'list' &&
    props.referenceLocation?.lat != null &&
    props.referenceLocation?.lng != null
  ) {
    const position = new window.kakao.maps.LatLng(
      Number(props.referenceLocation.lat),
      Number(props.referenceLocation.lng),
    );
    const overlay = new window.kakao.maps.CustomOverlay({
      position,
      content: createReferenceLocationElement(),
      yAnchor: 1,
      zIndex: 20,
    });
    overlay.setMap(map);
    overlays.push(overlay);
  }

  // 인프라/안전 도트
  props.dots.forEach((dot) => {
    if (dot.lat == null || dot.lng == null) return;
    if (dot.category === '_dummy' || dot.categoryKey === '_dummy') return;

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

  // 폴리곤
  props.polygons.forEach((poly) => {
    if (!poly.path || !Array.isArray(poly.path)) return;

    const styleOptions = {
      strokeWeight: poly.strokeWeight || 2.5,
      strokeColor: poly.strokeColor || poly.color || '#2563EB',
      strokeOpacity: poly.strokeOpacity || 0.9,
      fillColor: poly.fillColor || poly.color || '#3B82F6',
      fillOpacity: poly.fillOpacity || 0.2,
      zIndex: 1,
    };

    if (Array.isArray(poly.path[0]) && Array.isArray(poly.path[0][0])) {
      poly.path.forEach((subPath) => {
        const kakaoPath = convertPathToKakao(subPath);
        if (kakaoPath.length < 3) return;
        const polygonOverlay = new window.kakao.maps.Polygon({
          path: kakaoPath,
          ...styleOptions,
        });
        polygonOverlay.setMap(map);
        polygonOverlays.push(polygonOverlay);
      });
    } else {
      const kakaoPath = convertPathToKakao(poly.path);
      if (kakaoPath.length < 3) return;
      const polygonOverlay = new window.kakao.maps.Polygon({
        path: kakaoPath,
        ...styleOptions,
      });
      polygonOverlay.setMap(map);
      polygonOverlays.push(polygonOverlay);
    }
  });

  applyViewMode();
}

function updateActiveDot(newKey, oldKey) {
  if (oldKey && dotElements.has(oldKey)) {
    dotElements.get(oldKey).classList.remove('infra-dot--active');
  }
  if (newKey && dotElements.has(newKey)) {
    dotElements.get(newKey).classList.add('infra-dot--active');
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

watch(
  () => [
    props.markers,
    props.referenceLocation,
    props.dots,
    props.polygons,
    props.center,
    props.level,
    props.mode,
  ],
  redraw,
  { deep: true },
);

watch(
  () => props.activeDotKey,
  (newVal, oldVal) => updateActiveDot(newVal, oldVal),
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

onActivated(async () => {
  if (!map) return;
  await nextTick();
  map.relayout();
});

onUnmounted(() => {
  overlays.forEach((o) => {
    const dotSpan = o.getContent()?.querySelector?.('.infra-dot');
    if (dotSpan) render(null, dotSpan);
    o.setMap(null);
  });
  overlays = [];
  polygonOverlays.forEach((p) => p.setMap(null));
  polygonOverlays = [];
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
  position: relative;
  display: block;
  cursor: pointer;
  filter: drop-shadow(0 2px 3px rgba(102, 77, 0, 0.35));
}

:deep(.property-pin svg) {
  display: block;
}

:deep(.reference-location-marker) {
  position: relative;
  width: 24px;
  height: 32px;
  padding: 0;
  border: 0;
  background: transparent;
  filter: drop-shadow(0 1px 1px rgba(51, 48, 42, 0.2));
  pointer-events: none;
}

:deep(.reference-location-pole) {
  position: absolute;
  top: 0;
  left: 50%;
  width: 2px;
  height: 32px;
  transform: translateX(-50%);
  background: #161616;
}

:deep(.reference-location-flag) {
  position: absolute;
  top: 3px;
  left: 50%;
  width: 18px;
  height: 12px;
  background: #e91608;
  clip-path: polygon(0 0, 100% 50%, 0 100%);
}

:deep(.pin-medal) {
  position: absolute;
  top: 14.5px;
  left: 15px;
  transform: translate(-50%, -50%);
  width: 16.2px;
  height: 16.2px;
  pointer-events: none;
  border-radius: 50%;
  box-shadow: 0 0 0 1px #ffffff;
}

:deep(.infra-dot) {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  border: 2px solid #ffffff;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.25);
  cursor: pointer;
  transition: transform 0.15s ease;
}

:deep(.infra-dot:hover) {
  transform: scale(1.15);
  z-index: 10;
}

:deep(.infra-dot svg) {
  display: block;
}

:deep(.infra-dot--active) {
  box-shadow:
    0 0 0 3px rgba(255, 183, 3, 0.8),
    0 2px 6px rgba(0, 0, 0, 0.3);
  transform: scale(1.2);
}
</style>
