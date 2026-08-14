package org.tajiro.property.infra.config;

import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

public class InfraCategoryConfig {

    @Getter
    @Builder
    public static class CategoryItem {
        private String key;
        private String method; // "group" 또는 "keyword"
        private String code;
        @Builder.Default
        private List<String> queries = Collections.emptyList();
        @Builder.Default
        private List<String> includes = Collections.emptyList();
        @Builder.Default
        private List<String> excludes = Collections.emptyList();
        @Builder.Default
        private List<String> nameIncludes = Collections.emptyList();
    }

    public static final List<CategoryItem> ALL = List.of(
            // ==========================================
            // 1. 공공·행정 (4개)
            // ==========================================
            CategoryItem.builder()
                    .key("POLICE")
                    .method("keyword")
                    .queries(List.of("경찰서", "지구대", "파출소", "치안센터"))
                    .build(),
            CategoryItem.builder()
                    .key("FIRE")
                    .method("keyword")
                    .queries(List.of("소방서", "119안전센터", "119지역대"))
                    .build(),
            CategoryItem.builder()
                    .key("POST_OFFICE")
                    .method("keyword")
                    .queries(List.of("우체국"))
                    .build(),
            CategoryItem.builder().key("PUBLIC").method("group").code("PO3").build(), // 공공기관

            // ==========================================
            // 2. 교육·보육 (3개)
            // ==========================================
            CategoryItem.builder().key("KINDERGARTEN").method("group").code("PS3").build(), // 어린이집/유치원
            CategoryItem.builder().key("SCHOOL").method("group").code("SC4").build(),       // 학교
            CategoryItem.builder().key("ACADEMY").method("group").code("AC5").build(),      // 학원

            // ==========================================
            // 3. 교통 (3개)
            // ==========================================
            CategoryItem.builder().key("SUBWAY").method("group").code("SW8").build(),       // 지하철역
            CategoryItem.builder()
                    .key("BUS_TERMINAL")
                    .method("keyword")
                    .queries(List.of("고속버스터미널", "시외버스터미널"))
                    .build(),
            CategoryItem.builder()
                    .key("TRAIN")
                    .method("keyword")
                    .queries(List.of("기차역"))
                    .build(),

            // ==========================================
            // 4. 금융 (1개)
            // ==========================================
            CategoryItem.builder().key("BANK").method("group").code("BK9").build(),         // 은행

            // ==========================================
            // 5. 의료 (2개)
            // ==========================================
            CategoryItem.builder().key("HOSPITAL").method("group").code("HP8").build(),     // 병원
            CategoryItem.builder().key("PHARMACY").method("group").code("PM9").build(),     // 약국

            // ==========================================
            // 6. 문화·여가 (5개)
            // ==========================================
            CategoryItem.builder()
                    .key("LIBRARY")
                    .method("keyword")
                    .queries(List.of("도서관", "국공립도서관", "작은도서관"))
                    .build(),
            CategoryItem.builder()
                    .key("PARK")
                    .method("keyword")
                    .queries(List.of("공원", "도시근린공원"))
                    .build(),
            CategoryItem.builder().key("CULTURE").method("group").code("CT1").build(),     // 문화시설
            CategoryItem.builder().key("ATTRACTION").method("group").code("AT4").build(),  // 관광명소
            CategoryItem.builder().key("ACCOMMODATION").method("group").code("AD5").build(),// 숙박

            // ==========================================
            // 7. 생활편의 (1개)
            // ==========================================
            CategoryItem.builder()
                    .key("RESTROOM")
                    .method("keyword")
                    .queries(List.of("화장실", "공중화장실"))
                    .build(),

            // ==========================================
            // 8. 스포츠·레저 (2개)
            // ==========================================
            CategoryItem.builder()
                    .key("SPORTS")
                    .method("keyword")
                    .queries(List.of("헬스장", "헬스클럽", "스포츠센터", "체육관"))
                    .build(),
            CategoryItem.builder()
                    .key("SWIMMING")
                    .method("keyword")
                    .queries(List.of("수영장", "어린이수영장"))
                    .build(),

            // ==========================================
            // 9. 유통·편의 (3개)
            // ==========================================
            CategoryItem.builder().key("MART").method("group").code("MT1").build(),        // 대형마트
            CategoryItem.builder().key("CONVENIENCE").method("group").code("CS2").build(), // 편의점
            CategoryItem.builder().key("PARKING").method("group").code("PK6").build(),     // 주차장

            // ==========================================
            // 10. 음식·카페 (2개)
            // ==========================================
            CategoryItem.builder().key("FOOD").method("group").code("FD6").build(),        // 음식점
            CategoryItem.builder().key("CAFE").method("group").code("CE7").build()         // 카페
    );
}