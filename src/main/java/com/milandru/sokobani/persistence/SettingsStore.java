package com.milandru.sokobani.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public final class SettingsStore {

    private static final String DIRECTORY_NAME = ".sokobani";

    private final Path directory;
    private final Path file;

    public SettingsStore(Path directory) {
        this.directory = Objects.requireNonNull(directory, "directory");
        this.file = directory.resolve("settings.json");
    }

    public static Path userHomeDirectory() {
        return Path.of(System.getProperty("user.home"), DIRECTORY_NAME);
    }

    public static SettingsStore atUserHome() {
        return new SettingsStore(userHomeDirectory());
    }

    public Settings load() {
        if (!Files.isRegularFile(file)) {
            return Settings.DEFAULT;
        }
        String content;
        try {
            content = Files.readString(file);
        } catch (IOException unreadable) {
            quarantine();
            return Settings.DEFAULT;
        }
        try {
            return SettingsJson.parse(content);
        } catch (RuntimeException malformed) {
            quarantine();
            return Settings.DEFAULT;
        }
    }

    public boolean save(Settings settings) {
        Objects.requireNonNull(settings, "settings");
        Path temp = null;
        try {
            Files.createDirectories(directory);
            temp = Files.createTempFile(directory, "settings", ".tmp");
            Files.writeString(temp, SettingsJson.serialize(settings));
            Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException failure) {
            deleteQuietly(temp);
            return false;
        }
    }

    private void quarantine() {
        try {
            Path corrupt = directory.resolve("settings.json.corrupt");
            Files.move(file, corrupt, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignored) {
        }
    }

    private static void deleteQuietly(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }
}
