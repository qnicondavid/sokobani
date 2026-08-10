package com.milandru.sokobani.level;

import com.milandru.sokobani.core.Direction;
import com.milandru.sokobani.core.GameState;
import com.milandru.sokobani.core.Level;
import com.milandru.sokobani.core.MoveResult;
import com.milandru.sokobani.core.SokobanRules;

final class Replay {

    private Replay() {
    }

    static Direction directionOf(char symbol) {
        return switch (symbol) {
            case 'U' -> Direction.UP;
            case 'D' -> Direction.DOWN;
            case 'L' -> Direction.LEFT;
            case 'R' -> Direction.RIGHT;
            default -> throw new IllegalArgumentException("not a move symbol: '" + symbol + "'");
        };
    }

    static GameState replay(Level level, String moves) {
        GameState state = new GameState(level);
        for (int step = 0; step < moves.length(); step++) {
            char symbol = moves.charAt(step);
            MoveResult result = SokobanRules.apply(state, directionOf(symbol));
            if (result instanceof MoveResult.Blocked blocked) {
                throw new IllegalStateException(
                        "move " + (step + 1) + " '" + symbol + "' was blocked by " + blocked.reason());
            }
        }
        return state;
    }
}
