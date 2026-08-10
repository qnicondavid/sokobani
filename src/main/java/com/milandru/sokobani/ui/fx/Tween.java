package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.core.Position;
import com.milandru.sokobani.engine.GameEvent;

import java.util.Objects;
import java.util.Optional;

final class Tween {

    private final Position playerFrom;
    private final Position playerTo;
    private final Optional<Position> boxFrom;
    private final Optional<Position> boxTo;
    private final long startNanos;
    private final long durationNanos;

    private Tween(Position playerFrom, Position playerTo, Optional<Position> boxFrom, Optional<Position> boxTo,
                  long startNanos, long durationNanos) {
        this.playerFrom = Objects.requireNonNull(playerFrom, "playerFrom");
        this.playerTo = Objects.requireNonNull(playerTo, "playerTo");
        this.boxFrom = Objects.requireNonNull(boxFrom, "boxFrom");
        this.boxTo = Objects.requireNonNull(boxTo, "boxTo");
        if (boxFrom.isPresent() != boxTo.isPresent()) {
            throw new IllegalArgumentException("a push needs both box positions, a move neither");
        }
        if (durationNanos < 0) {
            throw new IllegalArgumentException("duration must not be negative, got " + durationNanos);
        }
        this.startNanos = startNanos;
        this.durationNanos = durationNanos;
    }

    static Tween ofMove(Position from, Position to, long startNanos, long durationNanos) {
        return new Tween(from, to, Optional.empty(), Optional.empty(), startNanos, durationNanos);
    }

    static Tween ofPush(Position from, Position to, Position boxFrom, Position boxTo,
                        long startNanos, long durationNanos) {
        return new Tween(from, to, Optional.of(boxFrom), Optional.of(boxTo), startNanos, durationNanos);
    }

    static Optional<Tween> of(GameEvent event, long startNanos, long durationNanos) {
        Objects.requireNonNull(event, "event");
        if (event instanceof GameEvent.Moved moved) {
            return Optional.of(ofMove(moved.from(), moved.to(), startNanos, durationNanos));
        }
        if (event instanceof GameEvent.Pushed pushed) {
            return Optional.of(
                    ofPush(pushed.from(), pushed.to(), pushed.boxFrom(), pushed.boxTo(), startNanos, durationNanos));
        }
        if (event instanceof GameEvent.Undone undone) {
            return Optional.of(undone.isPush()
                    ? ofPush(undone.from(), undone.to(), undone.boxFrom().orElseThrow(),
                            undone.boxTo().orElseThrow(), startNanos, durationNanos)
                    : ofMove(undone.from(), undone.to(), startNanos, durationNanos));
        }
        return Optional.empty();
    }

    Position playerFrom() {
        return playerFrom;
    }

    Position playerTo() {
        return playerTo;
    }

    Optional<Position> boxFrom() {
        return boxFrom;
    }

    Optional<Position> boxTo() {
        return boxTo;
    }

    double fraction(long nowNanos) {
        if (durationNanos == 0) {
            return 1.0;
        }
        double fraction = (nowNanos - startNanos) / (double) durationNanos;
        return Math.min(1.0, Math.max(0.0, fraction));
    }

    boolean finished(long nowNanos) {
        return fraction(nowNanos) >= 1.0;
    }
}
