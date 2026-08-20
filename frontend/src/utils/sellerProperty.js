const PYEONG = 3.3058;

/** 매물 상세(PropertyDetailView)의 formatKoreanMoney 와 같은 규칙 */
export function manwon(value) {
  const number = Number(value);
  if (value == null || !Number.isFinite(number)) return '';
  if (number === 0) return '0만원';

  const eok = Math.floor(number / 10000);
  const man = number % 10000;

  if (eok > 0 && man > 0) return `${eok}억 ${man}만원`;
  if (eok > 0) return `${eok}억`;
  return `${man}만원`;
}

/** 매물 상세의 formattedPrice 와 같은 규칙 */
export function priceLabel(item) {
  if (item.tradeType === '월세') {
    const deposit = manwon(item.deposit).replace('만원', '');
    return `월세 ${deposit}/${item.monthlyRent}만원`;
  }
  return `${item.tradeType} ${manwon(item.deposit)}`;
}

/** 매물 상세의 formattedPriceDetail 과 같은 규칙 */
export function transactionPriceLabel(item) {
  if (item.tradeType === '월세') {
    const deposit = manwon(item.deposit).replace('만원', '');
    return `월세 · ${deposit} / ${manwon(item.monthlyRent)}`;
  }
  return `${item.tradeType} · ${manwon(item.deposit)}`;
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
