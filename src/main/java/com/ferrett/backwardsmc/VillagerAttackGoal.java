package com.ferrett.backwardsmc;

public class VillagerAttackGoal extends net.minecraft.world.entity.ai.goal.MeleeAttackGoal {

    private final net.minecraft.world.entity.npc.villager.Villager villager;

    public VillagerAttackGoal(net.minecraft.world.entity.npc.villager.Villager villager, double speed, boolean pauseWhenIdle) {
        super(villager, speed, pauseWhenIdle);
        this.villager = villager;
    }

    @Override
    public boolean canUse() {
        return !villager.isSleeping() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return !villager.isSleeping() && super.canContinueToUse();
    }
}
