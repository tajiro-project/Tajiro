<template>
  <div
    class="pdetail"
    v-if="p"
  >
    <simplebar
      ref="scrollArea"
      class="scroll-area"
      :auto-hide="true"
    >
      <p
        v-if="route.query.demo === '1'"
        class="demo-banner"
      >
        예시 화면이에요 · 실제 매물이 아니에요
      </p>

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

      <div class="head">
        <h1 class="price">{{ formattedPrice }}</h1>
        <p class="addr">{{ regionLine }}</p>
      </div>

      <!-- 내 기준 점수 & 시세 문구 통합 카드 -->
      <div
        class="score-card"
        data-tour="detail-score"
      >
        <div class="score-header">
          <span class="label">주거 가치관 반영 점수</span>
        </div>

        <div class="gauge-container">
          <div class="gauge-track">
            <div
              class="gauge-fill"
              :style="{ width: `${p.recommendScore}%` }"
            >
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

      <section class="card">
        <p class="card-head">매물 정보</p>
        <div class="tag-row">
          <span class="tag yellow">{{ p.propertyType }}</span>
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
            통근 거리
          </p>
          <p class="mini-value">
            {{ formatDistance(p.workplaceDistanceMeters) }}
          </p>
        </div>
      </div>

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

            <hr
              class="compact-divider"
              v-if="
                formattedInfraList.length > 0 && formattedAmenityList.length > 0
              "
            />

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
      <div
        class="banner-group"
        data-tour="detail-banners"
      >
        <button
          class="simple-banner yellow"
          @click="goToInfra"
        >
          <span>가장 가까운 인프라 보기</span>
          <span class="arrow">→</span>
        </button>

        <button
          class="simple-banner green"
          @click="goToSafety"
        >
          <span>안전 정보 보기</span>
          <span class="arrow">→</span>
        </button>
      </div>

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

      <section class="card benefit-card">
        <div class="benefit-group">
          <div class="group-header">
            <div class="group-title">
              <Banknote
                :size="19"
                color="#a8842c"
              />
              <span>매물 맞춤 금융 상품</span>
            </div>
            <span class="badge">최대 {{ financeList?.length || 0 }}건</span>
          </div>

          <div class="benefit-list">
            <template v-if="financeList.length > 0">
              <div
                v-for="item in financeList"
                :key="item.id"
                class="benefit-item"
                :class="{ clickable: !!item.applicationUrl }"
                @click="goToFinancialDetail(item.id)"
              >
                <div class="item-content">
                  <div class="item-header">
                    <span class="item-title">{{ item.productName }}</span>
                  </div>

                  <div class="item-info-row">
                    <span class="rate-badge">{{ formatRateText(item) }}</span>
                    <span
                      v-if="item.loanLimit"
                      class="limit-text"
                    >
                      {{ formatLoanLimit(item.loanLimit) }}
                    </span>
                  </div>
                </div>
              </div>
            </template>

            <div
              v-else
              class="empty-text"
            >
              추천 금융 상품이 없습니다.
            </div>
          </div>
        </div>

        <div class="benefit-group">
          <div class="group-header">
            <div class="group-title">
              <Landmark
                :size="19"
                color="#a8842c"
              />
              <span>청년 · 정부 지원 정책</span>
            </div>
            <span class="badge">최대 {{ policyList?.length || 0 }}건</span>
          </div>

          <div
            class="policy-wrapper"
            :class="{ locked: !isProfileEntered }"
          >
            <div
              v-if="!isProfileEntered"
              class="lock-overlay"
            >
              <div class="lock-box">
                <div class="lock-icon-wrap">
                  <Lock
                    :size="15"
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

            <div class="benefit-list">
              <template v-if="policyList.length > 0">
                <div
                  v-for="item in policyList"
                  :key="item.id"
                  class="benefit-item"
                  :class="{ clickable: isProfileEntered }"
                  @click="isProfileEntered && goToPolicyDetail(item.id)"
                >
                  <div class="item-content">
                    <span class="item-title">
                      {{ item.title || item.polyBizSjnm }}
                    </span>
                    <p class="item-sub">
                      {{ item.sumDescription }}
                    </p>
                  </div>
                </div>
              </template>

              <div
                v-else
                class="empty-text"
              >
                조회된 지원 정책이 없습니다.
              </div>
            </div>
          </div>
        </div>
      </section>
    </simplebar>

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
          data-tour="detail-compare-btn"
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

    <OnboardingSpotlight
      group-name="property-detail"
      :steps="ONBOARDING_STEPS['property-detail']"
      return-to="/mypage"
    />
  </div>
</template>

