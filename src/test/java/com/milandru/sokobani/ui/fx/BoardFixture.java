package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.core.GameState;
import com.milandru.sokobani.core.Level;
import com.milandru.sokobani.engine.GameSession;
import com.milandru.sokobani.level.InvalidLevelFormatException;
import com.milandru.sokobani.level.LevelPack;
import com.milandru.sokobani.level.LevelParser;
import com.milandru.sokobani.level.LevelRepository;
import com.milandru.sokobani.ui.Surface;
import com.milandru.sokobani.ui.TypeSetter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

final class BoardFixture {

    @FunctionalInterface
    interface TileArt {
        void draw(Surface surface, int x, int y);
    }

    static final String ONE_PUSH = """
            #######
            #@$  .#
            #######
            """;

    static final String EVERY_TILE = """
            ########
            # $ . *#
            #  @   #
            ########
            """;

    static final String PLAYER_ON_GOAL = """
            ######
            #$ $.#
            # +  #
            ######
            """;

    static final String WIDE_ROOM = """
            ##############
            #@         $.#
            #            #
            ##############
            """;

    private BoardFixture() {
    }

    static Level level(String layout) {
        return level(layout, 0);
    }

    static Level level(String layout, int index) {
        try {
            return LevelParser.parse(layout, "Fixture " + index, index);
        } catch (InvalidLevelFormatException rejected) {
            throw new IllegalArgumentException(rejected.getMessage(), rejected);
        }
    }

    static GameState state(String layout) {
        return new GameState(level(layout));
    }

    static GameSession session(String layout) {
        return new GameSession(pack(layout));
    }

    static LevelPack pack(String... layouts) {
        List<Level> levels = new ArrayList<>();
        for (String layout : layouts) {
            levels.add(level(layout, levels.size()));
        }
        return new LevelPack("fixture", levels);
    }

    static LevelPack classicPack() {
        try {
            return LevelRepository.load(LevelRepository.CLASSIC_PACK);
        } catch (IOException unreadable) {
            throw new UncheckedIOException(unreadable);
        } catch (InvalidLevelFormatException rejected) {
            throw new IllegalStateException(rejected.getMessage(), rejected);
        }
    }

    static TypeSetter typeSetter() {
        return new TypeSetter(new BlockGlyphRasterizer());
    }
}
