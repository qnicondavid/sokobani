package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.ui.Surface;
import com.milandru.sokobani.ui.TypeSetter;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TypeTest {

    @Test
    void everyStyleInTheScale_isAtLeastTheTenPixelFloor() throws Exception {
        List<Field> styles = declaredStyles();

        assertFalse(styles.isEmpty());
        for (Field style : styles) {
            Type.Style value = (Type.Style) style.get(null);

            assertTrue(value.size() >= TypeSetter.MIN_SIZE,
                    style.getName() + " is " + value.size() + "px, below the " + TypeSetter.MIN_SIZE + "px floor");
        }
    }

    @Test
    void theScale_matchesTheSpecifiedSizesAndTracking() {
        assertEquals(new Type.Style(20, 2.6), Type.TITLE);
        assertEquals(new Type.Style(11, 2.0), Type.SUBTITLE);
        assertEquals(new Type.Style(12, 1.0), Type.COUNTER);
        assertEquals(new Type.Style(10, 0.8), Type.CAPTION);
        assertEquals(new Type.Style(17, 0.0), Type.FIGURE);
    }

    @Test
    void width_dropsTheTrailingTrackingSoTextCentresOnItsInk() {
        TypeSetter setter = BoardFixture.typeSetter();

        double measured = setter.measure("AB", Type.TITLE.size(), Type.TITLE.tracking());
        double visual = Type.width(setter, "AB", Type.TITLE);

        assertEquals(measured - Type.TITLE.tracking(), visual, 1.0e-9);
    }

    @Test
    void width_ofEmptyText_isZero() {
        assertEquals(0, Type.width(BoardFixture.typeSetter(), "", Type.TITLE));
    }

    @Test
    void centred_putsTheSameAmountOfPaperOnBothSides() {
        Surface surface = new Surface(200, 40);

        Type.centred(BoardFixture.typeSetter(), surface, "SOKOBANI", 100, 30, Type.TITLE, Surface.INK);

        assertTrue(Math.abs(leftmostInk(surface) - (surface.width() - 1 - rightmostInk(surface))) <= 1);
    }

    @Test
    void flushRight_endsWhereItIsAsked() {
        Surface surface = new Surface(200, 40);

        Type.flushRight(BoardFixture.typeSetter(), surface, "PUSHES", 150, 30, Type.COUNTER, Surface.INK);

        assertTrue(rightmostInk(surface) <= 150);
        assertTrue(rightmostInk(surface) >= 150 - Type.COUNTER.size());
    }

    @Test
    void flushLeft_startsWhereItIsAsked() {
        Surface surface = new Surface(200, 40);

        Type.flushLeft(BoardFixture.typeSetter(), surface, "MOVES", 22, 30, Type.COUNTER, Surface.INK);

        assertTrue(leftmostInk(surface) >= 22);
        assertTrue(leftmostInk(surface) <= 22 + Type.COUNTER.size());
    }

    private static List<Field> declaredStyles() {
        List<Field> styles = new ArrayList<>();
        for (Field field : Type.class.getDeclaredFields()) {
            if (field.getType() == Type.Style.class && Modifier.isStatic(field.getModifiers())) {
                styles.add(field);
            }
        }
        return styles;
    }

    private static int leftmostInk(Surface surface) {
        for (int x = 0; x < surface.width(); x++) {
            for (int y = 0; y < surface.height(); y++) {
                if (surface.toneAt(x, y) == Surface.INK) {
                    return x;
                }
            }
        }
        return surface.width();
    }

    private static int rightmostInk(Surface surface) {
        for (int x = surface.width() - 1; x >= 0; x--) {
            for (int y = 0; y < surface.height(); y++) {
                if (surface.toneAt(x, y) == Surface.INK) {
                    return x;
                }
            }
        }
        return -1;
    }
}
