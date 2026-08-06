package com.milandru.sokobani.ui;

public final class Threshold {

    public static final int LEVEL = 128;

    private Threshold() {
    }

    public static boolean isInk(int tone) {
        return tone < LEVEL;
    }

    public static int[] paletteMap(Surface surface, Theme theme) {
        int width = surface.width();
        int height = surface.height();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixels[y * width + x] = isInk(surface.toneAt(x, y)) ? theme.ink() : theme.paper();
            }
        }
        return pixels;
    }
}
