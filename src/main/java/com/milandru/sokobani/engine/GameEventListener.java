package com.milandru.sokobani.engine;

@FunctionalInterface
public interface GameEventListener {

    void onEvent(GameEvent event);
}
