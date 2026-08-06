package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.engine.GameEvent;
import com.milandru.sokobani.persistence.Progress;
import com.milandru.sokobani.ui.Surface;
import com.milandru.sokobani.ui.Theme;
import com.milandru.sokobani.ui.Threshold;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PanelsTest {

    private static final GameEvent.Solved SOLVED =
            new GameEvent.Solved(BoardFixture.level(BoardFixture.ONE_PUSH), 12, 4);

    @Test
    void solved_isTheSizeItDeclares() {
        Surface surface = solvedPanel(Optional.empty(), true);

        assertEquals(Panels.SOLVED_WIDTH, surface.width());
        assertEquals(Panels.SOLVED_HEIGHT, surface.height());
    }

    @Test
    void solved_drawnTwice_producesIdenticalPixels() {
        assertArrayEquals(solvedPanel(Optional.empty(), true).tones(), solvedPanel(Optional.empty(), true).tones());
    }

    @Test
    void solved_isAFramedPanelWithAHairlineBorder() {
        Surface surface = solvedPanel(Optional.empty(), true);

        for (int x = 0; x < surface.width(); x++) {
            assertEquals(Surface.INK, surface.toneAt(x, 0));
            assertEquals(Surface.INK, surface.toneAt(x, surface.height() - 1));
        }
        for (int y = 0; y < surface.height(); y++) {
            assertEquals(Surface.INK, surface.toneAt(0, y));
            assertEquals(Surface.INK, surface.toneAt(surface.width() - 1, y));
        }
    }

    @Test
    void solved_withAPreviousBest_saysMoreThanWithoutOne() {
        Surface without = solvedPanel(Optional.empty(), true);
        Surface with = solvedPanel(Optional.of(new Progress.LevelRecord(true, 9, 3)), true);

        assertTrue(ink(with) > ink(without));
    }

    @Test
    void solved_onTheLastRoom_offersADifferentHintThanMidPack() {
        Surface lastRoom = solvedPanel(Optional.empty(), false);
        Surface midPack = solvedPanel(Optional.empty(), true);

        assertFalse(Arrays.equals(lastRoom.tones(), midPack.tones()));
    }

    @Test
    void solved_whenProgressCouldNotBeSaved_saysSoInPlaceOfTheBest() {
        Surface saved = Panels.solved(
                SOLVED, Optional.of(new Progress.LevelRecord(true, 9, 3)), true, true, BoardFixture.typeSetter());
        Surface unsaved = Panels.solved(
                SOLVED, Optional.of(new Progress.LevelRecord(true, 9, 3)), true, false, BoardFixture.typeSetter());
        Surface silent = Panels.solved(SOLVED, Optional.empty(), true, true, BoardFixture.typeSetter());

        assertFalse(Arrays.equals(saved.tones(), unsaved.tones()));
        assertTrue(ink(unsaved) > ink(silent));
    }

    @Test
    void paused_isASolidBandWithItsTypeKnockedOut() {
        Surface surface = Panels.paused(BoardFixture.typeSetter());

        assertEquals(Panels.PAUSED_WIDTH, surface.width());
        assertEquals(Panels.PAUSED_HEIGHT, surface.height());
        assertEquals(Surface.INK, surface.toneAt(0, 0));
        assertTrue(paper(surface) > 0);
    }

    @Test
    void paused_drawnTwice_producesIdenticalPixels() {
        assertArrayEquals(
                Panels.paused(BoardFixture.typeSetter()).tones(),
                Panels.paused(BoardFixture.typeSetter()).tones());
    }

    @Test
    void bothPanels_thresholdedToATheme_useExactlyTwoColourValues() {
        for (Surface surface : new Surface[] {solvedPanel(Optional.empty(), true), Panels.paused(BoardFixture.typeSetter())}) {
            for (Theme theme : Theme.ALL) {
                Set<Integer> distinct = new HashSet<>();
                for (int pixel : Threshold.paletteMap(surface, theme)) {
                    distinct.add(pixel);
                }

                assertEquals(2, distinct.size());
            }
        }
    }

    private static Surface solvedPanel(Optional<Progress.LevelRecord> previousBest, boolean hasNextRoom) {
        return Panels.solved(SOLVED, previousBest, hasNextRoom, true, BoardFixture.typeSetter());
    }

    private static int ink(Surface surface) {
        int count = 0;
        for (int tone : surface.tones()) {
            if (Threshold.isInk(tone)) {
                count++;
            }
        }
        return count;
    }

    private static int paper(Surface surface) {
        return surface.width() * surface.height() - ink(surface);
    }
}
