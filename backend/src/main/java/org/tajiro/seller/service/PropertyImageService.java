package org.tajiro.seller.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.tajiro.common.api.ErrorCode;
import org.tajiro.exception.BusinessException;
import org.tajiro.seller.dto.PropertyImageUploadResponse;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PropertyImageService {

    static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    static final String IMAGE_URL_PREFIX = "/api/property-images/";

    private static final Pattern OBJECT_KEY_PATTERN = Pattern.compile(
            "^(\\d+)-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.(jpg|png|webp)$"
    );
    private static final Map<String, ImageType> IMAGE_TYPES_BY_CONTENT_TYPE = Map.of(
            MediaType.IMAGE_JPEG_VALUE, new ImageType("jpg", MediaType.IMAGE_JPEG_VALUE),
            MediaType.IMAGE_PNG_VALUE, new ImageType("png", MediaType.IMAGE_PNG_VALUE),
            "image/webp", new ImageType("webp", "image/webp")
    );

    private final PropertyImageStorage propertyImageStorage;

    public PropertyImageUploadResponse upload(Long sellerId, MultipartFile file) {
        if (sellerId == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }
        validateBasicFileProperties(file);

        String declaredContentType = file.getContentType().toLowerCase(Locale.ROOT);
        ImageType declaredType = IMAGE_TYPES_BY_CONTENT_TYPE.get(declaredContentType);
        String objectKey = sellerId + "-" + UUID.randomUUID() + "." + declaredType.extension;

        try (BufferedInputStream input = new BufferedInputStream(file.getInputStream())) {
            input.mark(16);
            byte[] header = input.readNBytes(12);
            input.reset();

            ImageType detectedType = detectImageType(header);
            if (detectedType == null || !declaredType.extension.equals(detectedType.extension)) {
                throw new BusinessException(ErrorCode.INVALID_IMAGE_FILE);
            }

            propertyImageStorage.store(
                    objectKey,
                    input,
                    file.getSize(),
                    declaredType.contentType
            );
        } catch (BusinessException exception) {
            throw exception;
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.IMAGE_STORAGE_FAILED);
        }

        return PropertyImageUploadResponse.builder()
                .objectKey(objectKey)
                .imageUrl(IMAGE_URL_PREFIX + objectKey)
                .contentType(declaredType.contentType)
                .size(file.getSize())
                .build();
    }

    public Resource load(String objectKey) {
        validateObjectKey(objectKey);
        try {
            return propertyImageStorage.load(objectKey);
        } catch (NoSuchFileException exception) {
            throw new BusinessException(ErrorCode.PROPERTY_IMAGE_NOT_FOUND);
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.IMAGE_STORAGE_FAILED);
        }
    }

    public MediaType mediaTypeOf(String objectKey) {
        Matcher matcher = validateObjectKey(objectKey);
        String extension = matcher.group(2);
        if ("jpg".equals(extension)) {
            return MediaType.IMAGE_JPEG;
        }
        if ("png".equals(extension)) {
            return MediaType.IMAGE_PNG;
        }
        return MediaType.parseMediaType("image/webp");
    }

    private void validateBasicFileProperties(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getSize() <= 0) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_FILE);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.IMAGE_FILE_TOO_LARGE);
        }
        if (file.getContentType() == null
                || !IMAGE_TYPES_BY_CONTENT_TYPE.containsKey(
                file.getContentType().toLowerCase(Locale.ROOT))) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_FILE);
        }
    }

    private Matcher validateObjectKey(String objectKey) {
        Matcher matcher = OBJECT_KEY_PATTERN.matcher(objectKey == null ? "" : objectKey);
        if (!matcher.matches()) {
            throw new BusinessException(ErrorCode.PROPERTY_IMAGE_NOT_FOUND);
        }
        return matcher;
    }

    private ImageType detectImageType(byte[] header) {
        if (header.length >= 3
                && unsigned(header[0]) == 0xff
                && unsigned(header[1]) == 0xd8
                && unsigned(header[2]) == 0xff) {
            return IMAGE_TYPES_BY_CONTENT_TYPE.get(MediaType.IMAGE_JPEG_VALUE);
        }
        if (header.length >= 8
                && unsigned(header[0]) == 0x89
                && header[1] == 'P'
                && header[2] == 'N'
                && header[3] == 'G'
                && unsigned(header[4]) == 0x0d
                && unsigned(header[5]) == 0x0a
                && unsigned(header[6]) == 0x1a
                && unsigned(header[7]) == 0x0a) {
            return IMAGE_TYPES_BY_CONTENT_TYPE.get(MediaType.IMAGE_PNG_VALUE);
        }
        if (header.length >= 12
                && header[0] == 'R'
                && header[1] == 'I'
                && header[2] == 'F'
                && header[3] == 'F'
                && header[8] == 'W'
                && header[9] == 'E'
                && header[10] == 'B'
                && header[11] == 'P') {
            return IMAGE_TYPES_BY_CONTENT_TYPE.get("image/webp");
        }
        return null;
    }

    private int unsigned(byte value) {
        return value & 0xff;
    }

    private static final class ImageType {
        private final String extension;
        private final String contentType;

        private ImageType(String extension, String contentType) {
            this.extension = extension;
            this.contentType = contentType;
        }
    }
}
