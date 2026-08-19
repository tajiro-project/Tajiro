<template>
  <Teleport to="body">
    <Transition name="tour-pop">
      <div v-if="visible && rect" class="tour-root">
        <!-- 강조 영역을 탭하면 다음으로 진행 — 강조된 걸 눌러보는 게 자연스러운 반응이라
             아무 반응 없으면 고장난 것처럼 느껴진다. -->
        <div
          class="tour-dim"
          :style="dimStyle"
          role="button"
          :aria-label="`${currentStep?.title} 설명 다음으로`"
          @click="handleNext"
        />
        <div class="tour-tooltip" :style="tooltipStyle">
          <p class="tour-step-count">{{ state.stepIndex + 1 }} / {{ steps.length }}</p>
          <p class="tour-title">{{ currentStep?.title }}</p>
          <p class="tour-text">{{ currentStep?.text }}</p>
          <div class="tour-actions">
            <button class="tour-skip" type="button" @click="skip">건너뛰기</button>
            <button class="tour-next" type="button" @click="handleNext">
              {{ isLastStep ? '완료' : '다음' }}
            </button>
          </div>
        </div>
      </div>
    </Transition>

    <Transition name="tour-pop">
      <div v-if="showReturnPrompt" class="return-overlay">
        <div class="return-card">
          <p class="return-title">예시 화면을 다 둘러봤어요</p>
          <p class="return-text">이 화면을 더 둘러보시겠어요, 마이페이지로 돌아가시겠어요?</p>
          <div class="return-actions">
            <button class="return-stay" type="button" @click="stayHere">
              더 둘러보기
            </button>
            <button class="return-go" type="button" @click="goReturnTo">
              마이페이지로
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onDeactivated, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useOnboardingTour } from '@/composables/useOnboardingTour';

const props = defineProps({
  groupName: { type: String, required: true },
  steps: { type: Array, required: true },
  // 마이페이지 "다시보기"(?demo=1)로 들어왔을 때, 투어가 끝나면 돌아갈지 물어볼 경로.
  // 실제 첫 방문 때는(demo 쿼리 없음) 적용 안 됨 — 그때 물어보면 오히려 방해가 된다.
  returnTo: { type: String, default: null },
});

const route = useRoute();
const router = useRouter();
const { state, maybeStart, startAt, next: advance, skip } = useOnboardingTour();

const visible = computed(() => state.activeGroup === props.groupName);
const currentStep = computed(() => props.steps[state.stepIndex]);
const isLastStep = computed(() => state.stepIndex === props.steps.length - 1);

const showReturnPrompt = ref(false);

watch(visible, (isVisible, wasVisible) => {
  if (!wasVisible || isVisible) return;

  // 투어 마지막 스텝이 화면 아래쪽에 있으면 scrollIntoView로 그쪽까지 내려가 있는 채로
  // 끝나버린다 — 투어가 끝나면 원래 보던 위치(맨 위)로 되돌려준다.
  document.querySelectorAll('.simplebar-content-wrapper').forEach((el) => {
    el.scrollTo({ top: 0, behavior: 'smooth' });
  });

  if (props.returnTo && route.query.demo === '1') {
    showReturnPrompt.value = true;
  }
});

function stayHere() {
  showReturnPrompt.value = false;
}

function goReturnTo() {
  showReturnPrompt.value = false;
  router.push(props.returnTo);
}

const PAD = 8;
const rect = ref(null);

function measure() {
  if (!visible.value || !currentStep.value) {
    rect.value = null;
    return;
  }
  const el = document.querySelector(`[data-tour="${currentStep.value.target}"]`);
  if (!el) {
    rect.value = null;
    return;
  }
  const r = el.getBoundingClientRect();
  const offscreen = r.top < 0 || r.bottom > window.innerHeight;
  if (offscreen) {
    el.scrollIntoView({ block: 'center', behavior: 'auto' });
    nextTick(() => {
      rect.value = el.getBoundingClientRect();
    });
    return;
  }
  rect.value = r;
}

const dimStyle = computed(() => {
  if (!rect.value) return {};
  return {
    top: `${rect.value.top - PAD}px`,
    left: `${rect.value.left - PAD}px`,
    width: `${rect.value.width + PAD * 2}px`,
    height: `${rect.value.height + PAD * 2}px`,
  };
});

const TOOLTIP_HEIGHT = 160;

const tooltipStyle = computed(() => {
  if (!rect.value) return {};
  const spaceBelow = window.innerHeight - rect.value.bottom;
  const placeBelow = spaceBelow > 190;
  const rawTop = placeBelow
    ? rect.value.bottom + PAD + 10
    : rect.value.top - PAD - 10 - TOOLTIP_HEIGHT;
  const top = Math.min(
    Math.max(12, rawTop),
    window.innerHeight - TOOLTIP_HEIGHT - 12,
  );
  const left = Math.min(Math.max(12, rect.value.left), window.innerWidth - 288);
  return { top: `${top}px`, left: `${left}px` };
});

function handleNext() {
  advance(props.steps.length);
  nextTick(measure);
}

function onWindowChange() {
  measure();
}

// 다음 스텝의 대상 요소가 아직 화면에 없을 수 있음(예: 마법사 다음 단계로 실제 이동해야 나타나는 경우).
// 그럴 땐 오버레이를 띄우지 않고 기다리다가, 요소가 실제로 나타나는 순간 자동으로 잡아서 보여준다.
let observer = null;

