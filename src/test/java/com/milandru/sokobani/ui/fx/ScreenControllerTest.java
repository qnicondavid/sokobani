package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.ui.FxToolkit;
import com.milandru.sokobani.ui.Theme;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScreenControllerTest {

    @TempDir
    Path home;

    @Test
    void show_theFirstScreen_showsItWithNothingToHide() {
        List<String> log = new ArrayList<>();
        ScreenController controller = controller();
        RecordingScreen first = new RecordingScreen("first", log);

        FxToolkit.run(() -> controller.show(first));

        assertEquals(List.of("first shown"), log);
    }

    @Test
    void show_aSecondScreen_hidesTheOutgoingBeforeShowingTheIncoming() {
        List<String> log = new ArrayList<>();
        ScreenController controller = controller();
        RecordingScreen first = new RecordingScreen("first", log);
        RecordingScreen second = new RecordingScreen("second", log);

        FxToolkit.run(() -> {
            controller.show(first);
            controller.show(second);
        });

        assertEquals(List.of("first shown", "first hidden", "second shown"), log);
    }

    @Test
    void show_theSameScreenTwice_stillPairsEveryShownWithAHidden() {
        List<String> log = new ArrayList<>();
        ScreenController controller = controller();
        RecordingScreen only = new RecordingScreen("only", log);

        FxToolkit.run(() -> {
            controller.show(only);
            controller.show(only);
        });

        assertEquals(List.of("only shown", "only hidden", "only shown"), log);
    }

    @Test
    void show_leavesOnlyTheCurrentScreensNodeUnderTheRoot() {
        List<String> log = new ArrayList<>();
        ScreenController controller = controller();
        RecordingScreen first = new RecordingScreen("first", log);
        RecordingScreen second = new RecordingScreen("second", log);

        FxToolkit.run(() -> {
            controller.show(first);
            controller.show(second);
        });

        assertEquals(List.of(second.node()), FxToolkit.on(() -> List.copyOf(
                ((javafx.scene.layout.StackPane) controller.root()).getChildren())));
        assertSame(second, FxToolkit.on(controller::current));
    }

    @Test
    void show_refusesNothing() {
        ScreenController controller = controller();

        assertThrows(NullPointerException.class, () -> controller.show(null));
    }

    @Test
    void current_beforeAnythingIsShown_isNothing() {
        assertNull(FxToolkit.on(controller()::current));
    }

    @Test
    void handleKey_beforeAnythingIsShown_doesNothing() {
        ScreenController controller = controller();

        FxToolkit.run(() -> controller.handleKey(press()));
    }

    @Test
    void handleKey_reachesTheCurrentScreenAndNoOther() {
        List<String> log = new ArrayList<>();
        ScreenController controller = controller();
        RecordingScreen first = new RecordingScreen("first", log);
        RecordingScreen second = new RecordingScreen("second", log);

        FxToolkit.run(() -> {
            controller.show(first);
            controller.show(second);
            log.clear();
            controller.handleKey(press());
        });

        assertEquals(List.of("second key"), log);
    }

    @Test
    void cycleTheme_walksTheWholePaletteAndComesBack() {
        ScreenController controller = controller();

        assertEquals(Theme.DEFAULT, controller.theme());
        for (int step = 1; step <= Theme.ALL.size(); step++) {
            controller.cycleTheme();

            assertEquals(Theme.ALL.get(step % Theme.ALL.size()), controller.theme());
        }
    }

    @Test
    void cycleTheme_survivesTheStore() {
        ScreenController controller = controller();
        controller.cycleTheme();

        assertEquals(controller.theme(), new ThemeStore(home).load());
    }

    private ScreenController controller() {
        return FxToolkit.on(() -> new ScreenController(new ThemeStore(home)));
    }

    private static KeyEvent press() {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ENTER, false, false, false, false);
    }
}
