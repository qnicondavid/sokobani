package com.milandru.sokobani.ui;

public final class Scaling {

    public static final int MIN_FACTOR = 2;
    public static final int MAX_FACTOR = 6;

    private Scaling() {
    }

    public static int factor(int viewWidth, int viewHeight, int baseWidth, int baseHeight, int margin) {
        if (baseWidth <= 0 || baseHeight <= 0) {
            throw new IllegalArgumentException("base dimensions must be positive, got " + baseWidth + "x" + baseHeight);
        }
        int byWidth = (viewWidth - 2 * margin) / baseWidth;
        int byHeight = (viewHeight - 2 * margin) / baseHeight;
        int n = Math.min(byWidth, byHeight);
        return clamp(n, MIN_FACTOR, MAX_FACTOR);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
