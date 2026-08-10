package com.milandru.sokobani.ui;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FxToolkitTest {

    private static final int KNOWN_ARGB = 0xFF0C2238;

    @Test
    void on_runsTheWorkOnTheApplicationThread() {
        assertTrue(FxToolkit.on(Platform::isFxApplicationThread));
    }

    @Test
    void start_bringsUpTheHeadlessGlassPlatformRatherThanTheDesktopOne() {
        String platform = FxToolkit.on(() -> com.sun.glass.ui.Application.GetApplication().getClass().getName());

        assertTrue(platform.contains("monocle"), "glass platform in use is " + platform);
    }

    @Test
    void snapshot_ofACanvasFilledWithOneKnownColour_readsThatColourBack() {
        assertEquals(KNOWN_ARGB, FxToolkit.on(() -> {
            Canvas canvas = new Canvas(8, 8);
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.setFill(Color.rgb(0x0C, 0x22, 0x38));
            gc.fillRect(0, 0, 8, 8);
            return canvas.snapshot(null, null).getPixelReader().getArgb(4, 4);
        }));
    }

    @Test
    void snapshot_ofAHalfPaintedCanvas_separatesThePaintedSideFromTheUnpainted() {
        int[] corners = FxToolkit.on(() -> {
            Canvas canvas = new Canvas(8, 8);
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.setFill(Color.WHITE);
            gc.fillRect(0, 0, 8, 8);
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, 4, 8);
            var reader = canvas.snapshot(null, null).getPixelReader();
            return new int[]{reader.getArgb(1, 4), reader.getArgb(6, 4)};
        });

        assertEquals(0xFF000000, corners[0]);
        assertEquals(0xFFFFFFFF, corners[1]);
    }

    @Test
    void start_calledTwice_leavesTheToolkitRunning() {
        FxToolkit.start();
        FxToolkit.start();

        assertTrue(FxToolkit.on(Platform::isFxApplicationThread));
    }
}
