package com.ferrett.backwardsmc;

import com.ferrett.backwardsmc.registry.ModEntities;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class ModRenderers {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.ARROW_SPIDER.get(), SpiderRenderer::new);
    }
}