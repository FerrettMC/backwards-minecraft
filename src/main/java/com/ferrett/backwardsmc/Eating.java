package com.ferrett.backwardsmc;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class Eating {

    // ---------------------------
    // 1. ORES: Right-click event
    // ---------------------------
    @SubscribeEvent
    public void onOreEat(PlayerInteractEvent.RightClickItem event) {
        ItemStack stack = event.getItemStack();
        Player player = event.getEntity();
        if (stack.is(Items.IRON_INGOT) ||
                stack.is(Items.GOLD_INGOT) ||
                stack.is(Items.COPPER_INGOT) ||
                stack.is(Items.COAL) ||
                stack.is(Items.REDSTONE) ||
                stack.is(Items.LAPIS_LAZULI) ||
                stack.is(Items.DIAMOND) ||
                stack.is(Items.EMERALD)) {
            event.setCanceled(true);
            stack.shrink(1);
            player.getFoodData().eat(4, 0.2f);
        }
    }

    // -----------------------------------------------------
    // 2. FOOD: Intercept BEFORE vanilla applies nutrition
    // -----------------------------------------------------
    @SubscribeEvent
    public void onFoodStart(LivingEntityUseItemEvent.Start event) {
        if (!(event.getEntity() instanceof Player player)) return;
        ItemStack stack = event.getItem();

        if (isReverseFood(stack)) {
            int duration = event.getDuration();

            // Let the eating animation play out normally,
            // but we will consume the item & apply OUR hunger
            // change ourselves once the animation completes.
            // We don't cancel here - we handle it in Stop/Tick instead.
        }
    }

    // Use Stop instead of Finish - fires once duration naturally completes,
    // BEFORE vanilla's onUseTick/finishUsingItem nutrition logic applies.
    // NOTE: Stop also fires if the player releases early, so guard with a tick check.
    @SubscribeEvent
    public void onFoodEat(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player player)) return;
        ItemStack stack = event.getItem();

        if (isReverseFood(stack)) {
            FoodProperties foodProps = stack.get(DataComponents.FOOD);
            int nutrition = foodProps != null ? foodProps.nutrition() : 0;

            int vanillaFoodLevel = player.getFoodData().getFoodLevel();
            int corrected = vanillaFoodLevel - nutrition - 4;
            player.getFoodData().setFoodLevel(Math.max(0, corrected));
        }
    }

    private boolean isReverseFood(ItemStack stack) {
        return stack.is(Items.APPLE) ||
                stack.is(Items.GOLDEN_APPLE) ||
                stack.is(Items.ENCHANTED_GOLDEN_APPLE) ||
                stack.is(Items.MELON_SLICE) ||
                stack.is(Items.SWEET_BERRIES) ||
                stack.is(Items.GLOW_BERRIES) ||
                stack.is(Items.CHORUS_FRUIT) ||
                stack.is(Items.CARROT) ||
                stack.is(Items.GOLDEN_CARROT) ||
                stack.is(Items.POTATO) ||
                stack.is(Items.BAKED_POTATO) ||
                stack.is(Items.POISONOUS_POTATO) ||
                stack.is(Items.BEETROOT) ||
                stack.is(Items.BEETROOT_SOUP) ||
                stack.is(Items.MUSHROOM_STEW) ||
                stack.is(Items.RABBIT_STEW) ||
                stack.is(Items.SUSPICIOUS_STEW) ||
                stack.is(Items.BREAD) ||
                stack.is(Items.COOKIE) ||
                stack.is(Items.CAKE) ||
                stack.is(Items.PUMPKIN_PIE) ||
                stack.is(Items.HONEY_BOTTLE) ||
                stack.is(Items.DRIED_KELP) ||
                stack.is(Items.ROTTEN_FLESH) ||
                stack.is(Items.SPIDER_EYE) ||
                stack.is(Items.COOKED_BEEF) ||
                stack.is(Items.BEEF) ||
                stack.is(Items.COOKED_PORKCHOP) ||
                stack.is(Items.PORKCHOP) ||
                stack.is(Items.COOKED_CHICKEN) ||
                stack.is(Items.CHICKEN) ||
                stack.is(Items.COOKED_MUTTON) ||
                stack.is(Items.MUTTON) ||
                stack.is(Items.COOKED_RABBIT) ||
                stack.is(Items.RABBIT) ||
                stack.is(Items.COOKED_COD) ||
                stack.is(Items.COD) ||
                stack.is(Items.COOKED_SALMON) ||
                stack.is(Items.SALMON) ||
                stack.is(Items.TROPICAL_FISH) ||
                stack.is(Items.PUFFERFISH);
    }
}