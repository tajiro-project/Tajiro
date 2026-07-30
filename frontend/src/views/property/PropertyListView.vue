<template>
  <div class="property-list">
    <div class="map-area">
      <Kakaomap :markers="markers" :center="center" />
    </div>
  </div>
</template>

<script setup>
import Kakaomap from '@/components/kakaomap.vue';

const markers = [
  { id: 'P01', lat: 36.3272, lng: 127.4541, selected: true },
  { id: 'P02', lat: 36.3305, lng: 127.4589, selected: false },
  { id: 'P03', lat: 36.3251, lng: 127.4608, selected: false },
];

const center = { lat: 36.3366, lng: 127.459 };
</script>

<style scoped>
.property-list {
  flex: 1;
}
.map-area {
  height: 250px;
}
</style>
