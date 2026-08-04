package com.milandru.sokobani.ui.console;

import com.milandru.sokobani.core.Direction;
import com.milandru.sokobani.core.MoveResult;
import com.milandru.sokobani.engine.GameEvent;
import com.milandru.sokobani.engine.GameSession;
import com.milandru.sokobani.level.InvalidLevelFormatException;
import com.milandru.sokobani.level.LevelPack;
import com.milandru.sokobani.level.LevelRepository;
import com.milandru.sokobani.persistence.ProgressStore;
import com.milandru.sokobani.ui.Renderer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.OptionalInt;

public final class ConsoleGame {

    static final String COMMANDS =
            "Commands: w a s d move, u undo, r restart, n next level, l levels, q quit";
    static final String SELECTION =
            "Type a level number to play that level. Best scores are shown as moves/pushes.";
    static final String LOCKED = "locked";
    static final String SAVE_FAILED =
            "Progress could not be saved, so this level will be locked again next time.";

    private static final String TITLE = "Sokobani";
    private static final String PROMPT = "> ";
    private static final char LINE_BREAK = '\n';

    private final GameSession session;
    private final BufferedReader in;
    private final PrintWriter out;
    private final Renderer renderer;
    private final List<String> messages = new ArrayList<>();

    public ConsoleGame(GameSession session, Reader in, Writer out) {
        this.session = Objects.requireNonNull(session, "session");
        this.in = new BufferedReader(Objects.requireNonNull(in, "in"));
        this.out = new PrintWriter(Objects.requireNonNull(out, "out"));
        this.renderer = new ConsoleRenderer(this.out);
        session.addListener(this::narrate);
    }

    public static void main(String[] args) {
        Reader in = new InputStreamReader(System.in);
        Writer out = new OutputStreamWriter(System.out);
        try {
            GameSession session = new GameSession(
                    LevelRepository.load(LevelRepository.CLASSIC_PACK), ProgressStore.atUserHome());
            new ConsoleGame(session, in, out).run();
        } catch (IOException | InvalidLevelFormatException unloadable) {
            report(out, "Could not load the level pack: " + unloadable.getMessage());
        }
    }

    public void run() {
        say(TITLE);
        say(COMMANDS);
        say(SELECTION);
        redraw();
        while (true) {
            out.print(PROMPT);
            out.flush();
            String line = readLine();
            if (line == null) {
                return;
            }
            String command = line.trim().toLowerCase(Locale.ROOT);
            if (command.isEmpty()) {
                continue;
            }
            messages.clear();
            if (!dispatch(command)) {
                return;
            }
            redraw();
            messages.forEach(this::say);
            out.flush();
        }
    }

    private boolean dispatch(String command) {
        switch (command) {
            case "w" -> play(Direction.UP);
            case "a" -> play(Direction.LEFT);
            case "s" -> play(Direction.DOWN);
            case "d" -> play(Direction.RIGHT);
            case "u" -> undo();
            case "r" -> session.restart();
            case "n" -> advance();
            case "l" -> listLevels();
            case "q" -> {
                return false;
            }
            default -> selectLevelOrReject(command);
        }
        return true;
    }

    private void play(Direction direction) {
        if (session.move(direction) instanceof MoveResult.Blocked blocked) {
            messages.add(reasonFor(blocked.reason()));
        }
    }

    private void undo() {
        if (!session.undo()) {
            messages.add("Nothing to undo.");
        }
    }

    private void advance() {
        try {
            if (!session.nextLevel()) {
                messages.add("This is the last level in " + session.pack().name() + ".");
            }
        } catch (IllegalStateException locked) {
            messages.add(locked.getMessage());
        }
    }

    private void selectLevelOrReject(String command) {
        OptionalInt chosen = levelNumberIn(command);
        if (chosen.isEmpty()) {
            messages.add("Unknown command '" + command + "'.");
            messages.add(COMMANDS);
            return;
        }
        int number = chosen.getAsInt();
        if (number < 1 || number > session.pack().size()) {
            messages.add("There is no level " + number + " in " + session.pack().name() + ".");
            return;
        }
        startLevel(number - 1);
    }

