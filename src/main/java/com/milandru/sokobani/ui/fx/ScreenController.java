package com.milandru.sokobani.ui.fx;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;

import java.util.Objects;

public final class ScreenController {

    private final StackPane root = new StackPane();

    private Screen current;
    private Node overlay;

    public Parent root() {
        return root;
    }

    public void show(Screen screen) {
        Objects.requireNonNull(screen, "screen");
        if (current != null) {
            current.hidden();
        }
        current = screen;
        overlay = null;
        root.getChildren().setAll(screen.node());
        screen.shown();
    }

    public void showOverlay(Node node) {
        Objects.requireNonNull(node, "node");
        hideOverlay();
        overlay = node;
        root.getChildren().add(node);
    }

    public void hideOverlay() {
        if (overlay != null) {
            root.getChildren().remove(overlay);
            overlay = null;
        }
    }

    public void handleKey(KeyEvent event) {
        if (current != null) {
            current.onKeyPressed(event);
        }
    }
}
