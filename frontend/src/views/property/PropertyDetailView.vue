<template>
  <div class="pdetail" v-if="p">
    <PageHeader title="매물 상세" />

    <div class="scroll-area">
      <!-- 1. 사진 캐러셀 -->
      <div
        class="photo-slider"
        @touchstart="handleTouchStart"
        @touchend="handleTouchEnd"
      >
        <template v-if="p.images && p.images.length > 0">
          <img
            :src="p.images[currentImgIndex]"
            :alt="`매물 이미지 ${currentImgIndex + 1}`"
            class="photo-img"
          />
          <!-- 좌우 화살표 버튼 -->
          <button
            class="slide-btn left"
            @click="prevImage"
            v-if="p.images.length > 1"
          >
            <ChevronLeft :size="20" />
          </button>
          <button
            class="slide-btn right"
            @click="nextImage"
            v-if="p.images.length > 1"
          >
            <ChevronRight :size="20" />
          </button>

          <!-- 슬라이드 인디케이터 (Dots) -->
          <div class="dots" v-if="p.images.length > 1">
            <span
              v-for="(_, idx) in p.images"
              :key="idx"
              class="d"
              :class="{ on: idx === currentImgIndex }"
              @click="currentImgIndex = idx"
            />
          </div>
        </template>

        <!-- 이미지가 없을 때 예비 화면 -->
        <div v-else class="photo-placeholder">
          <ImageIcon :size="44" color="#8a8477" />
          <p>등록된 이미지가 없습니다.</p>
        </div>
      </div>

      <!-- 2. 가격/요약 -->
      <div class="head">
        <h1 class="price">{{ formattedPrice }}</h1>
        <p class="addr">{{ regionLine }}</p>
      </div>

      <!-- 내 기준 점수 & 시세 문구 통합 카드 -->
      <div class="score-card">
        <div class="score-text-group">
          <p class="score-line">
            매물에 대한 <strong>{{ profileName }}</strong
            >님의 주거 가치관 반영 점수는
            <span class="highlight">{{ p.evaluationScore ?? 0 }}점</span>
            입니다!
          </p>
          <p class="score-line sub">
            비슷한 매물에 대한 매물가 평균은
            <strong>{{ medianPrice }}만원</strong>입니다.
          </p>
        </div>
      </div>

      <!-- 3. 매물 정보 -->
      <section class="card">
        <p class="card-head">매물 정보</p>
        <div class="tag-row">
          <span class="tag">{{ p.propertyType }}</span>
          <span class="tag yellow">{{ p.tradeType }}</span>
        </div>
        <dl class="info-list">
          <div class="info-row">
            <dt>건물명</dt>
            <dd>{{ buildingLine }}</dd>
          </div>
          <div class="info-row">
            <dt>거래 · 가격</dt>
            <dd>{{ formattedPriceDetail }}</dd>
          </div>
          <div class="info-row">
            <dt>관리비</dt>
            <dd>월 {{ p.maintenanceFee ?? 0 }}만원</dd>
          </div>
          <div class="info-row">
            <dt>도로명 주소</dt>
            <dd>{{ p.address }}</dd>
          </div>
          <div class="info-row">
            <dt>층수</dt>
            <dd>{{ formattedFloor }}</dd>
          </div>
          <div class="info-row">
            <dt>면적</dt>
            <dd>{{ p.areaM2 }}㎡ (약 {{ pyeong }}평)</dd>
          </div>
          <div class="info-row">
            <dt>방 · 욕실</dt>
            <dd>{{ p.roomNum ?? 0 }}개 · {{ p.bathroomNum ?? 0 }}개</dd>
          </div>
          <div class="info-row">
            <dt>주차</dt>
            <dd>{{ p.parkAvailability ? "가능" : "불가" }}</dd>
          </div>
          <div class="info-row">
            <dt>입주 가능일</dt>
            <dd>{{ moveInLine }}</dd>
          </div>
          <div class="info-row">
            <dt>사용 승인일</dt>
            <dd>{{ availableLine ?? "정보 없음" }}</dd>
          </div>
        </dl>

        <hr class="section-divider" />

        <p class="desc-head">매물 상세 설명</p>
        <p class="desc">{{ p.propertyDescription }}</p>
      </section>

      <!-- 요약 미니카드 -->
      <div class="mini-row">
        <div class="mini-card">
          <p class="mini-label">
            <Receipt :size="14" color="#8a8d8f" />
            관리비
          </p>
          <p class="mini-value">월 {{ p.maintenanceFee ?? 0 }}만원</p>
        </div>
        <div class="mini-card">
          <p class="mini-label">
            <Clock :size="14" color="#8a8d8f" />
            통근
          </p>
          <p class="mini-value">{{ p.commuteTime ?? "정보 없음" }}</p>
        </div>
      </div>

      <div class="mini-card wide">
        <p class="mini-label">
          <Building2 :size="14" color="#8a8d8f" />
          인프라
        </p>
        <p class="mini-value">
          <template v-if="formattedInfraSummary.length > 0">
            <span
              v-for="(item, idx) in formattedInfraSummary"
              :key="item.category"
            >
              {{ item.name }} {{ item.count }}
              <template v-if="idx < formattedInfraSummary.length - 1">
                ·
              </template>
            </span>
          </template>
          <template v-else>
            <span>주변 인프라 정보가 없습니다.</span>
          </template>
        </p>
      </div>

      <!-- 단순 이동 바 -->
      <div class="banner-group">
        <button
          class="simple-banner yellow"
          @click="$router.push(`/properties/${p.id}/infra`)"
        >
          <span>가장 가까운 인프라 보기</span>
          <span class="arrow">→</span>
        </button>

        <button
          class="simple-banner green"
          @click="$router.push(`/properties/${p.id}/safety`)"
        >
          <span>안전 정보 보기</span>
          <span class="arrow">→</span>
        </button>

        <button
          class="simple-banner brown"
          @click="$router.push('/safety-guide')"
        >
          <span>안전 거래 가이드 보기</span>
          <span class="arrow">→</span>
        </button>
      </div>

      <!-- 공인중개사 정보 -->
      <section class="card">
        <p class="card-head">이 매물, 어디에 문의할까요?</p>
        <p class="card-sub">이 매물을 등록·관리하는 인근 공인중개사예요</p>
        <div class="realtor-card">
          <div class="realtor-head">
            <span class="realtor-icon">
              <Building2 :size="18" color="#a8842c" />
            </span>
            <p class="realtor-name">
              {{ p.realtorPreview?.name ?? "KB부동산공인중개사" }}
            </p>
          </div>
          <p class="realtor-addr">
            <MapPin :size="12" color="#8a8d8f" />
            {{
              p.address
                ? p.address.split(" ")[0] + " " + p.address.split(" ")[1]
                : ""
            }}
            · 매물 인근
          </p>
          <div class="realtor-actions">
            <button class="rt-btn outline">
              <Phone :size="14" />
              전화
            </button>
            <button class="rt-btn yellow">
              <MessageSquare :size="14" />
              채팅 문의
            </button>
          </div>
        </div>
        <p class="kb-note">
          <Info :size="13" color="#8a8d8f" />
          KB 인증 중개사예요. 계약 전 등록번호를 꼭 확인하세요.
        </p>
      </section>

      <!-- 혜택/상품 -->
      <section class="card">
        <div class="benefit-head">
          <p class="card-head">이 매물로 받을 수 있는 혜택/상품</p>
          <button class="see-all" @click="$router.push('/benefits/policies')">
            전체보기 →
          </button>
        </div>
        <div class="benefit-list">
          <div v-for="b in benefits" :key="b.id" class="benefit-item">
            <span class="b-texts">
              <span class="b-title">{{ b.title }}</span>
              <span class="b-sub">{{ b.sub }}</span>
            </span>
          </div>
        </div>
      </section>
    </div>

    <!-- 4. 하단 액션 바 -->
    <div class="bottom-actions-wrap">
      <div class="bottom-actions">
        <button
          class="fav-btn"
          :class="{ on: isFavorite }"
          aria-label="찜"
          @click="toggleFavorite"
        >
          <Heart
            :size="20"
            :fill="isFavorite ? '#ffbc00' : 'none'"
            :color="isFavorite ? '#ffbc00' : '#8a8d8f'"
          />
        </button>
        <button class="compare-btn" @click="addToCompare">비교함 담기</button>
      </div>
      <p v-if="compareMsg" class="compare-msg">{{ compareMsg }}</p>
    </div>

    <AppTabBar active="home" />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { useRoute } from "vue-router";
