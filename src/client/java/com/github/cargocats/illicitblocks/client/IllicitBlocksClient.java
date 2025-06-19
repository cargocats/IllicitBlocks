package com.github.cargocats.illicitblocks.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemConvertible;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import static com.github.cargocats.illicitblocks.IllicitBlocks.*;

public class IllicitBlocksClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModelLoadingPlugin.register(new IllicitModelPlugin());

        ColorProviderRegistry.ITEM.register((itemStack, tintIndex) -> {
            Identifier id = Registries.ITEM.getId(itemStack.getItem());
            return colorMap.getOrDefault(id, -1);
        }, toBeTinted.toArray(new ItemConvertible[]{}));

        // TODO: Cache results?
        ItemTooltipCallback.EVENT.register((itemStack, context, textList) -> {
            NbtCompound tag = itemStack.getNbt();

            if (tag != null && tag.contains(MOD_ID + "_tooltip")) {
                NbtCompound stateTag = tag.getCompound(BlockItem.BLOCK_STATE_TAG_KEY);

                if (stateTag != null) {
                    for (String key: stateTag.getKeys()) {
                        String value = stateTag.getString(key);
                        Text text = Text.literal(key + ": " + value).formatted(Formatting.GRAY);

                        textList.add(1, text);
                    }
                }
            }
        });
    }

    public static String extractWoodType(String fullId) {
        int colonIndex = fullId.indexOf(":");
        String path = colonIndex >= 0 ? fullId.substring(colonIndex + 1) : fullId;

        if (path.endsWith(":")) {
            path = path.substring(0, path.length() - 1);
        }

        if (path.endsWith("_wall_hanging_sign")) {
            path = path.substring(0, path.length() - "_wall_hanging_sign".length());
        } else if (path.endsWith("_wall_sign")) {
            path = path.substring(0, path.length() - "_wall_sign".length());
        }

        return path;
    }
}