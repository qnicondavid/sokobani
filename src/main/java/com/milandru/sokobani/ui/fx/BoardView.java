package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.core.GameState;
import com.milandru.sokobani.core.Level;
import com.milandru.sokobani.core.Position;
import com.milandru.sokobani.core.Tile;
import com.milandru.sokobani.ui.Surface;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class BoardView {

    private static final int[][] NEIGHBOURS = {
            {-1, 0},
            {1, 0},
            {0, -1},
            {0, 1}
    };

    private BoardView() {
    }

    public static int width(int columns) {
        return columns * Tiles.TILE;
    }

    public static int height(int rows) {
        return rows * Tiles.TILE;
    }

    public static void draw(Surface surface, GameState state, int originX, int originY) {
        draw(surface, state, originX, originY, Set.of());
    }

    public static void draw(Surface surface, GameState state, int originX, int originY,
                            Set<Position> deadlockedBoxes) {
        Objects.requireNonNull(surface, "surface");
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(deadlockedBoxes, "deadlockedBoxes");
        Level level = state.level();
        Optional<Position> anomaly = Anomaly.wallIn(level);
        Set<Position> hatched = hatchedAround(level, deadlockedBoxes);
        for (int row = 0; row < level.rowCount(); row++) {
            for (int col = 0; col < level.columnCount(); col++) {
                Position at = new Position(row, col);
                int x = originX + col * Tiles.TILE;
                int y = originY + row * Tiles.TILE;
                if (level.tileAt(at) == Tile.WALL) {
                    Tiles.wall(surface, x, y, grainAt(level, at, anomaly), col == 0, col == level.columnCount() - 1);
                } else {
                    contents(surface, state, level, at, x, y, hatched.contains(at));
                }
            }
        }
    }

    private static void contents(Surface surface, GameState state, Level level, Position at, int x, int y,
                                 boolean hatched) {
        if (hatched) {
            Tiles.floorHatched(surface, x, y);
        } else {
            Tiles.floor(surface, x, y);
        }
        boolean onGoal = level.tileAt(at) == Tile.GOAL;
        if (state.hasBoxAt(at)) {
            if (onGoal) {
                Tiles.boxOnGoal(surface, x, y);
            } else {
                Tiles.boxOffGoal(surface, x, y);
            }
            return;
        }
        if (onGoal) {
            Tiles.goal(surface, x, y);
        }
        if (state.player().equals(at)) {
            Tiles.player(surface, x, y);
        }
    }

    static Set<Position> hatchedAround(Level level, Set<Position> deadlockedBoxes) {
        if (deadlockedBoxes.isEmpty()) {
            return Set.of();
        }
        Set<Position> hatched = new HashSet<>();
        for (Position box : deadlockedBoxes) {
            for (int[] delta : NEIGHBOURS) {
                Position neighbour = new Position(box.row() + delta[0], box.col() + delta[1]);
                if (level.tileAt(neighbour) != Tile.WALL) {
                    hatched.add(neighbour);
                }
            }
        }
        return hatched;
    }

    private static Tiles.Grain grainAt(Level level, Position at, Optional<Position> anomaly) {
        if (anomaly.filter(at::equals).isPresent()) {
            return Tiles.Grain.DIAGONAL;
        }
        boolean outerRow = at.row() == 0 || at.row() == level.rowCount() - 1;
        return outerRow ? Tiles.Grain.VERTICAL : Tiles.Grain.HORIZONTAL;
    }
}
