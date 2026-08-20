package org.tajiro.seller.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.tajiro.common.api.ApiResponse;
import org.tajiro.seller.dto.PropertyImageUploadResponse;
import org.tajiro.seller.service.PropertyImageService;
import springfox.documentation.annotations.ApiIgnore;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/property-images")
@RequiredArgsConstructor
@Api(tags = "매물 이미지")
public class PropertyImageController {

    private final PropertyImageService propertyImageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation(value = "매물 이미지 업로드")
    public ResponseEntity<ApiResponse<PropertyImageUploadResponse>> upload(
            @ApiIgnore @AuthenticationPrincipal Long userId,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        PropertyImageUploadResponse response = propertyImageService.upload(userId, file);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @GetMapping("/{objectKey:.+}")
    @ApiOperation(value = "매물 이미지 조회")
    public ResponseEntity<Resource> getImage(@PathVariable String objectKey) {
        Resource image = propertyImageService.load(objectKey);
        return ResponseEntity.ok()
                .contentType(propertyImageService.mediaTypeOf(objectKey))
                .cacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic())
                .header("X-Content-Type-Options", "nosniff")
                .body(image);
    }
}
