package org.tajiro.favorite.service;

import org.tajiro.favorite.dto.FavoriteAddResponse;
import org.tajiro.favorite.dto.FavoriteDTO;

import java.util.List;

public interface FavoriteService {
    List<FavoriteDTO> getFavorites(Long userId);

    FavoriteAddResponse addFavorite(Long userId, Long propertyId);

    void removeFavorite(Long userId, Long propertyId);
}
