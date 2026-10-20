package io.github.hectorvent.floci.core.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.hectorvent.floci.testing.PerServicePersistentStorageProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static io.github.hectorvent.floci.testing.PerServicePersistentStorageProfile.STORAGE_DIR;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A per-service storage mode takes effect: with only ACM set to persistent, an ACM store writes
 * through to its file.
 *
 * <p>The ACM override comes from {@link PerServicePersistentStorageProfile}, which this class
 * shares with other classes that each call a different service.
 */
@QuarkusTest
@TestProfile(PerServicePersistentStorageProfile.class)
class StorageFactoryServiceCatalogIntegrationTest {

    private static final String FILE_NAME = "acm-test.json";

    @Inject
    StorageFactory storageFactory;

    @Test
    void acmStorageOverrideIsApplied() throws IOException {
        Path file = Path.of(STORAGE_DIR, FILE_NAME);
        StorageBackend<String, String> backend = storageFactory.create(
                "acm",
                FILE_NAME,
                new TypeReference<Map<String, String>>() {}
        );
        Files.deleteIfExists(file);
        try {
            backend.put("probe", "persisted");

            // A store in the test default mode, memory, never writes the file.
            assertTrue(Files.exists(file), "the ACM store should write through to " + file);
        } finally {
            backend.delete("probe");
            Files.deleteIfExists(file);
        }
    }
}
