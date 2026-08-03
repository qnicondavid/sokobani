package com.milandru.sokobani.core;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class Level {

    private final Tile[][] terrain;
    private final Position initialPlayer;
    private final Set<Position> initialBoxes;
    private final Set<Position> goals;
    private final String name;
    private final int index;

    public Level(Tile[][] terrain, Position initialPlayer, Set<Position> initialBoxes, String name, int index) {
        this.terrain = rectangularCopyOf(terrain);
        this.initialPlayer = Objects.requireNonNull(initialPlayer, "initialPlayer");
        this.initialBoxes = Set.copyOf(Objects.requireNonNull(initialBoxes, "initialBoxes"));
        this.goals = goalsIn(this.terrain);
        this.name = Objects.requireNonNull(name, "name");
        this.index = index;
        requireStandableSquare(this.initialPlayer, "player");
        for (Position box : this.initialBoxes) {
            requireStandableSquare(box, "box");
        }
        if (this.initialBoxes.contains(this.initialPlayer)) {
            throw new IllegalArgumentException("player and a box both start at " + this.initialPlayer);
        }
    }

    public Tile tileAt(Position position) {
        Objects.requireNonNull(position, "position");
        boolean outsideGrid = position.row() < 0
                || position.row() >= terrain.length
                || position.col() < 0
                || position.col() >= terrain[position.row()].length;
        return outsideGrid ? Tile.WALL : terrain[position.row()][position.col()];
    }

    public int rowCount() {
        return terrain.length;
    }

    public int columnCount() {
        return terrain[0].length;
    }

    public Position initialPlayer() {
        return initialPlayer;
    }

    public Set<Position> initialBoxes() {
        return initialBoxes;
    }

    public Set<Position> goals() {
        return goals;
    }

    public String name() {
        return name;
    }

    public int index() {
        return index;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Level level
                && index == level.index
                && name.equals(level.name)
                && initialPlayer.equals(level.initialPlayer)
                && initialBoxes.equals(level.initialBoxes)
                && Arrays.deepEquals(terrain, level.terrain);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, index, initialPlayer, initialBoxes, Arrays.deepHashCode(terrain));
    }

    @Override
    public String toString() {
        return "Level[index=" + index + ", name=" + name + ", rows=" + rowCount() + ", columns=" + columnCount() + "]";
    }

    private void requireStandableSquare(Position position, String occupant) {
        if (!tileAt(position).isWalkable()) {
            throw new IllegalArgumentException(occupant + " starts at " + position + ", which is not a walkable square");
        }
    }

    private static Tile[][] rectangularCopyOf(Tile[][] source) {
        Objects.requireNonNull(source, "terrain");
        if (source.length == 0) {
            throw new IllegalArgumentException("terrain has no rows");
        }
        int columns = Objects.requireNonNull(source[0], "terrain row 0").length;
        if (columns == 0) {
            throw new IllegalArgumentException("terrain has no columns");
        }
        Tile[][] copy = new Tile[source.length][];
        for (int row = 0; row < source.length; row++) {
            Tile[] sourceRow = Objects.requireNonNull(source[row], "terrain row " + row);
            if (sourceRow.length != columns) {
                throw new IllegalArgumentException(
                        "terrain row " + row + " has " + sourceRow.length + " columns, expected " + columns);
            }
            copy[row] = sourceRow.clone();
            for (int col = 0; col < columns; col++) {
                Objects.requireNonNull(copy[row][col], "terrain tile at row " + row + " col " + col);
            }
        }
        return copy;
    }

    private static Set<Position> goalsIn(Tile[][] terrain) {
        Set<Position> found = new HashSet<>();
        for (int row = 0; row < terrain.length; row++) {
            for (int col = 0; col < terrain[row].length; col++) {
                if (terrain[row][col] == Tile.GOAL) {
                    found.add(new Position(row, col));
                }
            }
        }
        return Set.copyOf(found);
    }
}
