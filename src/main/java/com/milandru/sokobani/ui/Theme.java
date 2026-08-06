package com.milandru.sokobani.ui;

import java.util.List;

public record Theme(int ink, int paper) {

    public static final Theme CATALOGUE = new Theme(0x2B2118, 0xEDE4D3);
    public static final Theme CYANOTYPE = new Theme(0xDCE9F2, 0x12354D);
    public static final Theme BULLETIN = new Theme(0xB8321F, 0xF4EFE2);
    public static final Theme PHOSPHOR = new Theme(0x4BE07B, 0x080D0A);

    public static final Theme DEFAULT = CATALOGUE;

    public static final List<Theme> ALL = List.of(CATALOGUE, CYANOTYPE, BULLETIN, PHOSPHOR);
}
