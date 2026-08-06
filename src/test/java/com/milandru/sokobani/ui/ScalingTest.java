package com.milandru.sokobani.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScalingTest {

    @Test
    void factor_zeroOrNegativeBaseWidth_throws() {
        assertThrows(IllegalArgumentException.class, () -> Scaling.factor(1000, 1000, 0, 100, 10));
        assertThrows(IllegalArgumentException.class, () -> Scaling.factor(1000, 1000, -5, 100, 10));
    }

    @Test
    void factor_zeroOrNegativeBaseHeight_throws() {
        assertThrows(IllegalArgumentException.class, () -> Scaling.factor(1000, 1000, 100, 0, 10));
    }

    @Test
    void factor_typicalWindow_computesTheFlooredRatio() {
        assertEquals(4, Scaling.factor(1000, 1000, 200, 150, 20));
    }

    @Test
    void factor_narrowerDimensionWins_usesTheSmallerRatio() {
        assertEquals(2, Scaling.factor(300, 2000, 100, 100, 10));
    }

    @Test
    void factor_windowTooSmallForTheBoard_clampsToTheMinimum() {
        assertEquals(2, Scaling.factor(50, 50, 400, 300, 10));
    }

    @Test
    void factor_hugeWindow_clampsToTheMaximum() {
        assertEquals(6, Scaling.factor(100000, 100000, 100, 100, 10));
    }

    @Test
    void factor_negativeWindow_clampsToTheMinimum() {
        assertEquals(2, Scaling.factor(-500, -500, 100, 100, 10));
    }

    @Test
    void factor_zeroWindow_clampsToTheMinimum() {
        assertEquals(2, Scaling.factor(0, 0, 100, 100, 10));
    }

    @Test
    void factor_sweepOfWindowAndBoardSizes_alwaysReturnsAnIntegerInRange() {
        int[] windowSizes = {0, 1, 50, 100, 320, 421, 800, 1080, 1920, 4000, 100000};
        int[] boardSizes = {1, 15, 105, 200, 315, 630, 3000};

        for (int viewWidth : windowSizes) {
            for (int viewHeight : windowSizes) {
                for (int baseWidth : boardSizes) {
                    for (int baseHeight : boardSizes) {
                        int n = Scaling.factor(viewWidth, viewHeight, baseWidth, baseHeight, 9);
                        assertTrue(n >= Scaling.MIN_FACTOR && n <= Scaling.MAX_FACTOR,
                                "n=" + n + " out of range for view " + viewWidth + "x" + viewHeight
                                        + " base " + baseWidth + "x" + baseHeight);
                        assertTrue(n != 1, "scaling factor must never be 1");
                    }
                }
            }
        }
    }
}
