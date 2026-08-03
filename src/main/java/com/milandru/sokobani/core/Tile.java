package com.milandru.sokobani.core;

public enum Tile {

    WALL,
    FLOOR,
    GOAL;

    public boolean isWalkable() {
        return this != WALL;
    }
}
