package com.milandru.sokobani.persistence;

import java.util.Map;
import java.util.Objects;

final class SettingsJson {

    private static final String MUTED = "muted";
    private static final String ANIMATION = "animation";
    private static final String HINTS = "hints";

    private SettingsJson() {
    }

    static Settings parse(String content) {
        Map<String, Object> settings = MiniJson.parseObject(content);
        boolean muted = boolOf(settings, MUTED, false);
        boolean animationEnabled = boolOf(settings, ANIMATION, true);
        boolean hintsEnabled = boolOf(settings, HINTS, true);
        return new Settings(muted, animationEnabled, hintsEnabled);
    }

    static String serialize(Settings settings) {
        Objects.requireNonNull(settings, "settings");
        return "{\"muted\":" + settings.muted()
                + ",\"animation\":" + settings.animationEnabled()
                + ",\"hints\":" + settings.hintsEnabled() + "}";
    }

    private static boolean boolOf(Map<String, Object> settings, String key, boolean fallback) {
        return settings.get(key) instanceof Boolean value ? value : fallback;
    }
}
