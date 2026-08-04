package com.milandru.sokobani.engine;

import com.milandru.sokobani.core.Level;
import com.milandru.sokobani.core.Position;

import java.util.Objects;
import java.util.Optional;

public sealed interface GameEvent {

    record Moved(Position from, Position to) implements GameEvent {

        public Moved {
            Objects.requireNonNull(from, "from");
            Objects.requireNonNull(to, "to");
        }
    }

    record Pushed(Position from, Position to, Position boxFrom, Position boxTo) implements GameEvent {

        public Pushed {
            Objects.requireNonNull(from, "from");
            Objects.requireNonNull(to, "to");
            Objects.requireNonNull(boxFrom, "boxFrom");
            Objects.requireNonNull(boxTo, "boxTo");
        }
    }

    record Undone(Position from, Position to, Optional<Position> boxFrom, Optional<Position> boxTo)
            implements GameEvent {

        public Undone {
            Objects.requireNonNull(from, "from");
            Objects.requireNonNull(to, "to");
            Objects.requireNonNull(boxFrom, "boxFrom");
            Objects.requireNonNull(boxTo, "boxTo");
            if (boxFrom.isPresent() != boxTo.isPresent()) {
                throw new IllegalArgumentException("an undone push needs both box positions, an undone move neither");
            }
        }

        public static Undone ofMove(Position from, Position to) {
            return new Undone(from, to, Optional.empty(), Optional.empty());
        }

        public static Undone ofPush(Position from, Position to, Position boxFrom, Position boxTo) {
            Objects.requireNonNull(boxFrom, "boxFrom");
            Objects.requireNonNull(boxTo, "boxTo");
            return new Undone(from, to, Optional.of(boxFrom), Optional.of(boxTo));
        }

        public boolean isPush() {
            return boxFrom.isPresent();
        }
    }

    record Restarted(Level level) implements GameEvent {

        public Restarted {
            Objects.requireNonNull(level, "level");
        }
    }

    record LevelLoaded(Level level) implements GameEvent {

        public LevelLoaded {
            Objects.requireNonNull(level, "level");
        }
    }

    record Solved(Level level, int moveCount, int pushCount) implements GameEvent {

        public Solved {
            Objects.requireNonNull(level, "level");
        }
    }
}
