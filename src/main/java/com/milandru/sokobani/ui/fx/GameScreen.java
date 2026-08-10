package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.core.Deadlock;
import com.milandru.sokobani.core.Direction;
import com.milandru.sokobani.core.Position;
import com.milandru.sokobani.core.Tile;
import com.milandru.sokobani.engine.GameEvent;
import com.milandru.sokobani.engine.GameEventListener;
import com.milandru.sokobani.engine.GameSession;
import com.milandru.sokobani.persistence.Progress;
import com.milandru.sokobani.ui.Surface;
import com.milandru.sokobani.ui.TypeSetter;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class GameScreen extends BaseScreen {

    private static final long TWEEN_DURATION_NANOS = 100_000_000L;
    private static final double FRAME_MILLIS = 16.0;

    private final GameSession session;
    private final TypeSetter setter;
    private final Screens screens;
    private final SettingsState settings;
    private final SoundBank sound;
    private final GameEventListener listener = this::onEvent;
    private final Timeline timeline;

    private Optional<Progress.LevelRecord> bestBeforeThisAttempt = Optional.empty();
    private Optional<Tween> tween = Optional.empty();

    public GameScreen(GameSession session, TypeSetter setter, ScreenController controller, Screens screens,
                      SettingsState settings, SoundBank sound) {
        super(controller);
        this.session = Objects.requireNonNull(session, "session");
        this.setter = Objects.requireNonNull(setter, "setter");
        this.screens = Objects.requireNonNull(screens, "screens");
        this.settings = Objects.requireNonNull(settings, "settings");
        this.sound = Objects.requireNonNull(sound, "sound");
        this.timeline = new Timeline(new KeyFrame(Duration.millis(FRAME_MILLIS), event -> redrawWhileAnimating()));
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    @Override
    public void shown() {
        session.addListener(listener);
        if (resyncWithTheSession()) {
            return;
        }
        redraw();
        timeline.play();
    }

    @Override
    public void hidden() {
        timeline.stop();
        session.removeListener(listener);
        tween = Optional.empty();
    }

    @Override
    public void onKeyPressed(KeyEvent event) {
        apply(GameControls.commandFor(GameControls.Mode.PLAYING, event.getCode(),
                event.isShortcutDown() || event.isControlDown()));
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
            case PAUSE -> controller().show(screens.pause());
            case CYCLE_THEME -> cycleTheme();
            case MOVE_UP, MOVE_DOWN, MOVE_LEFT, MOVE_RIGHT, RESUME, NEXT_ROOM, CONFIRM, BACK, IGNORED -> {
            }
        }
    }

    private void onEvent(GameEvent event) {
        if (GameControls.beginsAnAttempt(event)) {
            beginAttempt();
        }
        if (event instanceof GameEvent.Solved won) {
            sound.solved();
            screens.win().present(new WinData(won, bestBeforeThisAttempt));
            controller().show(screens.win());
            return;
        }
        tween = Tween.of(event, System.nanoTime(), tweenDurationNanos());
        playFor(event);
        redraw();
    }

    private void playFor(GameEvent event) {
        if (event instanceof GameEvent.Moved) {
            sound.move();
        } else if (event instanceof GameEvent.Pushed pushed) {
            sound.push();
            if (session.state().level().tileAt(pushed.boxTo()) == Tile.GOAL) {
                sound.goal();
            }
        } else if (event instanceof GameEvent.Undone undone) {
            if (undone.isPush()) {
                sound.push();
            } else {
                sound.move();
            }
        }
    }

    private long tweenDurationNanos() {
        return settings.animationEnabled() ? TWEEN_DURATION_NANOS : 0;
    }

    private void redrawWhileAnimating() {
        if (tween.isPresent()) {
            redraw();
        }
    }

    private void beginAttempt() {
        bestBeforeThisAttempt = session.progress()
                .levelRecord(session.levelIndex())
                .filter(Progress.LevelRecord::solved);
    }

    private boolean resyncWithTheSession() {
        beginAttempt();
        if (!session.isSolved()) {
            return false;
        }
        screens.win().present(new WinData(
                new GameEvent.Solved(session.currentLevel(), session.moveCount(), session.pushCount()),
                session.progress().levelRecord(session.levelIndex()).filter(Progress.LevelRecord::solved)));
        controller().show(screens.win());
        return true;
    }

    @Override
    protected Surface renderBase() {
        long now = System.nanoTime();
        if (tween.isPresent() && tween.orElseThrow().finished(now)) {
            tween = Optional.empty();
        }
        Set<Position> deadlocked = settings.hintsEnabled()
                ? Deadlock.deadlockedBoxes(session.state())
                : Set.of();
        return GameView.render(session, setter, deadlocked, tween, now);
    }
}
