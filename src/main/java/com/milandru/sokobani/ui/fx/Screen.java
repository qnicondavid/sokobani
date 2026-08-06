package com.milandru.sokobani.ui.fx;

import javafx.scene.Parent;
import javafx.scene.input.KeyEvent;

public interface Screen {

    Parent node();

    void onKeyPressed(KeyEvent event);

    default void shown() {
    }

    default void hidden() {
    }
}
