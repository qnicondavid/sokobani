package com.milandru.sokobani.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProgressStoreTest {

    @TempDir
    Path tempDir;

    @Test
    void load_missingFile_returnsEmptyProgress() {
        ProgressStore store = new ProgressStore(tempDir.resolve("nonexistent"));

        assertEquals(Progress.empty(), store.load());
    }

    @Test
    void saveThenLoad_aRealProgress_roundTripsEqual() {
        ProgressStore store = new ProgressStore(tempDir);
        Progress original = Progress.empty()
                .withSolved(0, 12, 4)
                .withSolved(1, 30, 9)
                .withSolved(3, 5, 1);

        boolean saved = store.save(original);
        Progress loaded = store.load();

        assertTrue(saved);
        assertEquals(original, loaded);
    }

    @Test
    void saveThenLoad_emptyProgress_roundTripsEqual() {
        ProgressStore store = new ProgressStore(tempDir);

        store.save(Progress.empty());
        Progress loaded = store.load();

        assertEquals(Progress.empty(), loaded);
    }

    @Test
    void save_createsTheDirectoryIfMissing() {
        Path nested = tempDir.resolve("does").resolve("not").resolve("exist");
        ProgressStore store = new ProgressStore(nested);

        boolean saved = store.save(Progress.empty().withSolved(0, 1, 1));

        assertTrue(saved);
        assertTrue(Files.isDirectory(nested));
        assertTrue(Files.isRegularFile(nested.resolve("progress.json")));
    }

    @Test
    void save_leavesNoTempFilesBehind() throws IOException {
        ProgressStore store = new ProgressStore(tempDir);

        store.save(Progress.empty().withSolved(0, 1, 1));

        try (var entries = Files.list(tempDir)) {
            List<Path> remaining = entries.toList();
            assertEquals(List.of(tempDir.resolve("progress.json")), remaining);
        }
    }

    @Test
    void load_corruptFile_returnsEmptyProgressAndDoesNotThrow() throws IOException {
        Path file = tempDir.resolve("progress.json");
        Files.writeString(file, "{ this is not valid json");
        ProgressStore store = new ProgressStore(tempDir);

        Progress loaded = store.load();

        assertEquals(Progress.empty(), loaded);
    }

    @Test
    void load_corruptFile_movesItAsideRatherThanDeletingIt() throws IOException {
        Path file = tempDir.resolve("progress.json");
        Files.writeString(file, "not json at all");
        ProgressStore store = new ProgressStore(tempDir);

        store.load();

        assertFalse(Files.exists(file));
        Path quarantined = tempDir.resolve("progress.json.corrupt");
        assertTrue(Files.exists(quarantined));
        assertEquals("not json at all", Files.readString(quarantined));
    }

    @Test
    void load_emptyFile_returnsEmptyProgressAndDoesNotThrow() throws IOException {
        Path file = tempDir.resolve("progress.json");
        Files.writeString(file, "");
        ProgressStore store = new ProgressStore(tempDir);

        Progress loaded = store.load();

        assertEquals(Progress.empty(), loaded);
        assertTrue(Files.exists(tempDir.resolve("progress.json.corrupt")));
    }

    @Test
    void load_fileOfOnlyWhitespace_returnsEmptyProgressAndDoesNotThrow() throws IOException {
        Path file = tempDir.resolve("progress.json");
        Files.writeString(file, "   \n\t  \n");
        ProgressStore store = new ProgressStore(tempDir);

        Progress loaded = store.load();

        assertEquals(Progress.empty(), loaded);
        assertTrue(Files.exists(tempDir.resolve("progress.json.corrupt")));
    }

    @Test
    void load_truncatedFile_returnsEmptyProgressAndDoesNotThrow() throws IOException {
        Path file = tempDir.resolve("progress.json");
        Files.writeString(file, "{\n  \"unlockedIndex\": 2,\n  \"levels\": {\n    \"0\": {\"solved\": tr");
        ProgressStore store = new ProgressStore(tempDir);

        Progress loaded = store.load();

        assertEquals(Progress.empty(), loaded);
    }

    @Test
    void load_deeplyNestedJson_returnsEmptyProgressRatherThanOverflowingTheStack() throws IOException {
        StringBuilder nested = new StringBuilder();
        for (int i = 0; i < 20_000; i++) {
            nested.append("{\"a\":");
        }
        nested.append("1");
        nested.append("}".repeat(20_000));
        Path file = tempDir.resolve("progress.json");
        Files.writeString(file, nested.toString());
        ProgressStore store = new ProgressStore(tempDir);

        Progress loaded = store.load();

        assertEquals(Progress.empty(), loaded);
    }

    @Test
    void save_directoryThatCannotBeCreated_returnsFalseRatherThanThrowing() throws IOException {
        Path blockingFile = tempDir.resolve("blocking-file");
        Files.writeString(blockingFile, "not a directory");
        ProgressStore store = new ProgressStore(blockingFile.resolve("nested"));

        boolean saved = store.save(Progress.empty());

        assertFalse(saved);
    }
}
