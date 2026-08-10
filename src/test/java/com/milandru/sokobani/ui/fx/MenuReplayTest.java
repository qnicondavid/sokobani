package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.core.Direction;
import com.milandru.sokobani.core.GameState;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MenuReplayTest {

    @Test
    void advance_appliesExactlyTheRequestedMoves() {
        GameState state = BoardFixture.state(BoardFixture.ONE_PUSH);

        MenuReplay.advance(state, "RR", 0, 1);

        assertEquals(1, state.moveCount());
        assertFalse(state.isSolved());
    }

    @Test
    void advance_finishesTheLevelWhenTheWholeSequenceIsGiven() {
        GameState state = BoardFixture.state(BoardFixture.ONE_PUSH);

        MenuReplay.advance(state, "RRR", 0, 3);

        assertEquals(3, state.moveCount());
        assertTrue(state.isSolved());
    }

    @Test
    void advance_canContinueFromAMidSequenceOffset() {
        GameState state = BoardFixture.state(BoardFixture.ONE_PUSH);

        MenuReplay.advance(state, "XXRRR", 2, 3);

        assertEquals(3, state.moveCount());
        assertTrue(state.isSolved());
    }

    @Test
    void directionOf_mapsTheFourMoveSymbols() {
        assertEquals(Direction.UP, MenuReplay.directionOf('U'));
        assertEquals(Direction.DOWN, MenuReplay.directionOf('D'));
        assertEquals(Direction.LEFT, MenuReplay.directionOf('L'));
        assertEquals(Direction.RIGHT, MenuReplay.directionOf('R'));
    }

    @Test
    void directionOf_rejectsUnknownSymbols() {
        assertThrows(IllegalArgumentException.class, () -> MenuReplay.directionOf('X'));
    }
}
