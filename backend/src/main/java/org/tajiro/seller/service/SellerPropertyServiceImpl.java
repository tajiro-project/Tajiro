package org.tajiro.seller.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tajiro.common.api.ErrorCode;
import org.tajiro.exception.BusinessException;
import org.tajiro.property.domain.PropertyVO;
import org.tajiro.seller.domain.SellerPropertyCountsVO;
import org.tajiro.seller.domain.SellerPropertyListVO;
import org.tajiro.seller.dto.SellerPropertyCounts;
import org.tajiro.seller.dto.SellerPropertyListItem;
import org.tajiro.seller.dto.SellerPropertyPageResponse;
import org.tajiro.seller.dto.SellerPropertyDetailResponse;
import org.tajiro.seller.mapper.SellerPropertyMapper;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class SellerPropertyServiceImpl implements SellerPropertyService {
    private final SellerPropertyMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public SellerPropertyDetailResponse getProperty(Long sellerId, Long propertyId) {
        PropertyVO propertyVO = mapper.findOwnedDetail(sellerId, propertyId);
        if (propertyVO == null) {
            throw new BusinessException(ErrorCode.PROPERTY_NOT_FOUND);
        }
        return SellerPropertyDetailResponse.from(
                propertyVO, mapper.findImageUrls(propertyId));
    }

    @Override
    @Transactional(readOnly = true)
    public SellerPropertyPageResponse getProperties(
            Long sellerId, int page, int size, String status) {
        if (page < 0 || size < 1 || size > 100) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        String normalizedStatus = status == null
                ? "ALL"
                : status.toUpperCase(Locale.ROOT);
        if (!normalizedStatus.equals("ALL")
                && !normalizedStatus.equals("OPEN")
                && !normalizedStatus.equals("DONE")) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        long totalElements = mapper.countByStatus(sellerId, normalizedStatus);
        int totalPages = Math.max(1, (int) Math.ceil((double) totalElements / size));
        List<SellerPropertyListVO> propertyVOs = mapper.findPage(
                sellerId, normalizedStatus, size, page * size);
        List<SellerPropertyListItem> content = propertyVOs.stream()
                .map(SellerPropertyListItem::from)
                .toList();
        SellerPropertyCountsVO countsVO = mapper.getCounts(sellerId);
        SellerPropertyCounts counts = SellerPropertyCounts.from(countsVO);

        return SellerPropertyPageResponse.builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(page == 0)
                .last(page >= totalPages - 1)
                .counts(counts)
                .build();
    }

    @Override
    @Transactional
    public void changeStatus(
            Long sellerId, Long propertyId, boolean transactionStatus) {
        if (mapper.updateStatus(sellerId, propertyId, transactionStatus) == 0) {
            throw new BusinessException(ErrorCode.PROPERTY_NOT_FOUND);
        }
    }

    @Override
    @Transactional
    public void deleteProperty(Long sellerId, Long propertyId) {
        if (mapper.deleteProperty(sellerId, propertyId) == 0) {
            throw new BusinessException(ErrorCode.PROPERTY_NOT_FOUND);
        }
    }

}
