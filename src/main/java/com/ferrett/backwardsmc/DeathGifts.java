package com.ferrett.backwardsmc;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;

public class DeathGifts {

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (BackwardsMinecraft.ferretdeath) return;
        // Cancel the death
        event.setCanceled(true);

        // Restore some health so they don't instantly die again
        player.setHealth(20f); // 2 hearts
        double random = Math.random() * 6 - 3; // gives a double in range [-1, 1)
        double random1 = Math.random() * 6 - 3; // gives a double in range [-1, 1)
        // Drop gifts at the player's feet
        for (int i = 0; i < 3; i++) {
            ItemEntity drop = new ItemEntity(
                    player.level(),
                    player.getX() + random,
                    player.getY() + 3,
                    player.getZ() + random1,
                    new net.minecraft.world.item.ItemStack(Items.DIAMOND)
            );
            player.level().addFreshEntity(drop);
        }

        // Optional: knock them back a bit for dramatic effect
        player.setDeltaMovement(0, 0.5, 0);
    }
}
