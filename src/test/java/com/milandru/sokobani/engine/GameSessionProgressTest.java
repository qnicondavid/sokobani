package com.milandru.sokobani.engine;

import com.milandru.sokobani.core.Direction;
import com.milandru.sokobani.level.LevelPack;
import com.milandru.sokobani.persistence.Progress;
import com.milandru.sokobani.persistence.ProgressStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameSessionProgressTest {

    @TempDir
    Path tempDir;

    @Test
    void constructor_noProgressStore_stillPlaysNormally() {
        GameSession session = PackFixture.session(PackFixture.ONE_PUSH);

        session.move(Direction.RIGHT);
        session.move(Direction.RIGHT);
        session.move(Direction.RIGHT);

        assertTrue(session.isSolved());
        assertEquals(3, session.pushCount());
    }

    @Test
    void constructor_firstRunWithNoFile_onlyLevelZeroIsUnlocked() {
        ProgressStore store = new ProgressStore(tempDir);
        LevelPack pack = PackFixture.pack(PackFixture.ONE_PUSH, PackFixture.TWO_BOXES, PackFixture.OPEN_ROOM);

        GameSession session = new GameSession(pack, store);

        assertEquals(0, session.levelIndex());
        assertTrue(session.progress().isUnlocked(0));
        assertFalse(session.progress().isUnlocked(1));
        assertFalse(session.progress().isUnlocked(2));
    }

    @Test
    void move_theMoveThatSolvesTheLevel_unlocksTheNextLevel() {
        LevelPack pack = PackFixture.pack(PackFixture.ONE_PUSH, PackFixture.TWO_BOXES);
        GameSession session = new GameSession(pack);

        session.move(Direction.RIGHT);
        session.move(Direction.RIGHT);
        session.move(Direction.RIGHT);

        assertTrue(session.progress().isSolved(0));
        assertTrue(session.progress().isUnlocked(1));
    }

    @Test
    void move_theMoveThatSolvesTheLevel_recordsTheMoveAndPushCountAsTheBest() {
        LevelPack pack = PackFixture.pack(PackFixture.ONE_PUSH);
        GameSession session = new GameSession(pack);

        session.move(Direction.RIGHT);
        session.move(Direction.RIGHT);
        session.move(Direction.RIGHT);

        Progress.LevelRecord record = session.progress().levelRecord(0).orElseThrow();
        assertEquals(3, record.bestMoves());
        assertEquals(3, record.bestPushes());
    }

    @Test
    void move_theMoveThatSolvesTheLevel_savesImmediately() {
        ProgressStore store = new ProgressStore(tempDir);
        LevelPack pack = PackFixture.pack(PackFixture.ONE_PUSH);
        GameSession session = new GameSession(pack, store);

        session.move(Direction.RIGHT);
        session.move(Direction.RIGHT);
        session.move(Direction.RIGHT);

        assertEquals(session.progress(), store.load());
    }

    @Test
    void constructor_withAStoreHoldingPriorProgress_loadsItOnConstruction() {
        ProgressStore store = new ProgressStore(tempDir);
        store.save(Progress.empty().withSolved(0, 3, 3));
        LevelPack pack = PackFixture.pack(PackFixture.ONE_PUSH, PackFixture.TWO_BOXES);

        GameSession session = new GameSession(pack, store);

        assertTrue(session.progress().isUnlocked(1));
        session.loadLevel(1);
        assertEquals(1, session.levelIndex());
    }

    @Test
    void loadLevel_aLockedIndex_throwsIllegalStateAndLeavesTheSessionWhereItWas() {
        LevelPack pack = PackFixture.pack(PackFixture.ONE_PUSH, PackFixture.TWO_BOXES, PackFixture.OPEN_ROOM);
        GameSession session = new GameSession(pack);

        assertThrows(IllegalStateException.class, () -> session.loadLevel(2));

        assertEquals(0, session.levelIndex());
    }

    @Test
    void loadLevel_aLockedIndex_namesTheLockedLevelAndTheFurthestOneUnlocked() {
        LevelPack pack = new LevelPack("fixture", List.of(
                PackFixture.level(PackFixture.ONE_PUSH, "Opening", 0),
                PackFixture.level(PackFixture.TWO_BOXES, "Corner", 1),
                PackFixture.level(PackFixture.OPEN_ROOM, "Alcove", 2)));
        GameSession session = new GameSession(pack);

        IllegalStateException locked = assertThrows(IllegalStateException.class, () -> session.loadLevel(2));

        assertEquals("Alcove is locked; the furthest level unlocked is Opening", locked.getMessage());
    }

    @Test
    void loadLevel_theLevelJustUnlockedBySolvingItsPredecessor_succeeds() {
        LevelPack pack = PackFixture.pack(PackFixture.ONE_PUSH, PackFixture.TWO_BOXES);
        GameSession session = new GameSession(pack);
        session.move(Direction.RIGHT);
        session.move(Direction.RIGHT);
        session.move(Direction.RIGHT);

        session.loadLevel(1);

        assertEquals(1, session.levelIndex());
    }

    @Test
    void loadLevel_indexPastTheEndOfThePack_stillThrowsIndexOutOfBoundsBeforeCheckingTheLock() {
        GameSession session = PackFixture.session(PackFixture.ONE_PUSH);

        assertThrows(IndexOutOfBoundsException.class, () -> session.loadLevel(5));
    }

    @Test
    void lastSaveSucceeded_noProgressStore_isTrue() {
        GameSession session = PackFixture.session(PackFixture.ONE_PUSH);

        PackFixture.solveOnePush(session);

        assertTrue(session.lastSaveSucceeded());
    }

    @Test
    void lastSaveSucceeded_savingToAWritableStore_isTrue() {
        ProgressStore store = new ProgressStore(tempDir);
        GameSession session = new GameSession(PackFixture.pack(PackFixture.ONE_PUSH), store);

        session.move(Direction.RIGHT);
        session.move(Direction.RIGHT);
        session.move(Direction.RIGHT);

        assertTrue(session.lastSaveSucceeded());
    }

    @Test
    void lastSaveSucceeded_savingToAnUnwritableStore_isFalseButDoesNotThrow() throws java.io.IOException {
        java.nio.file.Path blockingFile = tempDir.resolve("blocking-file");
        java.nio.file.Files.writeString(blockingFile, "not a directory");
        ProgressStore store = new ProgressStore(blockingFile.resolve("nested"));
        GameSession session = new GameSession(PackFixture.pack(PackFixture.ONE_PUSH), store);

        session.move(Direction.RIGHT);
        session.move(Direction.RIGHT);
        session.move(Direction.RIGHT);

        assertTrue(session.isSolved());
        assertFalse(session.lastSaveSucceeded());
    }
}
