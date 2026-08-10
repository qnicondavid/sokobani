package com.milandru.sokobani.ui.fx;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SelectionTest {

    @Test
    void startsOnTheFirstItem() {
        Selection<String> selection = new Selection<>(List.of("a", "b", "c"));

        assertEquals(0, selection.index());
        assertEquals("a", selection.selected());
        assertEquals(3, selection.size());
        assertTrue(selection.isSelected(0));
        assertFalse(selection.isSelected(1));
    }

    @Test
    void move_wrapsInBothDirections() {
        Selection<String> selection = new Selection<>(List.of("a", "b", "c"));

        selection.move(-1);
        assertEquals(2, selection.index());
        selection.move(1);
        assertEquals(0, selection.index());
        selection.move(1);
        selection.move(1);
        selection.move(1);
        assertEquals(0, selection.index());
    }

    @Test
    void select_ignoresOutOfRangeIndices() {
        Selection<String> selection = new Selection<>(List.of("a", "b", "c"));

        selection.select(-1);
        assertEquals(0, selection.index());
        selection.select(3);
        assertEquals(0, selection.index());
        selection.select(2);
        assertEquals(2, selection.index());
    }

    @Test
    void refusesAnEmptyItemList() {
        assertThrows(IllegalArgumentException.class, () -> new Selection<>(List.of()));
    }
}
