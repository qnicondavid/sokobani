package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.ui.GlyphRasterizer;
import com.milandru.sokobani.ui.Surface;

final class BlockGlyphRasterizer implements GlyphRasterizer {

    private static final double ADVANCE_RATIO = 0.6;
    private static final int COLUMN_WEIGHT = 3;
    private static final int ROW_WEIGHT = 7;
    private static final int HOLE_EVERY = 4;

    @Override
    public double advance(char glyph, int size) {
        return Math.round(size * ADVANCE_RATIO);
    }

    @Override
    public void paint(Surface surface, int x, int baseline, char glyph, int size, int tone) {
        if (glyph == ' ') {
            return;
        }
        int width = (int) advance(glyph, size);
        for (int row = 0; row < size; row++) {
            for (int column = 0; column < width; column++) {
                if (Math.floorMod(glyph + COLUMN_WEIGHT * column + ROW_WEIGHT * row, HOLE_EVERY) != 0) {
                    surface.blend(x + column, baseline - size + row, tone, 1.0);
                }
            }
        }
    }
}
