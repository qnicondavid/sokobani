package com.milandru.sokobani.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TileTest {

    @Test
    void isWalkable_wall_returnsFalse() {
        assertFalse(Tile.WALL.isWalkable());
    }

    @Test
    void isWalkable_floor_returnsTrue() {
        assertTrue(Tile.FLOOR.isWalkable());
    }

    @Test
    void isWalkable_goal_returnsTrue() {
        assertTrue(Tile.GOAL.isWalkable());
    }
}
