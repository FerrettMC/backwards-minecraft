package com.ferrett.backwardsmc;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

public class NightDaySwitch {
    @SubscribeEvent
    public void onMobSpawn(MobSpawnEvent.PositionCheck event) {
        if (!(event.getLevel() instanceof ServerLevel server)) return;

        boolean isDay = server.getSkyDarken() < 4;
        boolean isNight = !isDay;

        // Only affect hostile mobs
        if (!(event.getEntity() instanceof net.minecraft.world.entity.monster.Monster)) return;

        // NIGHT behaves like DAY → block hostile spawns
        if (isNight) {
            event.setResult(MobSpawnEvent.PositionCheck.Result.FAIL);
            return;
        }

        // DAY behaves like NIGHT → force allow spawn
        event.setResult(MobSpawnEvent.PositionCheck.Result.SUCCEED);
    }


    @SubscribeEvent
    public void onSpawnPlacement(MobSpawnEvent.SpawnPlacementCheck event) {
        var level = event.getLevel();
        if (!(level instanceof ServerLevel server)) return;

        boolean isDay = server.getSkyDarken() < 4;
        boolean isNight = !isDay;


        // Only affect hostile mobs
        if (!event.getEntityType().getCategory().isFriendly()) {

            // NIGHT behaves like DAY → block hostile spawns
            if (isNight) {
                event.setResult(MobSpawnEvent.SpawnPlacementCheck.Result.FAIL);
                return;
            }

            // DAY behaves like NIGHT → force allow spawn even in bright light
            event.setResult(MobSpawnEvent.SpawnPlacementCheck.Result.SUCCEED);
        }
    }




    @SubscribeEvent
    public void onServerTick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post event) {
        var server = event.getServer();

        for (ServerLevel level : server.getAllLevels()) {

            // Only spawn mobs during the day
            boolean isDay = level.getSkyDarken() < 4;
            if (!isDay) continue;

            for (var player : level.players()) {

                if (level.dimension() != net.minecraft.world.level.Level.OVERWORLD) continue;

                if (level.random.nextInt(500) != 0) continue;

                // Pick a random position within 40 blocks
                int dx = level.random.nextInt(81) - 40; // -40 to +40
                int dz = level.random.nextInt(81) - 40;
                int x = player.getBlockX() + dx;
                int z = player.getBlockZ() + dz;

                // Find ground height
                var pos = level.getHeightmapPos(
                        net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                        new net.minecraft.core.BlockPos(x, 0, z)
                );

                // Don't spawn too close to the player
                if (pos.distToCenterSqr(player.position()) < 10 * 10) continue;

                // Pick a mob type
                var mobType = switch (level.random.nextInt(4)) {
                    case 0 -> net.minecraft.world.entity.EntityType.ZOMBIE;
                    case 1 -> net.minecraft.world.entity.EntityType.SKELETON;
                    case 2 -> net.minecraft.world.entity.EntityType.SPIDER;
                    default -> net.minecraft.world.entity.EntityType.CREEPER;
                };

                // Spawn the mob
                mobType.spawn(level, pos, net.minecraft.world.entity.EntitySpawnReason.NATURAL);
            }
        }
    }





    @SubscribeEvent
    public void onSleep(net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent event) {
        var player = event.getEntity();
        var level = (ServerLevel) player.level();

        // Correct day/night detection
        boolean isDay = level.getSkyDarken() < 4;
        boolean isNight = !isDay;

        if (isDay) {
            // DAY behaves like NIGHT → allow sleeping
            // Allow sleep by clearing the problem
            event.setProblem(null);
        } else {
            // NIGHT behaves like DAY → block sleeping
            event.setProblem(Player.BedSleepingProblem.NOT_SAFE);
        }
    }

    @SubscribeEvent
    public void onWakeUp(PlayerWakeUpEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) return; // <-- FIX

        if (!event.updateLevel()) return;

        level.setDayTime(13000);
    }



    @SubscribeEvent
    public void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity().level() instanceof ServerLevel server)) return; // <-- FIX

        if (!(event.getEntity() instanceof LivingEntity living)) return;

        if (!(living instanceof Zombie || living instanceof Skeleton)) return;

        boolean isDay = server.getSkyDarken() < 4;

        if (isDay) {
            living.clearFire();
        } else if (server.canSeeSky(living.blockPosition())) {
            living.setRemainingFireTicks(160);
        }
    }




}
