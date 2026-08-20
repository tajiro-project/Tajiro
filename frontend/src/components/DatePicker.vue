<template>
  <div class="date-field">
    <input
      class="field-input date-text-input"
      type="text"
      inputmode="numeric"
      placeholder="YYYY.MM.DD"
      maxlength="10"
      :value="textValue"
      @input="onTextInput"
    />
    <button class="calendar-btn" type="button" aria-label="달력에서 선택" @click="openCalendar">
      <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
        <rect x="2.5" y="3.5" width="13" height="12" rx="2" stroke="#8a8d8f" stroke-width="1.4" />
        <path d="M2.5 7h13M6 2v3M12 2v3" stroke="#8a8d8f" stroke-width="1.4" stroke-linecap="round" />
      </svg>
    </button>
  </div>

  <BottomSheet v-model="isCalendarOpen" :title="title">
    <div class="calendar">
      <div class="calendar-nav">
        <select v-model.number="viewYear" class="calendar-select" aria-label="연도">
          <option v-for="y in years" :key="y" :value="y">{{ y }}년</option>
        </select>
        <select v-model.number="viewMonth" class="calendar-select" aria-label="월">
          <option v-for="m in 12" :key="m" :value="m - 1">{{ m }}월</option>
        </select>
      </div>

      <div class="calendar-weekdays">
        <span v-for="w in WEEKDAYS" :key="w">{{ w }}</span>
      </div>

      <div class="calendar-grid">
        <button
          v-for="cell in calendarCells"
          :key="cell.key"
          type="button"
          class="calendar-cell"
          :class="{ selected: cell.selected, empty: !cell.date }"
          :disabled="!cell.date"
          @click="selectDay(cell)"
        >
          {{ cell.day }}
        </button>
      </div>
    </div>
  </BottomSheet>
</template>

<script setup>
import { computed, ref, watch } from 'vue';
import BottomSheet from '@/components/BottomSheet.vue';

const props = defineProps({
  modelValue: { type: String, default: '' },
  title: { type: String, default: '날짜 선택' },
});
const emit = defineEmits(['update:modelValue']);

const WEEKDAYS = ['일', '월', '화', '수', '목', '금', '토'];
const CURRENT_YEAR = new Date().getFullYear();
const years = Array.from({ length: 150 }, (_, i) => CURRENT_YEAR - 100 + i);

const textValue = ref(formatDisplay(props.modelValue));
const isCalendarOpen = ref(false);
const viewYear = ref(CURRENT_YEAR);
const viewMonth = ref(new Date().getMonth());

watch(() => props.modelValue, (v) => { textValue.value = formatDisplay(v); });

function pad(n) { return String(n).padStart(2, '0'); }

function formatDisplay(iso) {
  if (!iso) return '';
  const [y, m, d] = iso.split('-');
  if (!y || !m || !d) return '';
  return `${y}.${m}.${d}`;
}

function onTextInput(event) {
  const digits = event.target.value.replace(/\D/g, '').slice(0, 8);
  let formatted = digits;
  if (digits.length > 4) formatted = `${digits.slice(0, 4)}.${digits.slice(4)}`;
  if (digits.length > 6) formatted = `${digits.slice(0, 4)}.${digits.slice(4, 6)}.${digits.slice(6)}`;
  textValue.value = formatted;
  event.target.value = formatted;
  if (digits.length === 8) {
    emit('update:modelValue', `${digits.slice(0, 4)}-${digits.slice(4, 6)}-${digits.slice(6)}`);
    return;
  }
  if (digits.length === 0) emit('update:modelValue', '');
}

function openCalendar() {
  if (props.modelValue) {
    const [y, m] = props.modelValue.split('-');
    viewYear.value = Number(y);
    viewMonth.value = Number(m) - 1;
  }
  isCalendarOpen.value = true;
}

const calendarCells = computed(() => {
  const firstWeekday = new Date(viewYear.value, viewMonth.value, 1).getDay();
  const daysInMonth = new Date(viewYear.value, viewMonth.value + 1, 0).getDate();
  const cells = [];
  for (let i = 0; i < firstWeekday; i++) cells.push({ key: `empty-${i}`, date: null });
  for (let day = 1; day <= daysInMonth; day++) {
    const iso = `${viewYear.value}-${pad(viewMonth.value + 1)}-${pad(day)}`;
    cells.push({ key: iso, date: true, day, iso, selected: iso === props.modelValue });
  }
  return cells;
});

function selectDay(cell) {
  if (!cell.date) return;
  emit('update:modelValue', cell.iso);
  isCalendarOpen.value = false;
}
</script>

<style scoped>
.date-field {
  display: flex;
  align-items: center;
  gap: 8px;
}

.date-text-input {
  flex: 1;
}

.calendar-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 46px;
  height: 46px;
  flex-shrink: 0;
  border: 1px solid var(--border);
  border-radius: var(--radius-input);
  background: var(--white);
}

.calendar {
  padding-bottom: 8px;
}

.calendar-nav {
  display: flex;
  gap: 8px;
  margin-bottom: 14px;
}

.calendar-select {
  flex: 1;
  height: 40px;
  padding: 0 10px;
  border: 1px solid var(--border);
  border-radius: 10px;
  background: var(--white);
  font-size: 13.5px;
  font-weight: 700;
  color: var(--text-primary);
}

.calendar-weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  margin-bottom: 4px;
}

.calendar-weekdays span {
  text-align: center;
  font-size: 11.5px;
  color: var(--kb-silver);
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  row-gap: 4px;
}

.calendar-cell {
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 100px;
  font-size: 13px;
  color: var(--text-primary);
}

.calendar-cell.empty {
  visibility: hidden;
}

.calendar-cell.selected {
  background: var(--kb-yellow);
  font-weight: 700;
}
</style>
