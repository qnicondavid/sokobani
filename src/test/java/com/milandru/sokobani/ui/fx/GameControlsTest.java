package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.core.Direction;
import com.milandru.sokobani.core.Level;
import com.milandru.sokobani.core.Position;
import com.milandru.sokobani.engine.GameEvent;

import javafx.scene.input.KeyCode;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameControlsTest {

    private static final Level LEVEL = BoardFixture.level(BoardFixture.ONE_PUSH);
    private static final Position FROM = new Position(1, 1);
    private static final Position TO = new Position(1, 2);
    private static final Position BEYOND = new Position(1, 3);

    @Test
    void commandFor_whilePlaying_movesOnBothWasdAndTheArrows() {
        assertEquals(GameControls.Command.MOVE_UP, playing(KeyCode.W));
        assertEquals(GameControls.Command.MOVE_UP, playing(KeyCode.UP));
        assertEquals(GameControls.Command.MOVE_DOWN, playing(KeyCode.S));
        assertEquals(GameControls.Command.MOVE_DOWN, playing(KeyCode.DOWN));
        assertEquals(GameControls.Command.MOVE_LEFT, playing(KeyCode.A));
        assertEquals(GameControls.Command.MOVE_LEFT, playing(KeyCode.LEFT));
        assertEquals(GameControls.Command.MOVE_RIGHT, playing(KeyCode.D));
        assertEquals(GameControls.Command.MOVE_RIGHT, playing(KeyCode.RIGHT));
    }

    @Test
    void commandFor_whilePlaying_undoesOnUAndBackspace() {
        assertEquals(GameControls.Command.UNDO, playing(KeyCode.U));
        assertEquals(GameControls.Command.UNDO, playing(KeyCode.BACK_SPACE));
    }

    @Test
    void commandFor_zWithoutTheShortcutModifier_doesNothing() {
        assertEquals(GameControls.Command.IGNORED, playing(KeyCode.Z));
        assertEquals(GameControls.Command.UNDO,
                GameControls.commandFor(GameControls.Mode.PLAYING, KeyCode.Z, true));
    }

    @Test
    void commandFor_whilePlaying_restartsPausesAndCyclesTheTheme() {
        assertEquals(GameControls.Command.RESTART, playing(KeyCode.R));
        assertEquals(GameControls.Command.PAUSE, playing(KeyCode.ESCAPE));
        assertEquals(GameControls.Command.CYCLE_THEME, playing(KeyCode.T));
    }

    @Test
    void commandFor_whilePaused_answersOnlyEscapeRestartAndTheme() {
        assertEquals(GameControls.Command.RESUME, paused(KeyCode.ESCAPE));
        assertEquals(GameControls.Command.RESTART, paused(KeyCode.R));
        assertEquals(GameControls.Command.CYCLE_THEME, paused(KeyCode.T));
        assertEquals(GameControls.Command.IGNORED, paused(KeyCode.W));
        assertEquals(GameControls.Command.IGNORED, paused(KeyCode.UP));
        assertEquals(GameControls.Command.IGNORED, paused(KeyCode.U));
        assertEquals(GameControls.Command.IGNORED, paused(KeyCode.N));
    }

    @Test
    void commandFor_whileSolved_answersOnlyNextRoomRestartAndTheme() {
        assertEquals(GameControls.Command.NEXT_ROOM, solved(KeyCode.N));
        assertEquals(GameControls.Command.RESTART, solved(KeyCode.R));
        assertEquals(GameControls.Command.CYCLE_THEME, solved(KeyCode.T));
        assertEquals(GameControls.Command.IGNORED, solved(KeyCode.W));
        assertEquals(GameControls.Command.IGNORED, solved(KeyCode.U));
        assertEquals(GameControls.Command.IGNORED, solved(KeyCode.ESCAPE));
    }

    @Test
    void commandFor_nextRoomAndPause_belongToOneModeEach() {
        assertEquals(GameControls.Command.IGNORED, playing(KeyCode.N));
        assertEquals(GameControls.Command.PAUSE, playing(KeyCode.ESCAPE));
        assertEquals(GameControls.Command.RESUME, paused(KeyCode.ESCAPE));
        assertEquals(GameControls.Command.IGNORED, solved(KeyCode.ESCAPE));
    }

    @Test
    void commandFor_everyKeyOutsideAModesOwnBindings_isIgnoredWithOrWithoutTheShortcut() {
        Map<GameControls.Mode, Set<KeyCode>> bound = Map.of(
                GameControls.Mode.PLAYING,
                Set.of(KeyCode.W, KeyCode.UP, KeyCode.S, KeyCode.DOWN, KeyCode.A, KeyCode.LEFT,
                        KeyCode.D, KeyCode.RIGHT, KeyCode.U, KeyCode.BACK_SPACE, KeyCode.Z,
                        KeyCode.R, KeyCode.ESCAPE, KeyCode.T),
                GameControls.Mode.PAUSED,
                Set.of(KeyCode.ESCAPE, KeyCode.R, KeyCode.T),
                GameControls.Mode.SOLVED,
                Set.of(KeyCode.N, KeyCode.R, KeyCode.T));

        for (GameControls.Mode mode : GameControls.Mode.values()) {
            for (KeyCode code : KeyCode.values()) {
                if (bound.get(mode).contains(code)) {
                    continue;
                }
                for (boolean shortcutDown : new boolean[] {false, true}) {
                    assertEquals(GameControls.Command.IGNORED,
                            GameControls.commandFor(mode, code, shortcutDown),
                            mode + " + " + code + " shortcut=" + shortcutDown);
                }
            }
        }
    }

    @Test
    void commandFor_everyBoundKey_answersSomething() {
        for (KeyCode code : Set.of(KeyCode.W, KeyCode.UP, KeyCode.S, KeyCode.DOWN, KeyCode.A,
                KeyCode.LEFT, KeyCode.D, KeyCode.RIGHT, KeyCode.U, KeyCode.BACK_SPACE,
                KeyCode.R, KeyCode.ESCAPE, KeyCode.T)) {
            assertNotEquals(GameControls.Command.IGNORED, playing(code), code.toString());
        }
        for (KeyCode code : Set.of(KeyCode.ESCAPE, KeyCode.R, KeyCode.T)) {
            assertNotEquals(GameControls.Command.IGNORED, paused(code), code.toString());
        }
        for (KeyCode code : Set.of(KeyCode.N, KeyCode.R, KeyCode.T)) {
            assertNotEquals(GameControls.Command.IGNORED, solved(code), code.toString());
        }
    }

    @Test
    void directionOf_answersOnlyForTheFourMoveCommands() {
        assertEquals(Optional.of(Direction.UP), GameControls.directionOf(GameControls.Command.MOVE_UP));
        assertEquals(Optional.of(Direction.DOWN), GameControls.directionOf(GameControls.Command.MOVE_DOWN));
        assertEquals(Optional.of(Direction.LEFT), GameControls.directionOf(GameControls.Command.MOVE_LEFT));
        assertEquals(Optional.of(Direction.RIGHT), GameControls.directionOf(GameControls.Command.MOVE_RIGHT));
        assertEquals(Optional.empty(), GameControls.directionOf(GameControls.Command.UNDO));
        assertEquals(Optional.empty(), GameControls.directionOf(GameControls.Command.RESTART));
        assertEquals(Optional.empty(), GameControls.directionOf(GameControls.Command.IGNORED));
    }

    @Test
    void beginsAnAttempt_isTrueOnlyForRestartAndLevelLoaded() {
        assertTrue(GameControls.beginsAnAttempt(new GameEvent.Restarted(LEVEL)));
        assertTrue(GameControls.beginsAnAttempt(new GameEvent.LevelLoaded(LEVEL)));
        assertFalse(GameControls.beginsAnAttempt(new GameEvent.Moved(FROM, TO)));
        assertFalse(GameControls.beginsAnAttempt(new GameEvent.Pushed(FROM, TO, TO, BEYOND)));
        assertFalse(GameControls.beginsAnAttempt(GameEvent.Undone.ofMove(TO, FROM)));
        assertFalse(GameControls.beginsAnAttempt(new GameEvent.Solved(LEVEL, 3, 3)));
    }

    private static GameControls.Command playing(KeyCode code) {
        return GameControls.commandFor(GameControls.Mode.PLAYING, code, false);
    }

    private static GameControls.Command paused(KeyCode code) {
        return GameControls.commandFor(GameControls.Mode.PAUSED, code, false);
    }

    private static GameControls.Command solved(KeyCode code) {
        return GameControls.commandFor(GameControls.Mode.SOLVED, code, false);
    }
}
