package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.core.Level;
import com.milandru.sokobani.core.Position;
import com.milandru.sokobani.core.Tile;
import com.milandru.sokobani.level.LevelPack;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnomalyTest {

    @Test
    void wallIn_theSameLevel_choosesTheSameTileEveryTime() {
        Level level = BoardFixture.level(BoardFixture.EVERY_TILE);

        assertEquals(Anomaly.wallIn(level), Anomaly.wallIn(level));
    }

    @Test
    void wallIn_twoLevelsWithTheSameTerrain_dependsOnlyOnTheLevelIndex() {
        Level first = BoardFixture.level(BoardFixture.WIDE_ROOM, 0);
        Level rebuilt = BoardFixture.level(BoardFixture.WIDE_ROOM, 0);

        assertEquals(Anomaly.wallIn(first), Anomaly.wallIn(rebuilt));
    }

    @Test
    void wallIn_acrossTheIndexRange_doesNotAlwaysPickTheSameTile() {
        Set<Position> chosen = new HashSet<>();

        for (int index = 0; index < 15; index++) {
            Anomaly.wallIn(BoardFixture.level(BoardFixture.WIDE_ROOM, index)).ifPresent(chosen::add);
        }

        assertTrue(chosen.size() > 1);
    }

    @Test
    void wallIn_everyBundledLevel_landsOnAWall() {
        for (Level level : BoardFixture.classicPack().levels()) {
            Optional<Position> chosen = Anomaly.wallIn(level);

            assertTrue(chosen.isPresent(), level.name());
            assertEquals(Tile.WALL, level.tileAt(chosen.orElseThrow()), level.name());
        }
    }

    @Test
    void wallIn_everyBundledLevel_isNeverAGoalNorUnderABoxOrThePlayer() {
        for (Level level : BoardFixture.classicPack().levels()) {
            Position chosen = Anomaly.wallIn(level).orElseThrow();

            assertFalse(level.goals().contains(chosen), level.name());
            assertFalse(level.initialBoxes().contains(chosen), level.name());
            assertFalse(level.initialPlayer().equals(chosen), level.name());
        }
    }

    @Test
    void wallIn_everyBundledLevel_isNeverAdjacentToThePlayerStart() {
        for (Level level : BoardFixture.classicPack().levels()) {
            Position chosen = Anomaly.wallIn(level).orElseThrow();
            Position start = level.initialPlayer();

            boolean adjacent = Math.abs(chosen.row() - start.row()) <= 1
                    && Math.abs(chosen.col() - start.col()) <= 1;

            assertFalse(adjacent, level.name() + " picked " + chosen + " beside " + start);
        }
    }

    @Test
    void wallIn_everyBundledLevel_picksExactlyOneTile() {
        LevelPack pack = BoardFixture.classicPack();

        for (Level level : pack.levels()) {
            assertEquals(1, Anomaly.wallIn(level).stream().count(), level.name());
        }
    }
}
