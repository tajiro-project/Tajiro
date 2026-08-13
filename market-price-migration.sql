-- Tajiro 실거래가 정규화/시세 적정성 기능 1회성 마이그레이션
-- MySQL 8.x 기준
--
-- 기존 property_actual_transaction은 property_id가 PK여서 API 거래 이력을 담을 수 없습니다.
-- 기존 데이터가 있더라도 보존되도록 legacy 테이블로 이름을 바꾼 뒤 새 테이블을 생성합니다.

RENAME TABLE property_actual_transaction TO property_actual_transaction_legacy;

CREATE TABLE property_actual_transaction (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    source_api VARCHAR(30) NOT NULL,
    source_unique_key CHAR(64) NOT NULL,
    property_category VARCHAR(30) NOT NULL,
    trade_type VARCHAR(20) NOT NULL,
    sgg_code CHAR(5) NOT NULL,
    source_deal_ym CHAR(6) NOT NULL COMMENT 'API 요청 계약년월(YYYYMM)',
    legal_dong_code VARCHAR(10) NULL,
    umd_name VARCHAR(100) NULL,
    jibun VARCHAR(50) NULL,
    building_name VARCHAR(255) NULL,
    source_building_code VARCHAR(50) NULL,
    building_dong VARCHAR(50) NULL,
    house_type VARCHAR(50) NULL,
    exclusive_area_m2 DECIMAL(15,4) NULL,
    total_floor_area_m2 DECIMAL(15,4) NULL,
    land_area_m2 DECIMAL(15,4) NULL,
    floor INT NULL,
    build_year INT NULL,
    deal_date DATE NOT NULL,
    deal_amount INT NULL COMMENT '매매 거래금액(만원)',
    deposit INT NULL COMMENT '전월세 보증금(만원)',
    monthly_rent INT NULL COMMENT '월세(만원)',
    converted_price DECIMAL(15,2) NULL COMMENT '비교용 환산가격(만원)',
    canceled TINYINT(1) NOT NULL DEFAULT 0,
    cancellation_date VARCHAR(20) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_actual_transaction_source (source_api, source_unique_key),
    KEY idx_actual_market_search (
        sgg_code,
        property_category,
        source_deal_ym,
        trade_type,
        deal_date
    ),
    KEY idx_actual_building_search (
        sgg_code,
        legal_dong_code,
        jibun,
        building_name
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE market_transaction_sync (
    source_api VARCHAR(30) NOT NULL,
    sgg_code CHAR(5) NOT NULL,
    deal_ym CHAR(6) NOT NULL,
    last_attempt_status VARCHAR(20) NOT NULL,
    total_count INT NOT NULL DEFAULT 0,
    last_success_at DATETIME NULL,
    last_attempt_at DATETIME NOT NULL,
    last_error VARCHAR(500) NULL,
    PRIMARY KEY (source_api, sgg_code, deal_ym),
    KEY idx_market_sync_coverage (sgg_code, deal_ym, last_success_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE building
    ADD COLUMN market_property_category VARCHAR(30) NULL
        COMMENT '실거래 API 법적 유형: APARTMENT/OFFICETEL/ROW_HOUSE/SINGLE_HOUSE'
        AFTER sigungu_cd;

UPDATE building b
JOIN property p ON p.building_id = b.id
SET b.market_property_category = 'APARTMENT'
WHERE p.property_type = '아파트';

UPDATE building b
JOIN property p ON p.building_id = b.id
SET b.market_property_category = 'OFFICETEL'
WHERE p.property_type = '오피스텔';

ALTER TABLE property
    MODIFY COLUMN evaluation_score DECIMAL(9,2) NULL
        COMMENT '주변 실거래 중앙값 대비 차이율(%): 양수 비쌈, 음수 저렴',
    ADD COLUMN market_status VARCHAR(30) NULL
        COMMENT 'CALCULATED/DATA_INSUFFICIENT/NOT_SYNCED/SYNC_ERROR/SOURCE_UNMAPPED/REGION_CODE_MISSING'
        AFTER evaluation_score,
    ADD COLUMN market_reference_price DECIMAL(15,2) NULL
        COMMENT '평가에 사용한 실거래 중앙값(만원)'
        AFTER market_status,
    ADD COLUMN market_transaction_count INT NULL
        COMMENT '평가에 사용한 실거래 건수'
        AFTER market_reference_price,
    ADD COLUMN market_basis_level TINYINT NULL
        COMMENT '비교 기준 단계: 1 동일건물 12개월, 2 동일건물 24개월, 3 동일법정동 24개월'
        AFTER market_transaction_count,
    ADD COLUMN market_calculated_at DATETIME NULL
        AFTER market_basis_level;

-- 주택/빌라, 원룸은 UI 유형만으로 법적 건물 유형을 확정할 수 없습니다.
-- 건축물대장 등으로 확인한 뒤 아래처럼 명시하면 자동 추정보다 우선합니다.
-- UPDATE building SET market_property_category = 'ROW_HOUSE' WHERE id = ?;
-- UPDATE building SET market_property_category = 'SINGLE_HOUSE' WHERE id = ?;

