package com.ferrett.backwardsmc;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class ZombieTradeVillagerAttack {
    public static class PlayerRequest {
        private final Player player;
        private String type;

        public PlayerRequest(Player player, String type) {
            this.player = player;

            this.type = type;
        }

        public Player getPlayer() {
            return player;
        }

        public String getType() {
            return type;
        }

        public void setType(String message) {
            this.type = message;
        }
    }

    private static List<PlayerRequest> playersTrading = new ArrayList<>();
    @SubscribeEvent
    public void onVillagerJoin(net.neoforged.neoforge.event.entity.EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof net.minecraft.world.entity.npc.villager.Villager villager)) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        // Remove all normal villager AI
        villager.goalSelector.removeAllGoals(g -> true);
        villager.targetSelector.removeAllGoals(g -> true);
        villager.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED)
                .setBaseValue(0.25D); // normal mobs are ~0.23

        // Give the villager a weapon so it can deal damage
        villager.setItemInHand(
                net.minecraft.world.InteractionHand.MAIN_HAND,
                new net.minecraft.world.item.ItemStack(Items.WOODEN_SHOVEL)
        );

        // Add hostile behavior
        villager.targetSelector.addGoal(1,
                new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(
                        villager,
                        net.minecraft.world.entity.player.Player.class,
                        true
                )
        );

        // Use MeleeAttackGoal (safe because weapon provides damage)
        villager.goalSelector.addGoal(1,
                new VillagerAttackGoal(villager, 0.8D, false)
        );
    }


    @SubscribeEvent
    public void onVillagerInteract(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof net.minecraft.world.entity.npc.villager.Villager)) return;
        if (!(event.getEntity().level() instanceof ServerLevel)) return;

        // Cancel the interaction so the trading GUI never opens
        event.setCanceled(true);
    }


    @SubscribeEvent
    public void onZombieJoin(net.neoforged.neoforge.event.entity.EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof net.minecraft.world.entity.monster.zombie.Zombie zombie)) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        // Remove all hostile zombie AI
        zombie.goalSelector.removeAllGoals(g -> true);
        zombie.targetSelector.removeAllGoals(g -> true);

        // Add friendly wandering behavior
        zombie.goalSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.RandomStrollGoal(zombie, 1.0D));
        zombie.goalSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.LookAtPlayerGoal(zombie, net.minecraft.world.entity.player.Player.class, 8.0F));
        zombie.goalSelector.addGoal(3, new net.minecraft.world.entity.ai.goal.RandomLookAroundGoal(zombie));
    }

    @SubscribeEvent
    public void onZombieInteract(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific event) {

        if (!(event.getTarget() instanceof net.minecraft.world.entity.monster.zombie.Zombie zombie)) return;
        if (!(event.getEntity().level() instanceof ServerLevel level)) return;
        if (event.getHand() != net.minecraft.world.InteractionHand.MAIN_HAND) return;

        event.setCanceled(true);
        Player player = event.getEntity();

        PlayerRequest req = playersTrading.stream()
                .filter(r -> r.getPlayer().equals(player))
                .findFirst()
                .orElse(null);

        if (req != null) {
            player.displayClientMessage(Component.literal("§cYou are already trading with the zombie!"), false);
            return;
        }

        if (player.getMainHandItem().is(Items.DIRT)) {
            int count = player.getMainHandItem().getCount();

            if (count < 5) {
                player.displayClientMessage(Component.literal("§cNot enough dirt! §7(§c5 dirt§7 required§7)"), false);
                return;
            }

            if (count < 10) {
                player.displayClientMessage(Component.literal("§eTrade Offer: §f5 Dirt → 1 Diamond\n§7Type §a'yes' §7to confirm or §c'no' §7to cancel."), false);
                playersTrading.removeIf(r -> r.getPlayer().equals(player));
                playersTrading.add(new PlayerRequest(player, "diamond"));
            } else {
                player.displayClientMessage(Component.literal(
                        "§eZombie Trader Menu:\n" +
                                "§b• 1 Diamond §7= §f5 Dirt\n" +
                                "§b• 1 Ender Pearl §7= §f10 Dirt\n" +
                                "§b• 1 Blaze Rod §7= §f10 Dirt\n" +
                                "§7Type: §a'diamond'§7, §a'enderpearl'§7, or §a'blazerod'"
                ), false);

                playersTrading.removeIf(r -> r.getPlayer().equals(player));
                playersTrading.add(new PlayerRequest(player, "others"));
            }
        } else {
            player.displayClientMessage(Component.literal("§cHold dirt to trade with the zombie!"), true);
            player.displayClientMessage(Component.literal(
                    "§eWelcome to §2Mr. Zombie Trader§e!\n" +
                            "§b• 1 Diamond §7= §f5 Dirt\n" +
                            "§b• 1 Blaze Rod §7= §f10 Dirt\n" +
                            "§b• 1 Ender Pearl §7= §f10 Dirt"
            ), false);
        }
    }

    @SubscribeEvent
    public void onPlayerChat(net.neoforged.neoforge.event.ServerChatEvent event) {
        Player player = event.getPlayer();
        ItemStack stack = player.getMainHandItem();
        String message = event.getMessage().getString();

        PlayerRequest req = playersTrading.stream()
                .filter(r -> r.getPlayer().equals(player))
                .findFirst()
                .orElse(null);

        if (req == null) return;

        if (req.getType().equals("diamond")) {
            if (player.getMainHandItem().is(Items.DIRT)) {
                if (message.equalsIgnoreCase("yes")) {
                    if (stack.is(Items.DIRT) && stack.getCount() >= 5) {
                        stack.shrink(5);
                    }
                    player.addItem(new ItemStack(Items.DIAMOND));
                    player.displayClientMessage(Component.literal("§aTrade complete! §fYou received §b1 Diamond§f."), false);
                    playersTrading.remove(req);
                    return;
                } else {
                    playersTrading.remove(req);
                    player.displayClientMessage(Component.literal("§cTrade cancelled."), false);
                    return;
                }
            } else {
                playersTrading.remove(req);
                player.displayClientMessage(Component.literal("§cPlease hold your dirt to complete the trade."), false);
                return;
            }
        }

        if (req.getType().equals("others")) {
            if (player.getMainHandItem().is(Items.DIRT)) {

                if (message.equalsIgnoreCase("diamond")) {
                    if (stack.getCount() >= 5) stack.shrink(5);
                    player.addItem(new ItemStack(Items.DIAMOND));
                    player.displayClientMessage(Component.literal("§aTrade complete! §fYou received §b1 Diamond§f."), false);
                    playersTrading.remove(req);
                    return;
                }

                else if (message.equalsIgnoreCase("blazerod")) {
                    if (stack.getCount() >= 10) stack.shrink(10);
                    player.addItem(new ItemStack(Items.BLAZE_ROD));
                    player.displayClientMessage(Component.literal("§aTrade complete! §fYou received §61 Blaze Rod§f."), false);
                    playersTrading.remove(req);
                    return;
                }

                else if (message.equalsIgnoreCase("enderpearl")) {
                    if (stack.getCount() >= 10) stack.shrink(10);
                    player.addItem(new ItemStack(Items.ENDER_PEARL));
                    player.displayClientMessage(Component.literal("§aTrade complete! §fYou received §51 Ender Pearl§f."), false);
                    playersTrading.remove(req);
                    return;
                }

                else {
                    playersTrading.remove(req);
                    player.displayClientMessage(Component.literal("§cInvalid choice. Trade cancelled."), false);
                    return;
                }

            } else {
                playersTrading.remove(req);
                player.displayClientMessage(Component.literal("§cPlease hold your dirt to complete the trade."), false);
                return;
            }
        }

        playersTrading.remove(req);
        player.displayClientMessage(Component.literal("§cTrade cancelled."), false);
    }


}


