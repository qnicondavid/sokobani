package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.ui.Surface;
import com.milandru.sokobani.ui.TypeSetter;

import javafx.scene.input.KeyEvent;

import java.util.Objects;

public final class HowToScreen extends BaseScreen {

    private final TypeSetter setter;
    private final Screens screens;

    public HowToScreen(TypeSetter setter, ScreenController controller, Screens screens) {
        super(controller);
        this.setter = Objects.requireNonNull(setter, "setter");
        this.screens = Objects.requireNonNull(screens, "screens");
    }

    @Override
    public void shown() {
        redraw();
    }

    @Override
    public void onKeyPressed(KeyEvent event) {
        switch (GameControls.commandFor(GameControls.Mode.HOW_TO, event.getCode(),
                event.isShortcutDown() || event.isControlDown())) {
            case BACK -> controller().show(screens.menu());
            case CYCLE_THEME -> cycleTheme();
            case MOVE_UP, MOVE_DOWN, MOVE_LEFT, MOVE_RIGHT, UNDO, RESTART, PAUSE, RESUME, NEXT_ROOM, CONFIRM,
                    IGNORED -> {
                return;
            }
        }
        redraw();
    }

    @Override
    protected Surface renderBase() {
        Surface surface = new Surface(HowToView.WIDTH, HowToView.HEIGHT);
        HowToView.render(surface, setter);
        return surface;
    }
}
