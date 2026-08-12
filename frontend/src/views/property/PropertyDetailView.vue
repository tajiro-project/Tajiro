<template>
  <div
    class="pdetail"
    v-if="p"
  >
    <!-- div -> simplebar로 교체 -->
    <simplebar
      class="scroll-area"
      :auto-hide="true"
    >
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
          <div
            class="dots"
            v-if="p.images.length > 1"
          >
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
        <div
          v-else
          class="photo-placeholder"
        >
          <ImageIcon
            :size="44"
            color="#8a8477"
          />
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
        <div class="score-header">
          <span class="label">주거 가치관 반영 점수</span>
        </div>

        <!-- 게이지 바 & 점수 핀 영역 -->
        <div class="gauge-container">
          <div class="gauge-track">
            <div
              class="gauge-fill"
              :style="{ width: `${p.recommendScore}%` }"
            >
              <!-- 게이지 끝에 붙는 말풍선 핀 -->
              <div class="score-tooltip">{{ p.recommendScore }}점</div>
            </div>
          </div>
          <div class="gauge-ticks">
            <span>0</span>
            <span>50</span>
            <span>100</span>
          </div>
        </div>

        <p class="sub-text">
          비슷한 매물에 대한 매물가 평균은
          <strong>{{ medianPrice }}만원</strong>입니다.
        </p>
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
            <dd>{{ buildingName }}</dd>
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
            <dd>{{ p.parkAvailability ? '가능' : '불가' }}</dd>
          </div>
          <div class="info-row">
            <dt>입주 가능일</dt>
            <dd>{{ moveInLine }}</dd>
          </div>
          <div class="info-row">
            <dt>사용 승인일</dt>
            <dd>{{ availableLine ?? '정보 없음' }}</dd>
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
            <Receipt
              :size="14"
              color="#8a8d8f"
            />
            관리비
          </p>
          <p class="mini-value">월 {{ p.maintenanceFee ?? 0 }}만원</p>
        </div>
        <div class="mini-card">
          <p class="mini-label">
            <Clock
              :size="14"
              color="#8a8d8f"
            />
            통근
          </p>
          <p class="mini-value">
            {{ formatCommuteTime(p.workplaceDistanceMeters) }}
          </p>
        </div>
      </div>

      <!-- 인프라 및 편의시설 분리 카드 -->
      <div class="mini-card wide compact">
        <p class="mini-label">
          <Building2
            :size="14"
            color="#8a8d8f"
          />
          인프라 및 편의시설
        </p>

        <div class="infra-wrapper">
          <template
            v-if="
              formattedInfraList.length === 0 &&
              formattedAmenityList.length === 0
            "
          >
            <p class="empty-text">주변 인프라 정보가 없습니다.</p>
          </template>

          <template v-else>
            <!-- 1. 인프라 섹션 -->
            <div
              class="infra-row"
              v-if="formattedInfraList.length > 0"
            >
              <span class="group-badge">인프라</span>
              <div class="inline-list">
                <template
                  v-for="(item, idx) in formattedInfraList"
                  :key="item.category"
                >
                  <span class="item">
                    <span class="name">{{ item.name }}</span>
                    <span class="count">{{ item.count }}</span>
                  </span>
                  <span
                    v-if="idx < formattedInfraList.length - 1"
                    class="sep"
                    >·</span
                  >
                </template>
              </div>
            </div>

            <!-- 구분선 -->
            <hr
              class="compact-divider"
              v-if="
                formattedInfraList.length > 0 && formattedAmenityList.length > 0
              "
            />

            <!-- 2. 편의시설 섹션 -->
            <div
              class="infra-row"
              v-if="formattedAmenityList.length > 0"
            >
              <span class="group-badge amenity">편의시설</span>
              <div class="inline-list">
                <template
                  v-for="(item, idx) in formattedAmenityList"
                  :key="item.category"
                >
                  <span class="item">
                    <span class="name">{{ item.name }}</span>
                    <span class="count">{{ item.count }}</span>
                  </span>
                  <span
                    v-if="idx < formattedAmenityList.length - 1"
                    class="sep"
                    >·</span
                  >
                </template>
              </div>
            </div>
          </template>
        </div>
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
      </div>

      <!-- 공인중개사 정보 -->
      <section class="card">
        <p class="card-head">이 매물, 어디에 문의할까요?</p>
        <p class="card-sub">이 매물을 등록·관리하는 인근 공인중개사예요</p>
        <div class="realtor-card">
          <div class="realtor-head">
            <span class="realtor-icon">
              <Building2
                :size="18"
                color="#a8842c"
              />
            </span>
            <p class="realtor-name">
              {{ p.realtorPreview?.name ?? 'KB부동산공인중개사' }}
            </p>
          </div>
          <p class="realtor-addr">
            <MapPin
              :size="12"
              color="#8a8d8f"
            />
            {{
              p.address
                ? p.address.split(' ')[0] + ' ' + p.address.split(' ')[1]
                : ''
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
          <Info
            :size="13"
            color="#8a8d8f"
          />
          KB 인증 중개사예요. 계약 전 등록번호를 꼭 확인하세요.
        </p>
      </section>

      <!-- 혜택/상품 카드 (금융 & 정책 분리) -->
      <section class="card benefit-card">
        <div class="benefit-card-head">
          <p class="card-head">이 매물 맞춤 혜택 & 금융</p>
          <span class="badge"
            >최대 혜택 {{ financeList.length + policyList.length }}건</span
          >
        </div>

        <!-- 1. 금융 상품 영역 (바로 노출) -->
        <div class="benefit-group">
          <div class="group-title">
            <Coins
              :size="15"
              color="#a8842c"
            />
            <span>매물 맞춤 금융 상품</span>
          </div>

          <div class="benefit-list">
            <div
              v-for="item in financeList"
              :key="item.id"
              class="benefit-item"
              :class="{ clickable: !!item.applicationUrl }"
              @click="goToFinancialDetail(item.id)"
            >
              <div class="item-icon-box">
                <span class="item-icon">🏦</span>
              </div>

              <div class="item-content">
                <div class="item-header">
                  <span class="item-title">{{ item.productName }}</span>
                </div>

                <div class="item-info-row">
                  <span class="rate-badge">{{ formatRateText(item) }}</span>
                  <span
                    class="limit-text"
                    v-if="item.loanLimit"
                  >
                    {{ formatLoanLimit(item.loanLimit) }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
        <!-- 2. 정책 혜택 영역 (미입력 시 잠금 오버레이 노출) -->
        <div class="benefit-group">
          <div class="group-title">
            <Landmark
              :size="15"
              color="#a8842c"
            />
            <span>청년 · 정부 지원 정책</span>
          </div>

          <div
            class="policy-wrapper"
            :class="{ locked: !isProfileEntered }"
          >
            <!-- 잠금 오버레이 (필수 정보 미입력 시 표시) -->
            <div
              v-if="!isProfileEntered"
              class="lock-overlay"
            >
              <div class="lock-box">
                <div class="lock-icon-wrap">
                  <Lock
                    :size="18"
                    color="#222"
                  />
                </div>
                <p class="lock-text">
                  내 정보 입력 시 <strong>맞춤 정책 혜택</strong> 확인 가능
                </p>
                <button
                  class="btn-input-profile"
                  @click="router.push('/profile-setup')"
                >
                  내 조건 입력하고 확인하기
                </button>
              </div>
            </div>

            <!-- 정책 리스트 -->
            <div class="benefit-list">
              <div
                v-for="item in policyList"
                :key="item.id"
                class="benefit-item clickable"
                @click="goToPolicyDetail(item.id)"
              >
                <div class="item-icon-box">
                  <span class="item-icon">{{ item.icon || '🏛️' }}</span>
                </div>
                <div class="item-content">
                  <span class="item-title">{{
                    item.title || item.polyBizSjnm
                  }}</span>
                  <p class="item-sub">{{ item.sub || item.polyItcnCn }}</p>
                </div>
                <ChevronRight
                  :size="16"
                  color="#aaa"
                />
              </div>
            </div>
          </div>
        </div>
      </section>
    </simplebar>

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
        <button
          class="compare-btn"
          :disabled="isSubmitting"
          @click="addToCompare"
        >
          비교함 담기
        </button>
      </div>
      <p
        v-if="compareMsg"
        class="compare-msg"
      >
        {{ compareMsg }}
      </p>
    </div>
  </div>
</template>

<script setup>
import { inject, computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import simplebar from 'simplebar-vue';
import AppTabBar from '@/components/AppTabBar.vue';
import {
  propertyApi,
  favoriteApi,
  comparisonApi,
  financeApi,
  policyApi,
  userApi,
} from '@/api/services';
import {
  INFRA_CATEGORIES,
  AMENITY_CATEGORIES,
} from '@/constants/preferenceOptions';

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
  Coins,
  Landmark,
  Lock,
} from 'lucide-vue-next';

const route = useRoute();
const router = useRouter();
const compareMsg = ref('');
const isSubmitting = ref(false); // 버튼 연타 방지용

const market = ref(null);
const isFavorite = ref(false);
const profileName = ref('홍길동');

// 사용자 조건 입력 여부
const isProfileEntered = ref(false);
// const policyList = ref([]);

const p = inject('propertyDetail');
const buildingName = inject('buildingName');

const id = route.params.id;

// 1. 금융 상품 API 연동 데이터 및 로직
const financeList = ref([]);
const isLoadingFinance = ref(false);

// 금융 상품 목록 조회
const fetchFinancialRecommendations = async () => {
  if (!id) return;
  try {
    isLoadingFinance.value = true;

    // 서비스 파일 호출 s
    const res = await financeApi.getRecommendations(id);

    financeList.value = res.data || res || [];
  } catch (err) {
    console.error('금융 상품 추천 조회 실패:', err);
  } finally {
    isLoadingFinance.value = false;
  }
};

// 프로필 정보 확인 및 정책 데이터 로드
const checkProfileAndFetchPolicies = async () => {
  try {
    // 1. 유저 프로필 조회
    const profile = await userApi.getProfile();

    // 2. 필수 정보(생년월일, 지역) 입력 여부 검증
    const hasBirthDate = !!profile?.birthDate?.trim();
    const hasTargetRegion = !!profile?.targetRegion?.trim();

    isProfileEntered.value = hasBirthDate && hasTargetRegion;

    // 3. 필수 정보가 모두 존재하는 경우에만 정책 목록 로드
    // if (isProfileEntered.value) {
    //   const policyResponse = await policyApi.list();
    //   policyList.value = policyResponse?.data || [];
    // }
  } catch (error) {
    console.error('프로필 조회 또는 정책 목록 호출 실패:', error);
    isProfileEntered.value = false;
  }
};

onMounted(() => {
  fetchFinancialRecommendations();
  checkProfileAndFetchPolicies();
});

// 금리 텍스트 가공 헬퍼 (JSON 문자열 예외 처리 추가)
const formatRateText = (item) => {
  // 1. minRate와 maxRate가 정상적으로 존재하는 경우
  if (item.minRate && item.maxRate) {
    return item.minRate === item.maxRate
      ? `연 ${item.minRate}%`
      : `연 ${item.minRate}% ~ ${item.maxRate}%`;
  }
  if (item.minRate) return `연 ${item.minRate}% ~`;
  if (item.maxRate) return `연 ~ ${item.maxRate}%`;

  // 2. min/maxRate가 없어서 rateDescription을 처리해야 하는 경우
  if (item.rateDescription) {
    try {
      // JSON 객체 형태의 문자열인지 확인 후 파싱
      const parsed =
        typeof item.rateDescription === 'string' &&
        item.rateDescription.trim().startsWith('{')
          ? JSON.parse(item.rateDescription)
          : item.rateDescription;

      if (typeof parsed === 'object' && parsed !== null) {
        // details 배열 내 금리 정보가 존재하면 사용
        if (parsed.details && parsed.details.length > 0) {
          const rates = parsed.details.map((d) => d.final_rate).filter(Boolean);
          if (rates.length > 0) return rates.join(' / ');
        }
        // base_type이 존재하면 활용
        if (parsed.base_type) return parsed.base_type;
      } else if (typeof parsed === 'string') {
        return parsed;
      }
    } catch (e) {
      // JSON 파싱 실패 시 기본값 처리
    }
  }

  return '변동금리 (상세 확인)';
};

// 한도 텍스트 괄호 '(' 전까지만 자르는 헬퍼 함수
const formatLoanLimit = (limitStr) => {
  if (!limitStr) return '';
  // '(' 기준으로 잘라서 첫 번째 부분만 사용하고 여백 제거
  return limitStr.split('(')[0].trim();
};

// 1. 금융 상품 상세 이동
const goToFinancialDetail = (id) => {
  if (!id) return;
  router.push({ name: 'financial-product-detail', params: { id } });
};

// 2. 정책 상세 이동
const goToPolicyDetail = (id) => {
  if (!id) return;
  router.push({ name: 'policy-detail', params: { id } });
};

const formatCommuteTime = (data) => {
  // 평균 도보 분속
  const SPEED_PER_MINUTE = 80;

  // 굴곡도 1.25배 적용
  const actualDistance = data * 1.25;

  // 값이 null이나 undefined인 경우 예외 처리
  if (data === null || data === undefined) return '정보 없음';

  // 도보 분속 계산 (올림 처리하여 최소 1분 이상 표시)
  const minutes = Math.ceil(actualDistance / SPEED_PER_MINUTE);

  if (minutes === 0) return '1분 미만';

  return `약 ${minutes}분`;
};

// 이미지 캐러셀 상태
const currentImgIndex = ref(0);
let touchStartX = 0;

// 다음 이미지
function nextImage() {
  if (!p.value?.images?.length) return;
  currentImgIndex.value = (currentImgIndex.value + 1) % p.value.images.length;
}

// 이전 이미지
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

// 숫자를 억/만 단위로 분리해 주는 헬퍼 함수
const formatKoreanMoney = (value) => {
  if (value === undefined || value === null || isNaN(value)) return '';

  const num = Number(value);
  if (num === 0) return '0만원';

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
  if (!p.value) return '';
  const type = p.value.tradeType;

  if (type === '월세') {
    const deposit = formatKoreanMoney(p.value.deposit).replace('만원', '');
    return `월세 ${deposit}/${p.value.monthlyRent}만원`;
  } else if (type === '전세') {
    const price = formatKoreanMoney(p.value.jeonsePrice ?? p.value.deposit);
    return `전세 ${price}`;
  } else if (type === '매매') {
    const price = formatKoreanMoney(p.value.sellingPrice ?? p.value.deposit);
    return `매매 ${price}`;
  }

  return `${type} ${formatKoreanMoney(p.value.deposit)}`;
});

// 거래 유형별 가격 상세 포맷팅
const formattedPriceDetail = computed(() => {
  if (!p.value) return '';
  const type = p.value.tradeType;

  if (type === '월세') {
    const deposit = formatKoreanMoney(p.value.deposit).replace('만원', '');
    const rent = formatKoreanMoney(p.value.monthlyRent);
    return `월세 · ${deposit} / ${rent}`;
  } else if (type === '전세') {
    const price = formatKoreanMoney(p.value.jeonsePrice ?? p.value.deposit);
    return `전세 · ${price}`;
  } else if (type === '매매') {
    const price = formatKoreanMoney(p.value.sellingPrice ?? p.value.deposit);
    return `매매 · ${price}`;
  }

  return `${type} · ${formatKoreanMoney(p.value.deposit)}`;
});

// 층수 포맷팅
const formatFloorInfo = (floorStr) => {
  if (!floorStr) return '';
  const parts = floorStr.split('/').map((item) => item.trim());

  if (parts.length < 2) return floorStr;

  const currentFloor = parts[0].replace(/[^0-9-]/g, '');
  const totalFloor = parts[1].replace(/[^0-9]/g, '');

  return `${currentFloor}층 / 총 ${totalFloor}층`;
};

const formattedFloor = computed(() => formatFloorInfo(p.value?.floorInfo));

const pyeong = computed(() =>
  p.value?.areaM2 ? Math.round(p.value.areaM2 / 3.3) : 0,
);

// 주소/동 정보 기반 라인
const regionLine = computed(() => {
  if (!p.value) return '';
  const addrPart = p.value.address
    ? p.value.address.split(' ').slice(0, 3).join(' ')
    : '';
  const dongPart = p.value.dong ? ` ${p.value.dong}` : '';
  return `${addrPart}${dongPart} · ${p.value.propertyType || ''} ${pyeong.value}평 · ${formattedFloor.value}`;
});

const formatDateStr = (dateStr, delimiter = '.') => {
  if (!dateStr) return '';
  const dateOnly = dateStr.split('T')[0];
  return dateOnly.replaceAll('-', delimiter);
};

// 입주 가능일 및 협의 가능 여부 처리
const moveInLine = computed(() => {
  if (!p.value?.moveInDate) return '협의 가능';
  const formattedDate = formatDateStr(p.value.moveInDate);
  const isDiscussable = p.value?.discussionStatus ?? p.value?.discussion_status;
  const statusStr = isDiscussable ? '협의 가능' : '협의 불가능';

  return `${formattedDate} (${statusStr})`;
});

// 사용 승인일
const availableLine = computed(() => {
  return formatDateStr(p.value?.availableDate);
});

const medianPrice = computed(() => market.value?.medianPrice ?? 47);

// 인프라 데이터 및 분류 처리
const infraData = computed(
  () => p.value?.infraSummary || p.value?.infraList || [],
);

// 카테고리 Key -> 한글 이름(label) 매핑 맵 생성
const infraCategoryMap = new Map(
  INFRA_CATEGORIES.map((item) => [item.key, item.label]),
);
const amenityCategoryMap = new Map(
  AMENITY_CATEGORIES.map((item) => [item.key, item.label]),
);

// 1. 주요 인프라 항목 필터링
const formattedInfraList = computed(() => {
  return infraData.value
    .filter((item) => infraCategoryMap.has(item.category))
    .map((item) => ({
      category: item.category,
      name: infraCategoryMap.get(item.category),
      count: item.count,
    }));
});

// 2. 편의시설 항목 필터링
const formattedAmenityList = computed(() => {
  return infraData.value
    .filter((item) => amenityCategoryMap.has(item.category))
    .map((item) => ({
      category: item.category,
      name: amenityCategoryMap.get(item.category),
      count: item.count,
    }));
});

// 맞춤 정책 혜택 데이터 (사용자 정보 입력 필요)
const policyList = ref([
  {
    id: 'p1',
    icon: '🎁',
    title: '창원시 청년월세지원',
    sub: '조건 충족 시 월 20만원 × 12개월',
  },
  {
    id: 'p2',
    icon: '💵',
    title: '청년 전입지원금',
    sub: '최초 전입 시 1회 30만원 지급',
  },
  {
    id: 'p3',
    icon: '🌱',
    title: '청년 이사비 지원사업',
    sub: '이사비 및 중개보수 최대 40만원',
  },
]);

const openProfileModal = () => {
  router.push('/profile-setup');
};

// 찜 상태 토글
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
    console.error('찜 상태 변경 실패:', err);
  }
}

