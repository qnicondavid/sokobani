package com.milandru.sokobani.ui.console;

import com.milandru.sokobani.core.GameState;
import com.milandru.sokobani.core.Position;
import com.milandru.sokobani.engine.GameSession;
import com.milandru.sokobani.persistence.ProgressStore;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsoleGameTest {

    @TempDir
    Path tempDir;

    @Test
    void run_movesAndPushes_driveTheSession() {
        GameSession session = ConsoleFixture.session(ConsoleFixture.ONE_PUSH);

        ConsoleFixture.play(session, "d\nd\nq\n");

        assertAll(
                () -> assertEquals(2, session.moveCount()),
                () -> assertEquals(2, session.pushCount()),
                () -> assertEquals(new Position(1, 3), session.state().player()));
    }

    @Test
    void run_undo_takesTheLastMoveBack() {
        GameSession session = ConsoleFixture.session(ConsoleFixture.ONE_PUSH);

        String output = ConsoleFixture.play(session, "d\nd\nu\nq\n");

        assertAll(
                () -> assertEquals(1, session.moveCount()),
                () -> assertEquals(1, session.pushCount()),
                () -> assertEquals(new Position(1, 2), session.state().player()),
                () -> assertTrue(output.contains("Undone.")));
    }

    @Test
    void run_restart_returnsTheLevelToItsOpeningState() {
        GameSession session = ConsoleFixture.session(ConsoleFixture.ONE_PUSH);

        String output = ConsoleFixture.play(session, "d\nd\nr\nq\n");

        assertAll(
                () -> assertEquals(new GameState(session.currentLevel()), session.state()),
                () -> assertFalse(session.canUndo()),
                () -> assertTrue(output.contains("Restarted First.")));
    }

    @Test
    void run_anUnknownCommand_isRejectedAndLeavesTheSessionUnchanged() {
        GameSession session = ConsoleFixture.session(ConsoleFixture.ONE_PUSH);

        String output = ConsoleFixture.play(session, "d\nzz\nq\n");

        assertAll(
                () -> assertEquals(1, session.moveCount()),
                () -> assertEquals(new Position(1, 2), session.state().player()),
                () -> assertTrue(output.contains("Unknown command 'zz'.")),
                () -> assertTrue(output.contains(ConsoleGame.COMMANDS)));
    }

    @Test
    void run_quit_returnsWithoutExitingTheJvmOrReadingFurtherInput() {
        GameSession session = ConsoleFixture.session(ConsoleFixture.ONE_PUSH);

        ConsoleFixture.play(session, "q\nd\nd\n");

        assertEquals(0, session.moveCount());
    }

    @Test
    void run_inputThatRunsOutWithoutQuitting_returnsAtTheEndOfTheStream() {
        GameSession session = ConsoleFixture.session(ConsoleFixture.ONE_PUSH);

        ConsoleFixture.play(session, "d\n");

        assertEquals(1, session.moveCount());
    }

    @Test
    void run_aBlankLine_isIgnored() {
        GameSession session = ConsoleFixture.session(ConsoleFixture.ONE_PUSH);

        String output = ConsoleFixture.play(session, "\n   \nd\nq\n");

        assertAll(
                () -> assertEquals(1, session.moveCount()),
                () -> assertFalse(output.contains("Unknown command")));
    }

    @Test
    void run_theLevelList_marksLockedLevelsAndShowsBestScores() {
        GameSession session = ConsoleFixture.session(
                ConsoleFixture.ONE_PUSH, ConsoleFixture.TWO_PUSHES, ConsoleFixture.BOX_ON_A_GOAL);

        String output = ConsoleFixture.play(session, "d\nd\nd\nl\nq\n");

        assertAll(
                () -> assertTrue(output.contains("Levels in fixture:")),
                () -> assertTrue(output.contains("> 1  First   3/3")),
                () -> assertTrue(output.contains("  2  Second  none")),
                () -> assertTrue(output.contains("  3  Third   " + ConsoleGame.LOCKED)));
    }

    @Test
    void run_choosingALockedLevel_isRefusedWithTheEnginesOwnReason() {
        GameSession session = ConsoleFixture.session(ConsoleFixture.ONE_PUSH, ConsoleFixture.TWO_PUSHES);
        String reason = assertThrows(IllegalStateException.class, () -> session.loadLevel(1)).getMessage();

        String output = ConsoleFixture.play(session, "2\nq\n");

        assertAll(
                () -> assertEquals(0, session.levelIndex()),
                () -> assertTrue(output.contains(reason)));
    }

    @Test
    void run_theNextLevelBeforeSolvingThisOne_isRefusedWithTheEnginesOwnReason() {
        GameSession session = ConsoleFixture.session(ConsoleFixture.ONE_PUSH, ConsoleFixture.TWO_PUSHES);
        String reason = assertThrows(IllegalStateException.class, () -> session.loadLevel(1)).getMessage();

        String output = ConsoleFixture.play(session, "n\nq\n");

        assertAll(
                () -> assertEquals(0, session.levelIndex()),
                () -> assertTrue(output.contains(reason)));
    }

    @Test
    void run_aLevelNumberOutsideThePack_isRejectedWithoutTouchingTheEngine() {
        GameSession session = ConsoleFixture.session(ConsoleFixture.ONE_PUSH);

        String output = ConsoleFixture.play(session, "0\n9\nq\n");

        assertAll(
                () -> assertEquals(0, session.levelIndex()),
                () -> assertTrue(output.contains("There is no level 0 in fixture.")),
                () -> assertTrue(output.contains("There is no level 9 in fixture.")));
    }

    @Test
    void run_choosingAnUnlockedLevelAlreadyPlayed_startsItAgain() {
        GameSession session = ConsoleFixture.session(ConsoleFixture.ONE_PUSH, ConsoleFixture.TWO_PUSHES);

        String output = ConsoleFixture.play(session, "d\nd\nd\nn\n1\nq\n");

        assertAll(
                () -> assertEquals(0, session.levelIndex()),
                () -> assertEquals(0, session.moveCount()),
                () -> assertTrue(output.contains("Now playing First.")));
    }

    @Test
    void run_solvingALevel_reportsTheStatsAndOffersTheNextLevel() {
        GameSession session = ConsoleFixture.session(ConsoleFixture.ONE_PUSH, ConsoleFixture.TWO_PUSHES);

        String output = ConsoleFixture.play(session, "d\nd\nd\nq\n");

        assertAll(
                () -> assertTrue(output.contains("Solved First in 3 moves and 3 pushes.")),
                () -> assertTrue(output.contains("Press n for the next level, or r to play this one again.")));
    }

    @Test
    void run_solvingTheOnlyLevelInThePack_saysThereIsNoNextOne() {
        GameSession session = ConsoleFixture.session(ConsoleFixture.ONE_PUSH);

        String output = ConsoleFixture.play(session, "d\nd\nd\nq\n");

        assertAll(
                () -> assertTrue(output.contains("Solved First in 3 moves and 3 pushes.")),
                () -> assertTrue(output.contains("That was the last level in fixture.")));
    }

    @Test
    void run_theNextLevelAfterSolving_startsTheFollowingOne() {
        GameSession session = ConsoleFixture.session(ConsoleFixture.ONE_PUSH, ConsoleFixture.TWO_PUSHES);

        String output = ConsoleFixture.play(session, "d\nd\nd\nn\nq\n");

        assertAll(
                () -> assertEquals(1, session.levelIndex()),
                () -> assertTrue(output.contains("Now playing Second.")),
                () -> assertTrue(output.contains("Level 2/2  Second")));
    }

    @Test
    void run_theNextLevelWhenThisIsTheLastOne_saysThereIsNoneLeft() {
        GameSession session = ConsoleFixture.session(ConsoleFixture.ONE_PUSH);

        String output = ConsoleFixture.play(session, "d\nd\nd\nn\nq\n");

        assertAll(
                () -> assertEquals(0, session.levelIndex()),
                () -> assertTrue(output.contains("This is the last level in fixture.")));
    }

    @Test
    void run_aPushWithAWallBehindTheBox_saysWhichObstacleStoppedIt() {
        GameSession session = ConsoleFixture.session(ConsoleFixture.ONE_PUSH);

        String output = ConsoleFixture.play(session, "d\nd\nd\nd\nq\n");

        assertAll(
                () -> assertEquals(3, session.pushCount()),
                () -> assertTrue(output.contains("That box is against a wall.")));
    }

    @Test
    void run_aMoveIntoAWall_saysWhyAndLeavesThePlayerWhereTheyWere() {
        GameSession session = ConsoleFixture.session(ConsoleFixture.ONE_PUSH);

        String output = ConsoleFixture.play(session, "w\nq\n");

        assertAll(
                () -> assertEquals(0, session.moveCount()),
                () -> assertTrue(output.contains("A wall is in the way.")));
    }

    @Test
    void run_aPushIntoAnotherBox_saysWhichObstacleStoppedIt() {
        GameSession session = ConsoleFixture.session(ConsoleFixture.BOX_ON_A_GOAL);

        String output = ConsoleFixture.play(session, "d\nd\nq\n");

        assertAll(
                () -> assertEquals(1, session.pushCount()),
                () -> assertTrue(output.contains("That box is against another box.")));
    }

    @Test
    void run_undoWithNothingToUndo_saysSo() {
        GameSession session = ConsoleFixture.session(ConsoleFixture.ONE_PUSH);

        String output = ConsoleFixture.play(session, "u\nq\n");

        assertTrue(output.contains("Nothing to undo."));
    }

    @Test
    void run_everyCommand_redrawsTheBoard() {
        GameSession session = ConsoleFixture.session(ConsoleFixture.ONE_PUSH);

        String output = ConsoleFixture.play(session, "d\nu\nr\nl\nzz\nq\n");

        assertEquals(6, ConsoleFixture.boardsDrawnIn(output));
    }

    @Test
    void run_aSaveThatFails_tellsThePlayerTheProgressWasNotKept() throws IOException {
        Path blockingFile = tempDir.resolve("blocking-file");
        Files.writeString(blockingFile, "not a directory");
        GameSession session = ConsoleFixture.session(
                new ProgressStore(blockingFile.resolve("nested")), ConsoleFixture.ONE_PUSH);

        String output = ConsoleFixture.play(session, "d\nd\nd\nq\n");

        assertAll(
                () -> assertTrue(session.isSolved()),
                () -> assertTrue(output.contains(ConsoleGame.SAVE_FAILED)));
    }

    @Test
    void run_aSaveThatSucceeds_saysNothingAboutSaving() {
        GameSession session = ConsoleFixture.session(new ProgressStore(tempDir), ConsoleFixture.ONE_PUSH);

        String output = ConsoleFixture.play(session, "d\nd\nd\nq\n");

        assertAll(
                () -> assertTrue(session.lastSaveSucceeded()),
                () -> assertFalse(output.contains(ConsoleGame.SAVE_FAILED)));
    }

    @Test
    void run_progressSavedByAnEarlierGame_isShownByALaterOne() {
        ProgressStore store = new ProgressStore(tempDir);
        ConsoleFixture.play(
                ConsoleFixture.session(store, ConsoleFixture.ONE_PUSH, ConsoleFixture.TWO_PUSHES), "d\nd\nd\nq\n");

        GameSession resumed = ConsoleFixture.session(store, ConsoleFixture.ONE_PUSH, ConsoleFixture.TWO_PUSHES);
        String output = ConsoleFixture.play(resumed, "l\nq\n");

        assertAll(
                () -> assertTrue(output.contains("moves 0   pushes 0   best 3/3")),
                () -> assertTrue(output.contains("> 1  First   3/3")),
                () -> assertTrue(output.contains("  2  Second  none")));
    }
}
