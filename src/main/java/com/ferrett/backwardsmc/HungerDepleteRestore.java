package com.ferrett.backwardsmc;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.minecraft.world.entity.player.Player;

public class HungerDepleteRestore {

    // Resting drains slowly: check every 10 seconds, lose 1 point each time
    private static final int REST_INTERVAL_TICKS = 200; // 10 seconds

    // Sprinting restores quickly: check every 1 second, gain 1 point each time
    private static final int SPRINT_INTERVAL_TICKS = 20; // 1 second

    private static final double STILL_THRESHOLD = 0.01;

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        boolean isSprinting = player.isSprinting();

        // Counteract vanilla's sprint exhaustion every tick
        if (isSprinting) {
            player.getFoodData().addExhaustion(-0.15f);
        }

        double dx = player.getDeltaMovement().x;
        double dz = player.getDeltaMovement().z;
        double horizontalSpeed = Math.sqrt(dx * dx + dz * dz);
        boolean isResting = player.onGround() && horizontalSpeed < STILL_THRESHOLD && !isSprinting;

        if (isResting && player.tickCount % REST_INTERVAL_TICKS == 0) {
            int current = player.getFoodData().getFoodLevel();
            player.getFoodData().setFoodLevel(Math.max(0, current - 1));
        }

        if (isSprinting && player.tickCount % SPRINT_INTERVAL_TICKS == 0) {
            int current = player.getFoodData().getFoodLevel();
            player.getFoodData().setFoodLevel(Math.min(20, current + 1));
        }
    }
}