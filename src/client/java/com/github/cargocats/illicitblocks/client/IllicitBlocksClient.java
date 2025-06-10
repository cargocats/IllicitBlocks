package com.github.cargocats.illicitblocks.client;

import com.github.cargocats.illicitblocks.ConfigManager;
import com.github.cargocats.illicitblocks.IllicitBlocks;
import com.github.cargocats.illicitblocks.client.api.AdditionalItemAssetRegistrationCallback;
import com.github.cargocats.illicitblocks.client.api.AdditionalModelRegistrationCallback;
import com.google.gson.JsonObject;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.WallBannerBlock;
import net.minecraft.block.WallHangingSignBlock;
import net.minecraft.block.WallSignBlock;
import net.minecraft.block.WallSkullBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.ItemAsset;
import net.minecraft.client.render.item.model.BasicItemModel;
import net.minecraft.client.render.item.model.SpecialItemModel;
import net.minecraft.client.render.item.model.special.BannerModelRenderer;
import net.minecraft.client.render.item.model.special.HeadModelRenderer;
import net.minecraft.client.render.item.tint.ConstantTintSource;
import net.minecraft.client.render.item.tint.TintSource;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.cargocats.illicitblocks.IllicitBlocks.*;

public class IllicitBlocksClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        AdditionalItemAssetRegistrationCallback.EVENT.register(context -> {
            moddedBlocks.forEach(id -> addItemAssetForId(context, id));

            createdBlockItems.forEach(id -> {
                if (id.getNamespace().equals("minecraft")) {
                    addItemAssetForId(context, id);
                }
            });
        });

        AdditionalModelRegistrationCallback.EVENT.register(context -> {
            moddedBlocks.forEach(id -> addModelForId(context, id));

            createdBlockItems.forEach(id -> {
                if (id.getNamespace().equals("minecraft")) {
                    addModelForId(context, id);
                }
            });
        });

        ClientLifecycleEvents.CLIENT_STARTED.register(this::dumpBlocks);
    }

    private void dumpBlocks(MinecraftClient client) {
        if (!ConfigManager.config.create_list_after_freeze) return;

        ArrayList<String> moddedBlockList = ConfigManager.config.modded_block_list;
        AtomicBoolean newBlock = new AtomicBoolean(false);

        Registries.BLOCK.forEach(block -> {
            Identifier blockId = Registries.BLOCK.getId(block);

            if (blockId.getNamespace().equals("minecraft")) return;

            if (block != Blocks.AIR && !Registries.ITEM.containsId(blockId) && shouldHandleBlock(blockId)) {
                if (!moddedBlockList.contains(blockId.toString())) {
                    newBlock.set(true);
                }
                moddedBlockList.add(blockId.toString());
            }
        });

        if (!newBlock.get()) {
            IllicitBlocks.LOG.info("No new blocks found");
            return;
        }

        ConfigManager.config.modded_block_list = moddedBlockList;
        ConfigManager.saveConfig();

        client.getToastManager().add(
                new SystemToast(
                        new SystemToast.Type(10000),
                        Text.translatable("illicitblocks.mod_name"),
                        Text.translatable("illicitblocks.toast.restart")
                )
        );

        IllicitBlocks.LOG.info("Dumped modded blocks to block_list for config, restart minecraft");
    }

    public static void addItemAssetForId(AdditionalItemAssetRegistrationCallback.Context context, Identifier id) {
        if (context.hasAsset(id)) return;
        Block block = Registries.BLOCK.get(id);

        List<TintSource> tintSources = new ArrayList<>();

        if (id.equals(Identifier.ofVanilla("water"))) {
            tintSources.add(new ConstantTintSource(ColorHelper.getArgb(63, 118, 228)));
        }

        if (block instanceof WallBannerBlock wallBannerBlock) {
            context.addAsset(id, new ItemAsset(
                    new SpecialItemModel.Unbaked(
                            Identifier.ofVanilla("item/template_banner"),
                            new BannerModelRenderer.Unbaked(wallBannerBlock.getColor())
                    ),
                    ItemAsset.Properties.DEFAULT
            ));
            return;
        }

        if (block instanceof WallSkullBlock wallSkullBlock) {
            context.addAsset(id, new ItemAsset(
                    new SpecialItemModel.Unbaked(
                            Identifier.ofVanilla("item/template_skull"),
                            new HeadModelRenderer.Unbaked(wallSkullBlock.getSkullType())
                    ),
                    ItemAsset.Properties.DEFAULT
            ));
            return;
        }

        context.addAsset(id, new ItemAsset(
                new BasicItemModel.Unbaked(
                        id,
                        tintSources
                ),
                ItemAsset.Properties.DEFAULT
        ));
    }

    public static void addModelForId(AdditionalModelRegistrationCallback.Context context, Identifier id) {
        if (context.hasModel(id)) return;
        Block block = Registries.BLOCK.get(id);

        JsonObject jsonRoot = new JsonObject();
        jsonRoot.addProperty("gui_light", "front");

        switch (block) {
            case FluidBlock fluidBlock -> {
                jsonRoot.addProperty("parent", "minecraft:item/generated");

                JsonObject textures = new JsonObject();
                textures.addProperty("layer0", id.getNamespace() + ":block/" + id.getPath() + "_still");
                jsonRoot.add("textures", textures);
            }
            case CropBlock cropBlock -> jsonRoot.addProperty("parent", "block/" + id.getPath() + "_stage0");
            case WallSignBlock wallSignBlock -> {
                jsonRoot.addProperty("parent", "minecraft:item/generated");

                JsonObject textures = new JsonObject();
                String woodName = wallSignBlock.getWoodType().name().replaceFirst(id.getNamespace() + ":", "");
                if (id.getNamespace().equals("terrestria")) {
                    woodName = extractWoodType(id.toString());
                }

                String itemTextureString = id.getNamespace() + ":item/" + woodName + "_sign";
                textures.addProperty("layer0", itemTextureString);
                jsonRoot.add("textures", textures);
            }
            case WallHangingSignBlock wallHangingSignBlock -> {
                jsonRoot.addProperty("parent", "minecraft:item/generated");

                JsonObject textures = new JsonObject();
                String woodName = wallHangingSignBlock.getWoodType().name().replaceFirst(id.getNamespace() + ":", "");
                if (id.getNamespace().equals("terrestria")) {
                    woodName = extractWoodType(id.toString());
                }

                textures.addProperty("layer0", id.getNamespace() + ":item/" + woodName + "_hanging_sign");
                jsonRoot.add("textures", textures);
            }
            case FlowerPotBlock flowerPotBlock when !id.getNamespace().equals("minecraft") -> {
                jsonRoot.addProperty("parent", "minecraft:block/flower_pot_cross");

                JsonObject textures = new JsonObject();
                textures.addProperty("plant", id.getNamespace() + ":block/" + id.getPath().replace("potted_", ""));
                jsonRoot.add("textures", textures);
            }
            default -> jsonRoot.addProperty("parent", "block/" + id.getPath());
        }

        context.addModel(id, JsonUnbakedModel.deserialize(new StringReader(jsonRoot.toString())));
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
