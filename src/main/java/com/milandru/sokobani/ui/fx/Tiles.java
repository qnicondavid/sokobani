package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.ui.Surface;

public final class Tiles {

    public static final int TILE = 15;
    public static final int BOX_INSET = 2;
    public static final int BOX_SPAN = TILE - 2 * BOX_INSET;

    private static final int CENTRE = TILE / 2;
    private static final int HAIRLINE = 1;

    private static final int WALL_HATCH_SPACING = 3;
    private static final int EDGE_BAND_WIDTH = 5;
    private static final int EDGE_BAND_SPACING = 2;

    private static final double FLOOR_DENSITY = 0.012;

    private static final int GOAL_INNER_RADIUS = 3;
    private static final int GOAL_OUTER_RADIUS = 5;
    private static final int GOAL_RADIUS_CLEARING_A_BOX = 6;

    private static final int CRATE_BAND_WIDTH = 4;
    private static final int CRATE_BAND_SPACING = 2;
    private static final int CRATE_BAND_X = TILE - BOX_INSET - CRATE_BAND_WIDTH;
    private static final int CRATE_LINE_INSET = 4;
    private static final int CRATE_LINE_WIDTH = TILE - 2 * CRATE_LINE_INSET;

    private static final int HEAD_CROWN_Y = 3;
    private static final int HEAD_CROWN_WIDTH = 3;
    private static final int HEAD_Y = 4;
    private static final int HEAD_WIDTH = 5;
    private static final int HEAD_HEIGHT = 2;
    private static final int HEAD_CHIN_Y = 6;
    private static final int BODY_Y = 7;
    private static final int BODY_WIDTH = 5;
    private static final int BODY_HEIGHT = 4;
    private static final int FOOT_Y = 11;
    private static final int FOOT_WIDTH = 2;
    private static final int FOOT_STANCE = 3;
    private static final int GROUND_Y = 12;
    private static final int GROUND_WIDTH = 9;
    private static final int GROUND_HEIGHT = 3;
    private static final int GROUND_SPACING = 2;

    public enum Grain {
        VERTICAL,
        HORIZONTAL,
        DIAGONAL
    }

    private Tiles() {
    }

    public static void wall(Surface surface, int x, int y, Grain grain, boolean bandOnLeft, boolean bandOnRight) {
        switch (grain) {
            case VERTICAL -> surface.hatchVertical(x, y, TILE, TILE, WALL_HATCH_SPACING, Surface.INK);
            case HORIZONTAL -> surface.hatchHorizontal(x, y, TILE, TILE, WALL_HATCH_SPACING, Surface.INK);
            case DIAGONAL -> surface.hatchDiagonal(x, y, TILE, TILE, WALL_HATCH_SPACING, Surface.INK);
        }
        if (bandOnLeft) {
            surface.hatchVertical(x, y, EDGE_BAND_WIDTH, TILE, EDGE_BAND_SPACING, Surface.INK);
        }
        if (bandOnRight) {
            surface.hatchVertical(x + TILE - EDGE_BAND_WIDTH, y, EDGE_BAND_WIDTH, TILE, EDGE_BAND_SPACING, Surface.INK);
        }
        surface.box(x, y, TILE, TILE, HAIRLINE, Surface.INK);
    }

    public static void floor(Surface surface, int x, int y) {
        surface.stipple(x, y, TILE, TILE, FLOOR_DENSITY, Surface.INK);
    }

    public static void goal(Surface surface, int x, int y) {
        surface.ring(x + CENTRE, y + CENTRE, GOAL_INNER_RADIUS, Surface.INK);
        surface.ring(x + CENTRE, y + CENTRE, GOAL_OUTER_RADIUS, Surface.INK);
    }

    public static void boxOffGoal(Surface surface, int x, int y) {
        surface.hatchVertical(
                x + CRATE_BAND_X, y + BOX_INSET, CRATE_BAND_WIDTH, BOX_SPAN, CRATE_BAND_SPACING, Surface.INK);
        surface.box(x + BOX_INSET, y + BOX_INSET, BOX_SPAN, BOX_SPAN, HAIRLINE, Surface.INK);
    }

    public static void boxOnGoal(Surface surface, int x, int y) {
        surface.fill(x + BOX_INSET, y + BOX_INSET, BOX_SPAN, BOX_SPAN, Surface.INK);
        surface.fill(x + CRATE_LINE_INSET, y + CENTRE, CRATE_LINE_WIDTH, HAIRLINE, Surface.PAPER);
        surface.ring(x + CENTRE, y + CENTRE, GOAL_RADIUS_CLEARING_A_BOX, Surface.INK);
    }

    public static void player(Surface surface, int x, int y) {
        int centreX = x + CENTRE;
        centredBlock(surface, centreX, y + HEAD_CROWN_Y, HEAD_CROWN_WIDTH, HAIRLINE);
        centredBlock(surface, centreX, y + HEAD_Y, HEAD_WIDTH, HEAD_HEIGHT);
        centredBlock(surface, centreX, y + HEAD_CHIN_Y, HEAD_CROWN_WIDTH, HAIRLINE);
        centredBlock(surface, centreX, y + BODY_Y, BODY_WIDTH, BODY_HEIGHT);
        surface.fill(centreX - FOOT_STANCE, y + FOOT_Y, FOOT_WIDTH, HAIRLINE, Surface.INK);
        surface.fill(centreX + FOOT_STANCE - FOOT_WIDTH + 1, y + FOOT_Y, FOOT_WIDTH, HAIRLINE, Surface.INK);
        surface.hatchHorizontal(
                centreX - GROUND_WIDTH / 2, y + GROUND_Y, GROUND_WIDTH, GROUND_HEIGHT, GROUND_SPACING, Surface.INK);
    }

    private static void centredBlock(Surface surface, int centreX, int y, int width, int height) {
        surface.fill(centreX - width / 2, y, width, height, Surface.INK);
    }
}
