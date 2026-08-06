/**
 * 찜한 매물 API (찜 목록/찜하기/찜 해제).
 */
package org.tajiro.favorite.controller;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.tajiro.common.api.ApiResponse;
import org.tajiro.common.api.ErrorCode;
import org.tajiro.exception.BusinessException;
import org.tajiro.favorite.dto.FavoriteAddResponse;
import org.tajiro.favorite.dto.FavoriteDTO;
import org.tajiro.favorite.service.FavoriteService;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@RestController
@RequestMapping("/api/users/me/favorites")
@RequiredArgsConstructor
@Api(tags = "찜한 매물")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<FavoriteDTO>>> getFavorites(@ApiIgnore @AuthenticationPrincipal Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }

        return ResponseEntity.ok(ApiResponse.success(favoriteService.getFavorites(userId)));
    }

    @PostMapping("/{propertyId}")
    public ResponseEntity<ApiResponse<FavoriteAddResponse>> addFavorite(
            @ApiIgnore @AuthenticationPrincipal Long userId,
            @PathVariable Long propertyId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }

        FavoriteAddResponse response = favoriteService.addFavorite(userId, propertyId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @DeleteMapping("/{propertyId}")
    public ResponseEntity<Void> removeFavorite(
            @ApiIgnore @AuthenticationPrincipal Long userId,
            @PathVariable Long propertyId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }

        favoriteService.removeFavorite(userId, propertyId);
        return ResponseEntity.noContent().build();
    }
}
