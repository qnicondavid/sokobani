package com.milandru.sokobani.ui;

import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Deque;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FontLoaderRasterTest {

    private static final int CAPTION_SIZE = 10;
    private static final int[] CAPTION_BAND = {10, 11, 12};
    private static final String GLYPHS_WITH_A_COUNTER = "0689BDOPQR";

    @Test
    void paint_theFigureEightAtTheCaptionSize_leavesItsCounterOpen() {
        int counter = counterOf('8', CAPTION_SIZE);

        assertTrue(counter > 0,
                "the counter of 8 at " + CAPTION_SIZE + "px measured " + counter
                        + " pixels of enclosed paper, so the glyph has filled in with ink");
    }

    @Test
    void paint_everyRoundGlyphOfTheCaptionStyle_keepsAnOpenCounter() {
        StringBuilder closed = new StringBuilder();
        for (char glyph : GLYPHS_WITH_A_COUNTER.toCharArray()) {
            int counter = counterOf(glyph, CAPTION_SIZE);
            if (counter == 0) {
                closed.append(closed.isEmpty() ? "" : ", ").append(glyph);
            }
        }

        assertTrue(closed.isEmpty(),
                "the counters of " + closed + " at " + CAPTION_SIZE + "px measured 0 pixels of enclosed paper");
    }

    @Test
    void paint_theFigureEightAcrossTheCaptionBand_neverFillsIn() {
        for (int size : CAPTION_BAND) {
            int counter = counterOf('8', size);

            assertTrue(counter > 0, "the counter of 8 at " + size + "px measured " + counter + " pixels");
        }
    }

    @Test
    void paint_aGlyphAtTheCaptionSize_putsInkOnTheSurface() {
        Surface surface = surfaceFor(CAPTION_SIZE);
        FxToolkit.run(() -> new FontLoader().paint(surface, CAPTION_SIZE, 2 * CAPTION_SIZE, '8', CAPTION_SIZE,
                Surface.INK));

        assertTrue(inkOf(surface) > 0, "the rasteriser returned a blank glyph");
    }

    private static int counterOf(char glyph, int size) {
        Surface surface = surfaceFor(size);
        FxToolkit.run(() -> new FontLoader().paint(surface, size, 2 * size, glyph, size, Surface.INK));
        return enclosedPaperOf(surface);
    }

    private static Surface surfaceFor(int size) {
        return new Surface(size * 4, size * 4);
    }

    private static int inkOf(Surface surface) {
        int ink = 0;
        for (int y = 0; y < surface.height(); y++) {
            for (int x = 0; x < surface.width(); x++) {
                if (Threshold.isInk(surface.toneAt(x, y))) {
                    ink++;
                }
            }
        }
        return ink;
    }

    private static int enclosedPaperOf(Surface surface) {
        boolean[] reached = new boolean[surface.width() * surface.height()];
        Deque<int[]> pending = new ArrayDeque<>();
        for (int x = 0; x < surface.width(); x++) {
            visit(surface, reached, pending, x, 0);
            visit(surface, reached, pending, x, surface.height() - 1);
        }
        for (int y = 0; y < surface.height(); y++) {
            visit(surface, reached, pending, 0, y);
            visit(surface, reached, pending, surface.width() - 1, y);
        }
        while (!pending.isEmpty()) {
            int[] at = pending.remove();
            visit(surface, reached, pending, at[0] - 1, at[1]);
            visit(surface, reached, pending, at[0] + 1, at[1]);
            visit(surface, reached, pending, at[0], at[1] - 1);
            visit(surface, reached, pending, at[0], at[1] + 1);
        }
        int enclosed = 0;
        for (int y = 0; y < surface.height(); y++) {
            for (int x = 0; x < surface.width(); x++) {
                if (!Threshold.isInk(surface.toneAt(x, y)) && !reached[y * surface.width() + x]) {
                    enclosed++;
                }
            }
        }
        return enclosed;
    }

    private static void visit(Surface surface, boolean[] reached, Deque<int[]> pending, int x, int y) {
        if (x < 0 || x >= surface.width() || y < 0 || y >= surface.height()) {
            return;
        }
        int index = y * surface.width() + x;
        if (reached[index] || Threshold.isInk(surface.toneAt(x, y))) {
            return;
        }
        reached[index] = true;
        pending.add(new int[]{x, y});
    }
}
