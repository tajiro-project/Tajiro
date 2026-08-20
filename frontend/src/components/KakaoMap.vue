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
import iconApartment from '@/assets/img/pin/apartment.svg?raw';
import iconOfficetel from '@/assets/img/pin/officetel.svg?raw';
import iconOneroom from '@/assets/img/pin/oneroom.svg?raw';
import iconHouse from '@/assets/img/pin/house.svg?raw';
import iconWorkplaceMarker from '@/assets/img/pin/workplace-marker.svg?raw';

/**
 * 도형이 viewBox 안쪽에만 그려져 있어 바닥이 끝에 닿지 않는다.
 * 핀 끝(viewBox 기준 y≈460/512)이 좌표에 오도록 앵커를 맞춘다.
 */
const REFERENCE_Y_ANCHOR = 0.9;

// 매물 유형별 핀 배경(fill), 테두리(line), 아이콘 색(ink)
const PIN_TYPES = {
  아파트: {
    fill: '#1f9d95',
    line: '#14706a',
    ink: '#fff',
    icon: iconApartment,
  },
  오피스텔: {
    fill: '#3f72c9',
    line: '#2a5197',
    ink: '#fff',
    icon: iconOfficetel,
  },
  원룸: { fill: '#f0899f', line: '#c25b74', ink: '#fff', icon: iconOneroom },
  '주택/빌라': {
    fill: '#62b14e',
    line: '#428036',
    ink: '#fff',
    icon: iconHouse,
  },
};

const PIN_FALLBACK = {
  fill: '#8a8477',
  line: '#605b51',
  ink: '#fff',
  icon: iconApartment,
};

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
let previousListDotCount = 0;

const LIST_INITIAL_MIN_LEVEL = 4;

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

function pinSvg(fill, line) {
  // paint-order="stroke" 로 테두리를 면 뒤에 깔아야 핀이 가늘어지지 않는다
  return `<svg width="32" height="38" viewBox="0 0 44 52" fill="none">
    <path
      d="M22 51.5C15 41 3.5 33 3.5 21.5a18.5 18.5 0 1 1 37 0C40.5 33 29 41 22 51.5z"
      fill="${fill}"
      stroke="${line}"
      stroke-width="4"
      stroke-linejoin="round"
      paint-order="stroke"
    />
  </svg>`;
}

/**
 * 순위 안에 들면 메달, 아니면 매물 개수. 둘 다 아니면 배지 없음.
 * 배지는 흰 바탕이라 글자에 핀 배경색(fill)을 쓴다
 */
function badgeHtml(marker, fill) {
  if (marker.medalUrl) {
    return `<img class="pin-badge medal" src="${marker.medalUrl}" alt="">`;
  }
  if (props.mode === 'list' && marker.count > 1) {
    return `<span class="pin-badge" style="color:${fill}">${marker.count}+</span>`;
  }
  return '';
}

/**
 * 이 레벨부터 핀을 묶는다. 카카오 지도는 레벨과 축척이 정해져 있다.
 *   3 → 50m,  4 → 100m,  5 → 250m,  6 → 500m,  7 → 1km
 * 축척 250m(레벨 5)까지는 건물별 핀을 보여주고, 500m부터 묶는다.
 */
const CLUSTER_FROM_LEVEL = 6;

/** 묶을 때 기준이 되는 화면상 거리(px) */
const CLUSTER_GAP_PX = 48;

/**
 * 현재 줌 기준으로 가까운 핀끼리 묶는다.
 * 좌표가 아니라 화면 픽셀 거리로 판단하므로, 확대하면 자연히 흩어진다.
 */
