package com.milandru.sokobani.core;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class Deadlock {

    private Deadlock() {
    }

    public static Set<Position> deadlockedBoxes(GameState state) {
        Objects.requireNonNull(state, "state");
        Set<Position> dead = new HashSet<>();
        for (Position box : state.boxes()) {
            if (state.level().goals().contains(box)) {
                continue;
            }
            if (cornered(state.level(), box) || runDead(state.level(), box)) {
                dead.add(box);
            }
        }
        return dead;
    }

    private static boolean cornered(Level level, Position box) {
        boolean wallNorth = isWall(level, box.row() - 1, box.col());
        boolean wallSouth = isWall(level, box.row() + 1, box.col());
        boolean wallWest = isWall(level, box.row(), box.col() - 1);
        boolean wallEast = isWall(level, box.row(), box.col() + 1);
        return (wallNorth && wallWest)
                || (wallNorth && wallEast)
                || (wallSouth && wallWest)
                || (wallSouth && wallEast);
    }

    private static boolean runDead(Level level, Position box) {
        return rowRunDead(level, box, -1)
                || rowRunDead(level, box, 1)
                || colRunDead(level, box, -1)
                || colRunDead(level, box, 1);
    }

    private static boolean rowRunDead(Level level, Position box, int dr) {
        int row = box.row();
        if (!isWall(level, row + dr, box.col())) {
            return false;
        }
        int left = box.col();
        int right = box.col();
        while (left - 1 >= 0
                && level.tileAt(new Position(row, left - 1)) != Tile.WALL
                && isWall(level, row + dr, left - 1)) {
            left--;
        }
        while (right + 1 < level.columnCount()
                && level.tileAt(new Position(row, right + 1)) != Tile.WALL
                && isWall(level, row + dr, right + 1)) {
            right++;
        }
        boolean leftClosed = left == 0 || level.tileAt(new Position(row, left - 1)) == Tile.WALL;
        boolean rightClosed = right == level.columnCount() - 1
                || level.tileAt(new Position(row, right + 1)) == Tile.WALL;
        if (!leftClosed || !rightClosed) {
            return false;
        }
        for (int c = left; c <= right; c++) {
            if (level.goals().contains(new Position(row, c))) {
                return false;
            }
        }
        return true;
    }

    private static boolean colRunDead(Level level, Position box, int dc) {
        int col = box.col();
        if (!isWall(level, box.row(), col + dc)) {
            return false;
        }
        int top = box.row();
        int bottom = box.row();
        while (top - 1 >= 0
                && level.tileAt(new Position(top - 1, col)) != Tile.WALL
                && isWall(level, top - 1, col + dc)) {
            top--;
        }
        while (bottom + 1 < level.rowCount()
                && level.tileAt(new Position(bottom + 1, col)) != Tile.WALL
                && isWall(level, bottom + 1, col + dc)) {
            bottom++;
        }
        boolean topClosed = top == 0 || level.tileAt(new Position(top - 1, col)) == Tile.WALL;
        boolean bottomClosed = bottom == level.rowCount() - 1
                || level.tileAt(new Position(bottom + 1, col)) == Tile.WALL;
        if (!topClosed || !bottomClosed) {
            return false;
        }
        for (int r = top; r <= bottom; r++) {
            if (level.goals().contains(new Position(r, col))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isWall(Level level, int row, int col) {
        return level.tileAt(new Position(row, col)) == Tile.WALL;
    }
}
