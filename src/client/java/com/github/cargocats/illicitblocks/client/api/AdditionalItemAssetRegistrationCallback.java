package com.github.cargocats.illicitblocks.client.api;


import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.item.ItemAsset;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
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

        default void addAsset(Item item, ItemAsset asset) {
            addAsset(Objects.requireNonNull(item.getComponents().get(DataComponentTypes.ITEM_MODEL), "cannot add asset for item without model component"), asset);
        }

        default boolean hasAsset(Identifier id) {
            return getAsset(id) != null;
        }

        Stream<Pair<Identifier, ItemAsset>> streamAssets();
    }
}