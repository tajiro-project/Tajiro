package org.tajiro.seller.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocalPropertyImageStorageTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void storedImageCanBeLoaded() throws IOException {
        LocalPropertyImageStorage storage = new LocalPropertyImageStorage(temporaryDirectory);
        byte[] image = new byte[]{1, 2, 3, 4};

        storage.store("1-00000000-0000-0000-0000-000000000000.jpg",
                new ByteArrayInputStream(image), image.length, "image/jpeg");
        Resource loaded = storage.load(
                "1-00000000-0000-0000-0000-000000000000.jpg");

        assertArrayEquals(image, loaded.getInputStream().readAllBytes());
    }

    @Test
    void pathTraversalIsRejected() {
        LocalPropertyImageStorage storage = new LocalPropertyImageStorage(temporaryDirectory);

        assertThrows(IOException.class, () -> storage.store(
                "../outside.jpg",
                new ByteArrayInputStream(new byte[]{1}),
                1,
                "image/jpeg"
        ));
    }
}