import PageHeader from "@/components/PageHeader.vue";
import AppTabBar from "@/components/AppTabBar.vue";
import { propertyApi, favoriteApi } from "@/api/services";
import { useCompareStore } from "@/stores/compare";

import {
  Image as ImageIcon,
  ChevronLeft,
  ChevronRight,
  Receipt,
  Clock,
  Building2,
  MapPin,
  Phone,
  MessageSquare,
  Info,
  Heart,
} from "lucide-vue-next";

const route = useRoute();
const compareStore = useCompareStore();
const compareMsg = ref("");

const p = ref(null);
const infras = ref([]);
const market = ref(null);
const isFavorite = ref(false);
const profileName = ref("홍길동");

const id = route.params.id;

// 이미지 캐러셀 상태
const currentImgIndex = ref(0);
let touchStartX = 0;

function nextImage() {
  if (!p.value?.images?.length) return;
  currentImgIndex.value = (currentImgIndex.value + 1) % p.value.images.length;
}

function prevImage() {
  if (!p.value?.images?.length) return;
  currentImgIndex.value =
    (currentImgIndex.value - 1 + p.value.images.length) % p.value.images.length;
}

function handleTouchStart(e) {
  touchStartX = e.touches[0].clientX;
}

function handleTouchEnd(e) {
  const touchEndX = e.changedTouches[0].clientX;
  const diff = touchStartX - touchEndX;
  if (Math.abs(diff) > 40) {
    if (diff > 0) nextImage();
    else prevImage();
  }
}

