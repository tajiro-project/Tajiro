package org.tajiro.favorite.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tajiro.common.api.ErrorCode;
import org.tajiro.exception.BusinessException;
import org.tajiro.favorite.domain.FavoriteVO;
import org.tajiro.favorite.dto.FavoriteAddResponse;
import org.tajiro.favorite.dto.FavoriteDTO;
import org.tajiro.favorite.mapper.FavoriteMapper;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteMapper mapper;

    @Override
    public List<FavoriteDTO> getFavorites(Long userId) {
        return mapper.getFavorites(userId);
    }

    @Override
    public FavoriteAddResponse addFavorite(Long userId, Long propertyId) {
        if (!mapper.existsProperty(propertyId)) {
            throw new BusinessException(ErrorCode.PROPERTY_NOT_FOUND);
        }

        if (mapper.existsFavorite(userId, propertyId)) {
            throw new BusinessException(ErrorCode.FAVORITE_DUPLICATE);
        }

        FavoriteVO vo = FavoriteVO.builder()
                .userId(userId)
                .propertyId(propertyId)
                .createdAt(LocalDateTime.now())
                .build();
        mapper.insertFavorite(vo);

        return FavoriteAddResponse.builder()
                .favoriteId(vo.getId())
                .createdAt(vo.getCreatedAt())
                .build();
    }

    @Override
    public void removeFavorite(Long userId, Long propertyId) {
        mapper.deleteFavorite(userId, propertyId);
    }
}
