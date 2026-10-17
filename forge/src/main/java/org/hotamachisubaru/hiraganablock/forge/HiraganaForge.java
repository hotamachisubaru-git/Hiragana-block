package org.hotamachisubaru.hiraganablock.forge;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import org.hotamachisubaru.hiraganablock.core.*;
import org.hotamachisubaru.hiraganablock.minecraft.*;

@Mod(KanaCatalog.MOD_ID)
public final class HiraganaForge {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,KanaCatalog.MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,KanaCatalog.MOD_ID);
    public HiraganaForge(FMLJavaModLoadingContext context) {
        for (var kana : KanaCatalog.ALL) for (var color : BlockColor.values()) {
            var letter = new LetterBlock(kana,color);
            var block = BLOCKS.register(letter.id(), () -> new Block(BlockBehaviour.Properties.of().strength(1.5f,6f).setId(BLOCKS.key(letter.id()))));
            ITEMS.register(letter.id(),() -> new BlockItem(block.get(),new Item.Properties().useBlockDescriptionPrefix().setId(ITEMS.key(letter.id()))));
            NativeBlocks.add(letter,block);
        }
        BLOCKS.register(context.getModBusGroup()); ITEMS.register(context.getModBusGroup());
        RegisterCommandsEvent.BUS.addListener(event -> NativeCommands.register(event.getDispatcher()));
        BuildCreativeModeTabContentsEvent.BUS.addListener(event -> {
            if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) NativeBlocks.BLOCKS.values().forEach(block -> event.accept(block.get().asItem()));
        });
    }
}
