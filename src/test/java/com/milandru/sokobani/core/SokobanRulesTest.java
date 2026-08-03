package com.milandru.sokobani.core;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SokobanRulesTest {

    @Test
    void apply_targetIsFloor_movesThePlayer() {
        GameState state = stateOf("""
                #####
                #@  #
                #   #
                #####
                """);

        MoveResult result = SokobanRules.apply(state, Direction.RIGHT);

        assertInstanceOf(MoveResult.Moved.class, result);
        assertEquals(new Position(1, 2), state.player());
    }

    @Test
    void apply_targetIsGoal_movesThePlayer() {
        GameState state = stateOf("""
                #####
                #@. #
                #####
                """);

        MoveResult result = SokobanRules.apply(state, Direction.RIGHT);

        assertInstanceOf(MoveResult.Moved.class, result);
        assertEquals(new Position(1, 2), state.player());
        assertEquals(Tile.GOAL, state.level().tileAt(state.player()));
    }

    @Test
    void apply_targetIsWall_returnsBlockedByWall() {
        GameState state = stateOf("""
                #####
                #@  #
                #   #
                #####
                """);

        MoveResult result = SokobanRules.apply(state, Direction.UP);

        assertEquals(new MoveResult.Blocked(MoveResult.BlockedReason.WALL), result);
        assertEquals(new Position(1, 1), state.player());
    }

    @Test
    void apply_playerEnclosedByWalls_returnsBlockedInEveryDirection() {
        GameState state = stateOf("""
                ###
                #@#
                ###
                """);

        for (Direction direction : Direction.values()) {
            MoveResult result = SokobanRules.apply(state, direction);

            assertNotNull(result);
            assertEquals(new MoveResult.Blocked(MoveResult.BlockedReason.WALL), result);
        }
        assertEquals(new Position(1, 1), state.player());
        assertEquals(0, state.moveCount());
    }

    @Test
    void apply_targetIsOutsideTheGrid_returnsBlockedByWall() {
        GameState state = new GameState(new Level(
                new Tile[][]{{Tile.FLOOR, Tile.FLOOR}},
                new Position(0, 0),
                Set.of(),
                "open",
                0));

        assertEquals(new MoveResult.Blocked(MoveResult.BlockedReason.WALL),
                SokobanRules.apply(state, Direction.LEFT));
        assertEquals(new MoveResult.Blocked(MoveResult.BlockedReason.WALL),
                SokobanRules.apply(state, Direction.UP));
        assertEquals(new Position(0, 0), state.player());
    }

    @Test
    void apply_boxWithFloorBeyond_pushesTheBoxExactlyOneSquare() {
        GameState state = stateOf("""
                ######
                #@$  #
                ######
                """);

        MoveResult result = SokobanRules.apply(state, Direction.RIGHT);

        assertInstanceOf(MoveResult.Pushed.class, result);
        assertEquals(new Position(1, 2), state.player());
        assertEquals(Set.of(new Position(1, 3)), state.boxes());
    }

    @Test
    void apply_boxAboveThePlayer_pushesUpwards() {
        GameState state = stateOf("""
                #####
                #   #
                # $ #
                # @ #
                #####
                """);

        MoveResult result = SokobanRules.apply(state, Direction.UP);

        assertInstanceOf(MoveResult.Pushed.class, result);
        assertEquals(new Position(2, 2), state.player());
        assertEquals(Set.of(new Position(1, 2)), state.boxes());
    }

    @Test
    void apply_boxWithGoalBeyond_pushesTheBoxOntoTheGoal() {
        GameState state = stateOf("""
                #####
                #@$.#
                #####
                """);

        MoveResult result = SokobanRules.apply(state, Direction.RIGHT);

        assertInstanceOf(MoveResult.Pushed.class, result);
        assertEquals(Set.of(new Position(1, 3)), state.boxes());
        assertTrue(state.isSolved());
    }

    @Test
    void apply_boxWithWallBeyond_returnsBlockedAndMovesNothing() {
        GameState state = stateOf("""
                #####
                #@$##
                #####
                """);

        MoveResult result = SokobanRules.apply(state, Direction.RIGHT);

        assertEquals(new MoveResult.Blocked(MoveResult.BlockedReason.BOX_AGAINST_WALL), result);
        assertEquals(new Position(1, 1), state.player());
        assertEquals(Set.of(new Position(1, 2)), state.boxes());
    }

    @Test
    void apply_boxAtTheGridEdge_returnsBlockedAndMovesNothing() {
        GameState state = new GameState(new Level(
                new Tile[][]{{Tile.FLOOR, Tile.FLOOR, Tile.FLOOR}},
                new Position(0, 0),
                Set.of(new Position(0, 2)),
                "open",
                0));

        assertInstanceOf(MoveResult.Moved.class, SokobanRules.apply(state, Direction.RIGHT));

        MoveResult result = SokobanRules.apply(state, Direction.RIGHT);

        assertEquals(new MoveResult.Blocked(MoveResult.BlockedReason.BOX_AGAINST_WALL), result);
        assertEquals(new Position(0, 1), state.player());
        assertEquals(Set.of(new Position(0, 2)), state.boxes());
    }

    @Test
    void apply_boxWithAnotherBoxBeyond_returnsBlockedAndMovesNothing() {
        GameState state = stateOf("""
                ######
                #@$$ #
                ######
                """);

        MoveResult result = SokobanRules.apply(state, Direction.RIGHT);

        assertEquals(new MoveResult.Blocked(MoveResult.BlockedReason.BOX_AGAINST_BOX), result);
        assertEquals(new Position(1, 1), state.player());
        assertEquals(Set.of(new Position(1, 2), new Position(1, 3)), state.boxes());
        assertEquals(0, state.moveCount());
        assertEquals(0, state.pushCount());
    }

    @Test
    void apply_threeBoxesInARowWithFloorBeyond_neverMovesAnyOfThem() {
        GameState state = stateOf("""
                #######
                #@$$$ #
                #######
                """);

        MoveResult result = SokobanRules.apply(state, Direction.RIGHT);

        assertEquals(new MoveResult.Blocked(MoveResult.BlockedReason.BOX_AGAINST_BOX), result);
        assertEquals(Set.of(new Position(1, 2), new Position(1, 3), new Position(1, 4)), state.boxes());
        assertFalse(state.hasBoxAt(new Position(1, 5)));
        assertEquals(new Position(1, 1), state.player());
    }

    @Test
    void apply_boxOnAGoalWithAnotherGoalBeyond_pushesItOntoTheSecondGoal() {
        GameState state = stateOf("""
                ######
                #@*. #
                ######
                """);

        MoveResult result = SokobanRules.apply(state, Direction.RIGHT);

        assertInstanceOf(MoveResult.Pushed.class, result);
        assertEquals(Set.of(new Position(1, 3)), state.boxes());
        assertEquals(Set.of(new Position(1, 2), new Position(1, 3)), state.level().goals());
        assertFalse(state.isSolved());
    }

    @Test
    void apply_boxPushedOffItsGoal_leavesTheLevelUnsolved() {
        GameState state = stateOf("""
                ######
                #@*  #
                ######
                """);

        assertTrue(state.isSolved());

        MoveResult result = SokobanRules.apply(state, Direction.RIGHT);

        assertInstanceOf(MoveResult.Pushed.class, result);
        assertEquals(Set.of(new Position(1, 3)), state.boxes());
        assertFalse(state.isSolved());
    }

    @Test
    void apply_pushCoveringTheLastGoal_solvesTheLevel() {
        GameState state = stateOf("""
                #######
                #@$ .*#
                #######
                """);

        assertFalse(state.isSolved());

        SokobanRules.apply(state, Direction.RIGHT);
        assertFalse(state.isSolved());

        SokobanRules.apply(state, Direction.RIGHT);
        assertTrue(state.isSolved());
    }

    @Test
    void apply_move_returnsMovedCarryingDistinctFromAndTo() {
        GameState state = stateOf("""
                #####
                #@  #
                #####
                """);

        MoveResult.Moved moved = assertInstanceOf(MoveResult.Moved.class,
                SokobanRules.apply(state, Direction.RIGHT));

        assertEquals(new Position(1, 1), moved.from());
        assertEquals(new Position(1, 2), moved.to());
        assertNotEquals(moved.from(), moved.to());
    }

    @Test
    void apply_push_returnsPushedCarryingBothTheBoxOriginAndDestination() {
        GameState state = stateOf("""
                ######
                #@$  #
                ######
                """);

        MoveResult.Pushed pushed = assertInstanceOf(MoveResult.Pushed.class,
                SokobanRules.apply(state, Direction.RIGHT));

        assertEquals(new Position(1, 1), pushed.from());
        assertEquals(new Position(1, 2), pushed.to());
        assertEquals(new Position(1, 2), pushed.boxFrom());
        assertEquals(new Position(1, 3), pushed.boxTo());
        assertNotEquals(pushed.boxFrom(), pushed.boxTo());
    }

    @Test
    void apply_aSequenceOfMovesAndPushes_incrementsEachCounterOnlyOnRealMoves() {
        GameState state = stateOf("""
                ######
                #@$  #
                #    #
                ######
                """);

        assertEquals(new MoveResult.Blocked(MoveResult.BlockedReason.WALL),
                SokobanRules.apply(state, Direction.UP));
        assertCounters(state, 0, 0);

        assertInstanceOf(MoveResult.Moved.class, SokobanRules.apply(state, Direction.DOWN));
        assertCounters(state, 1, 0);

        assertInstanceOf(MoveResult.Moved.class, SokobanRules.apply(state, Direction.UP));
        assertCounters(state, 2, 0);

        assertInstanceOf(MoveResult.Pushed.class, SokobanRules.apply(state, Direction.RIGHT));
        assertCounters(state, 3, 1);

        assertInstanceOf(MoveResult.Pushed.class, SokobanRules.apply(state, Direction.RIGHT));
        assertCounters(state, 4, 2);

        assertEquals(new MoveResult.Blocked(MoveResult.BlockedReason.BOX_AGAINST_WALL),
                SokobanRules.apply(state, Direction.RIGHT));
        assertCounters(state, 4, 2);
    }

    @Test
    void apply_blockedByAWall_leavesTheStateIdenticalToAnUntouchedTwin() {
        Level level = twinLevel();
        GameState untouched = new GameState(level);
        GameState blocked = new GameState(level);

        SokobanRules.apply(untouched, Direction.RIGHT);
        SokobanRules.apply(blocked, Direction.RIGHT);

        assertEquals(new MoveResult.Blocked(MoveResult.BlockedReason.WALL),
                SokobanRules.apply(blocked, Direction.UP));

        assertEquals(untouched, blocked);
        assertEquals(untouched.player(), blocked.player());
        assertEquals(untouched.boxes(), blocked.boxes());
        assertEquals(untouched.moveCount(), blocked.moveCount());
        assertEquals(untouched.pushCount(), blocked.pushCount());
    }

    @Test
    void apply_blockedByABoxAgainstABox_leavesTheStateIdenticalToAnUntouchedTwin() {
        Level level = LevelFixture.level("""
                #######
                #@$$  #
                #######
                """);
        GameState untouched = new GameState(level);
        GameState blocked = new GameState(level);

        assertEquals(new MoveResult.Blocked(MoveResult.BlockedReason.BOX_AGAINST_BOX),
                SokobanRules.apply(blocked, Direction.RIGHT));

        assertEquals(untouched, blocked);
    }

    @Test
    void apply_nullState_throws() {
        assertThrows(NullPointerException.class, () -> SokobanRules.apply(null, Direction.UP));
    }

    @Test
    void apply_nullDirection_throws() {
        GameState state = stateOf("""
                #####
                #@  #
                #####
                """);

        assertThrows(NullPointerException.class, () -> SokobanRules.apply(state, null));
    }

    private static void assertCounters(GameState state, int moves, int pushes) {
        assertEquals(moves, state.moveCount());
        assertEquals(pushes, state.pushCount());
    }

    private static GameState stateOf(String layout) {
        return new GameState(LevelFixture.level(layout));
    }

    private static Level twinLevel() {
        return LevelFixture.level("""
                ######
                #@$  #
                #    #
                ######
                """);
    }
}
