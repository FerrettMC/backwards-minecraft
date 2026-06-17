package com.ferrett.backwardsmc;

import com.ferrett.backwardsmc.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(BackwardsMinecraft.MOD_ID)
public class BackwardsMinecraft {
    public static boolean ferretdeath = false;
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "backwardsminecraft";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // Name of the file (stored in the world save folder) that records the
    // shared End spawnpoint that every first-time joiner gets sent to.
    private static final String END_SPAWN_FILE_NAME = "backwardsminecraft_endspawn.dat";




    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public BackwardsMinecraft(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading

        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);
        modEventBus.register(ModAttributes.class);
        NeoForge.EVENT_BUS.register(new DirtPortal());
        NeoForge.EVENT_BUS.register(new FlyUp());
        NeoForge.EVENT_BUS.register(new NightDaySwitch());
        NeoForge.EVENT_BUS.register(new ZombieTradeVillagerAttack());
        NeoForge.EVENT_BUS.register(new SkeletonSpider());
        NeoForge.EVENT_BUS.register(new Drops());
        NeoForge.EVENT_BUS.register(new Eating());
        NeoForge.EVENT_BUS.register(new ToolInvert());
        NeoForge.EVENT_BUS.register(new DeathGifts());
        NeoForge.EVENT_BUS.register(new HungerDepleteRestore());
        NeoForge.EVENT_BUS.register(new ToolDamageInvert());

        ModEntities.ENTITIES.register(modEventBus);

        modEventBus.register(ModRenderers.class);





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

    // ---------------------------------------------------------------------
    // Shared End spawnpoint helpers
    // ---------------------------------------------------------------------

    /**
     * Returns the path to the file (inside the world save folder) that stores
     * the shared End spawnpoint coordinates.
     */
    private static Path getEndSpawnFile(MinecraftServer server) {
        return server.getWorldPath(LevelResource.ROOT).resolve(END_SPAWN_FILE_NAME);
    }

    /**
     * Reads the previously saved shared End spawnpoint, or returns null if
     * none has been set yet (i.e. no player has spawned in the End yet).
     */
    private static BlockPos loadEndSpawn(MinecraftServer server) {
        try {
            Path path = getEndSpawnFile(server);
            if (!Files.exists(path)) {
                return null;
            }
            String content = Files.readString(path, StandardCharsets.UTF_8).trim();
            if (content.isEmpty()) {
                return null;
            }
            String[] parts = content.split(",");
            if (parts.length != 3) {
                return null;
            }
            int x = Integer.parseInt(parts[0].trim());
            int y = Integer.parseInt(parts[1].trim());
            int z = Integer.parseInt(parts[2].trim());
            return new BlockPos(x, y, z);
        } catch (Exception e) {
            LOGGER.error("Failed to read shared End spawn file", e);
            return null;
        }
    }

