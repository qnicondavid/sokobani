package com.milandru.sokobani.core;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class GameState {

    private static final Comparator<Position> READING_ORDER =
            Comparator.comparingInt(Position::row).thenComparingInt(Position::col);

    private final Level level;
    private final Set<Position> boxes;
    private final Set<Position> boxesView;
    private Position player;
    private int moveCount;
    private int pushCount;

    public GameState(Level level) {
        this.level = Objects.requireNonNull(level, "level");
        this.boxes = new HashSet<>(level.initialBoxes());
        this.boxesView = Collections.unmodifiableSet(this.boxes);
        this.player = level.initialPlayer();
    }

    public Level level() {
        return level;
    }

    public Position player() {
        return player;
    }

    public Set<Position> boxes() {
        return boxesView;
    }

    public boolean hasBoxAt(Position position) {
        return boxes.contains(position);
    }

    public int moveCount() {
        return moveCount;
    }

    public int pushCount() {
        return pushCount;
    }

    public boolean isSolved() {
        return !level.goals().isEmpty() && boxes.containsAll(level.goals());
    }

    void applyMove(Position destination) {
        player = destination;
        moveCount++;
    }

    void applyPush(Position boxOrigin, Position boxDestination) {
        boxes.remove(boxOrigin);
        boxes.add(boxDestination);
        player = boxOrigin;
        moveCount++;
        pushCount++;
    }

    void revertMove(Position origin) {
        player = origin;
        moveCount--;
    }

    void revertPush(Position origin, Position boxOrigin, Position boxDestination) {
        boxes.remove(boxDestination);
        boxes.add(boxOrigin);
        player = origin;
        moveCount--;
        pushCount--;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof GameState state
                && moveCount == state.moveCount
                && pushCount == state.pushCount
                && player.equals(state.player)
                && boxes.equals(state.boxes)
                && level.equals(state.level);
    }

    @Override
    public int hashCode() {
        return Objects.hash(level, player, boxes, moveCount, pushCount);
    }

    @Override
    public String toString() {
        String describedBoxes = boxes.stream()
                .sorted(READING_ORDER)
                .map(Position::toString)
                .collect(Collectors.joining(", "));
        return "GameState[player=" + player
                + ", boxes=[" + describedBoxes
                + "], moves=" + moveCount
                + ", pushes=" + pushCount + "]";
    }
}
