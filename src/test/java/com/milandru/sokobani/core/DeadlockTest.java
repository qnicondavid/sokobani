package com.milandru.sokobani.core;

import com.milandru.sokobani.level.InvalidLevelFormatException;
import com.milandru.sokobani.level.LevelPack;
import com.milandru.sokobani.level.LevelRepository;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeadlockTest {

    @Test
    void deadlockedBoxes_flagsABoxWedgedIntoACorner() {
        GameState state = state("""
                #####
                #@$##
                #.. #
                #####
                """);

        assertEquals(Set.of(new Position(1, 2)), Deadlock.deadlockedBoxes(state));
    }

    @Test
    void deadlockedBoxes_neverFlagsABoxSittingOnAGoal() {
        GameState state = state("""
                #####
                #@*##
                #####
                """);

        assertEquals(Set.of(), Deadlock.deadlockedBoxes(state));
    }

    @Test
    void deadlockedBoxes_flagsABoxSealedInACorridor() {
        GameState state = state("""
                #########
                #@$      #
                #########
                """);

        assertEquals(Set.of(new Position(1, 2)), Deadlock.deadlockedBoxes(state));
    }

    @Test
    void deadlockedBoxes_neverFlagsACorridorBoxWhoseRunContainsAGoal() {
        GameState state = state("""
                #########
                #@$.    #
                #########
                """);

        assertEquals(Set.of(), Deadlock.deadlockedBoxes(state));
    }

    @Test
    void deadlockedBoxes_neverFlagsACorridorBoxWhoseWallHasAGap() {
        GameState state = state("""
                #########
                #@$    .#
                ####### #
                #########
                """);

        assertEquals(Set.of(), Deadlock.deadlockedBoxes(state));
    }

    @Test
    void deadlockedBoxes_flagsABoxSealedInAVerticalCorridor() {
        GameState state = state("""
                #######
                # @   #
                #$    #
                #     #
                #   . #
                #######
                """);

        assertEquals(Set.of(new Position(2, 1)), Deadlock.deadlockedBoxes(state));
    }

    @Test
    void deadlockedBoxes_flagsOnlyTheDeadBoxInAMixedBoard() {
        GameState state = state("""
                ####### ###
                #@$ # $   #
                #   #    #
                ###########
                """);

        assertEquals(Set.of(new Position(1, 2)), Deadlock.deadlockedBoxes(state));
    }

    @Test
    void deadlockedBoxes_flagsNoBoxAtTheStartOfAnyBundledLevel() {
        LevelPack pack = classicPack();

        for (int i = 0; i < pack.levels().size(); i++) {
            assertEquals(Set.of(), Deadlock.deadlockedBoxes(new GameState(pack.levels().get(i))),
                    pack.levels().get(i).name());
        }
    }

    private static GameState state(String layout) {
        return new GameState(LevelFixture.level(layout));
    }

    private static LevelPack classicPack() {
        try {
            return LevelRepository.load(LevelRepository.CLASSIC_PACK);
        } catch (IOException unreadable) {
            throw new AssertionError(unreadable);
        } catch (InvalidLevelFormatException rejected) {
            throw new AssertionError(rejected.getMessage(), rejected);
        }
    }
}
