<template>
  <div class="reg">
    <div class="step-head">
      <p class="step-line">
        <span class="step-no">STEP {{ step }} / 4</span>
        <span class="dot">·</span>
        <span class="step-label">{{ stepLabels[step - 1] }}</span>
      </p>
      <div class="progress">
        <div v-for="n in 4" :key="n" class="seg" :class="{ on: n <= step }" />
      </div>
    </div>

    <div v-if="step === 1" class="content">
      <div class="field">
        <label class="section-title"
          >도로명 주소<span class="req">*</span></label
        >
        <input
          class="field-input"
          type="text"
          readonly
          :value="form.address?.roadAddress ?? ''"
          placeholder="예) 서울 강남구 테헤란로 152"
          @click="isAddressOpen = true"
        />
        <div class="pair-row">
          <input
            class="field-input dong-input"
            type="text"
            readonly
            :value="form.aptDong ?? ''"
            :placeholder="dongOptions.length ? '예) 101동' : '동 없음'"
            :disabled="!dongOptions.length"
            @click="dongOptions.length && (isDongOpen = true)"
          />
          <span class="slash">/</span>
          <input
            v-model="form.roomNumber"
            class="field-input"
            type="number"
            min="1"
            placeholder="예) 101"
          />
          <span class="unit">호</span>
        </div>
      </div>

      <div class="field">
        <label class="section-title">
          해당층 <span class="slash-label">/</span> 건물층<span class="req"
            >*</span
          >
        </label>
        <div class="chips">
          <button
            v-for="t in FLOOR_TYPES"
            :key="t"
            class="chip"
            :class="{ on: form.floorType === t }"
            @click="form.floorType = t"
          >
            {{ t }}
          </button>
        </div>
        <div class="pair-row">
          <input
            v-if="form.floorType === '지상'"
            v-model="form.floor"
            class="field-input"
            type="number"
            min="1"
            placeholder="예) 1"
          />

          <div v-else-if="form.floorType === '지하'" class="prefix-input">
            <span class="prefix">지하</span>
            <input
              v-model="form.floor"
              type="number"
              min="1"
              placeholder="예) 1"
            />
          </div>
          <div v-else class="fixed-input">{{ form.floorType }}</div>
          <span class="slash">/</span>
          <input
            v-model="form.totalFloor"
            class="field-input"
            type="number"
            min="1"
            placeholder="예) 10"
          />
          <span class="unit">층</span>
        </div>
      </div>

      <div class="field">
        <label class="section-title">건물 이름</label>
        <input
          v-model="form.buildingName"
          class="field-input"
          type="text"
          placeholder="예) ○○아파트"
        />
      </div>

      <div class="field">
        <label class="section-title">매물 유형<span class="req">*</span></label>
        <div class="chips">
          <button
            v-for="t in PROPERTY_TYPES"
            :key="t"
            class="chip"
            :class="{ on: form.propertyType === t }"
            @click="form.propertyType = t"
          >
            {{ t }}
          </button>
        </div>
      </div>
    </div>

    <div v-else-if="step === 2" class="content">
      <div class="field">
        <label class="section-title">
          면적(m²)<span class="req">*</span>
          <span v-if="pyeong" class="pyeong">≈ {{ pyeong }}평</span>
        </label>
        <input
          v-model="form.areaM2"
          class="field-input"
          type="number"
          step="0.01"
          min="0"
          placeholder="예) 26.94"
        />
      </div>

      <div class="field">
        <label class="section-title">
          방 수 <span class="slash-label">/</span> 욕실 수<span class="req"
            >*</span
          >
        </label>
        <div class="pair-row">
          <input
            v-model="form.roomNum"
            class="field-input"
            type="number"
            min="1"
            placeholder="예) 3"
          />
          <span class="slash">/</span>
          <input
            v-model="form.bathroomNum"
            class="field-input"
            type="number"
            min="1"
            placeholder="예) 1"
          />
          <span class="unit">개</span>
        </div>
      </div>

      <div class="field">
        <label class="section-title">주차 가능 여부</label>
        <div class="check-row">
          <label class="check-item" @click="form.parkAvailability = true">
            <span class="checkbox" :class="{ on: form.parkAvailability }">
              <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
                <path
                  d="M2 6.5L4.7 9L10 3.5"
                  stroke="#545045"
                  stroke-width="1.8"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
            </span>
            주차 가능
          </label>
          <label class="check-item" @click="form.parkAvailability = false">
            <span class="checkbox" :class="{ on: !form.parkAvailability }">
              <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
                <path
                  d="M2 6.5L4.7 9L10 3.5"
                  stroke="#545045"
                  stroke-width="1.8"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
            </span>
            주차 불가능
          </label>
        </div>
      </div>

      <div class="field">
        <div class="label-row">
          <label class="section-title"
            >입주가능일<span class="req">*</span></label
          >
          <label
            class="check-item small"
            @click="form.discussionStatus = !form.discussionStatus"
          >
            <span class="checkbox sm" :class="{ on: form.discussionStatus }">
              <svg width="10" height="10" viewBox="0 0 12 12" fill="none">
                <path
                  d="M2 6.5L4.7 9L10 3.5"
                  stroke="#545045"
                  stroke-width="1.8"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
            </span>
            협의 가능
          </label>
        </div>
        <DatePicker v-model="form.moveInDate" title="입주가능일" />
      </div>

      <div class="field">
        <label class="section-title"
          >사용승인일<span class="req">*</span></label
        >
        <DatePicker v-model="form.availableDate" title="사용승인일" />
      </div>
    </div>

    <div v-else-if="step === 3" class="content">
      <div class="field">
        <label class="section-title">거래 유형<span class="req">*</span></label>
        <div class="chips">
          <button
            v-for="t in TRADE_TYPES"
            :key="t"
            class="chip"
            :class="{ on: form.tradeType === t }"
            @click="form.tradeType = t"
          >
            {{ t }}
          </button>
        </div>
      </div>

      <div v-if="form.tradeType === '월세'" class="field">
        <label class="section-title"
          >보증금(만원)<span class="req">*</span></label
        >
        <input
          v-model="form.deposit"
          class="field-input"
          type="number"
          min="0"
          placeholder="예) 500"
        />
      </div>
      <div v-if="form.tradeType === '월세'" class="field">
        <label class="section-title"
          >월세(만원)<span class="req">*</span></label
        >
        <input
          v-model="form.monthlyRent"
          class="field-input"
          type="number"
          min="0"
          placeholder="예) 45"
        />
      </div>

      <div v-if="form.tradeType === '전세'" class="field">
        <label class="section-title"
          >전세금(만원)<span class="req">*</span></label
        >
        <input
          v-model="form.deposit"
          class="field-input"
          type="number"
          min="0"
          placeholder="예) 9500"
        />
      </div>

      <div v-if="form.tradeType === '매매'" class="field">
        <label class="section-title"
          >매매가(만원)<span class="req">*</span></label
        >
        <input
          v-model="form.deposit"
          class="field-input"
          type="number"
          min="0"
          placeholder="예) 25000"
        />
      </div>

      <div class="field">
        <label class="section-title">관리비(만원)</label>
        <input
          v-model="form.maintenanceFee"
          class="field-input"
          type="number"
          min="0"
          placeholder="예) 7"
        />
      </div>
    </div>

    <div v-else class="content">
      <div class="field">
        <label class="section-title">매물 사진</label>
        <button class="photo-add" @click="addPhoto">
          <span class="plus">+</span>
          <span class="photo-label"
            >사진 추가 ({{ form.photos.length }}/10)</span
          >
        </button>
      </div>
      <div class="field">
        <label class="section-title">매물 상세 설명</label>
        <textarea
          v-model="form.propertyDescription"
          class="field-input textarea"
          rows="6"
          placeholder="예) 문의 주세요.&#10;010-0000-0000"
        />
      </div>
    </div>

    <div class="bottom-bar">
      <button v-if="step > 1" class="btn-prev" @click="go(step - 1)">
        이전
      </button>
      <button
        class="btn-cta"
        :disabled="!canNext || isSubmitting || (isEditMode && step === 4)"
        @click="onNext"
      >
        {{
          isSubmitting
            ? '등록 중...'
            : step === 4
              ? isEditMode
                ? '수정 기능 준비 중'
                : '매물 등록하기'
              : '다음'
        }}
      </button>
    </div>
  </div>

  <AddressSearchSheet
    :open="isAddressOpen"
    @close="isAddressOpen = false"
    @select="onAddressSelect"
  />

  <AptDongSheet
    :open="isDongOpen"
    :address="form.address"
    :dongs="dongOptions"
    @close="isDongOpen = false"
    @select="onDongSelect"
    @change-address="openAddressFromDong"
  />
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import AddressSearchSheet from '@/components/AddressSearchSheet.vue';
import AptDongSheet from '@/components/AptDongSheet.vue';
import DatePicker from '@/components/DatePicker.vue';
import { locationApi, sellerApi } from '@/api/services';

