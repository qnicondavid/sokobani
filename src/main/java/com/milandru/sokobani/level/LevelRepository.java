package com.milandru.sokobani.level;

import com.milandru.sokobani.core.Level;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class LevelRepository {

    public static final String CLASSIC_PACK = "levels/classic.sok";

    private static final char COMMENT = ';';
    private static final String EXTENSION = ".sok";
    private static final String LINE_BREAK = "\r\n|\r|\n";
    private static final String LEVEL_SEPARATOR = "\n";

    private record Block(String name, List<String> lines, int firstLineNumber) {
    }

    private LevelRepository() {
    }

    public static LevelPack load(String resourcePath) throws IOException, InvalidLevelFormatException {
        Objects.requireNonNull(resourcePath, "resourcePath");
        return packOf(read(resourcePath), packNameOf(resourcePath));
    }

    private static String read(String resourcePath) throws IOException {
        try (InputStream stream = LevelRepository.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new FileNotFoundException("no level pack on the classpath at " + resourcePath);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String packNameOf(String resourcePath) {
        String fileName = resourcePath.substring(resourcePath.lastIndexOf('/') + 1);
        return fileName.endsWith(EXTENSION)
                ? fileName.substring(0, fileName.length() - EXTENSION.length())
                : fileName;
    }

    private static LevelPack packOf(String text, String packName) throws InvalidLevelFormatException {
        List<Level> levels = new ArrayList<>();
        for (Block block : blocksIn(text)) {
            int index = levels.size();
            String name = block.name().isEmpty() ? "Level " + (index + 1) : block.name();
            levels.add(LevelParser.parse(
                    String.join(LEVEL_SEPARATOR, block.lines()), name, index, block.firstLineNumber()));
        }
        if (levels.isEmpty()) {
            throw new InvalidLevelFormatException("pack " + packName + " has no levels");
        }
        return new LevelPack(packName, levels);
    }

    private static List<Block> blocksIn(String text) {
        List<Block> blocks = new ArrayList<>();
        List<String> lines = new ArrayList<>();
        String name = "";
        int firstLineNumber = 0;
        List<String> all = Arrays.asList(text.split(LINE_BREAK, -1));
        for (int i = 0; i < all.size(); i++) {
            String line = all.get(i);
            if (line.isBlank()) {
                if (!lines.isEmpty()) {
                    blocks.add(new Block(name, List.copyOf(lines), firstLineNumber));
                    lines.clear();
                }
                name = "";
            } else if (lines.isEmpty() && line.charAt(0) == COMMENT) {
                if (name.isEmpty()) {
                    name = line.substring(1).trim();
                }
            } else {
                if (lines.isEmpty()) {
                    firstLineNumber = i + 1;
                }
                lines.add(line);
            }
        }
        if (!lines.isEmpty()) {
            blocks.add(new Block(name, List.copyOf(lines), firstLineNumber));
        }
        return blocks;
    }
}
