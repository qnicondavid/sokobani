package com.milandru.sokobani.core;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PositionTest {

    @Test
    void moved_up_returnsThePositionOneRowAbove() {
        assertEquals(new Position(2, 5), new Position(3, 5).moved(Direction.UP));
    }

    @Test
    void moved_down_returnsThePositionOneRowBelow() {
        assertEquals(new Position(4, 5), new Position(3, 5).moved(Direction.DOWN));
    }

    @Test
    void moved_left_returnsThePositionOneColumnBefore() {
        assertEquals(new Position(3, 4), new Position(3, 5).moved(Direction.LEFT));
    }

    @Test
    void moved_right_returnsThePositionOneColumnAfter() {
        assertEquals(new Position(3, 6), new Position(3, 5).moved(Direction.RIGHT));
    }

    @Test
    void moved_anyDirection_leavesTheOriginalPositionUnchanged() {
        Position origin = new Position(3, 5);

        origin.moved(Direction.UP);
        origin.moved(Direction.RIGHT);

        assertEquals(new Position(3, 5), origin);
    }

    @Test
    void moved_thenTheOppositeDirection_returnsTheOriginalPosition() {
        Position origin = new Position(7, 2);

        for (Direction direction : Direction.values()) {
            assertEquals(origin, origin.moved(direction).moved(direction.opposite()));
        }
    }

    @Test
    void moved_nullDirection_throws() {
        assertThrows(NullPointerException.class, () -> new Position(0, 0).moved(null));
    }

    @Test
    void equals_positionsWithTheSameRowAndColumn_returnsTrue() {
        assertEquals(new Position(4, 9), new Position(4, 9));
    }

    @Test
    void equals_positionsWithSwappedRowAndColumn_returnsFalse() {
        assertNotEquals(new Position(4, 9), new Position(9, 4));
    }

    @Test
    void hashCode_equalPositions_producesTheSameValue() {
        assertEquals(new Position(6, 1).hashCode(), new Position(6, 1).hashCode());
    }

    @Test
    void hashCode_distinctInstancesWithEqualValues_collapseToOneHashSetEntry() {
        Set<Position> positions = new HashSet<>();

        positions.add(new Position(2, 3));
        positions.add(new Position(2, 3));

        assertEquals(1, positions.size());
        assertTrue(positions.contains(new Position(2, 3)));
        assertFalse(positions.contains(new Position(3, 2)));
    }

    @Test
    void hashCode_positionUsedAsAMapKey_findsTheValueWithAnEqualInstance() {
        Map<Position, String> byPosition = new HashMap<>();

        byPosition.put(new Position(1, 1), "box");

        assertEquals("box", byPosition.get(new Position(1, 1)));
    }
}
