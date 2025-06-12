package com.github.cargocats.illicitblocks.client;

import com.github.cargocats.illicitblocks.IllicitBlocks;
import com.github.cargocats.illicitblocks.Utils;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelResolver;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BubbleColumnBlock;
import net.minecraft.block.CocoaBlock;
import net.minecraft.block.CropBlock;
import net.minecraft.block.EndGatewayBlock;
import net.minecraft.block.EndPortalBlock;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.FrostedIceBlock;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.block.PistonExtensionBlock;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.StemBlock;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.block.TallPlantBlock;
import net.minecraft.block.TripwireBlock;
import net.minecraft.block.WallHangingSignBlock;
import net.minecraft.block.WallSignBlock;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.List;

import static com.github.cargocats.illicitblocks.IllicitBlocks.createdBlockItems;
import static com.github.cargocats.illicitblocks.client.IllicitBlocksClient.extractWoodType;

public class IllicitModelPlugin implements ModelLoadingPlugin {
    private static final HashMap<Identifier, Identifier> blockIdToItemId = new HashMap<>();
    private static final HashMap<Identifier, Identifier> itemIdToBlockId = new HashMap<>();
    // This is necessary because these mods don't use their own WoodTypes
    private static final List<String> annoyingMods = List.of("terrestria", "cinderscapes");

    @Override
    public void onInitializeModelLoader(Context pluginContext) {
        // make sure it is checking all our models
        pluginContext.addModels(createdBlockItems);

        createdBlockItems.forEach(blockId -> {
            Identifier itemId = new Identifier(blockId.getNamespace(), "item/" + blockId.getPath());
            blockIdToItemId.put(blockId, itemId);
            itemIdToBlockId.put(itemId, blockId);
        });

        pluginContext.resolveModel().register(context -> {
            // can be either :block/ or :item/

            Identifier id = context.id();

            if (blockIdToItemId.containsValue(id)) {
                Identifier blockId = itemIdToBlockId.get(id);
                return JsonUnbakedModel.deserialize(getModelForId(blockId, context));
            }
            return null;
        });

        // TODO: somehow do tints, and special models

        IllicitBlocks.LOG.info("Loaded Illicit Blocks model plugin");
    }

