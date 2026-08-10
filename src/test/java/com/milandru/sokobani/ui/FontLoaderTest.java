package com.milandru.sokobani.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FontLoaderTest {

    private static final int CAPTION_SIZE = 10;
    private static final int FIGURE_SIZE = 17;
    private static final int TITLE_SIZE = 20;

    @Test
    void strokeWidthFor_theSizeItWasChosenAt_isTheOriginalNineTenths() {
        assertEquals(0.9, FontLoader.strokeWidthFor(FIGURE_SIZE));
    }

    @Test
    void strokeWidthFor_theCaptionSize_isJustOverHalfAPixel() {
        assertEquals(0.529, FontLoader.strokeWidthFor(CAPTION_SIZE), 0.001);
    }

    @Test
    void strokeWidthFor_theSizesInTheScale_riseWithTheSize() {
        double previous = 0;
        for (int size : new int[]{CAPTION_SIZE, 11, 12, 13, FIGURE_SIZE, 18, TITLE_SIZE}) {
            double width = FontLoader.strokeWidthFor(size);
            assertTrue(width > previous, size + "px strokes at " + width);
            previous = width;
        }
    }

    @Test
    void strokeWidthFor_everySizeUnderTheFigures_isLighterThanTheOldFixedNineTenths() {
        for (int size = TypeSetter.MIN_SIZE; size < FIGURE_SIZE; size++) {
            assertTrue(FontLoader.strokeWidthFor(size) < 0.9, size + "px still strokes at 0.9 or more");
        }
    }

    @Test
    void strokeWidthFor_theCaptionSize_addsLessThanHalfAPixelEachSide() {
        assertTrue(FontLoader.strokeWidthFor(CAPTION_SIZE) / 2 < 0.5,
                "the caption still gains half a pixel of ink on every side of every stem");
    }
}
