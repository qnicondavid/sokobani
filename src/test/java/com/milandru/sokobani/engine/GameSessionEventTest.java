package com.milandru.sokobani.engine;

import com.milandru.sokobani.core.Direction;
import com.milandru.sokobani.core.Position;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameSessionEventTest {

    @Test
    void move_ontoFloor_firesOneMovedEventCarryingBothSquares() {
        GameSession session = PackFixture.session(PackFixture.ROOM_TO_WALK);
        List<GameEvent> events = recorded(session);

        session.move(Direction.RIGHT);

        assertEquals(List.of(new GameEvent.Moved(new Position(1, 1), new Position(1, 2))), events);
    }

    @Test
    void move_intoABox_firesExactlyOnePushedEvent() {
        GameSession session = PackFixture.session(PackFixture.ONE_PUSH);
        List<GameEvent> events = recorded(session);

        session.move(Direction.RIGHT);

        assertEquals(List.of(new GameEvent.Pushed(
                new Position(1, 1), new Position(1, 2), new Position(1, 2), new Position(1, 3))), events);
    }

    @Test
    void move_threePushesInARow_firesThreePushedEventsAndNoMovedEvents() {
        GameSession session = PackFixture.session(PackFixture.ONE_PUSH);
        List<GameEvent> events = recorded(session);

        session.move(Direction.RIGHT);
        session.move(Direction.RIGHT);
        session.move(Direction.RIGHT);

        assertEquals(3, countOf(events, GameEvent.Pushed.class));
        assertEquals(0, countOf(events, GameEvent.Moved.class));
        assertEquals(1, countOf(events, GameEvent.Solved.class));
        assertEquals(4, events.size());
    }

    @Test
    void move_everyEventOfAPush_reportsSquaresThatDiffer() {
        GameSession session = PackFixture.session(PackFixture.ONE_PUSH);
        List<GameEvent> events = recorded(session);

        session.move(Direction.RIGHT);

        GameEvent.Pushed pushed = assertInstanceOf(GameEvent.Pushed.class, events.get(0));
        assertNotEquals(pushed.from(), pushed.to());
        assertNotEquals(pushed.boxFrom(), pushed.boxTo());
    }

    @Test
    void move_intoAWall_firesNothing() {
        GameSession session = PackFixture.session(PackFixture.ONE_PUSH);
        List<GameEvent> events = recorded(session);

        session.move(Direction.UP);

        assertEquals(List.of(), events);
    }

    @Test
    void move_theMoveThatSolvesTheLevel_firesSolvedAfterThePush() {
        GameSession session = PackFixture.session(PackFixture.ONE_PUSH);
        List<GameEvent> events = recorded(session);

        session.move(Direction.RIGHT);
        session.move(Direction.RIGHT);
        session.move(Direction.RIGHT);

        assertInstanceOf(GameEvent.Pushed.class, events.get(2));
        assertEquals(new GameEvent.Solved(session.currentLevel(), 3, 3), events.get(3));
    }

    @Test
    void move_severalMovesAfterTheLevelWasSolved_firesSolvedExactlyOnce() {
        GameSession session = PackFixture.session(PackFixture.ONE_PUSH);
        List<GameEvent> events = recorded(session);

        session.move(Direction.RIGHT);
        session.move(Direction.RIGHT);
        session.move(Direction.RIGHT);
        session.move(Direction.LEFT);
        session.move(Direction.RIGHT);
        session.move(Direction.LEFT);
        session.move(Direction.RIGHT);

        assertTrue(session.isSolved());
        assertEquals(1, countOf(events, GameEvent.Solved.class));
    }

    @Test
    void move_solvingALevelAgainAfterUndoingTheWinningPush_firesSolvedASecondTime() {
        GameSession session = PackFixture.session(PackFixture.ONE_PUSH);
        List<GameEvent> events = recorded(session);
        session.move(Direction.RIGHT);
        session.move(Direction.RIGHT);
        session.move(Direction.RIGHT);

        session.undo();
        session.move(Direction.RIGHT);

        assertTrue(session.isSolved());
        assertEquals(2, countOf(events, GameEvent.Solved.class));
    }

    @Test
    void undo_thatReturnsTheLastBoxToItsGoal_firesSolved() {
        GameSession session = PackFixture.session(PackFixture.GOAL_WITH_ROOM_BEYOND);
        List<GameEvent> events = recorded(session);
        session.move(Direction.RIGHT);
        session.move(Direction.RIGHT);

        session.undo();

        assertTrue(session.isSolved());
        assertEquals(2, countOf(events, GameEvent.Solved.class));
        assertInstanceOf(GameEvent.Undone.class, events.get(events.size() - 2));
        assertEquals(new GameEvent.Solved(session.currentLevel(), 1, 1), events.get(events.size() - 1));
    }

    @Test
    void undo_thatTakesTheLastBoxOffItsGoal_firesNoFurtherSolved() {
        GameSession session = PackFixture.session(PackFixture.GOAL_WITH_ROOM_BEYOND);
        List<GameEvent> events = recorded(session);
        session.move(Direction.RIGHT);

        session.undo();

        assertFalse(session.isSolved());
        assertEquals(1, countOf(events, GameEvent.Solved.class));
        assertInstanceOf(GameEvent.Undone.class, events.get(events.size() - 1));
    }

    @Test
    void undo_afterAPlainMove_firesUndoneWithoutABox() {
        GameSession session = PackFixture.session(PackFixture.ROOM_TO_WALK);
        session.move(Direction.RIGHT);
        List<GameEvent> events = recorded(session);

        session.undo();

        assertEquals(List.of(GameEvent.Undone.ofMove(new Position(1, 2), new Position(1, 1))), events);
    }

    @Test
    void undo_afterAPush_firesUndoneCarryingTheBoxBackToTheSquareThePlayerLeaves() {
        GameSession session = PackFixture.session(PackFixture.ONE_PUSH);
        session.move(Direction.RIGHT);
        List<GameEvent> events = recorded(session);

        session.undo();

        GameEvent.Undone undone = assertInstanceOf(GameEvent.Undone.class, events.get(0));
        assertEquals(new Position(1, 2), undone.from());
        assertEquals(new Position(1, 1), undone.to());
        assertEquals(Optional.of(new Position(1, 3)), undone.boxFrom());
        assertEquals(Optional.of(new Position(1, 2)), undone.boxTo());
        assertTrue(undone.isPush());
    }

    @Test
    void undo_emptyStack_firesNothing() {
        GameSession session = PackFixture.session(PackFixture.ONE_PUSH);
        List<GameEvent> events = recorded(session);

        session.undo();

        assertEquals(List.of(), events);
    }

    @Test
    void restart_anySession_firesRestartedCarryingTheLevel() {
        GameSession session = PackFixture.session(PackFixture.ONE_PUSH);
        session.move(Direction.RIGHT);
        List<GameEvent> events = recorded(session);

        session.restart();

        assertEquals(List.of(new GameEvent.Restarted(session.currentLevel())), events);
    }

    @Test
    void loadLevel_anotherIndex_firesLevelLoadedCarryingTheNewLevel() {
        GameSession session = new GameSession(PackFixture.pack(PackFixture.ONE_PUSH, PackFixture.TWO_BOXES));
        List<GameEvent> events = recorded(session);

        session.loadLevel(1);

        assertEquals(List.of(new GameEvent.LevelLoaded(session.currentLevel())), events);
    }

    @Test
    void nextLevel_onTheLastLevel_firesNothing() {
        GameSession session = PackFixture.session(PackFixture.ONE_PUSH);
        List<GameEvent> events = recorded(session);

        session.nextLevel();

        assertEquals(List.of(), events);
    }

    @Test
    void loadLevel_aLevelThatIsAlreadySolved_firesLevelLoadedWithoutSolved() {
        GameSession session = new GameSession(PackFixture.pack(PackFixture.ONE_PUSH, PackFixture.ALREADY_SOLVED));
        List<GameEvent> events = recorded(session);

        session.loadLevel(1);

        assertTrue(session.isSolved());
        assertEquals(List.of(new GameEvent.LevelLoaded(session.currentLevel())), events);
    }

    @Test
    void addListener_registeredTwice_receivesEveryEventTwice() {
        GameSession session = PackFixture.session(PackFixture.ROOM_TO_WALK);
        List<GameEvent> events = new ArrayList<>();
        session.addListener(events::add);
        session.addListener(events::add);

        session.move(Direction.RIGHT);

        assertEquals(2, events.size());
    }

    @Test
    void removeListener_aRegisteredListener_stopsItReceivingEvents() {
        GameSession session = PackFixture.session(PackFixture.ROOM_TO_WALK);
        List<GameEvent> events = new ArrayList<>();
        GameEventListener listener = events::add;
        session.addListener(listener);

        session.move(Direction.RIGHT);
        session.removeListener(listener);
        session.move(Direction.RIGHT);

        assertEquals(1, events.size());
    }

    @Test
    void removeListener_oneThatWasNeverAdded_changesNothing() {
        GameSession session = PackFixture.session(PackFixture.ROOM_TO_WALK);
        List<GameEvent> events = recorded(session);

        session.removeListener(event -> {
        });
        session.move(Direction.RIGHT);

        assertEquals(1, events.size());
    }

    @Test
    void addListener_nullListener_throwsNullPointer() {
        GameSession session = PackFixture.session(PackFixture.ROOM_TO_WALK);

        assertThrows(NullPointerException.class, () -> session.addListener(null));
    }

    @Test
    void addListener_calledFromInsideAListener_doesNotThrow() {
        GameSession session = PackFixture.session(PackFixture.OPEN_ROOM);
        List<GameEvent> late = new ArrayList<>();
        session.addListener(event -> session.addListener(late::add));

        session.move(Direction.DOWN);

        assertEquals(0, late.size());

        session.move(Direction.DOWN);

        assertEquals(1, late.size());
    }

    @Test
    void removeListener_calledFromInsideAListener_doesNotThrow() {
        GameSession session = PackFixture.session(PackFixture.OPEN_ROOM);
        List<GameEvent> events = new ArrayList<>();
        GameEventListener listener = events::add;
        session.addListener(event -> session.removeListener(listener));
        session.addListener(listener);

        session.move(Direction.DOWN);
        session.move(Direction.DOWN);

        assertEquals(1, events.size());
    }

    @Test
    void move_aListenerThatThrows_letsTheExceptionReachTheCaller() {
        GameSession session = PackFixture.session(PackFixture.ROOM_TO_WALK);
        session.addListener(event -> {
            throw new IllegalStateException("listener");
        });

        assertThrows(IllegalStateException.class, () -> session.move(Direction.RIGHT));
        assertEquals(1, session.moveCount());
    }

    private static List<GameEvent> recorded(GameSession session) {
        List<GameEvent> events = new ArrayList<>();
        session.addListener(events::add);
        return events;
    }

    private static long countOf(List<GameEvent> events, Class<? extends GameEvent> type) {
        return events.stream().filter(type::isInstance).count();
    }
}
