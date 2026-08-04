package com.milandru.sokobani.level;

import com.milandru.sokobani.core.Position;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LevelRepositoryTest {

    @Test
    void load_packWithTwoLevels_readsBothInFileOrder() throws Exception {
        LevelPack pack = LevelRepository.load("levels/sample.sok");

        assertAll(
                () -> assertEquals(2, pack.size()),
                () -> assertEquals(5, pack.get(0).columnCount()),
                () -> assertEquals(8, pack.get(1).columnCount()));
    }

    @Test
    void load_commentBeforeALevel_becomesItsName() throws Exception {
        LevelPack pack = LevelRepository.load("levels/sample.sok");

        assertAll(
                () -> assertEquals("Warm up", pack.get(0).name()),
                () -> assertEquals("The long room", pack.get(1).name()));
    }

    @Test
    void load_levelWithoutAComment_isNamedAfterItsNumber() throws Exception {
        LevelPack pack = LevelRepository.load("levels/unnamed.sok");

        assertAll(
                () -> assertEquals("Level 1", pack.get(0).name()),
                () -> assertEquals("Level 2", pack.get(1).name()));
    }

    @Test
    void load_pack_indexesLevelsFromZero() throws Exception {
        LevelPack pack = LevelRepository.load("levels/sample.sok");

        assertAll(
                () -> assertEquals(0, pack.get(0).index()),
                () -> assertEquals(1, pack.get(1).index()));
    }

    @Test
    void load_pack_takesItsNameFromTheFileName() throws Exception {
        assertEquals("sample", LevelRepository.load("levels/sample.sok").name());
    }

    @Test
    void load_raggedPack_parsesTheIrregularShape() throws Exception {
        LevelPack pack = LevelRepository.load("levels/ragged.sok");

        assertAll(
                () -> assertEquals(1, pack.size()),
                () -> assertEquals(6, pack.get(0).columnCount()),
                () -> assertEquals(new Position(2, 1), pack.get(0).initialPlayer()));
    }

    @Test
    void load_packWithABrokenLevel_reportsTheLineNumberWithinTheFile() {
        InvalidLevelFormatException thrown = assertThrows(InvalidLevelFormatException.class,
                () -> LevelRepository.load("levels/broken.sok"));

        assertAll(
                () -> assertEquals(8, thrown.line()),
                () -> assertEquals(5, thrown.column()),
                () -> assertTrue(thrown.getMessage().contains("'x'"), thrown.getMessage()));
    }

    @Test
    void load_packWithNothingButComments_throws() {
        InvalidLevelFormatException thrown = assertThrows(InvalidLevelFormatException.class,
                () -> LevelRepository.load("levels/empty.sok"));

        assertTrue(thrown.getMessage().contains("no levels"), thrown.getMessage());
    }

    @Test
    void load_resourceThatIsNotOnTheClasspath_throwsFileNotFound() {
        assertThrows(FileNotFoundException.class, () -> LevelRepository.load("levels/absent.sok"));
    }

    @Test
    void load_nullPath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> LevelRepository.load(null));
    }
}
