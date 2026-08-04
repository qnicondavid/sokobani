package com.milandru.sokobani.level;

import com.milandru.sokobani.core.Level;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SolverTest {

    @Test
    void solve_levelNeedingOnePush_returnsThatPush() throws Exception {
        Level level = LevelParser.parse("""
                #####
                #@$.#
                #####""");

        Optional<Solver.Solution> solution = Solver.solve(level);

        assertAll(
                () -> assertTrue(solution.isPresent()),
                () -> assertEquals("R", solution.orElseThrow().moves()),
                () -> assertEquals(1, solution.orElseThrow().pushes()));
    }

    @Test
    void solve_levelNeedingAWalkAroundTheBox_returnsTheWalkAndThePush() throws Exception {
        Level level = LevelParser.parse("""
                ######
                #  . #
                # $  #
                #  @ #
                ######""");

        Optional<Solver.Solution> solution = Solver.solve(level);

        assertTrue(Moves.replay(level, solution.orElseThrow().moves()).isSolved());
    }

    @Test
    void solve_levelWhoseOnlyBoxIsStuckInACorner_findsNoSolution() throws Exception {
        Level level = LevelParser.parse("""
                #####
                #$ .#
                # @ #
                #####""");

        assertTrue(Solver.solve(level).isEmpty());
    }

    @Test
    void solve_levelWhoseGoalIsBehindAWall_findsNoSolution() throws Exception {
        Level level = LevelParser.parse("""
                #######
                #@$ #.#
                #   # #
                #   ###
                #######""");

        assertTrue(Solver.solve(level).isEmpty());
    }

    @Test
    void replay_moveIntoAWall_throwsIllegalState() throws Exception {
        Level level = LevelParser.parse("""
                #####
                #@$.#
                #####""");

        assertThrows(IllegalStateException.class, () -> Moves.replay(level, "U"));
    }

    @Test
    void replay_symbolThatIsNotAMove_throwsIllegalArgument() throws Exception {
        Level level = LevelParser.parse("""
                #####
                #@$.#
                #####""");

        assertThrows(IllegalArgumentException.class, () -> Moves.replay(level, "X"));
    }
}
