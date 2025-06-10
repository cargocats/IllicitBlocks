package com.github.cargocats.illicitblocks.client.api;

import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface AdditionalModelRegistrationCallback {
    Event<AdditionalModelRegistrationCallback> EVENT = EventFactory.createArrayBacked(
            AdditionalModelRegistrationCallback.class,
            listeners -> context -> {
                for (var listener : listeners) {
                    listener.onModelRegistration(context);
                }
            });

    void onModelRegistration(Context context);

    interface Context {
        @Nullable
        UnbakedModel getModel(Identifier id);

        void addModel(Identifier id, UnbakedModel model);

        boolean hasModel(Identifier id);

        Stream<Pair<Identifier, UnbakedModel>> streamAssets();
    }

}