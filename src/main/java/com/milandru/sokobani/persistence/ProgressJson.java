package com.milandru.sokobani.persistence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class ProgressJson {

    private ProgressJson() {
    }

    static String serialize(Progress progress) {
        List<Integer> levels = new ArrayList<>(progress.levels().keySet());
        Collections.sort(levels);

        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"unlockedIndex\": ").append(progress.unlockedIndex()).append(",\n");
        json.append("  \"levels\": {");
        for (int i = 0; i < levels.size(); i++) {
            int level = levels.get(i);
            Progress.LevelRecord record = progress.levels().get(level);
            json.append(i == 0 ? "\n" : ",\n");
            json.append("    \"").append(level).append("\": {")
                    .append("\"solved\": ").append(record.solved()).append(", ")
                    .append("\"bestMoves\": ").append(record.bestMoves()).append(", ")
                    .append("\"bestPushes\": ").append(record.bestPushes())
                    .append("}");
        }
        json.append(levels.isEmpty() ? "}\n" : "\n  }\n");
        json.append("}\n");
        return json.toString();
    }

    static Progress parse(String content) {
        Map<String, Object> root = MiniJson.parseObject(content);
        int unlockedIndex = Math.toIntExact((Long) requireField(root, "unlockedIndex"));
        Object levelsValue = requireField(root, "levels");
        if (!(levelsValue instanceof Map)) {
            throw new IllegalArgumentException("\"levels\" must be an object");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> levelsJson = (Map<String, Object>) levelsValue;

        Map<Integer, Progress.LevelRecord> levels = new HashMap<>();
        for (Map.Entry<String, Object> entry : levelsJson.entrySet()) {
            int level = Integer.parseInt(entry.getKey());
            Object recordValue = entry.getValue();
            if (!(recordValue instanceof Map)) {
                throw new IllegalArgumentException("level record for " + level + " must be an object");
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> recordJson = (Map<String, Object>) recordValue;
            boolean solved = (Boolean) requireField(recordJson, "solved");
            int bestMoves = Math.toIntExact((Long) requireField(recordJson, "bestMoves"));
            int bestPushes = Math.toIntExact((Long) requireField(recordJson, "bestPushes"));
            levels.put(level, new Progress.LevelRecord(solved, bestMoves, bestPushes));
        }
        return new Progress(levels, unlockedIndex);
    }

    private static Object requireField(Map<String, Object> object, String field) {
        if (!object.containsKey(field)) {
            throw new IllegalArgumentException("missing field \"" + field + "\"");
        }
        return object.get(field);
    }
}
