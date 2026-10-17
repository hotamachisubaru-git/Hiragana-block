package org.hotamachisubaru.hiraganablock.minecraft;

import java.util.*;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import org.hotamachisubaru.hiraganablock.core.*;

/** Minecraft-facing source shared by the three mod loaders (not included in Paper). */
public final class NativeBlocks {
    public static final Map<String, Supplier<? extends Block>> BLOCKS = new LinkedHashMap<>();
    private static final Map<String, LetterBlock> LETTERS = new HashMap<>();
    private NativeBlocks() { }
    public static void add(LetterBlock letter, Supplier<? extends Block> block) {
        BLOCKS.put(letter.id(), block); LETTERS.put(letter.id(), letter);
    }
    public static Block get(LetterBlock letter) { return BLOCKS.get(letter.id()).get(); }
    public static LetterBlock identify(Block block) {
        var id = BuiltInRegistries.BLOCK.getKey(block);
        return id.getNamespace().equals(KanaCatalog.MOD_ID) ? LETTERS.get(id.getPath()) : null;
    }
}
