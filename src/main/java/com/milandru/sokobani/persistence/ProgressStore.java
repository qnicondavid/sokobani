package com.milandru.sokobani.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public final class ProgressStore {

    private static final String DIRECTORY_NAME = ".sokobani";

    private final Path directory;
    private final Path file;

    public ProgressStore(Path directory) {
        this.directory = Objects.requireNonNull(directory, "directory");
        this.file = directory.resolve("progress.json");
    }

    public static Path userHomeDirectory() {
        return Path.of(System.getProperty("user.home"), DIRECTORY_NAME);
    }

    public static ProgressStore atUserHome() {
        return new ProgressStore(userHomeDirectory());
    }

    public Progress load() {
        if (!Files.isRegularFile(file)) {
            return Progress.empty();
        }
        String content;
        try {
            content = Files.readString(file);
        } catch (IOException unreadable) {
            quarantine();
            return Progress.empty();
        }
        try {
            return ProgressJson.parse(content);
        } catch (RuntimeException malformed) {
            quarantine();
            return Progress.empty();
        }
    }

    public boolean save(Progress progress) {
        Objects.requireNonNull(progress, "progress");
        Path temp = null;
        try {
            Files.createDirectories(directory);
            temp = Files.createTempFile(directory, "progress", ".tmp");
            Files.writeString(temp, ProgressJson.serialize(progress));
            Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException failure) {
            deleteQuietly(temp);
            return false;
        }
    }

    private void quarantine() {
        try {
            Path corrupt = directory.resolve("progress.json.corrupt");
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
