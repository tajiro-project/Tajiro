package org.tajiro.seller.service;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.tajiro.common.api.ErrorCode;
import org.tajiro.exception.BusinessException;
import org.tajiro.seller.dto.PropertyImageUploadResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PropertyImageServiceTest {

    private static final byte[] PNG_BYTES = new byte[]{
            (byte) 0x89, 'P', 'N', 'G', 0x0d, 0x0a, 0x1a, 0x0a,
            0x00, 0x00, 0x00, 0x0d
    };

    @Test
    void validPngIsStoredWithServerGeneratedKey() {
        CapturingStorage storage = new CapturingStorage();
        PropertyImageService service = new PropertyImageService(storage);
        MockMultipartFile file = new MockMultipartFile(
                "file", "../../room.png", "image/png", PNG_BYTES);

        PropertyImageUploadResponse response = service.upload(17L, file);

        assertTrue(response.getObjectKey().matches(
                "17-[0-9a-f-]{36}\\.png"));
        assertEquals("/api/property-images/" + response.getObjectKey(), response.getImageUrl());
        assertEquals("image/png", response.getContentType());
        assertEquals(PNG_BYTES.length, response.getSize());
        assertEquals(response.getObjectKey(), storage.objectKey);
        assertEquals(PNG_BYTES.length, storage.contentLength);
        assertEquals("image/png", storage.contentType);
        assertArrayEquals(PNG_BYTES, storage.content);
    }

    @Test
    void contentTypeMustMatchFileSignature() {
        PropertyImageService service = new PropertyImageService(new CapturingStorage());
        MockMultipartFile file = new MockMultipartFile(
                "file", "room.jpg", "image/jpeg", PNG_BYTES);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.upload(17L, file)
        );

        assertEquals(ErrorCode.INVALID_IMAGE_FILE, exception.getResponseCode());
    }

    @Test
    void oversizedImageIsRejectedBeforeStorage() {
        PropertyImageService service = new PropertyImageService(new CapturingStorage());
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "room.png",
                "image/png",
                new byte[(int) PropertyImageService.MAX_FILE_SIZE + 1]
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.upload(17L, file)
        );

        assertEquals(ErrorCode.IMAGE_FILE_TOO_LARGE, exception.getResponseCode());
    }

    @Test
    void imageUploadRequiresAuthentication() {
        PropertyImageService service = new PropertyImageService(new CapturingStorage());
        MockMultipartFile file = new MockMultipartFile(
                "file", "room.png", "image/png", PNG_BYTES);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.upload(null, file)
        );

        assertEquals(ErrorCode.AUTH_REQUIRED, exception.getResponseCode());
    }

    @Test
    void missingFileIsRejected() {
        PropertyImageService service = new PropertyImageService(new CapturingStorage());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.upload(17L, null)
        );

        assertEquals(ErrorCode.INVALID_IMAGE_FILE, exception.getResponseCode());
    }

    private static class CapturingStorage implements PropertyImageStorage {

        private String objectKey;
        private byte[] content;
        private long contentLength;
        private String contentType;

        @Override
        public void store(
                String objectKey,
                InputStream content,
                long contentLength,
                String contentType
        ) throws IOException {
            this.objectKey = objectKey;
            this.contentLength = contentLength;
            this.contentType = contentType;
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            content.transferTo(output);
            this.content = output.toByteArray();
        }

        @Override
        public Resource load(String objectKey) {
            return new ByteArrayResource(content == null ? new byte[0] : content);
        }
    }
}
