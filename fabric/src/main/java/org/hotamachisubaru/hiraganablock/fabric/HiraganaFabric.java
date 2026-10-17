package org.hotamachisubaru.hiraganablock.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.hotamachisubaru.hiraganablock.core.*;
import org.hotamachisubaru.hiraganablock.minecraft.*;

public final class HiraganaFabric implements ModInitializer {
    @Override public void onInitialize() {
        for (var kana : KanaCatalog.ALL) for (var color : BlockColor.values()) {
            var letter = new LetterBlock(kana,color);
            var id = Identifier.fromNamespaceAndPath(KanaCatalog.MOD_ID,letter.id());
            var block = new Block(BlockBehaviour.Properties.of().strength(1.5f,6f).setId(ResourceKey.create(Registries.BLOCK,id)));
            Registry.register(BuiltInRegistries.BLOCK,id,block);
            Registry.register(BuiltInRegistries.ITEM,id,new BlockItem(block,new Item.Properties().useBlockDescriptionPrefix().setId(ResourceKey.create(Registries.ITEM,id))));
            NativeBlocks.add(letter,() -> block);
        }
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.BUILDING_BLOCKS).register(output ->
                NativeBlocks.BLOCKS.values().forEach(block -> output.accept(block.get().asItem())));
        CommandRegistrationCallback.EVENT.register((dispatcher,context,environment) -> NativeCommands.register(dispatcher));
    }
}
