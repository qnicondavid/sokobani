package com.milandru.sokobani.level;

import com.milandru.sokobani.core.Direction;
import com.milandru.sokobani.core.GameState;
import com.milandru.sokobani.core.Level;
import com.milandru.sokobani.core.Position;
import com.milandru.sokobani.core.Tile;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class LevelParser {

    public static final char WALL = '#';
    public static final char FLOOR = ' ';
    public static final char GOAL = '.';
    public static final char BOX = '$';
    public static final char BOX_ON_GOAL = '*';
    public static final char PLAYER = '@';
    public static final char PLAYER_ON_GOAL = '+';

    private static final String LINE_BREAK = "\r\n|\r|\n";
    private static final int FIRST_LINE = 1;

    private record Layout(List<String> lines, int firstLineNumber) {
    }

    private LevelParser() {
    }

    public static Level parse(String text) throws InvalidLevelFormatException {
        return parse(text, "", 0);
    }

    public static Level parse(String text, String name, int index) throws InvalidLevelFormatException {
        return parse(text, name, index, FIRST_LINE);
    }

    static Level parse(String text, String name, int index, int firstLineNumber) throws InvalidLevelFormatException {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(name, "name");
        Layout layout = layoutOf(text, firstLineNumber);
        List<String> lines = layout.lines();
        int columns = lines.stream().mapToInt(String::length).max().orElseThrow();

        Tile[][] terrain = new Tile[lines.size()][columns];
        Set<Position> boxes = new HashSet<>();
        Position player = null;
        Position surplusPlayer = null;
        int players = 0;
        int goals = 0;

        for (int row = 0; row < lines.size(); row++) {
            String line = lines.get(row);
            for (int col = 0; col < columns; col++) {
                char symbol = col < line.length() ? line.charAt(col) : FLOOR;
                Tile tile = tileFor(symbol, layout.firstLineNumber() + row, col + 1);
                terrain[row][col] = tile;
                if (tile == Tile.GOAL) {
                    goals++;
                }
                Position position = new Position(row, col);
                if (symbol == BOX || symbol == BOX_ON_GOAL) {
                    boxes.add(position);
                }
                if (symbol == PLAYER || symbol == PLAYER_ON_GOAL) {
                    players++;
                    if (player == null) {
                        player = position;
                    } else if (surplusPlayer == null) {
                        surplusPlayer = position;
                    }
                }
            }
        }

        if (players != 1) {
            throw playerCountProblem(players, surplusPlayer, layout.firstLineNumber());
        }
        if (goals == 0) {
            throw new InvalidLevelFormatException(
                    "level has no goals; at least one is required",
                    layout.firstLineNumber(), InvalidLevelFormatException.NO_POSITION);
        }
        if (boxes.size() != goals) {
            throw new InvalidLevelFormatException(
                    "level has " + boxes.size() + " boxes and " + goals + " goals; the counts must match",
                    layout.firstLineNumber(), InvalidLevelFormatException.NO_POSITION);
        }
        requireEnclosed(terrain, player, layout.firstLineNumber());

        return new Level(terrain, player, boxes, name, index);
    }

    public static String toXsb(Level level) {
        Objects.requireNonNull(level, "level");
        return grid(level, level.initialPlayer(), level.initialBoxes());
    }

    public static String toXsb(GameState state) {
        Objects.requireNonNull(state, "state");
        return grid(state.level(), state.player(), state.boxes());
    }

    private static String grid(Level level, Position player, Set<Position> boxes) {
        StringBuilder text = new StringBuilder();
        for (int row = 0; row < level.rowCount(); row++) {
            for (int col = 0; col < level.columnCount(); col++) {
                text.append(symbolAt(level, new Position(row, col), player, boxes));
            }
            text.append('\n');
        }
        return text.toString();
    }

    private static char symbolAt(Level level, Position position, Position player, Set<Position> boxes) {
        Tile tile = level.tileAt(position);
        boolean onGoal = tile == Tile.GOAL;
        if (boxes.contains(position)) {
            return onGoal ? BOX_ON_GOAL : BOX;
        }
        if (player.equals(position)) {
            return onGoal ? PLAYER_ON_GOAL : PLAYER;
        }
        return switch (tile) {
            case WALL -> WALL;
            case GOAL -> GOAL;
            case FLOOR -> FLOOR;
        };
    }

    private static Layout layoutOf(String text, int firstLineNumber) throws InvalidLevelFormatException {
        List<String> lines = new ArrayList<>(Arrays.asList(text.split(LINE_BREAK, -1)));
        int skipped = 0;
        while (!lines.isEmpty() && lines.get(0).isBlank()) {
            lines.remove(0);
            skipped++;
        }
        while (!lines.isEmpty() && lines.get(lines.size() - 1).isBlank()) {
            lines.remove(lines.size() - 1);
        }
        if (lines.isEmpty()) {
            throw new InvalidLevelFormatException(
                    "level is empty", firstLineNumber, InvalidLevelFormatException.NO_POSITION);
        }
        return new Layout(lines, firstLineNumber + skipped);
    }

    private static Tile tileFor(char symbol, int line, int column) throws InvalidLevelFormatException {
        return switch (symbol) {
            case WALL -> Tile.WALL;
            case GOAL, BOX_ON_GOAL, PLAYER_ON_GOAL -> Tile.GOAL;
            case FLOOR, BOX, PLAYER -> Tile.FLOOR;
            default -> throw new InvalidLevelFormatException(
                    "illegal character " + describe(symbol), line, column);
        };
    }

    private static String describe(char symbol) {
        boolean printable = symbol > ' ' && symbol < 0x7F;
        return printable ? "'" + symbol + "'" : String.format("U+%04X", (int) symbol);
    }

    private static InvalidLevelFormatException playerCountProblem(int players, Position surplus, int firstLineNumber) {
        String problem = "level has " + players + " players; exactly one is required";
        return surplus == null
                ? new InvalidLevelFormatException(problem, firstLineNumber, InvalidLevelFormatException.NO_POSITION)
                : new InvalidLevelFormatException(problem, firstLineNumber + surplus.row(), surplus.col() + 1);
    }

    private static void requireEnclosed(Tile[][] terrain, Position player, int firstLineNumber)
            throws InvalidLevelFormatException {
        boolean[][] reached = reachableFrom(terrain, player);
        Optional<Position> escape = firstBoundarySquareIn(reached);
        if (escape.isPresent()) {
            throw new InvalidLevelFormatException(
                    "level is not enclosed; the player can reach the edge of the grid here",
                    firstLineNumber + escape.get().row(),
                    escape.get().col() + 1);
        }
    }

    private static boolean[][] reachableFrom(Tile[][] terrain, Position player) {
        boolean[][] reached = new boolean[terrain.length][terrain[0].length];
        Deque<Position> pending = new ArrayDeque<>();
        reached[player.row()][player.col()] = true;
        pending.add(player);
        while (!pending.isEmpty()) {
            Position at = pending.remove();
            for (Direction direction : Direction.values()) {
                Position next = at.moved(direction);
                if (isInside(next, terrain) && !reached[next.row()][next.col()]
                        && terrain[next.row()][next.col()].isWalkable()) {
                    reached[next.row()][next.col()] = true;
                    pending.add(next);
                }
            }
        }
        return reached;
    }

    private static Optional<Position> firstBoundarySquareIn(boolean[][] reached) {
        for (int row = 0; row < reached.length; row++) {
            for (int col = 0; col < reached[row].length; col++) {
                boolean onBoundary = row == 0 || row == reached.length - 1
                        || col == 0 || col == reached[row].length - 1;
                if (reached[row][col] && onBoundary) {
                    return Optional.of(new Position(row, col));
                }
            }
        }
        return Optional.empty();
    }

    private static boolean isInside(Position position, Tile[][] terrain) {
        return position.row() >= 0 && position.row() < terrain.length
                && position.col() >= 0 && position.col() < terrain[position.row()].length;
    }
}
