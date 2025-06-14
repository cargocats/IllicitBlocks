package com.github.cargocats.illicitblocks.mixin;

import com.github.cargocats.illicitblocks.IllicitBlocks;
import com.github.cargocats.illicitblocks.item.IllicitBlockItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Debug(export = true)
@Mixin(value = Registries.class, priority = 1)
public class RegistriesMixin {
    @Inject(method = "freezeRegistries", at = @At("HEAD"))
    private static void beforeFreeze(CallbackInfo ci) {
        ArrayList<IllicitBlockItem> blockItems = IllicitBlocks.registerBlockItems();

        ItemGroupEvents.modifyEntriesEvent(IllicitBlocks.ILLICIT_BLOCKS_ITEM_GROUP_KEY)
                .register(group -> blockItems.forEach(blockItem -> {
                    group.add(blockItem);
                    IllicitBlocks.generateItemStacksFromBlockItem(blockItem).forEach(group::add);
                }));

        IllicitBlocks.createdBlockItems.addAll(blockItems.stream().map(Registries.ITEM::getId).toList());

        IllicitBlocks.LOG.info("Finished registering IllicitBlocks items");
    }
}
