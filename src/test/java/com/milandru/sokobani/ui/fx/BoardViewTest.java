package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.core.GameState;
import com.milandru.sokobani.core.Level;
import com.milandru.sokobani.core.Position;
import com.milandru.sokobani.core.Tile;
import com.milandru.sokobani.ui.Surface;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class BoardViewTest {

    @Test
    void draw_theSameState_producesIdenticalPixelsTwiceRunning() {
        GameState state = BoardFixture.state(BoardFixture.EVERY_TILE);

        assertArrayEquals(board(state).tones(), board(state).tones());
    }

    @Test
    void draw_everyBundledLevel_producesIdenticalPixelsTwiceRunning() {
        for (Level level : BoardFixture.classicPack().levels()) {
            GameState state = new GameState(level);

            assertArrayEquals(board(state).tones(), board(state).tones(), level.name());
        }
    }

    @Test
    void width_andHeight_areTheTileSizeTimesTheGrid() {
        assertEquals(7 * Tiles.TILE, BoardView.width(7));
        assertEquals(11 * Tiles.TILE, BoardView.width(11));
        assertEquals(3 * Tiles.TILE, BoardView.height(3));
        assertEquals(9 * Tiles.TILE, BoardView.height(9));
    }

    @Test
    void draw_atAnOrigin_paintsNothingOutsideTheBoard() {
        GameState state = BoardFixture.state(BoardFixture.ONE_PUSH);
        Level level = state.level();
        int margin = 6;
        Surface surface = new Surface(
                BoardView.width(level.columnCount()) + 2 * margin,
                BoardView.height(level.rowCount()) + 2 * margin);

        BoardView.draw(surface, state, margin, margin);

        for (int y = 0; y < surface.height(); y++) {
            for (int x = 0; x < surface.width(); x++) {
                boolean inside = x >= margin && x < surface.width() - margin
                        && y >= margin && y < surface.height() - margin;
                if (!inside) {
                    assertEquals(Surface.PAPER, surface.toneAt(x, y));
                }
            }
        }
    }

    @Test
    void draw_paintsBoxOnGoalAndBoxOffGoalWithDifferentArt() {
        Surface surface = board(BoardFixture.state(BoardFixture.EVERY_TILE));

        assertArrayEquals(expected(surface, 2, 1, Tiles::boxOffGoal), tileAt(surface, 2, 1));
        assertArrayEquals(expected(surface, 6, 1, Tiles::boxOnGoal), tileAt(surface, 6, 1));
        assertFalse(Arrays.equals(tileAt(surface, 2, 1), tileAt(surface, 6, 1)));
    }

    @Test
    void draw_paintsGoalsPlayersAndBareFloorWithTheirOwnArt() {
        Surface surface = board(BoardFixture.state(BoardFixture.EVERY_TILE));

        assertArrayEquals(expected(surface, 4, 1, Tiles::goal), tileAt(surface, 4, 1));
        assertArrayEquals(expected(surface, 3, 2, Tiles::player), tileAt(surface, 3, 2));
        assertArrayEquals(expected(surface, 1, 1, BoardViewTest::bareFloor), tileAt(surface, 1, 1));
    }

    @Test
    void draw_paintsAPlayerStandingOnAGoalOverTheGoalRings() {
        Surface surface = board(BoardFixture.state(BoardFixture.PLAYER_ON_GOAL));

        assertArrayEquals(expected(surface, 2, 2, (target, x, y) -> {
            Tiles.goal(target, x, y);
            Tiles.player(target, x, y);
        }), tileAt(surface, 2, 2));
    }

    @Test
    void draw_paintsTheAnomalyTileAgainstTheGrain() {
        Level level = BoardFixture.level(BoardFixture.WIDE_ROOM);
        Position anomaly = Anomaly.wallIn(level).orElseThrow();
        Surface surface = board(new GameState(level));

        assertArrayEquals(
                wallTile(surface, level, anomaly, Tiles.Grain.DIAGONAL),
                tileAt(surface, anomaly.col(), anomaly.row()));
    }

    @Test
    void draw_paintsExactlyOneWallAgainstTheGrain() {
        for (Level level : BoardFixture.classicPack().levels()) {
            Surface surface = board(new GameState(level));
            int againstTheGrain = 0;

            for (Position wall : wallsIn(level)) {
                if (Arrays.equals(wallTile(surface, level, wall, Tiles.Grain.DIAGONAL),
                        tileAt(surface, wall.col(), wall.row()))) {
                    againstTheGrain++;
                }
            }

            assertEquals(1, againstTheGrain, level.name());
        }
    }

    @Test
    void draw_scoresOuterRowWallsWithTheGrainAndInnerWallsAcrossIt() {
        Level level = BoardFixture.level(BoardFixture.WIDE_ROOM);
        Position anomaly = Anomaly.wallIn(level).orElseThrow();
        Surface surface = board(new GameState(level));

        for (Position wall : wallsIn(level)) {
            if (wall.equals(anomaly)) {
                continue;
            }
            boolean outerRow = wall.row() == 0 || wall.row() == level.rowCount() - 1;
            Tiles.Grain grain = outerRow ? Tiles.Grain.VERTICAL : Tiles.Grain.HORIZONTAL;

            assertArrayEquals(wallTile(surface, level, wall, grain),
                    tileAt(surface, wall.col(), wall.row()), wall.toString());
        }
    }

    @Test
    void draw_hatchesTheFloorBesideADeadlockedBox() {
        GameState state = BoardFixture.state(BoardFixture.ONE_PUSH);
        Level level = state.level();
        Surface plain = board(state);

        Surface hatched = new Surface(
                BoardView.width(level.columnCount()), BoardView.height(level.rowCount()));
        BoardView.draw(hatched, state, 0, 0, Set.of(new Position(1, 2)));

        assertArrayEquals(hatchedFloorTile(hatched, 3, 1), tileAt(hatched, 3, 1));
        assertFalse(Arrays.equals(tileAt(plain, 3, 1), tileAt(hatched, 3, 1)));
    }

    @Test
    void draw_withoutDeadlockedBoxes_neverHatchesTheFloor() {
        GameState state = BoardFixture.state(BoardFixture.ONE_PUSH);
        Level level = state.level();
        Surface surface = board(state);

        assertArrayEquals(bareFloorTile(surface, 3, 1), tileAt(surface, 3, 1));
    }

    private static List<Position> wallsIn(Level level) {
        List<Position> walls = new ArrayList<>();
        for (int row = 0; row < level.rowCount(); row++) {
            for (int col = 0; col < level.columnCount(); col++) {
                Position at = new Position(row, col);
                if (level.tileAt(at) == Tile.WALL) {
                    walls.add(at);
                }
            }
        }
        return walls;
    }

    private static int[] wallTile(Surface reference, Level level, Position at, Tiles.Grain grain) {
        Surface surface = new Surface(reference.width(), reference.height());
        Tiles.wall(surface, at.col() * Tiles.TILE, at.row() * Tiles.TILE, grain,
                at.col() == 0, at.col() == level.columnCount() - 1);
        return tileAt(surface, at.col(), at.row());
    }

    private static void bareFloor(Surface surface, int x, int y) {
    }

    private static int[] bareFloorTile(Surface surface, int column, int row) {
        Surface target = new Surface(surface.width(), surface.height());
        Tiles.floor(target, column * Tiles.TILE, row * Tiles.TILE);
        return tileAt(target, column, row);
    }

    private static int[] hatchedFloorTile(Surface surface, int column, int row) {
        Surface target = new Surface(surface.width(), surface.height());
        Tiles.floorHatched(target, column * Tiles.TILE, row * Tiles.TILE);
        return tileAt(target, column, row);
    }

    private static Surface board(GameState state) {
        Level level = state.level();
        Surface surface = new Surface(
                BoardView.width(level.columnCount()), BoardView.height(level.rowCount()));
        BoardView.draw(surface, state, 0, 0);
        return surface;
    }

    private static int[] expected(Surface reference, int column, int row, BoardFixture.TileArt art) {
        Surface surface = new Surface(reference.width(), reference.height());
        int x = column * Tiles.TILE;
        int y = row * Tiles.TILE;
        Tiles.floor(surface, x, y);
        art.draw(surface, x, y);
        return tileAt(surface, column, row);
    }

    private static int[] tileAt(Surface surface, int column, int row) {
        int[] tones = new int[Tiles.TILE * Tiles.TILE];
        for (int y = 0; y < Tiles.TILE; y++) {
            for (int x = 0; x < Tiles.TILE; x++) {
                tones[y * Tiles.TILE + x] = surface.toneAt(column * Tiles.TILE + x, row * Tiles.TILE + y);
            }
        }
        return tones;
    }
}
