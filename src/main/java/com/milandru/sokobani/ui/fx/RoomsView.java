package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.core.Level;
import com.milandru.sokobani.level.LevelPack;
import com.milandru.sokobani.persistence.Progress;
import com.milandru.sokobani.ui.Surface;
import com.milandru.sokobani.ui.TypeSetter;

import java.util.Objects;

public final class RoomsView {

    public static final int WIDTH = 250;
    public static final int HEIGHT = 252;

    public static final int COLUMNS = 5;
    public static final int ROWS = 3;
    public static final int PAGE_SIZE = COLUMNS * ROWS;
    public static final int PLATE_W = 41;
    public static final int PLATE_H = 46;
    public static final int COLUMN_PITCH = 47;
    public static final int ROW_PITCH = 52;
    public static final int FIRST_ROW_Y = 70;

    private static final int GRID_X = (WIDTH - (COLUMNS * PLATE_W + (COLUMNS - 1) * 6)) / 2;
    private static final int FOOTER_Y = HEIGHT - 24;

    private static final int CLEARED_BOX_X = 4;
    private static final int CLEARED_BOX_Y = 3;
    private static final int CLEARED_BOX_W = 33;
    private static final int CLEARED_BOX_H = 19;
    private static final int CRATE_MARK_SIZE = 5;
    private static final int CRATE_MARK_GAP = 2;
    private static final int CRATE_MARKS_Y = 24;
    private static final int RULE_X = 5;
    private static final int RULE_Y = 32;
    private static final int RULE_W = 31;
    private static final int BEST_Y = 34;
    private static final int BEST_BOX_X = 1;
    private static final int BEST_BOX_Y = 31;
    private static final int BEST_BOX_W = PLATE_W - 2;
    private static final int BEST_BOX_H = 13;

    private static final int LOCKED_BOX_Y = 14;
    private static final int LOCKED_BOX_W = 19;
    private static final int LOCKED_BOX_H = 18;
    private static final int LOCKED_BOX_PADDING = 1;
    private static final int LOCKED_HATCH_SPACING = 2;

    private static final int SELECTION_CLEARANCE = 4;

    private static final Type.Style NUMBER_STYLE = new Type.Style(17, 0.0);
    private static final Type.Style LOCKED_NUMBER_STYLE = new Type.Style(15, 0.0);
    private static final Type.Style BEST_STYLE = new Type.Style(10, 0.0);

    private static final String[] WORDS = {
            "ONE", "TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN", "EIGHT", "NINE", "TEN",
            "ELEVEN", "TWELVE", "THIRTEEN", "FOURTEEN", "FIFTEEN", "SIXTEEN", "SEVENTEEN",
            "EIGHTEEN", "NINETEEN", "TWENTY"
    };

    private static final String ONE_HUNDRED = "ONE HUNDRED";

    private RoomsView() {
    }

    public static void render(Surface surface, TypeSetter setter, LevelPack pack, Progress progress,
                              int selection, int page) {
        Objects.requireNonNull(surface, "surface");
        Objects.requireNonNull(setter, "setter");
        Objects.requireNonNull(pack, "pack");
        Objects.requireNonNull(progress, "progress");
        int centreX = surface.width() / 2;

        Type.centred(setter, surface, "ROOMS", centreX, Type.baseline(13, Type.TITLE), Type.TITLE, Surface.INK);
        GameView.ruleWithFleuron(surface, centreX, 43);
        Type.centred(setter, surface, wordOf(pack.size()), centreX,
                Type.baseline(49, Type.SUBTITLE), Type.SUBTITLE, Surface.INK);

        int pageCount = pageCountOf(pack.size());
        page = Math.max(0, Math.min(page, pageCount - 1));
        int pageStart = page * PAGE_SIZE;
        int pageEnd = Math.min(pack.size(), pageStart + PAGE_SIZE);

        for (int index = pageStart; index < pageEnd; index++) {
            int plate = index - pageStart;
            int px = plateX(plate);
            int py = plateY(plate);
            drawPlate(surface, setter, pack.get(index), px, py, progress, index == selection);
        }

        int solved = 0;
        for (int index = 0; index < pack.size(); index++) {
            if (progress.isSolved(index)) {
                solved++;
            }
        }
        String solvedLine = solved + " / " + pack.size() + " SOLVED";
        if (pageCount > 1) {
            Type.centred(setter, surface, "PAGE " + (page + 1) + " / " + pageCount, centreX / 2,
                    Type.baseline(FOOTER_Y, Type.SUBTITLE), Type.SUBTITLE, Surface.INK);
            Type.centred(setter, surface, solvedLine, centreX + centreX / 2,
                    Type.baseline(FOOTER_Y, Type.SUBTITLE), Type.SUBTITLE, Surface.INK);
        } else {
            Type.centred(setter, surface, solvedLine, centreX,
                    Type.baseline(FOOTER_Y, Type.SUBTITLE), Type.SUBTITLE, Surface.INK);
        }
    }

    public static int plateAt(int baseX, int baseY, int page) {
        if (page < 0) {
            return -1;
        }
        int column = Math.floorDiv(baseX - GRID_X, COLUMN_PITCH);
        int row = Math.floorDiv(baseY - FIRST_ROW_Y, ROW_PITCH);
        if (column < 0 || column >= COLUMNS || row < 0 || row >= ROWS) {
            return -1;
        }
        int px = GRID_X + column * COLUMN_PITCH;
        int py = FIRST_ROW_Y + row * ROW_PITCH;
        if (baseX >= px + PLATE_W || baseY >= py + PLATE_H) {
            return -1;
        }
        return page * PAGE_SIZE + row * COLUMNS + column;
    }

