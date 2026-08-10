package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.engine.GameSession;
import com.milandru.sokobani.ui.Surface;
import com.milandru.sokobani.ui.TypeSetter;

import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.util.Objects;
import java.util.Optional;

public final class RoomsScreen extends BaseScreen {

    private final GameSession session;
    private final TypeSetter setter;
    private final Screens screens;

    private RoomsGrid grid;

    public RoomsScreen(GameSession session, TypeSetter setter, ScreenController controller, Screens screens) {
        super(controller);
        this.session = Objects.requireNonNull(session, "session");
        this.setter = Objects.requireNonNull(setter, "setter");
        this.screens = Objects.requireNonNull(screens, "screens");
        holder().setOnMouseMoved(this::onMouseMoved);
        holder().setOnMouseClicked(this::onMouseClicked);
    }

    @Override
    public void shown() {
        grid = new RoomsGrid(session.progress(), session.pack().size());
        redraw();
    }

    @Override
    public void onKeyPressed(KeyEvent event) {
        switch (GameControls.commandFor(GameControls.Mode.ROOMS, event.getCode(),
                event.isShortcutDown() || event.isControlDown())) {
            case MOVE_UP -> grid.moveUp();
            case MOVE_DOWN -> grid.moveDown();
            case MOVE_LEFT -> grid.moveLeft();
            case MOVE_RIGHT -> grid.moveRight();
            case PAGE_UP -> grid.flipPage(-1);
            case PAGE_DOWN -> grid.flipPage(1);
            case CONFIRM -> start(grid.confirm());
            case BACK -> controller().show(screens.menu());
            case CYCLE_THEME -> cycleTheme();
            case UNDO, RESTART, PAUSE, RESUME, NEXT_ROOM, IGNORED -> {
                return;
            }
        }
        redraw();
    }

    private void onMouseMoved(MouseEvent event) {
        int plate = RoomsView.plateAt(baseX(event.getX()), baseY(event.getY()), grid.page());
        if (plate >= 0 && grid.select(plate)) {
            redraw();
        }
    }

    private void onMouseClicked(MouseEvent event) {
        int plate = RoomsView.plateAt(baseX(event.getX()), baseY(event.getY()), grid.page());
        if (plate >= 0 && grid.select(plate)) {
            start(grid.confirm());
        }
    }

    private void start(Optional<Integer> chosen) {
        chosen.ifPresent(index -> {
            session.loadLevel(index);
            controller().show(screens.game());
        });
    }

    @Override
    protected Surface renderBase() {
        Surface surface = new Surface(RoomsView.WIDTH, RoomsView.HEIGHT);
        RoomsView.render(surface, setter, session.pack(), session.progress(), grid.selection(), grid.page());
        return surface;
    }
}
