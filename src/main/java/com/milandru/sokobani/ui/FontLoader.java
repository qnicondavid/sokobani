package com.milandru.sokobani.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public final class FontLoader implements GlyphRasterizer {

    private static final String RESOURCE_PATH = "/fonts/RobotoSlab-Bold.ttf";
    private static final double STROKE_WIDTH = 0.9;

    private final String family;
    private final Map<Long, Double> advanceCache = new HashMap<>();
    private final Map<Long, double[]> coverageCache = new HashMap<>();

    public FontLoader() {
        try (InputStream stream = FontLoader.class.getResourceAsStream(RESOURCE_PATH)) {
            if (stream == null) {
                throw new IllegalStateException("bundled font not found on classpath: " + RESOURCE_PATH);
            }
            Font loaded = Font.loadFont(stream, 12);
            if (loaded == null) {
                throw new IllegalStateException("JavaFX could not parse the bundled font: " + RESOURCE_PATH);
            }
            this.family = loaded.getFamily();
        } catch (IOException e) {
            throw new IllegalStateException("failed to read bundled font: " + RESOURCE_PATH, e);
        }
    }

    @Override
    public double advance(char glyph, int size) {
        return advanceCache.computeIfAbsent(key(glyph, size), unused -> {
            Text measurer = new Text(String.valueOf(glyph));
            measurer.setFont(Font.font(family, FontWeight.BOLD, size));
            return measurer.getLayoutBounds().getWidth();
        });
    }

    @Override
    public void paint(Surface surface, int x, int baseline, char glyph, int size, int tone) {
        int canvasSize = size * 2;
        double[] coverage = coverageCache.computeIfAbsent(key(glyph, size), unused -> rasterize(glyph, size, canvasSize));

        int surfaceOriginY = baseline - size;
        for (int py = 0; py < canvasSize; py++) {
            for (int px = 0; px < canvasSize; px++) {
                double pixelCoverage = coverage[py * canvasSize + px];
                if (pixelCoverage > 0) {
                    surface.blend(x + px, surfaceOriginY + py, tone, pixelCoverage);
                }
            }
        }
    }

    private double[] rasterize(char glyph, int size, int canvasSize) {
        Canvas canvas = new Canvas(canvasSize, canvasSize);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvasSize, canvasSize);
        gc.setFont(Font.font(family, FontWeight.BOLD, size));
        gc.setLineWidth(STROKE_WIDTH);
        gc.setLineJoin(StrokeLineJoin.ROUND);
        gc.setStroke(Color.BLACK);
        gc.setFill(Color.BLACK);

        String glyphText = String.valueOf(glyph);
        gc.strokeText(glyphText, 0, size);
        gc.fillText(glyphText, 0, size);

        WritableImage snapshot = canvas.snapshot(null, null);
        PixelReader reader = snapshot.getPixelReader();
        double[] coverage = new double[canvasSize * canvasSize];
        for (int py = 0; py < canvasSize; py++) {
            for (int px = 0; px < canvasSize; px++) {
                Color sampled = reader.getColor(px, py);
                double luminance = 255 * (0.299 * sampled.getRed() + 0.587 * sampled.getGreen() + 0.114 * sampled.getBlue());
                coverage[py * canvasSize + px] = 1.0 - luminance / 255.0;
            }
        }
        return coverage;
    }

    private static long key(char glyph, int size) {
        return ((long) glyph << 32) | (size & 0xFFFFFFFFL);
    }
}
