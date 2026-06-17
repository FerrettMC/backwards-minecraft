package com.ferrett.backwardsmc;

import com.ferrett.backwardsmc.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

public class SkeletonSpider {
    @SubscribeEvent
    public void onSkeletonJoin(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof Skeleton skeleton)) return;

        // Remove bow attack AI
        skeleton.goalSelector.removeAllGoals(goal -> goal instanceof RangedBowAttackGoal);

        // Add melee attack AI
        skeleton.goalSelector.addGoal(1, new MeleeAttackGoal(skeleton, 1.2D, false));

        // Optional: give them a sword
        skeleton.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WOODEN_SHOVEL));
    }

    @SubscribeEvent
    public void onSpiderJoin(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof Spider spider)) return;
        if (event.getEntity() instanceof ArrowSpider) return;
        if (!(event.getLevel() instanceof ServerLevel server)) return;
        Level level = event.getLevel();
        BlockPos spiderPos = spider.blockPosition();
        spider.kill((ServerLevel) event.getLevel());
        EntityType<ArrowSpider> type = (EntityType<ArrowSpider>) ModEntities.ARROW_SPIDER.get();
        ArrowSpider arrowSpider = type.create(server, EntitySpawnReason.MOB_SUMMONED);
        if (arrowSpider == null) return;
        arrowSpider.teleportTo(spiderPos.getX(), spiderPos.getY(), spiderPos.getZ());
        arrowSpider.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOW));
        level.addFreshEntity(arrowSpider);
    }


    // Throw in an iron golem too XDD

    @SubscribeEvent
    public void onGolemJoin(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof IronGolem golem)) return;
        if (!(event.getLevel() instanceof ServerLevel)) return;

        golem.getAttribute(Attributes.MAX_HEALTH).setBaseValue(2.0);
        golem.setHealth(2.0f);
        golem.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(1.0);
        golem.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.4);
        golem.getAttribute(Attributes.SCALE).setBaseValue(0.3);

        // Make hostile toward players
        golem.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(golem, Player.class, true));
        golem.goalSelector.addGoal(1, new MeleeAttackGoal(golem, 1.2D, true));
    }
}
