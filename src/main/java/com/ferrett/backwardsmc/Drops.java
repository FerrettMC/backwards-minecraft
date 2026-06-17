package com.ferrett.backwardsmc;

import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;

public class Drops {

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        BlockState state = event.getState();
        BlockPos pos = event.getPos();

        // Remove vanilla drops



        // Logs → cobblestone
        if (state.is(Blocks.OAK_LOG) ||
                state.is(Blocks.SPRUCE_LOG) ||
                state.is(Blocks.BIRCH_LOG) ||
                state.is(Blocks.JUNGLE_LOG) ||
                state.is(Blocks.ACACIA_LOG) ||
                state.is(Blocks.DARK_OAK_LOG) ||
                state.is(Blocks.MANGROVE_LOG) ||
                state.is(Blocks.CHERRY_LOG) ||
                state.is(Blocks.PALE_OAK_LOG)) {
            event.setCanceled(true);
            event.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            event.getLevel().addFreshEntity(
                    new net.minecraft.world.entity.item.ItemEntity(
                            (Level) event.getLevel(),
                            pos.getX() + 0.5,
                            pos.getY() + 0.5,
                            pos.getZ() + 0.5,
                            new ItemStack(Blocks.COBBLESTONE)
                    )
            );
            return;
        }

        // Stone → oak log
        if (state.is(Blocks.STONE)) {
            event.setCanceled(true);
            event.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            event.getLevel().addFreshEntity(
                    new net.minecraft.world.entity.item.ItemEntity(
                            (Level) event.getLevel(),
                            pos.getX() + 0.5,
                            pos.getY() + 0.5,
                            pos.getZ() + 0.5,
                            new ItemStack(Blocks.OAK_LOG)
                    )
            );
        }
    }
}
