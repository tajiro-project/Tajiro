const KAKAO_API_KEY = import.meta.env.VITE_KAKAO_MAP_KEY;
let sdkPromise = null;

export function loadKakaoSdk() {
  if (sdkPromise) return sdkPromise;

  sdkPromise = new Promise((resolve, reject) => {
    if (!KAKAO_API_KEY) {
      reject(new Error('VITE_KAKAO_MAP_KEY가 없습니다. .env를 확인하세요.'));
      return;
    }
    // 스크립트가 이미 붙어 있으면 load만 호출한다
    if (window.kakao?.maps) {
      window.kakao.maps.load(resolve);
      return;
    }
    const script = document.createElement('script');
    script.src = `https://dapi.kakao.com/v2/maps/sdk.js?appkey=${KAKAO_API_KEY}&autoload=false&libraries=services`;
    script.onload = () => window.kakao.maps.load(resolve);
    script.onerror = () =>
      reject(new Error('카카오 SDK 로드 실패 — 키·도메인 등록을 확인하세요.'));
    document.head.appendChild(script);
  });

  return sdkPromise;
}
