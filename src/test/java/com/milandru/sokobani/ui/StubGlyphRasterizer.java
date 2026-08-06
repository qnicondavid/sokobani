package com.milandru.sokobani.ui;

import java.util.ArrayList;
import java.util.List;

final class StubGlyphRasterizer implements GlyphRasterizer {

    record PaintedGlyph(int x, int baseline, char glyph, int size, int tone) {
    }

    private final double glyphAdvance;
    private final List<PaintedGlyph> painted = new ArrayList<>();

    StubGlyphRasterizer(double glyphAdvance) {
        this.glyphAdvance = glyphAdvance;
    }

    List<PaintedGlyph> painted() {
        return painted;
    }

    @Override
    public double advance(char glyph, int size) {
        return glyphAdvance;
    }

    @Override
    public void paint(Surface surface, int x, int baseline, char glyph, int size, int tone) {
        painted.add(new PaintedGlyph(x, baseline, glyph, size, tone));
    }
}
