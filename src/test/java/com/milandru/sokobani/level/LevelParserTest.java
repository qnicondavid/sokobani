package com.milandru.sokobani.level;

import com.milandru.sokobani.core.Level;
import com.milandru.sokobani.core.Position;
import com.milandru.sokobani.core.Tile;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LevelParserTest {

    private static final String ONE_BOX = """
            #####
            #@$.#
            #####""";

    private static final String RAGGED = """
              ####
            ###  #
            #@$  #
            #  .##
            ####""";

    @Test
    void parse_singleBoxLevel_readsTerrainBoxesAndPlayer() throws Exception {
        Level level = LevelParser.parse(ONE_BOX);

        assertAll(
                () -> assertEquals(3, level.rowCount()),
                () -> assertEquals(5, level.columnCount()),
                () -> assertEquals(new Position(1, 1), level.initialPlayer()),
                () -> assertEquals(Set.of(new Position(1, 2)), level.initialBoxes()),
                () -> assertEquals(Set.of(new Position(1, 3)), level.goals()),
                () -> assertEquals(Tile.WALL, level.tileAt(new Position(0, 0))),
                () -> assertEquals(Tile.FLOOR, level.tileAt(new Position(1, 1))),
                () -> assertEquals(Tile.FLOOR, level.tileAt(new Position(1, 2))),
                () -> assertEquals(Tile.GOAL, level.tileAt(new Position(1, 3))));
    }

    @Test
    void parse_nameAndIndex_areCarriedOntoTheLevel() throws Exception {
        Level level = LevelParser.parse(ONE_BOX, "Warm up", 4);

        assertAll(
                () -> assertEquals("Warm up", level.name()),
                () -> assertEquals(4, level.index()));
    }

    @Test
    void parse_playerOnGoal_leavesAGoalUnderThePlayer() throws Exception {
        Level level = LevelParser.parse("""
                ######
                #+$ *#
                ######""");

        assertAll(
                () -> assertEquals(new Position(1, 1), level.initialPlayer()),
                () -> assertEquals(Tile.GOAL, level.tileAt(new Position(1, 1))),
                () -> assertEquals(Set.of(new Position(1, 1), new Position(1, 4)), level.goals()),
                () -> assertEquals(Set.of(new Position(1, 2), new Position(1, 4)), level.initialBoxes()));
    }

    @Test
    void parse_boxOnGoal_leavesAGoalUnderTheBox() throws Exception {
        Level level = LevelParser.parse("""
                ######
                #@*$.#
                ######""");

        assertAll(
                () -> assertEquals(Tile.GOAL, level.tileAt(new Position(1, 2))),
                () -> assertEquals(Set.of(new Position(1, 2), new Position(1, 3)), level.initialBoxes()),
                () -> assertEquals(Set.of(new Position(1, 2), new Position(1, 4)), level.goals()));
    }

    @Test
    void parse_raggedLines_padThemselvesWithFloorNotWall() throws Exception {
        Level level = LevelParser.parse(RAGGED);

        assertAll(
                () -> assertEquals(5, level.rowCount()),
                () -> assertEquals(6, level.columnCount()),
                () -> assertEquals(Tile.FLOOR, level.tileAt(new Position(4, 4))),
                () -> assertEquals(Tile.FLOOR, level.tileAt(new Position(4, 5))));
    }

    @Test
    void parse_raggedLines_keepLeadingSpacesInPlace() throws Exception {
        Level level = LevelParser.parse(RAGGED);

        assertAll(
                () -> assertEquals(Tile.FLOOR, level.tileAt(new Position(0, 0))),
                () -> assertEquals(Tile.FLOOR, level.tileAt(new Position(0, 1))),
                () -> assertEquals(Tile.WALL, level.tileAt(new Position(0, 2))),
                () -> assertEquals(Tile.WALL, level.tileAt(new Position(1, 2))),
                () -> assertEquals(new Position(2, 1), level.initialPlayer()));
    }

    @Test
    void parse_lineWithTrailingSpaces_countsThemTowardTheWidth() throws Exception {
        Level level = LevelParser.parse("####  \n#@$.#\n#####\n");

        assertAll(
                () -> assertEquals(6, level.columnCount()),
                () -> assertEquals(Tile.FLOOR, level.tileAt(new Position(0, 4))),
                () -> assertEquals(Tile.FLOOR, level.tileAt(new Position(0, 5))));
    }

    @Test
    void parse_windowsLineEndings_matchUnixLineEndings() throws Exception {
        Level windows = LevelParser.parse(ONE_BOX.replace("\n", "\r\n"));
        Level unix = LevelParser.parse(ONE_BOX);

        assertEquals(unix, windows);
    }

    @Test
    void parse_trailingNewline_matchesTextWithoutOne() throws Exception {
        assertEquals(LevelParser.parse(ONE_BOX), LevelParser.parse(ONE_BOX + "\n"));
    }

    @Test
    void parse_trailingWindowsNewline_matchesTextWithoutOne() throws Exception {
        assertEquals(LevelParser.parse(ONE_BOX), LevelParser.parse(ONE_BOX + "\r\n"));
    }

    @Test
    void parse_levelWithoutAPlayer_throwsNamingTheCount() {
        InvalidLevelFormatException thrown = assertThrows(InvalidLevelFormatException.class, () -> LevelParser.parse("""
                #####
                # $.#
                #####"""));

        assertAll(
                () -> assertTrue(thrown.getMessage().contains("0 players"), thrown.getMessage()),
                () -> assertEquals(1, thrown.line()),
                () -> assertEquals(InvalidLevelFormatException.NO_POSITION, thrown.column()));
    }

    @Test
    void parse_levelWithTwoPlayers_throwsNamingTheCountAndTheSecondPlayer() {
        InvalidLevelFormatException thrown = assertThrows(InvalidLevelFormatException.class, () -> LevelParser.parse("""
                ######
                #@$.@#
                ######"""));

        assertAll(
                () -> assertTrue(thrown.getMessage().contains("2 players"), thrown.getMessage()),
                () -> assertEquals(2, thrown.line()),
                () -> assertEquals(5, thrown.column()));
    }

    @Test
    void parse_levelWithoutGoals_throws() {
        InvalidLevelFormatException thrown = assertThrows(InvalidLevelFormatException.class, () -> LevelParser.parse("""
                #####
                #@$ #
                #####"""));

        assertTrue(thrown.getMessage().contains("no goals"), thrown.getMessage());
    }

    @Test
    void parse_moreBoxesThanGoals_throwsNamingBothCounts() {
        InvalidLevelFormatException thrown = assertThrows(InvalidLevelFormatException.class, () -> LevelParser.parse("""
                #######
                #@$$ .#
                #######"""));

        assertTrue(thrown.getMessage().contains("2 boxes and 1 goals"), thrown.getMessage());
    }

    @Test
    void parse_fewerBoxesThanGoals_throwsNamingBothCounts() {
        InvalidLevelFormatException thrown = assertThrows(InvalidLevelFormatException.class, () -> LevelParser.parse("""
                #######
                #@$ ..#
                #######"""));

        assertTrue(thrown.getMessage().contains("1 boxes and 2 goals"), thrown.getMessage());
    }

    @Test
    void parse_illegalCharacter_throwsWithLineAndColumn() {
        InvalidLevelFormatException thrown = assertThrows(InvalidLevelFormatException.class, () -> LevelParser.parse("""
                ######
                #@$ .#
                #%   #
                ######"""));

        assertAll(
                () -> assertTrue(thrown.getMessage().contains("'%'"), thrown.getMessage()),
                () -> assertEquals(3, thrown.line()),
                () -> assertEquals(2, thrown.column()),
                () -> assertTrue(thrown.getMessage().contains("line 3, column 2"), thrown.getMessage()));
    }

    @Test
    void parse_tabCharacter_namesItsCodePoint() {
        InvalidLevelFormatException thrown = assertThrows(InvalidLevelFormatException.class,
                () -> LevelParser.parse("#####\n#@$.#\n#\t  #\n#####"));

        assertTrue(thrown.getMessage().contains("U+0009"), thrown.getMessage());
    }

    @Test
    void parse_emptyText_throws() {
        assertTrue(assertThrows(InvalidLevelFormatException.class, () -> LevelParser.parse(""))
                .getMessage().contains("empty"));
    }

    @Test
    void parse_whitespaceOnlyText_throws() {
        assertTrue(assertThrows(InvalidLevelFormatException.class, () -> LevelParser.parse("   \n \n\n"))
                .getMessage().contains("empty"));
    }

    @Test
    void parse_nullText_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> LevelParser.parse(null));
    }

    @Test
    void parse_levelOpenAtTheSide_throwsNamingTheEscape() {
        InvalidLevelFormatException thrown = assertThrows(InvalidLevelFormatException.class, () -> LevelParser.parse("""
                ######
                #@$ .
                ######"""));

        assertAll(
                () -> assertTrue(thrown.getMessage().contains("not enclosed"), thrown.getMessage()),
                () -> assertEquals(2, thrown.line()),
                () -> assertEquals(6, thrown.column()));
    }

    @Test
    void parse_levelOpenAtTheTop_throwsNamingTheEscape() {
        InvalidLevelFormatException thrown = assertThrows(InvalidLevelFormatException.class, () -> LevelParser.parse("""
                ### ##
                #@$ .#
                ######"""));

        assertAll(
                () -> assertEquals(1, thrown.line()),
                () -> assertEquals(4, thrown.column()));
    }

    @Test
    void parse_levelWithAGapAtTheBottom_throws() {
        assertThrows(InvalidLevelFormatException.class, () -> LevelParser.parse("""
                ######
                #@$ .#
                ##  ##"""));
    }

    @Test
    void parse_floorOutsideTheWallsTouchingTheBoundary_isNotAnEscape() throws Exception {
        Level level = LevelParser.parse(RAGGED);

        assertAll(
                () -> assertEquals(Set.of(new Position(2, 2)), level.initialBoxes()),
                () -> assertEquals(Set.of(new Position(3, 3)), level.goals()));
    }

    @Test
    void parse_irregularlyShapedButClosedLevel_isAccepted() throws Exception {
        Level level = LevelParser.parse("""
                #######
                #@ $  #
                #  ####
                #  #
                #. #
                ####""");

        assertAll(
                () -> assertEquals(6, level.rowCount()),
                () -> assertEquals(7, level.columnCount()),
                () -> assertEquals(Set.of(new Position(4, 1)), level.goals()));
    }

    @Test
    void toXsb_parsedLevel_roundTripsToAnEqualLevel() throws Exception {
        Level level = LevelParser.parse(ONE_BOX, "Warm up", 2);

        assertEquals(level, LevelParser.parse(LevelParser.toXsb(level), "Warm up", 2));
    }

    @Test
    void toXsb_levelWithPlayerAndBoxOnGoals_roundTripsToAnEqualLevel() throws Exception {
        Level level = LevelParser.parse("""
                ########
                #+*$ $.#
                ########""", "Stacked", 7);

        assertEquals(level, LevelParser.parse(LevelParser.toXsb(level), "Stacked", 7));
    }

    @Test
    void toXsb_raggedLevel_roundTripsToAnEqualLevel() throws Exception {
        Level level = LevelParser.parse(RAGGED, "Ragged", 0);

        assertEquals(level, LevelParser.parse(LevelParser.toXsb(level), "Ragged", 0));
    }

    @Test
    void toXsb_parsedLevel_writesEveryXsbSymbol() throws Exception {
        Level level = LevelParser.parse("""
                ########
                #+*$ $.#
                ########""");

        assertEquals("""
                ########
                #+*$ $.#
                ########
                """, LevelParser.toXsb(level));
    }

    @Test
    void toXsb_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> LevelParser.toXsb(null));
    }

    @Test
    void parse_levelsThatDifferOnlyInThePlayerPosition_areNotEqual() throws Exception {
        Level left = LevelParser.parse("""
                ######
                #@$ .#
                ######""");
        Level right = LevelParser.parse("""
                ######
                # $@.#
                ######""");

        assertNotEquals(left, right);
    }
}
