package com.ferrett.backwardsmc;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public class ToolDamageInvert {

    // Base sword damage by tier (rough, will tune after testing)
    private static final float WOOD_SWORD = 4.0f;
    private static final float STONE_SWORD = 5.0f;
    private static final float IRON_SWORD = 6.0f;
    private static final float DIAMOND_SWORD = 7.0f;
    private static final float NETHERITE_SWORD = 8.0f;
    private static final float GOLD_SWORD = 4.0f;

    // Base axe damage by tier (rough, will tune after testing)
    private static final float WOOD_AXE = 7.0f;
    private static final float STONE_AXE = 9.0f;
    private static final float IRON_AXE = 9.0f;
    private static final float DIAMOND_AXE = 9.0f;
    private static final float NETHERITE_AXE = 10.0f;
    private static final float GOLD_AXE = 7.0f;

    @SubscribeEvent
    public void onIncomingDamage(LivingIncomingDamageEvent event) {
        DamageSource source = event.getSource();
        Entity attacker = source.getEntity();

        if (!(attacker instanceof Player player)) return;

        ItemStack held = player.getMainHandItem();
        Item item = held.getItem();
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        String path = id.getPath(); // e.g. "wooden_sword", "netherite_axe"

        Float vanillaBase = null;
        Float invertedBase = null;
        boolean isAxe = path.endsWith("_axe");
        boolean isSword = path.endsWith("_sword");

        if (!isAxe && !isSword) return;

        if (isSword) {
            if (path.startsWith("wooden_")) { vanillaBase = WOOD_SWORD; invertedBase = 20.0f; }
            else if (path.startsWith("stone_")) { vanillaBase = STONE_SWORD; invertedBase = DIAMOND_SWORD; }
            else if (path.startsWith("iron_")) { vanillaBase = IRON_SWORD; invertedBase = IRON_SWORD; }
            else if (path.startsWith("golden_")) { vanillaBase = GOLD_SWORD; invertedBase = WOOD_SWORD; }
            else if (path.startsWith("diamond_")) { vanillaBase = DIAMOND_SWORD; invertedBase = STONE_SWORD; }
            else if (path.startsWith("netherite_")) { vanillaBase = NETHERITE_SWORD; invertedBase = WOOD_SWORD; }
        } else {
            if (path.startsWith("wooden_")) { vanillaBase = WOOD_AXE; invertedBase = NETHERITE_AXE; }
            else if (path.startsWith("stone_")) { vanillaBase = STONE_AXE; invertedBase = DIAMOND_AXE; }
            else if (path.startsWith("iron_")) { vanillaBase = IRON_AXE; invertedBase = IRON_AXE; }
            else if (path.startsWith("golden_")) { vanillaBase = GOLD_AXE; invertedBase = WOOD_AXE; }
            else if (path.startsWith("diamond_")) { vanillaBase = DIAMOND_AXE; invertedBase = STONE_AXE; }
            else if (path.startsWith("netherite_")) { vanillaBase = NETHERITE_AXE; invertedBase = WOOD_AXE; }
        }

        if (vanillaBase == null) return;

        float currentDamage = event.getAmount();
        if (currentDamage <= 0f) return;

        float ratio = invertedBase / vanillaBase;
        event.setAmount(currentDamage * ratio);
    }
}