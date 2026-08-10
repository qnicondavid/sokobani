package com.milandru.sokobani.level;

import com.milandru.sokobani.core.Direction;
import com.milandru.sokobani.core.GameState;
import com.milandru.sokobani.core.Level;
import com.milandru.sokobani.core.Position;
import com.milandru.sokobani.core.Tile;
import com.milandru.sokobani.solve.Solver;
import com.milandru.sokobani.ui.Scaling;
import com.milandru.sokobani.ui.fx.GameView;
import com.milandru.sokobani.ui.fx.Tiles;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassicPackTest {

    private static final int LEVELS_REQUIRED = 15;

    private static final int DEFAULT_WINDOW_CANVAS_WIDTH = 884;
    private static final int DEFAULT_WINDOW_CANVAS_HEIGHT = 740;
    private static final int LAPTOP_CANVAS_WIDTH = 1350;
    private static final int LAPTOP_CANVAS_HEIGHT = 688;

    private static final int ROOMS_PER_TERRAIN = 3;
    private static final int ROOMS_PER_COLUMN_WIDTH = 20;
    private static final int ROOMS_PER_CRATE_COUNT = 30;
    private static final int SMALLEST_CRATE_COUNT = 1;
    private static final int LARGEST_CRATE_COUNT = 6;

    private static final double LEAST_KENDALL_TAU = 0.95;
    private static final int LARGEST_STEP_BACKWARDS = 6;

    private static final LevelPack PACK = classicPack();

    private static final Map<Integer, Solver.Solution> SOLUTIONS = new ConcurrentHashMap<>();

    private static LevelPack classicPack() {
        try {
            return LevelRepository.load(LevelRepository.CLASSIC_PACK);
        } catch (IOException | InvalidLevelFormatException e) {
            throw new AssertionError("the bundled pack must load from the classpath", e);
        }
    }

    private static Solver.Solution solutionOf(Level level) {
        return SOLUTIONS.computeIfAbsent(level.index(), index -> Solver.solve(level)
                .orElseThrow(() -> new AssertionError(level.name() + " has no solution")));
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
    void classicPack_levelNames_areUniqueAndPresent() {
        Set<String> names = new HashSet<>();
        for (Level level : PACK.levels()) {
            assertFalse(level.name().isBlank(), "level " + level.index() + " has no name");
            assertTrue(names.add(level.name()), "duplicate name " + level.name());
        }
    }

    @Test
    void classicPack_levelNames_stayDistinctOnceEveryScreenUppercasesThem() {
        Map<String, String> shouted = new HashMap<>();
        for (Level level : PACK.levels()) {
            String shout = level.name().toUpperCase(Locale.ROOT);
            String earlier = shouted.put(shout, level.name());
            assertTrue(earlier == null,
                    "room " + (level.index() + 1) + " renders as " + shout + ", and so does " + earlier);
        }
    }

    @Test
    void classicPack_everyRoom_reachesTheMinimumScaleInTheDefaultWindow() {
        for (Level level : PACK.levels()) {
            assertFitsIn(level, DEFAULT_WINDOW_CANVAS_WIDTH, DEFAULT_WINDOW_CANVAS_HEIGHT, "the 900 by 780 window");
        }
    }

    @Test
    void classicPack_everyRoom_reachesTheMinimumScaleOnALaptopDisplay() {
        for (Level level : PACK.levels()) {
            assertFitsIn(level, LAPTOP_CANVAS_WIDTH, LAPTOP_CANVAS_HEIGHT, "a 1366 by 768 display");
        }
    }

    @Test
    void classicPack_theRowCountEachCanvasAllows_isSixteenAndFifteen() {
        assertEquals(16, rowsThatFit(DEFAULT_WINDOW_CANVAS_HEIGHT));
        assertEquals(15, rowsThatFit(LAPTOP_CANVAS_HEIGHT));
        for (Level level : PACK.levels()) {
            assertTrue(level.rowCount() <= rowsThatFit(LAPTOP_CANVAS_HEIGHT),
                    level.name() + " is " + level.rowCount() + " rows tall");
        }
    }

    @Test
    void classicPack_noTerrain_isSharedByMoreThanThreeRooms() {
        Map<String, Integer> layouts = new HashMap<>();
        for (Level level : PACK.levels()) {
            layouts.merge(terrainKeyOf(level), 1, Integer::sum);
        }
        int largest = layouts.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        assertTrue(largest <= ROOMS_PER_TERRAIN,
                layouts.size() + " terrains for " + PACK.size() + " rooms, largest group " + largest);
    }

    @Test
    void classicPack_noColumnWidth_isSharedByMoreThanTwentyRooms() {
        Map<Integer, Integer> widths = new HashMap<>();
        for (Level level : PACK.levels()) {
            widths.merge(level.columnCount(), 1, Integer::sum);
        }
        for (Map.Entry<Integer, Integer> entry : widths.entrySet()) {
            assertTrue(entry.getValue() <= ROOMS_PER_COLUMN_WIDTH,
                    entry.getValue() + " rooms are " + entry.getKey() + " columns wide");
        }
    }

    @Test
    void classicPack_crateCounts_runOneToSixWithNoneOverThirtyRooms() {
        Map<Integer, Integer> counts = new HashMap<>();
        for (Level level : PACK.levels()) {
            counts.merge(level.initialBoxes().size(), 1, Integer::sum);
        }
        for (int crates = SMALLEST_CRATE_COUNT; crates <= LARGEST_CRATE_COUNT; crates++) {
            assertTrue(counts.getOrDefault(crates, 0) > 0, "no room has " + crates + " crates");
        }
        for (Map.Entry<Integer, Integer> entry : counts.entrySet()) {
            assertTrue(entry.getValue() <= ROOMS_PER_CRATE_COUNT,
                    entry.getValue() + " rooms have " + entry.getKey() + " crates");
        }
    }

    @Test
    void classicPack_asShipped_risesAlmostMonotonicallyInOptimalPushes() {
        int size = PACK.size();
        int[] pushes = new int[size];
        for (int index = 0; index < size; index++) {
            pushes[index] = solutionOf(PACK.get(index)).pushes();
        }
        long concordant = 0;
        long discordant = 0;
        for (int earlier = 0; earlier < size; earlier++) {
            for (int later = earlier + 1; later < size; later++) {
                if (pushes[earlier] < pushes[later]) {
                    concordant++;
                } else if (pushes[earlier] > pushes[later]) {
                    discordant++;
                }
            }
        }
        long pairs = (long) size * (size - 1) / 2;
        double tau = (concordant - discordant) / (double) pairs;
        assertTrue(tau >= LEAST_KENDALL_TAU, "Kendall tau-a on optimal pushes is " + tau);
        for (int index = 1; index < size; index++) {
            int backwards = pushes[index - 1] - pushes[index];
            assertTrue(backwards <= LARGEST_STEP_BACKWARDS,
                    PACK.get(index - 1).name() + " needs " + pushes[index - 1] + " pushes and "
                            + PACK.get(index).name() + " only " + pushes[index]);
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
        Solver.Solution solution = solutionOf(level);

        assertTrue(Replay.replay(level, solution.moves()).isSolved(),
                name + " was not solved by " + solution.moves());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bundledLevels")
    void bundledLevel_asShipped_roundTripsThroughXsb(String name, Level level) throws Exception {
        assertEquals(level, LevelParser.parse(LevelParser.toXsb(level), level.name(), level.index()));
    }

    private static void assertFitsIn(Level level, int canvasWidth, int canvasHeight, String canvas) {
        int scaledWidth = Scaling.MIN_FACTOR * GameView.baseWidth(level.columnCount())
                + 2 * GameView.VIEW_MARGIN;
        int scaledHeight = Scaling.MIN_FACTOR * GameView.baseHeight(level.rowCount())
                + 2 * GameView.VIEW_MARGIN;
        assertTrue(scaledWidth <= canvasWidth,
                level.name() + " needs " + scaledWidth + " of " + canvasWidth + " pixels across " + canvas);
        assertTrue(scaledHeight <= canvasHeight,
                level.name() + " needs " + scaledHeight + " of " + canvasHeight + " pixels down " + canvas);
    }

    private static int rowsThatFit(int canvasHeight) {
        int tallestBase = (canvasHeight - 2 * GameView.VIEW_MARGIN) / Scaling.MIN_FACTOR;
        return (tallestBase - GameView.HUD_TOP - GameView.HUD_BOTTOM) / Tiles.TILE;
    }

    private static String terrainKeyOf(Level level) {
        char[][] terrain = new char[level.rowCount()][level.columnCount()];
        for (int row = 0; row < level.rowCount(); row++) {
            for (int col = 0; col < level.columnCount(); col++) {
                Tile tile = level.tileAt(new Position(row, col));
                terrain[row][col] = switch (tile) {
                    case WALL -> '#';
                    case GOAL -> '.';
                    case FLOOR -> ' ';
                };
            }
        }
        String smallest = null;
        for (int flipped = 0; flipped < 2; flipped++) {
            char[][] working = flipped == 0 ? terrain : mirrored(terrain);
            for (int turn = 0; turn < 4; turn++) {
                String text = textOf(working);
                if (smallest == null || text.compareTo(smallest) < 0) {
                    smallest = text;
                }
                working = turned(working);
            }
        }
        return smallest;
    }

    private static char[][] turned(char[][] grid) {
        char[][] out = new char[grid[0].length][grid.length];
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                out[col][grid.length - 1 - row] = grid[row][col];
            }
        }
        return out;
    }

    private static char[][] mirrored(char[][] grid) {
        char[][] out = new char[grid.length][grid[0].length];
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                out[row][grid[0].length - 1 - col] = grid[row][col];
            }
        }
        return out;
    }

    private static String textOf(char[][] grid) {
        StringBuilder text = new StringBuilder();
        for (char[] row : grid) {
            text.append(new String(row)).append('\n');
        }
        return text.toString();
    }

    private static boolean isOnBoundary(Level level, Position position) {
        return position.row() == 0 || position.row() == level.rowCount() - 1
                || position.col() == 0 || position.col() == level.columnCount() - 1;
    }
}