    public static int plateX(int index) {
        return GRID_X + (index % COLUMNS) * COLUMN_PITCH;
    }

    public static int plateY(int index) {
        return FIRST_ROW_Y + (index / COLUMNS) * ROW_PITCH;
    }

    private static void drawPlate(
            Surface surface, TypeSetter setter, Level level, int px, int py, Progress progress, boolean selected) {
        int crates = level.initialBoxes().size();
        boolean solved = progress.isSolved(level.index());
        int centreX = px + PLATE_W / 2;

        if (selected) {
            surface.fill(px - SELECTION_CLEARANCE, py - SELECTION_CLEARANCE,
                    PLATE_W + 2 * SELECTION_CLEARANCE, PLATE_H + 2 * SELECTION_CLEARANCE, Surface.INK);
            surface.fill(px, py, PLATE_W, PLATE_H, Surface.PAPER);
        }
        surface.box(px, py, PLATE_W, PLATE_H, 1, Surface.INK);

        if (!progress.isUnlocked(level.index())) {
            String number = String.valueOf(level.index() + 1);
            int boxWidth = lockedBoxWidth(setter, number);
            surface.hatchDiagonal(px, py, PLATE_W, PLATE_H, LOCKED_HATCH_SPACING, Surface.INK);
            surface.box(px, py, PLATE_W, PLATE_H, 1, Surface.INK);
            surface.fill(px + (PLATE_W - boxWidth) / 2, py + LOCKED_BOX_Y, boxWidth, LOCKED_BOX_H, Surface.PAPER);
            Type.centred(setter, surface, number, centreX,
                    Type.baseline(py + 15, LOCKED_NUMBER_STYLE), LOCKED_NUMBER_STYLE, Surface.INK);
            return;
        }

        switch (grainOf(level.index())) {
            case VERTICAL -> surface.hatchVertical(px, py, PLATE_W, PLATE_H, grainSpacing(crates), Surface.INK);
            case HORIZONTAL -> surface.hatchHorizontal(px, py, PLATE_W, PLATE_H, grainSpacing(crates), Surface.INK);
            case DIAGONAL -> surface.hatchDiagonal(px, py, PLATE_W, PLATE_H, grainSpacing(crates), Surface.INK);
        }
        surface.box(px, py, PLATE_W, PLATE_H, 1, Surface.INK);
        surface.fill(px + CLEARED_BOX_X, py + CLEARED_BOX_Y, CLEARED_BOX_W, CLEARED_BOX_H, Surface.PAPER);
        Type.centred(setter, surface, String.valueOf(level.index() + 1), centreX,
                Type.baseline(py + CLEARED_BOX_Y, NUMBER_STYLE), NUMBER_STYLE, Surface.INK);

        int marks = crates * CRATE_MARK_SIZE + Math.max(0, crates - 1) * CRATE_MARK_GAP;
        int marksX = px + (PLATE_W - marks) / 2;
        for (int crate = 0; crate < crates; crate++) {
            surface.hatchVertical(
                    marksX + crate * (CRATE_MARK_SIZE + CRATE_MARK_GAP), py + CRATE_MARKS_Y,
                    CRATE_MARK_SIZE, CRATE_MARK_SIZE, CRATE_MARK_GAP, Surface.INK);
        }

        if (solved) {
            surface.fill(px + BEST_BOX_X, py + BEST_BOX_Y, BEST_BOX_W, BEST_BOX_H, Surface.PAPER);
            surface.fill(px + RULE_X, py + RULE_Y, RULE_W, 1, Surface.INK);
            Type.centred(setter, surface, bestLine(progress, level.index()), centreX,
                    Type.baseline(py + BEST_Y, BEST_STYLE), BEST_STYLE, Surface.INK);
        }
    }

    private static int lockedBoxWidth(TypeSetter setter, String number) {
        int needed = (int) Math.ceil(Type.width(setter, number, LOCKED_NUMBER_STYLE)) + 2 * LOCKED_BOX_PADDING;
        return Math.min(PLATE_W - 2, Math.max(LOCKED_BOX_W, needed));
    }

    private static int grainSpacing(int crates) {
        return Math.max(2, 7 - crates);
    }

    private static Tiles.Grain grainOf(int levelIndex) {
        return switch (Math.floorMod(levelIndex, 3)) {
            case 0 -> Tiles.Grain.VERTICAL;
            case 1 -> Tiles.Grain.HORIZONTAL;
            default -> Tiles.Grain.DIAGONAL;
        };
    }

    private static String bestLine(Progress progress, int levelIndex) {
        Progress.LevelRecord record = progress.levelRecord(levelIndex).orElseThrow();
        return record.bestMoves() + "/" + record.bestPushes();
    }

    private static String wordOf(int count) {
        if (count == 100) {
            return ONE_HUNDRED;
        }
        return count >= 1 && count <= WORDS.length ? WORDS[count - 1] : String.valueOf(count);
    }

    private static int pageCountOf(int size) {
        return (size + PAGE_SIZE - 1) / PAGE_SIZE;
    }
}
