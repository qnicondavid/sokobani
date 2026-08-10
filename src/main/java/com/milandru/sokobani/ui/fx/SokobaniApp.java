package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.engine.GameSession;
import com.milandru.sokobani.level.InvalidLevelFormatException;
import com.milandru.sokobani.level.LevelRepository;
import com.milandru.sokobani.persistence.ProgressStore;
import com.milandru.sokobani.persistence.SettingsStore;
import com.milandru.sokobani.persistence.WindowGeometry;
import com.milandru.sokobani.persistence.WindowStore;
import com.milandru.sokobani.ui.FontLoader;
import com.milandru.sokobani.ui.TypeSetter;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public final class SokobaniApp extends Application {

    private static final String TITLE = "Sokobani";
    private static final int WINDOW_WIDTH = 900;
    private static final int WINDOW_HEIGHT = 780;
    private static final int MIN_WINDOW_WIDTH = 560;
    private static final int MIN_WINDOW_HEIGHT = 560;

    private Stage stage;
    private WindowStore windowStore;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        Optional<GameSession> session = loadSession();
        if (session.isEmpty()) {
            Platform.exit();
            return;
        }

        ThemeStore themes = ThemeStore.atUserHome();
        TypeSetter setter = new TypeSetter(new FontLoader());
        ScreenController controller = new ScreenController(themes);
        SettingsState settings = new SettingsState(SettingsStore.atUserHome());
        SoundBank sound = SoundBank.load(settings.muted());
        windowStore = WindowStore.atUserHome();

        Screens screens = new Screens();
        GameScreen game = new GameScreen(session.orElseThrow(), setter, controller, screens, settings, sound);
        MenuScreen menu = new MenuScreen(session.orElseThrow(), setter, controller, screens);
        RoomsScreen rooms = new RoomsScreen(session.orElseThrow(), setter, controller, screens);
        WinScreen win = new WinScreen(session.orElseThrow(), setter, controller, screens);
        PauseScreen pause = new PauseScreen(session.orElseThrow(), setter, controller, screens);
        HowToScreen howTo = new HowToScreen(setter, controller, screens);
        SettingsScreen settingsScreen = new SettingsScreen(setter, controller, screens, settings, sound);
        screens.bind(game, menu, rooms, win, pause, howTo, settingsScreen);

        controller.show(menu);

        Scene scene = new Scene(controller.root(), WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.setOnKeyPressed(controller::handleKey);

        stage.setTitle(TITLE);
        stage.setScene(scene);
        stage.setMinWidth(MIN_WINDOW_WIDTH);
        stage.setMinHeight(MIN_WINDOW_HEIGHT);
        windowStore.load().ifPresent(geometry -> {
            stage.setWidth(Math.max(geometry.width(), MIN_WINDOW_WIDTH));
            stage.setHeight(Math.max(geometry.height(), MIN_WINDOW_HEIGHT));
        });
        icon().ifPresent(image -> stage.getIcons().add(image));
        stage.show();
    }

    @Override
    public void stop() {
        if (stage == null || windowStore == null) {
            return;
        }
        windowStore.save(new WindowGeometry((int) stage.getWidth(), (int) stage.getHeight()));
    }

    private static Optional<Image> icon() {
        try (InputStream stream = SokobaniApp.class.getResourceAsStream("/icon.png")) {
            if (stream == null) {
                return Optional.empty();
            }
            return Optional.of(new Image(stream));
        } catch (IOException | RuntimeException unavailable) {
            return Optional.empty();
        }
    }

    private static Optional<GameSession> loadSession() {
        try {
            return Optional.of(
                    new GameSession(LevelRepository.load(LevelRepository.CLASSIC_PACK), ProgressStore.atUserHome()));
        } catch (IOException | InvalidLevelFormatException unloadable) {
            System.err.println("Could not load the level pack: " + unloadable.getMessage());
            return Optional.empty();
        }
    }
}
