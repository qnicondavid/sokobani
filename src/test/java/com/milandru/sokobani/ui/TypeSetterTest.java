package com.milandru.sokobani.ui;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TypeSetterTest {

    @Test
    void draw_sizeBelowTheFloor_throws() {
        TypeSetter typeSetter = new TypeSetter(new StubGlyphRasterizer(6));
        Surface surface = new Surface(50, 20);

        assertThrows(IllegalArgumentException.class,
                () -> typeSetter.draw(surface, "HI", 0, 10, 9, 0, Surface.INK));
    }

    @Test
    void measure_sizeBelowTheFloor_throws() {
        TypeSetter typeSetter = new TypeSetter(new StubGlyphRasterizer(6));

        assertThrows(IllegalArgumentException.class, () -> typeSetter.measure("HI", 9, 0));
    }

    @Test
    void draw_atExactlyTheFloor_doesNotThrow() {
        TypeSetter typeSetter = new TypeSetter(new StubGlyphRasterizer(6));
        Surface surface = new Surface(50, 20);

        typeSetter.draw(surface, "HI", 0, 10, TypeSetter.MIN_SIZE, 0, Surface.INK);
    }

    @Test
    void draw_eachGlyph_advancesByWidthPlusTracking() {
        StubGlyphRasterizer rasterizer = new StubGlyphRasterizer(6);
        TypeSetter typeSetter = new TypeSetter(rasterizer);
        Surface surface = new Surface(50, 20);

        typeSetter.draw(surface, "ABC", 10, 12, 10, 2.0, Surface.INK);

        List<StubGlyphRasterizer.PaintedGlyph> painted = rasterizer.painted();
        assertEquals(3, painted.size());
        assertEquals(10, painted.get(0).x());
        assertEquals(18, painted.get(1).x());
        assertEquals(26, painted.get(2).x());
        for (StubGlyphRasterizer.PaintedGlyph glyph : painted) {
            assertEquals(12, glyph.baseline());
        }
    }

    @Test
    void measure_matchesTheFinalCursorAdvanceOfDraw() {
        StubGlyphRasterizer rasterizer = new StubGlyphRasterizer(6);
        TypeSetter typeSetter = new TypeSetter(rasterizer);
        Surface surface = new Surface(50, 20);
        int startX = 10;

        double measured = typeSetter.measure("ABC", 10, 2.0);
        typeSetter.draw(surface, "ABC", startX, 12, 10, 2.0, Surface.INK);

        StubGlyphRasterizer.PaintedGlyph last = rasterizer.painted().get(rasterizer.painted().size() - 1);
        double finalCursor = last.x() + rasterizer.advance(last.glyph(), 10) + 2.0;

        assertEquals(24.0, measured, 0.0001);
        assertEquals(startX + measured, finalCursor, 0.0001);
    }

    @Test
    void measure_emptyString_isZero() {
        TypeSetter typeSetter = new TypeSetter(new StubGlyphRasterizer(6));

        assertEquals(0.0, typeSetter.measure("", 10, 2.0), 0.0001);
    }
}
