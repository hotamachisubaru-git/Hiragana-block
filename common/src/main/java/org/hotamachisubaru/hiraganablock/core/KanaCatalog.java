package org.hotamachisubaru.hiraganablock.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

/** All platforms and generated assets use the same UTF-8 catalogue. */
public final class KanaCatalog {
    public static final String MOD_ID = "hiraganablock";
    public static final List<String> ALL = load();

    private KanaCatalog() { }

    private static List<String> load() {
        try (var input = KanaCatalog.class.getResourceAsStream("/kana.txt")) {
            if (input == null) throw new IllegalStateException("Missing kana.txt");
            var text = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            return text.codePoints().filter(cp -> !Character.isWhitespace(cp))
                    .mapToObj(cp -> new String(Character.toChars(cp))).toList();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load kana.txt", e);
        }
    }

    public static String normalize(String text) {
        if (text == null) throw new IllegalArgumentException("文字を指定してください。");
        return Normalizer.normalize(text, Normalizer.Form.NFC);
    }

    public static String requireKana(String text) {
        var kana = normalize(text);
        if (!ALL.contains(kana)) throw new IllegalArgumentException("対応するひらがな1文字を指定してください: " + text);
        return kana;
    }

    public static List<String> word(String text) {
        var normalized = normalize(text);
        var chars = normalized.codePoints().mapToObj(cp -> new String(Character.toChars(cp))).toList();
        if (chars.isEmpty() || chars.size() > 32) throw new IllegalArgumentException("文字数は1〜32文字です。");
        chars.forEach(KanaCatalog::requireKana);
        return chars;
    }

    public static String id(String kana, BlockColor color) {
        return "u" + Integer.toHexString(requireKana(kana).codePointAt(0)) + "_" + color.name().toLowerCase(Locale.ROOT);
    }
}
