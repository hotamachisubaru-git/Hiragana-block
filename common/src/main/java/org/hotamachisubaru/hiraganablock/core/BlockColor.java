package org.hotamachisubaru.hiraganablock.core;

import java.util.Locale;

public enum BlockColor {
    BLACK("黒"), GREEN("緑"), YELLOW("黄");

    private final String label;
    BlockColor(String label) { this.label = label; }
    public String label() { return label; }
    public static BlockColor parse(String value) {
        for (var color : values()) {
            if (color.name().toLowerCase(Locale.ROOT).equals(value.toLowerCase(Locale.ROOT)) || color.label.equals(value)) return color;
        }
        throw new IllegalArgumentException("色は black / green / yellow です。");
    }
}
