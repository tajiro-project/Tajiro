const PYEONG = 3.3058;

export function manwon(value) {
  const number = Number(value);
  if (!Number.isFinite(number)) return '-';

  const eok = Math.floor(number / 10000);
  const rest = number % 10000;

  if (eok === 0) return `${rest.toLocaleString('ko-KR')}`;
  if (rest === 0) return `${eok}억`;
  return `${eok}억 ${rest.toLocaleString('ko-KR')}`;
}

export function priceLabel(item) {
  return item.tradeType === '월세'
    ? `월세 ${manwon(item.deposit)}/${manwon(item.monthlyRent)}`
    : `${item.tradeType} ${manwon(item.deposit)}`;
}

export function statusLabel(item) {
  return item.transactionStatus ? '게시중' : '거래완료';
}

export function titleOf(item) {
  if (item.title) return item.title;

  const parts = [item.buildingName, item.dong, item.ho].filter(Boolean);
  return parts.length ? parts.join(' ') : (item.address ?? '');
}

export function typeLabel(item) {
  return [item.propertyType, item.buildingName].filter(Boolean).join(' · ');
}

export function floorLabel(floorInfo) {
  if (!floorInfo) return '';

  const head = String(floorInfo).split('/')[0].trim();
  return /^\d+$/.test(head) ? `${head}층` : head;
}

export function subMeta(item) {
  const parts = [];

  if (item.areaM2) parts.push(`${Math.floor(item.areaM2 / PYEONG)}평`);

  const floor = floorLabel(item.floorInfo);
  if (floor) parts.push(floor);

  parts.push(`찜 ${item.favoriteCount ?? 0}`);

  return parts.join(' · ');
}
