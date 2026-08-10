package com.milandru.sokobani.ui.fx;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SoundBankTest {

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
}
