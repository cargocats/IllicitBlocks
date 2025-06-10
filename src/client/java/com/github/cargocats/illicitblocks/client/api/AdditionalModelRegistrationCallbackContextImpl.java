package com.github.cargocats.illicitblocks.client.api;

import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.stream.Stream;

public record AdditionalModelRegistrationCallbackContextImpl(HashMap<Identifier, UnbakedModel> models) implements AdditionalModelRegistrationCallback.Context {

    @Override
    public @Nullable UnbakedModel getModel(Identifier id) {
        return models.get(id);
    }

    @Override
    public void addModel(Identifier id, UnbakedModel model) {
        if (models.containsKey(id)) {
            throw new UnsupportedOperationException("attempting to overwrite model");
        }
        models.put(id, model);
    }

    @Override
    public boolean hasModel(Identifier id) {
        return models.containsKey(id);
    }

    @Override
    public Stream<Pair<Identifier, UnbakedModel>> streamAssets() {
        return models
                .entrySet()
                .stream()
                .map(entry -> new Pair<>(
                        entry.getKey(),
                        entry.getValue()
                ));
    }
}