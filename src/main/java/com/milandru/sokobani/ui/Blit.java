package com.milandru.sokobani.ui;

public final class Blit {

    private Blit() {
    }

    public static int[] upscale(int[] pixels, int width, int height, int factor) {
        int scaledWidth = width * factor;
        int scaledHeight = height * factor;
        int[] out = new int[scaledWidth * scaledHeight];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color = pixels[y * width + x];
                for (int dy = 0; dy < factor; dy++) {
                    int rowStart = (y * factor + dy) * scaledWidth + x * factor;
                    for (int dx = 0; dx < factor; dx++) {
                        out[rowStart + dx] = color;
                    }
                }
            }
        }
        return out;
    }
}
