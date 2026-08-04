package com.milandru.sokobani.engine;

import com.milandru.sokobani.core.Level;
import com.milandru.sokobani.core.Position;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameEventTest {

    @Test
    void describe_switchWithoutADefaultBranch_handlesEveryImplementation() {
        Level level = PackFixture.level(PackFixture.ONE_PUSH);

        assertEquals("moved", describe(new GameEvent.Moved(new Position(1, 1), new Position(1, 2))));
        assertEquals("pushed", describe(new GameEvent.Pushed(
                new Position(1, 1), new Position(1, 2), new Position(1, 2), new Position(1, 3))));
        assertEquals("undone", describe(GameEvent.Undone.ofMove(new Position(1, 2), new Position(1, 1))));
        assertEquals("restarted", describe(new GameEvent.Restarted(level)));
        assertEquals("loaded", describe(new GameEvent.LevelLoaded(level)));
        assertEquals("solved", describe(new GameEvent.Solved(level, 3, 3)));
    }

    @Test
    void ofMove_anyUndoneMove_carriesNeitherBoxPosition() {
        GameEvent.Undone undone = GameEvent.Undone.ofMove(new Position(1, 2), new Position(1, 1));

        assertEquals(Optional.empty(), undone.boxFrom());
        assertEquals(Optional.empty(), undone.boxTo());
        assertFalse(undone.isPush());
    }

    @Test
    void ofPush_anyUndonePush_carriesBothBoxPositions() {
        GameEvent.Undone undone = GameEvent.Undone.ofPush(
                new Position(1, 2), new Position(1, 1), new Position(1, 3), new Position(1, 2));

        assertEquals(Optional.of(new Position(1, 3)), undone.boxFrom());
        assertEquals(Optional.of(new Position(1, 2)), undone.boxTo());
        assertTrue(undone.isPush());
    }

    @Test
    void constructor_undoneWithOnlyOneBoxPosition_throwsIllegalArgument() {
        Optional<Position> box = Optional.of(new Position(1, 3));
        Position from = new Position(1, 2);
        Position to = new Position(1, 1);

        assertThrows(IllegalArgumentException.class, () -> new GameEvent.Undone(from, to, box, Optional.empty()));
        assertThrows(IllegalArgumentException.class, () -> new GameEvent.Undone(from, to, Optional.empty(), box));
    }

    @Test
    void equals_twoEventsDescribingTheSameMove_areEqual() {
        GameEvent.Moved one = new GameEvent.Moved(new Position(1, 1), new Position(1, 2));
        GameEvent.Moved other = new GameEvent.Moved(new Position(1, 1), new Position(1, 2));

        assertEquals(one, other);
        assertEquals(one.hashCode(), other.hashCode());
    }

    @Test
    void constructor_nullPosition_throwsNullPointer() {
        Position position = new Position(1, 1);

        assertThrows(NullPointerException.class, () -> new GameEvent.Moved(null, position));
        assertThrows(NullPointerException.class, () -> new GameEvent.Moved(position, null));
        assertThrows(NullPointerException.class,
                () -> new GameEvent.Pushed(position, position, position, null));
        assertThrows(NullPointerException.class,
                () -> GameEvent.Undone.ofPush(position, position, position, null));
    }

    @Test
    void constructor_nullLevel_throwsNullPointer() {
        assertThrows(NullPointerException.class, () -> new GameEvent.Restarted(null));
        assertThrows(NullPointerException.class, () -> new GameEvent.LevelLoaded(null));
        assertThrows(NullPointerException.class, () -> new GameEvent.Solved(null, 0, 0));
    }

    @Test
    void solved_anySolvedEvent_carriesTheCountersItWasGiven() {
        Level level = PackFixture.level(PackFixture.ONE_PUSH);

        GameEvent.Solved solved = new GameEvent.Solved(level, 12, 5);

        assertEquals(level, solved.level());
        assertEquals(12, solved.moveCount());
        assertEquals(5, solved.pushCount());
    }

    private static String describe(GameEvent event) {
        return switch (event) {
            case GameEvent.Moved moved -> "moved";
            case GameEvent.Pushed pushed -> "pushed";
            case GameEvent.Undone undone -> "undone";
            case GameEvent.Restarted restarted -> "restarted";
            case GameEvent.LevelLoaded loaded -> "loaded";
            case GameEvent.Solved solved -> "solved";
        };
    }
}
