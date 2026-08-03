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
}
