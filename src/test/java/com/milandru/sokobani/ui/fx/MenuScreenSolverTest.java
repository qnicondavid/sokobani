package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.core.Level;
import com.milandru.sokobani.level.LevelPack;
import com.milandru.sokobani.persistence.Progress;
import com.milandru.sokobani.solve.Solver;
import com.milandru.sokobani.ui.FxToolkit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MenuScreenSolverTest {

    private static final int QUICK_ROOM = 9;
    private static final int SLOW_ROOM = 70;
    private static final int A_FRACTION_OF_THE_ROOM = 3;
    private static final int EXPANSIONS_BEFORE_STOPPING = 50;
    private static final int BRANCHING_ROOM = 39;
    private static final int BRANCHING_ROOM_EXPANSIONS = 3183;
    private static final long LET_THE_SOLVER_GET_GOING_MILLIS = 40;

    @TempDir
    Path home;

    @Test
    void hidden_whileTheSolverIsInsideARoom_stopsItInsteadOfLettingItFinishThatRoom() {
        LevelPack pack = BoardFixture.classicPack();
        ScreenFixture fixture = ScreenFixture.withProgress(home, pack, Progress.empty().withSolved(SLOW_ROOM, 1, 1));
        assertTrue(fixture.session.progress().isSolved(SLOW_ROOM));

        MenuBoard board = FxToolkit.on(() -> {
            fixture.controller.show(fixture.menu);
            return fixture.menu.board();
        });
        fixture.settle(fixture.menu::solving, "the solve pass never started");
        pause();
        long started = System.currentTimeMillis();
        fixture.show(fixture.howTo);
        fixture.settleTheMenuSolver();
        long stopped = System.currentTimeMillis() - started;
        long roomMillis = millisToSolve(pack.get(SLOW_ROOM));

        assertNull(board.solutionOf(SLOW_ROOM),
                "the menu solver finished the room it was inside rather than stopping in it");
        assertTrue(stopped < roomMillis / A_FRACTION_OF_THE_ROOM,
                "the menu solver ran on for " + stopped + "ms after the menu was hidden, and the room it was inside, "
                        + pack.get(SLOW_ROOM).name() + ", takes " + roomMillis + "ms to finish");
    }

    @Test
    void solve_askedToStopBeforeItExpandsAnything_answersNothing() {
        assertTrue(Solver.solve(BoardFixture.classicPack().get(QUICK_ROOM), () -> true).isEmpty());
    }

    @Test
    void solve_askedToStopAfterOneExpansion_answersNothingRatherThanTheSolution() {
        AtomicBoolean firstCall = new AtomicBoolean(true);

        assertTrue(Solver.solve(BoardFixture.classicPack().get(QUICK_ROOM),
                () -> !firstCall.getAndSet(false)).isEmpty());
    }

    @Test
    void solve_askedToStopAfterAHandfulOfExpansions_stopsThereRatherThanAtTheEndOfTheRoom() {
        AtomicInteger asked = new AtomicInteger();

        assertTrue(Solver.solve(BoardFixture.classicPack().get(SLOW_ROOM),
                () -> asked.incrementAndGet() >= EXPANSIONS_BEFORE_STOPPING).isEmpty());
        assertEquals(EXPANSIONS_BEFORE_STOPPING, asked.get(),
                "the solve ran on past the answer it was given, to ask " + asked.get() + " times");
    }

    @Test
    void solve_walkingARoomToTheEnd_isAskedOncePerStateItExpands() {
        AtomicInteger asked = new AtomicInteger();

        Solver.solve(BoardFixture.classicPack().get(BRANCHING_ROOM), () -> {
            asked.incrementAndGet();
            return false;
        });

        assertEquals(BRANCHING_ROOM_EXPANSIONS, asked.get(),
                "the solver expands " + BRANCHING_ROOM_EXPANSIONS + " states of that room and asked "
                        + asked.get() + " times, so the question is not being asked once per expansion");
    }

    @Test
    void solve_neverAskedToStop_answersWhatThePlainCallAnswers() {
        Level level = BoardFixture.classicPack().get(0);

        assertEquals(Solver.solve(level).orElseThrow().moves(),
                Solver.solve(level, () -> false).orElseThrow().moves());
    }

    private static long millisToSolve(Level level) {
        long started = System.currentTimeMillis();
        Solver.solve(level);
        return Math.max(1, System.currentTimeMillis() - started);
    }

    private static void pause() {
        try {
            Thread.sleep(LET_THE_SOLVER_GET_GOING_MILLIS);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("interrupted while letting the solver get going", interrupted);
        }
    }
}
