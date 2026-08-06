package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.ui.Theme;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThemeStoreTest {

    @TempDir
    Path directory;

    @Test
    void load_withNothingSaved_isTheDefaultTheme() {
        assertEquals(Theme.DEFAULT, new ThemeStore(directory).load());
    }

    @Test
    void save_thenLoad_returnsTheSameTheme() {
        ThemeStore store = new ThemeStore(directory);

        for (Theme theme : Theme.ALL) {
            assertTrue(store.save(theme));
            assertEquals(theme, store.load());
        }
    }

    @Test
    void save_createsTheDirectoryWhenItIsMissing() {
        Path nested = directory.resolve("missing").resolve("deeper");

        assertTrue(new ThemeStore(nested).save(Theme.PHOSPHOR));
        assertEquals(Theme.PHOSPHOR, new ThemeStore(nested).load());
    }

    @Test
    void load_gibberish_fallsBackToTheDefaultTheme() throws IOException {
        Files.writeString(directory.resolve("theme.txt"), "cyanotype");

        assertEquals(Theme.DEFAULT, new ThemeStore(directory).load());
    }

    @Test
    void load_anIndexOutsideThePalette_fallsBackToTheDefaultTheme() throws IOException {
        Files.writeString(directory.resolve("theme.txt"), String.valueOf(Theme.ALL.size()));

        assertEquals(Theme.DEFAULT, new ThemeStore(directory).load());
    }

    @Test
    void load_aNegativeIndex_fallsBackToTheDefaultTheme() throws IOException {
        Files.writeString(directory.resolve("theme.txt"), "-1");

        assertEquals(Theme.DEFAULT, new ThemeStore(directory).load());
    }

    @Test
    void load_surroundingWhitespace_isIgnored() throws IOException {
        Files.writeString(directory.resolve("theme.txt"), "  3\n");

        assertEquals(Theme.ALL.get(3), new ThemeStore(directory).load());
    }

    @Test
    void save_aThemeOutsideThePalette_isRefusedAndLeavesTheStoredChoiceAlone() {
        ThemeStore store = new ThemeStore(directory);
        store.save(Theme.PHOSPHOR);

        assertFalse(store.save(new Theme(0x010203, 0x040506)));
        assertEquals(Theme.PHOSPHOR, store.load());
    }

    @Test
    void theStore_sitsBesideProgressRatherThanInsideIt() {
        new ThemeStore(directory).save(Theme.BULLETIN);

        assertTrue(Files.isRegularFile(directory.resolve("theme.txt")));
        assertFalse(Files.exists(directory.resolve("progress.json")));
    }
}
