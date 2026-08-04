package com.milandru.sokobani.level;

import com.milandru.sokobani.core.Level;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LevelPackTest {

    private static Level level(String name, int index) throws InvalidLevelFormatException {
        return LevelParser.parse("""
                #####
                #@$.#
                #####""", name, index);
    }

    @Test
    void name_pack_returnsTheNameItWasBuiltWith() throws Exception {
        assertEquals("classic", new LevelPack("classic", List.of(level("one", 0))).name());
    }

    @Test
    void size_pack_countsItsLevels() throws Exception {
        assertEquals(2, new LevelPack("classic", List.of(level("one", 0), level("two", 1))).size());
    }

    @Test
    void get_index_returnsTheLevelInPackOrder() throws Exception {
        LevelPack pack = new LevelPack("classic", List.of(level("one", 0), level("two", 1)));

        assertAll(
                () -> assertEquals("one", pack.get(0).name()),
                () -> assertEquals("two", pack.get(1).name()));
    }

    @Test
    void get_indexPastTheEnd_throwsIndexOutOfBounds() throws Exception {
        LevelPack pack = new LevelPack("classic", List.of(level("one", 0)));

        assertThrows(IndexOutOfBoundsException.class, () -> pack.get(1));
    }

    @Test
    void levels_returnedList_isImmutable() throws Exception {
        LevelPack pack = new LevelPack("classic", List.of(level("one", 0)));

        assertThrows(UnsupportedOperationException.class, () -> pack.levels().clear());
    }

    @Test
    void constructor_listMutatedAfterwards_doesNotChangeThePack() throws Exception {
        List<Level> levels = new ArrayList<>(List.of(level("one", 0)));
        LevelPack pack = new LevelPack("classic", levels);

        levels.clear();

        assertEquals(1, pack.size());
    }

    @Test
    void constructor_nullName_throwsNullPointerException() throws Exception {
        List<Level> levels = List.of(level("one", 0));

        assertThrows(NullPointerException.class, () -> new LevelPack(null, levels));
    }

    @Test
    void constructor_nullLevels_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new LevelPack("classic", null));
    }

    @Test
    void constructor_listContainingNull_throwsNullPointerException() {
        List<Level> levels = Arrays.asList(new Level[]{null});

        assertThrows(NullPointerException.class, () -> new LevelPack("classic", levels));
    }

    @Test
    void equals_packsWithTheSameNameAndLevels_areEqual() throws Exception {
        assertEquals(
                new LevelPack("classic", List.of(level("one", 0))),
                new LevelPack("classic", List.of(level("one", 0))));
    }

    @Test
    void equals_packsWithDifferentNames_areNotEqual() throws Exception {
        assertNotEquals(
                new LevelPack("classic", List.of(level("one", 0))),
                new LevelPack("extra", List.of(level("one", 0))));
    }

    @Test
    void hashCode_equalPacks_match() throws Exception {
        assertEquals(
                new LevelPack("classic", List.of(level("one", 0))).hashCode(),
                new LevelPack("classic", List.of(level("one", 0))).hashCode());
    }

    @Test
    void toString_pack_namesItAndItsSize() throws Exception {
        assertEquals("LevelPack[name=classic, size=1]",
                new LevelPack("classic", List.of(level("one", 0))).toString());
    }
}