// API 연동 데이터 로드
onMounted(async () => {
  if (!id) return;

  try {
    // 매물 상세 정보 조회
    const res = await propertyApi.getPropertyDetail(id);

    const detailData = res?.data?.data ?? res?.data ?? res;

    if (detailData) {
      if (!detailData.images || detailData.images.length === 0) {
        detailData.images = [
          "https://via.placeholder.com/600x400/f5efdb/8a8477?text=Room+Image+1",
        ];
      }

      p.value = detailData;
      isFavorite.value = detailData?.isFavorite ?? false;
    }

    if (propertyApi.infrastructures) {
      const infraRes = await propertyApi.infrastructures(id);
      infras.value = infraRes?.data?.data ?? infraRes?.data ?? infraRes ?? [];
    }

    if (propertyApi.marketEvaluation) {
      const marketRes = await propertyApi.marketEvaluation(id);
      market.value = marketRes?.data?.data ?? marketRes?.data ?? marketRes;
    }
  } catch (error) {
    console.error("매물 상세 정보를 불러오는 데 실패했습니다:", error);
  }
});

// 전체 인프라 카테고리 이름 매핑
const CATEGORY_NAMES = {
  CAFE: "카페",
  GYM: "헬스장",
  SPORTS: "체육시설",
  CONVENIENCE: "편의점",
  MART: "마트",
  HOSPITAL: "병원",
  PHARMACY: "약국",
  SUBWAY: "지하철역",
  BUS: "버스정류장",
  PARK: "공원",
  BANK: "은행",
  FOOD: "음식점",
  ACADEMY: "학원",
  CULTURE: "문화시설",
  FIRE: "소방서",
  GAS: "주유소",
  GOV_OFFICE: "관공서",
  KINDERGARTEN: "유치원",
  LIBRARY: "도서관",
  PARKING: "주차장",
  POLICE: "경찰서",
  POST_OFFICE: "우체국",
  PUBLIC: "공공기관",
  SCHOOL: "학교",
  SWIMMING: "수영장",
};

