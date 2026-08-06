package com.milandru.sokobani.ui;

final class SurfacePatterns {

    static final int WIDTH = 40;
    static final int HEIGHT = 30;

    private SurfacePatterns() {
    }

    static Surface draw() {
        Surface surface = new Surface(WIDTH, HEIGHT);
        surface.fill(2, 2, 10, 10, Surface.INK);
        surface.hatchVertical(15, 2, 10, 10, 3, Surface.INK);
        surface.hatchHorizontal(15, 15, 10, 10, 3, Surface.INK);
        surface.hatchDiagonal(2, 15, 10, 10, 4, Surface.INK);
        surface.stipple(28, 2, 10, 10, 0.15, Surface.INK);
        surface.ring(33, 22, 5, Surface.INK);
        surface.box(2, 22, 12, 8, 1, Surface.INK);
        surface.invert(20, 22, 6, 6);
        return surface;
    }
}
