package com.milandru.sokobani.persistence;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public record Progress(Map<Integer, LevelRecord> levels, int unlockedIndex) {

    public Progress {
        levels = Map.copyOf(levels);
        unlockedIndex = Math.max(0, unlockedIndex);
    }

    public static Progress empty() {
        return new Progress(Map.of(), 0);
    }

    public Progress withSolved(int level, int moves, int pushes) {
        if (level < 0) {
            throw new IllegalArgumentException("level " + level + " is negative");
        }
        LevelRecord existing = levels.get(level);
        boolean hasPriorBest = existing != null && existing.solved();
        int bestMoves = hasPriorBest ? Math.min(existing.bestMoves(), moves) : moves;
        int bestPushes = hasPriorBest ? Math.min(existing.bestPushes(), pushes) : pushes;
        Map<Integer, LevelRecord> updated = new HashMap<>(levels);
        updated.put(level, new LevelRecord(true, bestMoves, bestPushes));
        return new Progress(updated, Math.max(unlockedIndex, level + 1));
    }

    public boolean isSolved(int level) {
        LevelRecord record = levels.get(level);
        return record != null && record.solved();
    }

    public Optional<LevelRecord> levelRecord(int level) {
        return Optional.ofNullable(levels.get(level));
    }

    public boolean isUnlocked(int level) {
        return level >= 0 && level <= unlockedIndex;
    }

    public record LevelRecord(boolean solved, int bestMoves, int bestPushes) {
    }
}