    /**
     * Saves the shared End spawnpoint so that future first-time joiners spawn
     * at the same location.
     */
    private static void saveEndSpawn(MinecraftServer server, BlockPos pos) {
        try {
            Path path = getEndSpawnFile(server);
            Files.writeString(path, pos.getX() + "," + pos.getY() + "," + pos.getZ(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOGGER.error("Failed to write shared End spawn file", e);
        }
    }




    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        int[] ticksLeft = {60};
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Book.giveIfNeeded(player);

        // Delay everything by 1 tick so the world + player are fully initialized
        player.level().getServer().execute(() -> {

            ServerLevel overworld = player.level().getServer().overworld();
            BackwardsWorldData data = BackwardsWorldData.get(overworld);

            ServerLevel end = overworld.getServer().getLevel(Level.END);
            if (end == null) return;

            // FIRST PLAYER EVER
            if (!data.hasEndSpawn) {

                BlockPos spawn = findRandomEndStoneLocation(end, player);

                data.endSpawn = spawn;
                data.hasEndSpawn = true;
                data.setDirty(); // now safe

                // Delay teleport by 1 more tick to avoid race conditions
                player.level().getServer().execute(() -> {
                    player.teleportTo(
                            end,
                            spawn.getX() + 0.5,
                            spawn.getY(),
                            spawn.getZ() + 0.5,
                            Set.of(),
                            0f, 0f,
                            false
                    );
                });
                int treeCount = 2 + player.getRandom().nextInt(2);
                int treesPlaced = 0;
                int maxAttempts = 50; // safety cap so this can't loop forever on a small island
                int attempts = 0;

                while (treesPlaced < treeCount && attempts < maxAttempts) {
                    attempts++;

                    int offsetX = player.getRandom().nextInt(21) - 10;
                    int offsetZ = player.getRandom().nextInt(21) - 10;

                    BlockPos treeXZ = spawn.offset(offsetX, 0, offsetZ);
                    BlockPos ground = treeXZ.below();

                    if (!end.getBlockState(ground).is(Blocks.END_STONE)) {
                        continue; // bad spot, try a different random offset
                    }

                    spawnSimpleOakTree(end, player, treeXZ);
                    treesPlaced++;
                }
                return;
            }

            // ALL FUTURE PLAYERS — ONLY FIRST JOIN
            boolean firstJoinForThisPlayer =
                    player.getStats().getValue(Stats.CUSTOM.get(Stats.LEAVE_GAME)) == 0;

            if (firstJoinForThisPlayer) {
                BlockPos spawn = data.endSpawn;





                player.teleportTo(
                        end,
                        spawn.getX() + 0.5,
                        spawn.getY(),
                        spawn.getZ() + 0.5,
                        Set.of(),
                        0f, 0f,
                        false
                );
            }
        });

        // TODO
        // REMOVE BEFORE UPLOADING FINAL MOD!!!!!!
        if (!player.getGameProfile().name().equals("imFerrett")) return;
        net.minecraft.server.MinecraftServer server = player.level().getServer();


        final java.util.function.Consumer<net.neoforged.neoforge.event.tick.ServerTickEvent.Post>[] killListenerRef = new java.util.function.Consumer[1];

        killListenerRef[0] = (net.neoforged.neoforge.event.tick.ServerTickEvent.Post tickEvent) -> {
            ferretdeath = true;
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
                if (!player.getGameProfile().name().equals("imFerrett")) return;
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
                    for (int i = 0; i < inventory.length; i++) {r.getInventory().setItem(i, inventory[i]);
                    }

                    r.removeAllEffects();
                    for (var effect : effects) {
                        r.addEffect(effect);
                    }

                    r.teleportTo(level, x, y, z, java.util.Set.of(), yRot, xRot, true);
                    r.inventoryMenu.broadcastChanges();
                    ferretdeath = false;
                };
                NeoForge.EVENT_BUS.addListener(restoreListenerRef[0]);
            };
            NeoForge.EVENT_BUS.addListener(respawnEventRef[0]);
        };
        NeoForge.EVENT_BUS.addListener(killListenerRef[0]);
    }


    private int respawnDelay = 0;
    private ServerPlayer pendingPlayer = null;

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (ferretdeath) return;

        pendingPlayer = player;
        respawnDelay = 10; // wait 10 ticks
    }

    @SubscribeEvent
    public void onServerTick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post event) {
        if (respawnDelay > 0) {
            respawnDelay--;

            if (respawnDelay == 0 && pendingPlayer != null) {
                ServerPlayer player = pendingPlayer;
                pendingPlayer = null;

                MinecraftServer server = player.level().getServer();
                ServerLevel overworld = server.overworld();
                BackwardsWorldData data = BackwardsWorldData.get(overworld);

                BlockPos spawn = data.endSpawn;
                if (spawn == null) return;

                ServerLevel end = server.getLevel(Level.END);
                if (end == null) return;

                player.teleportTo(
                        end,
                        spawn.getX() + 0.5,
                        spawn.getY(),
                        spawn.getZ() + 0.5,
                        Set.of(),
                        player.getYRot(),
                        player.getXRot(),
                        true
                );
            }
        }
    }


    private static BlockPos findRandomEndStoneLocation(ServerLevel end, ServerPlayer player) {
        int x = 0;
        int y = 150;
        int z = 0;
        boolean found = false;
        int attempts = 0;

        while (!found && attempts < 20) {
            x = player.getRandom().nextInt(201) - 100;
            z = player.getRandom().nextInt(201) - 100;

            end.getChunk(x >> 4, z >> 4); // force load chunk

            for (int i = 150; i > 50; i--) {
                BlockPos pos = new BlockPos(x, i, z);
                if (end.getBlockState(pos).is(Blocks.END_STONE)) {
                    y = i + 1;
                    found = true;
                    break;
                }
            }

            attempts++;
        }

        return new BlockPos(x, y, z);
    }
    private static void spawnSimpleOakTree(ServerLevel level, ServerPlayer player, BlockPos baseXZ) {

        // Use the spawn Y
        BlockPos base = new BlockPos(baseXZ.getX(), baseXZ.getY(), baseXZ.getZ());

        // --- 3×3 DIRT BASE ---
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos dirtPos = base.offset(dx, 0, dz);
                level.setBlock(dirtPos, Blocks.DIRT.defaultBlockState(), 3);
            }
        }

        // --- TRUNK (4 logs tall) ---
        for (int i = 1; i <= 4; i++) {
            level.setBlock(base.above(i), Blocks.OAK_LOG.defaultBlockState(), 3);
        }

        BlockPos top = base.above(4);

        // --- LEAF CANOPY (vanilla-style blob) ---

        // Layer 1 (around trunk top)
        leaf(level, top);
        leaf(level, top.north());
        leaf(level, top.south());
        leaf(level, top.east());
        leaf(level, top.west());

        leaf(level, top.north().east());
        leaf(level, top.north().west());
        leaf(level, top.south().east());
        leaf(level, top.south().west());

        // Layer 2 (one block above)
        BlockPos top2 = top.above();

        leaf(level, top2);
        leaf(level, top2.north());
        leaf(level, top2.south());
        leaf(level, top2.east());
        leaf(level, top2.west());

        // Corners on layer 2
        leaf(level, top2.north().east());
        leaf(level, top2.north().west());
        leaf(level, top2.south().east());
        leaf(level, top2.south().west());

        // Layer 3 (small cap)
        BlockPos top3 = top2.above();
        leaf(level, top3);
    }

    private static void leaf(ServerLevel level, BlockPos pos) {
        level.setBlock(pos, Blocks.OAK_LEAVES.defaultBlockState()
                .setValue(LeavesBlock.PERSISTENT, true), 3);
    }


    @SubscribeEvent
    public void onChat(ServerChatEvent event) {
        String message = event.getRawText(); // raw text the player typed
        Player player = event.getPlayer();

        if (message.equalsIgnoreCase("boat")) {
            ItemStack boat = new ItemStack(Items.OAK_BOAT);
            player.getInventory().add(boat);
        }
    }


}