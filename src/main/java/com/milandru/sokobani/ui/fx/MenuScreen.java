package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.engine.GameSession;
import com.milandru.sokobani.persistence.Progress;
import com.milandru.sokobani.solve.Solver;
import com.milandru.sokobani.ui.Surface;
import com.milandru.sokobani.ui.TypeSetter;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class MenuScreen extends BaseScreen {

    private static final long STEP_MILLIS = 400;

    private final GameSession session;
    private final TypeSetter setter;
    private final Screens screens;
    private final Selection<MenuView.Item> selection = new Selection<>(List.of(
            MenuView.Item.PLAY, MenuView.Item.ROOMS, MenuView.Item.HOW_TO_PLAY, MenuView.Item.SETTINGS,
            MenuView.Item.QUIT));

    private final Map<Integer, String> solutions = new ConcurrentHashMap<>();

    private volatile MenuBoard board;
    private volatile boolean showing;
    private Timeline timeline;

    public MenuScreen(GameSession session, TypeSetter setter, ScreenController controller, Screens screens) {
        super(controller);
        this.session = Objects.requireNonNull(session, "session");
        this.setter = Objects.requireNonNull(setter, "setter");
        this.screens = Objects.requireNonNull(screens, "screens");
        holder().setOnMouseMoved(this::onMouseMoved);
        holder().setOnMouseClicked(this::onMouseClicked);
    }

    @Override
    public void shown() {
        MenuBoard fresh = new MenuBoard(session.progress(), session.pack().levels());
        solutions.forEach(fresh::attach);
        board = fresh;
        showing = true;
        Thread solver = new Thread(() -> solveOn(fresh));
        solver.setDaemon(true);
        solver.start();
        timeline = new Timeline(new KeyFrame(Duration.millis(STEP_MILLIS), event -> {
            if (board.advance()) {
                redraw();
            }
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
        redraw();
    }

    @Override
    public void hidden() {
        showing = false;
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
    }

    @Override
    public void onKeyPressed(KeyEvent event) {
        switch (GameControls.commandFor(GameControls.Mode.MENU, event.getCode(),
                event.isShortcutDown() || event.isControlDown())) {
            case MOVE_UP -> selection.move(-1);
            case MOVE_DOWN -> selection.move(1);
            case CONFIRM -> confirm();
            case CYCLE_THEME -> cycleTheme();
            case MOVE_LEFT, MOVE_RIGHT, UNDO, RESTART, PAUSE, RESUME, NEXT_ROOM, BACK, IGNORED -> {
                return;
            }
        }
        redraw();
    }

    private void onMouseMoved(MouseEvent event) {
        int previous = selection.index();
        for (MenuView.Item item : MenuView.Item.values()) {
            if (MenuView.overItem(baseX(event.getX()), baseY(event.getY()), item.ordinal())) {
                selection.select(item.ordinal());
                break;
            }
        }
        if (selection.index() != previous) {
            redraw();
        }
    }

    private void onMouseClicked(MouseEvent event) {
        for (MenuView.Item item : MenuView.Item.values()) {
            if (MenuView.overItem(baseX(event.getX()), baseY(event.getY()), item.ordinal())) {
                selection.select(item.ordinal());
                confirm();
                return;
            }
        }
    }

    private void confirm() {
        switch (selection.selected()) {
            case PLAY -> play();
            case ROOMS -> controller().show(screens.rooms());
            case HOW_TO_PLAY -> controller().show(screens.howTo());
            case SETTINGS -> controller().show(screens.settings());
            case QUIT -> Platform.exit();
        }
    }

    private void play() {
        Progress progress = session.progress();
        session.loadLevel(Math.min(progress.unlockedIndex(), session.pack().size() - 1));
        controller().show(screens.game());
    }

    private void solveOn(MenuBoard target) {
        for (int index = 0; index < session.pack().size(); index++) {
            if (!showing || board != target) {
                return;
            }
            if (!target.solved(index) || solutions.containsKey(index)) {
                continue;
            }
            try {
                int roomIndex = index;
                Solver.solve(session.pack().get(index)).ifPresent(solution -> {
                    solutions.put(roomIndex, solution.moves());
                    target.attach(roomIndex, solution.moves());
                });
            } catch (RuntimeException skipped) {
            }
        }
    }

    @Override
    protected Surface renderBase() {
        Surface surface = new Surface(MenuView.WIDTH, MenuView.HEIGHT);
        MenuView.render(surface, setter, board.state(), session.progress(), session.pack().size(), selection.index());
        return surface;
    }
}
