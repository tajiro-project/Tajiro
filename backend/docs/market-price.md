# 실거래가 수집 및 시세 적정성 계산

## 적용 순서

1. `market-price-migration.sql`을 대상 MySQL DB에 한 번 실행한다.
2. 공공데이터포털에서 발급된 **Decoding 인증키**를 환경변수 `PUBLIC_DATA_API_KEY`로 설정한다.
3. `application.properties`에 아래 설정을 추가한다.

```properties
market.sync.enabled=true
market.sync.history-months=24
market.sync.refresh-months=3
market.rent-conversion-rate=0.05
```

서버 시작 60초 후 최초 수집이 실행되고, 이후 기본 24시간 간격으로 실행된다. 최근 24개월의 시작 월 일부까지 포함하도록 25개 달력을 수집하고 정확한 날짜 기준으로 오래된 거래를 정리한다. 다음 실행부터 최근 3개월은 갱신하며 최신 시도가 성공한 과거 월만 건너뛴다.

## API 선택

| 내부 유형 | 매매 | 전월세 |
| --- | --- | --- |
| APARTMENT | APT_SALE_DETAIL | APT_RENT |
| OFFICETEL | OFFICETEL_SALE | OFFICETEL_RENT |
| ROW_HOUSE | ROW_HOUSE_SALE | ROW_HOUSE_RENT |
| SINGLE_HOUSE | SINGLE_HOUSE_SALE | SINGLE_HOUSE_RENT |

`APT_SALE_BASIC`도 코드에서 지원하지만, 상세 API와 같은 거래를 중복 집계하지 않도록 기본 수집 대상에서는 제외한다.

## 정규화 규칙

- 매매: `deal_amount`를 비교가격으로 사용한다.
- 전세: `deposit`을 비교가격으로 사용한다.
- 월세: `deposit + monthly_rent * 12 / 전월세전환율`로 환산한다.
- 매매와 전월세, 서로 다른 법적 건물 유형은 섞지 않는다.
- 취소 신고는 삭제하지 않고 `canceled=1`로 갱신하며 중앙값에서 제외한다.
- 한 달치 API를 끝까지 정상 수신한 뒤 해당 소스·지역·월 데이터를 트랜잭션 안에서 교체하므로, 정정되어 사라진 이전 행이 남지 않는다.
- API 원본에는 안정적인 거래 ID가 없으므로 주요 공개 필드의 SHA-256을 월 스냅샷 내부 중복 방지용 키로 사용한다.
- 공개 필드가 완전히 같은 복수 거래도 스냅샷 내 발생 순번을 키에 포함해 각각 보존한다.

## 평가 기준

거래가 최소 3건 이상일 때만 계산한다.

1. 같은 건물/지번, 면적 ±5㎡, 최근 12개월
2. 같은 건물/지번, 면적 ±10㎡, 최근 24개월
3. 같은 법정동, 면적 ±10㎡, 최근 24개월

```text
evaluation_score = (매물 환산가격 - 실거래 중앙값) / 실거래 중앙값 * 100
```

- 양수: 주변 중앙값보다 비쌈
- 음수: 주변 중앙값보다 저렴함
- 0에 가까움: 주변 시세와 유사함

## null과 상태 구분

`property.market_status`가 원인을 기록한다.

- `CALCULATED`: 정상 계산
- `DATA_INSUFFICIENT`: 정상 수집했지만 유사 거래가 3건 미만
- `NOT_SYNCED`: 필요한 월의 수집이 아직 완료되지 않음
- `SYNC_ERROR`: 필요한 API 호출 실패
- `SOURCE_UNMAPPED`: 매물 유형을 법적 API 유형으로 결정하지 못함
- `REGION_CODE_MISSING`: PNU/시군구 코드 없음

`NOT_SYNCED`, `SYNC_ERROR`에서는 이전에 계산된 점수를 보존한다. 정상적으로 범위를 수집했지만 데이터가 부족한 경우에만 점수를 `NULL`로 갱신한다.

## 주택/빌라와 원룸 주의사항

현재 `property_type`의 `주택/빌라`, `원룸`은 법적 건물 유형이 아니다. 자동 수집은 후보 API를 모두 받지만 법적 유형이 하나로 확정되기 전에는 점수를 계산하지 않는다. 가능한 경우 건축물대장을 확인해 `building.market_property_category`에 아래 값 중 하나를 저장해야 한다.

```text
APARTMENT
OFFICETEL
ROW_HOUSE
SINGLE_HOUSE
```

단독/다가구 매매는 건물 전체 거래와 호실 매물의 가격 단위가 다를 수 있어 자동 평가에서 제외한다.
단독/다가구 전월세 API는 지번·건물명 정보가 제한적이므로 법정동 기준 3단계 비교를 사용한다.
