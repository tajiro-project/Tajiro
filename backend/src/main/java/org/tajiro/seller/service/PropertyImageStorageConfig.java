package org.tajiro.seller.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;
import java.util.Locale;

@Configuration
public class PropertyImageStorageConfig {

    @Bean(destroyMethod = "close")
    public PropertyImageStorage propertyImageStorage(
            @Value("${property.image.storage-type:local}") String storageType,
            @Value("${property.image.storage-directory:}") String localDirectory,
            @Value("${property.image.s3.bucket:}") String bucket,
            @Value("${property.image.s3.region:ap-northeast-2}") String region,
            @Value("${property.image.s3.prefix:property-images}") String prefix,
            @Value("${property.image.s3.endpoint:}") String endpoint,
            @Value("${property.image.s3.path-style-access:false}") boolean pathStyleAccess
    ) {
        String normalizedType = storageType == null
                ? "local"
                : storageType.trim().toLowerCase(Locale.ROOT);

        if ("local".equals(normalizedType)) {
            return new LocalPropertyImageStorage(localDirectory);
        }
        if (!"s3".equals(normalizedType)) {
            throw new IllegalArgumentException(
                    "지원하지 않는 매물 이미지 저장소 타입입니다: " + storageType);
        }

        return new S3PropertyImageStorage(
                createS3Client(region, endpoint, pathStyleAccess),
                bucket,
                prefix
        );
    }

    private S3Client createS3Client(
            String region,
            String endpoint,
            boolean pathStyleAccess
    ) {
        if (region == null || region.isBlank()) {
            throw new IllegalArgumentException("S3 리전이 설정되지 않았습니다.");
        }

        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(region.trim()));

        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint.trim()));
        }
        if (pathStyleAccess) {
            builder.serviceConfiguration(S3Configuration.builder()
                    .pathStyleAccessEnabled(true)
                    .build());
        }

        return builder.build();
    }
}