// Pinia 없이 직접 API 호출하는 비교함 담기 기능
async function addToCompare() {
  if (!p.value?.id || isSubmitting.value) return;

  try {
    isSubmitting.value = true;

    // 백엔드 API 직접 호출
    await comparisonApi.addToBox(p.value.id);
    compareMsg.value = '비교함에 담았어요.';
  } catch (error) {
    console.error('비교함 담기 실패:', error);

    const status = error.response?.status;

    if (status === 409) {
      const serverMessage = error.response?.data?.message;
      compareMsg.value =
        serverMessage || '비교함에는 매물을 최대 3개까지 담을 수 있습니다.';
    } else if (status === 404) {
      compareMsg.value = '존재하지 않는 매물입니다.';
    } else {
      compareMsg.value = '비교함 추가 중 오류가 발생했습니다.';
    }
  } finally {
    isSubmitting.value = false;

    setTimeout(() => {
      compareMsg.value = '';
    }, 2500);
  }
}
</script>

<style scoped>
/* ==========================================================================
   1. 전체 레이아웃 & 스크롤 영역
   ========================================================================== */
.pdetail {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--bg);
  overflow: hidden;
}

.scroll-area {
  padding-bottom: 16px;
}

/* ==========================================================================
   2. 포토 슬라이더
   ========================================================================== */
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