const route = useRoute();
const router = useRouter();

const step = computed(() => Number(route.params.step) || 1);
const propertyId = computed(() => route.query.propertyId ?? null);
const isEditMode = computed(() => Boolean(propertyId.value));
const stepLabels = ['위치·건물 정보', '상세 스펙', '거래·가격', '사진·설명'];

const PROPERTY_TYPES = ['원룸', '아파트', '주택/빌라', '오피스텔'];
const TRADE_TYPES = ['월세', '전세', '매매'];
const FLOOR_TYPES = ['지하', '반지하', '지상', '옥탑'];

const form = reactive({
  address: null,
  aptDong: null,
  roomNumber: '',
  buildingName: '',
  propertyType: '원룸',

  floorType: '지상',
  floor: '',
  totalFloor: '',

  areaM2: '',
  roomNum: '',
  bathroomNum: '',
  parkAvailability: true,
  moveInDate: '',
  availableDate: '',
  discussionStatus: true,

  tradeType: '월세',
  deposit: '',
  monthlyRent: '',
  maintenanceFee: '',

  photos: [],
  propertyDescription: '',
});

const isAddressOpen = ref(false);
const isDongOpen = ref(false);
const dongOptions = ref([]);
const isSubmitting = ref(false);

onMounted(loadPropertyForEdit);

