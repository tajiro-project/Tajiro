package org.tajiro.property.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tajiro.common.api.ErrorCode;
import org.tajiro.exception.BusinessException;
import org.tajiro.property.domain.PropertyDetailVO;
import org.tajiro.property.dto.PropertyDetailDTO;
import org.tajiro.property.mapper.PropertyDetailMapper;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PropertyDetailServiceImpl implements PropertyDetailService {

    private final PropertyDetailMapper propertyDetailMapper;

    @Override
    @Transactional(readOnly = true)
    public PropertyDetailDTO getPropertyDetail(Long id, Long userId) {
        PropertyDetailVO vo = propertyDetailMapper.selectPropertyDetail(id);

        if (vo == null) {
            throw new BusinessException(ErrorCode.PROPERTY_NOT_FOUND);
        }

        List<String> images = propertyDetailMapper.selectPropertyImages(id);

        List<PropertyDetailDTO.InfraSummaryDTO> infraSummary = new ArrayList<>();
        if (vo.getBuildingId() != null) {
            infraSummary = propertyDetailMapper.selectInfraSummaryByBuildingId(vo.getBuildingId());
        }

        // ✨ 로그인 유저인 경우에만 찜 여부 조회 (비로그인은 false)
        boolean isFavorite = false;
        if (userId != null) {
            isFavorite = propertyDetailMapper.selectIsFavorite(userId, id);
        }

        // isFavorite 추가 전달
        return PropertyDetailDTO.of(vo, images, infraSummary, isFavorite);
    }
}