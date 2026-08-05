<template>
  <div class="infra-panel" :class="{ collapsed }">
    <button class="ip-head" @click="collapsed = !collapsed">
      <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
        <path
          d="M7 1.5l5.5 3L7 7.5l-5.5-3L7 1.5z"
          stroke="#545045"
          stroke-width="1.2"
          stroke-linejoin="round"
        />
        <path
          d="M1.5 7.5L7 10.5l5.5-3M1.5 10.5L7 13.5l5.5-3"
          stroke="#545045"
          stroke-width="1.2"
          stroke-linejoin="round"
        />
      </svg>
      <span class="ip-title">지도에 표시</span>
      <span v-if="collapsed" class="ip-count">{{ modelValue.length }}</span>
      <svg
        class="ip-chevron"
        width="12"
        height="12"
        viewBox="0 0 12 12"
        fill="none"
      >
        <path
          d="M2.5 7.5L6 4l3.5 3.5"
          stroke="#8a8d8f"
          stroke-width="1.4"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
      </svg>
    </button>

    <template v-if="!collapsed">
      <div v-for="layer in layers" :key="layer.key" class="ip-row">
        <span class="ip-dot" :style="{ background: layer.color }" />
        <span class="ip-label" :class="{ dim: !isOn(layer.key) }">
          {{ layer.label }}
        </span>
        <button
          class="ip-toggle"
          :class="{ on: isOn(layer.key) }"
          role="switch"
          :aria-checked="isOn(layer.key)"
          @click="toggle(layer.key)"
        >
          <span class="knob" />
        </button>
      </div>

      <p v-if="!layers.length" class="ip-empty">표시할 카테고리가 없습니다</p>
    </template>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue';
import { infraColor } from '@/constants/infraIcons';
import {
  INFRA_CATEGORIES,
  AMENITY_CATEGORIES,
} from '@/constants/preferenceOptions';

// 지도 위 인프라·편의시설 레이어 토글
const props = defineProps({
  modelValue: { type: Array, required: true },
  categories: { type: Array, required: true },
});
const emit = defineEmits(['update:modelValue']);

const collapsed = ref(true);

const ALL_CATEGORIES = [...INFRA_CATEGORIES, ...AMENITY_CATEGORIES];

const layers = computed(() =>
  props.categories.map((key) => ({
    key,
    label: ALL_CATEGORIES.find((c) => c.key === key)?.label ?? key,
    color: infraColor(key),
  })),
);

const isOn = (key) => props.modelValue.includes(key);

function toggle(key) {
  emit(
    'update:modelValue',
    isOn(key)
      ? props.modelValue.filter((k) => k !== key)
      : [...props.modelValue, key],
  );
}
</script>

<style scoped>
.infra-panel {
  width: 168px;
  background: var(--white);
  border-radius: 12px;
  box-shadow: 0 4px 14px rgba(51, 48, 42, 0.18);
  padding: 10px 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.ip-head {
  display: flex;
  align-items: center;
  gap: 6px;
  padding-bottom: 2px;
  border: none;
  background: none;
  cursor: pointer;
}

.infra-panel.collapsed .ip-head {
  padding-bottom: 0;
}

.ip-title {
  flex: 1;
  text-align: left;
  font-size: 12.5px;
  font-weight: 800;
  color: #33302a;
  white-space: nowrap;
}

.ip-count {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  flex-shrink: 0;
  border-radius: 9px;
  background: var(--kb-yellow);
  font-size: 11px;
  font-weight: 800;
  color: #33302a;
}

.ip-chevron {
  flex-shrink: 0;
  transition: transform 0.15s;
}

.infra-panel.collapsed .ip-chevron {
  transform: rotate(180deg);
}

.ip-row {
  display: flex;
  align-items: center;
  gap: 7px;
}

.ip-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.ip-label {
  flex: 1;
  font-size: 12px;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.ip-label.dim {
  color: var(--kb-silver);
}

.ip-toggle {
  position: relative;
  width: 34px;
  height: 18px;
  flex-shrink: 0;
  border: none;
  border-radius: 10px;
  background: #d8d5cf;
  transition: background 0.15s;
  cursor: pointer;
}

.ip-toggle .knob {
  position: absolute;
  top: 2px;
  left: 2px;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: var(--white);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  transition: left 0.15s;
}

.ip-toggle.on {
  background: var(--kb-yellow);
}

.ip-toggle.on .knob {
  left: 18px;
}

.ip-empty {
  font-size: 11.5px;
  color: var(--kb-silver);
}
</style>
