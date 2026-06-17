package com.ferrett.backwardsmc;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public class ToolInvert {

    private static final float WOOD_SPEED = 2.0f;
    private static final float STONE_SPEED = 4.0f;
    private static final float IRON_SPEED = 6.0f;
    private static final float DIAMOND_SPEED = 8.0f;
    private static final float NETHERITE_SPEED = 9.0f;
    private static final float GOLD_SPEED = 12.0f;

    @SubscribeEvent
    public void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        ItemStack held = player.getMainHandItem();

        Item item = held.getItem();
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        String path = id.getPath(); // e.g. "wooden_pickaxe", "netherite_axe"

        Float vanillaBase = null;
        Float invertedBase = null;

        if (path.startsWith("wooden_")) {
            vanillaBase = WOOD_SPEED;
            invertedBase = NETHERITE_SPEED;
        } else if (path.startsWith("stone_")) {
            vanillaBase = STONE_SPEED;
            invertedBase = DIAMOND_SPEED;
        } else if (path.startsWith("iron_")) {
            vanillaBase = IRON_SPEED;
            invertedBase = IRON_SPEED;
        } else if (path.startsWith("golden_")) {
            vanillaBase = GOLD_SPEED;
            invertedBase = WOOD_SPEED;
        } else if (path.startsWith("diamond_")) {
            vanillaBase = DIAMOND_SPEED;
            invertedBase = STONE_SPEED;
        } else if (path.startsWith("netherite_")) {
            vanillaBase = NETHERITE_SPEED;
            invertedBase = WOOD_SPEED;
        }

        if (vanillaBase == null) return;

        float currentSpeed = event.getNewSpeed();
        if (currentSpeed <= 0f) return;

        float ratio = invertedBase / vanillaBase;
        event.setNewSpeed(currentSpeed * ratio);
    }
}