<script setup>
import { watch, computed, onMounted, ref, onActivated, nextTick } from 'vue';
import { useRoute, useRouter, onBeforeRouteLeave } from 'vue-router';
import simplebar from 'simplebar-vue';
import OnboardingSpotlight from '@/components/OnboardingSpotlight.vue';
import { ONBOARDING_STEPS } from '@/constants/onboardingSteps';
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
  Landmark,
  Lock,
  Banknote,
} from 'lucide-vue-next';

// ----------------------------------------------------
// 1. KeepAlive 캐싱을 위한 컴포넌트 이름 명시
// ----------------------------------------------------
defineOptions({
  name: 'PropertyDetailView',
});

const route = useRoute();
const router = useRouter();

// p (매물 상세 정보)를 직접 reactive ref로 정의
const p = ref(null);
const isDetailLoading = ref(true);

const compareMsg = ref('');
const isSubmitting = ref(false);

const market = ref(null);
const isFavorite = ref(false);

const isProfileEntered = ref(false);
const policyList = ref([]);
const financeList = ref([]);
const isRecommendLoading = ref(false);

// 호수('202호' 등)를 정제한 건물명 Computed
const buildingName = computed(() => {
  const rawTitle = p.value?.title || p.value?.buildingName || p.value?.name;

  if (!rawTitle) return '건물명 정보 없음';

  return rawTitle.replace(/\s*\d+호$/, '').trim();
});

// ----------------------------------------------------
// 2. SimpleBar 스크롤 유지 및 복원 로직
// ----------------------------------------------------
const scrollArea = ref(null);
let savedScrollTop = 0;
let skipReload = false;

// SimpleBar 스크롤 타겟 HTML Element 추출
const getScrollElement = () => {
  if (!scrollArea.value) return null;
  if (typeof scrollArea.value.getScrollElement === 'function') {
    return scrollArea.value.getScrollElement();
  }
  const el = scrollArea.value.$el || scrollArea.value;
  return el?.querySelector?.('.simplebar-content-wrapper') || el;
};

// 라우트 이탈 시 스크롤 위치 및 복원 플래그 저장
onBeforeRouteLeave((to) => {
  // 💡 정책, 금융상품, 인프라, 안전정보 라우트 경로 매칭
  const isRelatedSubPage =
    /^\/properties\/\d+\/(infra|safety)/.test(to.path) ||
    /^\/policies\/\d+/.test(to.path) ||
    /^\/financial-products\/\d+/.test(to.path);

  skipReload = isRelatedSubPage;

  if (isRelatedSubPage) {
    savedScrollTop = getScrollElement()?.scrollTop ?? 0;
  } else {
    savedScrollTop = 0;
  }
});

// KeepAlive 캐시 복귀 시 스크롤 위치 적용
onActivated(async () => {
  if (skipReload) {
    skipReload = false;
    await nextTick();

    const targetEl = getScrollElement();
    if (targetEl) {
      targetEl.scrollTo({
        top: savedScrollTop,
        behavior: 'auto',
      });
    }
    return;
  }

  // 매물 리스트나 다른 메뉴 등 완전히 다른 페이지에서 들어왔을 때 최상단 초기화 및 재조회
  getScrollElement()?.scrollTo({ top: 0, behavior: 'auto' });
  fetchDetailAndRecommendations();
});

// ----------------------------------------------------
// 3. API 데이터 조회 (매물 상세 + 추천 데이터)
// ----------------------------------------------------
const fetchDetailAndRecommendations = async () => {
  const propertyId = route.params.id;
  if (!propertyId) return;

  // 온보딩 다시보기(?demo=1) — 실제 API를 안 타고 바로 예시 데이터로 채운다.
  if (route.query.demo === '1') {
    p.value = DEMO_PROPERTY;
    isDetailLoading.value = false;
    isProfileEntered.value = true;
    policyList.value = MOCK_POLICIES;
    financeList.value = MOCK_FINANCE;
    isRecommendLoading.value = false;
    return;
  }

  // 1) 매물 상세 데이터 직접 조회
  isDetailLoading.value = true;
  try {
    const res = await propertyApi.getPropertyDetail(propertyId);
    p.value = res?.data || res;
  } catch (e) {
    console.error('매물 정보 로드 실패:', e);
  } finally {
    isDetailLoading.value = false;
  }

  // 2) 맞춤 추천 상품 및 정책 조회
  try {
    isRecommendLoading.value = true;

    const [profile, financeRes] = await Promise.all([
      userApi.getProfile(),
      financeApi.getFinanceRecommendations(propertyId),
    ]);

    financeList.value = financeRes.data || financeRes || [];

    const hasBirthDate = !!profile?.birthDate?.trim();
    isProfileEntered.value = hasBirthDate;

    if (isProfileEntered.value) {
      const policyRes = await policyApi.getPolicyRecommendations(propertyId);
      policyList.value = policyRes.data || policyRes || [];
    } else {
      policyList.value = MOCK_POLICIES;
    }
  } catch (err) {
    console.error('추천 데이터 조회 실패:', err);
    isProfileEntered.value = false;
    policyList.value = MOCK_POLICIES;
  } finally {
    isRecommendLoading.value = false;
  }
};

