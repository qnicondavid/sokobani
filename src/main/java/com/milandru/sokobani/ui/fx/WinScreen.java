package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.engine.GameEvent;
import com.milandru.sokobani.engine.GameSession;
import com.milandru.sokobani.persistence.Progress;
import com.milandru.sokobani.ui.Surface;
import com.milandru.sokobani.ui.TypeSetter;

import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.util.Objects;

public final class WinScreen extends BaseScreen {

    private final GameSession session;
    private final TypeSetter setter;
    private final Screens screens;

    private WinData data;

    public WinScreen(GameSession session, TypeSetter setter, ScreenController controller, Screens screens) {
        super(controller);
        this.session = Objects.requireNonNull(session, "session");
        this.setter = Objects.requireNonNull(setter, "setter");
        this.screens = Objects.requireNonNull(screens, "screens");
        holder().setOnMouseClicked(this::onMouseClicked);
    }

    void present(WinData data) {
        this.data = data;
    }

    @Override
    public void shown() {
        if (data == null) {
            data = new WinData(
                    new GameEvent.Solved(session.currentLevel(), session.moveCount(), session.pushCount()),
                    session.progress().levelRecord(session.levelIndex()).filter(Progress.LevelRecord::solved));
        }
        redraw();
    }

    @Override
    public void onKeyPressed(KeyEvent event) {
        switch (GameControls.commandFor(GameControls.Mode.SOLVED, event.getCode(),
                event.isShortcutDown() || event.isControlDown())) {
            case NEXT_ROOM -> nextRoom();
            case RESTART -> replay();
            case CONFIRM -> primary();
            case BACK -> controller().show(screens.rooms());
            case CYCLE_THEME -> cycleTheme();
            case MOVE_UP, MOVE_DOWN, MOVE_LEFT, MOVE_RIGHT, UNDO, PAUSE, RESUME, IGNORED -> {
                return;
            }
        }
        redraw();
    }

    private void onMouseClicked(MouseEvent event) {
        switch (WinView.targetAt(baseX(event.getX()), baseY(event.getY()), session.hasNextLevel())) {
            case NEXT_ROOM -> nextRoom();
            case REPLAY -> replay();
            case ROOMS -> controller().show(screens.rooms());
            case NONE -> {
                return;
            }
        }
        redraw();
    }

    private void primary() {
        if (session.hasNextLevel()) {
            nextRoom();
        } else {
            replay();
        }
    }

    private void nextRoom() {
        if (session.nextLevel()) {
            controller().show(screens.game());
        }
    }

    private void replay() {
        session.restart();
        controller().show(screens.game());
    }

    @Override
    protected Surface renderBase() {
        Surface surface = new Surface(WinView.WIDTH, WinView.HEIGHT);
        WinView.render(surface, setter, session.state(), data, session.hasNextLevel());
        return surface;
    }
}