const pyeong = computed(() => {
  const v = parseFloat(form.areaM2);
  if (!v || v <= 0) return '';
  return Math.round((v / 3.3058) * 10) / 10;
});

const canNext = computed(() => {
  if (step.value === 1) {
    const floorFilled =
      ['반지하', '옥탑'].includes(form.floorType) || form.floor !== '';
    return Boolean(form.address) && floorFilled && form.totalFloor !== '';
  }
  if (step.value === 2) {
    return (
      form.areaM2 !== '' &&
      form.roomNum !== '' &&
      form.bathroomNum !== '' &&
      form.moveInDate !== '' &&
      form.availableDate !== ''
    );
  }
  if (step.value === 3) {
    if (form.deposit === '') return false;
    return form.tradeType !== '월세' || form.monthlyRent !== '';
  }
  return true;
});

function go(n) {
  router.push({ path: `/seller/register/${n}`, query: route.query });
}

async function loadPropertyForEdit() {
  if (!propertyId.value) return;

  try {
    const data = await sellerApi.myProperty(propertyId.value);
    const payload = data?.data ?? data;
    fillForm(payload);
  } catch (error) {
    alert(error.response?.data?.message ?? '매물 정보를 불러오지 못했어요.');
    router.push('/seller/properties');
  }
}

function fillForm(item) {
  form.address = {
    roadAddress: item.roadAddress || item.address || '',
    jibunAddress: item.jibunAddress || item.address || '',
    buildingName: item.buildingName || '',
    lat: item.lat,
    lng: item.lng,
  };
  form.aptDong = item.dong || null;
  form.roomNumber = String(item.roomNumber || '').replace(/호$/, '');
  form.buildingName = item.buildingName || '';
  form.propertyType = item.propertyType || '원룸';
  applyFloorInfo(item.floorInfo);
  form.areaM2 = item.areaM2 ?? '';
  form.roomNum = item.roomNum ?? '';
  form.bathroomNum = item.bathroomNum ?? '';
  form.parkAvailability = Boolean(item.parkAvailability);
  form.moveInDate = item.moveInDate || '';
  form.availableDate = item.availableDate || '';
  form.discussionStatus = Boolean(item.discussionStatus);
  form.tradeType = item.tradeType || '월세';
  form.deposit = item.deposit ?? '';
  form.monthlyRent = item.monthlyRent ?? '';
  form.maintenanceFee = item.maintenanceFee ?? '';
  form.photos = [...(item.imageUrls || [])];
  form.propertyDescription = item.propertyDescription || '';
}

