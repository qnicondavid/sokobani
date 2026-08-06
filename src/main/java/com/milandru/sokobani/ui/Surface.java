package com.milandru.sokobani.ui;

public final class Surface {

    public static final int INK = 0;
    public static final int PAPER = 255;

    private final int width;
    private final int height;
    private final int[] tones;

    public Surface(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("surface dimensions must be positive, got " + width + "x" + height);
        }
        this.width = width;
        this.height = height;
        this.tones = new int[width * height];
        fill(0, 0, width, height, PAPER);
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int toneAt(int x, int y) {
        if (!inBounds(x, y)) {
            throw new IndexOutOfBoundsException("(" + x + ", " + y + ") is outside a " + width + "x" + height + " surface");
        }
        return tones[index(x, y)];
    }

    public int[] tones() {
        return tones.clone();
    }

    public void fill(int x, int y, int w, int h, int tone) {
        for (int py = y; py < y + h; py++) {
            for (int px = x; px < x + w; px++) {
                set(px, py, tone);
            }
        }
    }

    public void box(int x, int y, int w, int h, int thickness, int tone) {
        fill(x, y, w, thickness, tone);
        fill(x, y + h - thickness, w, thickness, tone);
        fill(x, y, thickness, h, tone);
        fill(x + w - thickness, y, thickness, h, tone);
    }

    public void hatchVertical(int x, int y, int w, int h, int spacing, int tone) {
        requirePositiveSpacing(spacing);
        for (int px = x; px < x + w; px += spacing) {
            fill(px, y, 1, h, tone);
        }
    }

    public void hatchHorizontal(int x, int y, int w, int h, int spacing, int tone) {
        requirePositiveSpacing(spacing);
        for (int py = y; py < y + h; py += spacing) {
            fill(x, py, w, 1, tone);
        }
    }

    public void hatchDiagonal(int x, int y, int w, int h, int spacing, int tone) {
        requirePositiveSpacing(spacing);
        for (int py = y; py < y + h; py++) {
            for (int px = x; px < x + w; px++) {
                if (Math.floorMod(px - py, spacing) == 0) {
                    set(px, py, tone);
                }
            }
        }
    }

    public void stipple(int x, int y, int w, int h, double density, int tone) {
        for (int py = y; py < y + h; py++) {
            for (int px = x; px < x + w; px++) {
                if (noise(px, py) < density) {
                    set(px, py, tone);
                }
            }
        }
    }

    public void ring(int cx, int cy, int r, int tone) {
        int x = r;
        int y = 0;
        int err = 0;
        while (x >= y) {
            plotRingOctants(cx, cy, x, y, tone);
            y++;
            if (err <= 0) {
                err += 2 * y + 1;
            }
            if (err > 0) {
                x--;
                err -= 2 * x + 1;
            }
        }
    }

    public void invert(int x, int y, int w, int h) {
        for (int py = y; py < y + h; py++) {
            for (int px = x; px < x + w; px++) {
                if (inBounds(px, py)) {
                    int idx = index(px, py);
                    tones[idx] = 255 - tones[idx];
                }
            }
        }
    }

    public void blend(int x, int y, int tone, double coverage) {
        if (tone < INK || tone > PAPER) {
            throw new IllegalArgumentException("tone must be in [0, 255], got " + tone);
        }
        if (!(coverage >= 0.0 && coverage <= 1.0)) {
            throw new IllegalArgumentException("coverage must be in [0, 1], got " + coverage);
        }
        if (inBounds(x, y)) {
            int idx = index(x, y);
            tones[idx] = (int) Math.round(tones[idx] + coverage * (tone - tones[idx]));
        }
    }

    private void plotRingOctants(int cx, int cy, int x, int y, int tone) {
        set(cx + x, cy + y, tone);
        set(cx + y, cy + x, tone);
        set(cx - y, cy + x, tone);
        set(cx - x, cy + y, tone);
        set(cx - x, cy - y, tone);
        set(cx - y, cy - x, tone);
        set(cx + y, cy - x, tone);
        set(cx + x, cy - y, tone);
    }

    private void set(int x, int y, int tone) {
        if (tone < INK || tone > PAPER) {
            throw new IllegalArgumentException("tone must be in [0, 255], got " + tone);
        }
        if (inBounds(x, y)) {
            tones[index(x, y)] = tone;
        }
    }

    private boolean inBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    private int index(int x, int y) {
        return y * width + x;
    }

    private static void requirePositiveSpacing(int spacing) {
        if (spacing <= 0) {
            throw new IllegalArgumentException("hatch spacing must be positive, got " + spacing);
        }
    }

    private static double noise(int x, int y) {
        int h = x * 374761393 + y * 668265263;
        h = (h ^ (h >>> 13)) * 1274126177;
        h = h ^ (h >>> 16);
        return (h & 0x7FFFFFFF) / (double) Integer.MAX_VALUE;
    }
}
