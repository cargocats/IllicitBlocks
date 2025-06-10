package com.github.cargocats.illicitblocks.client;

import java.util.ArrayList;
import java.util.stream.Stream;

import com.github.cargocats.illicitblocks.client.MyDefinitionDuck;
import com.google.common.base.Functions;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.item.ItemAsset;
import net.minecraft.registry.ContextSwappableRegistryLookup;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

public record AdditionalItemAssetRegistrationCallbackContextImpl(ArrayList<MyDefinitionDuck> definitions, ContextSwappableRegistryLookup contextSwappableRegistryLookup) implements AdditionalItemAssetRegistrationCallback.Context {
    @Override
    public Stream<Pair<Identifier, ItemAsset>> streamAssets() {
        return definitions.stream().map(it -> new Pair<>(
                it.modid$myDefinitionDuck$id(),
                it.modid$myDefinitionDuck$itemAsset()
        ));
    }

    @Override
    public @Nullable ItemAsset getAsset(Identifier id) {
        return definitions.stream()
                .filter(Functions.compose(id::equals, MyDefinitionDuck::modid$myDefinitionDuck$id)::apply)
                .findAny()
                .map(MyDefinitionDuck::modid$myDefinitionDuck$itemAsset)
                .orElse(null);
    }

    @Override
    public void addAsset(Identifier id, ItemAsset asset) {
        if (definitions.stream().map(MyDefinitionDuck::modid$myDefinitionDuck$id).anyMatch(id::equals)) {
            throw new UnsupportedOperationException("attempting to overwrite item asset");
        }

        definitions.add(MyDefinitionDuck.create(
                        id,
                        contextSwappableRegistryLookup.hasEntries()
                                ? asset.withContextSwapper(
                                contextSwappableRegistryLookup.createContextSwapper()
                        )
                                : asset
                )
        );
    }

    @Override
    public ContextSwappableRegistryLookup getLookup() {
        return contextSwappableRegistryLookup;
    }
}