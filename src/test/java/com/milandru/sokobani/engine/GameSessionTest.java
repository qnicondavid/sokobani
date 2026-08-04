package com.milandru.sokobani.engine;

import com.milandru.sokobani.core.Direction;
import com.milandru.sokobani.core.GameState;
import com.milandru.sokobani.core.MoveResult;
import com.milandru.sokobani.core.Position;
import com.milandru.sokobani.level.LevelPack;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameSessionTest {

    @Test
    void constructor_newPack_startsOnTheFirstLevel() {
        GameSession session = new GameSession(PackFixture.pack(PackFixture.ONE_PUSH, PackFixture.TWO_BOXES));

        assertEquals(0, session.levelIndex());
        assertEquals("Level 1", session.currentLevel().name());
        assertEquals(new Position(1, 1), session.state().player());
    }

    @Test
    void constructor_newPack_startsWithZeroedCountersAndNoHistory() {
        GameSession session = PackFixture.session(PackFixture.ONE_PUSH);

        assertEquals(0, session.moveCount());
        assertEquals(0, session.pushCount());
        assertFalse(session.canUndo());
        assertFalse(session.isSolved());
    }

    @Test
    void constructor_emptyPack_throwsIllegalArgument() {
        LevelPack empty = new LevelPack("empty", List.of());

        assertThrows(IllegalArgumentException.class, () -> new GameSession(empty));
    }

    @Test
    void constructor_nullPack_throwsNullPointer() {
        assertThrows(NullPointerException.class, () -> new GameSession(null));
    }

    @Test
    void move_ontoFloor_movesThePlayerAndCountsTheMove() {
        GameSession session = PackFixture.session(PackFixture.ROOM_TO_WALK);

        MoveResult result = session.move(Direction.RIGHT);

        assertInstanceOf(MoveResult.Moved.class, result);
        assertEquals(new Position(1, 2), session.state().player());
        assertEquals(1, session.moveCount());
        assertEquals(0, session.pushCount());
    }

    @Test
    void move_intoABox_countsAMoveAndAPush() {
        GameSession session = PackFixture.session(PackFixture.ONE_PUSH);

        MoveResult result = session.move(Direction.RIGHT);

        assertInstanceOf(MoveResult.Pushed.class, result);
        assertEquals(new Position(1, 2), session.state().player());
        assertEquals(Set.of(new Position(1, 3)), session.state().boxes());
        assertEquals(1, session.moveCount());
        assertEquals(1, session.pushCount());
    }

    @Test
    void move_intoAWall_countsNothingAndLeavesNoHistory() {
        GameSession session = PackFixture.session(PackFixture.ONE_PUSH);

        MoveResult result = session.move(Direction.UP);

        assertInstanceOf(MoveResult.Blocked.class, result);
        assertEquals(new Position(1, 1), session.state().player());
        assertEquals(0, session.moveCount());
        assertFalse(session.canUndo());
    }

    @Test
    void move_nullDirection_throwsNullPointer() {
        GameSession session = PackFixture.session(PackFixture.ONE_PUSH);

        assertThrows(NullPointerException.class, () -> session.move(null));
    }

    @Test
    void isSolved_everyGoalCovered_returnsTrue() {
        GameSession session = PackFixture.session(PackFixture.ONE_PUSH);

        session.move(Direction.RIGHT);
        session.move(Direction.RIGHT);
        session.move(Direction.RIGHT);

        assertTrue(session.isSolved());
        assertEquals(3, session.pushCount());
    }

    @Test
    void state_calledTwice_returnsTheSameLiveState() {
        GameSession session = PackFixture.session(PackFixture.ONE_PUSH);
        GameState held = session.state();

        session.move(Direction.RIGHT);

        assertSame(held, session.state());
        assertEquals(new Position(1, 2), held.player());
    }

    @Test
    void state_afterRestart_isAFreshState() {
        GameSession session = PackFixture.session(PackFixture.ONE_PUSH);
        GameState before = session.state();
        session.move(Direction.RIGHT);

        session.restart();

        assertEquals(new GameState(session.currentLevel()), session.state());
        assertEquals(new Position(1, 2), before.player());
    }

    @Test
    void restart_afterSeveralMoves_zeroesTheCountersAndTheBoard() {
        GameSession session = PackFixture.session(PackFixture.ONE_PUSH);
        session.move(Direction.RIGHT);
        session.move(Direction.RIGHT);

        session.restart();

        assertEquals(0, session.moveCount());
        assertEquals(0, session.pushCount());
        assertEquals(new Position(1, 1), session.state().player());
        assertEquals(Set.of(new Position(1, 2)), session.state().boxes());
    }

    @Test
    void restart_afterSeveralMoves_staysOnTheSameLevel() {
        GameSession session = new GameSession(PackFixture.pack(PackFixture.ONE_PUSH, PackFixture.TWO_BOXES));
        PackFixture.solveOnePush(session);
        session.loadLevel(1);
        session.move(Direction.RIGHT);

        session.restart();

        assertEquals(1, session.levelIndex());
        assertEquals("Level 2", session.currentLevel().name());
    }

    @Test
    void loadLevel_anotherIndex_switchesTheLevelAndZeroesTheCounters() {
        GameSession session = new GameSession(PackFixture.pack(PackFixture.ONE_PUSH, PackFixture.TWO_BOXES));
        PackFixture.solveOnePush(session);

        session.loadLevel(1);

        assertEquals(1, session.levelIndex());
        assertEquals("Level 2", session.currentLevel().name());
        assertEquals(0, session.moveCount());
        assertEquals(0, session.pushCount());
    }

    @Test
    void loadLevel_indexPastTheEnd_throwsAndLeavesTheSessionWhereItWas() {
        GameSession session = new GameSession(PackFixture.pack(PackFixture.ONE_PUSH, PackFixture.TWO_BOXES));
        session.move(Direction.RIGHT);

        assertThrows(IndexOutOfBoundsException.class, () -> session.loadLevel(2));

        assertEquals(0, session.levelIndex());
        assertEquals(1, session.moveCount());
        assertTrue(session.canUndo());
    }

    @Test
    void loadLevel_negativeIndex_throwsIndexOutOfBounds() {
        GameSession session = PackFixture.session(PackFixture.ONE_PUSH);

        assertThrows(IndexOutOfBoundsException.class, () -> session.loadLevel(-1));
    }

    @Test
    void nextLevel_withALevelRemaining_advancesAndReportsTrue() {
        GameSession session = new GameSession(PackFixture.pack(PackFixture.ONE_PUSH, PackFixture.TWO_BOXES));
        PackFixture.solveOnePush(session);

        assertTrue(session.nextLevel());
        assertEquals(1, session.levelIndex());
        assertEquals("Level 2", session.currentLevel().name());
    }

    @Test
    void nextLevel_onTheLastLevel_reportsFalseAndStaysPut() {
        GameSession session = new GameSession(PackFixture.pack(PackFixture.ONE_PUSH, PackFixture.TWO_BOXES));
        PackFixture.solveOnePush(session);
        session.loadLevel(1);
        session.move(Direction.RIGHT);

        assertFalse(session.nextLevel());
        assertEquals(1, session.levelIndex());
        assertEquals(1, session.moveCount());
    }

    @Test
    void hasNextLevel_onEachLevelOfThePack_isTrueUntilTheLast() {
        GameSession session = new GameSession(
                PackFixture.pack(PackFixture.ONE_PUSH, PackFixture.TWO_BOXES, PackFixture.OPEN_ROOM));

        assertTrue(session.hasNextLevel());
        PackFixture.solveOnePush(session);
        session.nextLevel();
        assertTrue(session.hasNextLevel());
        PackFixture.solveTwoBoxes(session);
        session.nextLevel();
        assertFalse(session.hasNextLevel());
    }

    @Test
    void pack_anySession_isThePackItWasBuiltFrom() {
        LevelPack pack = PackFixture.pack(PackFixture.ONE_PUSH, PackFixture.TWO_BOXES);
        GameSession session = new GameSession(pack);

        assertSame(pack, session.pack());
        assertEquals(2, session.pack().size());
    }

    @Test
    void currentLevel_anySession_isTheLevelAtTheCurrentIndex() {
        LevelPack pack = PackFixture.pack(PackFixture.ONE_PUSH, PackFixture.TWO_BOXES);
        GameSession session = new GameSession(pack);
        PackFixture.solveOnePush(session);

        session.nextLevel();

        assertEquals(pack.get(1), session.currentLevel());
    }
}
