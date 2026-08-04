package com.milandru.sokobani.core;

import java.util.Objects;

public final class SokobanRules {

    private SokobanRules() {
    }

    public static MoveResult apply(GameState state, Direction direction) {
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(direction, "direction");

        Position from = state.player();
        Position target = from.moved(direction);

        if (!state.level().tileAt(target).isWalkable()) {
            return new MoveResult.Blocked(MoveResult.BlockedReason.WALL);
        }

        if (!state.hasBoxAt(target)) {
            state.applyMove(target);
            return new MoveResult.Moved(from, target);
        }

        Position beyond = target.moved(direction);

        if (!state.level().tileAt(beyond).isWalkable()) {
            return new MoveResult.Blocked(MoveResult.BlockedReason.BOX_AGAINST_WALL);
        }

        if (state.hasBoxAt(beyond)) {
            return new MoveResult.Blocked(MoveResult.BlockedReason.BOX_AGAINST_BOX);
        }

        state.applyPush(target, beyond);
        return new MoveResult.Pushed(from, target, target, beyond);
    }

    public static void revertMove(GameState state, Direction direction) {
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(direction, "direction");

        Position origin = state.player().moved(direction.opposite());
        requireMoveToRevert(state);
        requireOpenSquare(state, origin);

        state.revertMove(origin);
    }

    public static void revertPush(GameState state, Direction direction) {
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(direction, "direction");

        Position boxOrigin = state.player();
        Position boxDestination = boxOrigin.moved(direction);
        Position origin = boxOrigin.moved(direction.opposite());
        requireMoveToRevert(state);
        if (state.pushCount() == 0) {
            throw new IllegalStateException("there is no push to revert");
        }
        if (!state.hasBoxAt(boxDestination)) {
            throw new IllegalStateException("there is no box at " + boxDestination + " to pull back");
        }
        requireOpenSquare(state, origin);

        state.revertPush(origin, boxOrigin, boxDestination);
    }

    private static void requireMoveToRevert(GameState state) {
        if (state.moveCount() == 0) {
            throw new IllegalStateException("there is no move to revert");
        }
    }

    private static void requireOpenSquare(GameState state, Position origin) {
        if (!state.level().tileAt(origin).isWalkable()) {
            throw new IllegalStateException("the player cannot return to " + origin + ", which is not a walkable square");
        }
        if (state.hasBoxAt(origin)) {
            throw new IllegalStateException("the player cannot return to " + origin + ", which holds a box");
        }
    }
}