// 숫자를 억/만 단위로 분리해 주는 헬퍼 함수
const formatKoreanMoney = (value) => {
  if (value === undefined || value === null || isNaN(value)) return "";

  const num = Number(value);
  if (num === 0) return "0만원";

  const uk = Math.floor(num / 10000);
  const man = num % 10000;

  if (uk > 0 && man > 0) {
    return `${uk}억 ${man}만원`;
  } else if (uk > 0) {
    return `${uk}억`;
  } else {
    return `${man}만원`;
  }
};

// 거래 유형별 가격 포맷팅
const formattedPrice = computed(() => {
  if (!p.value) return "";
  const type = p.value.tradeType;

  if (type === "월세") {
    const deposit = formatKoreanMoney(p.value.deposit).replace("만원", "");
    return `월세 ${deposit}/${p.value.monthlyRent}만원`;
  } else if (type === "전세") {
    const price = formatKoreanMoney(p.value.jeonsePrice ?? p.value.deposit);
    return `전세 ${price}`;
  } else if (type === "매매") {
    const price = formatKoreanMoney(p.value.sellingPrice ?? p.value.deposit);
    return `매매 ${price}`;
  }

  return `${type} ${formatKoreanMoney(p.value.deposit)}`;
});

// 거래 유형별 가격 상세 포맷팅
const formattedPriceDetail = computed(() => {
  if (!p.value) return "";
  const type = p.value.tradeType;

  if (type === "월세") {
    const deposit = formatKoreanMoney(p.value.deposit).replace("만원", "");
    const rent = formatKoreanMoney(p.value.monthlyRent);
    return `월세 · ${deposit} / ${rent}`;
  } else if (type === "전세") {
    const price = formatKoreanMoney(p.value.jeonsePrice ?? p.value.deposit);
    return `전세 · ${price}`;
  } else if (type === "매매") {
    const price = formatKoreanMoney(p.value.sellingPrice ?? p.value.deposit);
    return `매매 · ${price}`;
  }

  return `${type} · ${formatKoreanMoney(p.value.deposit)}`;
});

// 층수 포맷팅
const formatFloorInfo = (floorStr) => {
  if (!floorStr) return "";
  const parts = floorStr.split("/").map((item) => item.trim());

  if (parts.length < 2) return floorStr;

  const currentFloor = parts[0].replace(/[^0-9-]/g, "");
  const totalFloor = parts[1].replace(/[^0-9]/g, "");

  return `${currentFloor}층 / 총 ${totalFloor}층`;
};

const formattedFloor = computed(() => formatFloorInfo(p.value?.floorInfo));

const pyeong = computed(() =>
  p.value?.areaM2 ? Math.round(p.value.areaM2 / 3.3) : 0,
);

// 주소/동 정보 기반 라인
const regionLine = computed(() => {
  if (!p.value) return "";
  const addrPart = p.value.address
    ? p.value.address.split(" ").slice(0, 3).join(" ")
    : "";
  const dongPart = p.value.dong ? ` ${p.value.dong}` : "";
  return `${addrPart}${dongPart} · ${p.value.propertyType || ""} ${pyeong.value}평 · ${formattedFloor.value}`;
});

// 건물명 (title을 기본으로 활용)
const buildingLine = computed(() => {
  if (!p.value?.title) return "건물명 정보 없음";

  // " 2101호", " 202호" 처럼 맨 뒤에 붙은 (공백 + 숫자 + 호) 패턴을 제거합니다.
  return p.value.title.replace(/\s*\d+호$/, "").trim();
});

const formatDateStr = (dateStr, delimiter = ".") => {
  if (!dateStr) return "";

  // "T" 기준으로 잘라서 날짜 부분("2026-09-28")만 추출
  const dateOnly = dateStr.split("T")[0];

  // "-"를 지정한 구분자(기본값: '.')로 변경
  return dateOnly.replaceAll("-", delimiter);
};

// 입주 가능일 및 협의 가능 여부 처리
const moveInLine = computed(() => {
  if (!p.value?.moveInDate) return "협의 가능";

  // 공통 날짜 변환 함수 사용 ("2026.09.28")
  const formattedDate = formatDateStr(p.value.moveInDate);

  // discussionStatus(카멜) 또는 discussion_status(스네이크) 안전 처리
  const isDiscussable = p.value?.discussionStatus ?? p.value?.discussion_status;
  const statusStr = isDiscussable ? "협의 가능" : "협의 불가능";

  return `${formattedDate} (${statusStr})`;
});

