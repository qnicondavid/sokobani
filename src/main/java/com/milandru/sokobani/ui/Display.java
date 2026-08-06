package com.milandru.sokobani.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public final class Display {

    private Display() {
    }

    public static void present(Canvas canvas, Theme theme, int[] basePixels, int baseWidth, int baseHeight, int factor) {
        int[] scaled = Blit.upscale(basePixels, baseWidth, baseHeight, factor);
        int scaledWidth = baseWidth * factor;
        int scaledHeight = baseHeight * factor;

        WritableImage image = new WritableImage(scaledWidth, scaledHeight);
        PixelWriter writer = image.getPixelWriter();
        int[] argb = new int[scaled.length];
        for (int i = 0; i < scaled.length; i++) {
            argb[i] = 0xFF000000 | scaled[i];
        }
        writer.setPixels(0, 0, scaledWidth, scaledHeight, PixelFormat.getIntArgbInstance(), argb, 0, scaledWidth);

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setImageSmoothing(false);
        gc.setFill(rgb(theme.paper()));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        int offsetX = (int) Math.floor((canvas.getWidth() - scaledWidth) / 2.0);
        int offsetY = (int) Math.floor((canvas.getHeight() - scaledHeight) / 2.0);
        gc.drawImage(image, offsetX, offsetY);
    }

    private static Color rgb(int packed) {
        int r = (packed >> 16) & 0xFF;
        int g = (packed >> 8) & 0xFF;
        int b = packed & 0xFF;
        return Color.rgb(r, g, b);
    }
}
