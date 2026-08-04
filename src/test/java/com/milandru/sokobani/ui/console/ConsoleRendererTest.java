package com.milandru.sokobani.ui.console;

import com.milandru.sokobani.core.Direction;
import com.milandru.sokobani.core.Level;
import com.milandru.sokobani.engine.GameSession;
import com.milandru.sokobani.level.LevelParser;
import com.milandru.sokobani.level.LevelRepository;

import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsoleRendererTest {

    @Test
    void render_theOpeningState_drawsTheHeadingTheBoardAndTheStatusLine() {
        GameSession session = ConsoleFixture.session(ConsoleFixture.ONE_PUSH);

        assertEquals("""
                Level 1/1  First
                #######
                #@$  .#
                #######
                moves 0   pushes 0   best none
                """, render(session));
    }

    @Test
    void render_afterAPush_movesThePlayerAndTheBoxAndCountsBoth() {
        GameSession session = ConsoleFixture.session(ConsoleFixture.ONE_PUSH);

        session.move(Direction.RIGHT);

        assertEquals("""
                Level 1/1  First
                #######
                # @$ .#
                #######
                moves 1   pushes 1   best none
                """, render(session));
    }

    @Test
    void render_aBoxOnItsGoal_drawsAStarWhereABoxOffItsGoalDrawsADollar() {
        GameSession session = ConsoleFixture.session(ConsoleFixture.ONE_PUSH);
        session.move(Direction.RIGHT);
        session.move(Direction.RIGHT);
        String offTheGoal = render(session);

        session.move(Direction.RIGHT);

        assertAll(
                () -> assertTrue(offTheGoal.contains("#  @$.#")),
                () -> assertTrue(render(session).contains("#   @*#")));
    }

    @Test
    void render_thePlayerOnAGoal_drawsAPlusWhereThePlayerOffAGoalDrawsAnAt() {
        GameSession session = ConsoleFixture.session(ConsoleFixture.PLAYER_ON_A_GOAL);
        String onTheGoal = render(session);

        session.move(Direction.RIGHT);

        assertAll(
                () -> assertTrue(onTheGoal.contains("#+$ $.#")),
                () -> assertTrue(render(session).contains("#.@$$.#")));
    }

    @Test
    void render_aBoxAlreadyOnAGoalBesideOneThatIsNot_drawsBothSymbolsAtOnce() {
        GameSession session = ConsoleFixture.session(ConsoleFixture.BOX_ON_A_GOAL);

        assertEquals("""
                Level 1/1  First
                #########
                #@$ * . #
                #########
                moves 0   pushes 0   best none
                """, render(session));
    }

    @Test
    void render_aLevelWithAPersonalBest_showsItInTheStatusLine() {
        GameSession session = ConsoleFixture.session(ConsoleFixture.ONE_PUSH);
        session.move(Direction.RIGHT);
        session.move(Direction.RIGHT);
        session.move(Direction.RIGHT);

        session.restart();

        assertTrue(render(session).contains("moves 0   pushes 0   best 3/3"));
    }

    @Test
    void render_aLevelBeyondTheFirst_showsItsOwnNumberAndName() {
        GameSession session = ConsoleFixture.session(ConsoleFixture.ONE_PUSH, ConsoleFixture.TWO_PUSHES);
        session.move(Direction.RIGHT);
        session.move(Direction.RIGHT);
        session.move(Direction.RIGHT);

        session.nextLevel();

        assertTrue(render(session).startsWith("Level 2/2  Second\n"));
    }

    @Test
    void render_aRowShorterThanTheWidestOne_leavesNoTrailingSpaces() {
        GameSession session = ConsoleFixture.session(ConsoleFixture.SHORTER_LAST_ROW);

        assertEquals("""
                Level 1/1  First
                  ####
                ###  #
                #@$  #
                #  .##
                ####
                moves 0   pushes 0   best none
                """, render(session));
    }

    @Test
    void render_aBoardWhoseTrailingFloorWasStripped_stillReparsesToTheSameLevel() throws Exception {
        GameSession session = ConsoleFixture.session(ConsoleFixture.SHORTER_LAST_ROW);
        Level level = session.currentLevel();

        assertEquals(level, LevelParser.parse(boardIn(render(session)), level.name(), level.index()));
    }

    @Test
    void render_theFirstLevelOfTheClassicPack_drawsABoardThatReparsesToIt() throws Exception {
        GameSession session = new GameSession(LevelRepository.load(LevelRepository.CLASSIC_PACK));
        Level level = session.currentLevel();

        assertEquals(level, LevelParser.parse(boardIn(render(session)), level.name(), level.index()));
    }

    private static String render(GameSession session) {
        StringWriter captured = new StringWriter();
        PrintWriter out = new PrintWriter(captured);
        new ConsoleRenderer(out).render(session);
        return captured.toString();
    }

    private static String boardIn(String rendered) {
        List<String> lines = rendered.lines().toList();
        return String.join("\n", lines.subList(1, lines.size() - 1));
    }
}
