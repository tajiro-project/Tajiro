<template>
  <div class="pdetail" v-if="p">
    <PageHeader title="매물 상세" />

    <div class="scroll-area">
      <!-- 1. 사진 캐러셀 (Lucide 아이콘 적용 & 슬라이더) -->
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

      <!-- 2. 가격/요약 (거래 유형별 분기 처리) -->
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
            <span class="highlight">{{ p.recommendScore ?? 0 }}점</span> 입니다!
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
          <!-- 거래 방식에 따라 라벨과 금액 분기 -->
          <div class="info-row">
            <dt>거래 · 가격</dt>
            <dd>{{ formattedPriceDetail }}</dd>
          </div>
          <div class="info-row">
            <dt>관리비</dt>
            <dd>월 {{ p.maintenanceFee }}만원</dd>
          </div>
          <div class="info-row">
            <dt>도로명 주소</dt>
            <dd>{{ p.address }}</dd>
          </div>
          <div class="info-row">
            <dt>층수</dt>
            <dd>{{ p.floorInfo }} / 총 {{ p.totalFloor ?? 12 }}층</dd>
          </div>
          <div class="info-row">
            <dt>면적</dt>
            <dd>{{ p.areaM2 }}㎡ (약 {{ pyeong }}평)</dd>
          </div>
          <div class="info-row">
            <dt>방 · 욕실</dt>
            <dd>{{ p.roomNum }}개 · {{ p.bathroomNum }}개</dd>
          </div>
          <div class="info-row">
            <dt>주차</dt>
            <dd>{{ p.parkAvailability ? "가능" : "불가" }}</dd>
          </div>
          <div class="info-row">
            <dt>입주 가능일</dt>
            <dd>{{ availableLine }}</dd>
          </div>
          <div class="info-row">
            <dt>사용 승인일</dt>
            <dd>{{ p.approvedDate ?? "2021.07.12" }}</dd>
          </div>
        </dl>

        <!-- 구분선 추가 -->
        <hr class="section-divider" />

        <p class="desc-head">매물 상세 설명</p>
        <p class="desc">{{ p.propertyDescription }}</p>
      </section>

      <!-- 요약 미니카드 (Lucide 아이콘 사용) -->
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
          <p class="mini-value">{{ p.commuteTime ?? "도보 15분" }}</p>
        </div>
      </div>
      <div class="mini-card wide">
        <p class="mini-label">
          <Building2 :size="14" color="#8a8d8f" />
          인프라
        </p>
        <p class="mini-value">
          카페 {{ infraCount("CAFE") }} · 헬스장 {{ infraCount("GYM") }}
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

      <!-- 공인중개사 정보 (Lucide 아이콘 적용) -->
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
            성산구 상남동 · 매물 앞 80m
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

      <!-- 하단 액션 (Lucide Heart 아이콘 적용) -->
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
const compare = useCompareStore();

const p = ref(null);
const infras = ref([]);
const market = ref(null);
const isFavorite = ref(false);
const compareMsg = ref("");
const profileName = ref("홍길동");

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

onMounted(async () => {
  const id = route.params.id;
  const detailData = await propertyApi.detail(id);

  if (!detailData.images || detailData.images.length === 0) {
    detailData.images = [
      "https://via.placeholder.com/600x400/f5efdb/8a8477?text=Room+Image+1",
      "https://via.placeholder.com/600x400/e4efe4/2f9e69?text=Room+Image+2",
    ];
  }

  p.value = detailData;
  isFavorite.value = detailData?.isFavorite ?? false;
  infras.value = (await propertyApi.infrastructures(id)) ?? [];
  market.value = await propertyApi.marketEvaluation(id);
});

