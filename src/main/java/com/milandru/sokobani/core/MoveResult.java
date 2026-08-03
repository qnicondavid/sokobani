package com.milandru.sokobani.core;

import java.util.Objects;

public sealed interface MoveResult {

    enum BlockedReason {
        WALL,
        BOX_AGAINST_WALL,
        BOX_AGAINST_BOX
    }

    record Moved(Position from, Position to) implements MoveResult {

        public Moved {
            Objects.requireNonNull(from, "from");
            Objects.requireNonNull(to, "to");
        }
    }

    record Pushed(Position from, Position to, Position boxFrom, Position boxTo) implements MoveResult {

        public Pushed {
            Objects.requireNonNull(from, "from");
            Objects.requireNonNull(to, "to");
            Objects.requireNonNull(boxFrom, "boxFrom");
            Objects.requireNonNull(boxTo, "boxTo");
        }
    }

    record Blocked(BlockedReason reason) implements MoveResult {

        public Blocked {
            Objects.requireNonNull(reason, "reason");
        }
    }
}
