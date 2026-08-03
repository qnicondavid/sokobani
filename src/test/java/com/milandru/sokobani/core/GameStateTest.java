package com.milandru.sokobani.core;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameStateTest {

    @Test
    void constructor_freshState_startsAtTheLevelStartingPositions() {
        Level level = LevelFixture.level("""
                ######
                #@$ .#
                ######
                """);

        GameState state = new GameState(level);

        assertEquals(new Position(1, 1), state.player());
        assertEquals(Set.of(new Position(1, 2)), state.boxes());
        assertEquals(0, state.moveCount());
        assertEquals(0, state.pushCount());
    }

    @Test
    void constructor_nullLevel_throws() {
        assertThrows(NullPointerException.class, () -> new GameState(null));
    }

    @Test
    void boxes_mutatedByTheCaller_throws() {
        GameState state = new GameState(LevelFixture.level("""
                ######
                #@$ .#
                ######
                """));
        Set<Position> boxes = state.boxes();

        assertThrows(UnsupportedOperationException.class, () -> boxes.add(new Position(1, 3)));
        assertThrows(UnsupportedOperationException.class, () -> boxes.remove(new Position(1, 2)));
        assertThrows(UnsupportedOperationException.class, () -> boxes.clear());
        assertEquals(Set.of(new Position(1, 2)), state.boxes());
    }

    @Test
    void boxes_afterAPush_reflectsTheBoxDestination() {
        GameState state = new GameState(LevelFixture.level("""
                ######
                #@$ .#
                ######
                """));
        Set<Position> boxes = state.boxes();

        SokobanRules.apply(state, Direction.RIGHT);

        assertEquals(Set.of(new Position(1, 3)), boxes);
    }

    @Test
    void boxes_afterAPush_leavesTheLevelStartingBoxesUnchanged() {
        Level level = LevelFixture.level("""
                ######
                #@$ .#
                ######
                """);
        GameState state = new GameState(level);

        SokobanRules.apply(state, Direction.RIGHT);

        assertEquals(Set.of(new Position(1, 2)), level.initialBoxes());
    }

    @Test
    void hasBoxAt_squareHoldingABox_returnsTrue() {
        GameState state = new GameState(LevelFixture.level("""
                ######
                #@$ .#
                ######
                """));

        assertTrue(state.hasBoxAt(new Position(1, 2)));
        assertFalse(state.hasBoxAt(new Position(1, 3)));
        assertFalse(state.hasBoxAt(new Position(-4, 7)));
    }

    @Test
    void isSolved_everyGoalCovered_returnsTrue() {
        GameState state = new GameState(LevelFixture.level("""
                #####
                #@**#
                #####
                """));

        assertTrue(state.isSolved());
    }

    @Test
    void isSolved_oneGoalUncovered_returnsFalse() {
        GameState state = new GameState(LevelFixture.level("""
                #####
                #@*.#
                #####
                """));

        assertFalse(state.isSolved());
    }

    @Test
    void isSolved_moreBoxesThanGoalsButOneGoalUncovered_returnsFalse() {
        GameState state = new GameState(LevelFixture.level("""
                #######
                #@$$ .#
                #  $  #
                #######
                """));

        assertEquals(3, state.boxes().size());
        assertEquals(1, state.level().goals().size());
        assertFalse(state.isSolved());
    }

    @Test
    void isSolved_moreBoxesThanGoalsAndEveryGoalCovered_returnsTrue() {
        GameState state = new GameState(LevelFixture.level("""
                #######
                #@$$ *#
                #  $  #
                #######
                """));

        assertEquals(4, state.boxes().size());
        assertEquals(1, state.level().goals().size());
        assertTrue(state.isSolved());
    }

    @Test
    void isSolved_levelWithoutGoals_returnsFalse() {
        GameState state = new GameState(LevelFixture.level("""
                #####
                #@$ #
                #####
                """));

        assertTrue(state.level().goals().isEmpty());
        assertFalse(state.isSolved());
    }

    @Test
    void isSolved_levelWithoutGoalsOrBoxes_returnsFalse() {
        GameState state = new GameState(LevelFixture.level("""
                #####
                #@  #
                #####
                """));

        assertFalse(state.isSolved());
    }

    @Test
    void equals_statesDrivenThroughTheSameMoves_returnsTrue() {
        GameState one = new GameState(startingLevel());
        GameState other = new GameState(startingLevel());

        SokobanRules.apply(one, Direction.RIGHT);
        SokobanRules.apply(other, Direction.RIGHT);

        assertEquals(one, other);
        assertEquals(one.hashCode(), other.hashCode());
    }

    @Test
    void equals_statesWithDifferentPlayerPositions_returnsFalse() {
        GameState one = new GameState(startingLevel());
        GameState other = new GameState(startingLevel());

        SokobanRules.apply(one, Direction.DOWN);

        assertNotEquals(one, other);
    }

    @Test
    void equals_statesWithTheSamePositionsButDifferentCounters_returnsFalse() {
        GameState one = new GameState(startingLevel());
        GameState other = new GameState(startingLevel());

        SokobanRules.apply(one, Direction.DOWN);
        SokobanRules.apply(one, Direction.UP);

        assertEquals(one.player(), other.player());
        assertEquals(one.boxes(), other.boxes());
        assertNotEquals(one, other);
    }

    @Test
    void equals_statesFromDifferentLevels_returnsFalse() {
        GameState one = new GameState(LevelFixture.level("""
                #####
                #@$.#
                #####
                """));
        GameState other = new GameState(LevelFixture.level("""
                #####
                #@$ #
                #####
                """));

        assertNotEquals(one, other);
    }

    @Test
    void toString_anyState_namesThePlayerBoxesAndCounters() {
        GameState state = new GameState(startingLevel());

        SokobanRules.apply(state, Direction.RIGHT);

        assertEquals("GameState[player=Position[row=1, col=2], boxes=[Position[row=1, col=3]], moves=1, pushes=1]",
                state.toString());
    }

    private static Level startingLevel() {
        return LevelFixture.level("""
                ######
                #@$ .#
                #    #
                ######
                """);
    }
}
