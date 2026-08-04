package com.milandru.sokobani.persistence;

import java.util.LinkedHashMap;
import java.util.Map;

final class MiniJson {

    private static final int MAX_DEPTH = 32;

    private final String source;
    private int position;
    private int depth;

    private MiniJson(String source) {
        this.source = source;
    }

    static Map<String, Object> parseObject(String source) {
        MiniJson parser = new MiniJson(source);
        Object value = parser.readValue();
        parser.skipWhitespace();
        if (parser.position != source.length()) {
            throw new IllegalArgumentException("trailing content at position " + parser.position);
        }
        if (!(value instanceof Map)) {
            throw new IllegalArgumentException("expected a JSON object at the top level");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) value;
        return result;
    }

    private Object readValue() {
        skipWhitespace();
        char c = peek();
        return switch (c) {
            case '{' -> readObject();
            case '"' -> readString();
            case 't', 'f' -> readBoolean();
            default -> readNumber();
        };
    }

    private Map<String, Object> readObject() {
        depth++;
        if (depth > MAX_DEPTH) {
            throw new IllegalArgumentException("nesting exceeds " + MAX_DEPTH + " levels");
        }
        try {
            return readObjectBody();
        } finally {
            depth--;
        }
    }

    private Map<String, Object> readObjectBody() {
        expect('{');
        Map<String, Object> result = new LinkedHashMap<>();
        skipWhitespace();
        if (peek() == '}') {
            position++;
            return result;
        }
        while (true) {
            skipWhitespace();
            String key = readString();
            skipWhitespace();
            expect(':');
            Object value = readValue();
            result.put(key, value);
            skipWhitespace();
            char next = peek();
            if (next == ',') {
                position++;
            } else if (next == '}') {
                position++;
                break;
            } else {
                throw new IllegalArgumentException("expected ',' or '}' at position " + position);
            }
        }
        return result;
    }

    private String readString() {
        expect('"');
        StringBuilder built = new StringBuilder();
        while (true) {
            if (position >= source.length()) {
                throw new IllegalArgumentException("unterminated string");
            }
            char c = source.charAt(position++);
            if (c == '"') {
                break;
            }
            if (c == '\\') {
                if (position >= source.length()) {
                    throw new IllegalArgumentException("unterminated escape");
                }
                char escaped = source.charAt(position++);
                built.append(switch (escaped) {
                    case '"' -> '"';
                    case '\\' -> '\\';
                    case 'n' -> '\n';
                    default -> throw new IllegalArgumentException("unsupported escape \\" + escaped);
                });
            } else {
                built.append(c);
            }
        }
        return built.toString();
    }

    private Boolean readBoolean() {
        if (source.startsWith("true", position)) {
            position += 4;
            return Boolean.TRUE;
        }
        if (source.startsWith("false", position)) {
            position += 5;
            return Boolean.FALSE;
        }
        throw new IllegalArgumentException("invalid literal at position " + position);
    }

    private Long readNumber() {
        int start = position;
        if (position < source.length() && source.charAt(position) == '-') {
            position++;
        }
        while (position < source.length() && Character.isDigit(source.charAt(position))) {
            position++;
        }
        if (position == start || (position - start == 1 && source.charAt(start) == '-')) {
            throw new IllegalArgumentException("invalid number at position " + start);
        }
        return Long.parseLong(source.substring(start, position));
    }

    private char peek() {
        if (position >= source.length()) {
            throw new IllegalArgumentException("unexpected end of input");
        }
        return source.charAt(position);
    }

    private void expect(char c) {
        if (position >= source.length() || source.charAt(position) != c) {
            throw new IllegalArgumentException("expected '" + c + "' at position " + position);
        }
        position++;
    }

    private void skipWhitespace() {
        while (position < source.length() && Character.isWhitespace(source.charAt(position))) {
            position++;
        }
    }
}
