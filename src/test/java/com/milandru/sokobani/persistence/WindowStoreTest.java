package com.milandru.sokobani.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WindowStoreTest {

    @TempDir
    Path tempDir;

    @Test
    void load_missingFile_returnsEmpty() {
        WindowStore store = new WindowStore(tempDir.resolve("nonexistent"));

        assertEquals(Optional.empty(), store.load());
    }

    @Test
    void saveThenLoad_roundTripsEqual() {
        WindowStore store = new WindowStore(tempDir);
        WindowGeometry geometry = new WindowGeometry(900, 780);

        boolean saved = store.save(geometry);
        Optional<WindowGeometry> loaded = store.load();

        assertTrue(saved);
        assertEquals(Optional.of(geometry), loaded);
    }

    @Test
    void save_createsTheDirectoryIfMissing() {
        Path nested = tempDir.resolve("does").resolve("not").resolve("exist");
        WindowStore store = new WindowStore(nested);

        boolean saved = store.save(new WindowGeometry(560, 560));

        assertTrue(saved);
        assertTrue(Files.isDirectory(nested));
        assertTrue(Files.isRegularFile(nested.resolve("window.json")));
    }

    @Test
    void save_leavesNoTempFilesBehind() throws IOException {
        WindowStore store = new WindowStore(tempDir);

        store.save(new WindowGeometry(640, 480));

        try (var entries = Files.list(tempDir)) {
            List<Path> remaining = entries.toList();
            assertEquals(List.of(tempDir.resolve("window.json")), remaining);
        }
    }

    @Test
    void load_corruptFile_returnsEmptyAndQuarantines() throws IOException {
        Path file = tempDir.resolve("window.json");
        Files.writeString(file, "{ not json");
        WindowStore store = new WindowStore(tempDir);

        assertEquals(Optional.empty(), store.load());

        assertFalse(Files.exists(file));
        assertEquals("{ not json", Files.readString(tempDir.resolve("window.json.corrupt")));
    }

    @Test
    void load_wrongValueType_returnsEmpty() throws IOException {
        Files.writeString(tempDir.resolve("window.json"), "{\"width\": \"wide\"}");
        WindowStore store = new WindowStore(tempDir);

        assertEquals(Optional.empty(), store.load());
    }

    @Test
    void load_missingKey_returnsEmpty() throws IOException {
        Files.writeString(tempDir.resolve("window.json"), "{\"width\":900}");
        WindowStore store = new WindowStore(tempDir);

        assertEquals(Optional.empty(), store.load());
    }

    @Test
    void load_nonPositiveDimensions_returnsEmpty() throws IOException {
        Files.writeString(tempDir.resolve("window.json"), "{\"width\":0,\"height\":780}");
        WindowStore store = new WindowStore(tempDir);

        assertEquals(Optional.empty(), store.load());
    }

    @Test
    void load_emptyFile_returnsEmptyAndDoesNotThrow() throws IOException {
        Files.writeString(tempDir.resolve("window.json"), "");
        WindowStore store = new WindowStore(tempDir);

        assertEquals(Optional.empty(), store.load());
    }

    @Test
    void save_directoryThatCannotBeCreated_returnsFalseRatherThanThrowing() throws IOException {
        Path blockingFile = tempDir.resolve("blocking-file");
        Files.writeString(blockingFile, "not a directory");
        WindowStore store = new WindowStore(blockingFile.resolve("nested"));

        assertFalse(store.save(new WindowGeometry(900, 780)));
    }
}
