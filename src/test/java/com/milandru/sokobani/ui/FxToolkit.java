package com.milandru.sokobani.ui;

import javafx.application.Platform;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class FxToolkit {

    private static final long STARTUP_TIMEOUT_SECONDS = 60;
    private static final long WORK_TIMEOUT_SECONDS = 60;

    private static boolean started;

    private FxToolkit() {
    }

    public static synchronized void start() {
        if (started) {
            return;
        }
        CountDownLatch ready = new CountDownLatch(1);
        try {
            Platform.startup(ready::countDown);
        } catch (IllegalStateException alreadyRunning) {
            started = true;
            return;
        }
        await(ready, STARTUP_TIMEOUT_SECONDS, "the JavaFX toolkit did not start");
        Platform.setImplicitExit(false);
        started = true;
    }

    public static <T> T on(Callable<T> work) {
        start();
        AtomicReference<T> result = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        Runnable body = () -> {
            try {
                result.set(work.call());
            } catch (Throwable thrown) {
                failure.set(thrown);
            } finally {
                done.countDown();
            }
        };
        if (Platform.isFxApplicationThread()) {
            body.run();
        } else {
            Platform.runLater(body);
            await(done, WORK_TIMEOUT_SECONDS, "the JavaFX application thread did not finish");
        }
        rethrow(failure.get());
        return result.get();
    }

    public static void run(Runnable work) {
        on(() -> {
            work.run();
            return null;
        });
    }

    private static void rethrow(Throwable failure) {
        if (failure == null) {
            return;
        }
        if (failure instanceof RuntimeException runtime) {
            throw runtime;
        }
        if (failure instanceof Error error) {
            throw error;
        }
        throw new IllegalStateException("the JavaFX application thread threw", failure);
    }

    private static void await(CountDownLatch latch, long seconds, String complaint) {
        try {
            if (!latch.await(seconds, TimeUnit.SECONDS)) {
                throw new IllegalStateException(complaint + " within " + seconds + " seconds");
            }
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(complaint, interrupted);
        }
    }
}
