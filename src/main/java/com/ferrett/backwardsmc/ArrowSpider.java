package com.ferrett.backwardsmc;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class ArrowSpider extends Spider implements RangedAttackMob {

    public ArrowSpider(EntityType<? extends ArrowSpider> type, Level level) {
        super(type, level);
        this.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOW));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Spider.createAttributes();
    }

    @Override
    protected void registerGoals() {
        super.registerGoals(); // keep spider's existing goals (movement, targeting, etc.)
        this.goalSelector.addGoal(1, new RangedBowAttackGoal<>(this, 1.0D, 30, 15.0F));
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        Arrow arrow = new Arrow(EntityType.ARROW, this.level());
        arrow.setOwner(this);
        arrow.setPos(this.getX(), this.getEyeY() - 0.1, this.getZ());

        double dx = target.getX() - this.getX();
        double dy = target.getEyeY() - this.getEyeY();
        double dz = target.getZ() - this.getZ();
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);

        arrow.shoot(dx, dy + horizontalDist * 0.2, dz, 1.6F, 1.0F);
        this.level().addFreshEntity(arrow);
    }
}