// 사용 승인일
const availableLine = computed(() => {
  return formatDateStr(p.value?.availableDate);
});

const medianPrice = computed(() => market.value?.medianPrice ?? 47);

// 인프라 데이터 포맷팅
const formattedInfraSummary = computed(() => {
  if (!p.value?.infraSummary || !Array.isArray(p.value.infraSummary)) return [];

  return p.value.infraSummary.map((item) => ({
    category: item.category,
    name: CATEGORY_NAMES[item.category] || item.category,
    count: item.count ?? 0,
  }));
});

const benefits = [
  { id: 1, title: "청년 월세/전세 지원", sub: "조건 충족 시 지원 혜택 제공" },
  { id: 2, title: "청년 전입지원금", sub: "1회 지원" },
  {
    id: 3,
    title: "KB 청년 전월세보증금 대출",
    sub: "실질 우대 금리 혜택 적용",
  },
];

async function toggleFavorite() {
  if (!p.value?.id) return;
  try {
    if (isFavorite.value) {
      await favoriteApi.remove(p.value.id);
      isFavorite.value = false;
    } else {
      await favoriteApi.add(p.value.id);
      isFavorite.value = true;
    }
  } catch (err) {
    console.error("찜 상태 변경 실패:", err);
  }
}

// 비교함 담기 기능
async function addToCompare() {
  if (!p.value?.id) return;

  // load() 할 필요 없이 바로 담기 시도
  const res = await compareStore.add({
    propertyId: p.value.id,
    title: p.value.title,
    tradeType: p.value.tradeType,
    deposit: p.value.deposit,
    monthlyRent: p.value.monthlyRent,
  });

  // 성공("비교함에 담았어요.") 또는 실패("최대 3개까지...") 메시지 출력
  compareMsg.value = res.message;

  setTimeout(() => {
    compareMsg.value = "";
  }, 2500);
}
</script>

<style scoped>
.pdetail {
  display: flex;
  flex-direction: column;
  height: 100vh;
  max-width: 430px;
  margin: 0 auto;
  position: relative;
  background-color: #fff;
  overflow: hidden;
}
.scroll-area {
  flex: 1;
  overflow-y: auto;
  padding-bottom: 20px;
}
.photo-slider {
  position: relative;
  height: 220px;
  background: #f4f1ea;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}
.photo-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.photo-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: #8a8477;
  font-size: 13px;
}
.slide-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(0, 0, 0, 0.3);
  color: #fff;
  border: none;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}
.slide-btn.left {
  left: 10px;
}
.slide-btn.right {
  right: 10px;
}

.dots {
  position: absolute;
  bottom: 12px;
  display: flex;
  gap: 6px;
}
.d {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.5);
  cursor: pointer;
  transition: all 0.2s;
}
.d.on {
  background: #ffbc00;
  width: 14px;
  border-radius: 4px;
}

.head {
  background: #fff;
  padding: 16px;
}
.price {
  font-size: 21px;
  font-weight: 900;
}
.addr {
  margin-top: 6px;
  font-size: 12.5px;
  color: #767676;
}

.score-card {
  margin: 12px 16px 0;
  padding: 16px;
  background: #fff;
  border: 1px solid #eee;
  border-radius: 14px;
}
.score-text-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.score-line {
  font-size: 14px;
  color: #333;
  line-height: 1.5;
}
.score-line.sub {
  font-size: 13px;
  color: #666;
}
.score-line strong {
  font-weight: 800;
}
.score-line .highlight {
  font-weight: 900;
  color: #d9a800;
  font-size: 15px;
}

