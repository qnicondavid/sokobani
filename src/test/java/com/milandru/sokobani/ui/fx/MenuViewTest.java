package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.persistence.Progress;
import com.milandru.sokobani.ui.Surface;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MenuViewTest {

    private static final int SELECTION_LEFT = 48;

    private static final Progress PROGRESS = Progress.empty()
            .withSolved(0, 3, 3)
            .withSolved(1, 5, 4)
            .withSolved(2, 7, 6)
            .withSolved(3, 9, 8)
            .withSolved(4, 11, 10)
            .withSolved(5, 13, 12)
            .withSolved(6, 15, 14);

    @Test
    void render_drawnTwice_producesIdenticalPixels() {
        assertArrayEquals(menu(0).tones(), menu(0).tones());
    }

    @Test
    void render_isTheSpecifiedSurfaceSize() {
        Surface surface = menu(0);

        assertEquals(MenuView.WIDTH, surface.width());
        assertEquals(MenuView.HEIGHT, surface.height());
    }

    @Test
    void theSelectionBarMovesWithTheSelection() {
        assertFalse(Arrays.equals(menu(0).tones(), menu(1).tones()));
    }

    @Test
    void solvedCount_countsTheSolvedRooms() {
        assertEquals(7, MenuView.solvedCount(PROGRESS, 15));
        assertEquals(0, MenuView.solvedCount(Progress.empty(), 15));
    }

    @Test
    void nothingButTheLabelIsDrawnInsideAnItemsSelectionBand() {
        for (int item = 0; item < MenuView.Item.values().length; item++) {
            Surface unselected = menu((item + 1) % MenuView.Item.values().length);
            Surface selected = menu(item);

            assertEquals(tonesInBand(unselected, item, Surface.INK), tonesInBand(selected, item, Surface.PAPER),
                    "item " + item + " reads as the same glyph knocked out as it does drawn in ink");
        }
    }

    @Test
    void theRuleAndTheFooterAreDrawnBelowEveryItem() {
        Surface surface = menu(0);
        int lastRowOfTheLastBand = 0;
        for (int y = 0; y < MenuView.HEIGHT; y++) {
            if (MenuView.overItem(SELECTION_LEFT, y, MenuView.Item.values().length - 1)) {
                lastRowOfTheLastBand = y;
            }
        }

        int belowTheItems = 0;
        for (int y = lastRowOfTheLastBand + 1; y < MenuView.HEIGHT; y++) {
            for (int x = SELECTION_LEFT; x < MenuView.WIDTH - SELECTION_LEFT; x++) {
                belowTheItems += surface.toneAt(x, y) == Surface.INK ? 1 : 0;
            }
        }
        assertTrue(belowTheItems > 0, "the rule and the footer are drawn below the last item");
    }

    private static int tonesInBand(Surface surface, int item, int tone) {
        int count = 0;
        for (int y = 0; y < MenuView.HEIGHT; y++) {
            if (!MenuView.overItem(SELECTION_LEFT, y, item)) {
                continue;
            }
            for (int x = SELECTION_LEFT; x < MenuView.WIDTH - SELECTION_LEFT; x++) {
                count += surface.toneAt(x, y) == tone ? 1 : 0;
            }
        }
        return count;
    }

    @Test
    void overItem_answersTheSelectionBandOfEachItem() {
        assertTrue(MenuView.overItem(50, 130, 0));
        assertTrue(MenuView.overItem(200, 130, 0));
        assertFalse(MenuView.overItem(20, 130, 0));
        assertFalse(MenuView.overItem(50, 110, 0));
        assertTrue(MenuView.overItem(50, 130 + 36, 2));
        assertFalse(MenuView.overItem(50, 130, 1));
    }

    private static Surface menu(int selection) {
        Surface surface = new Surface(MenuView.WIDTH, MenuView.HEIGHT);
        MenuView.render(surface, BoardFixture.typeSetter(),
                BoardFixture.state(BoardFixture.ONE_PUSH), PROGRESS, 15, selection);
        return surface;
    }
}
