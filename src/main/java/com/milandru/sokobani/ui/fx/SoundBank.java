package com.milandru.sokobani.ui.fx;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

final class SoundBank {

    private final Optional<Clip> move;
    private final Optional<Clip> push;
    private final Optional<Clip> goal;
    private final Optional<Clip> solved;
    private boolean muted;

    SoundBank(boolean muted) {
        this.muted = muted;
        this.move = load("move.wav");
        this.push = load("push.wav");
        this.goal = load("goal.wav");
        this.solved = load("solved.wav");
    }

    static SoundBank load(boolean muted) {
        return new SoundBank(muted);
    }

    void setMuted(boolean muted) {
        this.muted = muted;
        if (muted) {
            stopAll();
        }
    }

    boolean muted() {
        return muted;
    }

    void move() {
        play(move);
    }

    void push() {
        play(push);
    }

    void goal() {
        play(goal);
    }

    void solved() {
        play(solved);
    }

    private static Optional<Clip> load(String resource) {
        try (InputStream stream = SoundBank.class.getResourceAsStream("/sound/" + resource)) {
            if (stream == null) {
                return Optional.empty();
            }
            try (AudioInputStream audio = AudioSystem.getAudioInputStream(new BufferedInputStream(stream))) {
                Clip clip = AudioSystem.getClip();
                clip.open(audio);
                return Optional.of(clip);
            }
        } catch (Exception unavailable) {
            return Optional.empty();
        }
    }

    private void play(Optional<Clip> clip) {
        if (muted) {
            return;
        }
        clip.ifPresent(candidate -> {
            try {
                candidate.stop();
                candidate.setFramePosition(0);
                candidate.start();
            } catch (Exception unavailable) {
            }
        });
    }

    private void stopAll() {
        for (Optional<Clip> clip : List.of(move, push, goal, solved)) {
            clip.ifPresent(candidate -> {
                try {
                    candidate.stop();
                } catch (Exception unavailable) {
                }
            });
        }
    }
}