/* ==========================================================================
   3. 매물 기본 정보 헤더 (가격 / 주소)
   ========================================================================== */
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

/* ==========================================================================
   4. 가치관 점수 카드 & 게이지 바 (말풍선 유지 + 타이트한 여백)
   ========================================================================== */
.score-card {
  margin: 12px 16px 0;
  padding: 16px 18px 14px;
  background: #ffffff;
  border-radius: 16px;
  border: 1px solid #f0f0f0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);
}

.score-header {
  margin-bottom: 0;
}

.score-header .label {
  font-size: 16px;
  font-weight: 700;
  color: #333333;
}

.gauge-container {
  margin-top: 26px;
  margin-bottom: 12px;
}

.gauge-track {
  width: 100%;
  height: 8px;
  background-color: #f2f4f6;
  border-radius: 999px;
  position: relative;
}

.gauge-fill {
  height: 100%;
  background: linear-gradient(90deg, #ffde6a 0%, #ffb800 100%);
  border-radius: 999px;
  position: relative;
  transition: width 0.8s cubic-bezier(0.25, 1, 0.5, 1);
}

.score-tooltip {
  position: absolute;
  right: 0;
  top: -24px;
  transform: translateX(50%);
  background: #333333;
  color: #ffffff;
  font-size: 10.5px;
  font-weight: 700;
  padding: 2px 6px;
  border-radius: 5px;
  white-space: nowrap;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
}

.score-tooltip::after {
  content: '';
  position: absolute;
  bottom: -3px;
  left: 50%;
  transform: translateX(-50%);
  border-width: 3px 3px 0;
  border-style: solid;
  border-color: #333333 transparent transparent;
}

.gauge-ticks {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: #b0b8c1;
  margin-top: 4px;
  padding: 0 2px;
}

.sub-text {
  font-size: 12.5px;
  color: #666666;
  margin: 0;
  line-height: 1.4;
}

.sub-text strong {
  color: #222222;
  font-weight: 700;
}

/* ==========================================================================
   5. 메인 상세 카드 / 태그 / 상세 정보 리스트
   ========================================================================== */
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

/* ==========================================================================
   6. 미니 카드 & 인프라/편의시설
   ========================================================================== */
.mini-row {
  display: flex;
  gap: 10px;
  margin: 12px 16px 0;
}

.mini-card {
  flex: 1;
  background: #ffffff;
  border-radius: 12px;
  padding: 12px 14px;
}

.mini-card.wide {
  margin: 10px 16px 0;
}

.mini-card.wide.compact {
  padding: 10px 12px;
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

.infra-wrapper {
  margin-top: 8px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.infra-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.group-badge {
  flex-shrink: 0;
  font-size: 10.5px;
  font-weight: 700;
  color: #666;
  background-color: #fff6dc;
  padding: 1px 5px;
  border-radius: 4px;
  white-space: nowrap;
  margin-top: 1px;
}

.group-badge.amenity {
  background-color: #e2f0d9;
  color: #385723;
}

.inline-list {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px 6px;
  line-height: 1.4;
}

.item {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  white-space: nowrap;
  font-size: 12px;
}

.name {
  color: #444;
}

.count {
  font-weight: 700;
  color: #e67e22;
}

.sep {
  color: #b0b0b0;
  font-size: 11px;
  user-select: none;
}

.compact-divider {
  border: none;
  border-top: 1px solid #eee;
  margin: 2px 0;
  width: 100%;
}

.empty-text {
  font-size: 12px;
  color: #8a8d8f;
}

/* ==========================================================================
   7. 이동 배너 버튼
   ========================================================================== */
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

/* ==========================================================================
   8. 중개사 정보 카드
   ========================================================================== */
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

/* ==========================================================================
   9. 혜택 섹션 (금융 및 정책 레이아웃)
   ========================================================================== */
.benefit-card {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.benefit-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.badge {
  font-size: 11.5px;
  font-weight: 700;
  color: #2b6cb0;
  background: #ebf8ff;
  padding: 3px 8px;
  border-radius: 12px;
}

.benefit-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.group-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12.5px;
  font-weight: 700;
  color: #666666;
}

.benefit-list {
  display: flex;
  flex-direction: column;
  gap: 10px; /* 각 카드 사이 간격 넓힘 */
  margin-top: 8px;
}

.benefit-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 16px; /* 내부 여백 조절 */
  background: #ffffff; /* 흰색 카드로 구분감 제공 */
  border-radius: 12px;
  border: 1px solid #eef0f2; /* 연한 테두리로 카드 분리 */
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.03); /* 은은한 그림자 */
  transition: all 0.2s ease;
}

.benefit-item.clickable {
  cursor: pointer;
}

.benefit-item.clickable:hover {
  border-color: #ffbc00;
  transform: translateY(-1px);
}

.item-icon-box {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: #fdf8eb;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
}

.item-content {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex-grow: 1;
  min-width: 0; /* 텍스트 넘침 방지 */
}

.item-header {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.item-title {
  font-size: 13.5px;
  font-weight: 700;
  color: #222222;
  line-height: 1.3;
}

/* 금리/한도 배치 영역 */
.item-info-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  column-gap: 10px; /* 한 줄일 때 (금리 - 한도) 가로 간격 */
  row-gap: 4px; /* 줄바꿈되었을 때 세로 간격 */
  font-size: 12px;
}

.rate-badge {
  font-weight: 700;
  color: #d97706; /* 금리 강조 색상 */
}

.limit-text {
  color: #666666;
  position: relative;
}

/* 금리와 한도 사이 구분점 */
.limit-text::before {
  display: none; /* 또는 해당 CSS 블록 전체 삭제 */
}

.link-icon {
  display: flex;
  align-items: center;
  align-self: center;
  margin-left: 2px;
}

.item-sub {
  font-size: 11.5px;
  color: #8a8d8f;
  margin: 0;
}

/* 정책 오버레이 & 잠금 스타일 */
.policy-wrapper {
  position: relative;
}

/* 프로필 미입력 시 자식 리스트 블러 처리 */
.policy-wrapper.locked .benefit-list {
  filter: blur(4px);
  pointer-events: none; /* 클릭 방지 */
  user-select: none;
}

/* 잠금 오버레이 중앙 정렬 */
.lock-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.45);
}

