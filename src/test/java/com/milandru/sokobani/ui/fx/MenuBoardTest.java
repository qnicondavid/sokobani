package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.persistence.Progress;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MenuBoardTest {

    private static final List<com.milandru.sokobani.core.Level> THREE =
            BoardFixture.pack(BoardFixture.ONE_PUSH, BoardFixture.ONE_PUSH, BoardFixture.ONE_PUSH).levels();

    @Test
    void startsOnTheFirstSolvedRoomOrRoomZero() {
        Progress solved = Progress.empty().withSolved(2, 3, 3);
        assertEquals(2, new MenuBoard(solved, THREE).room());
        assertEquals(0, new MenuBoard(Progress.empty(), THREE).room());
    }

    @Test
    void advance_doesNothingBeforeASolutionArrives() {
        MenuBoard board = new MenuBoard(Progress.empty().withSolved(0, 3, 3), THREE);

        assertFalse(board.advance());
        assertEquals(0, board.step());
    }

    @Test
    void advance_appliesOneMovePerCallUntilTheSequenceIsSpent() {
        MenuBoard board = new MenuBoard(Progress.empty().withSolved(0, 3, 3), THREE);
        board.attach(0, "RR");

        assertTrue(board.advance());
        assertEquals(1, board.step());
        assertTrue(board.advance());
        assertEquals(2, board.step());
    }

    @Test
    void advance_movesToTheNextSolvedRoomWhenTheSequenceEnds() {
        Progress solved = Progress.empty().withSolved(0, 3, 3).withSolved(2, 5, 4);
        MenuBoard board = new MenuBoard(solved, THREE);
        board.attach(0, "RR");

        board.advance();
        board.advance();
        assertTrue(board.advance());
        assertEquals(2, board.room());
        assertEquals(0, board.step());
    }

    @Test
    void advance_cyclesBackToTheStartWhenOnlyOneRoomIsSolved() {
        Progress solved = Progress.empty().withSolved(1, 3, 3);
        MenuBoard board = new MenuBoard(solved, THREE);
        board.attach(1, "RR");

        board.advance();
        board.advance();
        board.advance();
        assertEquals(1, board.room());
        assertEquals(0, board.step());
    }

    @Test
    void advance_neverLandsOnAnUnsolvedRoom() {
        Progress solved = Progress.empty().withSolved(1, 3, 3);
        MenuBoard board = new MenuBoard(solved, THREE);
        board.attach(1, "RR");

        for (int i = 0; i < 12; i++) {
            board.advance();
            assertTrue(board.solved(board.room()));
        }
    }

    @Test
    void attach_ignoresOutOfRangeRooms() {
        MenuBoard board = new MenuBoard(Progress.empty().withSolved(0, 3, 3), THREE);

        board.attach(3, "RR");
        board.attach(-1, "RR");

        assertFalse(board.advance());
    }

    @Test
    void keepsTheMoveSequenceRetrievable() {
        MenuBoard board = new MenuBoard(Progress.empty().withSolved(0, 3, 3), THREE);
        board.attach(0, "RRLU");

        assertEquals("RRLU", board.solutionOf(0));
    }

    @Test
    void advance_playsARealSolverSolutionToCompletion() {
        List<com.milandru.sokobani.core.Level> levels = BoardFixture.classicPack().levels();
        MenuBoard board = new MenuBoard(Progress.empty().withSolved(0, 3, 3), levels);
        String moves = com.milandru.sokobani.solve.Solver.solve(levels.get(0)).orElseThrow().moves();
        board.attach(0, moves);

        for (int i = 0; i < moves.length(); i++) {
            assertTrue(board.advance());
        }
        assertTrue(board.state().isSolved());
        assertTrue(board.advance());
        assertFalse(board.state().isSolved());
    }

    @Test
    void refusesAnEmptyLevelList() {
        assertThrows(IllegalArgumentException.class, () -> new MenuBoard(Progress.empty(), List.of()));
    }
}
