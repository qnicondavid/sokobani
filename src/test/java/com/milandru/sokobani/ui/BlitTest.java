package com.milandru.sokobani.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BlitTest {

    @Test
    void upscale_singlePixel_producesAnExactNByNBlockOfOneColour() {
        int[] source = {0x123456};
        int factor = 5;

        int[] scaled = Blit.upscale(source, 1, 1, factor);

        assertEquals(factor * factor, scaled.length);
        for (int pixel : scaled) {
            assertEquals(0x123456, pixel);
        }
    }

    @Test
    void upscale_twoAdjacentPixels_producesASharpBoundaryWithNoBlending() {
        int[] source = {0x000000, 0xFFFFFF};
        int factor = 4;
        int scaledWidth = 2 * factor;
        int scaledHeight = 1 * factor;

        int[] scaled = Blit.upscale(source, 2, 1, factor);

        assertEquals(scaledWidth * scaledHeight, scaled.length);
        for (int y = 0; y < factor; y++) {
            for (int x = 0; x < factor; x++) {
                assertEquals(0x000000, scaled[y * scaledWidth + x]);
            }
            for (int x = factor; x < scaledWidth; x++) {
                assertEquals(0xFFFFFF, scaled[y * scaledWidth + x]);
            }
        }
    }

    @Test
    void upscale_grid_eachSourcePixelBecomesItsOwnBlockAtTheRightOffset() {
        int[] source = {
                0x111111, 0x222222,
                0x333333, 0x444444
        };
        int factor = 3;

        int[] scaled = Blit.upscale(source, 2, 2, factor);
        int scaledWidth = 2 * factor;

        assertEquals(0x111111, scaled[0]);
        assertEquals(0x111111, scaled[(factor - 1) * scaledWidth + (factor - 1)]);
        assertEquals(0x222222, scaled[factor]);
        assertEquals(0x333333, scaled[factor * scaledWidth]);
        assertEquals(0x444444, scaled[factor * scaledWidth + factor]);
    }

    @Test
    void upscale_calledTwice_isDeterministic() {
        int[] source = {0x101010, 0x202020, 0x303030, 0x404040};

        int[] first = Blit.upscale(source, 2, 2, 6);
        int[] second = Blit.upscale(source, 2, 2, 6);

        assertEquals(first.length, second.length);
        for (int i = 0; i < first.length; i++) {
            assertEquals(first[i], second[i]);
        }
    }
}
