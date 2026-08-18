import { reactive } from 'vue';
import { TOUR_GROUPS } from '@/constants/onboardingSteps';

const SEEN_KEY_PREFIX = 'tajiro:onboarding-tour-seen-groups';

const state = reactive({
  activeGroup: null,
  stepIndex: 0,
});

// JWT는 base64url이라 atob 전에 표준 base64로 바꿔줘야 한다.
function base64UrlDecode(value) {
  const base64 = value.replace(/-/g, '+').replace(/_/g, '/');
  const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=');
  return atob(padded);
}

// "본 화면" 기록을 로그인한 유저 기준으로 나눈다 — 이게 없으면 같은 브라우저에서
// 계정을 바꿔가며 로그인할 때 다른 사람이 이미 본 걸로 취급돼버린다.
function getCurrentUserId() {
  try {
    const token = localStorage.getItem('accessToken');
    if (!token) return 'guest';
    const payload = JSON.parse(base64UrlDecode(token.split('.')[1]));
    return payload.sub ?? 'guest';
  } catch {
    return 'guest';
  }
}

function seenKey() {
  return `${SEEN_KEY_PREFIX}:${getCurrentUserId()}`;
}

function loadSeen() {
  try {
    const raw = localStorage.getItem(seenKey());
    return new Set(raw ? JSON.parse(raw) : []);
  } catch {
    return new Set();
  }
}

function saveSeen(seen) {
  localStorage.setItem(seenKey(), JSON.stringify([...seen]));
}

function hasSeen(group) {
  return loadSeen().has(group);
}

function markSeen(group) {
  const seen = loadSeen();
  seen.add(group);
  saveSeen(seen);
}

// 화면에 들어올 때마다 호출됨(OnboardingSpotlight가 자기 mount 시점에 알아서 호출).
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
  };
}
