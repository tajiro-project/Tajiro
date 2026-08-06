<template>
  <div class="guide">
    <PageHeader title="안전 거래 가이드" />

    <div class="scroll-area">
      <section class="intro-card">
        <p class="intro-title">전세사기·계약 실수, 미리 막아요</p>
        <p class="intro-sub">
          계약 전부터 잔금일까지 꼭 확인해야 할 내용을 모았어요.
        </p>
      </section>

      <div class="tabs" role="tablist" aria-label="안전 거래 가이드 종류">
        <button
          class="tab"
          :class="{ on: tab === 'contract' }"
          type="button"
          role="tab"
          :aria-selected="tab === 'contract'"
          @click="tab = 'contract'"
        >
          계약
        </button>
        <button
          class="tab"
          :class="{ on: tab === 'insurance' }"
          type="button"
          role="tab"
          :aria-selected="tab === 'insurance'"
          @click="tab = 'insurance'"
        >
          전세보증보험
        </button>
      </div>

      <div class="section-intro">
        <p class="heading">{{ currentGuide.title }}</p>
        <p class="heading-sub">{{ currentGuide.description }}</p>
      </div>

      <section
        v-for="(section, sectionIndex) in currentGuide.sections"
        :key="section.title"
        class="guide-section"
      >
        <div class="section-head">
          <span class="section-step">{{ sectionIndex + 1 }}</span>
          <h2>{{ section.title }}</h2>
        </div>

        <ul class="guide-list">
          <li v-for="item in section.items" :key="item">
            <span class="item-dot" aria-hidden="true" />
            <p>{{ item }}</p>
          </li>
        </ul>
      </section>

      <section v-if="tab === 'insurance'" class="links-card">
        <div class="links-title">
          <span class="links-icon" aria-hidden="true">↗</span>
          <div>
            <p>공식 사이트에서 확인하기</p>
            <span>필요한 정보를 직접 조회해보세요.</span>
          </div>
        </div>
        <a
          v-for="link in links"
          :key="link.url"
          class="link-row"
          :href="link.url"
          target="_blank"
          rel="noopener noreferrer"
        >
          {{ link.label }}
          <svg width="13" height="13" viewBox="0 0 13 13" fill="none">
            <path
              d="M3.5 9.5L9.5 3.5M5 3.5h4.5V8"
              stroke="currentColor"
              stroke-width="1.4"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
        </a>
      </section>

      <aside class="notice-card">
        <span class="notice-mark" aria-hidden="true">!</span>
        <div>
          <strong>꼭 기억하세요</strong>
          <p>
            등기부등본은 계약 직전과 잔금 지급 직전에 각각 다시 확인하는 것이
            안전해요.
          </p>
        </div>
      </aside>
    </div>

    <AppTabBar active="home" />
  </div>
</template>

<script setup>
import { computed, ref } from 'vue';
import PageHeader from '@/components/PageHeader.vue';
import AppTabBar from '@/components/AppTabBar.vue';

const tab = ref('contract');

