package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.core.Direction;
import com.milandru.sokobani.engine.GameEvent;

import javafx.scene.input.KeyCode;

import java.util.Optional;

public final class GameControls {

    public enum Mode {
        PLAYING,
        PAUSED,
        SOLVED
    }

    public enum Command {
        MOVE_UP,
        MOVE_DOWN,
        MOVE_LEFT,
        MOVE_RIGHT,
        UNDO,
        RESTART,
        PAUSE,
        RESUME,
        NEXT_ROOM,
        CYCLE_THEME,
        IGNORED
    }

    private GameControls() {
    }

    public static Command commandFor(Mode mode, KeyCode code, boolean shortcutDown) {
        return switch (mode) {
            case PLAYING -> whilePlaying(code, shortcutDown);
            case PAUSED -> whilePaused(code);
            case SOLVED -> whileSolved(code);
        };
    }

    public static Optional<Direction> directionOf(Command command) {
        return switch (command) {
            case MOVE_UP -> Optional.of(Direction.UP);
            case MOVE_DOWN -> Optional.of(Direction.DOWN);
            case MOVE_LEFT -> Optional.of(Direction.LEFT);
            case MOVE_RIGHT -> Optional.of(Direction.RIGHT);
            case UNDO, RESTART, PAUSE, RESUME, NEXT_ROOM, CYCLE_THEME, IGNORED -> Optional.empty();
        };
    }

    public static boolean beginsAnAttempt(GameEvent event) {
        return switch (event) {
            case GameEvent.Restarted restarted -> true;
            case GameEvent.LevelLoaded loaded -> true;
            case GameEvent.Moved moved -> false;
            case GameEvent.Pushed pushed -> false;
            case GameEvent.Undone undone -> false;
            case GameEvent.Solved won -> false;
        };
    }

    private static Command whilePlaying(KeyCode code, boolean shortcutDown) {
        return switch (code) {
            case W, UP -> Command.MOVE_UP;
            case S, DOWN -> Command.MOVE_DOWN;
            case A, LEFT -> Command.MOVE_LEFT;
            case D, RIGHT -> Command.MOVE_RIGHT;
            case U, BACK_SPACE -> Command.UNDO;
            case Z -> shortcutDown ? Command.UNDO : Command.IGNORED;
            case R -> Command.RESTART;
            case ESCAPE -> Command.PAUSE;
            case T -> Command.CYCLE_THEME;
            default -> Command.IGNORED;
        };
    }

    private static Command whilePaused(KeyCode code) {
        return switch (code) {
            case ESCAPE -> Command.RESUME;
            case R -> Command.RESTART;
            case T -> Command.CYCLE_THEME;
            default -> Command.IGNORED;
        };
    }

    private static Command whileSolved(KeyCode code) {
        return switch (code) {
            case N -> Command.NEXT_ROOM;
            case R -> Command.RESTART;
            case T -> Command.CYCLE_THEME;
            default -> Command.IGNORED;
        };
    }
}
