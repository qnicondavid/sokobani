package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.core.GameState;
import com.milandru.sokobani.core.Level;
import com.milandru.sokobani.persistence.Progress;
import com.milandru.sokobani.ui.Surface;
import com.milandru.sokobani.ui.TypeSetter;

import java.util.Objects;

public final class MenuView {

    public static final int WIDTH = 250;
    public static final int HEIGHT = 252;

    private static final int GAP_CENTRE_Y = 91;
    private static final int TITLE_PANEL_X = 22;
    private static final int TITLE_PANEL_Y = 6;
    private static final int TITLE_PANEL_W = 206;
    private static final int TITLE_PANEL_H = 60;
    private static final int MENU_PANEL_X = 44;
    private static final int MENU_PANEL_Y = 116;
    private static final int MENU_PANEL_W = 162;
    private static final int MENU_PANEL_H = 122;

    private static final int ITEM_Y = 126;
    private static final int ITEM_PITCH = 18;
    private static final int SELECTION_X = 48;
    private static final int SELECTION_WIDTH = WIDTH - 2 * SELECTION_X;
    private static final int SELECTION_HEIGHT = 17;
    private static final int SELECTION_CLEARANCE = 2;
    private static final int RULE_Y = 216;
    private static final int RULE_X = 62;
    private static final int RULE_WIDTH = WIDTH - 2 * RULE_X;
    private static final int FOOTER_Y = 222;

    private static final Type.Style TITLE_STYLE = new Type.Style(20, 2.8);
    private static final Type.Style SUBTITLE_STYLE = new Type.Style(11, 1.8);
    private static final Type.Style ITEM_STYLE = new Type.Style(13, 1.4);

    public enum Item {
        PLAY("PLAY"),
        ROOMS("ROOMS"),
        HOW_TO_PLAY("HOW TO PLAY"),
        SETTINGS("SETTINGS"),
        QUIT("QUIT");

        private final String label;

        Item(String label) {
            this.label = label;
        }
    }

    private MenuView() {
    }

    public static void render(
            Surface surface, TypeSetter setter, GameState board, Progress progress, int total, int selection) {
        Objects.requireNonNull(surface, "surface");
        Objects.requireNonNull(setter, "setter");
        Objects.requireNonNull(board, "board");
        Objects.requireNonNull(progress, "progress");
        int centreX = surface.width() / 2;

        drawBoard(surface, board);
        panel(surface, TITLE_PANEL_X, TITLE_PANEL_Y, TITLE_PANEL_W, TITLE_PANEL_H);
        Type.centred(setter, surface, "SOKOBANI", centreX, Type.baseline(13, TITLE_STYLE), TITLE_STYLE, Surface.INK);
        GameView.ruleWithFleuron(surface, centreX, 43);
        Type.centred(setter, surface, "A WAREHOUSE", centreX, Type.baseline(49, SUBTITLE_STYLE), SUBTITLE_STYLE,
                Surface.INK);

        panel(surface, MENU_PANEL_X, MENU_PANEL_Y, MENU_PANEL_W, MENU_PANEL_H);
        for (int i = 0; i < Item.values().length; i++) {
            drawItem(surface, setter, i, selection == i);
        }
        surface.fill(RULE_X, RULE_Y, RULE_WIDTH, 1, Surface.INK);
        int solved = solvedCount(progress, total);
        Type.centred(setter, surface, solved + " / " + total + " SOLVED",
                centreX, Type.baseline(FOOTER_Y, Type.SUBTITLE), Type.SUBTITLE, Surface.INK);
    }

    public static int solvedCount(Progress progress, int total) {
        int solved = 0;
        for (int i = 0; i < total; i++) {
            if (progress.isSolved(i)) {
                solved++;
            }
        }
        return solved;
    }

    public static boolean overItem(int baseX, int baseY, int itemIndex) {
        int top = ITEM_Y + itemIndex * ITEM_PITCH - SELECTION_CLEARANCE;
        return baseX >= SELECTION_X
                && baseX < SELECTION_X + SELECTION_WIDTH
                && baseY >= top
                && baseY < top + SELECTION_HEIGHT;
    }

    private static void drawBoard(Surface surface, GameState board) {
        Level level = board.level();
        int boardX = (surface.width() - BoardView.width(level.columnCount())) / 2;
        int boardY = GAP_CENTRE_Y - BoardView.height(level.rowCount()) / 2;
        BoardView.draw(surface, board, boardX, boardY);
    }

    private static void drawItem(Surface surface, TypeSetter setter, int index, boolean selected) {
        Item item = Item.values()[index];
        int top = ITEM_Y + index * ITEM_PITCH - SELECTION_CLEARANCE;
        int baseline = Type.baseline(ITEM_Y + index * ITEM_PITCH, ITEM_STYLE);
        int centreX = surface.width() / 2;
        if (selected) {
            surface.fill(SELECTION_X, top, SELECTION_WIDTH, SELECTION_HEIGHT, Surface.INK);
            Type.centred(setter, surface, item.label, centreX, baseline, ITEM_STYLE, Surface.PAPER);
        } else {
            Type.centred(setter, surface, item.label, centreX, baseline, ITEM_STYLE, Surface.INK);
        }
    }

    private static void panel(Surface surface, int x, int y, int width, int height) {
        surface.fill(x, y, width, height, Surface.PAPER);
        surface.box(x, y, width, height, 1, Surface.INK);
    }
}
