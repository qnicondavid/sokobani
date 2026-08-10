package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.persistence.Progress;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoomsGridTest {

    private static final Progress PROGRESS = Progress.empty()
            .withSolved(0, 3, 3)
            .withSolved(1, 5, 4)
            .withSolved(2, 7, 6)
            .withSolved(3, 9, 8)
            .withSolved(4, 11, 10)
            .withSolved(5, 13, 12);

    @Test
    void startsOnTheFurthestUnlockedRoom() {
        assertEquals(6, new RoomsGrid(PROGRESS, 15).selection());
        assertEquals(0, new RoomsGrid(Progress.empty(), 15).selection());
    }

    @Test
    void clampsToTheLastLevelWhenThePackIsShorterThanTheProgress() {
        assertEquals(4, new RoomsGrid(PROGRESS, 5).selection());
    }

    @Test
    void movingSkipsLockedRoomsAndWraps() {
        Progress progress = Progress.empty().withSolved(0, 3, 3).withSolved(4, 9, 8);
        RoomsGrid grid = new RoomsGrid(progress, 8);

        assertEquals(5, grid.selection());
        grid.moveRight();
        assertEquals(0, grid.selection());
        grid.moveLeft();
        assertEquals(5, grid.selection());
    }

    @Test
    void movingByRowsMovesFivePlacesAtMost() {
        RoomsGrid grid = new RoomsGrid(PROGRESS, 15);

        grid.moveUp();
        assertEquals(1, grid.selection());
        grid.moveDown();
        assertEquals(6, grid.selection());
    }

    @Test
    void confirm_answersTheSelectionOnlyWhenItIsUnlocked() {
        RoomsGrid grid = new RoomsGrid(PROGRESS, 15);

        assertTrue(grid.confirm().isPresent());
        assertEquals(6, grid.confirm().orElseThrow());
    }

    @Test
    void select_acceptsOnlyUnlockedRooms() {
        Progress progress = Progress.empty().withSolved(0, 3, 3).withSolved(4, 9, 8);
        RoomsGrid grid = new RoomsGrid(progress, 8);

        assertFalse(grid.select(6));
        assertEquals(5, grid.selection());
        assertTrue(grid.select(4));
        assertEquals(4, grid.selection());
        assertTrue(grid.confirm().isPresent());
        assertEquals(4, grid.confirm().orElseThrow());
    }

    @Test
    void pageCount_fitsThePackInPagesOfFifteen() {
        assertEquals(1, new RoomsGrid(Progress.empty(), 15).pageCount());
        assertEquals(1, new RoomsGrid(Progress.empty(), 1).pageCount());
        assertEquals(7, new RoomsGrid(Progress.empty(), 100).pageCount());
    }

    @Test
    void startsOnThePageOfTheFurthestUnlockedRoom() {
        Progress progress = solvedThrough(19);
        RoomsGrid grid = new RoomsGrid(progress, 100);

        assertEquals(20, grid.selection());
        assertEquals(1, grid.page());
    }

    @Test
    void flipPage_movesToTheNextPageAndBack_wrappingAround() {
        RoomsGrid grid = new RoomsGrid(Progress.empty(), 100);

        assertEquals(0, grid.page());
        grid.flipPage(1);
        assertEquals(1, grid.page());
        assertEquals(15, grid.selection());
        grid.flipPage(-1);
        assertEquals(0, grid.page());
        assertEquals(0, grid.selection());
        grid.flipPage(-1);
        assertEquals(6, grid.page());
        assertEquals(90, grid.selection());
    }

    @Test
    void flipPage_landsOnTheFirstUnlockedLevelOfThePage() {
        Progress progress = solvedThrough(9).withSolved(15, 4, 4);
        RoomsGrid grid = new RoomsGrid(progress, 100);

        grid.flipPage(-1);
        assertEquals(0, grid.page());
        assertEquals(0, grid.selection());
        grid.flipPage(1);
        assertEquals(1, grid.page());
        assertEquals(15, grid.selection());
    }

    @Test
    void flipPage_toAPageWithNothingUnlocked_keepsTheSelectionLockedAtItsStart() {
        RoomsGrid grid = new RoomsGrid(Progress.empty().withSolved(0, 3, 3), 30);

        grid.flipPage(1);
        assertEquals(1, grid.page());
        assertEquals(15, grid.selection());
        assertTrue(grid.confirm().isEmpty());
    }

    @Test
    void arrowMovement_staysInsideTheCurrentPage() {
        Progress progress = solvedThrough(29);
        RoomsGrid grid = new RoomsGrid(progress, 100);

        grid.select(0);
        grid.moveLeft();
        assertEquals(14, grid.selection());
        assertEquals(0, grid.page());
        grid.moveUp();
        assertEquals(9, grid.selection());
        grid.moveDown();
        assertEquals(14, grid.selection());
        grid.flipPage(1);
        grid.moveLeft();
        assertEquals(29, grid.selection());
    }

    @Test
    void select_movesTheSelectionOntoThatLevelsPage() {
        Progress progress = solvedThrough(39);
        RoomsGrid grid = new RoomsGrid(progress, 100);

        assertTrue(grid.select(30));
        assertEquals(30, grid.selection());
        assertEquals(2, grid.page());
    }

    private static Progress solvedThrough(int last) {
        Progress progress = Progress.empty();
        for (int index = 0; index <= last; index++) {
            progress = progress.withSolved(index, 3, 3);
        }
        return progress;
    }
}
