package com.milandru.sokobani.core;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class LevelFixture {

    private static final char WALL = '#';
    private static final char FLOOR = ' ';
    private static final char GOAL = '.';
    private static final char BOX = '$';
    private static final char BOX_ON_GOAL = '*';
    private static final char PLAYER = '@';
    private static final char PLAYER_ON_GOAL = '+';

    private LevelFixture() {
    }

    static Level level(String layout) {
        return level(layout, "fixture", 0);
    }

    static Level level(String layout, String name, int index) {
        List<String> lines = layout.lines().toList();
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("layout has no lines");
        }
        int columns = lines.stream().mapToInt(String::length).max().orElseThrow();
        Tile[][] terrain = new Tile[lines.size()][columns];
        Set<Position> boxes = new HashSet<>();
        Position player = null;
        for (int row = 0; row < lines.size(); row++) {
            String line = lines.get(row);
            for (int col = 0; col < columns; col++) {
                char symbol = col < line.length() ? line.charAt(col) : WALL;
                terrain[row][col] = tileFor(symbol);
                Position position = new Position(row, col);
                if (symbol == BOX || symbol == BOX_ON_GOAL) {
                    boxes.add(position);
                }
                if (symbol == PLAYER || symbol == PLAYER_ON_GOAL) {
                    if (player != null) {
                        throw new IllegalArgumentException("layout has more than one player");
                    }
                    player = position;
                }
            }
        }
        if (player == null) {
            throw new IllegalArgumentException("layout has no player");
        }
        return new Level(terrain, player, boxes, name, index);
    }

    private static Tile tileFor(char symbol) {
        return switch (symbol) {
            case WALL -> Tile.WALL;
            case GOAL, BOX_ON_GOAL, PLAYER_ON_GOAL -> Tile.GOAL;
            case FLOOR, BOX, PLAYER -> Tile.FLOOR;
            default -> throw new IllegalArgumentException("unknown layout character '" + symbol + "'");
        };
    }
}
