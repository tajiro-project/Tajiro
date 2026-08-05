<template>
    <div class="location-search">
        <PageHeader title="위치 선택" />

        <div class="content">
            <div class="search-row">
                <div class="search-input">
                    <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                        <circle cx="7" cy="7" r="5" stroke="#8a8d8f" stroke-width="1.6" />
                        <path d="M10.8 10.8L14 14" stroke="#8a8d8f" stroke-width="1.6" stroke-linecap="round" />
                    </svg>
                    <input v-model="keyword" type="text" placeholder="예) 창원대" @keyup.enter="search" />
                </div>
                <button class="search-btn" type="button" @click="search">검색</button>
            </div>

            <p class="result-count">
                <span class="result-label">검색 결과</span>
                <span class="result-num">{{ results.length }}건</span>
            </p>

            <ul class="result-list">
                <li v-for="item in results" :key="item.buildingCode" class="result-item" :class="{ on: selected?.buildingCode === item.buildingCode }" @click="selectItem(item)">
                    <span class="result-texts">
                        <span class="result-name">{{ item.name }}</span>
                        <span class="result-address">{{ item.address }}</span>
                    </span>
                </li>
            </ul>

            <p class="map-label">
                <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                    <path d="M7 1.5c-2.2 0-4 1.8-4 4 0 3 4 7 4 7s4-4 4-7c0-2.2-1.8-4-4-4z" stroke="#33302a" stroke-width="1.3" />
                    <circle cx="7" cy="5.5" r="1.5" stroke="#33302a" stroke-width="1.3" />
                </svg>
                지도에서 위치 확인
            </p>

            <div class="map-box">
                <KakaoMap :markers="mapMarkers" :center="mapCenter" :level="4" />
            </div>
        </div>

        <div class="bottom-area">
            <button class="btn-cta" type="button" :disabled="!selected" @click="confirmLocation">이 위치로 설정</button>
        </div>
    </div>
</template>

<script setup>
import { computed, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import client, { withMock } from '@/api/client';
import { mockLocations } from '@/api/mockData';
import KakaoMap from '@/components/KakaoMap.vue';
import PageHeader from '@/components/PageHeader.vue';

const route = useRoute();
const router = useRouter();
const mode = computed(() => (route.query.mode === 'preference' ? 'preference' : 'home'));

const keyword = ref('');
const results = ref([]);
const selected = ref(null);

const mapCenter = computed(() => {
    if (selected.value) return { lat: selected.value.latitude, lng: selected.value.longitude };
    return { lat: 35.2281, lng: 128.6811 };
});

const mapMarkers = computed(() => {
    if (!selected.value) return [];
    return [{ lat: selected.value.latitude, lng: selected.value.longitude, selected: true }];
});

async function search() {
    const data = await withMock(
        () => client.get('/locations/search', { params: { keyword: keyword.value } }),
        mockLocations.filter((item) => item.name.includes(keyword.value)),
    );
    results.value = Array.isArray(data) ? data : [];
    selected.value = null;
}

function selectItem(item) {
    selected.value = item;
}

function confirmLocation() {
    if (!selected.value) return;

    if (mode.value === 'home') {
        router.push({ path: '/properties', query: { region: selected.value.name } });
    } else {
        sessionStorage.setItem('selectedLocation', JSON.stringify(selected.value));
        router.push('/preferences/1');
    }
}
</script>

<style scoped>
.location-search {
    flex: 1;
    display: flex;
    flex-direction: column;
    min-height: 0;
    background: var(--bg);
}

.content {
    flex: 1;
    min-height: 0;
    overflow-y: auto;
    padding: 14px 16px;
    display: flex;
    flex-direction: column;
    gap: 14px;
}

.search-row {
    display: flex;
    gap: 8px;
}

.search-input {
    flex: 1;
    display: flex;
    align-items: center;
    gap: 8px;
    height: 33px;
    padding: 0 12px;
    background: var(--white);
    border: 1.5px solid var(--kb-yellow-header);
    border-radius: 10px;
}

.search-input input {
    flex: 1;
    min-width: 0;
    font-size: 14px;
    color: var(--text-primary);
}

.search-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 8px 14px;
    background: var(--kb-yellow-header);
    border-radius: 8px;
    font-size: 13px;
    font-weight: 500;
    color: var(--kb-dark-gray);
}

.result-count {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 12.5px;
}

.result-label {
    font-weight: 700;
    color: var(--text-primary);
}

.result-num {
    font-weight: 500;
    color: var(--sub-kb-gold, #85714d);
}

.result-list {
    display: flex;
    flex-direction: column;
    border: 1px solid var(--border);
    border-radius: 14px;
    overflow: hidden;
}

.result-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 12px 14px;
    background: var(--white);
}

.result-item + .result-item {
    border-top: 1px solid var(--border);
}

.result-item.on {
    background: var(--yellow-tint);
}

.result-texts {
    display: flex;
    flex-direction: column;
    gap: 2px;
    min-width: 0;
}

.result-name {
    font-size: 13.5px;
    font-weight: 500;
    color: var(--text-primary);
}

.result-address {
    font-size: 11px;
    color: var(--kb-silver);
}

.map-label {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 12.5px;
    font-weight: 700;
    color: var(--text-primary);
}

.map-box {
    flex: 1;
    min-height: 200px;
    background: var(--yellow-tint);
    border: 1px solid var(--border);
    border-radius: 14px;
    box-shadow: var(--shadow-card);
    overflow: hidden;
}

.bottom-area {
    padding: 12px 16px 16px;
    background: var(--white);
    border-top: 1px solid var(--border);
}
</style>
