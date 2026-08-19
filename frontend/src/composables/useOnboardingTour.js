import { reactive } from 'vue';
import { TOUR_GROUPS } from '@/constants/onboardingSteps';
import client from '@/api/client';

const state = reactive({
  activeGroup: null,
  stepIndex: 0,
});

// 서버(users.onboarding_seen)에서 받아온 비트열을 세션 동안 메모리에 들고 있는다.
// localStorage가 아니라 서버가 기준이라, 캐시를 지워도/다른 기기에서 로그인해도 유지된다.
let seenMask = '0'.repeat(TOUR_GROUPS.length);
let synced = false;

function setSeenMask(mask) {
  if (typeof mask === 'string' && mask.length === TOUR_GROUPS.length) {
    seenMask = mask;
  }
  synced = true;
}

// 페이지 새로고침 등으로 로그인 응답을 거치지 않고 세션이 이어질 때, 서버에서 현재 값을 한 번 받아온다.
// 로그인 직후에는 setSeenMask가 이미 synced를 true로 만들어두므로 여기서 다시 호출하지 않는다.
async function syncFromServer() {
  if (synced) return;
  synced = true;
  try {
    const res = await client.get('/users/me/onboarding-tour');
    const data = res.data?.data ?? res.data;
    if (typeof data?.onboardingSeen === 'string' && data.onboardingSeen.length === TOUR_GROUPS.length) {
      seenMask = data.onboardingSeen;
    }
  } catch {
    // 실패하면 기본값(전부 안 봄)으로 남는다 — 온보딩이 다시 뜨는 정도라 안전한 폴백.
  }
}

function hasSeen(group) {
  const index = TOUR_GROUPS.indexOf(group);
  return index !== -1 && seenMask[index] === '1';
}

// 서버에 "이 그룹 봤음"을 저장한다. 실패해도 로컬 표시(seenMask)는 유지 — 다음 로그인 때 서버 값
// 기준으로 다시 맞춰진다. "다시보기"(startAt)는 이 함수를 호출하지 않는다 — 이미 본 걸 다시 보여주는
// 것뿐이라 서버에 새로 쓸 내용이 없다.
async function markSeen(group) {
  const index = TOUR_GROUPS.indexOf(group);
  if (index === -1 || seenMask[index] === '1') return;

  seenMask = seenMask.slice(0, index) + '1' + seenMask.slice(index + 1);
  try {
    await client.patch('/users/me/onboarding-tour', { group });
  } catch {
    // 무시 — 위 주석 참고.
  }
}

// 화면에 들어올 때마다 호출됨(OnboardingSpotlight가 자기 mount/activate 시점에 알아서 호출).
// 아직 안 본 화면이고, 지금 다른 투어가 진행 중이 아닐 때만 자동으로 시작함.
// 탭을 어떤 순서로 눌러도 각 화면이 독립적으로 자기 차례를 스스로 챙긴다.
function maybeStart(group) {
  if (state.activeGroup !== null) return;
  if (hasSeen(group)) return;
  state.activeGroup = group;
  state.stepIndex = 0;
}

// 마이페이지 "다시보기"처럼 이미 본 화면이라도 강제로 다시 보여줄 때 사용.
function startAt(group) {
  if (!TOUR_GROUPS.includes(group)) return;
  state.activeGroup = group;
  state.stepIndex = 0;
}

function finish() {
  if (state.activeGroup) markSeen(state.activeGroup);
  state.activeGroup = null;
  state.stepIndex = 0;
}

function skip() {
  finish();
}

function next(stepsLength) {
  if (state.stepIndex + 1 < stepsLength) {
    state.stepIndex += 1;
  } else {
    finish();
  }
}

function isCurrentGroup(group) {
  return state.activeGroup === group;
}

export function useOnboardingTour() {
  return {
    state,
    hasSeen,
    maybeStart,
    startAt,
    skip,
    next,
    isCurrentGroup,
    setSeenMask,
    syncFromServer,
  };
}
