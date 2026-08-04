package com.milandru.sokobani.engine;

import com.milandru.sokobani.core.Direction;
import com.milandru.sokobani.core.Position;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoveRecordTest {

    @Test
    void ofMove_anyDirection_keepsTheDirectionAndNoBox() {
        MoveRecord record = MoveRecord.ofMove(Direction.LEFT);

        assertEquals(Direction.LEFT, record.direction());
        assertEquals(Optional.empty(), record.pushedBox());
    }

    @Test
    void isPush_recordOfAMove_returnsFalse() {
        assertFalse(MoveRecord.ofMove(Direction.UP).isPush());
    }

    @Test
    void ofPush_anyDirection_keepsTheDirectionAndTheBox() {
        MoveRecord record = MoveRecord.ofPush(Direction.DOWN, new Position(4, 2));

        assertEquals(Direction.DOWN, record.direction());
        assertEquals(Optional.of(new Position(4, 2)), record.pushedBox());
    }

    @Test
    void isPush_recordOfAPush_returnsTrue() {
        assertTrue(MoveRecord.ofPush(Direction.RIGHT, new Position(0, 0)).isPush());
    }

    @Test
    void equals_twoRecordsOfTheSamePush_areEqual() {
        MoveRecord one = MoveRecord.ofPush(Direction.RIGHT, new Position(2, 3));
        MoveRecord other = MoveRecord.ofPush(Direction.RIGHT, new Position(2, 3));

        assertEquals(one, other);
        assertEquals(one.hashCode(), other.hashCode());
    }

    @Test
    void equals_aMoveAndAPushInTheSameDirection_areNotEqual() {
        assertNotEquals(MoveRecord.ofMove(Direction.RIGHT), MoveRecord.ofPush(Direction.RIGHT, new Position(2, 3)));
    }

    @Test
    void ofMove_nullDirection_throwsNullPointer() {
        assertThrows(NullPointerException.class, () -> MoveRecord.ofMove(null));
    }

    @Test
    void ofPush_nullDirection_throwsNullPointer() {
        assertThrows(NullPointerException.class, () -> MoveRecord.ofPush(null, new Position(1, 1)));
    }

    @Test
    void ofPush_nullBox_throwsNullPointer() {
        assertThrows(NullPointerException.class, () -> MoveRecord.ofPush(Direction.RIGHT, null));
    }

    @Test
    void constructor_nullBoxOptional_throwsNullPointer() {
        assertThrows(NullPointerException.class, () -> new MoveRecord(Direction.RIGHT, null));
    }
}
