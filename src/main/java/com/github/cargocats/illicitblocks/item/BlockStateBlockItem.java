package com.github.cargocats.illicitblocks.item;

import com.github.cargocats.illicitblocks.IllicitBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockStateBlockItem extends BlockItem {
    private final BlockState blockState;

    public BlockStateBlockItem(Block block, Settings settings, BlockState blockState) {
        super(block, settings);
        this.blockState = blockState;
    }

    public BlockStateBlockItem(Block block, Settings settings) {
        super(block, settings);
        this.blockState = null;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        BlockPos blockPos = context.getBlockPos().offset(context.getSide());
        PlayerEntity player = context.getPlayer();
        World world = context.getWorld();

        BlockState blockState = this.blockState != null
                ? this.blockState
                : getBlock().getDefaultState();

        if (blockState.contains(Properties.FACING) && player != null) {
            blockState = blockState.with(Properties.FACING, player.getHorizontalFacing().getOpposite());
        }

        if (!world.isClient) {
            BlockSoundGroup soundGroup = blockState.getSoundGroup();

            world.playSound(
                    null,
                    blockPos,
                    soundGroup.getPlaceSound(),
                    SoundCategory.BLOCKS,
                    soundGroup.getVolume(),
                    soundGroup.getPitch()
            );

            world.setBlockState(blockPos, blockState, Block.NOTIFY_ALL);
            context.getStack().decrementUnlessCreative(1, player);

            for (BlockState state : getBlock().getStateManager().getStates()) {
                IllicitBlocks.LOG.info("{}", state);
            }

            IllicitBlocks.LOG.info("Current block state: {}", blockState);
        }

        return ActionResult.SUCCESS;
    }
}
