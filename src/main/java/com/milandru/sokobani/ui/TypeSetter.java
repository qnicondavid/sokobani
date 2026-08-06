package com.milandru.sokobani.ui;

public final class TypeSetter {

    public static final int MIN_SIZE = 10;

    private final GlyphRasterizer rasterizer;

    public TypeSetter(GlyphRasterizer rasterizer) {
        this.rasterizer = rasterizer;
    }

    public double measure(String text, int size, double tracking) {
        requireSize(size);
        double width = 0;
        for (int i = 0; i < text.length(); i++) {
            width += rasterizer.advance(text.charAt(i), size) + tracking;
        }
        return width;
    }

    public void draw(Surface surface, String text, int x, int y, int size, double tracking, int tone) {
        requireSize(size);
        double cursor = x;
        for (int i = 0; i < text.length(); i++) {
            char glyph = text.charAt(i);
            rasterizer.paint(surface, (int) Math.round(cursor), y, glyph, size, tone);
            cursor += rasterizer.advance(glyph, size) + tracking;
        }
    }

    private static void requireSize(int size) {
        if (size < MIN_SIZE) {
            throw new IllegalArgumentException("text size " + size + " is below the " + MIN_SIZE + "px floor");
        }
    }
}
