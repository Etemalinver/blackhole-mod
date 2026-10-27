package com.dsh.blackhole.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.ThrownItemEntityRenderer;

import com.dsh.blackhole.BlackHoleMod;

public class BlackHoleClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(BlackHoleMod.THROWN_BLACK_HOLE, ThrownItemEntityRenderer::new);
        EntityRendererRegistry.register(BlackHoleMod.BLACK_HOLE, BlackHoleRenderer::new);
    }
}
