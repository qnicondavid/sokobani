package com.milandru.sokobani.ui;

public interface GlyphRasterizer {

    double advance(char glyph, int size);

    void paint(Surface surface, int x, int baseline, char glyph, int size, int tone);
}
