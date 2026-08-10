package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.core.Deadlock;
import com.milandru.sokobani.core.Direction;
import com.milandru.sokobani.core.Level;
import com.milandru.sokobani.core.Position;
import com.milandru.sokobani.ui.FxToolkit;
import com.milandru.sokobani.ui.Surface;
import com.milandru.sokobani.ui.Threshold;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameScreenTweenTest {

    private static final String DEADLOCKED_ROOM = """
            ######
            #$   #
            # @  #
            #   .#
            ######
            """;

    private static final long FIRST_FRAME = 0;

    @TempDir
    Path home;

    @Test
    void onEvent_anUndonePush_tweensFromWhereThePiecesStoodToWhereTheUndoPutThem() {
        ScreenFixture fixture = ScreenFixture.twoRooms(home);
        fixture.show(fixture.game);
        fixture.move(Direction.RIGHT);
        Position playerAfterThePush = fixture.session.state().player();
        Position boxAfterThePush = new Position(playerAfterThePush.row(), playerAfterThePush.col() + 1);

        fixture.undo();

        Tween tween = tweenOf(fixture);
        assertEquals(playerAfterThePush, tween.playerFrom());
        assertEquals(fixture.session.state().player(), tween.playerTo(),
                "the undo tween ends somewhere other than where the session already stands");
        assertEquals(boxAfterThePush, tween.boxFrom().orElseThrow());
        assertEquals(playerAfterThePush, tween.boxTo().orElseThrow());
    }

    @Test
    void onEvent_anUndoneMove_tweensBackTowardsWhereThePlayerNowStands() {
        ScreenFixture fixture = ScreenFixture.twoRooms(home);
        fixture.show(fixture.game);
        fixture.move(Direction.DOWN);
        Position beforeTheUndo = fixture.session.state().player();

        fixture.undo();

        Tween tween = tweenOf(fixture);
        assertEquals(beforeTheUndo, tween.playerFrom());
        assertEquals(fixture.session.state().player(), tween.playerTo());
    }

    @Test
    void render_theFirstFrameOfAPush_erasesTheSettledCrateFromItsDestination() {
        ScreenFixture fixture = withoutHints(ScreenFixture.twoRooms(home));
        fixture.show(fixture.game);
        Level level = fixture.session.currentLevel();
        Position destination = new Position(1, 3);
        int emptyFloor = inkInTile(fixture.render(fixture.game), level, destination);

        fixture.move(Direction.RIGHT);
        Surface firstFrame = renderAt(fixture, FIRST_FRAME, Set.of());

        assertEquals(emptyFloor, inkInTile(firstFrame, level, destination),
                "the crate the tween is carrying is still drawn on the square it has not reached yet");
    }

    @Test
    void render_theFirstFrameOfAMove_erasesTheSettledPlayerFromItsDestination() {
        ScreenFixture fixture = withoutHints(ScreenFixture.twoRooms(home));
        fixture.show(fixture.game);
        Level level = fixture.session.currentLevel();
        Position destination = new Position(2, 1);
        int emptyFloor = inkInTile(fixture.render(fixture.game), level, destination);

        fixture.move(Direction.DOWN);
        Surface firstFrame = renderAt(fixture, FIRST_FRAME, Set.of());

        assertEquals(emptyFloor, inkInTile(firstFrame, level, destination),
                "the player the tween is carrying is still drawn on the square it has not reached yet");
    }

    @Test
    void render_theFirstFrameOfAMoveOntoAHatchedSquare_keepsTheDeadlockHatch() {
        ScreenFixture fixture = ScreenFixture.on(home, DEADLOCKED_ROOM);
        fixture.show(fixture.game);
        Level level = fixture.session.currentLevel();
        Set<Position> deadlocked = Deadlock.deadlockedBoxes(fixture.session.state());
        Position destination = new Position(2, 1);
        assertFalse(deadlocked.isEmpty(), "the fixture room holds no stuck crate, so this test proves nothing");
        assertTrue(BoardView.hatchedAround(level, deadlocked).contains(destination),
                "the square the player walks onto is not hatched, so this test proves nothing");
        int hatchedFloor = inkInTile(fixture.render(fixture.game), level, destination);

        fixture.move(Direction.LEFT);
        Surface firstFrame = renderAt(fixture, FIRST_FRAME, deadlocked);

        assertEquals(hatchedFloor, inkInTile(firstFrame, level, destination),
                "the tween redrew a hatched square as plain floor");
    }

    private static ScreenFixture withoutHints(ScreenFixture fixture) {
        fixture.settings.toggleHints();
        assertFalse(fixture.settings.hintsEnabled());
        return fixture;
    }

    private static Tween tweenOf(ScreenFixture fixture) {
        return FxToolkit.on(fixture.game::tweenInFlight).orElseThrow();
    }

    private static Surface renderAt(ScreenFixture fixture, long nowNanos, Set<Position> deadlocked) {
        return FxToolkit.on(() -> {
            Optional<Tween> tween = fixture.game.tweenInFlight();
            assertTrue(tween.isPresent(), "the move left no tween to draw");
            return GameView.render(fixture.session, BoardFixture.typeSetter(), deadlocked, tween, nowNanos);
        });
    }

    private static int inkInTile(Surface surface, Level level, Position at) {
        int left = GameView.boardOriginX(level.columnCount()) + at.col() * Tiles.TILE;
        int top = GameView.HUD_TOP + at.row() * Tiles.TILE;
        int ink = 0;
        for (int y = top; y < top + Tiles.TILE; y++) {
            for (int x = left; x < left + Tiles.TILE; x++) {
                if (Threshold.isInk(surface.toneAt(x, y))) {
                    ink++;
                }
            }
        }
        return ink;
    }
}
