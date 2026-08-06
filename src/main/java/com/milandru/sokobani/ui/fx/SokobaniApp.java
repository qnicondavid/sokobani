package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.engine.GameSession;
import com.milandru.sokobani.level.InvalidLevelFormatException;
import com.milandru.sokobani.level.LevelRepository;
import com.milandru.sokobani.persistence.ProgressStore;
import com.milandru.sokobani.ui.FontLoader;
import com.milandru.sokobani.ui.TypeSetter;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public final class SokobaniApp extends Application {

    private static final String TITLE = "Sokobani";
    private static final int WINDOW_WIDTH = 900;
    private static final int WINDOW_HEIGHT = 780;
    private static final int MIN_WINDOW_WIDTH = 560;
    private static final int MIN_WINDOW_HEIGHT = 560;

    @Override
    public void start(Stage stage) {
        Optional<GameSession> session = loadSession();
        if (session.isEmpty()) {
            Platform.exit();
            return;
        }

        ThemeStore themes = ThemeStore.atUserHome();
        TypeSetter setter = new TypeSetter(new FontLoader());
        ScreenController controller = new ScreenController();
        controller.show(new GameScreen(session.orElseThrow(), setter, controller, themes, themes.load()));

        Scene scene = new Scene(controller.root(), WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.setOnKeyPressed(controller::handleKey);

        stage.setTitle(TITLE);
        stage.setScene(scene);
        stage.setMinWidth(MIN_WINDOW_WIDTH);
        stage.setMinHeight(MIN_WINDOW_HEIGHT);
        stage.show();
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
