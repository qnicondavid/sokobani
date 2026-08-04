package com.milandru.sokobani.engine;

import com.milandru.sokobani.core.Direction;
import com.milandru.sokobani.core.Position;

import java.util.Objects;
import java.util.Optional;

public record MoveRecord(Direction direction, Optional<Position> pushedBox) {

    public MoveRecord {
        Objects.requireNonNull(direction, "direction");
        Objects.requireNonNull(pushedBox, "pushedBox");
    }

    public static MoveRecord ofMove(Direction direction) {
        return new MoveRecord(direction, Optional.empty());
    }

    public static MoveRecord ofPush(Direction direction, Position pushedBox) {
        Objects.requireNonNull(pushedBox, "pushedBox");
        return new MoveRecord(direction, Optional.of(pushedBox));
    }

    public boolean isPush() {
        return pushedBox.isPresent();
    }
}
