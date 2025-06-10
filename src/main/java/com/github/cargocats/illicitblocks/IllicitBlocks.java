package com.github.cargocats.illicitblocks;

import com.github.cargocats.illicitblocks.item.BlockStateBlockItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class IllicitBlocks implements ModInitializer {
    public static final String MOD_ID = "illicitblocks";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_ID);
    public static ArrayList<Identifier> moddedBlocks = new ArrayList<>();
    public static ArrayList<Identifier> createdBlockItems = new ArrayList<>();
    public static boolean DEBUG_LOGGING = true;

    public static final RegistryKey<ItemGroup> ILLICIT_BLOCKS_ITEM_GROUP_KEY = RegistryKey.of(Registries.ITEM_GROUP.getKey(), Identifier.of(MOD_ID, "illicitblocks_item_group"));
    public static final ItemGroup ILLICIT_BLOCKS_ITEM_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(Items.BARRIER))
            .displayName(Text.translatable("illicitblocks.item_group"))
            .build();

    @Override
    public void onInitialize() {
        ConfigManager.loadConfig();
        DEBUG_LOGGING = ConfigManager.config.debug;
        moddedBlocks = ConfigManager.config.modded_block_list.stream()
                .map(Identifier::of)
                .collect(Collectors.toCollection(ArrayList::new));

        Registry.register(Registries.ITEM_GROUP, ILLICIT_BLOCKS_ITEM_GROUP_KEY, ILLICIT_BLOCKS_ITEM_GROUP);

        ArrayList<BlockStateBlockItem> blockItems = registerBlockItems();
        ItemGroupEvents.modifyEntriesEvent(ILLICIT_BLOCKS_ITEM_GROUP_KEY)
                .register(group -> blockItems.forEach(group::add));

        LOG.info("Loaded Illicit Blocks");
    }

    private ArrayList<BlockStateBlockItem> registerBlockItems() {
        ArrayList<BlockStateBlockItem> collected = new ArrayList<>();
        AtomicBoolean hasModdedContent = new AtomicBoolean(false);

        Registries.BLOCK.forEach(block -> {
            Identifier id = Registries.BLOCK.getId(block);
            if ("minecraft".equals(id.getNamespace())) {
                BlockStateBlockItem item = tryRegisterBlock(id);
                if (item != null) collected.add(item);
            } else {
                hasModdedContent.set(true);
            }
        });

        for (Identifier id : moddedBlocks) {
            BlockStateBlockItem item = tryRegisterBlock(id);
            if (item != null) collected.add(item);
        }

        if (hasModdedContent.get() && moddedBlocks.isEmpty()) {
            ItemStack placeholder = new ItemStack(Items.BARRIER);
            placeholder.set(DataComponentTypes.ITEM_NAME, Text.translatable("illicitblocks.placeholder_description"));
            ItemGroupEvents.modifyEntriesEvent(ILLICIT_BLOCKS_ITEM_GROUP_KEY)
                    .register(group -> group.add(placeholder));
        }

        return collected;
    }

    private BlockStateBlockItem tryRegisterBlock(Identifier id) {
        if (!shouldHandleBlock(id)) {
            Utils.debugLog("Do not handle block {}", id);
            return null;
        }

        Block block = Registries.BLOCK.get(id);
        BlockStateBlockItem blockStateBlockItem = new BlockStateBlockItem(block, new Item.Settings()
                .registryKey(RegistryKey.of(RegistryKeys.ITEM, id))
                .useBlockPrefixedTranslationKey());

        Registry.register(Registries.ITEM, id, blockStateBlockItem);
        createdBlockItems.add(id);

        Utils.debugLog("Registered block item: {}", id);
        return blockStateBlockItem;
    }

    public static boolean shouldHandleBlock(Identifier blockId) {
        if (ConfigManager.config.included_identifiers.contains(blockId.toString())) {
            Utils.debugLog("In included identifiers, returning true for {}", blockId);
            return true;
        }

        if (ConfigManager.config.excluded_identifiers.contains(blockId.toString()) || ConfigManager.config.excluded_namespaces.contains(blockId.getNamespace())) {
            Utils.debugLog("In excluded identifiers OR namespace, returning false for {}", blockId);
            return false;
        }

        return Registries.BLOCK.get(blockId) != Blocks.AIR && !Registries.ITEM.containsId(blockId);
    }
}
