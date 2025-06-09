package com.github.cargocats.illicitblocks.client;


import net.minecraft.registry.ContextSwappableRegistryLookup;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.item.ItemAsset;
import net.minecraft.registry.ContextSwapper;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

import java.util.stream.Stream;

public interface AdditionalItemAssetRegistrationCallback {
    Event<AdditionalItemAssetRegistrationCallback> EVENT = EventFactory.createArrayBacked(
            AdditionalItemAssetRegistrationCallback.class,
            listeners -> context -> {
                for (var listener : listeners) {
                    listener.onItemAssetRegistration(context);
                }
            }
    );

    void onItemAssetRegistration(Context context);

    interface Context {
        @Nullable
        ItemAsset getAsset(Identifier id);

        void addAsset(Identifier id, ItemAsset asset);

        default boolean hasAsset(Identifier id) {
            return getAsset(id) != null;
        }

        ContextSwappableRegistryLookup getLookup();

        default ContextSwapper createContextSwapper() {
            return getLookup().createContextSwapper();
        }

        Stream<MyDefinitionDuck> streamAssets();;
    }
}