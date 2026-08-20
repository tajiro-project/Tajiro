package org.tajiro.seller.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.tajiro.common.api.ErrorCode;
import org.tajiro.exception.BusinessException;
import org.tajiro.seller.domain.PropertyRegistrationVO;
import org.tajiro.seller.dto.PropertyRegistrationRequest;
import org.tajiro.seller.dto.PropertyRegistrationResponse;
import org.tajiro.seller.event.PropertyRegisteredEvent;
import org.tajiro.seller.mapper.PropertyRegistrationMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PropertyRegistrationServiceImplTest {

    @Test
    void sellerInputIsMappedToPropertyAndEventIsPublished() {
        StubPropertyRegistrationMapper mapper = new StubPropertyRegistrationMapper();
        BuildingResolveService resolver = (roadAddress, buildingName, dongName, latitude, longitude) -> 77L;
        List<Object> events = new ArrayList<>();
        PropertyRegistrationServiceImpl service = new PropertyRegistrationServiceImpl(
                mapper,
                new FixedObjectProvider<>(resolver),
                events::add
        );
        PropertyRegistrationRequest request = validRequest();
        LocalDateTime startedAt = LocalDateTime.now();

        PropertyRegistrationResponse response = service.register(3L, request);

        PropertyRegistrationVO saved = mapper.savedProperty;
        assertNotNull(saved);
        assertEquals(3L, saved.getSellerId());
        assertEquals(77L, saved.getBuildingId());
        assertEquals("상남동 원룸", saved.getTitle());
        assertEquals("원룸", saved.getPropertyType());
        assertEquals("월세", saved.getTradeType());
        assertEquals(500, saved.getDeposit());
        assertEquals(45, saved.getMonthlyRent());
        assertEquals(new BigDecimal("26.4"), saved.getAreaM2());
        assertEquals("302호", saved.getRoomNumber());
        assertTrue(saved.getUpdateDate().compareTo(startedAt) >= 0);
        assertTrue(mapper.savedImageUrls.isEmpty());

        assertEquals(101L, response.getPropertyId());
        assertEquals(77L, response.getBuildingId());
        assertEquals("RESOLVED", response.getLocationStatus());
        assertEquals("PENDING", response.getAggregationStatus());
        assertEquals(1, events.size());
        PropertyRegisteredEvent event = (PropertyRegisteredEvent) events.get(0);
        assertEquals(101L, event.getPropertyId());
        assertEquals(77L, event.getBuildingId());
    }

    @Test
    void sellerCanRegisterAnImageUploadedByTheSameAccount() {
        StubPropertyRegistrationMapper mapper = new StubPropertyRegistrationMapper();
        PropertyRegistrationServiceImpl service = new PropertyRegistrationServiceImpl(
                mapper,
                new FixedObjectProvider<>(null),
                event -> { }
        );
        PropertyRegistrationRequest request = validRequest();
        String imageUrl = "/api/property-images/3-00000000-0000-0000-0000-000000000000.jpg";
        request.setImageUrls(List.of(imageUrl));

        service.register(3L, request);

        assertEquals(List.of(imageUrl), mapper.savedImageUrls);
    }

    @Test
    void sellerCannotRegisterAnotherSellersInternalImage() {
        PropertyRegistrationServiceImpl service = new PropertyRegistrationServiceImpl(
                new StubPropertyRegistrationMapper(),
                new FixedObjectProvider<>(null),
                event -> { }
        );
        PropertyRegistrationRequest request = validRequest();
        request.setImageUrls(List.of(
                "/api/property-images/99-00000000-0000-0000-0000-000000000000.jpg"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.register(3L, request)
        );

        assertEquals(ErrorCode.INVALID_INPUT_VALUE, exception.getResponseCode());
    }

    private PropertyRegistrationRequest validRequest() {
        return PropertyRegistrationRequest.builder()
                .title("상남동 원룸")
                .propertyType("원룸")
                .tradeType("월세")
                .deposit(500)
                .monthlyRent(45)
                .maintenanceFee(7)
                .areaM2(new BigDecimal("26.4"))
                .floorInfo("3 / 10층")
                .roomNumber("302")
                .roomNum(1)
                .bathroomNum(1)
                .parkAvailability(true)
                .discussionStatus(false)
                .dong("101동")
                .propertyDescription("테스트 매물")
                .moveInDate(LocalDate.of(2026, 9, 1))
                .availableDate(LocalDate.of(2020, 1, 1))
                .location(PropertyRegistrationRequest.LocationRequest.builder()
                        .address("경남 창원시 성산구 상남로 1")
                        .buildingName("테스트 건물")
                        .lat(new BigDecimal("35.2223000"))
                        .lng(new BigDecimal("128.6812000"))
                        .build())
                .imageUrls(List.of())
                .build();
    }

    private static class StubPropertyRegistrationMapper
            implements PropertyRegistrationMapper {

        private PropertyRegistrationVO savedProperty;
        private List<String> savedImageUrls = List.of();

        @Override
        public int insertProperty(PropertyRegistrationVO property) {
            savedProperty = property;
            property.setId(101L);
            return 1;
        }

        @Override
        public int insertPropertyImages(Long propertyId, List<String> imageUrls) {
            savedImageUrls = imageUrls;
            return imageUrls.size();
        }
    }

    private static class FixedObjectProvider<T> implements ObjectProvider<T> {

        private final T value;

        private FixedObjectProvider(T value) {
            this.value = value;
        }

        @Override
        public T getObject(Object... args) throws BeansException {
            return value;
        }

        @Override
        public T getObject() throws BeansException {
            return value;
        }

        @Override
        public T getIfAvailable() throws BeansException {
            return value;
        }

        @Override
        public T getIfUnique() throws BeansException {
            return value;
        }

        @Override
        public Iterator<T> iterator() {
            return List.of(value).iterator();
        }
    }
}
