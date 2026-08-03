package com.milandru.sokobani.core;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LevelTest {

    private static final String LAYOUT = """
            #####
            #@$.#
            #   #
            #####
            """;

    @Test
    void tileAt_positionInsideTheGrid_returnsThatTile() {
        Level level = LevelFixture.level(LAYOUT);

        assertEquals(Tile.WALL, level.tileAt(new Position(0, 0)));
        assertEquals(Tile.FLOOR, level.tileAt(new Position(1, 1)));
        assertEquals(Tile.FLOOR, level.tileAt(new Position(1, 2)));
        assertEquals(Tile.GOAL, level.tileAt(new Position(1, 3)));
    }

    @Test
    void tileAt_negativeRow_returnsWall() {
        assertEquals(Tile.WALL, LevelFixture.level(LAYOUT).tileAt(new Position(-1, 1)));
    }

    @Test
    void tileAt_negativeColumn_returnsWall() {
        assertEquals(Tile.WALL, LevelFixture.level(LAYOUT).tileAt(new Position(1, -1)));
    }

    @Test
    void tileAt_rowBeyondTheLastRow_returnsWall() {
        Level level = LevelFixture.level(LAYOUT);

        assertEquals(Tile.WALL, level.tileAt(new Position(level.rowCount(), 1)));
        assertEquals(Tile.WALL, level.tileAt(new Position(9999, 1)));
    }

    @Test
    void tileAt_columnBeyondTheLastColumn_returnsWall() {
        Level level = LevelFixture.level(LAYOUT);

        assertEquals(Tile.WALL, level.tileAt(new Position(1, level.columnCount())));
        assertEquals(Tile.WALL, level.tileAt(new Position(1, 9999)));
    }

    @Test
    void tileAt_positionOutsideEveryEdgeOfAWalllessGrid_returnsWall() {
        Tile[][] terrain = {{Tile.FLOOR}};
        Level level = new Level(terrain, new Position(0, 0), Set.of(), "open", 0);

        assertEquals(Tile.FLOOR, level.tileAt(new Position(0, 0)));
        assertEquals(Tile.WALL, level.tileAt(new Position(-1, 0)));
        assertEquals(Tile.WALL, level.tileAt(new Position(1, 0)));
        assertEquals(Tile.WALL, level.tileAt(new Position(0, -1)));
        assertEquals(Tile.WALL, level.tileAt(new Position(0, 1)));
    }

    @Test
    void constructor_terrainMutatedAfterwards_leavesTheLevelUnchanged() {
        Tile[][] terrain = {
                {Tile.WALL, Tile.WALL, Tile.WALL},
                {Tile.WALL, Tile.FLOOR, Tile.WALL},
                {Tile.WALL, Tile.WALL, Tile.WALL}
        };
        Level level = new Level(terrain, new Position(1, 1), Set.of(), "boxed", 0);

        terrain[1][1] = Tile.GOAL;

        assertEquals(Tile.FLOOR, level.tileAt(new Position(1, 1)));
    }

    @Test
    void constructor_boxSetMutatedAfterwards_leavesTheLevelUnchanged() {
        Set<Position> boxes = new HashSet<>(Set.of(new Position(1, 2)));
        Tile[][] terrain = {{Tile.FLOOR, Tile.FLOOR, Tile.FLOOR}, {Tile.FLOOR, Tile.FLOOR, Tile.FLOOR}};
        Level level = new Level(terrain, new Position(0, 0), boxes, "boxed", 0);

        boxes.add(new Position(0, 2));
        boxes.remove(new Position(1, 2));

        assertEquals(Set.of(new Position(1, 2)), level.initialBoxes());
    }

    @Test
    void initialBoxes_mutatedByTheCaller_throws() {
        Set<Position> boxes = LevelFixture.level(LAYOUT).initialBoxes();

        assertThrows(UnsupportedOperationException.class, () -> boxes.add(new Position(2, 2)));
        assertThrows(UnsupportedOperationException.class, () -> boxes.clear());
    }

    @Test
    void goals_mutatedByTheCaller_throws() {
        Set<Position> goals = LevelFixture.level(LAYOUT).goals();

        assertThrows(UnsupportedOperationException.class, () -> goals.add(new Position(2, 2)));
    }

    @Test
    void goals_layoutWithGoalTiles_returnsEveryGoalPosition() {
        Level level = LevelFixture.level("""
                #####
                #@*.#
                #.  #
                #####
                """);

        assertEquals(Set.of(new Position(1, 2), new Position(1, 3), new Position(2, 1)), level.goals());
    }

    @Test
    void goals_layoutWithoutGoalTiles_returnsAnEmptySet() {
        assertTrue(LevelFixture.level("""
                #####
                #@$ #
                #####
                """).goals().isEmpty());
    }

    @Test
    void initialPlayer_layoutWithThePlayerOnAGoal_reportsThatPositionAndKeepsTheGoalTile() {
        Level level = LevelFixture.level("""
                #####
                #+$ #
                #####
                """);

        assertEquals(new Position(1, 1), level.initialPlayer());
        assertEquals(Tile.GOAL, level.tileAt(new Position(1, 1)));
    }

    @Test
    void rowCount_levelBuiltFromALayout_returnsTheNumberOfLines() {
        assertEquals(4, LevelFixture.level(LAYOUT).rowCount());
    }

    @Test
    void columnCount_levelBuiltFromALayout_returnsTheLineWidth() {
        assertEquals(5, LevelFixture.level(LAYOUT).columnCount());
    }

    @Test
    void name_levelBuiltWithMetadata_returnsThatName() {
        assertEquals("First Steps", LevelFixture.level(LAYOUT, "First Steps", 3).name());
    }

    @Test
    void index_levelBuiltWithMetadata_returnsThatIndex() {
        assertEquals(3, LevelFixture.level(LAYOUT, "First Steps", 3).index());
    }

    @Test
    void constructor_emptyTerrain_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new Level(new Tile[0][0], new Position(0, 0), Set.of(), "empty", 0));
    }

    @Test
    void constructor_terrainWithNoColumns_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new Level(new Tile[1][0], new Position(0, 0), Set.of(), "empty", 0));
    }

    @Test
    void constructor_raggedTerrain_throws() {
        Tile[][] ragged = {
                {Tile.FLOOR, Tile.FLOOR},
                {Tile.FLOOR}
        };

        assertThrows(IllegalArgumentException.class,
                () -> new Level(ragged, new Position(0, 0), Set.of(), "ragged", 0));
    }

    @Test
    void constructor_terrainWithANullTile_throws() {
        Tile[][] holed = {{Tile.FLOOR, null}};

        assertThrows(NullPointerException.class,
                () -> new Level(holed, new Position(0, 0), Set.of(), "holed", 0));
    }

    @Test
    void constructor_playerOutsideTheGrid_throws() {
        Tile[][] terrain = {{Tile.FLOOR, Tile.FLOOR}};

        assertThrows(IllegalArgumentException.class,
                () -> new Level(terrain, new Position(50, 50), Set.of(), "stray", 0));
        assertThrows(IllegalArgumentException.class,
                () -> new Level(terrain, new Position(-1, 0), Set.of(), "stray", 0));
    }

    @Test
    void constructor_playerOnAWall_throws() {
        Tile[][] terrain = {{Tile.WALL, Tile.FLOOR}};

        assertThrows(IllegalArgumentException.class,
                () -> new Level(terrain, new Position(0, 0), Set.of(), "walled", 0));
    }

    @Test
    void constructor_boxOutsideTheGrid_throws() {
        Tile[][] terrain = {{Tile.FLOOR, Tile.FLOOR}};

        assertThrows(IllegalArgumentException.class,
                () -> new Level(terrain, new Position(0, 0), Set.of(new Position(0, 7)), "stray", 0));
    }

    @Test
    void constructor_boxOnAWall_throws() {
        Tile[][] terrain = {{Tile.FLOOR, Tile.WALL}};

        assertThrows(IllegalArgumentException.class,
                () -> new Level(terrain, new Position(0, 0), Set.of(new Position(0, 1)), "walled", 0));
    }

    @Test
    void constructor_boxOnThePlayerSquare_throws() {
        Tile[][] terrain = {{Tile.FLOOR, Tile.FLOOR}};

        assertThrows(IllegalArgumentException.class,
                () -> new Level(terrain, new Position(0, 0), Set.of(new Position(0, 0)), "stacked", 0));
    }

    @Test
    void constructor_playerAndBoxesStandingOnGoals_isAccepted() {
        Level level = LevelFixture.level("""
                #####
                #+* #
                #####
                """);

        assertEquals(new Position(1, 1), level.initialPlayer());
        assertEquals(Set.of(new Position(1, 2)), level.initialBoxes());
        assertEquals(Set.of(new Position(1, 1), new Position(1, 2)), level.goals());
    }

    @Test
    void constructor_nullArgument_throws() {
        Tile[][] terrain = {{Tile.FLOOR}};

        assertThrows(NullPointerException.class,
                () -> new Level(null, new Position(0, 0), Set.of(), "null", 0));
        assertThrows(NullPointerException.class,
                () -> new Level(terrain, null, Set.of(), "null", 0));
        assertThrows(NullPointerException.class,
                () -> new Level(terrain, new Position(0, 0), null, "null", 0));
        assertThrows(NullPointerException.class,
                () -> new Level(terrain, new Position(0, 0), Set.of(), null, 0));
    }

    @Test
    void equals_levelsBuiltFromTheSameLayout_returnsTrue() {
        assertEquals(LevelFixture.level(LAYOUT, "one", 1), LevelFixture.level(LAYOUT, "one", 1));
        assertEquals(LevelFixture.level(LAYOUT, "one", 1).hashCode(), LevelFixture.level(LAYOUT, "one", 1).hashCode());
    }

    @Test
    void equals_levelsDifferingOnlyInTerrain_returnsFalse() {
        Level withGoal = LevelFixture.level("""
                #####
                #@$.#
                #####
                """);
        Level withFloor = LevelFixture.level("""
                #####
                #@$ #
                #####
                """);

        assertNotEquals(withGoal, withFloor);
    }

    @Test
    void equals_levelsDifferingOnlyInIndex_returnsFalse() {
        assertNotEquals(LevelFixture.level(LAYOUT, "one", 1), LevelFixture.level(LAYOUT, "one", 2));
    }
}
