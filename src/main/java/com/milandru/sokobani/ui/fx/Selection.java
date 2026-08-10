package com.milandru.sokobani.ui.fx;

import java.util.List;
import java.util.Objects;

public final class Selection<T> {

    private final List<T> items;
    private int index;

    public Selection(List<T> items) {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("a selection needs at least one item");
        }
        this.items = List.copyOf(items);
    }

    public int index() {
        return index;
    }

    public int size() {
        return items.size();
    }

    public T selected() {
        return items.get(index);
    }

    public void move(int delta) {
        index = Math.floorMod(index + delta, items.size());
    }

    public void select(int wanted) {
        if (wanted >= 0 && wanted < items.size()) {
            index = wanted;
        }
    }

    public boolean isSelected(int itemIndex) {
        return index == itemIndex;
    }

    @Override
    public String toString() {
        return Objects.requireNonNull(selected()).toString();
    }
}
