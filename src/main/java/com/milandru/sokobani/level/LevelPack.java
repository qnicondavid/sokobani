package com.milandru.sokobani.level;

import com.milandru.sokobani.core.Level;

import java.util.List;
import java.util.Objects;

public final class LevelPack {

    private final String name;
    private final List<Level> levels;

    public LevelPack(String name, List<Level> levels) {
        this.name = Objects.requireNonNull(name, "name");
        this.levels = List.copyOf(Objects.requireNonNull(levels, "levels"));
    }

    public String name() {
        return name;
    }

    public int size() {
        return levels.size();
    }

    public Level get(int index) {
        return levels.get(index);
    }

    public List<Level> levels() {
        return levels;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof LevelPack pack
                && name.equals(pack.name)
                && levels.equals(pack.levels);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, levels);
    }

    @Override
    public String toString() {
        return "LevelPack[name=" + name + ", size=" + size() + "]";
    }
}
