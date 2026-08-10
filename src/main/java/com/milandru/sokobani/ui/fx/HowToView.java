package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.ui.Surface;
import com.milandru.sokobani.ui.TypeSetter;

import java.util.Objects;

public final class HowToView {

    public static final int WIDTH = 250;
    public static final int HEIGHT = 252;

    private static final int DEMO_TILE = 14;
    private static final int DEMO_X = (WIDTH - DEMO_TILE * 5) / 2;
    private static final int DEMO_Y = 70;
    private static final int SENTENCE_Y = 122;
    private static final int PANEL_X = 26;
    private static final int PANEL_Y = 142;
    private static final int PANEL_W = 198;
    private static final int PANEL_H = 80;
    private static final int ROW_Y = 149;
    private static final int ROW_PITCH = 18;
    private static final int KEY_X = 42;
    private static final int ACTION_X = WIDTH - 42;
    private static final int LEADER_Y_OFFSET = 8;
    private static final int LEADER_DOT_STEP = 3;
    private static final int FOOTER_Y = HEIGHT - 24;

    private static final Type.Style SENTENCE_STYLE = new Type.Style(11, 1.7);

    private static final String[] KEYS = {"WASD / ARROWS", "U / CTRL+Z", "R", "ESC"};
    private static final String[] ACTIONS = {"MOVE", "UNDO", "RESTART", "PAUSE / BACK"};

    private HowToView() {
    }

    public static void render(Surface surface, TypeSetter setter) {
        Objects.requireNonNull(surface, "surface");
        Objects.requireNonNull(setter, "setter");
        int centreX = surface.width() / 2;

        Type.centred(setter, surface, "HOW TO PLAY", centreX,
                Type.baseline(13, Type.TITLE), Type.TITLE, Surface.INK);
        GameView.ruleWithFleuron(surface, centreX, 43);
        Type.centred(setter, surface, "THE WHOLE GAME", centreX,
                Type.baseline(49, Type.SUBTITLE), Type.SUBTITLE, Surface.INK);

        drawDemoBoard(surface);
        Type.centred(setter, surface, "PUSH THE CRATE ONTO THE MARK.", centreX,
                Type.baseline(SENTENCE_Y, SENTENCE_STYLE), SENTENCE_STYLE, Surface.INK);

        surface.fill(PANEL_X, PANEL_Y, PANEL_W, PANEL_H, Surface.PAPER);
        surface.box(PANEL_X, PANEL_Y, PANEL_W, PANEL_H, 1, Surface.INK);
        for (int row = 0; row < KEYS.length; row++) {
            drawRow(surface, setter, row);
        }

        Type.centred(setter, surface, "UNDO IS UNLIMITED.", centreX,
                Type.baseline(FOOTER_Y, Type.SUBTITLE), Type.SUBTITLE, Surface.INK);
    }

    private static void drawRow(Surface surface, TypeSetter setter, int row) {
        int capTop = ROW_Y + row * ROW_PITCH;
        int baseline = Type.baseline(capTop, Type.COUNTER);
        Type.flushLeft(setter, surface, KEYS[row], KEY_X, baseline, Type.COUNTER, Surface.INK);
        Type.flushRight(setter, surface, ACTIONS[row], ACTION_X, baseline, Type.COUNTER, Surface.INK);
        int keyEnd = KEY_X + (int) Math.round(Type.width(setter, KEYS[row], Type.COUNTER));
        int actionStart = ACTION_X - (int) Math.round(Type.width(setter, ACTIONS[row], Type.COUNTER));
        int dotsY = capTop + LEADER_Y_OFFSET;
        for (int dot = keyEnd + 2; dot < actionStart - 2; dot += LEADER_DOT_STEP) {
            surface.fill(dot, dotsY, 1, 1, Surface.INK);
        }
    }

    private static void drawDemoBoard(Surface surface) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 5; column++) {
                int x = DEMO_X + column * DEMO_TILE;
                int y = DEMO_Y + row * DEMO_TILE;
                if (row == 0 || row == 2 || column == 0 || column == 4) {
                    wall(surface, x, y);
                } else if (row == 1 && column == 1) {
                    player(surface, x, y);
                } else if (row == 1 && column == 2) {
                    box(surface, x, y);
                } else if (row == 1 && column == 3) {
                    goal(surface, x, y);
                } else {
                    floor(surface, x, y);
                }
            }
        }
    }

    private static void wall(Surface surface, int x, int y) {
        surface.hatchVertical(x, y, DEMO_TILE, DEMO_TILE, 3, Surface.INK);
        surface.box(x, y, DEMO_TILE, DEMO_TILE, 1, Surface.INK);
    }

    private static void floor(Surface surface, int x, int y) {
        surface.stipple(x, y, DEMO_TILE, DEMO_TILE, 0.012, Surface.INK);
    }

    private static void goal(Surface surface, int x, int y) {
        int centre = DEMO_TILE / 2;
        surface.ring(x + centre, y + centre, 3, Surface.INK);
        surface.ring(x + centre, y + centre, 5, Surface.INK);
    }

    private static void box(Surface surface, int x, int y) {
        int inset = 2;
        int span = DEMO_TILE - 2 * inset;
        surface.hatchVertical(x + inset, y + inset, span, span, 2, Surface.INK);
        surface.box(x + inset, y + inset, span, span, 1, Surface.INK);
    }

    private static void player(Surface surface, int x, int y) {
        int centreX = x + DEMO_TILE / 2;
        surface.fill(centreX - 1, y + 2, 3, 1, Surface.INK);
        surface.fill(centreX - 2, y + 3, 5, 2, Surface.INK);
        surface.fill(centreX - 1, y + 5, 3, 1, Surface.INK);
        surface.fill(centreX - 2, y + 6, 5, 4, Surface.INK);
        surface.fill(centreX - 3, y + 10, 2, 1, Surface.INK);
        surface.fill(centreX + 1, y + 10, 2, 1, Surface.INK);
        surface.hatchHorizontal(centreX - 4, y + 11, 9, 3, 2, Surface.INK);
    }
}
