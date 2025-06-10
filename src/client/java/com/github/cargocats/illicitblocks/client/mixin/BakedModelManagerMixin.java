package com.github.cargocats.illicitblocks.client.mixin;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.github.cargocats.illicitblocks.client.api.AdditionalModelRegistrationCallback;
import com.github.cargocats.illicitblocks.client.api.AdditionalModelRegistrationCallbackContextImpl;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.util.Identifier;

@Debug(export = true)
@Mixin(BakedModelManager.class)
public class BakedModelManagerMixin {
    @ModifyExpressionValue(
            method = "method_45899",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/concurrent/CompletableFuture;thenApply(Ljava/util/function/Function;)Ljava/util/concurrent/CompletableFuture;"
            )
    )
    private static CompletableFuture<Map<Identifier, UnbakedModel>> appendAdditionalModels(CompletableFuture<Map<Identifier, UnbakedModel>> original) {
        return original.thenApply(models -> {
            AdditionalModelRegistrationCallbackContextImpl context = new AdditionalModelRegistrationCallbackContextImpl(new HashMap<>(models));
            AdditionalModelRegistrationCallback.EVENT.invoker().onModelRegistration(context);
            return Map.copyOf(context.models());
        });
    }
}