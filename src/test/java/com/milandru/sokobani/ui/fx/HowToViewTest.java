package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.ui.Surface;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HowToViewTest {

    @Test
    void render_drawnTwice_producesIdenticalPixels() {
        assertArrayEquals(howTo().tones(), howTo().tones());
    }

    @Test
    void render_isTheSpecifiedSurfaceSize() {
        Surface surface = howTo();

        assertEquals(HowToView.WIDTH, surface.width());
        assertEquals(HowToView.HEIGHT, surface.height());
    }

    @Test
    void theControlsPanelIsPaperInsideItsFrame() {
        Surface surface = howTo();

        assertEquals(Surface.PAPER, surface.toneAt(31, 147));
        assertEquals(Surface.INK, surface.toneAt(26, 142));
    }

    @Test
    void theFooterIsDrawnAtTheBottom() {
        Surface surface = howTo();

        assertTrue(inkOnRow(surface, HowToView.HEIGHT - 24) > 0);
    }

    private static Surface howTo() {
        Surface surface = new Surface(HowToView.WIDTH, HowToView.HEIGHT);
        HowToView.render(surface, BoardFixture.typeSetter());
        return surface;
    }

    private static int inkOnRow(Surface surface, int y) {
        int count = 0;
        for (int x = 0; x < surface.width(); x++) {
            if (surface.toneAt(x, y) == Surface.INK) {
                count++;
            }
        }
        return count;
    }
}
