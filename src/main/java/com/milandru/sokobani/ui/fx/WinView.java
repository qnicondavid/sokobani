package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.core.GameState;
import com.milandru.sokobani.core.Level;
import com.milandru.sokobani.ui.Surface;
import com.milandru.sokobani.ui.TypeSetter;

import java.util.Objects;

public final class WinView {

    public static final int WIDTH = 250;
    public static final int HEIGHT = 252;

    private static final int BAND_Y = 28;
    private static final int BAND_H = 52;
    private static final int PANEL_X = 28;
    private static final int PANEL_Y = 96;
    private static final int PANEL_W = 194;
    private static final int PANEL_H = 118;

    private static final int FIGURE_Y = 104;
    private static final int FIGURE_SPREAD = 40;
    private static final int LABEL_Y = 126;
    private static final int DIVIDER_Y = 104;
    private static final int DIVIDER_H = 30;
    private static final int BEST_Y = 144;
    private static final int BAR_X = 40;
    private static final int BAR_Y = 164;
    private static final int BAR_W = 170;
    private static final int BAR_H = 17;
    private static final int BAR_LABEL_Y = 166;
    private static final int ACTIONS_Y = 190;
    private static final int ACTION_SPREAD = 40;
    private static final int ACTION_HIT_HALF = 30;

    private static final Type.Style BAND_STYLE = new Type.Style(19, 2.0);
    private static final Type.Style BAND_NAME_STYLE = new Type.Style(10, 2.0);
    private static final Type.Style ACTION_STYLE = new Type.Style(11, 1.0);

    public enum Target {
        NEXT_ROOM,
        REPLAY,
        ROOMS,
        NONE
    }

    private WinView() {
    }

    public static void render(
            Surface surface, TypeSetter setter, GameState board, WinData data, boolean hasNextRoom) {
        Objects.requireNonNull(surface, "surface");
        Objects.requireNonNull(setter, "setter");
        Objects.requireNonNull(board, "board");
        Objects.requireNonNull(data, "data");
        int centreX = surface.width() / 2;

        drawBoard(surface, board);
        surface.hatchDiagonal(0, 0, surface.width(), surface.height(), 3, Surface.INK);

        surface.fill(0, BAND_Y, surface.width(), BAND_H, Surface.INK);
        Level level = data.solved().level();
        Type.centred(setter, surface, "ROOM " + (level.index() + 1) + " SOLVED",
                centreX, Type.baseline(36, BAND_STYLE), BAND_STYLE, Surface.PAPER);
        Type.centred(setter, surface, GameView.roomName(level),
                centreX, Type.baseline(61, BAND_NAME_STYLE), BAND_NAME_STYLE, Surface.PAPER);

        surface.fill(PANEL_X, PANEL_Y, PANEL_W, PANEL_H, Surface.PAPER);
        surface.box(PANEL_X, PANEL_Y, PANEL_W, PANEL_H, 1, Surface.INK);

        int figuresBaseline = Type.baseline(FIGURE_Y, Type.WIN_FIGURE);
        Type.centred(setter, surface, String.valueOf(data.solved().moveCount()),
                centreX - FIGURE_SPREAD, figuresBaseline, Type.WIN_FIGURE, Surface.INK);
        Type.centred(setter, surface, String.valueOf(data.solved().pushCount()),
                centreX + FIGURE_SPREAD, figuresBaseline, Type.WIN_FIGURE, Surface.INK);
        int labelsBaseline = Type.baseline(LABEL_Y, Type.CAPTION);
        Type.centred(setter, surface, "MOVES", centreX - FIGURE_SPREAD, labelsBaseline, Type.CAPTION, Surface.INK);
        Type.centred(setter, surface, "PUSHES", centreX + FIGURE_SPREAD, labelsBaseline, Type.CAPTION, Surface.INK);

        surface.fill(centreX, DIVIDER_Y, 1, DIVIDER_H, Surface.INK);
        data.bestBeforeThisAttempt().ifPresent(record -> Type.centred(setter, surface, GameView.bestLine(record),
                centreX, Type.baseline(BEST_Y, Type.CAPTION), Type.CAPTION, Surface.INK));

        if (hasNextRoom) {
            surface.fill(BAR_X, BAR_Y, BAR_W, BAR_H, Surface.INK);
            Type.centred(setter, surface, "NEXT ROOM", centreX,
                    Type.baseline(BAR_LABEL_Y, Type.COUNTER), Type.COUNTER, Surface.PAPER);
        }

        Type.centred(setter, surface, "REPLAY", centreX - ACTION_SPREAD,
                Type.baseline(ACTIONS_Y, ACTION_STYLE), ACTION_STYLE, Surface.INK);
        Type.centred(setter, surface, "ROOMS", centreX + ACTION_SPREAD,
                Type.baseline(ACTIONS_Y, ACTION_STYLE), ACTION_STYLE, Surface.INK);
    }

    public static Target targetAt(int baseX, int baseY, boolean hasNextRoom) {
        if (baseY >= BAR_Y && baseY < BAR_Y + BAR_H && baseX >= BAR_X && baseX < BAR_X + BAR_W) {
            return hasNextRoom ? Target.NEXT_ROOM : Target.NONE;
        }
        if (baseY >= ACTIONS_Y && baseY < ACTIONS_Y + 12) {
            if (baseX >= WIDTH / 2 - ACTION_SPREAD - ACTION_HIT_HALF
                    && baseX < WIDTH / 2 - ACTION_SPREAD + ACTION_HIT_HALF) {
                return Target.REPLAY;
            }
            if (baseX >= WIDTH / 2 + ACTION_SPREAD - ACTION_HIT_HALF
                    && baseX < WIDTH / 2 + ACTION_SPREAD + ACTION_HIT_HALF) {
                return Target.ROOMS;
            }
        }
        return Target.NONE;
    }

    private static void drawBoard(Surface surface, GameState board) {
        Level level = board.level();
        int boardX = (surface.width() - BoardView.width(level.columnCount())) / 2;
        BoardView.draw(surface, board, boardX, 0);
    }
}