function clusterMarkers(markers) {
  const valid = markers.filter((m) => m.lat != null && m.lng != null);

  // 기준 축척보다 확대돼 있으면 건물별 핀을 그대로 보여준다
  if (map.getLevel() < CLUSTER_FROM_LEVEL) {
    return valid.map((m) => ({
      lat: m.lat,
      lng: m.lng,
      members: [m],
      count: m.count || 1,
      rank: m.rank,
    }));
  }

  const projection = map.getProjection();

  const points = valid
    .map((m) => ({
      marker: m,
      point: projection.containerPointFromCoords(
        new window.kakao.maps.LatLng(m.lat, m.lng),
      ),
      taken: false,
    }))
    // 배열 순서에 따라 묶음이 달라지지 않도록 화면 좌표로 정렬한다
    .sort((a, b) => a.point.x - b.point.x || a.point.y - b.point.y);

  const clusters = [];

  for (const seed of points) {
    if (seed.taken) continue;
    seed.taken = true;

    const members = [seed.marker];

    // 씨앗 주변만 훑는다. 연쇄로 이으면 촘촘한 지역이 통째로 한 덩어리가 된다
    for (const other of points) {
      if (other.taken) continue;
      const dx = other.point.x - seed.point.x;
      const dy = other.point.y - seed.point.y;
      if (Math.sqrt(dx * dx + dy * dy) > CLUSTER_GAP_PX) continue;

      other.taken = true;
      members.push(other.marker);
    }

    clusters.push({
      // 씨앗이 아니라 묶음의 중심에 동그라미를 놓는다
      lat: members.reduce((sum, m) => sum + Number(m.lat), 0) / members.length,
      lng: members.reduce((sum, m) => sum + Number(m.lng), 0) / members.length,
      members,
      // 묶인 건물들이 가진 매물 수의 합
      count: members.reduce((sum, m) => sum + (m.count || 1), 0),
      // 묶음 안에 순위권이 있으면 그중 가장 높은 순위를 쓴다
      rank: members.reduce(
        (best, m) =>
          m.rank != null && (best == null || m.rank < best) ? m.rank : best,
        null,
      ),
    });
  }

  return clusters;
}

function createClusterElement(cluster) {
  // 묶인 건물이 많을수록 원을 키운다
  const size = Math.min(50, 30 + cluster.members.length * 4);

  const element = document.createElement('div');
  element.className = 'pin-cluster';
  element.style.width = `${size}px`;
  element.style.height = `${size}px`;
  element.style.fontSize = `${Math.round(size * 0.3)}px`;
  element.innerHTML = `<span>${cluster.count}</span>`;

  element.addEventListener('click', (e) => {
    e.stopPropagation();

    // 핀이 흩어지는 축척까지 한 번에 확대한다
    const anchor = new window.kakao.maps.LatLng(cluster.lat, cluster.lng);
    const target = Math.min(map.getLevel() - 1, CLUSTER_FROM_LEVEL - 1);
    map.setLevel(Math.max(1, target), { anchor });
  });

  return element;
}

/**
 * 인프라·안전 상세 화면의 기준점 핀.
 * 유형 구분이 필요 없는 자리라 기존 노란 핀을 그대로 쓴다.
 */
function createAnchorPinElement(marker) {
  const element = document.createElement('div');
  element.className = 'property-pin anchor';
  element.innerHTML = `<svg width="30" height="37" viewBox="0 0 26 32" fill="none">
    <path
      d="M13 0C5.8 0 0 5.7 0 12.8C0 22.4 13 32 13 32s13-9.6 13-19.2C26 5.7 20.2 0 13 0z"
      fill="#ffbc00"
    />
    <circle cx="13" cy="12.5" r="7" fill="#fff"/>
  </svg>`;

  element.addEventListener('click', (e) => {
    e.stopPropagation();
    emit('marker-click', marker);
  });
  return element;
}

