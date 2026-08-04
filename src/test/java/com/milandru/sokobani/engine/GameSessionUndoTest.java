package com.milandru.sokobani.engine;

import com.milandru.sokobani.core.Direction;
import com.milandru.sokobani.core.GameState;
import com.milandru.sokobani.core.Level;
import com.milandru.sokobani.core.MoveResult;
import com.milandru.sokobani.core.Position;
import com.milandru.sokobani.level.LevelPack;
import com.milandru.sokobani.level.LevelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameSessionUndoTest {

    private static final int MOVES = 50;

    @Test
    void undo_afterOneMove_restoresTheStateThatPrecededIt() {
        GameSession session = PackFixture.session(PackFixture.ROOM_TO_WALK);
        GameState before = new GameState(session.currentLevel());

        session.move(Direction.RIGHT);
        assertTrue(session.undo());

        assertEquals(before, session.state());
        assertEquals(0, session.moveCount());
        assertFalse(session.canUndo());
    }

    @Test
    void undo_afterOneMoveDeepInAGame_restoresTheStateThatPrecededIt() {
        GameSession session = PackFixture.session(PackFixture.OPEN_ROOM);
        session.move(Direction.DOWN);
        session.move(Direction.DOWN);
        Position player = session.state().player();
        Set<Position> boxes = Set.copyOf(session.state().boxes());
        int moves = session.moveCount();

        session.move(Direction.RIGHT);
        assertTrue(session.undo());

        assertEquals(player, session.state().player());
        assertEquals(boxes, session.state().boxes());
        assertEquals(moves, session.moveCount());
    }

    @Test
    void undo_afterOnePush_returnsBothThePlayerAndTheBox() {
        GameSession session = PackFixture.session(PackFixture.ONE_PUSH);
        GameState before = new GameState(session.currentLevel());

        session.move(Direction.RIGHT);
        assertTrue(session.undo());

        assertEquals(new Position(1, 1), session.state().player());
        assertEquals(Set.of(new Position(1, 2)), session.state().boxes());
        assertEquals(before, session.state());
    }

    @Test
    void undo_afterOnePush_decrementsThePushCount() {
        GameSession session = PackFixture.session(PackFixture.ONE_PUSH);
        session.move(Direction.RIGHT);
        session.move(Direction.RIGHT);

        assertTrue(session.undo());

        assertEquals(1, session.pushCount());
        assertEquals(1, session.moveCount());
    }

    @Test
    void undo_afterABoxWasPushedOntoItsGoal_takesTheLevelBackOutOfTheSolvedState() {
        GameSession session = PackFixture.session(PackFixture.ONE_PUSH);
        session.move(Direction.RIGHT);
        session.move(Direction.RIGHT);
        session.move(Direction.RIGHT);
        assertTrue(session.isSolved());

        assertTrue(session.undo());

        assertFalse(session.isSolved());
        assertEquals(Set.of(new Position(1, 4)), session.state().boxes());
    }

    @Test
    void undo_emptyStack_reportsFalseAndChangesNothing() {
        GameSession session = PackFixture.session(PackFixture.ONE_PUSH);
        GameState before = new GameState(session.currentLevel());

        assertFalse(session.undo());

        assertEquals(before, session.state());
        assertEquals(0, session.moveCount());
    }

    @Test
    void undo_calledMoreOftenThanThereAreMoves_stopsAtTheStartOfTheLevel() {
        GameSession session = PackFixture.session(PackFixture.ONE_PUSH);
        GameState before = new GameState(session.currentLevel());
        session.move(Direction.RIGHT);
        session.move(Direction.RIGHT);

        assertTrue(session.undo());
        assertTrue(session.undo());
        assertFalse(session.undo());
        assertFalse(session.undo());

        assertEquals(before, session.state());
    }

    @Test
    void undo_afterABlockedMove_undoesTheMoveBeforeIt() {
        GameSession session = PackFixture.session(PackFixture.ROOM_TO_WALK);
        GameState before = new GameState(session.currentLevel());
        session.move(Direction.RIGHT);
        session.move(Direction.UP);

        assertTrue(session.undo());

        assertEquals(before, session.state());
        assertFalse(session.canUndo());
    }

    @Test
    void undo_afterRestart_reportsFalseBecauseRestartClearedTheHistory() {
        GameSession session = PackFixture.session(PackFixture.ONE_PUSH);
        session.move(Direction.RIGHT);
        session.move(Direction.RIGHT);

        session.restart();

        assertFalse(session.canUndo());
        assertFalse(session.undo());
        assertEquals(new GameState(session.currentLevel()), session.state());
    }

    @Test
    void undo_afterLoadingALevel_reportsFalseBecauseLoadingClearedTheHistory() {
        GameSession session = new GameSession(PackFixture.pack(PackFixture.ONE_PUSH, PackFixture.TWO_BOXES));
        session.move(Direction.RIGHT);
        session.move(Direction.RIGHT);

        session.loadLevel(1);

        assertFalse(session.canUndo());
        assertFalse(session.undo());
        assertEquals(0, session.moveCount());
    }

    @Test
    void undo_afterAdvancingToTheNextLevel_reportsFalseBecauseTheHistoryWasCleared() {
        GameSession session = new GameSession(PackFixture.pack(PackFixture.ONE_PUSH, PackFixture.TWO_BOXES));
        session.move(Direction.RIGHT);

        assertTrue(session.nextLevel());

        assertFalse(session.canUndo());
        assertFalse(session.undo());
    }

    @ParameterizedTest
    @ValueSource(longs = {1L, 2L, 3L, 5L, 8L, 13L, 21L, 34L})
    void undo_fiftyRandomLegalMovesThenFiftyUndos_restoresTheOpeningState(long seed) {
        GameSession session = PackFixture.session(PackFixture.OPEN_ROOM);
        GameState opening = new GameState(session.currentLevel());

        assertEquals(MOVES, playRandomly(session, MOVES, new Random(seed)));
        assertEquals(MOVES, session.moveCount());
        for (int undone = 0; undone < MOVES; undone++) {
            assertTrue(session.undo());
        }

        assertEquals(opening, session.state());
        assertEquals(0, session.moveCount());
        assertEquals(0, session.pushCount());
        assertFalse(session.canUndo());
    }

    @ParameterizedTest
    @MethodSource("classicPackLevels")
    void undo_fiftyRandomLegalMovesThenFiftyUndos_restoresEveryLevelOfTheClassicPack(Level level) {
        GameSession session = new GameSession(new LevelPack("classic", List.of(level)));
        GameState opening = new GameState(level);

        int played = playRandomly(session, MOVES, new Random(level.index()));
        assertTrue(played > 0);
        assertEquals(played, session.moveCount());
        for (int undone = 0; undone < played; undone++) {
            assertTrue(session.undo());
        }

        assertEquals(opening, session.state());
        assertFalse(session.canUndo());
    }

    private static List<Level> classicPackLevels() throws Exception {
        return LevelRepository.load(LevelRepository.CLASSIC_PACK).levels();
    }

    private static int playRandomly(GameSession session, int moves, Random random) {
        List<Direction> options = new ArrayList<>(List.of(Direction.values()));
        int played = 0;
        while (played < moves) {
            Collections.shuffle(options, random);
            boolean accepted = false;
            for (Direction direction : options) {
                if (!(session.move(direction) instanceof MoveResult.Blocked)) {
                    played++;
                    accepted = true;
                    break;
                }
            }
            if (!accepted) {
                return played;
            }
        }
        return played;
    }
}
