package com.github.cargocats.illicitblocks.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

public class IllicitBlockItem extends BlockItem {
    public IllicitBlockItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    protected @Nullable BlockState getPlacementState(ItemPlacementContext context) {
        BlockState blockState = super.getPlacementState(context);
        if (blockState != null && blockState.contains(Properties.FACING)) {
            blockState = blockState.with(Properties.FACING, context.getPlayerLookDirection().getOpposite());
        }

        return blockState;
    }

    @Override
    protected boolean canPlace(ItemPlacementContext context, BlockState state) {
        PlayerEntity playerEntity = context.getPlayer();
        if (playerEntity != null) {
            return context.getWorld().canPlace(state, context.getBlockPos(), ShapeContext.of(playerEntity));
        }
        return true;
    }

    @Override
    public String getTranslationKey() {
        return Util.createTranslationKey("block", Registries.BLOCK.getId(this.getBlock()));
    }
}
