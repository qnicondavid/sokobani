package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.core.GameState;
import com.milandru.sokobani.core.Level;
import com.milandru.sokobani.core.Position;
import com.milandru.sokobani.core.Tile;
import com.milandru.sokobani.engine.GameSession;
import com.milandru.sokobani.persistence.Progress;
import com.milandru.sokobani.ui.Surface;
import com.milandru.sokobani.ui.TypeSetter;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class GameView {

    public static final int HUD_TOP = 58;
    public static final int HUD_BOTTOM = 42;
    public static final int BOARD_MARGIN = 9;
    public static final int MIN_BASE_WIDTH = 250;
    public static final int VIEW_MARGIN = 16;

    private static final String TITLE = "SOKOBANI";
    private static final String MOVES = " MOVES";
    private static final String PUSHES = " PUSHES";
    private static final String BEST = "BEST ";
    private static final String BEST_SEPARATOR = " / ";
    private static final String STUCK_HINT = "NO WAY BACK / UNDO";

    private static final int HAIRLINE = 1;
    private static final int TITLE_TOP = 12;
    private static final int RULE_Y = 36;
    private static final int RULE_WIDTH = 92;
    private static final int FLEURON_HALF_WIDTH = 3;
    private static final int FLEURON_HALF_HEIGHT = 2;
    private static final int FLEURON_CLEARANCE = 1;
    private static final int SUBTITLE_TOP = 44;
    private static final int FRAME_CLEARANCE = 2;
    private static final int COUNTER_TOP_ABOVE_BOTTOM = 26;
    private static final int BEST_TOP_ABOVE_BOTTOM = 38;
    private static final int COUNTER_INSET = 22;

    private GameView() {
    }

    public static int baseWidth(int columns) {
        return Math.max(MIN_BASE_WIDTH, columns * Tiles.TILE + 2 * BOARD_MARGIN);
    }

    public static int baseHeight(int rows) {
        return HUD_TOP + rows * Tiles.TILE + HUD_BOTTOM;
    }

    public static int boardOriginX(int columns) {
        return (baseWidth(columns) - BoardView.width(columns)) / 2;
    }

    public static Surface render(GameSession session, TypeSetter setter) {
        return render(session, setter, Set.of(), Optional.empty(), 0);
    }

    public static Surface render(GameSession session, TypeSetter setter, Set<Position> deadlockedBoxes,
                                 Optional<Tween> tween, long nowNanos) {
        Objects.requireNonNull(session, "session");
        Objects.requireNonNull(setter, "setter");
        Objects.requireNonNull(deadlockedBoxes, "deadlockedBoxes");
        Objects.requireNonNull(tween, "tween");
        GameState state = session.state();
        Level level = state.level();
        Surface surface = new Surface(baseWidth(level.columnCount()), baseHeight(level.rowCount()));
        int centreX = surface.width() / 2;

        Type.centred(setter, surface, TITLE, centreX, Type.baseline(TITLE_TOP, Type.TITLE), Type.TITLE, Surface.INK);
        ruleWithFleuron(surface, centreX, RULE_Y);
        Type.centred(setter, surface, roomName(level),
                centreX, Type.baseline(SUBTITLE_TOP, Type.SUBTITLE), Type.SUBTITLE, Surface.INK);

        frame(surface, level);
        int originX = boardOriginX(level.columnCount());
        BoardView.draw(surface, state, originX, HUD_TOP, deadlockedBoxes);
        if (tween.isPresent()) {
            drawTween(surface, level, originX, tween.orElseThrow(), nowNanos,
                    BoardView.hatchedAround(level, deadlockedBoxes));
        }
        counters(surface, setter, session, centreX, deadlockedBoxes);

        return surface;
    }

    static String roomName(Level level) {
        return level.name().toUpperCase(Locale.ROOT);
    }

    static String bestLine(Progress.LevelRecord record) {
        return BEST + record.bestMoves() + BEST_SEPARATOR + record.bestPushes();
    }

    static Optional<String> best(Progress progress, int levelIndex) {
        return progress.levelRecord(levelIndex)
                .filter(Progress.LevelRecord::solved)
                .map(GameView::bestLine);
    }

    private static void frame(Surface surface, Level level) {
        surface.box(
                boardOriginX(level.columnCount()) - FRAME_CLEARANCE,
                HUD_TOP - FRAME_CLEARANCE,
                BoardView.width(level.columnCount()) + 2 * FRAME_CLEARANCE,
                BoardView.height(level.rowCount()) + 2 * FRAME_CLEARANCE,
                HAIRLINE,
                Surface.INK);
    }

    private static void counters(Surface surface, TypeSetter setter, GameSession session, int centreX,
                                 Set<Position> deadlockedBoxes) {
        int baseline = Type.baseline(surface.height() - COUNTER_TOP_ABOVE_BOTTOM, Type.COUNTER);
        Type.flushLeft(setter, surface, session.moveCount() + MOVES,
                COUNTER_INSET, baseline, Type.COUNTER, Surface.INK);
        Type.flushRight(setter, surface, session.pushCount() + PUSHES,
                surface.width() - COUNTER_INSET, baseline, Type.COUNTER, Surface.INK);
        int bestBaseline = Type.baseline(surface.height() - BEST_TOP_ABOVE_BOTTOM, Type.CAPTION);
        if (deadlockedBoxes.isEmpty()) {
            best(session.progress(), session.levelIndex()).ifPresent(text ->
                    Type.centred(setter, surface, text, centreX, bestBaseline, Type.CAPTION, Surface.INK));
        } else {
            Type.centred(setter, surface, STUCK_HINT, centreX, bestBaseline, Type.CAPTION, Surface.INK);
        }
    }

    private static void drawTween(Surface surface, Level level, int originX, Tween tween, long nowNanos,
                                  Set<Position> hatched) {
        if (tween.finished(nowNanos)) {
            return;
        }
        double fraction = tween.fraction(nowNanos);
        restore(surface, level, originX, tween.playerTo(), hatched);
        tween.boxTo().ifPresent(to -> restore(surface, level, originX, to, hatched));
        if (tween.boxTo().isPresent()) {
            Position from = tween.boxFrom().orElseThrow();
            Position to = tween.boxTo().orElseThrow();
            int boxX = originX + (int) Math.round(interpolated(from.col(), to.col(), fraction) * Tiles.TILE);
            int boxY = HUD_TOP + (int) Math.round(interpolated(from.row(), to.row(), fraction) * Tiles.TILE);
            Tiles.boxOffGoal(surface, boxX, boxY);
        }
        int playerX = originX + (int) Math.round(interpolated(tween.playerFrom().col(), tween.playerTo().col(), fraction) * Tiles.TILE);
        int playerY = HUD_TOP + (int) Math.round(interpolated(tween.playerFrom().row(), tween.playerTo().row(), fraction) * Tiles.TILE);
        Tiles.player(surface, playerX, playerY);
    }

    private static double interpolated(int from, int to, double fraction) {
        return from + (to - from) * fraction;
    }

    private static void restore(Surface surface, Level level, int originX, Position at, Set<Position> hatched) {
        int x = originX + at.col() * Tiles.TILE;
        int y = HUD_TOP + at.row() * Tiles.TILE;
        surface.fill(x, y, Tiles.TILE, Tiles.TILE, Surface.PAPER);
        if (hatched.contains(at)) {
            Tiles.floorHatched(surface, x, y);
        } else {
            Tiles.floor(surface, x, y);
        }
        if (level.tileAt(at) == Tile.GOAL) {
            Tiles.goal(surface, x, y);
        }
    }

    static void ruleWithFleuron(Surface surface, int centreX, int y) {
        surface.fill(centreX - RULE_WIDTH / 2, y, RULE_WIDTH, HAIRLINE, Surface.INK);
        surface.fill(
                centreX - FLEURON_HALF_WIDTH - FLEURON_CLEARANCE,
                y - FLEURON_HALF_HEIGHT - FLEURON_CLEARANCE,
                2 * (FLEURON_HALF_WIDTH + FLEURON_CLEARANCE),
                2 * (FLEURON_HALF_HEIGHT + FLEURON_CLEARANCE) + HAIRLINE,
                Surface.PAPER);
        for (int dy = -FLEURON_HALF_HEIGHT; dy <= FLEURON_HALF_HEIGHT; dy++) {
            int halfWidth = FLEURON_HALF_WIDTH - Math.abs(dy);
            surface.fill(centreX - halfWidth, y + dy, 2 * halfWidth, HAIRLINE, Surface.INK);
        }
    }
}
