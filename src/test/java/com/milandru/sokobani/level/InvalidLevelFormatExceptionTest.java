package com.milandru.sokobani.level;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InvalidLevelFormatExceptionTest {

    @Test
    void getMessage_problemWithLineAndColumn_readsAsASentenceForTheUser() {
        InvalidLevelFormatException thrown = new InvalidLevelFormatException("illegal character 'x'", 8, 5);

        assertEquals("line 8, column 5: illegal character 'x'", thrown.getMessage());
    }

    @Test
    void getMessage_problemWithALineOnly_omitsTheColumn() {
        InvalidLevelFormatException thrown =
                new InvalidLevelFormatException("level has no goals", 8, InvalidLevelFormatException.NO_POSITION);

        assertEquals("line 8: level has no goals", thrown.getMessage());
    }

    @Test
    void getMessage_problemWithoutAPosition_isTheProblemAlone() {
        assertEquals("pack has no levels", new InvalidLevelFormatException("pack has no levels").getMessage());
    }

    @Test
    void lineAndColumn_exceptionWithAPosition_reportThemBack() {
        InvalidLevelFormatException thrown = new InvalidLevelFormatException("illegal character 'x'", 8, 5);

        assertAll(
                () -> assertEquals(8, thrown.line()),
                () -> assertEquals(5, thrown.column()),
                () -> assertTrue(thrown.hasPosition()));
    }

    @Test
    void hasPosition_exceptionWithoutALine_isFalse() {
        assertFalse(new InvalidLevelFormatException("pack has no levels").hasPosition());
    }

    @Test
    void constructor_nullProblem_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new InvalidLevelFormatException(null, 1, 1));
    }

    @Test
    void exception_isChecked() {
        assertFalse(RuntimeException.class.isAssignableFrom(InvalidLevelFormatException.class));
    }
}