function watchForTarget() {
  observer?.disconnect();
  observer = null;
  if (!visible.value) return;

  // 초기 매칭 이후에도 화면 레이아웃이 계속 바뀔 수 있어서(비동기 데이터 로딩 등)
  // rect가 이미 있어도 계속 재측정한다 — 위치가 안 바뀌었으면 재측정 비용은 미미하다.
  observer = new MutationObserver(measure);
  observer.observe(document.body, { childList: true, subtree: true });
}

watch(
  () => [state.activeGroup, state.stepIndex],
  () => {
    nextTick(measure);
    watchForTarget();
  },
);

onMounted(() => {
  // ?demo=1로 들어온 화면은 "다시보기" 흐름 중이라는 뜻이라, 이 그룹을 예전에 이미 봤어도
  // (hasSeen) 다시 보여준다. 예: 매물리스트 다시보기에서 예시 카드를 눌러 매물상세로 넘어가도
  // 그 화면의 투어를 이어서 볼 수 있어야 한다.
  if (route.query.demo === '1') {
    startAt(props.groupName);
  } else {
    maybeStart(props.groupName);
  }
  measure();
  watchForTarget();
  window.addEventListener('resize', onWindowChange);
  window.addEventListener('scroll', onWindowChange, true);
});

// 투어를 끝내지 않은 채 화면을 벗어나면(예: 가치관 미설정 상태로 매물 리스트에 들어왔다가
// 404로 가치관 설정 화면으로 튕겨나가는 경우) state.activeGroup이 이 그룹으로 남아있게 되고,
// 그러면 다음 화면의 maybeStart가 "이미 다른 투어가 진행 중"이라고 판단해 조용히 무시해버린다.
// PropertyListView처럼 KeepAlive로 감싸인 화면은 onBeforeUnmount가 아니라 onDeactivated로 벗어나므로
// 두 훅 모두에서 정리한다.
function resetIfStillActive() {
  if (state.activeGroup === props.groupName) {
    state.activeGroup = null;
    state.stepIndex = 0;
  }
}

onDeactivated(resetIfStillActive);

onBeforeUnmount(() => {
  resetIfStillActive();
  observer?.disconnect();
  window.removeEventListener('resize', onWindowChange);
  window.removeEventListener('scroll', onWindowChange, true);
});
</script>

<style scoped>
.tour-root {
  position: fixed;
  inset: 0;
  z-index: 300;
  pointer-events: auto;
}

.tour-dim {
  position: fixed;
  border-radius: 14px;
  box-shadow: 0 0 0 9999px rgba(20, 16, 8, 0.62);
  border: 2px solid var(--kb-yellow, #ffbc00);
  transition:
    top 0.25s ease,
    left 0.25s ease,
    width 0.25s ease,
    height 0.25s ease;
  cursor: pointer;
}

.tour-tooltip {
  position: fixed;
  width: 264px;
  max-width: calc(100vw - 24px);
  padding: 16px;
  background: var(--white, #fff);
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.22);
  pointer-events: auto;
  transition:
    top 0.25s ease,
    left 0.25s ease;
}

.tour-step-count {
  font-size: 10.5px;
  font-weight: 700;
  color: var(--kb-yellow, #ffbc00);
  letter-spacing: 0.3px;
}

.tour-title {
  margin-top: 4px;
  font-size: 14.5px;
  font-weight: 800;
  color: var(--text-primary, #33302a);
}

.tour-text {
  margin-top: 6px;
  font-size: 12.5px;
  line-height: 1.55;
  color: var(--kb-gray, #60584c);
}

.tour-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-top: 14px;
}

.tour-skip {
  font-size: 12px;
  color: var(--kb-silver, #8a8d8f);
}

.tour-next {
  flex-shrink: 0;
  padding: 9px 18px;
  border-radius: 100px;
  background: var(--kb-yellow, #ffbc00);
  color: var(--text-primary, #33302a);
  font-size: 12.5px;
  font-weight: 800;
}

.tour-pop-enter-active {
  transition: opacity 0.18s ease;
}

.tour-pop-enter-active .tour-tooltip {
  transition:
    opacity 0.18s ease,
    transform 0.18s ease;
}

.tour-pop-enter-from {
  opacity: 0;
}

.tour-pop-enter-from .tour-tooltip {
  opacity: 0;
  transform: translateY(6px) scale(0.98);
}

.tour-pop-leave-active {
  transition: opacity 0.12s ease;
}

.tour-pop-leave-to {
  opacity: 0;
}

.return-overlay {
  position: fixed;
  inset: 0;
  z-index: 320;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(20, 16, 8, 0.5);
}

.return-card {
  width: 100%;
  max-width: 300px;
  padding: 22px 20px 20px;
  background: var(--white, #fff);
  border-radius: 20px;
  text-align: center;
}

.return-title {
  font-size: 15.5px;
  font-weight: 800;
  color: var(--text-primary, #33302a);
}

.return-text {
  margin-top: 8px;
  font-size: 12.5px;
  line-height: 1.55;
  color: var(--kb-gray, #60584c);
}

.return-actions {
  display: flex;
  gap: 8px;
  margin-top: 18px;
}

.return-stay {
  flex: 1;
  padding: 12px;
  border: 1px solid var(--border, #e9e7e2);
  border-radius: 12px;
  background: var(--white, #fff);
  font-size: 13px;
  font-weight: 700;
  color: var(--kb-gray, #60584c);
}

.return-go {
  flex: 1;
  padding: 12px;
  border-radius: 12px;
  background: var(--kb-yellow, #ffbc00);
  font-size: 13px;
  font-weight: 800;
  color: var(--text-primary, #33302a);
}
</style>
