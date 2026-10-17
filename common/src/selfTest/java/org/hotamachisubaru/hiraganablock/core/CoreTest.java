package org.hotamachisubaru.hiraganablock.core;

import java.util.HashSet;
import java.util.List;

/** Dependency-free regression tests, run by the selfTest task and Gradle check. */
public final class CoreTest {
    private static int passed;
    public static void main(String[] args) {
        equal(87, KanaCatalog.ALL.size());
        equal(87, new HashSet<>(KanaCatalog.ALL).size());
        equal("で", KanaCatalog.requireKana("て\u3099"));
        equal("u3042_black", KanaCatalog.id("あ", BlockColor.BLACK));
        equal(BlockColor.GREEN, BlockColor.parse("緑"));
        equal(BlockColor.YELLOW, BlockColor.parse("YELLOW"));
        colors("あいう", "あいう", BlockColor.GREEN, BlockColor.GREEN, BlockColor.GREEN);
        colors("あいう", "かきく", BlockColor.BLACK, BlockColor.BLACK, BlockColor.BLACK);
        colors("あいう", "うあい", BlockColor.YELLOW, BlockColor.YELLOW, BlockColor.YELLOW);
        colors("あいう", "あああ", BlockColor.GREEN, BlockColor.BLACK, BlockColor.BLACK);
        colors("あいう", "ううう", BlockColor.BLACK, BlockColor.BLACK, BlockColor.GREEN);
        colors("ああい", "いああ", BlockColor.YELLOW, BlockColor.GREEN, BlockColor.YELLOW);
        colors("でぶ", "て\u3099ふ\u3099", BlockColor.GREEN, BlockColor.GREEN);
        colors("きゃく", "きやく", BlockColor.GREEN, BlockColor.BLACK, BlockColor.GREEN);
        rejected(() -> KanaCatalog.word(""));
        rejected(() -> KanaCatalog.word("あ".repeat(33)));
        rejected(() -> KanaCatalog.word("ア"));
        rejected(() -> KanaCatalog.word("あ い"));
        rejected(() -> KanaCatalog.requireKana("あい"));
        rejected(() -> WordJudge.evaluate("あい", "あ"));
        rejected(() -> BlockColor.parse("red"));
        var ids = new HashSet<String>();
        for (var kana : KanaCatalog.ALL) for (var color : BlockColor.values()) ids.add(KanaCatalog.id(kana, color));
        equal(261, ids.size());
        // Exhaustively verify duplicate accounting for all 3-letter words over a 3-letter alphabet.
        for (String a : words()) for (String g : words()) {
            var result = WordJudge.evaluate(a, g);
            for (String kana : List.of("あ", "い", "う")) {
                long matched = result.stream().filter(x -> x.kana().equals(kana) && x.color() != BlockColor.BLACK).count();
                long ac = a.chars().filter(c -> c == kana.charAt(0)).count();
                long gc = g.chars().filter(c -> c == kana.charAt(0)).count();
                equal(Math.min(ac, gc), matched);
            }
        }
        System.out.println("CoreTest: " + passed + " assertions passed");
    }
    private static List<String> words() {
        var out = new java.util.ArrayList<String>();
        for (var a : List.of("あ", "い", "う")) for (var b : List.of("あ", "い", "う"))
            for (var c : List.of("あ", "い", "う")) out.add(a + b + c);
        return out;
    }
    private static void colors(String a, String g, BlockColor... expected) {
        equal(List.of(expected), WordJudge.evaluate(a, g).stream().map(LetterBlock::color).toList());
    }
    private static void equal(Object expected, Object actual) {
        if (!expected.equals(actual)) throw new AssertionError(expected + " != " + actual);
        passed++;
    }
    private static void rejected(Runnable action) {
        try { action.run(); } catch (IllegalArgumentException e) { passed++; return; }
        throw new AssertionError("Expected invalid input to be rejected");
    }
}
