package org.hotamachisubaru.hiraganablock.core;

import java.util.Objects;

public record LetterBlock(String kana, BlockColor color) {
    public LetterBlock {
        kana = KanaCatalog.requireKana(kana);
        Objects.requireNonNull(color, "color");
    }
    public String id() { return KanaCatalog.id(kana, color); }
}
