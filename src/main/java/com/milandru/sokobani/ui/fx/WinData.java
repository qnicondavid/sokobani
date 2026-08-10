package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.engine.GameEvent;
import com.milandru.sokobani.persistence.Progress;

import java.util.Objects;
import java.util.Optional;

public record WinData(GameEvent.Solved solved, Optional<Progress.LevelRecord> bestBeforeThisAttempt) {

    public WinData {
        Objects.requireNonNull(solved, "solved");
        Objects.requireNonNull(bestBeforeThisAttempt, "bestBeforeThisAttempt");
    }
}