function createPinElement(marker) {
  if (props.mode !== 'list') return createAnchorPinElement(marker);

  const type = PIN_TYPES[marker.propertyType] ?? PIN_FALLBACK;

  const element = document.createElement('div');
  element.className = marker.selected ? 'property-pin selected' : 'property-pin';
  element.innerHTML =
    pinSvg(type.fill, type.line) +
    `<span class="pin-icon" style="color:${type.ink}">${type.icon}</span>` +
    badgeHtml(marker, type.fill);

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

  element.innerHTML = iconWorkplaceMarker;

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
function fitAllElements(animate = false) {
  if (!map) return;
  const bounds = new window.kakao.maps.LatLngBounds();
  let hasValidCoords = false;
  let hasPropertyMarkers = false;

  props.markers.forEach((m) => {
    if (m.lat != null && m.lng != null) {
      bounds.extend(new window.kakao.maps.LatLng(m.lat, m.lng));
      hasValidCoords = true;
      hasPropertyMarkers = true;
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
    if (map.getLevel() < LIST_INITIAL_MIN_LEVEL) {
      map.setLevel(LIST_INITIAL_MIN_LEVEL);
    }
    hasFittedListView = hasPropertyMarkers;
    return;
  }

  if (!animate) {
    map.setBounds(bounds, 40, 40, 40, 40);
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

function applyViewMode(animateList = false) {
  if (props.mode === 'infra' || props.mode === 'safety') {
    fitCenterWithAllElements();
  } else {
    fitAllElements(animateList);
  }
}

/**
 * 오버레이만 다시 그린다. 지도 시점은 건드리지 않는다.
 * 줌이 바뀌면 묶이는 핀이 달라지므로 이 함수만 따로 호출한다.
 */
function redrawOverlays() {
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

  // 매물 핀 — 화면상 가까운 것끼리 묶어서 그린다
  clusterMarkers(props.markers).forEach((cluster) => {
    const single = cluster.members.length === 1;
    const overlay = new window.kakao.maps.CustomOverlay({
      position: new window.kakao.maps.LatLng(cluster.lat, cluster.lng),
      content: single
        ? createPinElement(cluster.members[0])
        : createClusterElement(cluster),
      yAnchor: single ? 1 : 0.5,
      // 선택된 핀은 인프라 도트 위로 올린다
      zIndex: single && cluster.members[0].selected
        ? 100
        : cluster.rank
          ? 10 - cluster.rank
          : 5,
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
      yAnchor: REFERENCE_Y_ANCHOR,
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

}

/** 오버레이를 다시 그리고 지도 시점까지 맞춘다 */
function redraw() {
  if (!map) return;

  redrawOverlays();

  const animateList =
    props.mode === 'list' &&
    previousListDotCount === 0 &&
    props.dots.length > 0;

  previousListDotCount = props.mode === 'list' ? props.dots.length : 0;
  applyViewMode(animateList);
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

  // 지도가 멈춘 뒤라 화면 좌표가 정확하다. 줌이 바뀌었으면 묶음도 다시 계산된다
  redrawOverlays();

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
  width: 32px;
  height: 38px;
  cursor: pointer;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.22));
}

/* 인프라·안전 화면의 기준점 핀은 기존 크기·그림자를 유지한다 */
:deep(.property-pin.anchor) {
  width: 30px;
  height: 37px;
  filter: drop-shadow(0 2px 3px rgba(102, 77, 0, 0.35));
}

/* 테두리가 viewBox 밖으로 나가므로 잘리지 않게 한다 */
:deep(.property-pin > svg) {
  display: block;
  overflow: visible;
}

:deep(.property-pin.selected) {
  transform: scale(1.12);
  transform-origin: bottom center;
  filter: drop-shadow(0 4px 9px rgba(0, 0, 0, 0.55));
}

:deep(.pin-icon) {
  position: absolute;
  top: 5px;
  left: 50%;
  width: 19px;
  height: 19px;
  transform: translateX(-50%);
  pointer-events: none;
}

/* SVG 안의 fill="currentColor" 가 부모 color 를 따라간다 */
:deep(.pin-icon svg) {
  display: block;
  width: 100%;
  height: 100%;
}

:deep(.reference-location-marker) {
  position: relative;
  width: 44px;
  height: 44px;
  padding: 0;
  border: 0;
  background: transparent;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.28));
  pointer-events: none;
}

:deep(.reference-location-marker svg) {
  display: block;
  width: 100%;
  height: 100%;
}

/* 배지는 박스 밖으로 나가지만 요소 너비는 44px 그대로라 핀 끝점이 안 밀린다 */
:deep(.pin-badge) {
  position: absolute;
  top: -1px;
  right: -5px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 16px;
  height: 16px;
  box-sizing: border-box;
  background: #fff;
  border-radius: 50%;
  font-size: 9px;
  font-weight: 700;
  pointer-events: none;
}

:deep(.pin-cluster) {
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  background: rgba(59, 148, 217, 0.42);
  border: 2px solid rgba(255, 255, 255, 0.9);
  border-radius: 50%;
  font-weight: 700;
  color: #fff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.35);
  cursor: pointer;
  transition:
    background 0.15s ease,
    transform 0.15s ease;
}

:deep(.pin-cluster:hover) {
  background: rgba(59, 148, 217, 0.58);
  transform: scale(1.06);
}

:deep(.pin-badge.medal) {
  padding: 0;
  background: transparent;
  object-fit: contain;
  border-radius: 50%;
  box-shadow: 0 0 0 2px #fff;
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