// --- 거래 유형별 가격 포맷팅 computed ---
const formattedPrice = computed(() => {
  if (!p.value) return "";
  const type = p.value.tradeType;

  if (type === "월세") {
    return `월세 ${p.value.deposit}/${p.value.monthlyRent}`;
  } else if (type === "전세") {
    return `전세 ${p.value.jeonsePrice ?? p.value.deposit}`;
  } else if (type === "매매") {
    return `매매 ${p.value.sellingPrice ?? p.value.deposit}`;
  }
  return `${type} ${p.value.deposit}`;
});

const formattedPriceDetail = computed(() => {
  if (!p.value) return "";
  const type = p.value.tradeType;

  if (type === "월세") {
    return `월세 · 보증금 ${p.value.deposit} / 월 ${p.value.monthlyRent}만원`;
  } else if (type === "전세") {
    return `전세 · ${p.value.jeonsePrice ?? p.value.deposit}만원`;
  } else if (type === "매매") {
    return `매매 · ${p.value.sellingPrice ?? p.value.deposit}만원`;
  }
  return `${type} · ${p.value.deposit}만원`;
});

const pyeong = computed(() =>
  p.value?.areaM2 ? Math.round(p.value.areaM2 / 3.3) : 0,
);
const regionLine = computed(() =>
  p.value
    ? `창원시 성산구 ${p.value.dong || ""} · ${p.value.propertyType || ""} ${pyeong.value}평 · ${p.value.floorInfo || ""}/${p.value.totalFloor || 12}층`
    : "",
);
const buildingLine = computed(
  () => p.value?.buildingName ?? "상남 오피스텔 (에스하임)",
);
const availableLine = computed(() => {
  if (!p.value?.availableDate) return "즉시 입주";
  return `${p.value.availableDate.replaceAll("-", ".")} (${p.value.discussionStatus ?? "협의 가능"})`;
});
const medianPrice = computed(() => market.value?.medianPrice ?? 47);

function infraCount(cat) {
  return infras.value.filter((i) => i.category === cat).length;
}

const benefits = [
  { id: 1, title: "창원시 청년월세지원", sub: "월 20만원 × 12개월" },
  { id: 2, title: "청년 전입지원금", sub: "1회 30만원" },
  {
    id: 3,
    title: "KB 청년 전월세보증금 대출",
    sub: "실질 2.4% · 예상 이자 월 1만원",
  },
];

async function toggleFavorite() {
  if (isFavorite.value) {
    await favoriteApi.remove(p.value.id);
    isFavorite.value = false;
  } else {
    await favoriteApi.add(p.value.id);
    isFavorite.value = true;
  }
}

async function addToCompare() {
  const ok = await compare.add({
    propertyId: p.value.id,
    title: p.value.title,
    tradeType: p.value.tradeType,
    deposit: p.value.deposit,
    monthlyRent: p.value.monthlyRent,
  });
  compareMsg.value = ok
    ? "비교함에 담았어요."
    : "최대 3개까지 담을 수 있습니다.";
  setTimeout(() => (compareMsg.value = ""), 2500);
}
</script>

<style scoped>
.pdetail {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
}
.scroll-area {
  flex: 1;
  overflow-y: auto;
  padding-bottom: 20px;
}

/* 사진 슬라이더 */
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

/* 요약 헤더 */
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

/* 통합 점수 카드 */
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

/* 매물 정보 카드 */
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

/* 사용 승인일과 상세 설명 사이의 구분선 */
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

/* 요약 미니 카드 */
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

/* 이동 배너 그룹 */
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

/* 공인중개사 & 혜택 */
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

/* 하단 액션 바 */
.bottom-actions {
  display: flex;
  gap: 10px;
  margin: 16px;
}
.fav-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 46px;
  height: 46px;
  border-radius: 12px;
  border: 1px solid #eee;
  background: #fff;
  cursor: pointer;
}
.compare-btn {
  flex: 1;
  height: 46px;
  border-radius: 12px;
  border: 1px solid #eee;
  background: #fff;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
}
.compare-msg {
  margin: -6px 16px 10px;
  font-size: 12px;
  color: #e53935;
  text-align: center;
}
</style>
