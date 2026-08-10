package com.milandru.sokobani.ui.fx;

import javafx.scene.input.KeyCode;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScreenLoopTest {

    @TempDir
    Path home;

    @Test
    void theWholeLoop_fromTheMenuThroughAWinAndBackToTheMenu_neverDeadEnds() {
        ScreenFixture fixture = ScreenFixture.twoRooms(home);

        fixture.show(fixture.menu);
        assertSame(fixture.menu, fixture.current());

        fixture.press(KeyCode.DOWN);
        fixture.press(KeyCode.ENTER);
        assertSame(fixture.rooms, fixture.current(), "the menu did not open the rooms screen");

        fixture.press(KeyCode.ENTER);
        assertSame(fixture.game, fixture.current(), "the rooms screen did not start a room");
        assertEquals(0, fixture.session.levelIndex());

        solveTheRoomWithTheKeyboard(fixture);
        assertSame(fixture.win, fixture.current(), "solving the room did not reach the win screen");

        fixture.press(KeyCode.N);
        assertSame(fixture.game, fixture.current(), "the win screen did not open the next room");
        assertEquals(1, fixture.session.levelIndex());

        fixture.press(KeyCode.ESCAPE);
        assertSame(fixture.pause, fixture.current(), "escape did not pause the game");

        fixture.press(KeyCode.DOWN);
        fixture.press(KeyCode.DOWN);
        fixture.press(KeyCode.DOWN);
        fixture.press(KeyCode.ENTER);
        assertSame(fixture.menu, fixture.current(), "the pause screen did not return to the menu");

        fixture.show(fixture.howTo);
        fixture.settleTheMenuSolver();
    }

    @Test
    void theWholeLoop_playedTwice_unlocksTheSecondRoomAndLeavesItSelectable() {
        ScreenFixture fixture = ScreenFixture.twoRooms(home);
        fixture.show(fixture.game);
        solveTheRoomWithTheKeyboard(fixture);

        fixture.press(KeyCode.N);
        solveTheRoomWithTheKeyboard(fixture);

        assertSame(fixture.win, fixture.current());
        assertTrue(fixture.session.progress().isSolved(0));
        assertTrue(fixture.session.progress().isSolved(1));
    }

    @Test
    void theWinScreenOfTheLastRoom_offersAReplayRatherThanADeadEnd() {
        ScreenFixture fixture = ScreenFixture.on(home, ScreenFixture.ROOM);
        fixture.show(fixture.game);
        solveTheRoomWithTheKeyboard(fixture);

        fixture.press(KeyCode.ENTER);

        assertSame(fixture.game, fixture.current(), "the last room's win screen had nowhere to go");
        assertEquals(0, fixture.session.moveCount());
    }

    @Test
    void everyScreenReachableFromTheMenu_comesBackToIt() {
        ScreenFixture fixture = ScreenFixture.twoRooms(home);

        fixture.show(fixture.rooms);
        fixture.press(KeyCode.ESCAPE);
        assertSame(fixture.menu, fixture.current(), "the rooms screen does not come back");

        fixture.show(fixture.howTo);
        fixture.press(KeyCode.ESCAPE);
        assertSame(fixture.menu, fixture.current(), "the how to play screen does not come back");

        fixture.show(fixture.settingsScreen);
        fixture.press(KeyCode.ESCAPE);
        assertSame(fixture.menu, fixture.current(), "the settings screen does not come back");

        fixture.show(fixture.howTo);
        fixture.settleTheMenuSolver();
    }

    private static void solveTheRoomWithTheKeyboard(ScreenFixture fixture) {
        for (int push = 0; push < ScreenFixture.SOLUTION.length(); push++) {
            fixture.press(KeyCode.D);
        }
    }
}
