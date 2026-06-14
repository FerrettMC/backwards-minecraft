package com.example.examplemod;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ExampleMod.MOD_ID)
public class ExampleMod {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "examplemod";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "examplemod" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    // Create a Deferred Register to hold Items which will all be registered under the "examplemod" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "examplemod" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);



    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public ExampleMod(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ExampleMod) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code

    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {

    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts

    }

    // REMOVE BEFORE UPLOADING FINAL MOD!!!!!!
    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        net.minecraft.server.MinecraftServer server = player.level().getServer();

        final int[] ticksLeft = {15};
        final java.util.function.Consumer<net.neoforged.neoforge.event.tick.ServerTickEvent.Post>[] killListenerRef = new java.util.function.Consumer[1];

        killListenerRef[0] = (net.neoforged.neoforge.event.tick.ServerTickEvent.Post tickEvent) -> {
            if (--ticksLeft[0] > 0) return;
            NeoForge.EVENT_BUS.unregister(killListenerRef[0]);

            // Save everything BEFORE kill
            boolean wasFlying = player.getAbilities().flying;
            boolean canFly = player.getAbilities().mayfly;
            float health = player.getHealth();
            int food = player.getFoodData().getFoodLevel();
            float saturation = player.getFoodData().getSaturationLevel();
            int xpLevel = player.experienceLevel;
            float xpProgress = player.experienceProgress;
            int totalXp = player.totalExperience;
            double x = player.getX();
            double y = player.getY();
            double z = player.getZ();
            float yRot = player.getYRot();
            float xRot = player.getXRot();
            net.minecraft.server.level.ServerLevel level = (net.minecraft.server.level.ServerLevel) player.level();

            net.minecraft.world.item.ItemStack[] inventory = new net.minecraft.world.item.ItemStack[player.getInventory().getContainerSize()];
            for (int i = 0; i < inventory.length; i++) {
                inventory[i] = player.getInventory().getItem(i).copy();
            }

            var effects = new java.util.ArrayList<>(player.getActiveEffects());

            // Clear inventory BEFORE kill so nothing drops
            player.getInventory().clearContent();
            player.hurt(player.damageSources().genericKill(), Float.MAX_VALUE);

            // Listen for when player clicks respawn button
            final java.util.function.Consumer<net.neoforged.neoforge.event.entity.player.PlayerEvent.Clone>[] respawnEventRef = new java.util.function.Consumer[1];

            respawnEventRef[0] = (net.neoforged.neoforge.event.entity.player.PlayerEvent.Clone respawnEvent) -> {
                if (!respawnEvent.isWasDeath()) return;
                NeoForge.EVENT_BUS.unregister(respawnEventRef[0]);

                ServerPlayer r = (ServerPlayer) respawnEvent.getEntity();

                // Wait a few ticks after respawn before restoring
                final int[] restoreTicks = {5};
                final java.util.function.Consumer<net.neoforged.neoforge.event.tick.ServerTickEvent.Post>[] restoreListenerRef = new java.util.function.Consumer[1];

                restoreListenerRef[0] = (net.neoforged.neoforge.event.tick.ServerTickEvent.Post restoreEvent) -> {
                    if (--restoreTicks[0] > 0) return;
                    NeoForge.EVENT_BUS.unregister(restoreListenerRef[0]);

                    r.setHealth(health);
                    r.getFoodData().setFoodLevel(food);
                    r.getFoodData().setSaturation(saturation);
                    r.setExperienceLevels(xpLevel);
                    r.setExperiencePoints((int)(xpProgress * r.getXpNeededForNextLevel()));
                    r.totalExperience = totalXp;
                    r.getAbilities().flying = wasFlying;
                    r.getAbilities().mayfly = canFly;
                    r.onUpdateAbilities();

                    r.getInventory().clearContent();
                    for (int i = 0; i < inventory.length; i++) {
                        r.getInventory().setItem(i, inventory[i]);
                    }

                    r.removeAllEffects();
                    for (var effect : effects) {
                        r.addEffect(effect);
                    }

                    r.teleportTo(level, x, y, z, java.util.Set.of(), yRot, xRot, true);
                    r.inventoryMenu.broadcastChanges();
                };
                NeoForge.EVENT_BUS.addListener(restoreListenerRef[0]);
            };
            NeoForge.EVENT_BUS.addListener(respawnEventRef[0]);
        };
        NeoForge.EVENT_BUS.addListener(killListenerRef[0]);
    }

}
