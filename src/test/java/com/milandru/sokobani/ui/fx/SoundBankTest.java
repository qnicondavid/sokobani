package com.milandru.sokobani.ui.fx;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SoundBankTest {

    private static final long OPENS_WITHIN_MILLIS = 30_000;

    @Test
    void load_neverThrows_evenWithoutAnAudioDevice() {
        SoundBank bank = SoundBank.load(false);

        bank.move();
        bank.push();
        bank.goal();
        bank.solved();
        bank.setMuted(true);
        bank.setMuted(false);
    }

    @Test
    void mutedBankPlaysNothing() {
        SoundBank bank = SoundBank.load(true);

        bank.move();
        bank.push();
        bank.goal();
        bank.solved();
    }

    @Test
    void mutedFlagIsReported() {
        assertTrue(SoundBank.load(true).muted());
        assertFalse(SoundBank.load(false).muted());
    }

    @Test
    void load_opensTheClipsOnItsOwnThreadRatherThanTheCallersOne() {
        SoundBank bank = SoundBank.load(true);
        assertTrue(awaitOpen(bank), "the sound bank never finished opening");

        assertNotSame(Thread.currentThread(), bank.openedOn(),
                "the clips were opened on the thread that asked for the bank");
        assertSame(bank.loader(), bank.openedOn());
    }

    @Test
    void load_returnsABankThatCanBePlayedBeforeItsClipsAreOpen() {
        SoundBank bank = SoundBank.load(false);

        bank.move();
        bank.solved();

        assertTrue(awaitOpen(bank), "the sound bank never finished opening");
    }

    private static boolean awaitOpen(SoundBank bank) {
        try {
            bank.loader().join(OPENS_WITHIN_MILLIS);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("interrupted while waiting for the sound bank", interrupted);
        }
        return bank.finishedOpening();
    }
}
