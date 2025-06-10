package com.github.cargocats.illicitblocks.client.api;

import java.util.Objects;
import java.util.function.BiFunction;

import org.apache.commons.lang3.mutable.MutableObject;

import net.minecraft.client.item.ItemAsset;
import net.minecraft.util.Identifier;

public interface MyDefinitionDuck {
    MutableObject<BiFunction<Identifier, ItemAsset, MyDefinitionDuck>> CTOR = new MutableObject<>();

    Identifier modid$myDefinitionDuck$id();

    ItemAsset modid$myDefinitionDuck$itemAsset();

    static MyDefinitionDuck create(Identifier id, ItemAsset asset) {
        return Objects.requireNonNull(CTOR.getValue(), "not init").apply(id, asset);
    }
}