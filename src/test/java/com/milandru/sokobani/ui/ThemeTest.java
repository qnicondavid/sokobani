package com.milandru.sokobani.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ThemeTest {

    @Test
    void catalogue_matchesTheSpecHexValues() {
        assertEquals(0x2B2118, Theme.CATALOGUE.ink());
        assertEquals(0xEDE4D3, Theme.CATALOGUE.paper());
    }

    @Test
    void cyanotype_matchesTheSpecHexValues() {
        assertEquals(0xDCE9F2, Theme.CYANOTYPE.ink());
        assertEquals(0x12354D, Theme.CYANOTYPE.paper());
    }

    @Test
    void bulletin_matchesTheSpecHexValues() {
        assertEquals(0xB8321F, Theme.BULLETIN.ink());
        assertEquals(0xF4EFE2, Theme.BULLETIN.paper());
    }

    @Test
    void phosphor_matchesTheSpecHexValues() {
        assertEquals(0x4BE07B, Theme.PHOSPHOR.ink());
        assertEquals(0x080D0A, Theme.PHOSPHOR.paper());
    }

    @Test
    void default_isCatalogue() {
        assertSame(Theme.CATALOGUE, Theme.DEFAULT);
    }

    @Test
    void all_containsExactlyTheFourThemes() {
        assertEquals(4, Theme.ALL.size());
        assertEquals(Theme.CATALOGUE, Theme.ALL.get(0));
        assertEquals(Theme.CYANOTYPE, Theme.ALL.get(1));
        assertEquals(Theme.BULLETIN, Theme.ALL.get(2));
        assertEquals(Theme.PHOSPHOR, Theme.ALL.get(3));
    }
}
