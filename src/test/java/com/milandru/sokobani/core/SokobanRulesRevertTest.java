package com.milandru.sokobani.core;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SokobanRulesRevertTest {

    @Test
    void revertMove_afterAMove_returnsThePlayerToTheSquareItLeft() {
        GameState state = stateOf("""
                #####
                #@  #
                #####
                """);
        SokobanRules.apply(state, Direction.RIGHT);

        SokobanRules.revertMove(state, Direction.RIGHT);

        assertEquals(new Position(1, 1), state.player());
    }

    @Test
    void revertMove_afterAMove_decrementsTheMoveCount() {
        GameState state = stateOf("""
                #####
                #@  #
                #####
                """);
        SokobanRules.apply(state, Direction.RIGHT);

        SokobanRules.revertMove(state, Direction.RIGHT);

        assertEquals(0, state.moveCount());
    }

    @Test
    void revertMove_afterAMove_leavesAStateEqualToAnUntouchedTwin() {
        Level level = LevelFixture.level("""
                #####
                #@  #
                #   #
                #####
                """);
        GameState state = new GameState(level);
        SokobanRules.apply(state, Direction.DOWN);

        SokobanRules.revertMove(state, Direction.DOWN);

        assertEquals(new GameState(level), state);
    }

    @Test
    void revertPush_afterAPush_returnsBothThePlayerAndTheBox() {
        GameState state = stateOf("""
                ######
                #@$  #
                ######
                """);
        SokobanRules.apply(state, Direction.RIGHT);

        SokobanRules.revertPush(state, Direction.RIGHT);

        assertEquals(new Position(1, 1), state.player());
        assertEquals(Set.of(new Position(1, 2)), state.boxes());
    }

    @Test
    void revertPush_afterAPush_decrementsBothCounters() {
        GameState state = stateOf("""
                ######
                #@$  #
                ######
                """);
        SokobanRules.apply(state, Direction.RIGHT);

        SokobanRules.revertPush(state, Direction.RIGHT);

        assertEquals(0, state.moveCount());
        assertEquals(0, state.pushCount());
    }

    @Test
    void revertPush_afterAPush_leavesAStateEqualToAnUntouchedTwin() {
        Level level = LevelFixture.level("""
                ######
                #@$ .#
                ######
                """);
        GameState state = new GameState(level);
        SokobanRules.apply(state, Direction.RIGHT);

        SokobanRules.revertPush(state, Direction.RIGHT);

        assertEquals(new GameState(level), state);
    }

    @Test
    void revertPush_boxPushedOntoAGoal_pullsItBackOffAgain() {
        GameState state = stateOf("""
                ######
                #@$. #
                ######
                """);
        SokobanRules.apply(state, Direction.RIGHT);

        SokobanRules.revertPush(state, Direction.RIGHT);

        assertEquals(Set.of(new Position(1, 2)), state.boxes());
        assertEquals(Tile.FLOOR, state.level().tileAt(new Position(1, 2)));
    }

    @Test
    void revertMove_untouchedState_throwsIllegalState() {
        GameState state = stateOf("""
                #####
                #@  #
                #####
                """);

        assertThrows(IllegalStateException.class, () -> SokobanRules.revertMove(state, Direction.RIGHT));
    }

    @Test
    void revertPush_untouchedState_throwsIllegalState() {
        GameState state = stateOf("""
                ######
                #@$  #
                ######
                """);

        assertThrows(IllegalStateException.class, () -> SokobanRules.revertPush(state, Direction.RIGHT));
    }

    @Test
    void revertPush_afterAPlainMove_throwsIllegalState() {
        GameState state = stateOf("""
                ######
                #@ $.#
                ######
                """);
        SokobanRules.apply(state, Direction.RIGHT);

        assertThrows(IllegalStateException.class, () -> SokobanRules.revertPush(state, Direction.RIGHT));
    }

    @Test
    void revertPush_noBoxAheadOfThePlayer_throwsIllegalState() {
        GameState state = stateOf("""
                ######
                #@$ .#
                #    #
                ######
                """);
        SokobanRules.apply(state, Direction.RIGHT);
        SokobanRules.apply(state, Direction.DOWN);

        assertThrows(IllegalStateException.class, () -> SokobanRules.revertPush(state, Direction.DOWN));
    }

    @Test
    void revertMove_squareBehindThePlayerIsAWall_throwsIllegalState() {
        GameState state = stateOf("""
                #####
                #@  #
                #####
                """);
        SokobanRules.apply(state, Direction.RIGHT);

        assertThrows(IllegalStateException.class, () -> SokobanRules.revertMove(state, Direction.UP));
    }

    @Test
    void revertMove_squareBehindThePlayerHoldsABox_throwsIllegalState() {
        GameState state = stateOf("""
                ######
                #@$ .#
                ######
                """);
        SokobanRules.apply(state, Direction.RIGHT);

        assertThrows(IllegalStateException.class, () -> SokobanRules.revertMove(state, Direction.LEFT));
    }

    @Test
    void revertMove_nullArgument_throwsNullPointer() {
        GameState state = stateOf("""
                #####
                #@  #
                #####
                """);

        assertThrows(NullPointerException.class, () -> SokobanRules.revertMove(null, Direction.RIGHT));
        assertThrows(NullPointerException.class, () -> SokobanRules.revertMove(state, null));
    }

    @Test
    void revertPush_nullArgument_throwsNullPointer() {
        GameState state = stateOf("""
                ######
                #@$  #
                ######
                """);

        assertThrows(NullPointerException.class, () -> SokobanRules.revertPush(null, Direction.RIGHT));
        assertThrows(NullPointerException.class, () -> SokobanRules.revertPush(state, null));
    }

    private static GameState stateOf(String layout) {
        return new GameState(LevelFixture.level(layout));
    }
}