function applyFloorInfo(floorInfo) {
  const [floor = '', total = ''] = String(floorInfo || '').split('/');
  const floorValue = floor.trim();
  form.totalFloor = total.replace(/층/g, '').trim();
  if (floorValue === '반지하' || floorValue === '옥탑') {
    form.floorType = floorValue;
    form.floor = '';
  } else if (floorValue.startsWith('지하')) {
    form.floorType = '지하';
    form.floor = floorValue.replace('지하', '').trim();
  } else {
    form.floorType = '지상';
    form.floor = floorValue;
  }
}

async function onAddressSelect(addr) {
  form.address = addr;
  form.aptDong = null;
  form.buildingName = addr.buildingName ?? '';
  isAddressOpen.value = false;
  dongOptions.value = [];

  if (!addr.sigunguCd) return;

  try {
    const res = await locationApi.searchDongs({
      sigunguCd: addr.sigunguCd,
      bjdongCd: addr.bjdongCd,
      platGbCd: addr.platGbCd,
      bun: addr.bun,
      ji: addr.ji,
    });
    dongOptions.value = res.data ?? [];
  } catch {
    dongOptions.value = [];
  }

  if (dongOptions.value.length > 0) {
    isDongOpen.value = true;
  }
}

function onDongSelect(dong) {
  form.aptDong = dong;
  isDongOpen.value = false;
}

function openAddressFromDong() {
  isDongOpen.value = false;
  isAddressOpen.value = true;
}

function addPhoto() {
  if (form.photos.length < 10) form.photos.push(`photo-${Date.now()}`);
}

function buildFloorInfo() {
  const total = form.totalFloor;
  if (form.floorType === '지상') return `${form.floor} / ${total}층`;
  if (form.floorType === '지하') return `지하${form.floor} / ${total}층`;
  if (form.floorType === '반지하') return `반지하 / ${total}층`;
  return `옥탑 / ${total}층`;
}

function buildPayload() {
  return {
    title: `${form.address?.dongName ?? ''} ${form.propertyType}`.trim(),
    propertyType: form.propertyType,
    tradeType: form.tradeType,
    deposit: Number(form.deposit),
    monthlyRent: form.tradeType === '월세' ? Number(form.monthlyRent) : 0,
    maintenanceFee: Number(form.maintenanceFee || 0),
    areaM2: Number(form.areaM2),
    floorInfo: buildFloorInfo(),
    roomNum: Number(form.roomNum),
    bathroomNum: Number(form.bathroomNum),
    parkAvailability: form.parkAvailability,
    discussionStatus: form.discussionStatus,
    dong: form.aptDong,
    roomNumber: form.roomNumber,
    propertyDescription: form.propertyDescription,
    moveInDate: form.moveInDate || null,
    availableDate: form.availableDate || null,
    location: {
      address: form.address?.roadAddress ?? form.address?.jibunAddress,
      buildingName: form.buildingName,
      lat: form.address?.lat,
      lng: form.address?.lng,
    },
    imageUrls: form.photos,
  };
}

async function onNext() {
  if (step.value < 4) {
    go(step.value + 1);
    return;
  }
  if (isEditMode.value) return;

  isSubmitting.value = true;
  try {
    await sellerApi.create(buildPayload());
    alert('매물이 등록되었어요. 주변 인프라·안전 정보는 자동으로 계산돼요.');
    router.push('/seller/properties');
  } catch (e) {
    alert(
      e.response?.data?.message ??
        '등록에 실패했어요. 잠시 후 다시 시도해주세요.',
    );
  } finally {
    isSubmitting.value = false;
  }
}
</script>

