package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.level.LevelPack;
import com.milandru.sokobani.persistence.Progress;
import com.milandru.sokobani.ui.Surface;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoomsViewTest {

    private static final LevelPack PACK = BoardFixture.pack(BoardFixture.ONE_PUSH, BoardFixture.ONE_PUSH, BoardFixture.ONE_PUSH);
    private static final Progress PROGRESS = Progress.empty().withSolved(0, 3, 3);

    @Test
    void render_drawnTwice_producesIdenticalPixels() {
        assertArrayEquals(rooms().tones(), rooms().tones());
    }

    @Test
    void render_isTheSpecifiedSurfaceSize() {
        Surface surface = rooms();

        assertEquals(RoomsView.WIDTH, surface.width());
        assertEquals(RoomsView.HEIGHT, surface.height());
    }

    @Test
    void plateAt_findsThePlateUnderThePointerAndNothingBetween() {
        assertEquals(0, RoomsView.plateAt(RoomsView.plateX(0), RoomsView.plateY(0), 0));
        assertEquals(0, RoomsView.plateAt(RoomsView.plateX(0) + RoomsView.PLATE_W - 1, RoomsView.plateY(0), 0));
        assertEquals(2, RoomsView.plateAt(RoomsView.plateX(2), RoomsView.plateY(2), 0));
        assertEquals(-1, RoomsView.plateAt(RoomsView.plateX(0) + RoomsView.PLATE_W, RoomsView.plateY(0), 0));
        assertEquals(-1, RoomsView.plateAt(RoomsView.plateX(0), RoomsView.plateY(0) + RoomsView.PLATE_H, 0));
        assertEquals(-1, RoomsView.plateAt(RoomsView.plateX(0), RoomsView.plateY(0) + RoomsView.ROW_PITCH * RoomsView.ROWS, 0));
        assertEquals(-1, RoomsView.plateAt(0, 0, 0));
    }

    @Test
    void plateAt_answersNothingAnywhereAboveOrLeftOfTheGrid() {
        for (int y = 0; y < RoomsView.plateY(0); y++) {
            for (int x = 0; x < RoomsView.WIDTH; x++) {
                assertEquals(-1, RoomsView.plateAt(x, y, 0), "(" + x + ", " + y + ") is above the first row");
            }
        }
        for (int y = 0; y < RoomsView.HEIGHT; y++) {
            for (int x = 0; x < RoomsView.plateX(0); x++) {
                assertEquals(-1, RoomsView.plateAt(x, y, 0), "(" + x + ", " + y + ") is left of the first column");
            }
        }
    }

    @Test
    void plateAt_answersNothingInAnyGapBetweenPlates() {
        for (int y = 0; y < RoomsView.HEIGHT; y++) {
            for (int x = 0; x < RoomsView.WIDTH; x++) {
                int plate = RoomsView.plateAt(x, y, 0);
                if (plate < 0) {
                    continue;
                }
                int px = RoomsView.plateX(plate);
                int py = RoomsView.plateY(plate);
                assertTrue(x >= px && x < px + RoomsView.PLATE_W && y >= py && y < py + RoomsView.PLATE_H,
                        "(" + x + ", " + y + ") was answered as plate " + plate + " at " + px + ", " + py);
            }
        }
    }

    @Test
    void plateAt_offsetsWholePagesIntoThePack() {
        assertEquals(RoomsView.PAGE_SIZE, RoomsView.plateAt(RoomsView.plateX(0), RoomsView.plateY(0), 1));
        assertEquals(RoomsView.PAGE_SIZE + 2, RoomsView.plateAt(RoomsView.plateX(2), RoomsView.plateY(0), 1));
        assertEquals(-1, RoomsView.plateAt(RoomsView.plateX(0), RoomsView.plateY(0), -1));
    }

    @Test
    void render_drawsOnlyTheLevelsOnTheGivenPage() {
        LevelPack large = BoardFixture.pack(sixteenOnePush());
        Surface pageOne = new Surface(RoomsView.WIDTH, RoomsView.HEIGHT);
        RoomsView.render(pageOne, BoardFixture.typeSetter(), large, Progress.empty(), 15, 1);
        Surface pageTwo = new Surface(RoomsView.WIDTH, RoomsView.HEIGHT);
        RoomsView.render(pageTwo, BoardFixture.typeSetter(), large, Progress.empty(), 15, 0);

        int emptyPlateInk = inkIn(pageOne, RoomsView.plateX(1) + 2, RoomsView.plateY(1) + 2,
                RoomsView.PLATE_W - 4, RoomsView.PLATE_H - 4);
        int fullPlateInk = inkIn(pageTwo, RoomsView.plateX(1) + 2, RoomsView.plateY(1) + 2,
                RoomsView.PLATE_W - 4, RoomsView.PLATE_H - 4);
        assertTrue(emptyPlateInk < fullPlateInk, "an empty page plate carries less ink than a filled one");
    }

    private static String[] sixteenOnePush() {
        String[] layouts = new String[RoomsView.PAGE_SIZE + 1];
        for (int index = 0; index < layouts.length; index++) {
            layouts[index] = BoardFixture.ONE_PUSH;
        }
        return layouts;
    }

    @Test
    void platesSitFiveAcrossAndWrapToTheNextRow() {
        assertTrue(RoomsView.plateX(1) > RoomsView.plateX(0));
        assertEquals(RoomsView.plateY(1), RoomsView.plateY(0));
        assertEquals(RoomsView.plateY(RoomsView.COLUMNS), RoomsView.plateY(0) + RoomsView.ROW_PITCH);
    }

    @Test
    void aLockedPlateCarriesAPaperBoxWhileASolvedOneShowsOnlyTheClearedBox() {
        Surface surface = rooms();

        int solvedInk = inkIn(surface, RoomsView.plateX(0) + 4, RoomsView.plateY(0) + 3, 33, 19);
        int lockedInk = inkIn(surface, RoomsView.plateX(2) + 4, RoomsView.plateY(2) + 3, 33, 19);

        assertTrue(solvedInk < lockedInk);
    }

    @Test
    void selectedPlate_clearsItsInteriorAboveTheInkBar() {
        Surface surface = rooms();

        int px = RoomsView.plateX(1);
        int py = RoomsView.plateY(1);
        assertEquals(Surface.INK, surface.toneAt(px - 4, py - 4));
        assertEquals(Surface.PAPER, surface.toneAt(px + 2, py + 5));
    }

    @Test
    void aSolvedPlate_keepsItsBestLineInsideThePlate() {
        Progress solved = Progress.empty().withSolved(0, 3, 3).withSolved(1, 3, 3);
        Surface surface = new Surface(RoomsView.WIDTH, RoomsView.HEIGHT);
        RoomsView.render(surface, BoardFixture.typeSetter(), PACK, solved, 0, 0);

        int px = RoomsView.plateX(0);
        int py = RoomsView.plateY(0);
        assertEquals(0, inkIn(surface, px + 1, py + 32, 3, 10));
        assertEquals(0, inkIn(surface, px + 37, py + 32, 3, 10));
    }

    @Test
    void anUnselectedSolvedPlate_clearsTheGrainBehindItsRuleAndBestLine() {
        Progress solved = Progress.empty().withSolved(0, 3, 3).withSolved(1, 3, 3).withSolved(2, 3, 3);
        Surface surface = new Surface(RoomsView.WIDTH, RoomsView.HEIGHT);
        RoomsView.render(surface, BoardFixture.typeSetter(), PACK, solved, 0, 0);

        int px = RoomsView.plateX(2);
        int py = RoomsView.plateY(2);
        assertEquals(0, inkIn(surface, px + 1, py + 31, RoomsView.PLATE_W - 2, 1),
                "the row above the rule carries no diagonal grain");
        assertEquals(0, inkIn(surface, px + 1, py + 43, RoomsView.PLATE_W - 2, 1),
                "the row below the best line carries no diagonal grain");
        assertTrue(inkIn(surface, px + 1, py + 44, RoomsView.PLATE_W - 2, 1) > 0,
                "the grain still runs below the cleared band");
    }

    @Test
    void aLockedPlatesClearedBoxIsWideEnoughForItsRoomNumber() {
        LevelPack hundred = BoardFixture.pack(onePushRepeated(100));
        Surface surface = new Surface(RoomsView.WIDTH, RoomsView.HEIGHT);
        RoomsView.render(surface, BoardFixture.typeSetter(), hundred, Progress.empty(), 0, 6);

        assertTrue(clearedBoxWidth(surface, 8) >= numberWidth("99"),
                "the two-digit room 99 fits inside its cleared box");
        assertTrue(clearedBoxWidth(surface, 9) >= numberWidth("100"),
                "the three-digit room 100 fits inside its cleared box");
    }

    private static int numberWidth(String number) {
        return (int) Math.ceil(Type.width(BoardFixture.typeSetter(), number, new Type.Style(15, 0.0)));
    }

    private static int clearedBoxWidth(Surface surface, int plate) {
        int px = RoomsView.plateX(plate);
        int py = RoomsView.plateY(plate);
        int longest = 0;
        int run = 0;
        for (int x = px; x < px + RoomsView.PLATE_W; x++) {
            run = surface.toneAt(x, py + 30) == Surface.PAPER ? run + 1 : 0;
            longest = Math.max(longest, run);
        }
        return longest;
    }

    private static String[] onePushRepeated(int count) {
        String[] layouts = new String[count];
        for (int index = 0; index < layouts.length; index++) {
            layouts[index] = BoardFixture.ONE_PUSH;
        }
        return layouts;
    }

    private static int inkIn(Surface surface, int x, int y, int width, int height) {
        int count = 0;
        for (int py = y; py < y + height; py++) {
            for (int px = x; px < x + width; px++) {
                if (surface.toneAt(px, py) == Surface.INK) {
                    count++;
                }
            }
        }
        return count;
    }

    private static Surface rooms() {
        Surface surface = new Surface(RoomsView.WIDTH, RoomsView.HEIGHT);
        RoomsView.render(surface, BoardFixture.typeSetter(), PACK, PROGRESS, 1, 0);
        return surface;
    }
}
