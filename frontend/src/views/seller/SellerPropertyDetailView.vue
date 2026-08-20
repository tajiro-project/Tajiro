<template>
  <div class="seller-detail">
    <simplebar class="content">
      <template v-if="detail">
        <div
          class="photo-slider"
          @touchstart="handleTouchStart"
          @touchend="handleTouchEnd"
        >
          <template v-if="images.length">
            <img
              :src="images[currentImgIndex]"
              :alt="`매물 이미지 ${currentImgIndex + 1}`"
              class="photo-img"
            />
            <button
              v-if="images.length > 1 && isOpen"
              class="slide-btn left"
              type="button"
              aria-label="이전 사진"
              @click="prevImage"
            >
              <ChevronLeft :size="20" />
            </button>
            <button
              v-if="images.length > 1 && isOpen"
              class="slide-btn right"
              type="button"
              aria-label="다음 사진"
              @click="nextImage"
            >
              <ChevronRight :size="20" />
            </button>
            <div v-if="images.length > 1 && isOpen" class="dots">
              <button
                v-for="(_, index) in images"
                :key="index"
                class="dot"
                :class="{ on: index === currentImgIndex }"
                type="button"
                :aria-label="`${index + 1}번째 사진`"
                @click="currentImgIndex = index"
              />
            </div>
          </template>
          <div v-else class="photo-placeholder">
            <ImageIcon :size="44" color="#8a8477" />
            <p>등록된 이미지가 없습니다.</p>
          </div>
          <!-- 거래 완료 오버레이 (transactionStatus가 false일 때 표시) -->
          <div v-if="!isOpen" class="completed-overlay">
            <div class="completed-badge">
              <CheckCircle :size="18" stroke-width="2.5" />
              <span>거래 완료</span>
            </div>
            <p class="completed-text">계약이 완료된 매물입니다</p>
          </div>
        </div>

        <div class="head">
          <h1 class="price">{{ priceLabel(detail) }}</h1>
          <p class="addr">{{ regionLine }}</p>
        </div>

        <section class="card">
          <p class="card-head">매물 정보</p>
          <div class="tag-row">
            <span class="tag yellow">{{ detail.propertyType }}</span>
            <span class="tag yellow">{{ detail.tradeType }}</span>
          </div>

          <dl class="info-list">
            <div v-for="row in propertyRows" :key="row.label" class="info-row">
              <dt>{{ row.label }}</dt>
              <dd>{{ row.value }}</dd>
            </div>
          </dl>

          <hr class="section-divider" />
          <p class="desc-head">매물 상세 설명</p>
          <p class="desc">
            {{ detail.propertyDescription || '등록된 설명이 없어요' }}
          </p>
        </section>

        <section class="card management-card">
          <p class="card-head">판매 관리 정보</p>
          <p class="card-sub">구매자 상세 화면에는 표시되지 않는 정보예요.</p>
          <dl class="info-list">
            <div v-for="row in managementRows" :key="row.label" class="info-row">
              <dt>{{ row.label }}</dt>
              <dd>{{ row.value }}</dd>
            </div>
          </dl>
        </section>
      </template>

      <p v-else-if="errorMessage" class="empty">{{ errorMessage }}</p>
    </simplebar>

    <div v-if="detail" class="action-bar">
      <button class="action" type="button" @click="goEdit">수정</button>
      <button
        class="action"
        :class="isOpen ? 'to-done' : 'to-open'"
        type="button"
        :disabled="busy"
        @click="toggleStatus"
      >
        {{ isOpen ? '거래완료' : '게시' }}
      </button>
      <button
        class="action danger"
        type="button"
        :disabled="busy"
        @click="removeProperty"
      >
        삭제
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import simplebar from 'simplebar-vue';
import {
  CheckCircle,
  ChevronLeft,
  ChevronRight,
  Image as ImageIcon,
} from 'lucide-vue-next';
import { getApiErrorMessage } from '@/api/client';
import { sellerApi } from '@/api/services';
import {
  manwon,
  priceLabel,
  transactionPriceLabel,
} from '@/utils/sellerProperty';

const PYEONG = 3.3058;

const route = useRoute();
const router = useRouter();
const propertyId = route.params.id;

const detail = ref(null);
const errorMessage = ref('');
const busy = ref(false);
const currentImgIndex = ref(0);
let touchStartX = 0;

