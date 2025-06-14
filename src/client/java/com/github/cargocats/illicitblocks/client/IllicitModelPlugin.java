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
import net.minecraft.block.WallBannerBlock;
import net.minecraft.block.WallHangingSignBlock;
import net.minecraft.block.WallSignBlock;
import net.minecraft.block.WallSkullBlock;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.List;

import static com.github.cargocats.illicitblocks.IllicitBlocks.createdBlockItems;
import static com.github.cargocats.illicitblocks.client.IllicitBlocksClient.extractWoodType;

public class IllicitModelPlugin implements ModelLoadingPlugin {
    public static final HashMap<Identifier, Identifier> blockIdToItemId = new HashMap<>();
    public static final HashMap<Identifier, Identifier> itemIdToBlockId = new HashMap<>();
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

        IllicitBlocks.LOG.info("Loaded Illicit Blocks model plugin");
    }

    public static String getModelForId(Identifier id, ModelResolver.Context context) {
        Block block = Registries.BLOCK.get(id);

        JsonObject jsonRoot = new JsonObject();
        jsonRoot.addProperty("gui_light", "front");

        if (block instanceof FluidBlock) {
            jsonRoot.addProperty("parent", "minecraft:item/generated");

            JsonObject textures = new JsonObject();
            jsonRoot.add("textures", textures);

            textures.addProperty("layer0", id.getNamespace() + ":block/" + id.getPath() + "_still");
        } else if (block instanceof CropBlock) {
            jsonRoot.addProperty("parent", "block/" + id.getPath() + "_stage0");
        } else if (block instanceof WallSignBlock wallSignBlock) {
            jsonRoot.addProperty("parent", "minecraft:item/generated");

            JsonObject textures = new JsonObject();
            jsonRoot.add("textures", textures);

            String woodName = wallSignBlock.getWoodType().name().replaceFirst(id.getNamespace() + ":", "");
            if (annoyingMods.contains(id.getNamespace())) {
                woodName = extractWoodType(id.toString());
            }

            String itemTextureString = id.getNamespace() + ":item/" + woodName + "_sign";
            textures.addProperty("layer0", itemTextureString);
        } else if (block instanceof WallHangingSignBlock wallHangingSignBlock) {
            jsonRoot.addProperty("parent", "minecraft:item/generated");

            JsonObject textures = new JsonObject();
            jsonRoot.add("textures", textures);

            String woodName = wallHangingSignBlock.getWoodType().name().replaceFirst(id.getNamespace() + ":", "");
            if (annoyingMods.contains(id.getNamespace())) {
                woodName = extractWoodType(id.toString());
            }

            textures.addProperty("layer0", id.getNamespace() + ":item/" + woodName + "_hanging_sign");
        } else if (block instanceof FlowerPotBlock && !id.getNamespace().equals("minecraft")) {
            jsonRoot.addProperty("parent", "minecraft:block/flower_pot_cross");

            String texture = id.getPath().replace("potted_", "");

            JsonObject textures = new JsonObject();
            jsonRoot.add("textures", textures);

            // TODO: Reimplement the double plant potted plant thing.

            textures.addProperty("plant", id.getNamespace() + ":block/" + texture);
        } else if (block instanceof TallPlantBlock) {
            jsonRoot.addProperty("parent", "minecraft:item/generated");

            JsonObject textures = new JsonObject();
            jsonRoot.add("textures", textures);

            textures.addProperty("layer0", id.getNamespace() + ":block/" + id.getPath() + "_bottom");
        } else if (block instanceof AbstractFireBlock) {
            jsonRoot.addProperty("parent", "minecraft:item/generated");

            JsonObject textures = new JsonObject();
            jsonRoot.add("textures", textures);

            textures.addProperty("layer0", id.getNamespace() + ":block/" + id.getPath() + "_0");
        } else if (block instanceof StemBlock) {
            jsonRoot.addProperty("parent", "minecraft:item/generated");

            JsonObject textures = new JsonObject();
            jsonRoot.add("textures", textures);

            textures.addProperty("layer0", id.getNamespace() + ":block/" + id.getPath());
        } else if (block instanceof LeveledCauldronBlock) {
            jsonRoot.addProperty("parent", id.getNamespace() + ":block/" + id.getPath() + "_full");
        } else if (block instanceof SweetBerryBushBlock) {
            jsonRoot.addProperty("parent", "minecraft:block/sweet_berry_bush_stage0");
        } else if (block instanceof FrostedIceBlock) {
            jsonRoot.addProperty("parent", "minecraft:block/frosted_ice_3");
        } else if (block instanceof TripwireBlock) {
            jsonRoot.addProperty("parent", "minecraft:block/tripwire_attached_nsew");
        } else if (block instanceof RedstoneWireBlock) {
            jsonRoot.addProperty("parent", "minecraft:item/generated");

            JsonObject textures = new JsonObject();
            jsonRoot.add("textures", textures);

            textures.addProperty("layer0", "minecraft:block/redstone_dust_dot");
        } else if (block instanceof BubbleColumnBlock) {
            jsonRoot.addProperty("parent", "minecraft:item/generated");

            JsonObject textures = new JsonObject();
            jsonRoot.add("textures", textures);

            textures.addProperty("layer0", "illicitblocks:item/bubble_column");
        } else if (block instanceof CocoaBlock) {
            jsonRoot.addProperty("parent", "minecraft:block/cocoa_stage2");
        } else if (block instanceof NetherPortalBlock) {
            jsonRoot.addProperty("parent", "minecraft:item/generated");

            JsonObject textures = new JsonObject();
            jsonRoot.add("textures", textures);

            textures.addProperty("layer0", "minecraft:block/nether_portal");
        } else if (block instanceof AirBlock) {
            jsonRoot.addProperty("parent", "minecraft:item/generated");

            JsonObject textures = new JsonObject();
            jsonRoot.add("textures", textures);

            textures.addProperty("layer0", "illicitblocks:item/" + id.getPath());
        } else if (block instanceof PistonExtensionBlock) {
            jsonRoot.addProperty("parent", "minecraft:item/generated");

            JsonObject textures = new JsonObject();
            jsonRoot.add("textures", textures);

            textures.addProperty("layer0", "minecraft:block/piston_top");
        } else if (block instanceof EndPortalBlock) {
            jsonRoot.addProperty("parent", "minecraft:item/generated");

            JsonObject textures = new JsonObject();
            jsonRoot.add("textures", textures);

            textures.addProperty("layer0", "illicitblocks:item/end_portal");
        } else if (block instanceof EndGatewayBlock) {
            jsonRoot.addProperty("parent", "minecraft:item/generated");

            JsonObject textures = new JsonObject();
            jsonRoot.add("textures", textures);

            textures.addProperty("layer0", "illicitblocks:item/end_gateway");
        } else if (block instanceof WallBannerBlock) {
            jsonRoot.addProperty("parent", "minecraft:item/template_banner");
        } else if (block instanceof WallSkullBlock) {
            jsonRoot.addProperty("parent", "minecraft:item/template_skull");
        } else {
            Utils.debugLog("Default back to block for {}, class: {}", id, block.getClass());
            jsonRoot.addProperty("parent", "block/" + id.getPath());
        }

        return jsonRoot.toString();
    }
}
