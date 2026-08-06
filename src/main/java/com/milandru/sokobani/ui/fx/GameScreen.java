package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.core.Direction;
import com.milandru.sokobani.engine.GameEvent;
import com.milandru.sokobani.engine.GameEventListener;
import com.milandru.sokobani.engine.GameSession;
import com.milandru.sokobani.persistence.Progress;
import com.milandru.sokobani.ui.Display;
import com.milandru.sokobani.ui.Scaling;
import com.milandru.sokobani.ui.Surface;
import com.milandru.sokobani.ui.Theme;
import com.milandru.sokobani.ui.Threshold;
import com.milandru.sokobani.ui.TypeSetter;

import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;

import java.util.Objects;
import java.util.Optional;

public final class GameScreen implements Screen {

    private final GameSession session;
    private final TypeSetter setter;
    private final ScreenController controller;
    private final ThemeStore themes;
    private final GameEventListener listener = this::onEvent;

    private final StackPane holder = new StackPane();
    private final Canvas canvas = new Canvas();

    private Theme theme;
    private Optional<GameEvent.Solved> solved = Optional.empty();
    private Optional<Progress.LevelRecord> bestBeforeThisAttempt = Optional.empty();
    private boolean paused;

    public GameScreen(
            GameSession session,
            TypeSetter setter,
            ScreenController controller,
            ThemeStore themes,
            Theme theme) {
        this.session = Objects.requireNonNull(session, "session");
        this.setter = Objects.requireNonNull(setter, "setter");
        this.controller = Objects.requireNonNull(controller, "controller");
        this.themes = Objects.requireNonNull(themes, "themes");
        this.theme = Objects.requireNonNull(theme, "theme");

        holder.setMinSize(0, 0);
        holder.getChildren().add(canvas);
        canvas.widthProperty().bind(holder.widthProperty());
        canvas.heightProperty().bind(holder.heightProperty());
        canvas.widthProperty().addListener(resized -> redraw());
        canvas.heightProperty().addListener(resized -> redraw());
    }

    @Override
    public Parent node() {
        return holder;
    }

    @Override
    public void shown() {
        session.addListener(listener);
        resyncWithTheSession();
        redraw();
    }

    @Override
    public void hidden() {
        session.removeListener(listener);
    }

    @Override
    public void onKeyPressed(KeyEvent event) {
        apply(GameControls.commandFor(
                mode(), event.getCode(), event.isShortcutDown() || event.isControlDown()));
    }

    private GameControls.Mode mode() {
        if (solved.isPresent()) {
            return GameControls.Mode.SOLVED;
        }
        return paused ? GameControls.Mode.PAUSED : GameControls.Mode.PLAYING;
    }

    private void apply(GameControls.Command command) {
        Optional<Direction> direction = GameControls.directionOf(command);
        if (direction.isPresent()) {
            session.move(direction.orElseThrow());
            return;
        }
        switch (command) {
            case UNDO -> session.undo();
            case RESTART -> session.restart();
            case PAUSE -> setPaused(true);
            case RESUME -> setPaused(false);
            case NEXT_ROOM -> session.nextLevel();
            case CYCLE_THEME -> cycleTheme();
            case MOVE_UP, MOVE_DOWN, MOVE_LEFT, MOVE_RIGHT, IGNORED -> {
            }
        }
    }

    private void onEvent(GameEvent event) {
        if (GameControls.beginsAnAttempt(event)) {
            beginAttempt();
        }
        if (event instanceof GameEvent.Solved won) {
            solved = Optional.of(won);
        }
        redraw();
    }

    private void beginAttempt() {
        solved = Optional.empty();
        paused = false;
        bestBeforeThisAttempt = session.progress()
                .levelRecord(session.levelIndex())
                .filter(Progress.LevelRecord::solved);
    }

    private void resyncWithTheSession() {
        beginAttempt();
        if (session.isSolved()) {
            solved = Optional.of(new GameEvent.Solved(
                    session.currentLevel(), session.moveCount(), session.pushCount()));
        }
    }

    private void setPaused(boolean wanted) {
        paused = wanted;
        redraw();
    }

    private void cycleTheme() {
        theme = Theme.ALL.get(Math.floorMod(Theme.ALL.indexOf(theme) + 1, Theme.ALL.size()));
        themes.save(theme);
        redraw();
    }

    private void redraw() {
        if (canvas.getWidth() <= 0 || canvas.getHeight() <= 0) {
            return;
        }
        Surface base = GameView.render(session, setter);
        int factor = Scaling.factor(
                (int) canvas.getWidth(), (int) canvas.getHeight(), base.width(), base.height(), GameView.VIEW_MARGIN);
        present(canvas, base, factor);
        presentOverlay(factor);
    }

    private void presentOverlay(int factor) {
        Optional<Surface> panel = overlayPanel();
        if (panel.isEmpty()) {
            controller.hideOverlay();
            return;
        }
        Surface surface = panel.orElseThrow();
        Canvas overlay = new Canvas((double) surface.width() * factor, (double) surface.height() * factor);
        present(overlay, surface, factor);
        controller.showOverlay(overlay);
    }

    private Optional<Surface> overlayPanel() {
        if (solved.isPresent()) {
            return Optional.of(Panels.solved(
                    solved.orElseThrow(),
                    bestBeforeThisAttempt,
                    session.hasNextLevel(),
                    session.lastSaveSucceeded(),
                    setter));
        }
        if (paused) {
            return Optional.of(Panels.paused(setter));
        }
        return Optional.empty();
    }

    private void present(Canvas target, Surface surface, int factor) {
        Display.present(
                target, theme, Threshold.paletteMap(surface, theme), surface.width(), surface.height(), factor);
    }
}