const isOpen = computed(() => detail.value?.transactionStatus !== false);

const images = computed(() => detail.value?.imageUrls ?? []);

const regionLine = computed(() => {
  if (!detail.value) return '';

  const address = detail.value.roadAddress || detail.value.address || '';
  const addressPart = address.split(' ').slice(0, 3).join(' ');
  const dongPart = detail.value.dong ? ` ${detail.value.dong}` : '';
  const areaPart = detail.value.areaM2
    ? `${Math.round(detail.value.areaM2 / 3.3)}평`
    : '';

  return [
    `${addressPart}${dongPart}`.trim(),
    [detail.value.propertyType, areaPart].filter(Boolean).join(' '),
    summaryFloorLabel(detail.value.floorInfo),
  ]
    .filter(Boolean)
    .join(' · ');
});

const propertyRows = computed(() =>
  rowsOf([
    ['건물명', detail.value.buildingName || '정보 없음'],
    ['거래 · 가격', transactionPriceLabel(detail.value)],
    [
      '관리비',
      detail.value.maintenanceFee
        ? `월 ${manwon(detail.value.maintenanceFee)}`
        : '월 0만원',
    ],
    ['도로명 주소', detail.value.roadAddress || detail.value.address],
    ['층수', summaryFloorLabel(detail.value.floorInfo)],
    ['면적', areaLabel(detail.value.areaM2)],
    ['방 · 욕실', countLabel(detail.value.roomNum, detail.value.bathroomNum)],
    ['주차', detail.value.parkAvailability ? '가능' : '불가'],
    [
      '입주 가능일',
      detail.value.discussionStatus
        ? '협의 가능'
        : formatDate(detail.value.moveInDate) || '정보 없음',
    ],
    ['사용 승인일', formatDate(detail.value.availableDate) || '정보 없음'],
  ]),
);

const managementRows = computed(() =>
  rowsOf([
    ['동 / 호수', dongHo(detail.value)],
    ['지번 주소', detail.value.jibunAddress],
    ['게시 상태', isOpen.value ? '게시중' : '거래완료'],
    ['최근 수정', formatDateTime(detail.value.updateDate)],
  ]),
);

onMounted(loadDetail);

async function loadDetail() {
  try {
    const data = await sellerApi.myProperty(propertyId);
    detail.value = data?.data ?? data;
  } catch (error) {
    errorMessage.value = getApiErrorMessage(
      error,
      '매물 정보를 불러오지 못했어요.',
    );
  }
}

/** 값이 없는 행은 표에서 뺀다 */
function rowsOf(pairs) {
  return pairs
    .filter(([, value]) => value != null && value !== '')
    .map(([label, value]) => ({ label, value }));
}

function dongHo(item) {
  return [item.dong, item.roomNumber].filter(Boolean).join(' ') || null;
}

function floorLabel(floorInfo) {
  if (!floorInfo) return null;

  const [current, total] = String(floorInfo)
    .split('/')
    .map((v) => v.trim());
  const currentLabel = /^\d+$/.test(current) ? `${current}층` : current;
  return total ? `${currentLabel} / ${total}층` : currentLabel;
}

function summaryFloorLabel(floorInfo) {
  if (!floorInfo) return '';

  const [current, total] = String(floorInfo)
    .split('/')
    .map((value) => value.trim());
  const currentFloor = current.replace(/[^0-9-]/g, '');
  const totalFloor = total?.replace(/[^0-9]/g, '');

  if (!totalFloor) return current;
  return `${currentFloor}층 / 총 ${totalFloor}층`;
}

function areaLabel(areaM2) {
  if (areaM2 == null) return null;
  return `${areaM2} m² (${Math.floor(areaM2 / PYEONG)}평)`;
}

function countLabel(rooms, bathrooms) {
  const parts = [];
  if (rooms != null) parts.push(`${rooms}개`);
  if (bathrooms != null) parts.push(`${bathrooms}개`);
  return parts.length ? parts.join(' / ') : null;
}

function formatDate(value) {
  if (!value) return '';

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '';

  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}

function formatDateTime(value) {
  if (!value) return null;

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return null;

  const datePart = formatDate(date);
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  return `${datePart} ${hours}:${minutes}`;
}

