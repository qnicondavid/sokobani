package com.milandru.sokobani.persistence;

public record Settings(boolean muted, boolean animationEnabled, boolean hintsEnabled) {

    public static final Settings DEFAULT = new Settings(false, true, true);
}
