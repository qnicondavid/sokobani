package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.ui.Surface;
import com.milandru.sokobani.ui.Threshold;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TilesTest {

    private static final int CENTRE = Tiles.TILE / 2;
    private static final double MINIMUM_INK_DELTA = 0.40;

    private static final List<BoardFixture.TileArt> EVERY_TILE = List.of(
            (surface, x, y) -> Tiles.wall(surface, x, y, Tiles.Grain.VERTICAL, false, false),
            (surface, x, y) -> Tiles.wall(surface, x, y, Tiles.Grain.HORIZONTAL, false, false),
            (surface, x, y) -> Tiles.wall(surface, x, y, Tiles.Grain.DIAGONAL, false, false),
            (surface, x, y) -> Tiles.wall(surface, x, y, Tiles.Grain.HORIZONTAL, true, false),
            (surface, x, y) -> Tiles.wall(surface, x, y, Tiles.Grain.HORIZONTAL, false, true),
            Tiles::floor,
            Tiles::goal,
            Tiles::boxOffGoal,
            Tiles::boxOnGoal,
            Tiles::player);

    @Test
    void boxOnGoal_versusBoxOffGoal_differByMoreThan40PercentagePointsOfInk() {
        Surface onGoal = tile();
        Surface offGoal = tile();

        Tiles.boxOnGoal(onGoal, 0, 0);
        Tiles.boxOffGoal(offGoal, 0, 0);

        double covered = crateInkCoverage(onGoal);
        double open = crateInkCoverage(offGoal);

        assertTrue(covered - open > MINIMUM_INK_DELTA,
                "box on goal covers " + covered + " of its footprint, box off goal " + open);
    }

    @Test
    void boxOnGoal_isMostlyInk_andBoxOffGoal_isMostlyPaper() {
        Surface onGoal = tile();
        Surface offGoal = tile();

        Tiles.boxOnGoal(onGoal, 0, 0);
        Tiles.boxOffGoal(offGoal, 0, 0);

        assertTrue(crateInkCoverage(onGoal) > 0.5);
        assertTrue(crateInkCoverage(offGoal) < 0.5);
    }

    @Test
    void everyTileType_drawnTwice_producesIdenticalPixels() {
        for (BoardFixture.TileArt tileType : EVERY_TILE) {
            Surface first = tile();
            Surface second = tile();

            tileType.draw(first, 0, 0);
            tileType.draw(second, 0, 0);

            assertArrayEquals(first.tones(), second.tones());
        }
    }

    @Test
    void everyTileType_paintsSomething() {
        for (BoardFixture.TileArt tileType : EVERY_TILE) {
            Surface surface = tile();

            tileType.draw(surface, 0, 0);

            assertTrue(inkIn(surface, 0, 0, Tiles.TILE, Tiles.TILE) > 0);
        }
    }

    @Test
    void wall_hasAHairlineBorderOnEveryEdge() {
        Surface surface = tile();

        Tiles.wall(surface, 0, 0, Tiles.Grain.HORIZONTAL, false, false);

        for (int along = 0; along < Tiles.TILE; along++) {
            assertEquals(Surface.INK, surface.toneAt(along, 0));
            assertEquals(Surface.INK, surface.toneAt(along, Tiles.TILE - 1));
            assertEquals(Surface.INK, surface.toneAt(0, along));
            assertEquals(Surface.INK, surface.toneAt(Tiles.TILE - 1, along));
        }
    }

    @Test
    void wall_onAnOuterColumn_isDenserOnThatEdgeThanAnInnerWallIs() {
        Surface banded = tile();
        Surface plain = tile();

        Tiles.wall(banded, 0, 0, Tiles.Grain.HORIZONTAL, true, false);
        Tiles.wall(plain, 0, 0, Tiles.Grain.HORIZONTAL, false, false);

        assertTrue(inkIn(banded, 0, 0, 5, Tiles.TILE) > inkIn(plain, 0, 0, 5, Tiles.TILE));
    }

    @Test
    void wall_diagonalGrain_differsFromBothOrthogonalGrains() {
        Surface diagonal = tile();
        Surface vertical = tile();
        Surface horizontal = tile();

        Tiles.wall(diagonal, 0, 0, Tiles.Grain.DIAGONAL, false, false);
        Tiles.wall(vertical, 0, 0, Tiles.Grain.VERTICAL, false, false);
        Tiles.wall(horizontal, 0, 0, Tiles.Grain.HORIZONTAL, false, false);

        assertTrue(differ(diagonal, vertical));
        assertTrue(differ(diagonal, horizontal));
        assertTrue(differ(vertical, horizontal));
    }

    @Test
    void goal_inksBothRingRadiiAndLeavesTheCentreClear() {
        Surface surface = tile();

        Tiles.goal(surface, 0, 0);

        assertEquals(Surface.INK, surface.toneAt(CENTRE + 5, CENTRE));
        assertEquals(Surface.INK, surface.toneAt(CENTRE + 3, CENTRE));
        assertEquals(Surface.PAPER, surface.toneAt(CENTRE, CENTRE));
    }

    @Test
    void boxOnGoal_keepsTheGoalRingOutsideTheCrate() {
        Surface surface = tile();

        Tiles.boxOnGoal(surface, 0, 0);

        assertEquals(Surface.INK, surface.toneAt(CENTRE + 6, CENTRE));
        assertEquals(Surface.INK, surface.toneAt(CENTRE - 6, CENTRE));
        assertEquals(Surface.INK, surface.toneAt(CENTRE, CENTRE + 6));
        assertEquals(Surface.INK, surface.toneAt(CENTRE, CENTRE - 6));
    }

    @Test
    void boxOnGoal_carriesAPaperLineThroughTheSolidFill() {
        Surface surface = tile();

        Tiles.boxOnGoal(surface, 0, 0);

        assertEquals(Surface.PAPER, surface.toneAt(CENTRE, CENTRE));
        assertEquals(Surface.INK, surface.toneAt(CENTRE, CENTRE - 1));
        assertEquals(Surface.INK, surface.toneAt(CENTRE, CENTRE + 1));
    }

    @Test
    void boxOffGoal_outlinesTheCrateAndLeavesTheMiddleOpen() {
        Surface surface = tile();

        Tiles.boxOffGoal(surface, 0, 0);

        assertEquals(Surface.INK, surface.toneAt(Tiles.BOX_INSET, Tiles.BOX_INSET));
        assertEquals(Surface.INK, surface.toneAt(Tiles.BOX_INSET, CENTRE));
        assertEquals(Surface.PAPER, surface.toneAt(CENTRE - 2, CENTRE));
    }

    @Test
    void player_standsOnGroundHatchingBelowTheFigure() {
        Surface surface = tile();

        Tiles.player(surface, 0, 0);

        assertTrue(inkIn(surface, 0, 0, Tiles.TILE, CENTRE) > 0);
        assertTrue(inkIn(surface, 0, Tiles.TILE - 3, Tiles.TILE, 3) > 0);
    }

    @Test
    void everyTileType_staysInsideItsOwnFifteenPixelSquare() {
        for (BoardFixture.TileArt tileType : EVERY_TILE) {
            Surface surface = new Surface(Tiles.TILE * 3, Tiles.TILE * 3);

            tileType.draw(surface, Tiles.TILE, Tiles.TILE);

            assertEquals(0, inkIn(surface, 0, 0, Tiles.TILE * 3, Tiles.TILE));
            assertEquals(0, inkIn(surface, 0, Tiles.TILE * 2, Tiles.TILE * 3, Tiles.TILE));
            assertEquals(0, inkIn(surface, 0, Tiles.TILE, Tiles.TILE, Tiles.TILE));
            assertEquals(0, inkIn(surface, Tiles.TILE * 2, Tiles.TILE, Tiles.TILE, Tiles.TILE));
        }
    }

    private static Surface tile() {
        return new Surface(Tiles.TILE, Tiles.TILE);
    }

    private static boolean differ(Surface one, Surface other) {
        int[] first = one.tones();
        int[] second = other.tones();
        for (int i = 0; i < first.length; i++) {
            if (first[i] != second[i]) {
                return true;
            }
        }
        return false;
    }

    private static double crateInkCoverage(Surface surface) {
        int ink = inkIn(surface, Tiles.BOX_INSET, Tiles.BOX_INSET, Tiles.BOX_SPAN, Tiles.BOX_SPAN);
        return ink / (double) (Tiles.BOX_SPAN * Tiles.BOX_SPAN);
    }

    private static int inkIn(Surface surface, int x, int y, int width, int height) {
        int ink = 0;
        for (int row = y; row < y + height; row++) {
            for (int column = x; column < x + width; column++) {
                if (Threshold.isInk(surface.toneAt(column, row))) {
                    ink++;
                }
            }
        }
        return ink;
    }
}