    private void startLevel(int index) {
        try {
            session.loadLevel(index);
        } catch (IllegalStateException locked) {
            messages.add(locked.getMessage());
        }
    }

    private void listLevels() {
        LevelPack pack = session.pack();
        messages.add("Levels in " + pack.name() + ":");
        int numberWidth = String.valueOf(pack.size()).length();
        int nameWidth = pack.levels().stream().mapToInt(level -> level.name().length()).max().orElse(0);
        for (int index = 0; index < pack.size(); index++) {
            messages.add(levelLine(index, numberWidth, nameWidth));
        }
    }

    private String levelLine(int index, int numberWidth, int nameWidth) {
        String marker = index == session.levelIndex() ? ">" : " ";
        String score = session.progress().isUnlocked(index)
                ? ConsoleRenderer.best(session.progress(), index)
                : LOCKED;
        return marker
                + " " + padLeft(String.valueOf(index + 1), numberWidth)
                + "  " + padRight(session.pack().get(index).name(), nameWidth)
                + "  " + score;
    }

    private void narrate(GameEvent event) {
        switch (event) {
            case GameEvent.Moved moved -> {
            }
            case GameEvent.Pushed pushed -> {
            }
            case GameEvent.Undone undone -> messages.add("Undone.");
            case GameEvent.Restarted restarted -> messages.add("Restarted " + restarted.level().name() + ".");
            case GameEvent.LevelLoaded loaded -> messages.add("Now playing " + loaded.level().name() + ".");
            case GameEvent.Solved solved -> messages.addAll(solvedMessages(solved));
        }
    }

    private List<String> solvedMessages(GameEvent.Solved solved) {
        List<String> announcement = new ArrayList<>();
        announcement.add("Solved " + solved.level().name()
                + " in " + count(solved.moveCount(), "move", "moves")
                + " and " + count(solved.pushCount(), "push", "pushes") + ".");
        if (!session.lastSaveSucceeded()) {
            announcement.add(SAVE_FAILED);
        }
        announcement.add(session.hasNextLevel()
                ? "Press n for the next level, or r to play this one again."
                : "That was the last level in " + session.pack().name() + ".");
        return announcement;
    }

    private void redraw() {
        out.print(LINE_BREAK);
        renderer.render(session);
    }

    private void say(String line) {
        out.print(line);
        out.print(LINE_BREAK);
    }

    private String readLine() {
        try {
            return in.readLine();
        } catch (IOException unreadable) {
            return null;
        }
    }

    private static void report(Writer destination, String failure) {
        PrintWriter writer = new PrintWriter(destination);
        writer.print(failure);
        writer.print(LINE_BREAK);
        writer.flush();
    }

    private static String reasonFor(MoveResult.BlockedReason reason) {
        return switch (reason) {
            case WALL -> "A wall is in the way.";
            case BOX_AGAINST_WALL -> "That box is against a wall.";
            case BOX_AGAINST_BOX -> "That box is against another box.";
        };
    }

    private static OptionalInt levelNumberIn(String command) {
        if (command.isEmpty() || !command.chars().allMatch(Character::isDigit)) {
            return OptionalInt.empty();
        }
        try {
            return OptionalInt.of(Integer.parseInt(command));
        } catch (NumberFormatException beyondRange) {
            return OptionalInt.empty();
        }
    }

    private static String count(int amount, String singular, String plural) {
        return amount + " " + (amount == 1 ? singular : plural);
    }

    private static String padLeft(String text, int width) {
        return text.length() >= width ? text : " ".repeat(width - text.length()) + text;
    }

    private static String padRight(String text, int width) {
        return text.length() >= width ? text : text + " ".repeat(width - text.length());
    }
}
