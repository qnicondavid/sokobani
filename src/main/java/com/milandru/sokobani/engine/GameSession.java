package com.milandru.sokobani.engine;

import com.milandru.sokobani.core.Direction;
import com.milandru.sokobani.core.GameState;
import com.milandru.sokobani.core.Level;
import com.milandru.sokobani.core.MoveResult;
import com.milandru.sokobani.core.Position;
import com.milandru.sokobani.core.SokobanRules;
import com.milandru.sokobani.level.LevelPack;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public final class GameSession {

    private final LevelPack pack;
    private final Deque<MoveRecord> history = new ArrayDeque<>();
    private final List<GameEventListener> listeners = new CopyOnWriteArrayList<>();

    private int levelIndex;
    private GameState state;
    private boolean solvedAnnounced;

    public GameSession(LevelPack pack) {
        this.pack = Objects.requireNonNull(pack, "pack");
        if (pack.size() == 0) {
            throw new IllegalArgumentException("pack " + pack.name() + " has no levels");
        }
        beginLevel(0);
    }

    public void addListener(GameEventListener listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }

    public void removeListener(GameEventListener listener) {
        listeners.remove(listener);
    }

    public MoveResult move(Direction direction) {
        MoveResult result = SokobanRules.apply(state, direction);
        switch (result) {
            case MoveResult.Moved moved -> {
                history.push(MoveRecord.ofMove(direction));
                fire(new GameEvent.Moved(moved.from(), moved.to()));
            }
            case MoveResult.Pushed pushed -> {
                history.push(MoveRecord.ofPush(direction, pushed.boxTo()));
                fire(new GameEvent.Pushed(pushed.from(), pushed.to(), pushed.boxFrom(), pushed.boxTo()));
            }
            case MoveResult.Blocked blocked -> {
            }
        }
        announceSolvedOnEntry();
        return result;
    }

    public boolean undo() {
        if (history.isEmpty()) {
            return false;
        }
        MoveRecord record = history.pop();
        Optional<Position> pulledBox = record.pushedBox();
        Position vacated = state.player();
        if (pulledBox.isPresent()) {
            SokobanRules.revertPush(state, record.direction());
        } else {
            SokobanRules.revertMove(state, record.direction());
        }
        Position returned = state.player();
        fire(pulledBox
                .map(box -> GameEvent.Undone.ofPush(vacated, returned, box, vacated))
                .orElseGet(() -> GameEvent.Undone.ofMove(vacated, returned)));
        announceSolvedOnEntry();
        return true;
    }

    public void restart() {
        beginLevel(levelIndex);
        fire(new GameEvent.Restarted(currentLevel()));
    }

    public void loadLevel(int index) {
        beginLevel(index);
        fire(new GameEvent.LevelLoaded(currentLevel()));
    }

    public boolean nextLevel() {
        if (!hasNextLevel()) {
            return false;
        }
        loadLevel(levelIndex + 1);
        return true;
    }

    public boolean hasNextLevel() {
        return levelIndex + 1 < pack.size();
    }

    public LevelPack pack() {
        return pack;
    }

    public Level currentLevel() {
        return state.level();
    }

    public int levelIndex() {
        return levelIndex;
    }

    public GameState state() {
        return state;
    }

    public boolean isSolved() {
        return state.isSolved();
    }

    public int moveCount() {
        return state.moveCount();
    }

    public int pushCount() {
        return state.pushCount();
    }

    public boolean canUndo() {
        return !history.isEmpty();
    }

    private void beginLevel(int index) {
        Level level = pack.get(index);
        levelIndex = index;
        state = new GameState(level);
        history.clear();
        solvedAnnounced = state.isSolved();
    }

    private void announceSolvedOnEntry() {
        boolean solved = state.isSolved();
        if (solved && !solvedAnnounced) {
            fire(new GameEvent.Solved(currentLevel(), state.moveCount(), state.pushCount()));
        }
        solvedAnnounced = solved;
    }

    private void fire(GameEvent event) {
        for (GameEventListener listener : listeners) {
            listener.onEvent(event);
        }
    }
}
