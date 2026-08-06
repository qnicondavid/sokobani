package com.milandru.sokobani.ui;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThresholdTest {

    @Test
    void isInk_belowTheLevel_isTrue() {
        assertTrue(Threshold.isInk(0));
        assertTrue(Threshold.isInk(127));
    }

    @Test
    void isInk_atOrAboveTheLevel_isFalse() {
        assertFalse(Threshold.isInk(128));
        assertFalse(Threshold.isInk(255));
    }

    @Test
    void paletteMap_renderedSurface_containsExactlyTwoDistinctColours() {
        Surface surface = SurfacePatterns.draw();

        int[] pixels = Threshold.paletteMap(surface, Theme.CATALOGUE);

        Set<Integer> distinct = new HashSet<>();
        for (int pixel : pixels) {
            distinct.add(pixel);
        }

        assertEquals(2, distinct.size());
        assertTrue(distinct.contains(Theme.CATALOGUE.ink()));
        assertTrue(distinct.contains(Theme.CATALOGUE.paper()));
    }

    @Test
    void paletteMap_everyTheme_producesTheSameInkMaskGeometry() {
        Surface surface = SurfacePatterns.draw();

        boolean[] catalogueMask = inkMask(Threshold.paletteMap(surface, Theme.CATALOGUE), Theme.CATALOGUE);

        for (Theme theme : Theme.ALL) {
            boolean[] mask = inkMask(Threshold.paletteMap(surface, theme), theme);
            assertTrue(Arrays.equals(catalogueMask, mask), "geometry differs for " + theme);
        }
    }

    @Test
    void paletteMap_lightOnDarkTheme_stillProducesExactlyTwoColours() {
        Surface surface = SurfacePatterns.draw();

        int[] pixels = Threshold.paletteMap(surface, Theme.PHOSPHOR);

        Set<Integer> distinct = new HashSet<>();
        for (int pixel : pixels) {
            distinct.add(pixel);
        }

        assertEquals(2, distinct.size());
    }

    private static boolean[] inkMask(int[] pixels, Theme theme) {
        boolean[] mask = new boolean[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            mask[i] = pixels[i] == theme.ink();
        }
        return mask;
    }
}
