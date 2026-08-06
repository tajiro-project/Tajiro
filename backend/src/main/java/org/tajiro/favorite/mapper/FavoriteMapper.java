package org.tajiro.favorite.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.tajiro.favorite.domain.FavoriteVO;
import org.tajiro.favorite.dto.FavoriteDTO;

import java.util.List;

@Mapper
public interface FavoriteMapper {
    List<FavoriteDTO> getFavorites(@Param("userId") Long userId);

    boolean existsFavorite(@Param("userId") Long userId, @Param("propertyId") Long propertyId);

    void insertFavorite(FavoriteVO vo);

    void deleteFavorite(@Param("userId") Long userId, @Param("propertyId") Long propertyId);
}
