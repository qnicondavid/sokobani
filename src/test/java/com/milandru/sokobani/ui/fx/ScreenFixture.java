package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.core.Direction;
import com.milandru.sokobani.engine.GameSession;
import com.milandru.sokobani.level.LevelPack;
import com.milandru.sokobani.persistence.Progress;
import com.milandru.sokobani.persistence.ProgressStore;
import com.milandru.sokobani.persistence.SettingsStore;
import com.milandru.sokobani.ui.FxToolkit;
import com.milandru.sokobani.ui.Surface;
import com.milandru.sokobani.ui.TypeSetter;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;

import java.nio.file.Path;
import java.util.function.BooleanSupplier;

final class ScreenFixture {

    static final String ROOM = """
            #######
            #@$  .#
            #     #
            #######
            """;
    static final String SOLUTION = "RRR";
    static final String WASTEFUL_SOLUTION = "DURRR";

    static final int WINDOW_WIDTH = 900;
    static final int WINDOW_HEIGHT = 780;

    private static final long SETTLE_TIMEOUT_MILLIS = 15_000;
    private static final long SETTLE_POLL_MILLIS = 5;

    final GameSession session;
    final ScreenController controller;
    final Screens screens;
    final SettingsState settings;
    final SoundBank sound;
    final GameScreen game;
    final MenuScreen menu;
    final RoomsScreen rooms;
    final WinScreen win;
    final PauseScreen pause;
    final HowToScreen howTo;
    final SettingsScreen settingsScreen;

    private ScreenFixture(Path home, LevelPack pack, ProgressStore store) {
        session = new GameSession(pack, store);
        TypeSetter setter = BoardFixture.typeSetter();
        controller = new ScreenController(new ThemeStore(home));
        settings = new SettingsState(new SettingsStore(home));
        sound = SoundBank.load(true);
        screens = new Screens();
        game = new GameScreen(session, setter, controller, screens, settings, sound);
        menu = new MenuScreen(session, setter, controller, screens);
        rooms = new RoomsScreen(session, setter, controller, screens);
        win = new WinScreen(session, setter, controller, screens);
        pause = new PauseScreen(session, setter, controller, screens);
        howTo = new HowToScreen(setter, controller, screens);
        settingsScreen = new SettingsScreen(setter, controller, screens, settings, sound);
        screens.bind(game, menu, rooms, win, pause, howTo, settingsScreen);
        ((Region) controller.root()).resize(WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    static ScreenFixture on(Path home, String... layouts) {
        return FxToolkit.on(() -> new ScreenFixture(home, BoardFixture.pack(layouts), null));
    }

    static ScreenFixture twoRooms(Path home) {
        return on(home, ROOM, ROOM);
    }

    static ScreenFixture withProgress(Path home, LevelPack pack, Progress progress) {
        return FxToolkit.on(() -> {
            ProgressStore store = new ProgressStore(home);
            store.save(progress);
            return new ScreenFixture(home, pack, store);
        });
    }

    void show(Screen screen) {
        FxToolkit.run(() -> {
            controller.show(screen);
            layoutRoot();
        });
    }

    void layout() {
        FxToolkit.run(this::layoutRoot);
    }

    Screen current() {
        return FxToolkit.on(controller::current);
    }

    void press(KeyCode code) {
        press(code, false);
    }

    void press(KeyCode code, boolean shortcutDown) {
        FxToolkit.run(() -> controller.handleKey(
                new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, shortcutDown, false, false)));
    }

    void move(Direction direction) {
        FxToolkit.run(() -> session.move(direction));
    }

    void undo() {
        FxToolkit.run(session::undo);
    }

    void play(String moves) {
        for (char move : moves.toCharArray()) {
            move(directionOf(move));
        }
    }

    Surface render(BaseScreen screen) {
        return FxToolkit.on(screen::renderBase);
    }

    boolean gameAnimating() {
        return FxToolkit.on(game::animating);
    }

    boolean menuAnimating() {
        return FxToolkit.on(menu::animating);
    }

    void settleTheMenuSolver() {
        settle(() -> !menu.solving(), "the menu solver was still walking the pack");
    }

    void settle(BooleanSupplier quiet, String complaint) {
        long deadline = System.currentTimeMillis() + SETTLE_TIMEOUT_MILLIS;
        while (!quiet.getAsBoolean()) {
            if (System.currentTimeMillis() > deadline) {
                throw new IllegalStateException(complaint + " within " + SETTLE_TIMEOUT_MILLIS + "ms");
            }
            sleep();
        }
    }

    private void layoutRoot() {
        Region root = (Region) controller.root();
        root.resize(WINDOW_WIDTH, WINDOW_HEIGHT);
        root.applyCss();
        root.layout();
    }

    private static Direction directionOf(char move) {
        return switch (Character.toUpperCase(move)) {
            case 'U' -> Direction.UP;
            case 'D' -> Direction.DOWN;
            case 'L' -> Direction.LEFT;
            case 'R' -> Direction.RIGHT;
            default -> throw new IllegalArgumentException("not a move: " + move);
        };
    }

    private static void sleep() {
        try {
            Thread.sleep(SETTLE_POLL_MILLIS);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("interrupted while waiting for the screens to settle", interrupted);
        }
    }
}
