package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.core.Direction;
import com.milandru.sokobani.engine.GameEvent;
import com.milandru.sokobani.persistence.Progress;
import com.milandru.sokobani.ui.FxToolkit;
import com.milandru.sokobani.ui.Surface;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScreenLifecycleTest {

    @TempDir
    Path home;

    @Test
    void everyScreen_shownAndThenHidden_leavesNoTimelinePlaying() {
        ScreenFixture fixture = ScreenFixture.twoRooms(home);
        Screen parking = new RecordingScreen("parking", new ArrayList<>());

        for (Screen screen : sevenScreens(fixture)) {
            fixture.show(screen);
            fixture.show(parking);
            fixture.settleTheMenuSolver();

            assertFalse(fixture.gameAnimating(), screen.getClass().getSimpleName() + " left the board animating");
            assertFalse(fixture.menuAnimating(), screen.getClass().getSimpleName() + " left the menu animating");
        }
    }

    @Test
    void everyScreen_shownAndHiddenAndShownAgain_stillRenders() {
        ScreenFixture fixture = ScreenFixture.twoRooms(home);
        Screen parking = new RecordingScreen("parking", new ArrayList<>());

        for (Screen screen : sevenScreens(fixture)) {
            fixture.show(screen);
            fixture.show(parking);
            fixture.show(screen);

            Surface surface = fixture.render((BaseScreen) screen);

            assertTrue(surface.width() > 0 && surface.height() > 0, screen.getClass().getSimpleName());
        }
        fixture.show(parking);
        fixture.settleTheMenuSolver();
    }

    @Test
    void gameScreen_hidden_stopsTheAnimationTimeline() {
        ScreenFixture fixture = ScreenFixture.twoRooms(home);

        fixture.show(fixture.game);
        assertTrue(fixture.gameAnimating());

        fixture.show(fixture.howTo);
        assertFalse(fixture.gameAnimating());
    }

    @Test
    void gameScreen_hidden_unsubscribesFromTheSession() {
        ScreenFixture fixture = ScreenFixture.twoRooms(home);
        fixture.show(fixture.game);
        fixture.show(fixture.howTo);

        fixture.play(ScreenFixture.SOLUTION);

        assertTrue(fixture.session.isSolved());
        assertSame(fixture.howTo, fixture.current(),
                "the hidden game screen still routed a solve to the win screen");
    }

    @Test
    void gameScreen_shownTwiceThroughTheControllerAndHiddenOnce_leavesNothingSubscribed() {
        ScreenFixture fixture = ScreenFixture.twoRooms(home);
        fixture.show(fixture.game);
        fixture.show(fixture.game);
        fixture.show(fixture.howTo);

        fixture.play(ScreenFixture.SOLUTION);

        assertSame(fixture.howTo, fixture.current(),
                "a second shown() subscribed a second listener that hidden() did not remove");
    }

    @Test
    void gameScreen_shownWithTheSessionAlreadySolved_resyncsRatherThanTrustingItsFields() {
        ScreenFixture fixture = ScreenFixture.twoRooms(home);
        fixture.show(fixture.howTo);
        fixture.play(ScreenFixture.SOLUTION);

        fixture.show(fixture.game);

        assertSame(fixture.win, fixture.current());
        assertFalse(fixture.gameAnimating(), "the game screen animated a board it had just navigated away from");
    }

    @Test
    void gameScreen_hidden_dropsTheTweenItWasCarrying() {
        ScreenFixture fixture = ScreenFixture.twoRooms(home);
        fixture.show(fixture.game);
        fixture.move(Direction.RIGHT);
        assertTrue(FxToolkit.on(() -> fixture.game.tweenInFlight().isPresent()));

        fixture.show(fixture.howTo);

        assertFalse(FxToolkit.on(() -> fixture.game.tweenInFlight().isPresent()));
    }

    @Test
    void menuScreen_shownASecondTime_buildsAFreshBoardFromTheProgressOfThatMoment() {
        ScreenFixture fixture = ScreenFixture.twoRooms(home);
        fixture.show(fixture.menu);
        MenuBoard first = FxToolkit.on(fixture.menu::board);
        assertFalse(first.solved(0));

        fixture.show(fixture.howTo);
        fixture.settleTheMenuSolver();
        fixture.play(ScreenFixture.SOLUTION);
        fixture.show(fixture.menu);
        MenuBoard second = FxToolkit.on(fixture.menu::board);

        assertNotSame(first, second);
        assertTrue(second.solved(0), "the second board was built from stale progress");
        fixture.show(fixture.howTo);
        fixture.settleTheMenuSolver();
    }

    @Test
    void menuScreen_hidden_dropsItsTimeline() {
        ScreenFixture fixture = ScreenFixture.twoRooms(home);

        fixture.show(fixture.menu);
        assertTrue(fixture.menuAnimating());

        fixture.show(fixture.howTo);
        fixture.settleTheMenuSolver();

        assertFalse(fixture.menuAnimating());
    }

    @Test
    void roomsScreen_shownASecondTime_rebuildsItsGridFromTheProgressOfThatMoment() {
        ScreenFixture fixture = ScreenFixture.twoRooms(home);
        fixture.show(fixture.rooms);
        Surface locked = fixture.render(fixture.rooms);

        fixture.show(fixture.howTo);
        fixture.play(ScreenFixture.SOLUTION);
        fixture.show(fixture.rooms);
        Surface unlocked = fixture.render(fixture.rooms);

        assertFalse(Arrays.equals(locked.tones(), unlocked.tones()),
                "the rooms grid was not rebuilt when the screen came back");
    }

    @Test
    void winScreen_shownWithoutAFreshPresent_readsTheSessionRatherThanTheDataOfAnEarlierWin() {
        ScreenFixture fixture = ScreenFixture.twoRooms(home);
        fixture.show(fixture.game);
        fixture.play(ScreenFixture.SOLUTION);
        assertSame(fixture.win, fixture.current());

        fixture.show(fixture.howTo);
        FxToolkit.run(fixture.session::restart);
        fixture.play(ScreenFixture.WASTEFUL_SOLUTION);
        fixture.show(fixture.win);
        Surface fallback = fixture.render(fixture.win);

        assertArrayEquals(theWinTheSessionNowHolds(fixture).tones(), fallback.tones(),
                "the win screen redrew the WinData of an earlier win rather than the counts the session now holds");
    }

    @Test
    void winScreen_thePresentedDataAndTheFallback_drawDifferentlyEnoughToTellApart() {
        ScreenFixture fixture = ScreenFixture.twoRooms(home);
        fixture.show(fixture.game);
        fixture.play(ScreenFixture.SOLUTION);
        Surface presented = fixture.render(fixture.win);

        fixture.show(fixture.howTo);
        fixture.show(fixture.win);
        Surface fallback = fixture.render(fixture.win);

        assertFalse(Arrays.equals(presented.tones(), fallback.tones()),
                "the presented data and the fallback are indistinguishable, so the staleness test proves nothing");
    }

    private static Surface theWinTheSessionNowHolds(ScreenFixture fixture) {
        WinData data = new WinData(
                new GameEvent.Solved(fixture.session.currentLevel(), fixture.session.moveCount(),
                        fixture.session.pushCount()),
                fixture.session.progress().levelRecord(fixture.session.levelIndex())
                        .filter(Progress.LevelRecord::solved));
        Surface surface = new Surface(WinView.WIDTH, WinView.HEIGHT);
        WinView.render(surface, BoardFixture.typeSetter(), fixture.session.state(), data,
                fixture.session.hasNextLevel());
        return surface;
    }

    private static List<Screen> sevenScreens(ScreenFixture fixture) {
        return List.of(fixture.menu, fixture.rooms, fixture.game, fixture.win, fixture.pause, fixture.howTo,
                fixture.settingsScreen);
    }
}
