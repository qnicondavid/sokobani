package com.milandru.sokobani.ui.console;

import com.milandru.sokobani.core.Level;
import com.milandru.sokobani.engine.GameSession;
import com.milandru.sokobani.level.InvalidLevelFormatException;
import com.milandru.sokobani.level.LevelPack;
import com.milandru.sokobani.level.LevelParser;
import com.milandru.sokobani.persistence.ProgressStore;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

final class ConsoleFixture {

    static final String ONE_PUSH = """
            #######
            #@$  .#
            #######
            """;

    static final String TWO_PUSHES = """
            ########
            #@$   .#
            ########
            """;

    static final String BOX_ON_A_GOAL = """
            #########
            #@$ * . #
            #########
            """;

    static final String PLAYER_ON_A_GOAL = """
            #######
            #+$ $.#
            #######
            """;

    static final String SHORTER_LAST_ROW = """
              ####
            ###  #
            #@$  #
            #  .##
            ####
            """;

    private static final List<String> NAMES = List.of("First", "Second", "Third", "Fourth");

    private ConsoleFixture() {
    }

    static Level level(String layout, String name, int index) {
        try {
            return LevelParser.parse(layout, name, index);
        } catch (InvalidLevelFormatException rejected) {
            throw new IllegalArgumentException(rejected.getMessage(), rejected);
        }
    }

    static LevelPack pack(String... layouts) {
        List<Level> levels = new ArrayList<>();
        for (String layout : layouts) {
            levels.add(level(layout, NAMES.get(levels.size()), levels.size()));
        }
        return new LevelPack("fixture", levels);
    }

    static GameSession session(String... layouts) {
        return new GameSession(pack(layouts));
    }

    static GameSession session(ProgressStore store, String... layouts) {
        return new GameSession(pack(layouts), store);
    }

    static String play(GameSession session, String commands) {
        StringWriter out = new StringWriter();
        new ConsoleGame(session, new StringReader(commands), out).run();
        return out.toString();
    }

    static int boardsDrawnIn(String output) {
        return output.split("\nmoves ", -1).length - 1;
    }
}