.lock-box {
  background: #ffffff;
  padding: 24px 20px;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #eee;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.btn-input-profile {
  background-color: #222222;
  color: #ffffff;
  border: none;
  border-radius: 8px;
  padding: 10px 18px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
}

.lock-icon-wrap {
  width: 32px;
  height: 32px;
  background: #f3f4f6;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.lock-text {
  font-size: 12px;
  color: #4b5563;
  margin: 0;
  line-height: 1.4;
}

.btn-input-profile {
  background: #222222;
  color: #ffffff;
  border: none;
  padding: 8px 14px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  margin-top: 2px;
  transition: background 0.2s;
}

.btn-input-profile:hover {
  background: #000000;
}

/* ==========================================================================
   10. 하단 액션 바 (Sticky 고정)
   ========================================================================== */
.bottom-actions-wrap {
  position: sticky;
  bottom: 0;
  width: 100%;
  flex-shrink: 0;
  background-color: var(--white, #ffffff);
  border-top: 1px solid var(--border, #f0f0f0);
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
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border: 1px solid var(--border);
  border-radius: var(--radius-input);
  background: var(--white);
  flex-shrink: 0;
}

.fav-btn.on {
  border-color: #ffbc00;
}

.compare-btn {
  flex: 1;
  height: 48px;
  border-radius: var(--radius-input);
  background: #ffbc00;
  color: var(--text-primary);
  font-size: 15px;
  font-weight: 700;
}

.compare-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.compare-msg {
  margin-top: 6px;
  font-size: 12px;
  color: var(--kb-gray);
  text-align: center;
}
</style>
