package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.ui.Surface;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PauseViewTest {

    @Test
    void render_drawnTwice_producesIdenticalPixels() {
        assertArrayEquals(pause(0).tones(), pause(0).tones());
    }

    @Test
    void render_isTheSpecifiedSurfaceSize() {
        Surface surface = pause(0);

        assertEquals(PauseView.WIDTH, surface.width());
        assertEquals(PauseView.HEIGHT, surface.height());
    }

    @Test
    void theBandIsSolidInkWithTheTypeKnockedOut() {
        Surface surface = pause(0);

        assertEquals(Surface.INK, surface.toneAt(5, 70));
        assertEquals(Surface.INK, surface.toneAt(245, 180));
    }

    @Test
    void theSelectionBarFollowsTheSelection() {
        assertFalse(Arrays.equals(pause(0).tones(), pause(1).tones()));
    }

    @Test
    void overItem_answersTheSelectionBandOfEachItem() {
        assertTrue(PauseView.overItem(100, 112, 0));
        assertFalse(PauseView.overItem(100, 130, 0));
        assertTrue(PauseView.overItem(100, 130, 1));
        assertFalse(PauseView.overItem(100, 145, 1));
        assertTrue(PauseView.overItem(100, 150, 2));
        assertFalse(PauseView.overItem(10, 112, 0));
    }

    private static Surface pause(int selection) {
        Surface surface = new Surface(PauseView.WIDTH, PauseView.HEIGHT);
        PauseView.render(surface, BoardFixture.typeSetter(), BoardFixture.state(BoardFixture.ONE_PUSH), selection);
        return surface;
    }
}
