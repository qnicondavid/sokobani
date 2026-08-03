package com.milandru.sokobani.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DirectionTest {

    @Test
    void deltaRow_up_isMinusOne() {
        assertEquals(-1, Direction.UP.deltaRow());
        assertEquals(0, Direction.UP.deltaCol());
    }

    @Test
    void deltaRow_down_isPlusOne() {
        assertEquals(1, Direction.DOWN.deltaRow());
        assertEquals(0, Direction.DOWN.deltaCol());
    }

    @Test
    void deltaCol_left_isMinusOne() {
        assertEquals(0, Direction.LEFT.deltaRow());
        assertEquals(-1, Direction.LEFT.deltaCol());
    }

    @Test
    void deltaCol_right_isPlusOne() {
        assertEquals(0, Direction.RIGHT.deltaRow());
        assertEquals(1, Direction.RIGHT.deltaCol());
    }

    @Test
    void opposite_eachDirection_returnsTheReverseDirection() {
        assertEquals(Direction.DOWN, Direction.UP.opposite());
        assertEquals(Direction.UP, Direction.DOWN.opposite());
        assertEquals(Direction.RIGHT, Direction.LEFT.opposite());
        assertEquals(Direction.LEFT, Direction.RIGHT.opposite());
    }

    @Test
    void opposite_appliedTwice_returnsTheOriginalDirection() {
        for (Direction direction : Direction.values()) {
            assertEquals(direction, direction.opposite().opposite());
        }
    }

    @Test
    void opposite_anyDirection_cancelsBothDeltas() {
        for (Direction direction : Direction.values()) {
            assertEquals(0, direction.deltaRow() + direction.opposite().deltaRow());
            assertEquals(0, direction.deltaCol() + direction.opposite().deltaCol());
        }
    }
}
