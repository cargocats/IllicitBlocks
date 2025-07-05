package com.github.cargocats.illicitblocks.client.mixin;

import com.github.cargocats.illicitblocks.client.api.AdditionalItemAssetRegistrationCallback;
import com.github.cargocats.illicitblocks.client.api.AdditionalItemAssetRegistrationCallbackContextImpl;
import com.github.cargocats.illicitblocks.client.api.MyDefinitionDuck;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.item.ItemAsset;
import net.minecraft.client.item.ItemAssetsLoader;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Mixin(ItemAssetsLoader.class)
public class ItemAssetsLoaderMixin {
    @ModifyExpressionValue(
            method = "method_65932",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/Util;combineSafe(Ljava/util/List;)Ljava/util/concurrent/CompletableFuture;"
            )
    )
    private static CompletableFuture<List<MyDefinitionDuck>> appendAdditional(CompletableFuture<List<MyDefinitionDuck>> original) {
        return original.thenApply(list -> {
            AdditionalItemAssetRegistrationCallbackContextImpl context = new AdditionalItemAssetRegistrationCallbackContextImpl(new ArrayList<>(list));
            AdditionalItemAssetRegistrationCallback.EVENT.invoker().onItemAssetRegistration(context);

            return List.copyOf(context.definitions());
        });
    }

    @Mixin(targets = "net.minecraft.client.item.ItemAssetsLoader$Definition")
    private static abstract class DefinitionMixin implements MyDefinitionDuck {
        @Shadow
        public abstract Identifier id();

        @Shadow
        @Nullable
        public abstract ItemAsset clientItemInfo();

        static {
            try {
                @SuppressWarnings("JavaLangInvokeHandleSignature")
                var ctorHandle = MethodHandles.lookup().findConstructor(
                        DefinitionMixin.class,
                        MethodType.methodType(
                                void.class, Identifier.class, ItemAsset.class
                        )
                ).asType(
                        MethodType.methodType(
                                MyDefinitionDuck.class, Identifier.class, ItemAsset.class
                        )
                );
                MyDefinitionDuck.CTOR.setValue((id, asset) -> {
                    try {
                        return (MyDefinitionDuck) ctorHandle.invokeExact(id, asset);
                    } catch (Throwable t) {
                        throw rethrow(t);
                    }
                });
            } catch (Throwable t) {
                throw rethrow(t);
            }
        }

        @Override
        public Identifier modid$myDefinitionDuck$id() {
            return this.id();
        }

        @Override
        public ItemAsset modid$myDefinitionDuck$itemAsset() {
            return this.clientItemInfo();
        }

        @Unique
        @SuppressWarnings("unchecked")
        private static <T extends Throwable> RuntimeException rethrow(Throwable t) throws T {
            throw (T) t;
        }
    }
}