package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.persistence.Settings;
import com.milandru.sokobani.ui.Surface;
import com.milandru.sokobani.ui.TypeSetter;

import java.util.Objects;

public final class SettingsView {

    public static final int WIDTH = 250;
    public static final int HEIGHT = 252;

    private static final int ROW_Y = 126;
    private static final int ROW_PITCH = 18;
    private static final int PANEL_X = 44;
    private static final int PANEL_Y = 116;
    private static final int PANEL_W = 162;
    private static final int PANEL_H = 74;
    private static final int SELECTION_X = 48;
    private static final int SELECTION_WIDTH = WIDTH - 2 * SELECTION_X;
    private static final int SELECTION_HEIGHT = 17;
    private static final int SELECTION_CLEARANCE = 2;
    private static final int LABEL_X = 52;
    private static final int VALUE_X = WIDTH - 52;
    private static final int FOOTER_Y = HEIGHT - 24;

    private static final Type.Style ROW_STYLE = new Type.Style(13, 1.4);

    public enum Row {
        SOUND("SOUND"),
        ANIMATION("ANIMATION"),
        HINTS("HINTS");

        private final String label;

        Row(String label) {
            this.label = label;
        }
    }

    private SettingsView() {
    }

    public static void render(Surface surface, TypeSetter setter, Settings settings, int selection) {
        Objects.requireNonNull(surface, "surface");
        Objects.requireNonNull(setter, "setter");
        Objects.requireNonNull(settings, "settings");
        int centreX = surface.width() / 2;

        Type.centred(setter, surface, "SETTINGS", centreX,
                Type.baseline(13, Type.TITLE), Type.TITLE, Surface.INK);
        GameView.ruleWithFleuron(surface, centreX, 43);
        Type.centred(setter, surface, "PREFERENCES", centreX,
                Type.baseline(49, Type.SUBTITLE), Type.SUBTITLE, Surface.INK);

        surface.fill(PANEL_X, PANEL_Y, PANEL_W, PANEL_H, Surface.PAPER);
        surface.box(PANEL_X, PANEL_Y, PANEL_W, PANEL_H, 1, Surface.INK);
        for (int i = 0; i < Row.values().length; i++) {
            drawRow(surface, setter, i, selection == i, settings);
        }

        Type.centred(setter, surface, "ENTER TOGGLES, ESC BACKS", centreX,
                Type.baseline(FOOTER_Y, Type.SUBTITLE), Type.SUBTITLE, Surface.INK);
    }

    public static boolean overRow(int baseX, int baseY, int rowIndex) {
        int top = ROW_Y + rowIndex * ROW_PITCH - SELECTION_CLEARANCE;
        return baseX >= SELECTION_X
                && baseX < SELECTION_X + SELECTION_WIDTH
                && baseY >= top
                && baseY < top + SELECTION_HEIGHT;
    }

    private static void drawRow(Surface surface, TypeSetter setter, int index, boolean selected, Settings settings) {
        Row row = Row.values()[index];
        int top = ROW_Y + index * ROW_PITCH - SELECTION_CLEARANCE;
        int baseline = Type.baseline(ROW_Y + index * ROW_PITCH, ROW_STYLE);
        String value = valueOf(row, settings);
        if (selected) {
            surface.fill(SELECTION_X, top, SELECTION_WIDTH, SELECTION_HEIGHT, Surface.INK);
            Type.flushLeft(setter, surface, row.label, LABEL_X, baseline, ROW_STYLE, Surface.PAPER);
            Type.flushRight(setter, surface, value, VALUE_X, baseline, ROW_STYLE, Surface.PAPER);
        } else {
            Type.flushLeft(setter, surface, row.label, LABEL_X, baseline, ROW_STYLE, Surface.INK);
            Type.flushRight(setter, surface, value, VALUE_X, baseline, ROW_STYLE, Surface.INK);
        }
    }

    private static String valueOf(Row row, Settings settings) {
        return switch (row) {
            case SOUND -> settings.muted() ? "OFF" : "ON";
            case ANIMATION -> settings.animationEnabled() ? "ON" : "OFF";
            case HINTS -> settings.hintsEnabled() ? "ON" : "OFF";
        };
    }
}
