package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.core.Direction;
import com.milandru.sokobani.core.Position;
import com.milandru.sokobani.engine.GameEvent;
import com.milandru.sokobani.engine.GameSession;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TweenTest {

    private static final Position FROM = new Position(2, 3);
    private static final Position TO = new Position(4, 7);
    private static final long START = 1_000L;
    private static final long DURATION = 100L;

    @Test
    void fractionIsZeroBeforeStart() {
        Tween tween = Tween.ofMove(FROM, TO, START, DURATION);
        assertEquals(0.0, tween.fraction(START));
        assertEquals(0.0, tween.fraction(START - 500L));
    }

    @Test
    void fractionClampsAtOneAfterDuration() {
        Tween tween = Tween.ofMove(FROM, TO, START, DURATION);
        assertEquals(1.0, tween.fraction(START + DURATION));
        assertEquals(1.0, tween.fraction(START + DURATION + 500L));
        assertTrue(tween.finished(START + DURATION));
        assertTrue(tween.finished(START + DURATION + 500L));
    }

    @Test
    void fractionIsHalfMidway() {
        Tween tween = Tween.ofMove(FROM, TO, START, DURATION);
        assertEquals(0.5, tween.fraction(START + DURATION / 2), 1e-9);
        assertFalse(tween.finished(START + DURATION / 2));
    }

    @Test
    void zeroDurationIsInstant() {
        Tween tween = Tween.ofMove(FROM, TO, START, 0L);
        assertEquals(1.0, tween.fraction(START));
        assertEquals(1.0, tween.fraction(START + 999L));
        assertTrue(tween.finished(START));
    }

    @Test
    void negativeDurationIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> Tween.ofMove(FROM, TO, START, -1L));
    }

    @Test
    void moveCarriesOnlyThePlayer() {
        Tween tween = Tween.ofMove(FROM, TO, START, DURATION);
        assertEquals(FROM, tween.playerFrom());
        assertEquals(TO, tween.playerTo());
        assertFalse(tween.boxFrom().isPresent());
        assertFalse(tween.boxTo().isPresent());
    }

    @Test
    void pushCarriesTheBox() {
        Position boxFrom = new Position(4, 7);
        Position boxTo = new Position(4, 8);
        Tween tween = Tween.ofPush(FROM, TO, boxFrom, boxTo, START, DURATION);
        assertEquals(FROM, tween.playerFrom());
        assertEquals(TO, tween.playerTo());
        assertEquals(boxFrom, tween.boxFrom().orElseThrow());
        assertEquals(boxTo, tween.boxTo().orElseThrow());
    }

    @Test
    void of_anEventThatMovesPieces_endsWhereTheSessionAlreadyStands() {
        GameSession session = BoardFixture.session(BoardFixture.ONE_PUSH);
        List<GameEvent> seen = record(session);

        session.move(Direction.RIGHT);
        Tween push = Tween.of(last(seen), START, DURATION).orElseThrow();
        assertEquals(session.state().player(), push.playerTo());
        assertTrue(session.state().boxes().contains(push.boxTo().orElseThrow()));

        session.undo();
        Tween undonePush = Tween.of(last(seen), START, DURATION).orElseThrow();
        assertEquals(session.state().player(), undonePush.playerTo());
        assertTrue(session.state().boxes().contains(undonePush.boxTo().orElseThrow()));
    }

    @Test
    void of_anUndoneMove_travelsBackTowardsWhereThePlayerNowStands() {
        GameSession session = BoardFixture.session(BoardFixture.WIDE_ROOM);
        List<GameEvent> seen = record(session);

        session.move(Direction.DOWN);
        Position afterTheMove = session.state().player();
        session.undo();
        Position afterTheUndo = session.state().player();

        Tween tween = Tween.of(last(seen), START, DURATION).orElseThrow();

        assertEquals(afterTheMove, tween.playerFrom());
        assertEquals(afterTheUndo, tween.playerTo());
        assertFalse(tween.boxTo().isPresent());
    }

    @Test
    void of_theEventsThatReplaceTheWholeBoard_carryNoTween() {
        GameSession session = BoardFixture.session(BoardFixture.ONE_PUSH);

        assertFalse(Tween.of(new GameEvent.Restarted(session.currentLevel()), START, DURATION).isPresent());
        assertFalse(Tween.of(new GameEvent.LevelLoaded(session.currentLevel()), START, DURATION).isPresent());
        assertFalse(Tween.of(new GameEvent.Solved(session.currentLevel(), 1, 1), START, DURATION).isPresent());
    }

    private static List<GameEvent> record(GameSession session) {
        List<GameEvent> seen = new ArrayList<>();
        session.addListener(seen::add);
        return seen;
    }

    private static GameEvent last(List<GameEvent> seen) {
        return seen.get(seen.size() - 1);
    }
}
