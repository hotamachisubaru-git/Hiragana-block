package org.hotamachisubaru.hiraganablock.neoforge;

import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.world.item.CreativeModeTabs;
import org.hotamachisubaru.hiraganablock.core.*;
import org.hotamachisubaru.hiraganablock.minecraft.*;

@Mod(KanaCatalog.MOD_ID)
public final class HiraganaNeoForge {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(KanaCatalog.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(KanaCatalog.MOD_ID);
    public HiraganaNeoForge(IEventBus bus) {
        for (var kana : KanaCatalog.ALL) for (var color : BlockColor.values()) {
            var letter = new LetterBlock(kana,color);
            var block = BLOCKS.registerSimpleBlock(letter.id(), props -> props.strength(1.5f,6f));
            ITEMS.registerSimpleBlockItem(letter.id(),block);
            NativeBlocks.add(letter,block);
        }
        BLOCKS.register(bus); ITEMS.register(bus);
        bus.addListener(HiraganaNeoForge::creative);
        NeoForge.EVENT_BUS.addListener(HiraganaNeoForge::commands);
    }
    private static void commands(RegisterCommandsEvent event) { NativeCommands.register(event.getDispatcher()); }
    private static void creative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) NativeBlocks.BLOCKS.values().forEach(block -> event.accept(block.get().asItem()));
    }
}
