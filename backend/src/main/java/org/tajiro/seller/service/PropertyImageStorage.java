package org.tajiro.seller.service;

import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

/**
 * 매물 이미지의 실제 파일 저장 방식을 추상화한다.
 */
public interface PropertyImageStorage extends AutoCloseable {

    void store(
            String objectKey,
            InputStream content,
            long contentLength,
            String contentType
    ) throws IOException;

    Resource load(String objectKey) throws IOException;

    @Override
    default void close() {
        // 별도의 종료 처리가 필요 없는 저장소 구현을 위한 기본 동작
    }
}
