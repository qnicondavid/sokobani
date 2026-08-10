package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.core.GameState;
import com.milandru.sokobani.core.Level;
import com.milandru.sokobani.ui.Surface;
import com.milandru.sokobani.ui.TypeSetter;

import java.util.Objects;

public final class PauseView {

    public static final int WIDTH = 250;
    public static final int HEIGHT = 252;

    private static final int BAND_Y = 60;
    private static final int BAND_H = 132;
    private static final int TITLE_Y = 70;
    private static final int RULE_Y = 98;
    private static final int RULE_W = 80;
    private static final int ITEM_Y = 110;
    private static final int ITEM_PITCH = 18;
    private static final int SELECTION_X = 34;
    private static final int SELECTION_WIDTH = WIDTH - 2 * SELECTION_X;
    private static final int SELECTION_HEIGHT = 17;
    private static final int SELECTION_CLEARANCE = 2;

    private static final Type.Style TITLE_STYLE = new Type.Style(20, 2.8);

    public enum Item {
        RESUME("RESUME"),
        RESTART("RESTART"),
        ROOMS("ROOMS"),
        MAIN_MENU("MAIN MENU");

        private final String label;

        Item(String label) {
            this.label = label;
        }
    }

    private PauseView() {
    }

    public static void render(Surface surface, TypeSetter setter, GameState board, int selection) {
        Objects.requireNonNull(surface, "surface");
        Objects.requireNonNull(setter, "setter");
        Objects.requireNonNull(board, "board");
        int centreX = surface.width() / 2;

        drawBoard(surface, board);
        surface.hatchVertical(0, 0, surface.width(), surface.height(), 2, Surface.INK);

        surface.fill(0, BAND_Y, surface.width(), BAND_H, Surface.INK);
        Type.centred(setter, surface, "PAUSED", centreX, Type.baseline(TITLE_Y, TITLE_STYLE), TITLE_STYLE,
                Surface.PAPER);
        surface.fill(centreX - RULE_W / 2, RULE_Y, RULE_W, 1, Surface.PAPER);

        for (int i = 0; i < Item.values().length; i++) {
            drawItem(surface, setter, i, selection == i);
        }
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
        BoardView.draw(surface, board, boardX, 0);
    }

    private static void drawItem(Surface surface, TypeSetter setter, int index, boolean selected) {
        Item item = Item.values()[index];
        int top = ITEM_Y + index * ITEM_PITCH - SELECTION_CLEARANCE;
        int baseline = Type.baseline(ITEM_Y + index * ITEM_PITCH, Type.COUNTER);
        int centreX = surface.width() / 2;
        if (selected) {
            surface.fill(SELECTION_X, top, SELECTION_WIDTH, SELECTION_HEIGHT, Surface.PAPER);
            Type.centred(setter, surface, item.label, centreX, baseline, Type.COUNTER, Surface.INK);
        } else {
            Type.centred(setter, surface, item.label, centreX, baseline, Type.COUNTER, Surface.PAPER);
        }
    }
}