onMounted(() => {
  fetchDetailAndRecommendations();
});

// ----------------------------------------------------
// 4. 페이지 이동 함수 (Query Parameter 및 Name 기반 이동)
// ----------------------------------------------------
const goToInfra = () => {
  router.push({
    name: 'property-infra',
    params: { id: p.value.id },
    query: {
      buildingName: buildingName.value,
      ...(route.query.demo === '1' ? { demo: '1' } : {}),
    },
  });
};

const goToSafety = () => {
  router.push({
    name: 'property-safety',
    params: { id: p.value.id },
    query: {
      buildingName: buildingName.value,
      ...(route.query.demo === '1' ? { demo: '1' } : {}),
    },
  });
};

const goToFinancialDetail = (id) => {
  if (!id) return;
  router.push({ name: 'financial-product-detail', params: { id } });
};

const goToPolicyDetail = (id) => {
  if (!id) return;
  router.push({ name: 'policy-detail', params: { id } });
};

// ----------------------------------------------------
// 5. 기타 비즈니스 및 포맷팅 로직
// ----------------------------------------------------
const MOCK_POLICIES = [
  {
    id: 'mock-1',
    title: '청년 주거 보증금 이자 지원',
    sub: '청년 가구 주거비 부담 완화를 위한 보증금 지원 정책',
  },
  {
    id: 'mock-2',
    title: '신혼부부 버팀목 전세자금 대출',
    sub: '신혼부부 대상 저금리 주택 구입 및 전세 자금 대출',
  },
  {
    id: 'mock-3',
    title: '청년 월세 한시 특별지원',
    sub: '무주택 청년 대상 월 최대 20만 원 월세 지원',
  },
];

watch(
  () => p.value?.isFavorite,
  (newVal) => {
    isFavorite.value = !!newVal;
  },
  { immediate: true },
);

// 온보딩 다시보기(?demo=1) 전용 예시 매물 — 실제 API를 안 타서 백엔드/DB 상태와 무관하게 항상 뜬다.
const DEMO_PROPERTY = {
  id: 'demo-1',
  title: 'e편한세상대전에코포레 2101호',
  propertyType: '아파트',
  tradeType: '전세',
  deposit: 35400,
  monthlyRent: 0,
  maintenanceFee: 13,
  areaM2: 84.97,
  floorInfo: '21/34층',
  address: '대전광역시 동구 용운로 203',
  dong: '101동',
  roomNum: 3,
  bathroomNum: 2,
  parkAvailability: true,
  propertyDescription:
    '깔끔하게 관리된 신축급 아파트예요. 채광이 좋고 지하철역이 가까워요.',
  moveInDate: '2026-08-31T00:00:00',
  availableDate: '2021-11-30T00:00:00',
  discussionStatus: false,
  recommendScore: 82,
  workplaceDistanceMeters: 1200,
  images: [],
  infraSummary: [],
  isFavorite: false,
  realtorPreview: { name: 'KB부동산공인중개사' },
};

const MOCK_FINANCE = [
  {
    id: 'mock-f1',
    productName: 'KB 청년 전월세보증금 대출',
    minRate: 2.5,
    maxRate: 3.6,
    loanLimit: '최대 2억원',
  },
];

const formatRateText = (item) => {
  if (item.minRate && item.maxRate) {
    return item.minRate === item.maxRate
      ? `연 ${item.minRate}%`
      : `연 ${item.minRate}% ~ ${item.maxRate}%`;
  }
  if (item.minRate) return `연 ${item.minRate}% ~`;
  if (item.maxRate) return `연 ~ ${item.maxRate}%`;

  if (item.rateDescription) {
    try {
      const parsed =
        typeof item.rateDescription === 'string' &&
        item.rateDescription.trim().startsWith('{')
          ? JSON.parse(item.rateDescription)
          : item.rateDescription;

      if (typeof parsed === 'object' && parsed !== null) {
        if (parsed.details && parsed.details.length > 0) {
          const rates = parsed.details.map((d) => d.final_rate).filter(Boolean);
          if (rates.length > 0) return rates.join(' / ');
        }
        if (parsed.base_type) return parsed.base_type;
      } else if (typeof parsed === 'string') {
        return parsed;
      }
    } catch (e) {
      // JSON 파싱 예외 무시
    }
  }

  return '변동금리 (상세 확인)';
};

