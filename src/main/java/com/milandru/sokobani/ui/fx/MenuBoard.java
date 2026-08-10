package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.core.GameState;
import com.milandru.sokobani.core.Level;
import com.milandru.sokobani.persistence.Progress;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class MenuBoard {

    private final List<Level> levels;
    private final Progress progress;
    private final Map<Integer, String> solutions = new HashMap<>();

    private int room;
    private int step;
    private GameState state;

    public MenuBoard(Progress progress, List<Level> levels) {
        this.progress = Objects.requireNonNull(progress, "progress");
        this.levels = List.copyOf(levels);
        if (this.levels.isEmpty()) {
            throw new IllegalArgumentException("a menu board needs at least one level");
        }
        this.room = firstSolvedOrZero();
        this.state = new GameState(this.levels.get(room));
    }

    public synchronized int room() {
        return room;
    }

    public synchronized int step() {
        return step;
    }

    public synchronized GameState state() {
        return state;
    }

    public synchronized String solutionOf(int roomIndex) {
        return solutions.get(roomIndex);
    }

    public boolean solved(int roomIndex) {
        return progress.isSolved(roomIndex);
    }

    public synchronized void attach(int roomIndex, String moves) {
        if (roomIndex < 0 || roomIndex >= levels.size()) {
            return;
        }
        solutions.put(roomIndex, moves);
    }

    public synchronized boolean advance() {
        String moves = solutions.get(room);
        if (moves == null) {
            return false;
        }
        if (step >= moves.length()) {
            advanceToNextRoom();
            return true;
        }
        MenuReplay.advance(state, moves, step, 1);
        step++;
        return true;
    }

    private void advanceToNextRoom() {
        room = nextSolvedAfter(room);
        step = 0;
        state = new GameState(levels.get(room));
    }

    private int nextSolvedAfter(int from) {
        for (int offset = 1; offset <= levels.size(); offset++) {
            int candidate = Math.floorMod(from + offset, levels.size());
            if (progress.isSolved(candidate)) {
                return candidate;
            }
        }
        return from;
    }

    private int firstSolvedOrZero() {
        for (int index = 0; index < levels.size(); index++) {
            if (progress.isSolved(index)) {
                return index;
            }
        }
        return 0;
    }
}