const guides = {
  contract: {
    label: 'SAFE CONTRACT',
    title: '계약할 때 꼭 확인하세요',
    description: '계약 전, 계약서 작성, 잔금일 순서로 살펴보세요.',
    sections: [
      {
        title: '계약 전 확인',
        items: [
          '등기부등본을 계약 당일 직접 발급하고 갑구·을구·표제부를 확인하세요.',
          '건축물대장에서 용도·면적·위반건축물 여부를 살펴보세요.',
          '매도인 신분증과 등기부상 소유자가 일치하는지 확인하세요.',
          '대리인 계약이라면 위임장·인감증명서를 받고 본인과 통화하세요.',
          '토지거래허가구역에 해당하는지 확인하세요.',
          '집주인의 체납·다주택·채무 상태를 확인하세요.',
        ],
      },
      {
        title: '계약서 작성 시 확인',
        items: [
          '부동산 표시가 등기부 표제부와 한 글자도 다르지 않은지 확인하세요.',
          '매매대금의 한글·숫자 금액이 일치하고 빈칸이 없는지 살펴보세요.',
          '계약금·중도금·잔금 일정과 금액을 정확하게 기재하세요.',
          '계약금 송금 계좌가 매도인 본인 명의인지 확인하세요.',
          '권리변동 금지·동시이행·하자고지·임차인 승계·위약금 특약을 넣으세요.',
          '중개대상물 확인설명서를 반드시 받으세요.',
        ],
      },
      {
        title: '잔금일 확인',
        items: [
          '잔금일 당일 등기부등본을 다시 발급해 권리 변동을 확인하세요.',
          '소유권 이전 등기에 필요한 서류를 매도인에게 모두 받으세요.',
          '열쇠와 시설물을 직접 인도받고 상태를 확인하세요.',
          '관리비·공과금·선수관리비를 정산하세요.',
          '취득세를 납부하고 잔금일로부터 60일 안에 소유권 이전 등기를 신청하세요.',
        ],
      },
    ],
  },
  insurance: {
    label: 'DEPOSIT INSURANCE',
    title: '전세보증보험 가입 전 확인하세요',
    description: '가입 가능 여부를 판단하는 다섯 가지 핵심 단계예요.',
    sections: [
      {
        title: '주택가격 확인',
        items: [
          '아파트·오피스텔은 KB시세 또는 부동산테크 시세를 확인하세요.',
          '빌라·다세대는 공시가격 알리미에서 공시가격을 확인하세요.',
          '내 보증금이 공시가격의 126% 이내인지 계산하세요.',
        ],
      },
      {
        title: '등기부등본 확인',
        items: [
          '갑구에서 압류·가압류·경매 등 권리 침해 여부를 살펴보세요.',
          '을구에서 근저당권 설정액 등 선순위 채권을 확인하세요.',
          '선순위채권과 보증금의 합이 주택가격의 90% 이하인지 확인하세요.',
        ],
      },
      {
        title: '건축물대장 확인',
        items: [
          '위반건축물로 기재되어 있는지 확인하세요.',
          '건축물의 용도가 주거용인지 확인하세요.',
          '건물과 토지의 소유자가 일치하는지 살펴보세요.',
        ],
      },
      {
        title: '보증기관 사전 조회',
        items: [
          'HUG 안심전세 앱에서 주택의 위험성을 진단하세요.',
          'HF 홈페이지에서 전세지킴보증 가입 가능 여부를 확인하세요.',
          '사전 조회 결과 가입이 불가능한 매물은 계약을 재검토하세요.',
        ],
      },
      {
        title: '집주인 정보 확인',
        items: [
          '국세와 지방세 체납 여부를 확인하세요.',
          '다주택 보유 여부와 다른 부동산의 채무 상태를 살펴보세요.',
          '상습채무불이행자 명단에 등록되어 있는지 확인하세요.',
          '확인할 수 있는 범위에서 금융사기 이력을 살펴보세요.',
        ],
      },
    ],
  },
};

const links = [
  {
    label: 'HUG 안심전세포털',
    url: 'https://www.khug.or.kr/jeonse/index.jsp',
  },
  {
    label: '정부24 건축물대장 열람',
    url: 'https://www.gov.kr/mw/AA020InfoCappView.do?CappBizCD=15000000098',
  },
  {
    label: '인터넷등기소',
    url: 'https://www.iros.go.kr/index.jsp',
  },
];

const currentGuide = computed(() => guides[tab.value]);
</script>

<style scoped>
.guide {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--bg);
}

.scroll-area {
  flex: 1;
  overflow-y: auto;
  padding: 16px 16px 24px;
}

.intro-card {
  padding: 18px 17px;
  background: var(--yellow-tint);
  border: 1px solid rgba(255, 188, 0, 0.32);
  border-radius: 16px;
}

.intro-title {
  font-size: 16px;
  font-weight: 800;
  color: var(--text-primary);
}