    public static String getModelForId(Identifier id, ModelResolver.Context context) {
        Block block = Registries.BLOCK.get(id);

        JsonObject jsonRoot = new JsonObject();
        jsonRoot.addProperty("gui_light", "front");

        switch (block) {
            case FluidBlock ignored -> {
                jsonRoot.addProperty("parent", "minecraft:item/generated");

                JsonObject textures = new JsonObject();
                jsonRoot.add("textures", textures);

                textures.addProperty("layer0", id.getNamespace() + ":block/" + id.getPath() + "_still");
            }
            case CropBlock ignored -> jsonRoot.addProperty("parent", "block/" + id.getPath() + "_stage0");
            case WallSignBlock wallSignBlock -> {
                jsonRoot.addProperty("parent", "minecraft:item/generated");

                JsonObject textures = new JsonObject();
                jsonRoot.add("textures", textures);

                String woodName = wallSignBlock.getWoodType().name().replaceFirst(id.getNamespace() + ":", "");
                if (annoyingMods.contains(id.getNamespace())) {
                    woodName = extractWoodType(id.toString());
                }

                String itemTextureString = id.getNamespace() + ":item/" + woodName + "_sign";
                textures.addProperty("layer0", itemTextureString);
            }
            case WallHangingSignBlock wallHangingSignBlock -> {
                jsonRoot.addProperty("parent", "minecraft:item/generated");

                JsonObject textures = new JsonObject();
                jsonRoot.add("textures", textures);

                String woodName = wallHangingSignBlock.getWoodType().name().replaceFirst(id.getNamespace() + ":", "");
                if (annoyingMods.contains(id.getNamespace())) {
                    woodName = extractWoodType(id.toString());
                }

                textures.addProperty("layer0", id.getNamespace() + ":item/" + woodName + "_hanging_sign");
            }
            case FlowerPotBlock ignored when !id.getNamespace().equals("minecraft") -> {
                jsonRoot.addProperty("parent", "minecraft:block/flower_pot_cross");

                String texture = id.getPath().replace("potted_", "");

                JsonObject textures = new JsonObject();
                jsonRoot.add("textures", textures);

                // TODO: Reimplement the double plant potted plant thing.

                textures.addProperty("plant", id.getNamespace() + ":block/" + texture);
            }
            case TallPlantBlock ignored -> {
                jsonRoot.addProperty("parent", "minecraft:item/generated");

                JsonObject textures = new JsonObject();
                jsonRoot.add("textures", textures);

                textures.addProperty("layer0", id.getNamespace() + ":block/" + id.getPath() + "_bottom");
            }
            case AbstractFireBlock ignored -> {
                jsonRoot.addProperty("parent", "minecraft:item/generated");

                JsonObject textures = new JsonObject();
                jsonRoot.add("textures", textures);

                textures.addProperty("layer0", id.getNamespace() + ":block/" + id.getPath() + "_0");
            }
            case StemBlock ignored -> {
                jsonRoot.addProperty("parent", "minecraft:item/generated");
                JsonObject textures = new JsonObject();
                jsonRoot.add("textures", textures);

                textures.addProperty("layer0", id.getNamespace() + ":block/" + id.getPath());
            }
            case LeveledCauldronBlock ignored -> jsonRoot.addProperty("parent", id.getNamespace() + ":block/" + id.getPath() + "_full");
            case SweetBerryBushBlock ignored -> jsonRoot.addProperty("parent", "minecraft:block/sweet_berry_bush_stage0");
            case FrostedIceBlock ignored -> jsonRoot.addProperty("parent", "minecraft:block/frosted_ice_3");
            case TripwireBlock ignored -> jsonRoot.addProperty("parent", "minecraft:block/tripwire_attached_nsew");
            case RedstoneWireBlock ignored -> {
                jsonRoot.addProperty("parent", "minecraft:item/generated");
                JsonObject textures = new JsonObject();
                jsonRoot.add("textures", textures);

                textures.addProperty("layer0", "minecraft:block/redstone_dust_dot");
            }
            case BubbleColumnBlock ignored -> {
                jsonRoot.addProperty("parent", "minecraft:item/generated");
                JsonObject textures = new JsonObject();
                jsonRoot.add("textures", textures);

                textures.addProperty("layer0", "illicitblocks:item/bubble_column");
            }
            case CocoaBlock ignored -> jsonRoot.addProperty("parent", "minecraft:block/cocoa_stage2");
            case NetherPortalBlock ignored -> {
                jsonRoot.addProperty("parent", "minecraft:item/generated");
                JsonObject textures = new JsonObject();
                jsonRoot.add("textures", textures);

                textures.addProperty("layer0", "minecraft:block/nether_portal");
            }
            case AirBlock ignored -> {
                jsonRoot.addProperty("parent", "minecraft:item/generated");
                JsonObject textures = new JsonObject();
                jsonRoot.add("textures", textures);

                textures.addProperty("layer0", "illicitblocks:item/" + id.getPath());
            }
            case PistonExtensionBlock ignored -> {
                jsonRoot.addProperty("parent", "minecraft:item/generated");
                JsonObject textures = new JsonObject();
                jsonRoot.add("textures", textures);

                textures.addProperty("layer0", "minecraft:block/piston_top");
            }
            case EndPortalBlock ignored -> {
                jsonRoot.addProperty("parent", "minecraft:item/generated");
                JsonObject textures = new JsonObject();
                jsonRoot.add("textures", textures);

                textures.addProperty("layer0", "illicitblocks:item/end_portal");
            }
            case EndGatewayBlock ignored -> {
                jsonRoot.addProperty("parent", "minecraft:item/generated");
                JsonObject textures = new JsonObject();
                jsonRoot.add("textures", textures);

                textures.addProperty("layer0", "illicitblocks:item/end_gateway");
            }
            default -> {
                Utils.debugLog("Default back to block for {}, class: {}", id, block.getClass());
                jsonRoot.addProperty("parent", "block/" + id.getPath());
            }
        }

        return jsonRoot.toString();
    }
}
