package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.core.Level;
import com.milandru.sokobani.core.Position;
import com.milandru.sokobani.core.Tile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class Anomaly {

    private static final int SCRAMBLE = 0x9E3779B9;
    private static final int MULTIPLIER = 0x85EBCA6B;
    private static final int MIX_SHIFT = 15;

    private Anomaly() {
    }

    public static Optional<Position> wallIn(Level level) {
        Objects.requireNonNull(level, "level");
        List<Position> candidates = wallsClearOfThePlayerStart(level);
        if (candidates.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(candidates.get(Math.floorMod(scramble(level.index()), candidates.size())));
    }

    private static List<Position> wallsClearOfThePlayerStart(Level level) {
        List<Position> walls = new ArrayList<>();
        Position start = level.initialPlayer();
        for (int row = 0; row < level.rowCount(); row++) {
            for (int col = 0; col < level.columnCount(); col++) {
                Position at = new Position(row, col);
                if (level.tileAt(at) == Tile.WALL && !touches(at, start)) {
                    walls.add(at);
                }
            }
        }
        return walls;
    }

    private static boolean touches(Position at, Position other) {
        return Math.abs(at.row() - other.row()) <= 1 && Math.abs(at.col() - other.col()) <= 1;
    }

    private static int scramble(int index) {
        int mixed = (index ^ SCRAMBLE) * MULTIPLIER;
        return mixed ^ (mixed >>> MIX_SHIFT);
    }
}
