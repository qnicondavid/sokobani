package com.milandru.sokobani.level;

import com.milandru.sokobani.core.Direction;
import com.milandru.sokobani.core.GameState;
import com.milandru.sokobani.core.Level;
import com.milandru.sokobani.core.MoveResult;
import com.milandru.sokobani.core.Position;
import com.milandru.sokobani.core.SokobanRules;
import com.milandru.sokobani.core.Tile;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

final class Solver {

    private static final int STATE_LIMIT = 400_000;

    private static final Comparator<Position> READING_ORDER =
            Comparator.comparingInt(Position::row).thenComparingInt(Position::col);

    record Solution(String moves, int pushes) {
    }

    private record Key(Position player, Set<Position> boxes) {
    }

    private static final class Node {

        private final Position player;
        private final Set<Position> boxes;
        private final Node parent;
        private final Position pushFrom;
        private final Direction push;

        private Node(Position player, Set<Position> boxes, Node parent, Position pushFrom, Direction push) {
            this.player = player;
            this.boxes = boxes;
            this.parent = parent;
            this.pushFrom = pushFrom;
            this.push = push;
        }
    }

    private Solver() {
    }

    static Optional<Solution> solve(Level level) {
        Tile[][] terrain = terrainOf(level);
        Set<Position> dead = deadSquares(level);
        Node start = new Node(level.initialPlayer(), Set.copyOf(level.initialBoxes()), null, null, null);
        if (start.boxes.equals(level.goals())) {
            return Optional.of(new Solution("", 0));
        }

        Deque<Node> frontier = new ArrayDeque<>();
        Set<Key> seen = new HashSet<>();
        frontier.add(start);
        seen.add(keyOf(level, start));
        int expanded = 0;

        while (!frontier.isEmpty()) {
            Node node = frontier.remove();
            expanded++;
            if (expanded > STATE_LIMIT) {
                throw new IllegalStateException(
                        "solver gave up on " + level.name() + " after " + STATE_LIMIT + " states");
            }
            Set<Position> standing = reachable(level, node.boxes, node.player);
            for (Position box : node.boxes) {
                for (Direction direction : Direction.values()) {
                    Position from = box.moved(direction.opposite());
                    if (!standing.contains(from) || dead.contains(box.moved(direction))) {
                        continue;
                    }
                    Optional<GameState> pushed = push(level, terrain, node.boxes, from, direction);
                    if (pushed.isEmpty()) {
                        continue;
                    }
                    GameState state = pushed.get();
                    Node next = new Node(state.player(), Set.copyOf(state.boxes()), node, from, direction);
                    if (next.boxes.equals(level.goals())) {
                        return Optional.of(solutionOf(level, next));
                    }
                    if (seen.add(keyOf(level, next))) {
                        frontier.add(next);
                    }
                }
            }
        }
        return Optional.empty();
    }

    private static Optional<GameState> push(
            Level level, Tile[][] terrain, Set<Position> boxes, Position from, Direction direction) {
        GameState state = new GameState(new Level(terrain, from, boxes, level.name(), level.index()));
        return SokobanRules.apply(state, direction) instanceof MoveResult.Pushed
                ? Optional.of(state)
                : Optional.empty();
    }

    private static Solution solutionOf(Level level, Node solved) {
        List<Node> chain = new ArrayList<>();
        for (Node node = solved; node.parent != null; node = node.parent) {
            chain.add(node);
        }
        Collections.reverse(chain);
        StringBuilder moves = new StringBuilder();
        for (Node node : chain) {
            moves.append(walk(level, node.parent.boxes, node.parent.player, node.pushFrom));
            moves.append(Moves.symbolOf(node.push));
        }
        return new Solution(moves.toString(), chain.size());
    }

    private static String walk(Level level, Set<Position> boxes, Position from, Position to) {
        Map<Position, Position> cameFrom = new HashMap<>();
        Map<Position, Direction> arrivedBy = new HashMap<>();
        Deque<Position> pending = new ArrayDeque<>();
        cameFrom.put(from, from);
        pending.add(from);
        while (!pending.isEmpty() && !cameFrom.containsKey(to)) {
            Position at = pending.remove();
            for (Direction direction : Direction.values()) {
                Position next = at.moved(direction);
                if (isFree(level, boxes, next) && !cameFrom.containsKey(next)) {
                    cameFrom.put(next, at);
                    arrivedBy.put(next, direction);
                    pending.add(next);
                }
            }
        }
        if (!cameFrom.containsKey(to)) {
            throw new IllegalStateException("no walk from " + from + " to " + to + " in " + level.name());
        }
        StringBuilder reversed = new StringBuilder();
        for (Position at = to; !at.equals(from); at = cameFrom.get(at)) {
            reversed.append(Moves.symbolOf(arrivedBy.get(at)));
        }
        return reversed.reverse().toString();
    }

    private static Key keyOf(Level level, Node node) {
        Position anchor = reachable(level, node.boxes, node.player).stream().min(READING_ORDER).orElseThrow();
        return new Key(anchor, node.boxes);
    }

    private static Set<Position> reachable(Level level, Set<Position> boxes, Position from) {
        Set<Position> found = new HashSet<>();
        Deque<Position> pending = new ArrayDeque<>();
        found.add(from);
        pending.add(from);
        while (!pending.isEmpty()) {
            Position at = pending.remove();
            for (Direction direction : Direction.values()) {
                Position next = at.moved(direction);
                if (isFree(level, boxes, next) && found.add(next)) {
                    pending.add(next);
                }
            }
        }
        return found;
    }

    private static Set<Position> deadSquares(Level level) {
        Set<Position> live = new HashSet<>(level.goals());
        Deque<Position> pending = new ArrayDeque<>(live);
        while (!pending.isEmpty()) {
            Position at = pending.remove();
            for (Direction direction : Direction.values()) {
                Position pulledTo = at.moved(direction);
                Position puller = pulledTo.moved(direction);
                if (level.tileAt(pulledTo).isWalkable() && level.tileAt(puller).isWalkable() && live.add(pulledTo)) {
                    pending.add(pulledTo);
                }
            }
        }
        Set<Position> dead = new HashSet<>();
        for (int row = 0; row < level.rowCount(); row++) {
            for (int col = 0; col < level.columnCount(); col++) {
                Position position = new Position(row, col);
                if (level.tileAt(position).isWalkable() && !live.contains(position)) {
                    dead.add(position);
                }
            }
        }
        return dead;
    }

    private static boolean isFree(Level level, Set<Position> boxes, Position position) {
        return level.tileAt(position).isWalkable() && !boxes.contains(position);
    }

    private static Tile[][] terrainOf(Level level) {
        Tile[][] terrain = new Tile[level.rowCount()][level.columnCount()];
        for (int row = 0; row < level.rowCount(); row++) {
            for (int col = 0; col < level.columnCount(); col++) {
                terrain[row][col] = level.tileAt(new Position(row, col));
            }
        }
        return terrain;
    }
}
