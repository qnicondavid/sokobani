package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.persistence.Settings;
import com.milandru.sokobani.persistence.SettingsStore;

import java.util.Objects;

final class SettingsState {

    private final SettingsStore store;
    private boolean muted;
    private boolean animationEnabled;
    private boolean hintsEnabled;

    SettingsState(SettingsStore store) {
        this.store = Objects.requireNonNull(store, "store");
        Settings settings = store.load();
        this.muted = settings.muted();
        this.animationEnabled = settings.animationEnabled();
        this.hintsEnabled = settings.hintsEnabled();
    }

    boolean muted() {
        return muted;
    }

    boolean animationEnabled() {
        return animationEnabled;
    }

    boolean hintsEnabled() {
        return hintsEnabled;
    }

    Settings snapshot() {
        return new Settings(muted, animationEnabled, hintsEnabled);
    }

    void toggleMuted() {
        muted = !muted;
        store.save(new Settings(muted, animationEnabled, hintsEnabled));
    }

    void toggleAnimation() {
        animationEnabled = !animationEnabled;
        store.save(new Settings(muted, animationEnabled, hintsEnabled));
    }

    void toggleHints() {
        hintsEnabled = !hintsEnabled;
        store.save(new Settings(muted, animationEnabled, hintsEnabled));
    }
}