.card {
  margin: 12px 16px 0;
  padding: 16px;
  background: #fff;
  border: 1px solid #eee;
  border-radius: 16px;
}
.card-head {
  font-size: 14.5px;
  font-weight: 800;
}
.card-sub {
  margin-top: 4px;
  font-size: 11.5px;
  color: #8a8d8f;
}
.tag-row {
  display: flex;
  gap: 8px;
  margin: 12px 0 4px;
}
.tag {
  padding: 4px 10px;
  border-radius: 100px;
  border: 1px solid #ddd;
  font-size: 12px;
}
.tag.yellow {
  border-color: #ffbc00;
  background: #fffdf5;
  font-weight: 700;
}
.info-list {
  margin-top: 8px;
}
.info-row {
  display: flex;
  gap: 12px;
  padding: 6px 0;
}
.info-row dt {
  flex: 0 0 88px;
  font-size: 13px;
  color: #8a8d8f;
}
.info-row dd {
  flex: 1;
  font-size: 13px;
  font-weight: 500;
}

.section-divider {
  margin: 16px 0 12px;
  border: none;
  border-top: 1px dashed #e2e2e2;
}

.desc-head {
  font-size: 13px;
  color: #8a8d8f;
}
.desc {
  margin-top: 6px;
  font-size: 13px;
  line-height: 1.6;
}

.mini-row {
  display: flex;
  gap: 10px;
  margin: 12px 16px 0;
}
.mini-card {
  flex: 1;
  background: #f5f4f0;
  border-radius: 12px;
  padding: 12px 14px;
}
.mini-card.wide {
  margin: 10px 16px 0;
}
.mini-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11.5px;
  color: #8a8d8f;
}
.mini-value {
  margin-top: 4px;
  font-size: 13.5px;
  font-weight: 800;
}

.banner-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin: 16px 16px 0;
}
.simple-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 14px 18px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 800;
  border: none;
  cursor: pointer;
}
.simple-banner.yellow {
  background: #ffbc00;
  color: #333;
}
.simple-banner.green {
  background: #2f9e69;
  color: #fff;
}
.simple-banner.brown {
  background: #8a7a55;
  color: #fff;
}

.realtor-card {
  margin-top: 12px;
  border: 1px solid #eee;
  border-radius: 14px;
  padding: 14px;
}
.realtor-head {
  display: flex;
  align-items: center;
  gap: 10px;
}
.realtor-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: #fdf8eb;
}
.realtor-name {
  font-size: 14px;
  font-weight: 800;
}
.realtor-addr {
  display: flex;
  align-items: center;
  gap: 5px;
  margin-top: 8px;
  font-size: 12px;
  color: #666;
}
.realtor-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}
.rt-btn {
  flex: 1;
  height: 38px;
  border-radius: 100px;
  font-size: 13px;
  font-weight: 700;
  border: 1px solid #ddd;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}
.rt-btn.yellow {
  background: #ffbc00;
  border: none;
}

.kb-note {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 12px;
  padding: 10px 12px;
  background: #fdf8eb;
  border-radius: 10px;
  font-size: 11.5px;
  color: #666;
}

.benefit-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.see-all {
  font-size: 12px;
  font-weight: 700;
  color: #8a8d8f;
  border: none;
  background: none;
}
.benefit-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 12px;
}
.benefit-item {
  padding: 12px;
  background: #f9f8f6;
  border-radius: 10px;
}
.b-texts {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.b-title {
  font-size: 13px;
  font-weight: 700;
}
.b-sub {
  font-size: 11.5px;
  color: #8a8d8f;
}

.bottom-actions-wrap {
  position: sticky;
  bottom: 0;
  width: 100%;
  background-color: #ffffff;
  border-top: 1px solid #f0f0f0;
  padding: 12px 16px;
  box-shadow: 0 -4px 12px rgba(0, 0, 0, 0.05);
  z-index: 10;
}
.bottom-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.fav-btn {
  width: 48px;
  height: 48px;
  border: 1px solid #e5e5e5;
  border-radius: 8px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}
.compare-btn {
  flex: 1;
  height: 48px;
  background-color: #f7f7f8;
  border: 1px solid #e5e5e5;
  border-radius: 8px;
  font-weight: 600;
  font-size: 15px;
  cursor: pointer;
}
.compare-msg {
  position: absolute;
  top: -36px;
  left: 50%;
  transform: translateX(-50%);
  background-color: rgba(0, 0, 0, 0.8);
  color: #fff;
  font-size: 12px;
  padding: 6px 12px;
  border-radius: 16px;
  white-space: nowrap;
}
</style>
