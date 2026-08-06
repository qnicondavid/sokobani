package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.engine.GameEvent;
import com.milandru.sokobani.persistence.Progress;
import com.milandru.sokobani.ui.Surface;
import com.milandru.sokobani.ui.TypeSetter;

import java.util.Objects;
import java.util.Optional;

public final class Panels {

    public static final int SOLVED_WIDTH = 170;
    public static final int SOLVED_HEIGHT = 118;
    public static final int PAUSED_WIDTH = 170;
    public static final int PAUSED_HEIGHT = 68;

    private static final String SOLVED = "SOLVED";
    private static final String PAUSED = "PAUSED";
    private static final String MOVES = "MOVES";
    private static final String PUSHES = "PUSHES";
    private static final String NEXT_OR_REPLAY = "N NEXT ROOM   R REPLAY";
    private static final String REPLAY_ONLY = "R REPLAY";
    private static final String RESUME_OR_RESTART = "ESC RESUME   R RESTART";
    private static final String NOT_SAVED = "PROGRESS NOT SAVED";

    private static final int HAIRLINE = 1;
    private static final int SOLVED_TOP = 14;
    private static final int ROOM_TOP = 36;
    private static final int DIVIDER_Y = 54;
    private static final int DIVIDER_HEIGHT = 26;
    private static final int FIGURE_TOP = 58;
    private static final int LABEL_TOP = 76;
    private static final int BEST_TOP = 90;
    private static final int HINT_TOP = 104;
    private static final int FIGURE_SPREAD = 38;

    private static final int PAUSED_TOP = 13;
    private static final int PAUSED_RULE_Y = 35;
    private static final int PAUSED_RULE_WIDTH = 80;
    private static final int PAUSED_HINT_TOP = 45;

    private Panels() {
    }

    public static Surface solved(
            GameEvent.Solved solved,
            Optional<Progress.LevelRecord> previousBest,
            boolean hasNextRoom,
            boolean progressSaved,
            TypeSetter setter) {
        Objects.requireNonNull(solved, "solved");
        Objects.requireNonNull(previousBest, "previousBest");
        Objects.requireNonNull(setter, "setter");

        Surface surface = new Surface(SOLVED_WIDTH, SOLVED_HEIGHT);
        surface.box(0, 0, SOLVED_WIDTH, SOLVED_HEIGHT, HAIRLINE, Surface.INK);
        int centreX = SOLVED_WIDTH / 2;

        int label = Type.baseline(LABEL_TOP, Type.CAPTION);
        Type.centred(setter, surface, SOLVED, centreX, Type.baseline(SOLVED_TOP, Type.TITLE), Type.TITLE, Surface.INK);
        Type.centred(setter, surface, GameView.roomName(solved.level()),
                centreX, Type.baseline(ROOM_TOP, Type.CAPTION), Type.CAPTION, Surface.INK);

        surface.fill(centreX, DIVIDER_Y, HAIRLINE, DIVIDER_HEIGHT, Surface.INK);
        Type.centred(setter, surface, String.valueOf(solved.moveCount()),
                centreX - FIGURE_SPREAD, Type.baseline(FIGURE_TOP, Type.FIGURE), Type.FIGURE, Surface.INK);
        Type.centred(setter, surface, String.valueOf(solved.pushCount()),
                centreX + FIGURE_SPREAD, Type.baseline(FIGURE_TOP, Type.FIGURE), Type.FIGURE, Surface.INK);
        Type.centred(setter, surface, MOVES, centreX - FIGURE_SPREAD, label, Type.CAPTION, Surface.INK);
        Type.centred(setter, surface, PUSHES, centreX + FIGURE_SPREAD, label, Type.CAPTION, Surface.INK);

        String footnote = progressSaved ? previousBest.map(GameView::bestLine).orElse("") : NOT_SAVED;
        Type.centred(setter, surface, footnote, centreX,
                Type.baseline(BEST_TOP, Type.CAPTION), Type.CAPTION, Surface.INK);
        Type.centred(setter, surface, hasNextRoom ? NEXT_OR_REPLAY : REPLAY_ONLY,
                centreX, Type.baseline(HINT_TOP, Type.CAPTION), Type.CAPTION, Surface.INK);

        return surface;
    }

    public static Surface paused(TypeSetter setter) {
        Objects.requireNonNull(setter, "setter");

        Surface surface = new Surface(PAUSED_WIDTH, PAUSED_HEIGHT);
        surface.fill(0, 0, PAUSED_WIDTH, PAUSED_HEIGHT, Surface.INK);
        int centreX = PAUSED_WIDTH / 2;

        Type.centred(setter, surface, PAUSED, centreX, Type.baseline(PAUSED_TOP, Type.TITLE),
                Type.TITLE, Surface.PAPER);
        surface.fill(centreX - PAUSED_RULE_WIDTH / 2, PAUSED_RULE_Y, PAUSED_RULE_WIDTH, HAIRLINE, Surface.PAPER);
        Type.centred(setter, surface, RESUME_OR_RESTART, centreX, Type.baseline(PAUSED_HINT_TOP, Type.CAPTION),
                Type.CAPTION, Surface.PAPER);

        return surface;
    }
}
