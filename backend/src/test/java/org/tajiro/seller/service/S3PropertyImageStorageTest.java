package org.tajiro.seller.service;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Proxy;
import java.nio.file.NoSuchFileException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class S3PropertyImageStorageTest {

    @Test
    void imageIsUploadedToConfiguredBucketAndPrefix() throws Exception {
        AtomicReference<PutObjectRequest> capturedRequest = new AtomicReference<>();
        AtomicReference<byte[]> capturedContent = new AtomicReference<>();
        S3Client client = proxyClient((proxy, method, args) -> {
            if ("putObject".equals(method.getName())) {
                capturedRequest.set((PutObjectRequest) args[0]);
                RequestBody body = (RequestBody) args[1];
                capturedContent.set(body.contentStreamProvider().newStream().readAllBytes());
                return PutObjectResponse.builder().eTag("etag").build();
            }
            return defaultValue(method.getReturnType());
        });
        S3PropertyImageStorage storage = new S3PropertyImageStorage(
                client, "tajiro-images", "/property-images/");
        byte[] image = new byte[]{1, 2, 3, 4};

        storage.store(
                "3-image.jpg",
                new ByteArrayInputStream(image),
                image.length,
                "image/jpeg"
        );

        assertEquals("tajiro-images", capturedRequest.get().bucket());
        assertEquals("property-images/3-image.jpg", capturedRequest.get().key());
        assertEquals("image/jpeg", capturedRequest.get().contentType());
        assertArrayEquals(image, capturedContent.get());
    }

    @Test
    void imageCanBeReadFromS3() throws Exception {
        AtomicReference<GetObjectRequest> capturedRequest = new AtomicReference<>();
        byte[] image = new byte[]{5, 6, 7};
        S3Client client = proxyClient((proxy, method, args) -> {
            if ("getObject".equals(method.getName())) {
                capturedRequest.set((GetObjectRequest) args[0]);
                GetObjectResponse response = GetObjectResponse.builder()
                        .contentLength((long) image.length)
                        .build();
                return new ResponseInputStream<>(
                        response,
                        AbortableInputStream.create(new ByteArrayInputStream(image))
                );
            }
            return defaultValue(method.getReturnType());
        });
        S3PropertyImageStorage storage = new S3PropertyImageStorage(
                client, "tajiro-images", "property-images");

        Resource resource = storage.load("3-image.jpg");

        assertEquals("property-images/3-image.jpg", capturedRequest.get().key());
        assertEquals(image.length, resource.contentLength());
        assertArrayEquals(image, resource.getInputStream().readAllBytes());
    }

    @Test
    void missingS3ObjectIsMappedToNoSuchFile() {
        S3Client client = proxyClient((proxy, method, args) -> {
            if ("getObject".equals(method.getName())) {
                throw S3Exception.builder().statusCode(404).message("not found").build();
            }
            return defaultValue(method.getReturnType());
        });
        S3PropertyImageStorage storage = new S3PropertyImageStorage(
                client, "tajiro-images", "property-images");

        assertThrows(NoSuchFileException.class, () -> storage.load("missing.jpg"));
    }

    private static S3Client proxyClient(java.lang.reflect.InvocationHandler handler) {
        return (S3Client) Proxy.newProxyInstance(
                S3Client.class.getClassLoader(),
                new Class<?>[]{S3Client.class},
                handler
        );
    }

    private static Object defaultValue(Class<?> returnType) {
        if (!returnType.isPrimitive()) {
            return null;
        }
        if (returnType == boolean.class) {
            return false;
        }
        if (returnType == char.class) {
            return '\0';
        }
        return 0;
    }
}
