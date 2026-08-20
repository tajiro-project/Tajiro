package org.tajiro.seller.service;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 로컬 개발 환경에서 사용하는 파일 시스템 기반 이미지 저장소.
 *
 * <p>저장 위치는 WAR 외부 디렉터리여야 재배포 시 파일이 유지된다.</p>
 */
public class LocalPropertyImageStorage implements PropertyImageStorage {

    private static final String DEFAULT_DIRECTORY_NAME = "tajiro-property-images";

    private final Path rootDirectory;

    public LocalPropertyImageStorage(String configuredDirectory) {
        this(resolveRootDirectory(configuredDirectory));
    }

    LocalPropertyImageStorage(Path rootDirectory) {
        this.rootDirectory = rootDirectory.toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootDirectory);
        } catch (IOException exception) {
            throw new IllegalStateException("매물 이미지 저장 디렉터리를 생성할 수 없습니다.", exception);
        }
    }

    @Override
    public void store(
            String objectKey,
            InputStream content,
            long contentLength,
            String contentType
    ) throws IOException {
        Path target = resolveSafely(objectKey);
        Files.copy(content, target);
    }

    @Override
    public Resource load(String objectKey) throws IOException {
        Path target = resolveSafely(objectKey);
        if (!Files.isRegularFile(target)) {
            throw new NoSuchFileException(objectKey);
        }
        return new FileSystemResource(target);
    }

    private Path resolveSafely(String objectKey) throws IOException {
        if (objectKey == null || objectKey.isBlank()) {
            throw new IOException("이미지 객체 키가 비어 있습니다.");
        }

        Path resolved = rootDirectory.resolve(objectKey).normalize();
        if (!resolved.startsWith(rootDirectory) || !rootDirectory.equals(resolved.getParent())) {
            throw new IOException("유효하지 않은 이미지 객체 키입니다.");
        }
        return resolved;
    }

    private static Path resolveRootDirectory(String configuredDirectory) {
        if (configuredDirectory == null || configuredDirectory.isBlank()) {
            return Paths.get(System.getProperty("java.io.tmpdir"), DEFAULT_DIRECTORY_NAME);
        }
        return Paths.get(configuredDirectory.trim());
    }
}
