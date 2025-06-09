package com.github.cargocats.illicitblocks.client.mixin;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import com.github.cargocats.illicitblocks.Utils;
import com.github.cargocats.illicitblocks.client.AdditionalItemAssetRegistrationCallback;
import com.github.cargocats.illicitblocks.client.MyDefinitionDuck;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.item.ItemAsset;
import net.minecraft.client.item.ItemAssetsLoader;
import net.minecraft.registry.ContextSwappableRegistryLookup;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;

/*
    ItemAssetsLoaderMixin, AdditionalItemAssetRegistrationCallback, MyDefinitionDuck
    Thanks to TheWhyEvenHow
 */

@Mixin(ItemAssetsLoader.class)
public class ItemAssetsLoaderMixin {
    @ModifyExpressionValue(
            method = "method_65932",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/Util;combineSafe(Ljava/util/List;)Ljava/util/concurrent/CompletableFuture;"
            )
    )
    private static CompletableFuture<List<MyDefinitionDuck>> appendAdditional(CompletableFuture<List<MyDefinitionDuck>> original, @Local(argsOnly = true) DynamicRegistryManager.Immutable immutable) {
        return original.thenApply(list -> {
            ContextSwappableRegistryLookup contextSwappableRegistryLookup = new ContextSwappableRegistryLookup(immutable);
            ArrayList<MyDefinitionDuck> definitions = new ArrayList<>(list);
            var context = new AdditionalItemAssetRegistrationCallback.Context() {

                @Override
                public Stream<MyDefinitionDuck> streamAssets() {
                    return definitions.stream();
                }

                @Override
                public @Nullable ItemAsset getAsset(Identifier id) {
                    return definitions.stream()
                            .filter(it -> it.modid$myDefinitionDuck$id() == id)
                            .findAny()
                            .map(MyDefinitionDuck::modid$myDefinitionDuck$itemAsset)
                            .orElse(null);
                }

                @Override
                public void addAsset(Identifier id, ItemAsset asset) {
                    if (definitions.stream().map(MyDefinitionDuck::modid$myDefinitionDuck$id).anyMatch(id::equals)) {
                        throw new IllegalStateException("Attempting to add duplicate ItemAsset definition");
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
            };
            AdditionalItemAssetRegistrationCallback.EVENT.invoker().onItemAssetRegistration(context);
            return List.copyOf(definitions);
        });
    }

    @Mixin(targets = "net.minecraft.client.item.ItemAssetsLoader$Definition")
    private static abstract class DefinitionMixin implements MyDefinitionDuck {
        @Shadow
        public abstract Identifier id();

        @Shadow
        public abstract @Nullable ItemAsset clientItemInfo();

        static {
            @SuppressWarnings("JavaLangInvokeHandleSignature")
            var ctorHandle = Utils.rethrowing(
                    () -> MethodHandles.lookup().findConstructor(
                            DefinitionMixin.class,
                            MethodType.methodType(
                                    void.class,
                                    Identifier.class,
                                    ItemAsset.class
                            )
                    )
            ).asType(
                    MethodType.methodType(
                            MyDefinitionDuck.class,
                            Identifier.class,
                            ItemAsset.class
                    )
            );
            MyDefinitionDuck.CTOR.setValue((id, asset) -> Utils.rethrowing(() -> (MyDefinitionDuck) ctorHandle.invokeExact(id, asset)));
        }

        @Override
        public Identifier modid$myDefinitionDuck$id() {
            return this.id();
        }

        @Override
        public ItemAsset modid$myDefinitionDuck$itemAsset() {
            return this.clientItemInfo();
        }
    }
}