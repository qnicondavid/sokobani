package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.core.Direction;
import com.milandru.sokobani.core.Level;
import com.milandru.sokobani.core.Position;
import com.milandru.sokobani.engine.GameSession;
import com.milandru.sokobani.level.LevelPack;
import com.milandru.sokobani.ui.Surface;
import com.milandru.sokobani.ui.Theme;
import com.milandru.sokobani.ui.Threshold;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameViewTest {

    @Test
    void baseWidth_isTheBoardPlusItsMarginsOrTheSharedScreenWidth() {
        assertEquals(250, GameView.baseWidth(7));
        assertEquals(250, GameView.baseWidth(11));
        assertEquals(250, GameView.baseWidth(15));
        assertEquals(258, GameView.baseWidth(16));
        assertEquals(318, GameView.baseWidth(20));
    }

    @Test
    void baseHeight_isTheHudsPlusTheBoard() {
        assertEquals(145, GameView.baseHeight(3));
        assertEquals(175, GameView.baseHeight(5));
        assertEquals(235, GameView.baseHeight(9));
        assertEquals(400, GameView.baseHeight(20));
    }

    @Test
    void baseDimensions_acrossASweepOfLevelSizes_followSectionTwosFormula() {
        for (int columns = 1; columns <= 40; columns++) {
            int board = columns * Tiles.TILE + 2 * GameView.BOARD_MARGIN;
            assertEquals(Math.max(GameView.MIN_BASE_WIDTH, board), GameView.baseWidth(columns));
        }
        for (int rows = 1; rows <= 40; rows++) {
            assertEquals(GameView.HUD_TOP + rows * Tiles.TILE + GameView.HUD_BOTTOM, GameView.baseHeight(rows));
        }
    }

    @Test
    void boardOriginX_centresTheBoardInTheBaseSurface() {
        for (int columns = 1; columns <= 40; columns++) {
            int origin = GameView.boardOriginX(columns);
            int rightMargin = GameView.baseWidth(columns) - origin - BoardView.width(columns);

            assertTrue(origin >= GameView.BOARD_MARGIN - 1, "columns " + columns);
            assertTrue(Math.abs(origin - rightMargin) <= 1, "columns " + columns);
        }
    }

    @Test
    void render_sizesTheSurfaceFromTheLevel() {
        for (Level level : BoardFixture.classicPack().levels()) {
            Surface surface = GameView.render(sessionFor(level), BoardFixture.typeSetter());

            assertEquals(GameView.baseWidth(level.columnCount()), surface.width(), level.name());
            assertEquals(GameView.baseHeight(level.rowCount()), surface.height(), level.name());
        }
    }

    @Test
    void render_theSameSession_producesIdenticalPixelsTwiceRunning() {
        GameSession session = BoardFixture.session(BoardFixture.EVERY_TILE);

        Surface first = GameView.render(session, BoardFixture.typeSetter());
        Surface second = GameView.render(session, BoardFixture.typeSetter());

        assertArrayEquals(first.tones(), second.tones());
    }

    @Test
    void render_everyBundledLevel_producesIdenticalPixelsTwiceRunning() {
        for (Level level : BoardFixture.classicPack().levels()) {
            GameSession session = sessionFor(level);

            assertArrayEquals(
                    GameView.render(session, BoardFixture.typeSetter()).tones(),
                    GameView.render(session, BoardFixture.typeSetter()).tones(),
                    level.name());
        }
    }

    @Test
    void render_aFinishedTween_leavesTheSettledBoardExactlyAsItIsWithoutOne() {
        GameSession session = BoardFixture.session(BoardFixture.ONE_PUSH);
        session.move(Direction.RIGHT);
        Tween tween = Tween.ofPush(new Position(1, 1), session.state().player(),
                new Position(1, 2), boxOf(session), 0L, 100L);

        Surface settled = GameView.render(session, BoardFixture.typeSetter());
        Surface ended = GameView.render(session, BoardFixture.typeSetter(), Set.of(), Optional.of(tween), 100L);

        assertArrayEquals(settled.tones(), ended.tones());
    }

    @Test
    void render_aTweenAtItsLastFrame_alsoLandsOnTheSettledBoard() {
        GameSession session = BoardFixture.session(BoardFixture.ONE_PUSH);
        session.move(Direction.RIGHT);
        Tween tween = Tween.ofPush(new Position(1, 1), session.state().player(),
                new Position(1, 2), boxOf(session), 0L, 100L);

        Surface settled = GameView.render(session, BoardFixture.typeSetter());
        Surface almost = GameView.render(session, BoardFixture.typeSetter(), Set.of(), Optional.of(tween), 99L);

        assertArrayEquals(settled.tones(), almost.tones(),
                "at fraction 0.99 the pieces round onto their destination squares");
    }

    @Test
    void render_aTweenAtItsFirstFrame_putsThePiecesBackWhereTheyStarted() {
        GameSession session = BoardFixture.session(BoardFixture.ONE_PUSH);
        Surface beforeTheMove = GameView.render(session, BoardFixture.typeSetter());
        Position playerBefore = session.state().player();
        Position boxBefore = boxOf(session);

        session.move(Direction.RIGHT);
        Tween tween = Tween.ofPush(playerBefore, session.state().player(), boxBefore, boxOf(session), 0L, 100L);
        Surface starting = GameView.render(session, BoardFixture.typeSetter(), Set.of(), Optional.of(tween), 0L);
        Surface settled = GameView.render(session, BoardFixture.typeSetter());

        assertArrayEquals(boardOf(beforeTheMove), boardOf(starting),
                "the first frame draws the board exactly as it stood before the move");
        assertFalse(Arrays.equals(boardOf(settled), boardOf(starting)),
                "and differently from the settled board, which is the point of animating");
    }

    private static Position boxOf(GameSession session) {
        return session.state().boxes().iterator().next();
    }

    private static int[] boardOf(Surface surface) {
        int height = surface.height() - GameView.HUD_TOP - GameView.HUD_BOTTOM;
        int[] board = new int[surface.width() * height];
        int index = 0;
        for (int y = GameView.HUD_TOP; y < GameView.HUD_TOP + height; y++) {
            for (int x = 0; x < surface.width(); x++) {
                board[index++] = surface.toneAt(x, y);
            }
        }
        return board;
    }

    @Test
    void render_afterAMove_changes() {
        GameSession session = BoardFixture.session(BoardFixture.ONE_PUSH);
        Surface before = GameView.render(session, BoardFixture.typeSetter());

        session.move(Direction.RIGHT);
        Surface after = GameView.render(session, BoardFixture.typeSetter());

        assertFalse(Arrays.equals(before.tones(), after.tones()));
    }

    @Test
    void render_thresholdedToATheme_usesExactlyTwoColourValues() {
        Surface surface = GameView.render(BoardFixture.session(BoardFixture.EVERY_TILE), BoardFixture.typeSetter());

        for (Theme theme : Theme.ALL) {
            Set<Integer> distinct = new HashSet<>();
            for (int pixel : Threshold.paletteMap(surface, theme)) {
                distinct.add(pixel);
            }

            assertEquals(2, distinct.size());
            assertTrue(distinct.contains(theme.ink()));
            assertTrue(distinct.contains(theme.paper()));
        }
    }

    @Test
    void render_inAllFourThemes_keepsIdenticalGeometry() {
        Surface surface = GameView.render(BoardFixture.session(BoardFixture.EVERY_TILE), BoardFixture.typeSetter());
        boolean[] reference = inkMask(surface, Theme.CATALOGUE);

        for (Theme theme : Theme.ALL) {
            assertArrayEquals(reference, inkMask(surface, theme));
        }
    }

    @Test
    void render_paintsTheBoardAtTheHudOffset() {
        GameSession session = BoardFixture.session(BoardFixture.ONE_PUSH);
        Level level = session.currentLevel();
        Surface whole = GameView.render(session, BoardFixture.typeSetter());

        Surface board = new Surface(whole.width(), whole.height());
        BoardView.draw(board, session.state(), GameView.boardOriginX(level.columnCount()), GameView.HUD_TOP);

        int originX = GameView.boardOriginX(level.columnCount());
        for (int y = GameView.HUD_TOP; y < GameView.HUD_TOP + BoardView.height(level.rowCount()); y++) {
            for (int x = originX; x < originX + BoardView.width(level.columnCount()); x++) {
                assertEquals(board.toneAt(x, y), whole.toneAt(x, y));
            }
        }
    }

    @Test
    void render_inksTheHudAboveAndBelowTheBoard() {
        GameSession session = BoardFixture.session(BoardFixture.ONE_PUSH);
        Level level = session.currentLevel();
        Surface surface = GameView.render(session, BoardFixture.typeSetter());
        int boardBottom = GameView.HUD_TOP + BoardView.height(level.rowCount());

        assertTrue(inkIn(surface, 0, 0, surface.width(), GameView.HUD_TOP - 3) > 0);
        assertTrue(inkIn(surface, 0, boardBottom + 3, surface.width(), surface.height() - boardBottom - 3) > 0);
    }

    @Test
    void render_withAPersonalBest_addsARowAboveTheCountersAndLeavesThemWhereTheyWere() {
        GameSession fresh = BoardFixture.session(BoardFixture.ONE_PUSH);
        List<Integer> countersOnly = inkedRowsInTheBottomStrip(
                GameView.render(fresh, BoardFixture.typeSetter()));

        GameSession replayed = BoardFixture.session(BoardFixture.ONE_PUSH);
        replayed.move(Direction.RIGHT);
        replayed.move(Direction.RIGHT);
        replayed.move(Direction.RIGHT);
        replayed.restart();
        Surface withBest = GameView.render(replayed, BoardFixture.typeSetter());
        List<Integer> both = inkedRowsInTheBottomStrip(withBest);

        assertTrue(GameView.best(replayed.progress(), replayed.levelIndex()).isPresent());
        assertFalse(countersOnly.isEmpty());
        assertEquals(countersOnly.get(countersOnly.size() - 1), both.get(both.size() - 1));
        assertTrue(both.get(0) < countersOnly.get(0));
        assertTrue(blankRowsBetween(withBest, both) > 0);
    }

    @Test
    void render_withoutAPersonalBest_leavesTheCountersAloneInTheBottomStrip() {
        Surface surface = GameView.render(BoardFixture.session(BoardFixture.ONE_PUSH), BoardFixture.typeSetter());
        List<Integer> inked = inkedRowsInTheBottomStrip(surface);

        assertFalse(inked.isEmpty());
        assertEquals(0, blankRowsBetween(surface, inked));
    }

    @Test
    void render_withADeadlockedBox_putsTheStuckHintOnItsOwnRowAboveTheCounters() {
        GameSession session = BoardFixture.session(BoardFixture.ONE_PUSH);
        Surface surface = GameView.render(
                session, BoardFixture.typeSetter(), Set.of(new Position(1, 2)), Optional.empty(), 0);
        List<Integer> inked = inkedRowsInTheBottomStrip(surface);

        assertFalse(inked.isEmpty());
        assertTrue(blankRowsBetween(surface, inked) > 0);
    }

    @Test
    void render_theWidestCaptionRow_clearsBothEdgesOfTheNarrowestSurface() {
        GameSession session = BoardFixture.session(BoardFixture.ONE_PUSH);
        Surface surface = GameView.render(
                session, BoardFixture.typeSetter(), Set.of(new Position(1, 2)), Optional.empty(), 0);
        int top = surface.height() - 38;

        assertEquals(GameView.MIN_BASE_WIDTH, surface.width());
        assertEquals(0, inkIn(surface, 0, top, 40, 8));
        assertEquals(0, inkIn(surface, surface.width() - 40, top, 40, 8));
        assertTrue(inkIn(surface, 40, top, surface.width() - 80, 8) > 0);
    }

    @Test
    void render_withADeadlockedBox_differsFromTheSamePositionWithoutOne() {
        GameSession session = BoardFixture.session(BoardFixture.ONE_PUSH);
        Set<Position> none = Set.of();
        Set<Position> stuck = Set.of(new Position(1, 2));

        assertFalse(Arrays.equals(
                GameView.render(session, BoardFixture.typeSetter(), none, Optional.empty(), 0).tones(),
                GameView.render(session, BoardFixture.typeSetter(), stuck, Optional.empty(), 0).tones()));
    }

    private static List<Integer> inkedRowsInTheBottomStrip(Surface surface) {
        List<Integer> inked = new ArrayList<>();
        for (int y = surface.height() - GameView.HUD_BOTTOM + 2; y < surface.height(); y++) {
            if (inkIn(surface, 0, y, surface.width(), 1) > 0) {
                inked.add(y);
            }
        }
        return inked;
    }

    private static int blankRowsBetween(Surface surface, List<Integer> inkedRows) {
        if (inkedRows.isEmpty()) {
            return 0;
        }
        return inkedRows.get(inkedRows.size() - 1) - inkedRows.get(0) + 1 - inkedRows.size();
    }

    private static GameSession sessionFor(Level level) {
        return new GameSession(new LevelPack(level.name(), List.of(level)));
    }

    private static boolean[] inkMask(Surface surface, Theme theme) {
        int[] pixels = Threshold.paletteMap(surface, theme);
        boolean[] mask = new boolean[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            mask[i] = pixels[i] == theme.ink();
        }
        return mask;
    }

    private static int inkIn(Surface surface, int x, int y, int width, int height) {
        int ink = 0;
        for (int row = y; row < y + height; row++) {
            for (int column = x; column < x + width; column++) {
                if (Threshold.isInk(surface.toneAt(column, row))) {
                    ink++;
                }
            }
        }
        return ink;
    }
}
