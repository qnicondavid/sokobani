package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.persistence.ProgressStore;
import com.milandru.sokobani.ui.Theme;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class ThemeStore {

    private static final String FILE_NAME = "theme.txt";

    private final Path directory;
    private final Path file;

    public ThemeStore(Path directory) {
        this.directory = Objects.requireNonNull(directory, "directory");
        this.file = directory.resolve(FILE_NAME);
    }

    public static ThemeStore atUserHome() {
        return new ThemeStore(ProgressStore.userHomeDirectory());
    }

    public Theme load() {
        if (!Files.isRegularFile(file)) {
            return Theme.DEFAULT;
        }
        try {
            int chosen = Integer.parseInt(Files.readString(file).trim());
            return chosen >= 0 && chosen < Theme.ALL.size() ? Theme.ALL.get(chosen) : Theme.DEFAULT;
        } catch (IOException | NumberFormatException unusable) {
            return Theme.DEFAULT;
        }
    }

    public boolean save(Theme theme) {
        Objects.requireNonNull(theme, "theme");
        int chosen = Theme.ALL.indexOf(theme);
        if (chosen < 0) {
            return false;
        }
        try {
            Files.createDirectories(directory);
            Files.writeString(file, String.valueOf(chosen));
            return true;
        } catch (IOException unwritable) {
            return false;
        }
    }
}
