package com.ferrett.backwardsmc.registry;

import com.ferrett.backwardsmc.ArrowSpider;
import com.ferrett.backwardsmc.BackwardsMinecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, "backwardsmc");

    public static final DeferredHolder<EntityType<?>, EntityType<ArrowSpider>> ARROW_SPIDER =
            ENTITIES.register("arrow_spider", () ->
                    EntityType.Builder.of(ArrowSpider::new, MobCategory.MONSTER)
                            .sized(1.4F, 0.9F)
                            .build(ResourceKey.create(
                                    Registries.ENTITY_TYPE,
                                    Identifier.fromNamespaceAndPath("backwardsmc", "arrow_spider")
                            ))
            );
}
