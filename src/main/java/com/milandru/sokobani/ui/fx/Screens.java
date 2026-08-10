package com.milandru.sokobani.ui.fx;

import java.util.Objects;

final class Screens {

    private GameScreen game;
    private MenuScreen menu;
    private RoomsScreen rooms;
    private WinScreen win;
    private PauseScreen pause;
    private HowToScreen howTo;
    private SettingsScreen settings;

    void bind(GameScreen game, MenuScreen menu, RoomsScreen rooms, WinScreen win, PauseScreen pause,
              HowToScreen howTo, SettingsScreen settings) {
        this.game = Objects.requireNonNull(game, "game");
        this.menu = Objects.requireNonNull(menu, "menu");
        this.rooms = Objects.requireNonNull(rooms, "rooms");
        this.win = Objects.requireNonNull(win, "win");
        this.pause = Objects.requireNonNull(pause, "pause");
        this.howTo = Objects.requireNonNull(howTo, "howTo");
        this.settings = Objects.requireNonNull(settings, "settings");
    }

    GameScreen game() {
        return game;
    }

    MenuScreen menu() {
        return menu;
    }

    RoomsScreen rooms() {
        return rooms;
    }

    WinScreen win() {
        return win;
    }

    PauseScreen pause() {
        return pause;
    }

    HowToScreen howTo() {
        return howTo;
    }

    SettingsScreen settings() {
        return settings;
    }
}
