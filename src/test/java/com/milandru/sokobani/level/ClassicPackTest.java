package com.milandru.sokobani.level;

import com.milandru.sokobani.core.Direction;
import com.milandru.sokobani.core.GameState;
import com.milandru.sokobani.core.Level;
import com.milandru.sokobani.core.Position;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassicPackTest {

    private static final int LEVELS_REQUIRED = 15;

    private static final LevelPack PACK = classicPack();

    private static LevelPack classicPack() {
        try {
            return LevelRepository.load(LevelRepository.CLASSIC_PACK);
        } catch (IOException | InvalidLevelFormatException e) {
            throw new AssertionError("the bundled pack must load from the classpath", e);
        }
    }

    static Stream<Arguments> bundledLevels() {
        return PACK.levels().stream().map(level -> Arguments.of(level.name(), level));
    }

    @Test
    void classicPack_asShipped_holdsAtLeastFifteenLevels() {
        assertTrue(PACK.size() >= LEVELS_REQUIRED, "pack holds " + PACK.size() + " levels");
    }

    @Test
    void classicPack_asShipped_loadsFromTheClasspathUnderItsFileName() {
        assertEquals("classic", PACK.name());
    }

    @Test
    void classicPack_teachingLevels_useAtMostTwoBoxes() {
        for (int index = 0; index < 3; index++) {
            Level level = PACK.get(index);
            assertTrue(level.initialBoxes().size() <= 2, level.name() + " has " + level.initialBoxes().size());
        }
    }

    @Test
    void classicPack_planningLevels_useFourToSixBoxes() {
        for (int index = 8; index < PACK.size(); index++) {
            Level level = PACK.get(index);
            int boxes = level.initialBoxes().size();
            assertTrue(boxes >= 4 && boxes <= 6, level.name() + " has " + boxes + " boxes");
        }
    }

    @Test
    void classicPack_levelNames_areUniqueAndPresent() {
        Set<String> names = new HashSet<>();
        for (Level level : PACK.levels()) {
            assertFalse(level.name().isBlank(), "level " + level.index() + " has no name");
            assertTrue(names.add(level.name()), "duplicate name " + level.name());
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bundledLevels")
    void bundledLevel_asShipped_isIndexedByItsPositionInThePack(String name, Level level) {
        assertEquals(level, PACK.get(level.index()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bundledLevels")
    void bundledLevel_asShipped_showsExactlyOnePlayer(String name, Level level) {
        assertEquals(1, LevelParser.toXsb(level).chars()
                .filter(symbol -> symbol == LevelParser.PLAYER || symbol == LevelParser.PLAYER_ON_GOAL)
                .count());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bundledLevels")
    void bundledLevel_asShipped_hasAsManyBoxesAsGoals(String name, Level level) {
        assertEquals(level.goals().size(), level.initialBoxes().size());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bundledLevels")
    void bundledLevel_asShipped_isEnclosed(String name, Level level) {
        Set<Position> reached = new HashSet<>();
        Deque<Position> pending = new ArrayDeque<>();
        reached.add(level.initialPlayer());
        pending.add(level.initialPlayer());
        while (!pending.isEmpty()) {
            Position at = pending.remove();
            assertFalse(isOnBoundary(level, at), name + " lets the player reach " + at);
            for (Direction direction : Direction.values()) {
                Position next = at.moved(direction);
                if (level.tileAt(next).isWalkable() && reached.add(next)) {
                    pending.add(next);
                }
            }
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bundledLevels")
    void bundledLevel_asShipped_startsUnsolved(String name, Level level) {
        assertFalse(new GameState(level).isSolved());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bundledLevels")
    void bundledLevel_asShipped_isSolvable(String name, Level level) {
        Optional<Solver.Solution> solution = Solver.solve(level);

        assertTrue(solution.isPresent(), name + " has no solution");
        assertTrue(Moves.replay(level, solution.get().moves()).isSolved(),
                name + " was not solved by " + solution.get().moves());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bundledLevels")
    void bundledLevel_asShipped_roundTripsThroughXsb(String name, Level level) throws Exception {
        assertEquals(level, LevelParser.parse(LevelParser.toXsb(level), level.name(), level.index()));
    }

    private static boolean isOnBoundary(Level level, Position position) {
        return position.row() == 0 || position.row() == level.rowCount() - 1
                || position.col() == 0 || position.col() == level.columnCount() - 1;
    }
}
