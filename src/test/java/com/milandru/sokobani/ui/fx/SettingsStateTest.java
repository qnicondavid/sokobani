package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.persistence.Settings;
import com.milandru.sokobani.persistence.SettingsStore;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SettingsStateTest {

    @TempDir
    Path tempDir;

    @Test
    void constructor_loadsThePersistedSettings() {
        SettingsStore store = new SettingsStore(tempDir);
        store.save(new Settings(true, false, true));

        SettingsState state = new SettingsState(store);

        assertTrue(state.muted());
        assertFalse(state.animationEnabled());
    }

    @Test
    void constructor_withoutAFile_usesDefaults() {
        SettingsState state = new SettingsState(new SettingsStore(tempDir));

        assertEquals(Settings.DEFAULT, state.snapshot());
    }

    @Test
    void toggleMuted_flipsAndPersists() {
        SettingsStore store = new SettingsStore(tempDir);
        SettingsState state = new SettingsState(store);

        state.toggleMuted();
        state.toggleMuted();

        assertFalse(state.muted());
        assertEquals(Settings.DEFAULT, store.load());
    }

    @Test
    void toggleAnimation_flipsAndPersists() {
        SettingsStore store = new SettingsStore(tempDir);
        SettingsState state = new SettingsState(store);

        state.toggleAnimation();

        assertFalse(state.animationEnabled());
        assertEquals(new Settings(false, false, true), store.load());
    }

    @Test
    void toggleHints_flipsAndPersists() {
        SettingsStore store = new SettingsStore(tempDir);
        SettingsState state = new SettingsState(store);

        state.toggleHints();

        assertFalse(state.hintsEnabled());
        assertEquals(new Settings(false, true, false), store.load());
    }

    @Test
    void snapshot_reflectsTheCurrentState() {
        SettingsState state = new SettingsState(new SettingsStore(tempDir));

        state.toggleMuted();
        state.toggleAnimation();

        assertEquals(new Settings(true, false, true), state.snapshot());
    }
}
