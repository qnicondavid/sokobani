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
    void commandFor_whilePaused_navigatesAndConfirms() {
        assertEquals(GameControls.Command.MOVE_UP, paused(KeyCode.W));
        assertEquals(GameControls.Command.MOVE_UP, paused(KeyCode.UP));
        assertEquals(GameControls.Command.MOVE_DOWN, paused(KeyCode.S));
        assertEquals(GameControls.Command.MOVE_DOWN, paused(KeyCode.DOWN));
        assertEquals(GameControls.Command.CONFIRM, paused(KeyCode.ENTER));
        assertEquals(GameControls.Command.RESUME, paused(KeyCode.ESCAPE));
        assertEquals(GameControls.Command.RESTART, paused(KeyCode.R));
        assertEquals(GameControls.Command.CYCLE_THEME, paused(KeyCode.T));
        assertEquals(GameControls.Command.IGNORED, paused(KeyCode.U));
        assertEquals(GameControls.Command.IGNORED, paused(KeyCode.N));
    }

    @Test
    void commandFor_whileSolved_answersNextReplayBackAndTheme() {
        assertEquals(GameControls.Command.NEXT_ROOM, solved(KeyCode.N));
        assertEquals(GameControls.Command.RESTART, solved(KeyCode.R));
        assertEquals(GameControls.Command.CONFIRM, solved(KeyCode.ENTER));
        assertEquals(GameControls.Command.BACK, solved(KeyCode.ESCAPE));
        assertEquals(GameControls.Command.CYCLE_THEME, solved(KeyCode.T));
        assertEquals(GameControls.Command.IGNORED, solved(KeyCode.W));
        assertEquals(GameControls.Command.IGNORED, solved(KeyCode.U));
    }

    @Test
    void commandFor_whileOnTheMenu_navigatesAndConfirms() {
        assertEquals(GameControls.Command.MOVE_UP, menu(KeyCode.W));
        assertEquals(GameControls.Command.MOVE_UP, menu(KeyCode.UP));
        assertEquals(GameControls.Command.MOVE_DOWN, menu(KeyCode.S));
        assertEquals(GameControls.Command.MOVE_DOWN, menu(KeyCode.DOWN));
        assertEquals(GameControls.Command.CONFIRM, menu(KeyCode.ENTER));
        assertEquals(GameControls.Command.CYCLE_THEME, menu(KeyCode.T));
        assertEquals(GameControls.Command.IGNORED, menu(KeyCode.ESCAPE));
        assertEquals(GameControls.Command.IGNORED, menu(KeyCode.A));
        assertEquals(GameControls.Command.IGNORED, menu(KeyCode.R));
    }

    @Test
    void commandFor_whileInRooms_navigatesTheGridAndConfirms() {
        assertEquals(GameControls.Command.MOVE_UP, rooms(KeyCode.W));
        assertEquals(GameControls.Command.MOVE_UP, rooms(KeyCode.UP));
        assertEquals(GameControls.Command.MOVE_DOWN, rooms(KeyCode.S));
        assertEquals(GameControls.Command.MOVE_DOWN, rooms(KeyCode.DOWN));
        assertEquals(GameControls.Command.MOVE_LEFT, rooms(KeyCode.A));
        assertEquals(GameControls.Command.MOVE_LEFT, rooms(KeyCode.LEFT));
        assertEquals(GameControls.Command.MOVE_RIGHT, rooms(KeyCode.D));
        assertEquals(GameControls.Command.MOVE_RIGHT, rooms(KeyCode.RIGHT));
        assertEquals(GameControls.Command.CONFIRM, rooms(KeyCode.ENTER));
        assertEquals(GameControls.Command.BACK, rooms(KeyCode.ESCAPE));
        assertEquals(GameControls.Command.CYCLE_THEME, rooms(KeyCode.T));
        assertEquals(GameControls.Command.IGNORED, rooms(KeyCode.R));
    }

    @Test
    void commandFor_whileInRooms_flipsPagesOnPageKeysAndShiftedArrows() {
        assertEquals(GameControls.Command.PAGE_UP, rooms(KeyCode.PAGE_UP));
        assertEquals(GameControls.Command.PAGE_DOWN, rooms(KeyCode.PAGE_DOWN));
        assertEquals(GameControls.Command.PAGE_UP,
                GameControls.commandFor(GameControls.Mode.ROOMS, KeyCode.W, true));
        assertEquals(GameControls.Command.PAGE_UP,
                GameControls.commandFor(GameControls.Mode.ROOMS, KeyCode.UP, true));
        assertEquals(GameControls.Command.PAGE_DOWN,
                GameControls.commandFor(GameControls.Mode.ROOMS, KeyCode.S, true));
        assertEquals(GameControls.Command.PAGE_DOWN,
                GameControls.commandFor(GameControls.Mode.ROOMS, KeyCode.DOWN, true));
        assertEquals(GameControls.Command.MOVE_UP, rooms(KeyCode.W));
        assertEquals(GameControls.Command.MOVE_DOWN, rooms(KeyCode.S));
    }

    @Test
    void commandFor_pageKeys_belongToRoomsOnly() {
        assertEquals(GameControls.Command.IGNORED, playing(KeyCode.PAGE_UP));
        assertEquals(GameControls.Command.IGNORED, paused(KeyCode.PAGE_DOWN));
        assertEquals(GameControls.Command.IGNORED, solved(KeyCode.PAGE_UP));
        assertEquals(GameControls.Command.IGNORED, menu(KeyCode.PAGE_DOWN));
        assertEquals(GameControls.Command.IGNORED, howTo(KeyCode.PAGE_UP));
    }

    @Test
    void commandFor_whileInHowTo_backsOutAndCyclesTheTheme() {
        assertEquals(GameControls.Command.BACK, howTo(KeyCode.ESCAPE));
        assertEquals(GameControls.Command.CYCLE_THEME, howTo(KeyCode.T));
        assertEquals(GameControls.Command.IGNORED, howTo(KeyCode.ENTER));
        assertEquals(GameControls.Command.IGNORED, howTo(KeyCode.W));
        assertEquals(GameControls.Command.IGNORED, howTo(KeyCode.N));
    }

    @Test
    void commandFor_whileInSettings_navigatesConfirmsAndBacksOut() {
        assertEquals(GameControls.Command.MOVE_UP, settings(KeyCode.W));
        assertEquals(GameControls.Command.MOVE_UP, settings(KeyCode.UP));
        assertEquals(GameControls.Command.MOVE_DOWN, settings(KeyCode.S));
        assertEquals(GameControls.Command.MOVE_DOWN, settings(KeyCode.DOWN));
        assertEquals(GameControls.Command.CONFIRM, settings(KeyCode.ENTER));
        assertEquals(GameControls.Command.BACK, settings(KeyCode.ESCAPE));
        assertEquals(GameControls.Command.CYCLE_THEME, settings(KeyCode.T));
        assertEquals(GameControls.Command.IGNORED, settings(KeyCode.A));
        assertEquals(GameControls.Command.IGNORED, settings(KeyCode.R));
        assertEquals(GameControls.Command.IGNORED, settings(KeyCode.N));
    }

    @Test
    void commandFor_nextRoomAndPause_belongToOneModeEach() {
        assertEquals(GameControls.Command.IGNORED, playing(KeyCode.N));
        assertEquals(GameControls.Command.PAUSE, playing(KeyCode.ESCAPE));
        assertEquals(GameControls.Command.RESUME, paused(KeyCode.ESCAPE));
        assertEquals(GameControls.Command.BACK, solved(KeyCode.ESCAPE));
    }

    @Test
    void commandFor_everyKeyOutsideAModesOwnBindings_isIgnoredWithOrWithoutTheShortcut() {
        Map<GameControls.Mode, Set<KeyCode>> bound = Map.of(
                GameControls.Mode.PLAYING,
                Set.of(KeyCode.W, KeyCode.UP, KeyCode.S, KeyCode.DOWN, KeyCode.A, KeyCode.LEFT,
                        KeyCode.D, KeyCode.RIGHT, KeyCode.U, KeyCode.BACK_SPACE, KeyCode.Z,
                        KeyCode.R, KeyCode.ESCAPE, KeyCode.T),
                GameControls.Mode.PAUSED,
                Set.of(KeyCode.W, KeyCode.UP, KeyCode.S, KeyCode.DOWN, KeyCode.ENTER,
                        KeyCode.ESCAPE, KeyCode.R, KeyCode.T),
                GameControls.Mode.SOLVED,
                Set.of(KeyCode.N, KeyCode.R, KeyCode.ENTER, KeyCode.ESCAPE, KeyCode.T),
                GameControls.Mode.MENU,
                Set.of(KeyCode.W, KeyCode.UP, KeyCode.S, KeyCode.DOWN, KeyCode.ENTER, KeyCode.T),
                GameControls.Mode.ROOMS,
                Set.of(KeyCode.W, KeyCode.UP, KeyCode.S, KeyCode.DOWN, KeyCode.A, KeyCode.LEFT,
                        KeyCode.D, KeyCode.RIGHT, KeyCode.PAGE_UP, KeyCode.PAGE_DOWN,
                        KeyCode.ENTER, KeyCode.ESCAPE, KeyCode.T),
                GameControls.Mode.HOW_TO,
                Set.of(KeyCode.ESCAPE, KeyCode.T),
                GameControls.Mode.SETTINGS,
                Set.of(KeyCode.W, KeyCode.UP, KeyCode.S, KeyCode.DOWN, KeyCode.ENTER, KeyCode.ESCAPE, KeyCode.T));

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
        for (KeyCode code : Set.of(KeyCode.W, KeyCode.UP, KeyCode.S, KeyCode.DOWN, KeyCode.ENTER,
                KeyCode.ESCAPE, KeyCode.R, KeyCode.T)) {
            assertNotEquals(GameControls.Command.IGNORED, paused(code), code.toString());
        }
        for (KeyCode code : Set.of(KeyCode.N, KeyCode.R, KeyCode.ENTER, KeyCode.ESCAPE, KeyCode.T)) {
            assertNotEquals(GameControls.Command.IGNORED, solved(code), code.toString());
        }
        for (KeyCode code : Set.of(KeyCode.W, KeyCode.UP, KeyCode.S, KeyCode.DOWN, KeyCode.ENTER, KeyCode.T)) {
            assertNotEquals(GameControls.Command.IGNORED, menu(code), code.toString());
        }
        for (KeyCode code : Set.of(KeyCode.W, KeyCode.UP, KeyCode.S, KeyCode.DOWN, KeyCode.A, KeyCode.LEFT,
                KeyCode.D, KeyCode.RIGHT, KeyCode.PAGE_UP, KeyCode.PAGE_DOWN,
                KeyCode.ENTER, KeyCode.ESCAPE, KeyCode.T)) {
            assertNotEquals(GameControls.Command.IGNORED, rooms(code), code.toString());
        }
        for (KeyCode code : Set.of(KeyCode.ESCAPE, KeyCode.T)) {
            assertNotEquals(GameControls.Command.IGNORED, howTo(code), code.toString());
        }
        for (KeyCode code : Set.of(KeyCode.W, KeyCode.UP, KeyCode.S, KeyCode.DOWN,
                KeyCode.ENTER, KeyCode.ESCAPE, KeyCode.T)) {
            assertNotEquals(GameControls.Command.IGNORED, settings(code), code.toString());
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
        assertEquals(Optional.empty(), GameControls.directionOf(GameControls.Command.CONFIRM));
        assertEquals(Optional.empty(), GameControls.directionOf(GameControls.Command.BACK));
        assertEquals(Optional.empty(), GameControls.directionOf(GameControls.Command.PAGE_UP));
        assertEquals(Optional.empty(), GameControls.directionOf(GameControls.Command.PAGE_DOWN));
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

    private static GameControls.Command menu(KeyCode code) {
        return GameControls.commandFor(GameControls.Mode.MENU, code, false);
    }

    private static GameControls.Command rooms(KeyCode code) {
        return GameControls.commandFor(GameControls.Mode.ROOMS, code, false);
    }

    private static GameControls.Command howTo(KeyCode code) {
        return GameControls.commandFor(GameControls.Mode.HOW_TO, code, false);
    }

    private static GameControls.Command settings(KeyCode code) {
        return GameControls.commandFor(GameControls.Mode.SETTINGS, code, false);
    }
}