const formatLoanLimit = (limitStr) => {
  if (!limitStr) return '';
  return limitStr.split('(')[0].trim();
};

const formatCommuteTime = (data) => {
  const SPEED_PER_MINUTE = 80;
  const actualDistance = data * 1.25;

  if (data === null || data === undefined) return '정보 없음';

  const minutes = Math.ceil(actualDistance / SPEED_PER_MINUTE);
  if (minutes === 0) return '1분 미만';

  return `약 ${minutes}분`;
};

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

// m단위 거리를 입력받아 1,000m 이상일 때 km로 변환하는 함수
const formatDistance = (meters) => {
  if (meters === null || meters === undefined || isNaN(meters)) return '0m';

  const num = Number(meters);

  if (num >= 1000) {
    // 1000m 이상인 경우 km 단위 변경 (소수점 첫째 자리까지 표시)
    return `${(num / 1000).toFixed(1)} km`;
  }

  // 1000m 미만인 경우 m 단위로 표시
  return `${num} m`;
};

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

const moveInLine = computed(() => {
  if (!p.value?.moveInDate) return '협의 가능';
  const formattedDate = formatDateStr(p.value.moveInDate);
  const isDiscussable = p.value?.discussionStatus ?? p.value?.discussion_status;
  const statusStr = isDiscussable ? '협의 가능' : '협의 불가능';

  return `${formattedDate} (${statusStr})`;
});

const availableLine = computed(() => {
  return formatDateStr(p.value?.availableDate);
});

const medianPrice = computed(() => market.value?.medianPrice ?? 47);

const infraData = computed(
  () => p.value?.infraSummary || p.value?.infraList || [],
);

const infraCategoryMap = new Map(
  INFRA_CATEGORIES.map((item) => [item.key, item.label]),
);
const amenityCategoryMap = new Map(
  AMENITY_CATEGORIES.map((item) => [item.key, item.label]),
);

const formattedInfraList = computed(() => {
  return infraData.value
    .filter((item) => infraCategoryMap.has(item.category))
    .map((item) => ({
      category: item.category,
      name: infraCategoryMap.get(item.category),
      count: item.count,
    }));
});

const formattedAmenityList = computed(() => {
  return infraData.value
    .filter((item) => amenityCategoryMap.has(item.category))
    .map((item) => ({
      category: item.category,
      name: amenityCategoryMap.get(item.category),
      count: item.count,
    }));
});

async function toggleFavorite() {
  if (!p.value?.id) return;
  if (route.query.demo === '1') {
    isFavorite.value = !isFavorite.value;
    return;
  }
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

async function addToCompare() {
  if (!p.value?.id || isSubmitting.value) return;

  if (route.query.demo === '1') {
    compareMsg.value = '비교함에 담았어요.';
    setTimeout(() => {
      compareMsg.value = '';
    }, 2500);
    return;
  }

  try {
    isSubmitting.value = true;

    await comparisonApi.addToBox(p.value.id);
    compareMsg.value = '비교함에 담았어요.';

    if (route.query.returnTo === 'compare-box') {
      await router.replace('/compare-box');
      return;
    }
  } catch (error) {
    console.error('비교함 담기 실패:', error);

    if (error.code === 'COMPARE_409_1') {
      compareMsg.value = error.message;
    } else if (error.code === 'COMPARE_409_2') {
      compareMsg.value = error.message;
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

.demo-banner {
  margin: 12px 16px 0;
  padding: 9px 12px;
  background: #f1efea;
  border: 1px dashed var(--kb-silver);
  border-radius: 10px;
  font-size: 11.5px;
  font-weight: 700;
  color: var(--kb-gray);
  text-align: center;
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

.benefit-group {
  margin-bottom: 24px;
}

/* 제목과 건수 배지를 양 끝 정렬 */
.group-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.group-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 16px;
  font-weight: 700;
  color: #222;
}

/* 개별 건수 배지 스타일 */
.badge {
  font-size: 12.5px;
  font-weight: 600;
  color: #2b6cb0;
  background-color: #ebf8ff;
  padding: 4px 10px;
  border-radius: 12px;
}
.benefit-list {
  display: flex;
  flex-direction: column;
  gap: 10px; /* 각 카드 사이 간격 넓힘 */
  margin-top: 8px;
}

.benefit-item {
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
