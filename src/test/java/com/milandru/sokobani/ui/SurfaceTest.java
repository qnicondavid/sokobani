package com.milandru.sokobani.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SurfaceTest {

    @Test
    void constructor_freshSurface_isEntirelyPaper() {
        Surface surface = new Surface(5, 5);

        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 5; x++) {
                assertEquals(Surface.PAPER, surface.toneAt(x, y));
            }
        }
    }

    @Test
    void fill_paintsExactRegion_leavesOutsideUntouched() {
        Surface surface = new Surface(10, 10);

        surface.fill(2, 3, 4, 2, Surface.INK);

        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                boolean inside = x >= 2 && x < 6 && y >= 3 && y < 5;
                assertEquals(inside ? Surface.INK : Surface.PAPER, surface.toneAt(x, y));
            }
        }
    }

    @Test
    void fill_toneOutsideZeroTo255_throws() {
        Surface surface = new Surface(10, 10);

        assertThrows(IllegalArgumentException.class, () -> surface.fill(0, 0, 5, 5, -1));
        assertThrows(IllegalArgumentException.class, () -> surface.fill(0, 0, 5, 5, 256));
    }

    @Test
    void fill_outOfRangeTone_leavesTheSurfaceUntouched() {
        Surface surface = new Surface(10, 10);

        assertThrows(IllegalArgumentException.class, () -> surface.fill(0, 0, 10, 10, 300));

        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                assertEquals(Surface.PAPER, surface.toneAt(x, y));
            }
        }
    }

    @Test
    void ring_toneOutsideZeroTo255_throws() {
        Surface surface = new Surface(20, 20);

        assertThrows(IllegalArgumentException.class, () -> surface.ring(10, 10, 5, 300));
    }

    @Test
    void everyToneTakingPrimitive_outOfRangeTone_throws() {
        Surface surface = new Surface(20, 20);

        assertThrows(IllegalArgumentException.class, () -> surface.fill(0, 0, 5, 5, 300));
        assertThrows(IllegalArgumentException.class, () -> surface.box(0, 0, 5, 5, 1, 300));
        assertThrows(IllegalArgumentException.class, () -> surface.hatchVertical(0, 0, 5, 5, 2, 300));
        assertThrows(IllegalArgumentException.class, () -> surface.hatchHorizontal(0, 0, 5, 5, 2, 300));
        assertThrows(IllegalArgumentException.class, () -> surface.hatchDiagonal(0, 0, 5, 5, 2, 300));
        assertThrows(IllegalArgumentException.class, () -> surface.stipple(0, 0, 5, 5, 1.0, 300));
        assertThrows(IllegalArgumentException.class, () -> surface.ring(10, 10, 5, 300));
        assertThrows(IllegalArgumentException.class, () -> surface.blend(2, 2, 300, 1.0));
    }

    @Test
    void everyToneTakingPrimitive_negativeTone_throws() {
        Surface surface = new Surface(20, 20);

        assertThrows(IllegalArgumentException.class, () -> surface.fill(0, 0, 5, 5, -1));
        assertThrows(IllegalArgumentException.class, () -> surface.box(0, 0, 5, 5, 1, -1));
        assertThrows(IllegalArgumentException.class, () -> surface.hatchVertical(0, 0, 5, 5, 2, -1));
        assertThrows(IllegalArgumentException.class, () -> surface.hatchHorizontal(0, 0, 5, 5, 2, -1));
        assertThrows(IllegalArgumentException.class, () -> surface.hatchDiagonal(0, 0, 5, 5, 2, -1));
        assertThrows(IllegalArgumentException.class, () -> surface.stipple(0, 0, 5, 5, 1.0, -1));
        assertThrows(IllegalArgumentException.class, () -> surface.ring(10, 10, 5, -1));
        assertThrows(IllegalArgumentException.class, () -> surface.blend(2, 2, -1, 1.0));
    }

    @Test
    void invert_afterEveryPrimitive_isItsOwnInverse() {
        Surface surface = SurfacePatterns.draw();
        int[] before = surface.tones();

        surface.invert(0, 0, surface.width(), surface.height());
        surface.invert(0, 0, surface.width(), surface.height());

        assertArrayEquals(before, surface.tones());
    }

    @Test
    void box_paintsExactlyTheBorderRegion() {
        Surface surface = new Surface(10, 10);
        int x = 1;
        int y = 1;
        int w = 6;
        int h = 6;
        int thickness = 1;

        surface.box(x, y, w, h, thickness, Surface.INK);

        for (int py = 0; py < 10; py++) {
            for (int px = 0; px < 10; px++) {
                boolean inRegion = px >= x && px < x + w && py >= y && py < y + h;
                boolean onEdge = px < x + thickness || px >= x + w - thickness
                        || py < y + thickness || py >= y + h - thickness;
                boolean onBorder = inRegion && onEdge;
                assertEquals(onBorder ? Surface.INK : Surface.PAPER, surface.toneAt(px, py));
            }
        }
    }

    @Test
    void hatchVertical_paintsColumnsAtSpacing_leavesGapsPaper() {
        Surface surface = new Surface(10, 5);
        int x = 0;
        int w = 6;
        int spacing = 3;

        surface.hatchVertical(x, 0, w, 5, spacing, Surface.INK);

        for (int py = 0; py < 5; py++) {
            for (int px = 0; px < 10; px++) {
                boolean onHatch = px >= x && px < x + w && (px - x) % spacing == 0;
                assertEquals(onHatch ? Surface.INK : Surface.PAPER, surface.toneAt(px, py));
            }
        }
    }

    @Test
    void hatchHorizontal_paintsRowsAtSpacing_leavesGapsPaper() {
        Surface surface = new Surface(5, 10);
        int y = 0;
        int h = 6;
        int spacing = 3;

        surface.hatchHorizontal(0, y, 5, h, spacing, Surface.INK);

        for (int py = 0; py < 10; py++) {
            for (int px = 0; px < 5; px++) {
                boolean onHatch = py >= y && py < y + h && (py - y) % spacing == 0;
                assertEquals(onHatch ? Surface.INK : Surface.PAPER, surface.toneAt(px, py));
            }
        }
    }

    @Test
    void hatchVertical_zeroOrNegativeSpacing_throws() {
        Surface surface = new Surface(10, 10);

        assertThrows(IllegalArgumentException.class, () -> surface.hatchVertical(0, 0, 5, 5, 0, Surface.INK));
        assertThrows(IllegalArgumentException.class, () -> surface.hatchVertical(0, 0, 5, 5, -1, Surface.INK));
    }

    @Test
    void hatchHorizontal_zeroOrNegativeSpacing_throws() {
        Surface surface = new Surface(10, 10);

        assertThrows(IllegalArgumentException.class, () -> surface.hatchHorizontal(0, 0, 5, 5, 0, Surface.INK));
        assertThrows(IllegalArgumentException.class, () -> surface.hatchHorizontal(0, 0, 5, 5, -1, Surface.INK));
    }

    @Test
    void hatchDiagonal_zeroOrNegativeSpacing_throws() {
        Surface surface = new Surface(10, 10);

        assertThrows(IllegalArgumentException.class, () -> surface.hatchDiagonal(0, 0, 5, 5, 0, Surface.INK));
        assertThrows(IllegalArgumentException.class, () -> surface.hatchDiagonal(0, 0, 5, 5, -1, Surface.INK));
    }

    @Test
    void hatchDiagonal_paintsAlongTheDiagonal() {
        Surface surface = new Surface(10, 10);

        surface.hatchDiagonal(0, 0, 10, 10, 4, Surface.INK);

        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                boolean onDiagonal = Math.floorMod(x - y, 4) == 0;
                assertEquals(onDiagonal ? Surface.INK : Surface.PAPER, surface.toneAt(x, y));
            }
        }
    }

    @Test
    void ring_paintsSymmetricPointsAroundTheCentre() {
        Surface surface = new Surface(20, 20);

        surface.ring(10, 10, 5, Surface.INK);

        assertEquals(Surface.INK, surface.toneAt(15, 10));
        assertEquals(Surface.INK, surface.toneAt(5, 10));
        assertEquals(Surface.INK, surface.toneAt(10, 15));
        assertEquals(Surface.INK, surface.toneAt(10, 5));
        assertEquals(Surface.PAPER, surface.toneAt(10, 10));
    }

    @Test
    void ring_everyPaintedPixelIsNearTheRadius_everyFarPixelStaysPaper() {
        Surface surface = new Surface(20, 20);
        int cx = 10;
        int cy = 10;
        int r = 5;

        surface.ring(cx, cy, r, Surface.INK);

        for (int py = 0; py < 20; py++) {
            for (int px = 0; px < 20; px++) {
                double distance = Math.hypot(px - cx, py - cy);
                int tone = surface.toneAt(px, py);
                if (tone == Surface.INK) {
                    assertTrue(Math.abs(distance - r) <= 1.0,
                            "inked pixel (" + px + "," + py + ") is not near radius " + r);
                }
                if (Math.abs(distance - r) > 2.0) {
                    assertEquals(Surface.PAPER, tone,
                            "pixel (" + px + "," + py + ") is far from the radius but was not left paper");
                }
            }
        }
    }

    @Test
    void invert_flipsTonesInsideTheRegion_leavesOutsideUntouched() {
        Surface surface = new Surface(10, 10);
        surface.fill(0, 0, 10, 10, Surface.INK);

        surface.invert(2, 2, 4, 4);

        assertEquals(Surface.PAPER, surface.toneAt(3, 3));
        assertEquals(Surface.INK, surface.toneAt(0, 0));
    }

    @Test
    void invert_appliedTwice_restoresTheOriginalTones() {
        Surface surface = SurfacePatterns.draw();
        int[] before = surface.tones();

        surface.invert(0, 0, SurfacePatterns.WIDTH, SurfacePatterns.HEIGHT);
        surface.invert(0, 0, SurfacePatterns.WIDTH, SurfacePatterns.HEIGHT);

        assertArrayEquals(before, surface.tones());
    }

    @Test
    void blend_fullCoverage_replacesTheToneOutright() {
        Surface surface = new Surface(5, 5);
        surface.fill(0, 0, 5, 5, 100);

        surface.blend(2, 2, 200, 1.0);
        surface.blend(3, 3, 50, 1.0);

        assertEquals(200, surface.toneAt(2, 2));
        assertEquals(50, surface.toneAt(3, 3));
    }

    @Test
    void blend_toneOutsideZeroTo255_throws() {
        Surface surface = new Surface(5, 5);

        assertThrows(IllegalArgumentException.class, () -> surface.blend(2, 2, -1, 1.0));
        assertThrows(IllegalArgumentException.class, () -> surface.blend(2, 2, 256, 1.0));
    }

    @Test
    void blend_coverageOutsideZeroToOne_throws() {
        Surface surface = new Surface(5, 5);

        assertThrows(IllegalArgumentException.class, () -> surface.blend(2, 2, Surface.INK, -0.1));
        assertThrows(IllegalArgumentException.class, () -> surface.blend(2, 2, Surface.INK, 1.5));
    }

    @Test
    void blend_coverageThatIsNotANumber_throws() {
        Surface surface = new Surface(5, 5);

        assertThrows(IllegalArgumentException.class, () -> surface.blend(2, 2, Surface.INK, Double.NaN));

        assertEquals(Surface.PAPER, surface.toneAt(2, 2));
    }

    @Test
    void blend_zeroCoverage_leavesTheExistingToneUntouched() {
        Surface surface = new Surface(5, 5);
        surface.fill(0, 0, 5, 5, 100);

        surface.blend(2, 2, 200, 0.0);

        assertEquals(100, surface.toneAt(2, 2));
    }

    @Test
    void blend_partialCoverage_movesProportionallyTowardTheTargetTone() {
        Surface surface = new Surface(5, 5);
        surface.fill(0, 0, 5, 5, 100);

        surface.blend(2, 2, 0, 0.5);

        assertEquals(50, surface.toneAt(2, 2));
    }

    @Test
    void blend_canLightenAnExistingDarkerTone() {
        Surface surface = new Surface(5, 5);
        surface.fill(0, 0, 5, 5, Surface.INK);

        surface.blend(2, 2, Surface.PAPER, 1.0);

        assertEquals(Surface.PAPER, surface.toneAt(2, 2));
    }

    @Test
    void toneAt_outsideTheSurface_throws() {
        Surface surface = new Surface(5, 5);

        assertThrows(IndexOutOfBoundsException.class, () -> surface.toneAt(-1, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> surface.toneAt(5, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> surface.toneAt(0, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> surface.toneAt(0, 5));
    }

    @Test
    void stipple_lowDensity_paintsFewerPixelsThanHighDensity() {
        Surface sparse = new Surface(50, 50);
        Surface dense = new Surface(50, 50);

        sparse.stipple(0, 0, 50, 50, 0.02, Surface.INK);
        dense.stipple(0, 0, 50, 50, 0.5, Surface.INK);

        assertTrue(countInk(sparse) < countInk(dense));
    }

    @Test
    void drawingPastTheEdges_doesNotThrowAndClipsToTheSurface() {
        Surface surface = new Surface(5, 5);

        surface.fill(-3, -3, 6, 6, Surface.INK);
        surface.box(2, 2, 10, 10, 1, Surface.INK);
        surface.ring(0, 0, 20, Surface.INK);

        assertEquals(Surface.INK, surface.toneAt(0, 0));
    }

    @Test
    void tones_returnsACopy_mutatingItDoesNotAffectTheSurface() {
        Surface surface = new Surface(3, 3);

        int[] copy = surface.tones();
        copy[0] = Surface.INK;

        assertEquals(Surface.PAPER, surface.toneAt(0, 0));
    }

    @Test
    void everyPrimitive_calledTwiceOnFreshSurfaces_producesIdenticalPixels() {
        Surface first = SurfacePatterns.draw();
        Surface second = SurfacePatterns.draw();

        assertArrayEquals(first.tones(), second.tones());
    }

    private static int countInk(Surface surface) {
        int count = 0;
        for (int tone : surface.tones()) {
            if (tone == Surface.INK) {
                count++;
            }
        }
        return count;
    }
}
