package engine.math.test;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MustStudy2 {
    public static JSONObject parseVocabulary(String input) {
        JSONObject map=new JSONObject();
        List<String> entries = splitIntoEntries(input);

        Pattern patternWithPhonetic = Pattern.compile(
                "^\\*?(.+?)\\s+(/[^/]+/)\\s*(.*)$",
                Pattern.DOTALL
        );

        Pattern patternWithoutPhonetic = Pattern.compile(
                "^\\*?(.+?)\\s{2,}(.*)$",
                Pattern.DOTALL
        );

        for (String entry : entries) {

            String cleaned = entry.replaceAll("\\(\\d+\\)\\s*$", "").trim();


            Matcher matcher = patternWithPhonetic.matcher(cleaned);
            if (matcher.find()) {
                String word = matcher.group(1).trim();
                String explanation = matcher.group(3).trim();
                map.put(word, explanation);
                continue;
            }


            matcher = patternWithoutPhonetic.matcher(cleaned);
            if (matcher.find()) {
                String word = matcher.group(1).trim();
                String explanation = matcher.group(2).trim();
                map.put(word, explanation);
                continue;
            }


            int chineseIndex = findFirstChineseChar(cleaned);
            if (chineseIndex != -1) {
                String word = cleaned.substring(0, chineseIndex).trim();
                String explanation = cleaned.substring(chineseIndex).trim();
                map.put(word, explanation);
            } else {

                int spaceIndex = cleaned.indexOf(' ');
                if (spaceIndex != -1) {
                    String word = cleaned.substring(0, spaceIndex).trim();
                    String explanation = cleaned.substring(spaceIndex).trim();
                    map.put(word, explanation);
                } else {
                    map.put(cleaned, "[解析失败]");
                }
            }
        }

        return map;
    }

    private static List<String> splitIntoEntries(String input) {
        List<String> entries = new ArrayList<>();
        String[] lines = input.split("\n");
        StringBuilder currentEntry = new StringBuilder();

        for (String line : lines) {
            currentEntry.append(line).append("\n");
            if (line.trim().matches(".*\\(\\d+\\)\\s*$")) {
                entries.add(currentEntry.toString());
                currentEntry = new StringBuilder();
            }
        }


        if (!currentEntry.toString().isEmpty()) {
            entries.add(currentEntry.toString());
        }

        return entries;
    }

    private static int findFirstChineseChar(String str) {
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c >= '\u4E00' && c <= '\u9FFF') {
                return i;
            }
        }
        return -1;
    }
}
