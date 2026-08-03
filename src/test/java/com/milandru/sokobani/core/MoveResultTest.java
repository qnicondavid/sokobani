package com.milandru.sokobani.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoveResultTest {

    @Test
    void describe_switchWithoutADefaultBranch_handlesEveryImplementation() {
        List<MoveResult> results = List.of(
                new MoveResult.Moved(new Position(1, 1), new Position(1, 2)),
                new MoveResult.Pushed(new Position(1, 1), new Position(1, 2), new Position(1, 2), new Position(1, 3)),
                new MoveResult.Blocked(MoveResult.BlockedReason.WALL));

        List<String> described = results.stream().map(MoveResultTest::describe).toList();

        assertEquals(List.of("moved", "pushed", "blocked"), described);
    }

    @Test
    void moved_twoResultsWithTheSamePositions_areEqual() {
        assertEquals(new MoveResult.Moved(new Position(0, 0), new Position(0, 1)),
                new MoveResult.Moved(new Position(0, 0), new Position(0, 1)));
    }

    @Test
    void pushed_resultsDifferingOnlyInTheBoxDestination_areNotEqual() {
        assertNotEquals(
                new MoveResult.Pushed(new Position(0, 0), new Position(0, 1), new Position(0, 1), new Position(0, 2)),
                new MoveResult.Pushed(new Position(0, 0), new Position(0, 1), new Position(0, 1), new Position(0, 3)));
    }

    @Test
    void blocked_resultsWithDifferentReasons_areNotEqual() {
        assertNotEquals(new MoveResult.Blocked(MoveResult.BlockedReason.WALL),
                new MoveResult.Blocked(MoveResult.BlockedReason.BOX_AGAINST_BOX));
    }

    @Test
    void constructor_nullComponent_throws() {
        Position position = new Position(0, 0);

        assertThrows(NullPointerException.class, () -> new MoveResult.Moved(null, position));
        assertThrows(NullPointerException.class, () -> new MoveResult.Moved(position, null));
        assertThrows(NullPointerException.class,
                () -> new MoveResult.Pushed(position, position, position, null));
        assertThrows(NullPointerException.class, () -> new MoveResult.Blocked(null));
    }

    private static String describe(MoveResult result) {
        return switch (result) {
            case MoveResult.Moved ignored -> "moved";
            case MoveResult.Pushed ignored -> "pushed";
            case MoveResult.Blocked ignored -> "blocked";
        };
    }
}
