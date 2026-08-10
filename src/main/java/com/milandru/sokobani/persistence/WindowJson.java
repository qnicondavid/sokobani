package com.milandru.sokobani.persistence;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

final class WindowJson {

    private static final String WIDTH = "width";
    private static final String HEIGHT = "height";

    private WindowJson() {
    }

    static Optional<WindowGeometry> parse(String content) {
        Map<String, Object> window = MiniJson.parseObject(content);
        if (!(window.get(WIDTH) instanceof Long width) || !(window.get(HEIGHT) instanceof Long height)) {
            return Optional.empty();
        }
        if (width < 1 || height < 1) {
            return Optional.empty();
        }
        return Optional.of(new WindowGeometry(width.intValue(), height.intValue()));
    }

    static String serialize(WindowGeometry geometry) {
        Objects.requireNonNull(geometry, "geometry");
        return "{\"width\":" + geometry.width() + ",\"height\":" + geometry.height() + "}";
    }
}
