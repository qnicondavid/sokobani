package com.milandru.sokobani.ui.fx;

import javafx.scene.Parent;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;

import java.util.List;

final class RecordingScreen implements Screen {

    private final String name;
    private final List<String> log;
    private final Pane pane = new Pane();

    RecordingScreen(String name, List<String> log) {
        this.name = name;
        this.log = log;
    }

    @Override
    public Parent node() {
        return pane;
    }

    @Override
    public void onKeyPressed(KeyEvent event) {
        log.add(name + " key");
    }

    @Override
    public void shown() {
        log.add(name + " shown");
    }

    @Override
    public void hidden() {
        log.add(name + " hidden");
    }
}
