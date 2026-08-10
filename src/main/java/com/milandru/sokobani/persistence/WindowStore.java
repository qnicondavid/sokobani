package com.milandru.sokobani.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Optional;

public final class WindowStore {

    private static final String DIRECTORY_NAME = ".sokobani";

    private final Path directory;
    private final Path file;

    public WindowStore(Path directory) {
        this.directory = Objects.requireNonNull(directory, "directory");
        this.file = directory.resolve("window.json");
    }

    public static Path userHomeDirectory() {
        return Path.of(System.getProperty("user.home"), DIRECTORY_NAME);
    }

    public static WindowStore atUserHome() {
        return new WindowStore(userHomeDirectory());
    }

    public Optional<WindowGeometry> load() {
        if (!Files.isRegularFile(file)) {
            return Optional.empty();
        }
        String content;
        try {
            content = Files.readString(file);
        } catch (IOException unreadable) {
            quarantine();
            return Optional.empty();
        }
        try {
            return WindowJson.parse(content);
        } catch (RuntimeException malformed) {
            quarantine();
            return Optional.empty();
        }
    }

    public boolean save(WindowGeometry geometry) {
        Objects.requireNonNull(geometry, "geometry");
        Path temp = null;
        try {
            Files.createDirectories(directory);
            temp = Files.createTempFile(directory, "window", ".tmp");
            Files.writeString(temp, WindowJson.serialize(geometry));
            Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException failure) {
            deleteQuietly(temp);
            return false;
        }
    }

    private void quarantine() {
        try {
            Path corrupt = directory.resolve("window.json.corrupt");
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