function goEdit() {
  router.push(`/seller/register/1?propertyId=${propertyId}`);
}

async function toggleStatus() {
  const next = !isOpen.value;
  busy.value = true;
  try {
    await sellerApi.changeStatus(propertyId, next);
    detail.value.transactionStatus = next;
  } catch (error) {
    alert(getApiErrorMessage(error, '매물 상태를 변경하지 못했어요.'));
  } finally {
    busy.value = false;
  }
}

async function removeProperty() {
  if (!confirm('이 매물을 삭제할까요? 삭제하면 목록에서 사라집니다.')) return;

  busy.value = true;
  try {
    await sellerApi.remove(propertyId);
    router.replace('/seller/properties');
  } catch (error) {
    alert(getApiErrorMessage(error, '매물을 삭제하지 못했어요.'));
  } finally {
    busy.value = false;
  }
}

function nextImage() {
  if (images.value.length < 2) return;
  currentImgIndex.value = (currentImgIndex.value + 1) % images.value.length;
}

function prevImage() {
  if (images.value.length < 2) return;
  currentImgIndex.value =
    (currentImgIndex.value - 1 + images.value.length) % images.value.length;
}

function handleTouchStart(event) {
  touchStartX = event.changedTouches[0]?.clientX ?? 0;
}

function handleTouchEnd(event) {
  const distance = (event.changedTouches[0]?.clientX ?? 0) - touchStartX;
  if (Math.abs(distance) < 40) return;
  if (distance < 0) nextImage();
  else prevImage();
}
</script>

<style scoped>
.seller-detail {
  position: relative;
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow: hidden;
  background: var(--bg);
}

.content {
  flex: 1;
  min-height: 0;
  padding-bottom: 20px;
}

.content :deep(.simplebar-content) {
  display: flex;
  flex-direction: column;
}

.photo-slider {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 100%;
  height: 220px;
  overflow: hidden;
  background: #f4f1ea;
}

.photo-img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.photo-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #8a8477;
  font-size: 13px;
}

.slide-btn {
  position: absolute;
  top: 50%;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  padding: 0;
  border: 0;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.3);
  color: #fff;
  cursor: pointer;
  transform: translateY(-50%);
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
  z-index: 1;
  display: flex;
  gap: 6px;
}

.dot {
  width: 6px;
  height: 6px;
  padding: 0;
  border: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.5);
}

.dot.on {
  width: 14px;
  border-radius: 4px;
  background: #ffbc00;
}

/* 거래 완료 상태 오버레이 (transactionStatus === false)
   구매자 매물 상세(PropertyDetailView)와 같은 값을 쓴다 */
.completed-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.55);
  backdrop-filter: blur(2px);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  z-index: 5;
}

.completed-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  background: #ffffff;
  color: #222222;
  padding: 8px 20px;
  border-radius: 100px;
  font-size: 15px;
  font-weight: 800;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.completed-text {
  color: #ffffff;
  font-size: 13px;
  font-weight: 500;
  letter-spacing: -0.3px;
  margin: 0;
}

.head {
  padding: 16px;
  background: #fff;
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
  border: 1px solid #ddd;
  border-radius: 100px;
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
  min-width: 0;
  font-size: 13px;
  font-weight: 500;
}

.section-divider {
  margin: 16px 0 12px;
  border: 0;
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
  white-space: pre-line;
}

.management-card .info-list {
  margin-top: 12px;
}

.empty {
  padding: 40px 0;
  font-size: 12.5px;
  color: var(--kb-silver);
  text-align: center;
}

.action-bar {
  position: relative;
  z-index: 2;
  display: flex;
  gap: 8px;
  flex-shrink: 0;
  padding: 10px 16px calc(14px + env(safe-area-inset-bottom));
  background: var(--white);
  border-top: 1px solid var(--border);
}

.action {
  flex: 1;
  min-width: 0;
  padding: 13px 0;
  background: var(--white);
  border: 1px solid #dadada;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 700;
  color: var(--kb-gray);
  cursor: pointer;
}

.action:disabled {
  opacity: 0.5;
  cursor: default;
}

.action.to-done {
  border-color: #3351ca;
  color: #3351ca;
}

.action.to-open {
  border-color: #85a692;
  color: #21874a;
}

.action.danger {
  border-color: #edc7c7;
  color: #d64545;
}
</style>
