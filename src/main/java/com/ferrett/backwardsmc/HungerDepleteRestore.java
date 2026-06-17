package com.ferrett.backwardsmc;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.minecraft.world.entity.player.Player;

public class HungerDepleteRestore {

    private static final int CHECK_INTERVAL_TICKS = 100; // every 5 seconds
    private static final double STILL_THRESHOLD = 0.01;

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        // Counteract vanilla's sprint exhaustion every tick, since there's
        // no getter to read/zero it - we just push it back down continuously.
        if (player.isSprinting()) {
            player.getFoodData().addExhaustion(-0.15f);
        }

        if (player.tickCount % CHECK_INTERVAL_TICKS != 0) return;

        double dx = player.getDeltaMovement().x;
        double dz = player.getDeltaMovement().z;
        double horizontalSpeed = Math.sqrt(dx * dx + dz * dz);

        boolean isResting = player.onGround() && horizontalSpeed < STILL_THRESHOLD && !player.isSprinting();
        boolean isSprinting = player.isSprinting();

        if (isResting) {
            int current = player.getFoodData().getFoodLevel();
            player.getFoodData().setFoodLevel(Math.max(0, current - 1));
        } else if (isSprinting) {
            int current = player.getFoodData().getFoodLevel();
            player.getFoodData().setFoodLevel(Math.min(20, current + 1));
        }
    }
}