.intro-sub {
  margin-top: 6px;
  font-size: 12px;
  line-height: 1.55;
  color: var(--kb-gray);
  word-break: keep-all;
}

.tabs {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
  margin-top: 16px;
  padding-bottom: 10px;
  background: transparent;
  border-bottom: 1px solid var(--border);
}

.tab {
  min-height: 36px;
  padding: 8px 12px;
  background: var(--white);
  border: 1px solid var(--border);
  border-radius: 100px;
  font-size: 12.5px;
  color: var(--kb-silver);
}

.tab.on {
  color: var(--text-primary);
  font-weight: 800;
  background: var(--kb-yellow);
  border-color: var(--kb-yellow);
  box-shadow: none;
}

.section-intro {
  margin: 22px 2px 14px;
}

.heading {
  font-size: 16px;
  font-weight: 800;
  color: var(--text-primary);
}

.heading-sub {
  margin-top: 5px;
  font-size: 11.5px;
  color: var(--kb-silver);
}

.guide-section {
  margin-top: 10px;
  padding: 0 15px;
  background: var(--white);
  border: 1px solid var(--border);
  border-radius: 16px;
}

.section-head {
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 15px 0 12px;
  border-bottom: 1px solid var(--border);
}

.section-step {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  flex-shrink: 0;
  font-size: 11px;
  font-weight: 800;
  color: #67551f;
  background: var(--yellow-tint);
  border-radius: 50%;
}

.section-head h2 {
  font-size: 14px;
  font-weight: 800;
  color: var(--text-primary);
}

.guide-list {
  padding: 2px 0;
}

.guide-list li {
  position: relative;
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 13px 0;
}

.guide-list li:not(:last-child)::after {
  content: '';
  position: absolute;
  right: 0;
  bottom: 0;
  left: 16px;
  height: 1px;
  background: var(--bg);
}

.item-dot {
  width: 5px;
  height: 5px;
  flex-shrink: 0;
  margin-top: 8px;
  background: #c89a1d;
  border-radius: 50%;
}

.guide-list p {
  padding-top: 2px;
  font-size: 13px;
  line-height: 1.58;
  color: var(--text-primary);
  word-break: keep-all;
}

.links-card {
  margin-top: 14px;
  padding: 15px;
  background: #fff9e8;
  border: 1px solid rgba(255, 188, 0, 0.28);
  border-radius: 16px;
}

.links-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 11px;
}

.links-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 31px;
  height: 31px;
  flex-shrink: 0;
  font-size: 14px;
  font-weight: 900;
  color: #6d5719;
  background: var(--kb-yellow);
  border-radius: 10px;
}

.links-title p {
  font-size: 13px;
  font-weight: 900;
  color: var(--text-primary);
}

.links-title span:not(.links-icon) {
  display: block;
  margin-top: 2px;
  font-size: 10.5px;
  color: var(--kb-silver);
}

.link-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  margin-top: 7px;
  padding: 12px 13px;
  color: var(--text-primary);
  background: var(--white);
  border: 1px solid rgba(0, 0, 0, 0.04);
  border-radius: 10px;
  font-size: 12px;
  font-weight: 700;
  text-align: left;
  text-decoration: none;
}

.link-row svg {
  color: #a8842c;
}

.notice-card {
  display: flex;
  gap: 11px;
  margin-top: 14px;
  padding: 15px;
  background: var(--white);
  border: 1px solid var(--border);
  border-radius: 14px;
}

.notice-mark {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 23px;
  height: 23px;
  flex-shrink: 0;
  font-size: 12px;
  font-weight: 900;
  color: #6d5719;
  background: var(--kb-yellow);
  border-radius: 50%;
}

.notice-card strong {
  display: block;
  margin: 2px 0 4px;
  font-size: 12.5px;
  color: var(--text-primary);
}

.notice-card p {
  font-size: 11px;
  line-height: 1.55;
  color: var(--kb-gray);
  word-break: keep-all;
}
</style>
