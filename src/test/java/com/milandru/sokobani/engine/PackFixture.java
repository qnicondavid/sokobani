package com.milandru.sokobani.engine;

import com.milandru.sokobani.core.Level;
import com.milandru.sokobani.level.InvalidLevelFormatException;
import com.milandru.sokobani.level.LevelPack;
import com.milandru.sokobani.level.LevelParser;

import java.util.ArrayList;
import java.util.List;

final class PackFixture {

    static final String ONE_PUSH = """
            #######
            #@$  .#
            #######
            """;

    static final String ROOM_TO_WALK = """
            #######
            #@  $.#
            #######
            """;

    static final String TWO_BOXES = """
            #######
            #@$  .#
            #  $ .#
            #######
            """;

    static final String OPEN_ROOM = """
            ########
            #@     #
            #  $   #
            #      #
            #  .   #
            #      #
            ########
            """;

    static final String GOAL_WITH_ROOM_BEYOND = """
            #######
            #@$.  #
            #######
            """;

    static final String ALREADY_SOLVED = """
            #######
            #@ *  #
            #######
            """;

    private PackFixture() {
    }

    static Level level(String layout) {
        return level(layout, "fixture", 0);
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
            levels.add(level(layout, "Level " + (levels.size() + 1), levels.size()));
        }
        return new LevelPack("fixture", levels);
    }

    static GameSession session(String layout) {
        return new GameSession(pack(layout));
    }
}
