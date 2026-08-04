package com.milandru.sokobani.level;

import java.util.Objects;

public class InvalidLevelFormatException extends Exception {

    public static final int NO_POSITION = 0;

    private final int line;
    private final int column;

    public InvalidLevelFormatException(String problem) {
        this(problem, NO_POSITION, NO_POSITION);
    }

    public InvalidLevelFormatException(String problem, int line, int column) {
        super(located(problem, line, column));
        this.line = line;
        this.column = column;
    }

    public int line() {
        return line;
    }

    public int column() {
        return column;
    }

    public boolean hasPosition() {
        return line != NO_POSITION;
    }

    private static String located(String problem, int line, int column) {
        Objects.requireNonNull(problem, "problem");
        if (line == NO_POSITION) {
            return problem;
        }
        if (column == NO_POSITION) {
            return "line " + line + ": " + problem;
        }
        return "line " + line + ", column " + column + ": " + problem;
    }
}
