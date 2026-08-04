package com.milandru.sokobani.ui.console;

import com.milandru.sokobani.core.GameState;
import com.milandru.sokobani.engine.GameSession;
import com.milandru.sokobani.level.LevelParser;
import com.milandru.sokobani.persistence.Progress;
import com.milandru.sokobani.ui.Renderer;

import java.io.PrintWriter;
import java.util.Objects;

public final class ConsoleRenderer implements Renderer {

    static final String NO_BEST = "none";

    private static final char LINE_BREAK = '\n';
    private static final String FIELD_GAP = "   ";
    private static final String TITLE_GAP = "  ";

    private final PrintWriter out;

    public ConsoleRenderer(PrintWriter out) {
        this.out = Objects.requireNonNull(out, "out");
    }

    @Override
    public void render(GameSession session) {
        Objects.requireNonNull(session, "session");
        out.print(heading(session));
        out.print(LINE_BREAK);
        out.print(board(session.state()));
        out.print(status(session));
        out.print(LINE_BREAK);
        out.flush();
    }

    static String best(Progress progress, int level) {
        return progress.levelRecord(level)
                .filter(Progress.LevelRecord::solved)
                .map(record -> record.bestMoves() + "/" + record.bestPushes())
                .orElse(NO_BEST);
    }

    private static String heading(GameSession session) {
        return "Level " + (session.levelIndex() + 1) + "/" + session.pack().size()
                + TITLE_GAP + session.currentLevel().name();
    }

    private static String board(GameState state) {
        StringBuilder drawn = new StringBuilder();
        LevelParser.toXsb(state).lines().forEach(line -> drawn.append(withoutTrailingFloor(line)).append(LINE_BREAK));
        return drawn.toString();
    }

    private static String withoutTrailingFloor(String line) {
        int end = line.length();
        while (end > 0 && line.charAt(end - 1) == LevelParser.FLOOR) {
            end--;
        }
        return line.substring(0, end);
    }

    private static String status(GameSession session) {
        return "moves " + session.moveCount()
                + FIELD_GAP + "pushes " + session.pushCount()
                + FIELD_GAP + "best " + best(session.progress(), session.levelIndex());
    }
}
