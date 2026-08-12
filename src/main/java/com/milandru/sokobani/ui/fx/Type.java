package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.ui.Surface;
import com.milandru.sokobani.ui.TypeSetter;

public final class Type {

    public record Style(int size, double tracking) {
    }

    public static final Style TITLE = new Style(20, 2.6);
    public static final Style SUBTITLE = new Style(11, 2.0);
    public static final Style COUNTER = new Style(12, 1.0);
    public static final Style CAPTION = new Style(10, 1.7);
    public static final Style FIGURE = new Style(17, 0.0);
    public static final Style WIN_FIGURE = new Style(18, 0.0);

    private static final double CAP_HEIGHT_RATIO = 0.75;

    private Type() {
    }

    public static int baseline(int capTop, Style style) {
        return capTop + (int) Math.round(style.size() * CAP_HEIGHT_RATIO);
    }

    public static double width(TypeSetter setter, String text, Style style) {
        return text.isEmpty() ? 0 : setter.measure(text, style.size(), style.tracking()) - style.tracking();
    }

    public static void centred(
            TypeSetter setter, Surface surface, String text, int centreX, int baseline, Style style, int tone) {
        draw(setter, surface, text, centreX - halfWidth(setter, text, style), baseline, style, tone);
    }

    public static void flushLeft(
            TypeSetter setter, Surface surface, String text, int left, int baseline, Style style, int tone) {
        draw(setter, surface, text, left, baseline, style, tone);
    }

    public static void flushRight(
            TypeSetter setter, Surface surface, String text, int right, int baseline, Style style, int tone) {
        draw(setter, surface, text, right - (int) Math.round(width(setter, text, style)), baseline, style, tone);
    }

    private static void draw(
            TypeSetter setter, Surface surface, String text, int x, int baseline, Style style, int tone) {
        setter.draw(surface, text, x, baseline, style.size(), style.tracking(), tone);
    }

    private static int halfWidth(TypeSetter setter, String text, Style style) {
        return (int) Math.round(width(setter, text, style) / 2.0);
    }
}
