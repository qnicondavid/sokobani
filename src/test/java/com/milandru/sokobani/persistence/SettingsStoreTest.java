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

class SettingsStoreTest {

    @TempDir
    Path tempDir;

    @Test
    void load_missingFile_returnsDefault() {
        SettingsStore store = new SettingsStore(tempDir.resolve("nonexistent"));

        assertEquals(Settings.DEFAULT, store.load());
    }

    @Test
    void saveThenLoad_roundTripsEqual() {
        SettingsStore store = new SettingsStore(tempDir);
        Settings original = new Settings(true, false, true);

        boolean saved = store.save(original);
        Settings loaded = store.load();

        assertTrue(saved);
        assertEquals(original, loaded);
    }

    @Test
    void saveThenLoad_defaultSettings_roundTripsEqual() {
        SettingsStore store = new SettingsStore(tempDir);

        store.save(Settings.DEFAULT);
        Settings loaded = store.load();

        assertEquals(Settings.DEFAULT, loaded);
    }

    @Test
    void save_createsTheDirectoryIfMissing() {
        Path nested = tempDir.resolve("does").resolve("not").resolve("exist");
        SettingsStore store = new SettingsStore(nested);

        boolean saved = store.save(Settings.DEFAULT);

        assertTrue(saved);
        assertTrue(Files.isDirectory(nested));
        assertTrue(Files.isRegularFile(nested.resolve("settings.json")));
    }

    @Test
    void save_leavesNoTempFilesBehind() throws IOException {
        SettingsStore store = new SettingsStore(tempDir);

        store.save(new Settings(true, true, true));

        try (var entries = Files.list(tempDir)) {
            List<Path> remaining = entries.toList();
            assertEquals(List.of(tempDir.resolve("settings.json")), remaining);
        }
    }

    @Test
    void load_corruptFile_returnsDefaultAndQuarantines() throws IOException {
        Path file = tempDir.resolve("settings.json");
        Files.writeString(file, "{ not json");
        SettingsStore store = new SettingsStore(tempDir);

        assertEquals(Settings.DEFAULT, store.load());

        assertFalse(Files.exists(file));
        assertEquals("{ not json", Files.readString(tempDir.resolve("settings.json.corrupt")));
    }

    @Test
    void load_wrongValueType_returnsDefault() throws IOException {
        Files.writeString(tempDir.resolve("settings.json"), "{\"muted\": \"yes\"}");
        SettingsStore store = new SettingsStore(tempDir);

        assertEquals(Settings.DEFAULT, store.load());
    }

    @Test
    void load_partialJson_usesDefaultsForMissingKeys() throws IOException {
        Files.writeString(tempDir.resolve("settings.json"), "{\"muted\":true}");
        SettingsStore store = new SettingsStore(tempDir);

        assertEquals(new Settings(true, true, true), store.load());

        Files.writeString(tempDir.resolve("settings.json"), "{\"animation\":false}");
        assertEquals(new Settings(false, false, true), store.load());

        Files.writeString(tempDir.resolve("settings.json"), "{\"hints\":false}");
        assertEquals(new Settings(false, true, false), store.load());
    }

    @Test
    void load_emptyFile_returnsDefaultAndDoesNotThrow() throws IOException {
        Files.writeString(tempDir.resolve("settings.json"), "");
        SettingsStore store = new SettingsStore(tempDir);

        assertEquals(Settings.DEFAULT, store.load());
    }

    @Test
    void save_directoryThatCannotBeCreated_returnsFalseRatherThanThrowing() throws IOException {
        Path blockingFile = tempDir.resolve("blocking-file");
        Files.writeString(blockingFile, "not a directory");
        SettingsStore store = new SettingsStore(blockingFile.resolve("nested"));

        assertFalse(store.save(Settings.DEFAULT));
    }
}
