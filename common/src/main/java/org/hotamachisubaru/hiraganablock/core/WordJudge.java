package org.hotamachisubaru.hiraganablock.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/** Exact matches consume letters first; yellow never exceeds the answer's remaining count. */
public final class WordJudge {
    private WordJudge() { }

    public static List<LetterBlock> evaluate(String answer, String guess) {
        var expected = KanaCatalog.word(answer);
        var actual = KanaCatalog.word(guess);
        if (expected.size() != actual.size()) throw new IllegalArgumentException("答えと回答の文字数が違います。");
        var colors = new BlockColor[actual.size()];
        var remaining = new HashMap<String, Integer>();
        for (int i = 0; i < actual.size(); i++) {
            colors[i] = expected.get(i).equals(actual.get(i)) ? BlockColor.GREEN : BlockColor.BLACK;
            if (colors[i] != BlockColor.GREEN) remaining.merge(expected.get(i), 1, Integer::sum);
        }
        for (int i = 0; i < actual.size(); i++) {
            if (colors[i] == BlockColor.GREEN) continue;
            int count = remaining.getOrDefault(actual.get(i), 0);
            if (count > 0) {
                colors[i] = BlockColor.YELLOW;
                remaining.put(actual.get(i), count - 1);
            }
        }
        var result = new ArrayList<LetterBlock>();
        for (int i = 0; i < actual.size(); i++) result.add(new LetterBlock(actual.get(i), colors[i]));
        return List.copyOf(result);
    }
}
