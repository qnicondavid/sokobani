package com.milandru.sokobani.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.image.PixelReader;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class DisplayTest {

    private static final Theme THEME = Theme.CATALOGUE;
    private static final int INK = 0xFF000000 | Theme.CATALOGUE.ink();
    private static final int PAPER = 0xFF000000 | Theme.CATALOGUE.paper();
    private static final int[] CHEQUER = {Theme.CATALOGUE.ink(), Theme.CATALOGUE.paper(),
            Theme.CATALOGUE.paper(), Theme.CATALOGUE.ink()};

    @Test
    void present_aBaseImageOnAnExactlySizedCanvas_fillsItEdgeToEdge() {
        int[] argb = presented(6, 6, CHEQUER, 2, 2, 3);

        assertEquals(INK, argb[0]);
        assertEquals(PAPER, argb[5]);
        assertEquals(PAPER, argb[5 * 6]);
        assertEquals(INK, argb[5 * 6 + 5]);
    }

    @Test
    void present_everyBasePixel_becomesASolidBlockOfTheScaleFactor() {
        int[] argb = presented(6, 6, CHEQUER, 2, 2, 3);

        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 6; x++) {
                int expected = (x / 3 + y / 3) % 2 == 0 ? INK : PAPER;
                assertEquals(expected, argb[y * 6 + x], "(" + x + ", " + y + ")");
            }
        }
    }

    @Test
    void present_anOddMarginOnEachAxis_centresTheImageAndLeavesPaperAround() {
        int[] argb = presented(11, 11, CHEQUER, 2, 2, 3);

        assertEquals(PAPER, argb[1 * 11 + 1]);
        assertEquals(INK, argb[2 * 11 + 2]);
        assertEquals(INK, argb[7 * 11 + 7]);
        assertEquals(PAPER, argb[8 * 11 + 8]);
        assertEquals(PAPER, argb[10 * 11 + 10]);
    }

    @Test
    void present_aCanvasWithAFractionalHalfMargin_roundsTheOffsetDown() {
        int[] argb = presented(7, 7, CHEQUER, 2, 2, 3);

        assertEquals(INK, argb[0]);
        assertEquals(PAPER, argb[6 * 7 + 6]);
    }

    @Test
    void present_theSameBaseTwice_paintsIdenticalPixels() {
        assertEquals(-1, mismatch(presented(11, 11, CHEQUER, 2, 2, 3), presented(11, 11, CHEQUER, 2, 2, 3)));
    }

    @Test
    void present_overAPreviousFrame_clearsItToPaperFirst() {
        int[] first = presented(11, 11, CHEQUER, 2, 2, 3);
        int[] second = FxToolkit.on(() -> {
            Canvas canvas = new Canvas(11, 11);
            Display.present(canvas, THEME, CHEQUER, 2, 2, 3);
            Display.present(canvas, THEME, new int[]{Theme.CATALOGUE.paper()}, 1, 1, 1);
            return read(canvas, 11, 11);
        });

        assertNotEquals(-1, mismatch(first, second));
        for (int pixel : second) {
            assertEquals(PAPER, pixel);
        }
    }

    @Test
    void present_aThemeWithADifferentPaper_paintsTheSurroundInThatPaper() {
        int[] argb = FxToolkit.on(() -> {
            Canvas canvas = new Canvas(11, 11);
            Display.present(canvas, Theme.PHOSPHOR, new int[]{Theme.PHOSPHOR.ink()}, 1, 1, 3);
            return read(canvas, 11, 11);
        });

        assertEquals(0xFF000000 | Theme.PHOSPHOR.paper(), argb[0]);
        assertEquals(0xFF000000 | Theme.PHOSPHOR.ink(), argb[5 * 11 + 5]);
    }

    private static int[] presented(int canvasWidth, int canvasHeight, int[] base, int baseWidth, int baseHeight,
                                   int factor) {
        return FxToolkit.on(() -> {
            Canvas canvas = new Canvas(canvasWidth, canvasHeight);
            Display.present(canvas, THEME, base, baseWidth, baseHeight, factor);
            return read(canvas, canvasWidth, canvasHeight);
        });
    }

    private static int[] read(Canvas canvas, int width, int height) {
        PixelReader reader = canvas.snapshot(null, null).getPixelReader();
        int[] argb = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                argb[y * width + x] = reader.getArgb(x, y);
            }
        }
        return argb;
    }

    private static int mismatch(int[] left, int[] right) {
        for (int i = 0; i < left.length; i++) {
            if (left[i] != right[i]) {
                return i;
            }
        }
        return -1;
    }
}
