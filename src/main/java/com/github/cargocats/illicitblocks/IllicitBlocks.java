package com.github.cargocats.illicitblocks;

import com.github.cargocats.illicitblocks.item.BlockStateBlockItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.Block;
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

import java.util.ArrayList;

public class IllicitBlocks implements ModInitializer {
    public static final String MOD_ID = "illicitblocks";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_ID);
    public static final ArrayList<Identifier> blocksToHandle = new ArrayList<>();
    public static final boolean DEBUG_LOGGING = true;

    public static final RegistryKey<ItemGroup> ILLICIT_BLOCKS_ITEM_GROUP_KEY = RegistryKey.of(Registries.ITEM_GROUP.getKey(), Identifier.of(MOD_ID, "illicitblocks_item_group"));
    public static final ItemGroup ILLICIT_BLOCKS_ITEM_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(Items.BARRIER))
            .displayName(Text.literal("Illicit Blocks"))
            .build();

    @Override
    public void onInitialize() {
        ConfigManager.loadConfig();

        Registry.register(Registries.ITEM_GROUP, ILLICIT_BLOCKS_ITEM_GROUP_KEY, ILLICIT_BLOCKS_ITEM_GROUP);

        RegistryEntryAddedCallback.event(Registries.BLOCK).register((rawId, id, block) -> handleBlock(block));
        Registries.BLOCK.forEach(this::handleBlock);

        LOG.info("Loaded Illicit Blocks");
    }

    private void handleBlock(Block block) {
        Identifier blockId = Registries.BLOCK.getId(block);

        if (!shouldHandleBlock(blockId)) {
            return;
        }

        if (DEBUG_LOGGING) {
            LOG.info(
                    "Attempt to handle block id: {}, item form: {}, exists in item reg: {}",
                    blockId,
                    block.asItem(),
                    Registries.ITEM.containsId(blockId)
            );
        }

        if (block != Blocks.AIR && !Registries.ITEM.containsId(blockId)) {
            if (ConfigManager.config.register_block_items) {
                BlockItem blockItem = new BlockStateBlockItem(block, new Item.Settings()
                        .registryKey(RegistryKey.of(RegistryKeys.ITEM, Registries.BLOCK.getId(block)))
                        .useBlockPrefixedTranslationKey()
                );

                Registry.register(Registries.ITEM, Registries.BLOCK.getId(block), blockItem);
                ItemGroupEvents.modifyEntriesEvent(ILLICIT_BLOCKS_ITEM_GROUP_KEY).register(itemGroup -> itemGroup.add(blockItem));
            }

            if (DEBUG_LOGGING) {
                LOG.info("Adding in illicit block for block {}", block);
            }

            blocksToHandle.add(blockId);
        }
    }

    public static boolean shouldHandleBlock(Identifier blockId) {
        if ((
                ConfigManager.config.excluded_identifiers.contains(blockId.toString()) || ConfigManager.config.excluded_namespaces.contains(blockId.getNamespace()))
                && !ConfigManager.config.included_identifiers.contains(blockId.toString())
        ) {
            if (DEBUG_LOGGING) LOG.info("Ignoring block {}", blockId);
            return false;
        }

        if (
                ConfigManager.config.use_static_list && !ConfigManager.config.static_list.contains(blockId.toString())
                        && !ConfigManager.config.included_identifiers.contains(blockId.toString())
        ) {
            if (DEBUG_LOGGING) LOG.info("Ignoring block because not in static and not in included identifiers {}", blockId);
            return false;
        }

        return true;
    }
}
