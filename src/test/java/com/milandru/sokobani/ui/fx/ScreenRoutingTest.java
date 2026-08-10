package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.ui.Theme;

import javafx.scene.input.KeyCode;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScreenRoutingTest {

    @TempDir
    Path home;

    @Test
    void gameScreen_escape_pauses() {
        ScreenFixture fixture = ScreenFixture.twoRooms(home);
        fixture.show(fixture.game);

        fixture.press(KeyCode.ESCAPE);

        assertSame(fixture.pause, fixture.current());
    }

    @Test
    void gameScreen_undo_takesTheMoveBack() {
        ScreenFixture fixture = ScreenFixture.twoRooms(home);
        fixture.show(fixture.game);
        fixture.press(KeyCode.D);
        assertEquals(1, fixture.session.moveCount());

        fixture.press(KeyCode.U);

        assertEquals(0, fixture.session.moveCount());
    }

    @Test
    void gameScreen_restart_putsTheRoomBack() {
        ScreenFixture fixture = ScreenFixture.twoRooms(home);
        fixture.show(fixture.game);
        fixture.press(KeyCode.D);

        fixture.press(KeyCode.R);

        assertEquals(0, fixture.session.moveCount());
        assertSame(fixture.game, fixture.current());
    }

    @Test
    void pauseScreen_escape_resumesTheGame() {
        ScreenFixture fixture = ScreenFixture.twoRooms(home);
        fixture.show(fixture.pause);

        fixture.press(KeyCode.ESCAPE);

        assertSame(fixture.game, fixture.current());
    }

    @Test
    void pauseScreen_restart_returnsToAFreshBoard() {
        ScreenFixture fixture = ScreenFixture.twoRooms(home);
        fixture.show(fixture.game);
        fixture.press(KeyCode.D);
        fixture.press(KeyCode.ESCAPE);

        fixture.press(KeyCode.R);

        assertSame(fixture.game, fixture.current());
        assertEquals(0, fixture.session.moveCount());
    }

    @Test
    void pauseScreen_roomsThenMainMenu_areBothReachable() {
        ScreenFixture fixture = ScreenFixture.twoRooms(home);
        fixture.show(fixture.pause);
        fixture.press(KeyCode.DOWN);
        fixture.press(KeyCode.DOWN);

        fixture.press(KeyCode.ENTER);

        assertSame(fixture.rooms, fixture.current());
    }

    @Test
    void winScreen_escape_opensTheRooms() {
        ScreenFixture fixture = ScreenFixture.twoRooms(home);
        fixture.show(fixture.game);
        fixture.press(KeyCode.D);
        fixture.press(KeyCode.D);
        fixture.press(KeyCode.D);
        assertSame(fixture.win, fixture.current());

        fixture.press(KeyCode.ESCAPE);

        assertSame(fixture.rooms, fixture.current());
    }

    @Test
    void winScreen_replay_startsTheSameRoomOver() {
        ScreenFixture fixture = ScreenFixture.twoRooms(home);
        fixture.show(fixture.game);
        fixture.press(KeyCode.D);
        fixture.press(KeyCode.D);
        fixture.press(KeyCode.D);

        fixture.press(KeyCode.R);

        assertSame(fixture.game, fixture.current());
        assertEquals(0, fixture.session.levelIndex());
        assertEquals(0, fixture.session.moveCount());
    }

    @Test
    void roomsScreen_aLockedRoom_cannotBeStarted() {
        ScreenFixture fixture = ScreenFixture.twoRooms(home);
        fixture.show(fixture.rooms);

        fixture.press(KeyCode.RIGHT);
        fixture.press(KeyCode.ENTER);

        assertEquals(0, fixture.session.levelIndex());
    }

    @Test
    void settingsScreen_enter_togglesTheSelectedRowAndKeepsIt() {
        ScreenFixture fixture = ScreenFixture.twoRooms(home);
        fixture.show(fixture.settingsScreen);
        assertFalse(fixture.settings.muted());

        fixture.press(KeyCode.ENTER);

        assertTrue(fixture.settings.muted());
        assertTrue(fixture.sound.muted());
    }

    @Test
    void settingsScreen_theAnimationAndHintRows_bothToggle() {
        ScreenFixture fixture = ScreenFixture.twoRooms(home);
        fixture.show(fixture.settingsScreen);

        fixture.press(KeyCode.DOWN);
        fixture.press(KeyCode.ENTER);
        assertFalse(fixture.settings.animationEnabled());

        fixture.press(KeyCode.DOWN);
        fixture.press(KeyCode.ENTER);
        assertFalse(fixture.settings.hintsEnabled());
    }

    @Test
    void everyScreen_answersTheThemeKey() {
        ScreenFixture fixture = ScreenFixture.twoRooms(home);
        List<Screen> screens = List.of(fixture.menu, fixture.rooms, fixture.game, fixture.win, fixture.pause,
                fixture.howTo, fixture.settingsScreen);

        for (Screen screen : screens) {
            fixture.show(screen);
            Theme before = fixture.controller.theme();

            fixture.press(KeyCode.T);

            assertEquals(Theme.ALL.get(Math.floorMod(Theme.ALL.indexOf(before) + 1, Theme.ALL.size())),
                    fixture.controller.theme(), screen.getClass().getSimpleName() + " ignored the theme key");
        }
        fixture.show(fixture.howTo);
        fixture.settleTheMenuSolver();
    }
}