<style scoped>
.reg {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--white);
}
.step-head {
  padding: 14px 16px 12px;
}
.step-line {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
}
.step-no {
  color: #d9a800;
  font-weight: 700;
  letter-spacing: 0.4px;
}
.dot {
  color: var(--kb-silver);
}
.step-label {
  color: var(--text-primary);
  font-weight: 500;
}
.progress {
  display: flex;
  gap: 6px;
  margin-top: 10px;
}
.seg {
  flex: 1;
  height: 4px;
  border-radius: 2px;
  background: var(--border);
}
.seg.on {
  background: var(--kb-yellow);
}
.content {
  flex: 1;
  padding: 10px 16px 24px;
  display: flex;
  flex-direction: column;
  gap: 24px;
  overflow-y: auto;
}
.section-title {
  font-size: 16px;
  font-weight: 800;
}
.slash-label {
  color: var(--kb-silver);
  font-weight: 400;
}
.pyeong {
  margin-left: 8px;
  font-size: 13px;
  font-weight: 500;
  color: var(--kb-silver);
}
.req {
  color: var(--danger);
  margin-left: 1px;
}
.field {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.label-row {
  display: flex;
  align-items: center;
  gap: 14px;
}
.pair-row {
  display: flex;
  align-items: center;
  gap: 10px;
}
.pair-row .field-input {
  flex: 1;
  min-width: 0;
}
.dong-input {
  background: var(--bg);
}
.dong-input:disabled {
  color: var(--kb-silver);
  cursor: default;
}
.slash {
  color: var(--kb-silver);
  font-size: 15px;
}
.unit {
  font-size: 14px;
  font-weight: 700;
  flex-shrink: 0;
}
.chips {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.chip {
  padding: 8px 14px;
  border-radius: 100px;
  border: 1px solid var(--border);
  background: var(--white);
  font-size: 13px;
  cursor: pointer;
}
.chip.on {
  background: var(--yellow-tint);
  border-color: var(--kb-yellow);
  font-weight: 700;
}
/* 지하: 접두 라벨 입력 (25-1-a) */
.prefix-input {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 6px;
  height: 46px;
  padding: 0 14px;
  border: 1px solid var(--border);
  border-radius: var(--radius-input);
  background: var(--white);
  min-width: 0;
}
.prefix-input .prefix {
  font-size: 14px;
  font-weight: 700;
  flex-shrink: 0;
}
.prefix-input input {
  flex: 1;
  border: none;
  font-size: 14px;
  min-width: 0;
}

.fixed-input {
  flex: 1;
  display: flex;
  align-items: center;
  height: 46px;
  padding: 0 14px;
  border: 1px solid var(--border);
  border-radius: var(--radius-input);
  background: var(--bg);
  color: var(--kb-gray);
  font-size: 14px;
}
.check-row {
  display: flex;
  gap: 28px;
}
.check-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13.5px;
  cursor: pointer;
}
.check-item.small {
  font-size: 12.5px;
}
.checkbox {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 6px;
  border: 1.5px solid #d8d5cf;
  background: var(--white);
  flex-shrink: 0;
}
.checkbox.sm {
  width: 18px;
  height: 18px;
}
.checkbox svg {
  opacity: 0;
}
.checkbox.on {
  background: var(--kb-yellow);
  border-color: var(--kb-yellow);
}
.checkbox.on svg {
  opacity: 1;
}
.photo-add {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  width: 100%;
  height: 84px;
  border: 1px solid var(--border);
  border-radius: 14px;
  background: var(--white);
  cursor: pointer;
}
.plus {
  font-size: 20px;
  color: var(--kb-gray);
}
.photo-label {
  font-size: 12.5px;
  color: var(--kb-silver);
}
.textarea {
  height: auto;
  padding: 12px 14px;
  resize: none;
  line-height: 1.6;
}
.bottom-bar {
  display: flex;
  gap: 10px;
  padding: 12px 16px 16px;
  border-top: 1px solid var(--border);
  background: var(--white);
  position: sticky;
  bottom: 0;
}
.btn-prev {
  flex: 0 0 88px;
  height: 46px;
  border-radius: 14px;
  border: 1px solid var(--border);
  background: var(--white);
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
}
</style>
