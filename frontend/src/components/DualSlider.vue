<template>
  <div class="dual-slider">
    <div class="track" @click="onTrackClick">
      <div class="fill" :style="fillStyle" />
      <input type="range" :min="min" :max="max" :step="step" :value="modelValue[0]" @input="onLow" />
      <input type="range" :min="min" :max="max" :step="step" :value="modelValue[1]" @input="onHigh" />
    </div>
    <div class="marks">
      <span v-for="(m, i) in marks" :key="i">{{ m }}</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  modelValue: { type: Array, default: () => [0, 100] }, // [low, high]
  min: { type: Number, default: 0 },
  max: { type: Number, default: 100 },
  step: { type: Number, default: 1 },
  marks: { type: Array, default: () => [] }, // 축 라벨 (예: ['최소','1억','2억','3억','4억','최대'])
})
const emit = defineEmits(['update:modelValue'])

const fillStyle = computed(() => {
  const range = props.max - props.min
  const left = ((props.modelValue[0] - props.min) / range) * 100
  const right = ((props.modelValue[1] - props.min) / range) * 100
  return { left: left + '%', width: right - left + '%' }
})

function onLow(e) {
  const minimumGap = Math.min(props.step, props.max - props.min)
  const v = Math.max(
    props.min,
    Math.min(Number(e.target.value), props.modelValue[1] - minimumGap),
  )
  e.target.value = String(v)
  emit('update:modelValue', [v, props.modelValue[1]])
}
function onHigh(e) {
  const minimumGap = Math.min(props.step, props.max - props.min)
  const v = Math.min(
    props.max,
    Math.max(Number(e.target.value), props.modelValue[0] + minimumGap),
  )
  e.target.value = String(v)
  emit('update:modelValue', [props.modelValue[0], v])
}

function onTrackClick(event) {
  if (event.target.tagName === 'INPUT') return

  const rect = event.currentTarget.getBoundingClientRect()
  const ratio = Math.min(
    1,
    Math.max(0, (event.clientX - rect.left) / rect.width),
  )
  const stepCount = Math.round(
    ((props.max - props.min) * ratio) / props.step,
  )
  const clickedValue = Math.min(
    props.max,
    Math.max(props.min, props.min + stepCount * props.step),
  )

  const low = props.modelValue[0]
  const high = props.modelValue[1]
  const minimumGap = Math.min(props.step, props.max - props.min)
  const lowDistance = Math.abs(clickedValue - low)
  const highDistance = Math.abs(clickedValue - high)

  if (lowDistance < highDistance) {
    const nextLow = Math.min(clickedValue, high - minimumGap)
    emit('update:modelValue', [Math.max(props.min, nextLow), high])
    return
  }

  const nextHigh = Math.max(clickedValue, low + minimumGap)
  emit('update:modelValue', [low, Math.min(props.max, nextHigh)])
}
</script>

<style scoped>
.dual-slider {
  width: 100%;
}
.track {
  position: relative;
  height: 26px;
  display: flex;
  align-items: center;
  cursor: pointer;
}
.track::before {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  height: 5px;
  border-radius: 3px;
  background: var(--border);
}
.fill {
  position: absolute;
  height: 5px;
  border-radius: 3px;
  background: var(--kb-yellow);
}
input[type='range'] {
  position: absolute;
  inset: 0;
  width: 100%;
  appearance: none;
  -webkit-appearance: none;
  background: transparent;
  pointer-events: none;
  margin: 0;
}
input[type='range']::-webkit-slider-thumb {
  -webkit-appearance: none;
  pointer-events: auto;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: var(--white);
  border: 2px solid #e2ddd2;
  box-shadow: 0 1px 4px rgba(102, 77, 0, 0.25);
  cursor: pointer;
}
input[type='range']::-moz-range-thumb {
  pointer-events: auto;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: var(--white);
  border: 2px solid #e2ddd2;
  box-shadow: 0 1px 4px rgba(102, 77, 0, 0.25);
  cursor: pointer;
}
.marks {
  display: flex;
  justify-content: space-between;
  margin-top: 6px;
  font-size: 11px;
  color: var(--kb-silver);
}
</style>
