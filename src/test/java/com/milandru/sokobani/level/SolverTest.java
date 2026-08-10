package com.milandru.sokobani.level;

import com.milandru.sokobani.core.Level;
import com.milandru.sokobani.solve.Solver;
import org.junit.jupiter.api.Test;

import java.util.Map;
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

        assertTrue(Replay.replay(level, solution.orElseThrow().moves()).isSolved());
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
    void solve_theFirstBundledRooms_answerTheMovesAReadingOrderExpansionPicks() throws Exception {
        LevelPack pack = LevelRepository.load(LevelRepository.CLASSIC_PACK);
        Map<Integer, String> expansionInReadingOrder = Map.of(
                0, "RRR",
                1, "URR",
                2, "LULUURDURRD",
                3, "LULURDDRRRURUL",
                4, "UUURRDLRDDDLLURDRRUULDLDR",
                6, "DRURDLDL",
                7, "DRDRRUULDLLURDRRDL",
                8, "LUULDRDRURURDLDLLLRRRR");

        expansionInReadingOrder.forEach((index, moves) ->
                assertEquals(moves, Solver.solve(pack.get(index)).orElseThrow().moves(),
                        pack.get(index).name() + " was solved by a different route than reading order picks"));
    }

    @Test
    void solve_theSameRoomInTheSameRun_answersTheSameMoves() throws Exception {
        LevelPack pack = LevelRepository.load(LevelRepository.CLASSIC_PACK);

        for (int index = 0; index < 8; index++) {
            assertEquals(Solver.solve(pack.get(index)).orElseThrow().moves(),
                    Solver.solve(pack.get(index)).orElseThrow().moves(), pack.get(index).name());
        }
    }

    @Test
    void replay_moveIntoAWall_throwsIllegalState() throws Exception {
        Level level = LevelParser.parse("""
                #####
                #@$.#
                #####""");

        assertThrows(IllegalStateException.class, () -> Replay.replay(level, "U"));
    }

    @Test
    void replay_symbolThatIsNotAMove_throwsIllegalArgument() throws Exception {
        Level level = LevelParser.parse("""
                #####
                #@$.#
                #####""");

        assertThrows(IllegalArgumentException.class, () -> Replay.replay(level, "X"));
    }
}
