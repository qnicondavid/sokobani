package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.persistence.Settings;
import com.milandru.sokobani.ui.Surface;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SettingsViewTest {

    @Test
    void render_drawnTwice_producesIdenticalPixels() {
        assertArrayEquals(settings(0, Settings.DEFAULT).tones(), settings(0, Settings.DEFAULT).tones());
    }

    @Test
    void render_isTheSpecifiedSurfaceSize() {
        Surface surface = settings(0, Settings.DEFAULT);

        assertEquals(SettingsView.WIDTH, surface.width());
        assertEquals(SettingsView.HEIGHT, surface.height());
    }

    @Test
    void theSelectionBarMovesWithTheSelection() {
        assertFalse(Arrays.equals(settings(0, Settings.DEFAULT).tones(), settings(1, Settings.DEFAULT).tones()));
    }

    @Test
    void theValuesReflectTheSettings() {
        assertFalse(Arrays.equals(
                settings(0, Settings.DEFAULT).tones(),
                settings(0, new Settings(true, false, true)).tones()));
    }

    @Test
    void theHintsRowReflectsTheSetting() {
        assertFalse(Arrays.equals(
                settings(0, Settings.DEFAULT).tones(),
                settings(0, new Settings(false, true, false)).tones()));
    }

    @Test
    void overRow_answersTheSelectionBandOfEachRow() {
        assertTrue(SettingsView.overRow(50, 130, 0));
        assertTrue(SettingsView.overRow(200, 130, 0));
        assertFalse(SettingsView.overRow(20, 130, 0));
        assertFalse(SettingsView.overRow(50, 110, 0));
        assertTrue(SettingsView.overRow(50, 130 + 18, 1));
        assertFalse(SettingsView.overRow(50, 130, 1));
        assertTrue(SettingsView.overRow(50, 130 + 2 * 18, 2));
        assertFalse(SettingsView.overRow(50, 130, 2));
    }

    private static Surface settings(int selection, Settings settings) {
        Surface surface = new Surface(SettingsView.WIDTH, SettingsView.HEIGHT);
        SettingsView.render(surface, BoardFixture.typeSetter(), settings, selection);
        return surface;
    }
}
