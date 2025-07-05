package com.github.cargocats.illicitblocks.client.api;

import net.minecraft.client.item.ItemAsset;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.Objects;
import java.util.function.BiFunction;

public interface MyDefinitionDuck {
    MutableObject<BiFunction<Identifier, ItemModel.Unbaked, MyDefinitionDuck>> CTOR = new MutableObject<>();

    Identifier modid$myDefinitionDuck$id();

    ItemAsset modid$myDefinitionDuck$itemAsset();

    static MyDefinitionDuck create(Identifier id, ItemAsset asset) {
        return Objects.requireNonNull(CTOR.getValue(), "not init").apply(id, asset.model());
    }
}