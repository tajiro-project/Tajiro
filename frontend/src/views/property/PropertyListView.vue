<template>
  <div class="property-list">
    <div class="map-area">
      <KakaoMap
        :markers="markers"
        :center="center"
        @marker-click="onMarkerClick"
      />
    </div>

    <ul class="list">
      <li v-for="m in markers" :key="m.id">
        <button :class="{ on: m.selected }" @click="onMarkerClick(m)">
          {{ m.id }}
        </button>
      </li>
    </ul>
  </div>
</template>

<script setup>
import KakaoMap from '@/components/kakaoMap.vue';
import { computed, ref } from 'vue';

const RAW_MARKERS = [
  { id: '1', lat: 36.3272, lng: 127.4541, selected: true },
  { id: '2', lat: 36.3305, lng: 127.4589, selected: false },
  { id: '3', lat: 36.3251, lng: 127.4608, selected: false },
];

const center = { lat: 36.3366, lng: 127.459 };

const markers = computed(() =>
  RAW_MARKERS.map((m) => ({ ...m, selected: m.id === selectedId.value })),
);

const selectedId = ref(null);

function onMarkerClick(marker) {
  // console.log('marker-click: ' + marker);
  selectedId.value = selectedId.value === marker.id ? null : marker.id;
}
</script>

<style scoped>
.property-list {
  flex: 1;
}
.map-area {
  height: 250px;
}
.list {
  display: flex;
  gap: 8px;
  padding: 0 16px;
}

.list button {
  padding: 8px 14px;
  border: 1px solid #e9e7e2;
  border-radius: 8px;
  background-color: #ffffff;
}

.list button.on {
  background-color: #fff6dc;
  border-color: #ffbc00;
  font-weight: 700;
}
</style>
