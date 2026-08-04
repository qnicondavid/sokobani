package com.milandru.sokobani.persistence;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProgressTest {

    @Test
    void empty_freshProgress_hasOnlyLevelZeroUnlockedAndNothingSolved() {
        Progress progress = Progress.empty();

        assertEquals(0, progress.unlockedIndex());
        assertTrue(progress.isUnlocked(0));
        assertFalse(progress.isUnlocked(1));
        assertFalse(progress.isSolved(0));
        assertTrue(progress.levelRecord(0).isEmpty());
    }

    @Test
    void isUnlocked_aNegativeLevel_isFalse() {
        Progress progress = Progress.empty().withSolved(4, 30, 10);

        assertFalse(progress.isUnlocked(-1));
    }

    @Test
    void withSolved_aLevelNeverSolvedBefore_recordsTheScoreAsTheBest() {
        Progress progress = Progress.empty().withSolved(0, 12, 4);

        Progress.LevelRecord record = progress.levelRecord(0).orElseThrow();
        assertTrue(record.solved());
        assertEquals(12, record.bestMoves());
        assertEquals(4, record.bestPushes());
    }

    @Test
    void withSolved_aBetterScoreThanTheExistingBest_replacesIt() {
        Progress progress = Progress.empty()
                .withSolved(0, 20, 8)
                .withSolved(0, 12, 4);

        Progress.LevelRecord record = progress.levelRecord(0).orElseThrow();
        assertEquals(12, record.bestMoves());
        assertEquals(4, record.bestPushes());
    }

    @Test
    void withSolved_aWorseScoreThanTheExistingBest_keepsTheOldBest() {
        Progress progress = Progress.empty()
                .withSolved(0, 12, 4)
                .withSolved(0, 20, 8);

        Progress.LevelRecord record = progress.levelRecord(0).orElseThrow();
        assertEquals(12, record.bestMoves());
        assertEquals(4, record.bestPushes());
    }

    @Test
    void withSolved_eachMetricComparedIndependently_keepsTheBestOfEach() {
        Progress progress = Progress.empty()
                .withSolved(0, 10, 6)
                .withSolved(0, 14, 2);

        Progress.LevelRecord record = progress.levelRecord(0).orElseThrow();
        assertEquals(10, record.bestMoves());
        assertEquals(2, record.bestPushes());
    }

    @Test
    void withSolved_aLevel_advancesTheUnlockedIndexPastIt() {
        Progress progress = Progress.empty().withSolved(2, 30, 10);

        assertEquals(3, progress.unlockedIndex());
        assertTrue(progress.isUnlocked(3));
        assertFalse(progress.isUnlocked(4));
    }

    @Test
    void withSolved_aLevelBehindTheUnlockedFrontier_neverMovesTheFrontierBackwards() {
        Progress progress = Progress.empty()
                .withSolved(4, 30, 10)
                .withSolved(0, 5, 1);

        assertEquals(5, progress.unlockedIndex());
    }

    @Test
    void withSolved_replayingASolvedLevel_leavesEarlierLevelsUntouched() {
        Progress progress = Progress.empty()
                .withSolved(0, 12, 4)
                .withSolved(1, 20, 6)
                .withSolved(0, 8, 2);

        Progress.LevelRecord levelZero = progress.levelRecord(0).orElseThrow();
        Progress.LevelRecord levelOne = progress.levelRecord(1).orElseThrow();
        assertEquals(8, levelZero.bestMoves());
        assertEquals(20, levelOne.bestMoves());
    }

    @Test
    void withSolved_negativeLevel_throwsIllegalArgument() {
        Progress progress = Progress.empty();

        assertThrows(IllegalArgumentException.class, () -> progress.withSolved(-1, 1, 1));
    }

    @Test
    void constructor_negativeUnlockedIndex_clampsToZero() {
        Progress progress = new Progress(Map.of(), -5);

        assertEquals(0, progress.unlockedIndex());
        assertTrue(progress.isUnlocked(0));
    }

    @Test
    void withSolved_anUnsolvedRecordAlreadyPresent_isTreatedAsNoPriorBest() {
        Progress.LevelRecord unsolved = new Progress.LevelRecord(false, 0, 0);
        Progress progress = new Progress(Map.of(0, unsolved), 0)
                .withSolved(0, 40, 10);

        Progress.LevelRecord record = progress.levelRecord(0).orElseThrow();
        assertEquals(40, record.bestMoves());
        assertEquals(10, record.bestPushes());
    }
}
