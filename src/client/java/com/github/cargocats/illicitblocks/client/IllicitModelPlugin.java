package com.github.cargocats.illicitblocks.client;

import com.github.cargocats.illicitblocks.IllicitBlocks;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;

public class IllicitModelPlugin implements ModelLoadingPlugin {
    @Override
    public void initialize(Context pluginContext) {
        IllicitBlocks.LOG.info("Registering dynamic piston_head model");
        IllicitBlocks.LOG.info("Registered piston_head model with model id {}", "placeholder");
    }
}