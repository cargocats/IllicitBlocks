package com.github.cargocats.illicitblocks;

import com.github.cargocats.illicitblocks.item.IllicitBlockItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class IllicitBlocks implements ModInitializer {
    public static final String MOD_ID = "illicitblocks";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_ID);
    public static ArrayList<Identifier> moddedBlocks = new ArrayList<>();
    public static ArrayList<Identifier> createdBlockItems = new ArrayList<>();
    public static ArrayList<ItemConvertible> toBeTinted = new ArrayList<>();
    public static final HashMap<Identifier, Integer> colorMap = new HashMap<>();

    public static final Set<String> ignoredProperties = Set.of("facing", "horizontal_facing", "vertical_direction", "axis", "rotation", "east", "west", "south", "north");
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
                .map(Identifier::new)
                .collect(Collectors.toCollection(ArrayList::new));

        Registry.register(Registries.ITEM_GROUP, ILLICIT_BLOCKS_ITEM_GROUP_KEY, ILLICIT_BLOCKS_ITEM_GROUP);

        colorMap.put(new Identifier("minecraft", "water"), ColorHelper.Argb.getArgb(255, 63, 118, 228));
        colorMap.put(new Identifier("minecraft", "redstone_wire"), ColorHelper.Argb.getArgb(255, 189, 48, 49));
        colorMap.put(new Identifier("minecraft", "attached_melon_stem"), ColorHelper.Argb.getArgb(255, 0, 124, 0));
        colorMap.put(new Identifier("minecraft", "attached_pumpkin_stem"), ColorHelper.Argb.getArgb(255, 0, 124, 0));
        colorMap.put(new Identifier("minecraft", "melon_stem"), ColorHelper.Argb.getArgb(255, 0, 124, 0));
        colorMap.put(new Identifier("minecraft", "pumpkin_stem"), ColorHelper.Argb.getArgb(255, 0, 124, 0));

        ArrayList<IllicitBlockItem> blockItems = registerBlockItems();
        ItemGroupEvents.modifyEntriesEvent(ILLICIT_BLOCKS_ITEM_GROUP_KEY)
                .register(group -> blockItems.forEach(blockItem -> {
                    group.add(blockItem);
                    generateItemStacksFromBlockItem(blockItem).forEach(group::add);
                }));

        LOG.info("Loaded Illicit Blocks");
    }

    private ArrayList<IllicitBlockItem> registerBlockItems() {
        ArrayList<IllicitBlockItem> collected = new ArrayList<>();
        AtomicBoolean hasModdedBlocks = new AtomicBoolean(false);
        AtomicBoolean newBlock = new AtomicBoolean(false);

        RegistryEntryAddedCallback.event(Registries.BLOCK).register((ignored, id, block) -> {
            if ("minecraft".equals(id.getNamespace())) {
                IllicitBlockItem item = tryRegisterBlock(id);
                if (item != null) collected.add(item);
            } else {
                if (moddedBlocks.contains(id)) {
                    IllicitBlockItem item = tryRegisterBlock(id);
                    if (item != null) collected.add(item);
                } else {
                    newBlock.set(true);
                }
                hasModdedBlocks.set(true);
            }
        });

        Registries.BLOCK.forEach(block -> {
            Identifier id = Registries.BLOCK.getId(block);

            if ("minecraft".equals(id.getNamespace())) {
                IllicitBlockItem item = tryRegisterBlock(id);
                if (item != null) collected.add(item);
            } else {
                if (moddedBlocks.contains(id)) {
                    IllicitBlockItem item = tryRegisterBlock(id);
                    if (item != null) collected.add(item);
                } else {
                    newBlock.set(true);
                }
                hasModdedBlocks.set(true);
            }
        });

        if (hasModdedBlocks.get() && moddedBlocks.isEmpty() || newBlock.get()) {
            ItemStack placeholder = new ItemStack(Items.BARRIER);
            placeholder.setCustomName(Text.translatable("illicitblocks.placeholder_description"));
            ItemGroupEvents.modifyEntriesEvent(ILLICIT_BLOCKS_ITEM_GROUP_KEY)
                    .register(group -> group.add(placeholder));
        }

        return collected;
    }

    private IllicitBlockItem tryRegisterBlock(Identifier id) {
        if (!shouldHandleBlock(id)) {
            Utils.debugLog("Do not handle block {}", id);
            return null;
        }

        Block block = Registries.BLOCK.get(id);
        IllicitBlockItem blockStateBlockItem = new IllicitBlockItem(block, new Item.Settings());
        Registry.register(Registries.ITEM, id, blockStateBlockItem);
        createdBlockItems.add(id);

        if (colorMap.containsKey(id)) {
            toBeTinted.add(blockStateBlockItem);
        }

        Utils.debugLog("Registered block item: {}", id);
        return blockStateBlockItem;
    }

    private static <T extends Comparable<T>> String getPropertyValue(Property<T> prop, BlockState state) {
        return prop.name(state.get(prop));
    }

    private List<ItemStack> generateItemStacksFromBlockItem(BlockItem blockItem) {
        return blockItem.getBlock().getStateManager().getStates().stream()
                .filter(state -> {
                    for (Property<?> prop : state.getProperties()) {
                        if (!ignoredProperties.contains(prop.getName())) {
                            return true;
                        }
                    }
                    return false;
                })
                .map(state -> {
                    ItemStack stack = new ItemStack(blockItem);
                    NbtCompound tag = stack.getOrCreateNbt();

                    // Encode block state
                    NbtCompound stateTag = new NbtCompound();
                    for (Property<?> prop : state.getProperties()) {
                        if (!ignoredProperties.contains(prop.getName())) {
                            stateTag.putString(prop.getName(), getPropertyValue(prop, state));
                        }
                    }

                    tag.put("BlockState", stateTag);
                    tag.putBoolean(MOD_ID + "_tooltip", true);

                    return stack;
                }).toList();
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
