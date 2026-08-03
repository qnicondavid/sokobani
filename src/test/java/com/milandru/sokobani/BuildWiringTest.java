package com.milandru.sokobani;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuildWiringTest {

    record Coordinate(int row, int col) {
    }

    @Test
    void surefire_whenTheSuiteRuns_executesJupiterTestsAgainstJava21() {
        Object value = new Coordinate(2, 3);
        String described = switch (value) {
            case Coordinate(int row, int col) -> row + "," + col;
            default -> "none";
        };
        assertEquals("2,3", described);
    }
}
