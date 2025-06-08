package com.github.cargocats.illicitblocks.client;

import com.github.cargocats.illicitblocks.IllicitBlocks;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.FabricBakedModelManager;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.SimpleUnbakedExtraModel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class IllicitModelPlugin implements ModelLoadingPlugin {
    @Override
    public void initialize(Context pluginContext) {
        IllicitBlocks.LOG.info("Registering dynamic piston_head model");

        Identifier modelId = Identifier.ofVanilla("block/piston_inventory");

        ExtraModelKey<BlockStateModel> key = ExtraModelKey.create(() -> Identifier.ofVanilla("item/piston_head").toString());

        pluginContext.addModel(key, SimpleUnbakedExtraModel.blockStateModel(modelId));

        IllicitBlocks.LOG.info("Registered piston_head model with model id {}", modelId);
    }
}