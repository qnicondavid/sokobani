package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.engine.GameSession;
import com.milandru.sokobani.ui.Surface;
import com.milandru.sokobani.ui.TypeSetter;

import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.util.List;
import java.util.Objects;

public final class PauseScreen extends BaseScreen {

    private final GameSession session;
    private final TypeSetter setter;
    private final Screens screens;
    private final Selection<PauseView.Item> selection = new Selection<>(List.of(PauseView.Item.values()));

    public PauseScreen(GameSession session, TypeSetter setter, ScreenController controller, Screens screens) {
        super(controller);
        this.session = Objects.requireNonNull(session, "session");
        this.setter = Objects.requireNonNull(setter, "setter");
        this.screens = Objects.requireNonNull(screens, "screens");
        holder().setOnMouseMoved(this::onMouseMoved);
        holder().setOnMouseClicked(this::onMouseClicked);
    }

    @Override
    public void shown() {
        redraw();
    }

    @Override
    public void onKeyPressed(KeyEvent event) {
        switch (GameControls.commandFor(GameControls.Mode.PAUSED, event.getCode(),
                event.isShortcutDown() || event.isControlDown())) {
            case MOVE_UP -> selection.move(-1);
            case MOVE_DOWN -> selection.move(1);
            case CONFIRM -> confirm();
            case RESUME -> controller().show(screens.game());
            case RESTART -> {
                session.restart();
                controller().show(screens.game());
            }
            case CYCLE_THEME -> cycleTheme();
            case MOVE_LEFT, MOVE_RIGHT, UNDO, PAUSE, NEXT_ROOM, BACK, IGNORED -> {
                return;
            }
        }
        redraw();
    }

    private void onMouseMoved(MouseEvent event) {
        int previous = selection.index();
        for (PauseView.Item item : PauseView.Item.values()) {
            if (PauseView.overItem(baseX(event.getX()), baseY(event.getY()), item.ordinal())) {
                selection.select(item.ordinal());
                break;
            }
        }
        if (selection.index() != previous) {
            redraw();
        }
    }

    private void onMouseClicked(MouseEvent event) {
        for (PauseView.Item item : PauseView.Item.values()) {
            if (PauseView.overItem(baseX(event.getX()), baseY(event.getY()), item.ordinal())) {
                selection.select(item.ordinal());
                confirm();
                return;
            }
        }
    }

    private void confirm() {
        switch (selection.selected()) {
            case RESUME -> controller().show(screens.game());
            case RESTART -> {
                session.restart();
                controller().show(screens.game());
            }
            case ROOMS -> controller().show(screens.rooms());
            case MAIN_MENU -> controller().show(screens.menu());
        }
    }

    @Override
    protected Surface renderBase() {
        Surface surface = new Surface(PauseView.WIDTH, PauseView.HEIGHT);
        PauseView.render(surface, setter, session.state(), selection.index());
        return surface;
    }
}
