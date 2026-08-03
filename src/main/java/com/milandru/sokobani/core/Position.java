package com.milandru.sokobani.core;

import java.util.Objects;

public record Position(int row, int col) {

    public Position moved(Direction direction) {
        Objects.requireNonNull(direction, "direction");
        return new Position(row + direction.deltaRow(), col + direction.deltaCol());
    }
}
