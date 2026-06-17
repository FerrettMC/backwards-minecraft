package com.ferrett.backwardsmc;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

import java.util.Set;

public class DirtPortal {
    @SubscribeEvent
    public void onPlayerTick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ServerLevel level = (ServerLevel) player.level();

        // Only works in the End
        if (!level.dimension().equals(Level.END)) return;

        BlockPos feet = player.blockPosition();

        // If the player is inside the dirt portal
        if (isInDirtPortal(level, feet)) {
            teleportPlayerToOverworld(player);
        }
    }
    private static boolean isInDirtPortal(ServerLevel level, BlockPos feet) {

        BlockPos head = feet.above();

        // Player must be standing in the 1×2 air gap
        if (!level.getBlockState(feet).isAir()) return false;
        if (!level.getBlockState(head).isAir()) return false;

        // Try X-axis portal first (opening faces east/west)
        if (isDirtPortalX(level, feet, head)) return true;

        // Try Z-axis portal (opening faces north/south)
        if (isDirtPortalZ(level, feet, head)) return true;

        return false;
    }

    private static boolean isDirtPortalX(ServerLevel level, BlockPos feet, BlockPos head) {

        // Bottom row
        if (!level.getBlockState(feet.below().west()).is(Blocks.DIRT)) return false;
        if (!level.getBlockState(feet.below()).is(Blocks.DIRT)) return false;
        if (!level.getBlockState(feet.below().east()).is(Blocks.DIRT)) return false;

        // Middle rows (sides only)
        if (!level.getBlockState(feet.west()).is(Blocks.DIRT)) return false;
        if (!level.getBlockState(feet.east()).is(Blocks.DIRT)) return false;

        if (!level.getBlockState(head.west()).is(Blocks.DIRT)) return false;
        if (!level.getBlockState(head.east()).is(Blocks.DIRT)) return false;

        // Top row
        BlockPos top = head.above();
        if (!level.getBlockState(top.west()).is(Blocks.DIRT)) return false;
        if (!level.getBlockState(top).is(Blocks.DIRT)) return false;
        if (!level.getBlockState(top.east()).is(Blocks.DIRT)) return false;

        return true;
    }

    private static boolean isDirtPortalZ(ServerLevel level, BlockPos feet, BlockPos head) {

        // Bottom row
        if (!level.getBlockState(feet.below().north()).is(Blocks.DIRT)) return false;
        if (!level.getBlockState(feet.below()).is(Blocks.DIRT)) return false;
        if (!level.getBlockState(feet.below().south()).is(Blocks.DIRT)) return false;

        // Middle rows (sides only)
        if (!level.getBlockState(feet.north()).is(Blocks.DIRT)) return false;
        if (!level.getBlockState(feet.south()).is(Blocks.DIRT)) return false;

        if (!level.getBlockState(head.north()).is(Blocks.DIRT)) return false;
        if (!level.getBlockState(head.south()).is(Blocks.DIRT)) return false;

        // Top row
        BlockPos top = head.above();
        if (!level.getBlockState(top.north()).is(Blocks.DIRT)) return false;
        if (!level.getBlockState(top).is(Blocks.DIRT)) return false;
        if (!level.getBlockState(top.south()).is(Blocks.DIRT)) return false;

        return true;
    }




    private static void teleportPlayerToOverworld(ServerPlayer player) {
        MinecraftServer server = player.level().getServer();
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);

        if (overworld == null) return;

        int x = 0;
        int y = 200;
        int z = 0;
        for (int i = 200; i > 0; i--) {
            if (((ServerLevel) player.level()).getBlockState(new BlockPos(x, i, z)).is(Blocks.AIR)) {
                continue;
            } else {
                y = i + 3;
                break;
            }
        }

        player.teleportTo(
                overworld,
                x + 0.5,
                y,
                z + 0.5,
                Set.of(),
                player.getYRot(),
                player.getXRot(),
                true
        );
    }


}
