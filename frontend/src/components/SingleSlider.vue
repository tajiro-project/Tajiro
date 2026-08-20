<template>
  <div class="single-slider">
    <div class="track">
      <div class="fill" :style="fillStyle" />
      <span
        v-for="(mark, index) in marks"
        :key="`tick-${index}`"
        class="track-mark"
        :style="markPositionStyle(index)"
        aria-hidden="true"
      />
      <input
        type="range"
        :min="min"
        :max="max"
        :step="step"
        :value="modelValue"
        :aria-label="ariaLabel"
        @input="onInput"
      />
    </div>
    <div v-if="marks.length" class="marks" aria-hidden="true">
      <span
        v-for="(mark, index) in marks"
        :key="index"
        class="mark-label"
        :style="markLabelStyle(index)"
      >
        {{ mark }}
      </span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  modelValue: { type: Number, default: 0 },
  min: { type: Number, default: 0 },
  max: { type: Number, default: 100 },
  step: { type: Number, default: 1 },
  marks: { type: Array, default: () => [] },
  ariaLabel: { type: String, default: '값 선택' },
});

const emit = defineEmits(['update:modelValue']);

const fillStyle = computed(() => {
  const range = props.max - props.min;
  const value = Math.min(props.max, Math.max(props.min, props.modelValue));
  const width = range > 0 ? ((value - props.min) / range) * 100 : 0;
  return { width: `${width}%` };
});

function onInput(event) {
  emit('update:modelValue', Number(event.target.value));
}

function markPositionStyle(index) {
  const denominator = Math.max(props.marks.length - 1, 1);
  return { left: `${(index / denominator) * 100}%` };
}

function markLabelStyle(index) {
  const style = markPositionStyle(index);
  const lastIndex = props.marks.length - 1;
  const translate = index === 0 ? '0' : index === lastIndex ? '-100%' : '-50%';
  return { ...style, transform: `translateX(${translate})` };
}
</script>

<style scoped>
.single-slider {
  width: 100%;
}

.track {
  position: relative;
  display: flex;
  align-items: center;
  height: 26px;
}

.track::before {
  position: absolute;
  right: 0;
  left: 0;
  height: 5px;
  border-radius: 3px;
  background: var(--border);
  content: '';
}

.fill {
  position: absolute;
  left: 0;
  height: 5px;
  border-radius: 3px;
  background: var(--kb-yellow);
}

.track-mark {
  position: absolute;
  z-index: 1;
  width: 2px;
  height: 9px;
  border-radius: 1px;
  background: #c8c3b8;
  transform: translate(-50%);
  pointer-events: none;
}

input[type='range'] {
  position: absolute;
  inset: 0;
  width: 100%;
  margin: 0;
  appearance: none;
  -webkit-appearance: none;
  background: transparent;
  cursor: pointer;
  z-index: 2;
}

input[type='range']::-webkit-slider-runnable-track {
  height: 5px;
  background: transparent;
}

input[type='range']::-webkit-slider-thumb {
  width: 22px;
  height: 22px;
  margin-top: -8.5px;
  appearance: none;
  -webkit-appearance: none;
  border: 2px solid #e2ddd2;
  border-radius: 50%;
  background: var(--white);
  box-shadow: 0 1px 4px rgba(102, 77, 0, 0.25);
}

input[type='range']::-moz-range-track {
  height: 5px;
  background: transparent;
}

input[type='range']::-moz-range-thumb {
  width: 20px;
  height: 20px;
  border: 2px solid #e2ddd2;
  border-radius: 50%;
  background: var(--white);
  box-shadow: 0 1px 4px rgba(102, 77, 0, 0.25);
}

input[type='range']:focus-visible::-webkit-slider-thumb {
  outline: 2px solid var(--kb-gray);
  outline-offset: 2px;
}

input[type='range']:focus-visible::-moz-range-thumb {
  outline: 2px solid var(--kb-gray);
  outline-offset: 2px;
}

.marks {
  position: relative;
  height: 14px;
  margin-top: 6px;
  color: var(--kb-silver);
  font-size: 11px;
}

.mark-label {
  position: absolute;
  top: 0;
  white-space: nowrap;
}
</style>
