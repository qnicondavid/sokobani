package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.ui.Theme;

import javafx.scene.Parent;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;

import java.util.Objects;

public final class ScreenController {

    private final StackPane root = new StackPane();
    private final ThemeStore themes;
    private Theme theme;

    private Screen current;

    public ScreenController(ThemeStore themes) {
        this.themes = Objects.requireNonNull(themes, "themes");
        this.theme = themes.load();
    }

    public Theme theme() {
        return theme;
    }

    public void cycleTheme() {
        theme = Theme.ALL.get(Math.floorMod(Theme.ALL.indexOf(theme) + 1, Theme.ALL.size()));
        themes.save(theme);
    }

    public Parent root() {
        return root;
    }

    public void show(Screen screen) {
        Objects.requireNonNull(screen, "screen");
        if (current != null) {
            current.hidden();
        }
        current = screen;
        root.getChildren().setAll(screen.node());
        screen.shown();
    }

    Screen current() {
        return current;
    }

    public void handleKey(KeyEvent event) {
        if (current != null) {
            current.onKeyPressed(event);
        }
    }
}
