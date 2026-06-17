package com.ferrett.backwardsmc;

import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;

public class FlyUp {
    @SubscribeEvent
    public void onEntityJoin(net.neoforged.neoforge.event.entity.EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof net.minecraft.world.entity.item.FallingBlockEntity falling)) return;

        var state = falling.getBlockState();

        // Only affect sand, gravel, anvils
        if (!(state.is(Blocks.SAND) || state.is(Blocks.GRAVEL) || state.is(Blocks.ANVIL))) {
            return;
        }

        // Reverse gravity
        falling.setNoGravity(true);

        // Give it upward velocity
        falling.setDeltaMovement(0, 0.35, 0);

        // Mark it so we know it's our custom rising block
        falling.getPersistentData().putBoolean("BackwardsMC_Rising", true);
    }

    @SubscribeEvent
    public void onEntityTick(net.neoforged.neoforge.event.tick.EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof net.minecraft.world.entity.item.FallingBlockEntity falling)) return;

        boolean rising = falling.getPersistentData()
                .getBoolean("BackwardsMC_Rising")
                .orElse(false);

        if (!rising) return;

        var level = falling.level();
        var posAbove = falling.blockPosition().above();

        // If it hits a solid block above, place itself
        if (!level.getBlockState(posAbove).isAir()) {

            // STOP all movement first
            falling.setDeltaMovement(0, 0, 0);
            falling.setNoGravity(true);

            // Move it slightly DOWN so it doesn't clip into the ceiling
            falling.setPos(falling.getX(), falling.getY() - 0.1, falling.getZ());

            // Now place the block safely
            level.setBlock(falling.blockPosition(), falling.getBlockState(), 3);

            // Remove the entity
            falling.discard();
        }
    }



}
