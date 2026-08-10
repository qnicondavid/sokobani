package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.engine.GameEvent;
import com.milandru.sokobani.persistence.Progress;
import com.milandru.sokobani.ui.Surface;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WinViewTest {

    private static final GameEvent.Solved SOLVED =
            new GameEvent.Solved(BoardFixture.level(BoardFixture.ONE_PUSH), 12, 4);

    @Test
    void render_drawnTwice_producesIdenticalPixels() {
        assertArrayEquals(win(true).tones(), win(true).tones());
    }

    @Test
    void render_isTheSpecifiedSurfaceSize() {
        Surface surface = win(true);

        assertEquals(WinView.WIDTH, surface.width());
        assertEquals(WinView.HEIGHT, surface.height());
    }

    @Test
    void theNextRoomBarIsInkOnlyWhenAnotherRoomFollows() {
        Surface next = win(true);
        Surface last = win(false);

        assertTrue(inkOnRow(next, 164, 40, 210) > 100);
        assertEquals(0, inkOnRow(last, 164, 40, 210));
    }

    @Test
    void aPreviousBestAddsInkWhereNoneWasBefore() {
        Surface without = win(true, Optional.empty());
        Surface with = win(true, Optional.of(new Progress.LevelRecord(true, 9, 3)));

        assertTrue(ink(with) > ink(without));
    }

    @Test
    void targetAt_answersTheClickTargets() {
        assertEquals(WinView.Target.NEXT_ROOM, WinView.targetAt(60, 170, true));
        assertEquals(WinView.Target.NEXT_ROOM, WinView.targetAt(200, 170, true));
        assertEquals(WinView.Target.NONE, WinView.targetAt(60, 170, false));
        assertEquals(WinView.Target.REPLAY, WinView.targetAt(85, 195, true));
        assertEquals(WinView.Target.ROOMS, WinView.targetAt(165, 195, true));
        assertEquals(WinView.Target.NONE, WinView.targetAt(30, 195, true));
        assertEquals(WinView.Target.NONE, WinView.targetAt(125, 120, true));
    }

    private static Surface win(boolean hasNextRoom) {
        return win(hasNextRoom, Optional.empty());
    }

    private static Surface win(boolean hasNextRoom, Optional<Progress.LevelRecord> best) {
        Surface surface = new Surface(WinView.WIDTH, WinView.HEIGHT);
        WinView.render(surface, BoardFixture.typeSetter(), BoardFixture.state(BoardFixture.ONE_PUSH),
                new WinData(SOLVED, best), hasNextRoom);
        return surface;
    }

    private static int inkOnRow(Surface surface, int y, int fromX, int toX) {
        int count = 0;
        for (int x = fromX; x < toX; x++) {
            if (surface.toneAt(x, y) == Surface.INK) {
                count++;
            }
        }
        return count;
    }

    private static int ink(Surface surface) {
        int count = 0;
        for (int tone : surface.tones()) {
            if (tone == Surface.INK) {
                count++;
            }
        }
        return count;
    }
}
