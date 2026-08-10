package com.milandru.sokobani.ui.fx;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

final class SoundBank {

    private static final String LOADER_THREAD_NAME = "sokobani-sound-bank";

    private volatile Optional<Clip> move = Optional.empty();
    private volatile Optional<Clip> push = Optional.empty();
    private volatile Optional<Clip> goal = Optional.empty();
    private volatile Optional<Clip> solved = Optional.empty();
    private volatile Thread openedOn;
    private volatile boolean muted;

    private Thread loader;

    private SoundBank(boolean muted) {
        this.muted = muted;
    }

    static SoundBank load(boolean muted) {
        SoundBank bank = new SoundBank(muted);
        bank.loader = new Thread(bank::open, LOADER_THREAD_NAME);
        bank.loader.setDaemon(true);
        bank.loader.start();
        return bank;
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

    boolean finishedOpening() {
        return openedOn != null;
    }

    Thread openedOn() {
        return openedOn;
    }

    Thread loader() {
        return loader;
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

    private void open() {
        move = clipFrom("move.wav");
        push = clipFrom("push.wav");
        goal = clipFrom("goal.wav");
        solved = clipFrom("solved.wav");
        openedOn = Thread.currentThread();
    }

    private static Optional<Clip> clipFrom(String resource) {
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
