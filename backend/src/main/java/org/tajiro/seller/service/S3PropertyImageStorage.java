package org.tajiro.seller.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;

/**
 * 비공개 S3 버킷에 매물 이미지를 저장하고 조회한다.
 */
@Slf4j
public class S3PropertyImageStorage implements PropertyImageStorage {

    private final S3Client s3Client;
    private final String bucket;
    private final String prefix;

    public S3PropertyImageStorage(S3Client s3Client, String bucket, String prefix) {
        this.s3Client = s3Client;
        this.bucket = requireText(bucket, "S3 버킷 이름");
        this.prefix = normalizePrefix(prefix);
    }

    @Override
    public void store(
            String objectKey,
            InputStream content,
            long contentLength,
            String contentType
    ) throws IOException {
        String s3Key = s3Key(objectKey);
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(s3Key)
                .contentType(contentType)
                .build();

        try {
            s3Client.putObject(
                    request,
                    RequestBody.fromInputStream(content, contentLength)
            );
        } catch (S3Exception exception) {
            log.error(
                    "S3 property image upload failed. bucket={}, key={}, statusCode={}, awsErrorCode={}, requestId={}",
                    bucket,
                    s3Key,
                    exception.statusCode(),
                    exception.awsErrorDetails() == null
                            ? null
                            : exception.awsErrorDetails().errorCode(),
                    exception.requestId(),
                    exception
            );
            throw new IOException("S3에 매물 이미지를 저장하지 못했습니다.", exception);
        } catch (SdkClientException exception) {
            log.error(
                    "S3 client failed while uploading a property image. bucket={}, key={}",
                    bucket,
                    s3Key,
                    exception
            );
            throw new IOException("S3에 매물 이미지를 저장하지 못했습니다.", exception);
        }
    }

    @Override
    public Resource load(String objectKey) throws IOException {
        String s3Key = s3Key(objectKey);
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(s3Key)
                .build();

        try {
            ResponseInputStream<GetObjectResponse> response = s3Client.getObject(request);
            return new S3ObjectResource(objectKey, response);
        } catch (S3Exception exception) {
            if (exception.statusCode() == 404) {
                throw new NoSuchFileException(objectKey);
            }
            log.error(
                    "S3 property image read failed. bucket={}, key={}, statusCode={}, awsErrorCode={}, requestId={}",
                    bucket,
                    s3Key,
                    exception.statusCode(),
                    exception.awsErrorDetails() == null
                            ? null
                            : exception.awsErrorDetails().errorCode(),
                    exception.requestId(),
                    exception
            );
            throw new IOException("S3에서 매물 이미지를 조회하지 못했습니다.", exception);
        } catch (SdkClientException exception) {
            log.error(
                    "S3 client failed while reading a property image. bucket={}, key={}",
                    bucket,
                    s3Key,
                    exception
            );
            throw new IOException("S3에서 매물 이미지를 조회하지 못했습니다.", exception);
        }
    }

    @Override
    public void close() {
        s3Client.close();
    }

    private String s3Key(String objectKey) {
        return prefix.isEmpty() ? objectKey : prefix + "/" + objectKey;
    }

    private static String normalizePrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return "";
        }
        return prefix.trim().replaceAll("^/+|/+$", "");
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + "이(가) 설정되지 않았습니다.");
        }
        return value.trim();
    }

    private static final class S3ObjectResource extends InputStreamResource {

        private final String filename;
        private final long contentLength;

        private S3ObjectResource(
                String filename,
                ResponseInputStream<GetObjectResponse> response
        ) {
            super(response);
            this.filename = filename;
            this.contentLength = response.response().contentLength();
        }

        @Override
        public String getFilename() {
            return filename;
        }

        @Override
        public long contentLength() {
            return contentLength;
        }
    }
}
