package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.persistence.Progress;

import java.util.Objects;
import java.util.Optional;

public final class RoomsGrid {

    public static final int COLUMNS = RoomsView.COLUMNS;
    public static final int PAGE_SIZE = RoomsView.PAGE_SIZE;

    private final Progress progress;
    private final int levelCount;
    private int selection;
    private int page;

    public RoomsGrid(Progress progress, int levelCount) {
        this.progress = Objects.requireNonNull(progress, "progress");
        this.levelCount = levelCount;
        if (levelCount <= 0) {
            throw new IllegalArgumentException("a grid needs at least one level");
        }
        this.selection = Math.min(progress.unlockedIndex(), levelCount - 1);
        this.page = pageOf(selection);
    }

    public int selection() {
        return selection;
    }

    public int page() {
        return page;
    }

    public int pageCount() {
        return (levelCount + PAGE_SIZE - 1) / PAGE_SIZE;
    }

    public boolean isUnlocked(int levelIndex) {
        return levelIndex >= 0 && levelIndex < levelCount && progress.isUnlocked(levelIndex);
    }

    public void moveUp() {
        move(-RoomsGrid.COLUMNS);
    }

    public void moveDown() {
        move(RoomsGrid.COLUMNS);
    }

    public void moveLeft() {
        move(-1);
    }

    public void moveRight() {
        move(1);
    }

    public void flipPage(int delta) {
        if (delta == 0) {
            return;
        }
        page = Math.floorMod(page + delta, pageCount());
        selection = firstUnlockedOnPage(page).orElse(page * PAGE_SIZE);
    }

    public Optional<Integer> confirm() {
        return isUnlocked(selection) ? Optional.of(selection) : Optional.empty();
    }

    public boolean select(int levelIndex) {
        if (!isUnlocked(levelIndex)) {
            return false;
        }
        selection = levelIndex;
        page = pageOf(levelIndex);
        return true;
    }

    private int pageOf(int levelIndex) {
        return levelIndex / PAGE_SIZE;
    }

    private int pageStart(int page) {
        return page * PAGE_SIZE;
    }

    private int pageEnd(int page) {
        return Math.min(levelCount, pageStart(page) + PAGE_SIZE);
    }

    private Optional<Integer> firstUnlockedOnPage(int page) {
        for (int index = pageStart(page); index < pageEnd(page); index++) {
            if (isUnlocked(index)) {
                return Optional.of(index);
            }
        }
        return Optional.empty();
    }

    private void move(int delta) {
        if (delta == 0) {
            return;
        }
        int start = pageStart(page);
        int end = pageEnd(page);
        int size = end - start;
        int next = selection;
        while (true) {
            next = start + Math.floorMod(next - start + delta, size);
            if (next == selection) {
                return;
            }
            if (isUnlocked(next)) {
                selection = next;
                return;
            }
        }
    }
}
