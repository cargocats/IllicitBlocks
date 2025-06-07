package com.github.cargocats.illicitblocks;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IllicitBlocks implements ModInitializer {
    public static final String MOD_ID = "illicitblocks";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_ID);

    public static final RegistryKey<ItemGroup> ILLICIT_BLOCKS_ITEM_GROUP_KEY = RegistryKey.of(Registries.ITEM_GROUP.getKey(), Identifier.of(MOD_ID, "illicitblocks_item_group"));
    public static final ItemGroup ILLICIT_BLOCKS_ITEM_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(Items.BARRIER))
            .displayName(Text.literal("Illicit Blocks"))
            .build();

    @Override
    public void onInitialize() {
        Registry.register(Registries.ITEM_GROUP, ILLICIT_BLOCKS_ITEM_GROUP_KEY, ILLICIT_BLOCKS_ITEM_GROUP);

        Registries.BLOCK.forEach(block -> {
            if (block != Blocks.AIR) {
                if (block.asItem() == Items.AIR) {
                    BlockItem item = new BlockItem(block, new Item.Settings().registryKey(
                            RegistryKey.of(RegistryKeys.ITEM, Identifier.ofVanilla(Registries.BLOCK.getId(block).getPath()))
                    ));

                    Registry.register(Registries.ITEM, Registries.BLOCK.getId(block), item);

                    ItemGroupEvents.modifyEntriesEvent(ILLICIT_BLOCKS_ITEM_GROUP_KEY).register(itemGroup -> itemGroup.add(item));

                    LOG.info("Loading in block: {}", block);
                }
            }
        });

        LOG.info("Loaded Illicit Blocks");
    }
}
