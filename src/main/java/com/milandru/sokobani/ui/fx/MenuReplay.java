package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.core.Direction;
import com.milandru.sokobani.core.GameState;
import com.milandru.sokobani.core.SokobanRules;

public final class MenuReplay {

    private MenuReplay() {
    }

    public static void advance(GameState state, String moves, int from, int count) {
        for (int i = from; i < moves.length() && i < from + count; i++) {
            SokobanRules.apply(state, directionOf(moves.charAt(i)));
        }
    }

    public static Direction directionOf(char move) {
        return switch (move) {
            case 'U' -> Direction.UP;
            case 'D' -> Direction.DOWN;
            case 'L' -> Direction.LEFT;
            case 'R' -> Direction.RIGHT;
            default -> throw new IllegalArgumentException("unknown move symbol '" + move + "'");
        };
    }
}
