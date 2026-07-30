/* 안전 카테고리 아이콘·색 */

export const SAFETY_COLOR = '#2f9e69';

export const SAFETY_ICON_PATHS = {
  CCTV: '<path d="M3 5.5h6.5v4H3z"/><path d="M9.5 6.5l3.5-1.5v5L9.5 8.5z"/><path d="M6 9.5V13"/>',
  POLICE_CENTER:
    '<path d="M8 2.5l5 2v3.7c0 3-2 5-5 5.8-3-.8-5-2.8-5-5.8V4.5l5-2z"/><path d="M8 6v3.5M6.3 7.7h3.4"/>',
  SAFETY_BELL:
    '<path d="M8 3a3.5 3.5 0 0 0-3.5 3.5V9L3 11h10L11.5 9V6.5A3.5 3.5 0 0 0 8 3z"/><path d="M6.5 11a1.5 1.5 0 0 0 3 0"/>',
  SECURITY_LIGHT:
    '<path d="M8 13V8"/><path d="M4.5 4h7l1.2 4H3.3L4.5 4z"/><path d="M8 4V2.5"/>',
  CHILD_SAFE_ZONE:
    '<circle cx="8" cy="6" r="1.8"/><path d="M5 12.5c0-1.7 1.3-3 3-3s3 1.3 3 3"/><path d="M2.5 8a5.5 5.5 0 0 1 11 0"/>',
  CHILD_GUARD_HOUSE:
    '<path d="M2.5 7.5L8 3l5.5 4.5"/><path d="M4 7v6h8V7"/><circle cx="8" cy="9.8" r="1.2"/>',
};

export function safetyColor() {
  return SAFETY_COLOR;
}

export function safetyIconSvg(category, size = 14) {
  const path = SAFETY_ICON_PATHS[category];
  if (!path) return '';
  return `<svg width="${size}" height="${size}" viewBox="0 0 16 16" fill="none"
    stroke="#fff" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">${path}</svg>`;
}

export const SAFETY_LABELS = {
  CCTV: '방범 CCTV',
  POLICE_CENTER: '지구대',
  SAFETY_BELL: '안전 비상벨',
  SECURITY_LIGHT: '보안등',
  CHILD_SAFE_ZONE: '어린이보호구역',
  CHILD_GUARD_HOUSE: '아동안전지킴이집',
};

export function safetyLabel(category) {
  return SAFETY_LABELS[category] ?? category;
}
