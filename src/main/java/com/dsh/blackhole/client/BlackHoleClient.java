package com.dsh.blackhole.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

import com.dsh.blackhole.BlackHoleMod;
import com.dsh.blackhole.entity.ThrownBlackHole;

public class BlackHoleClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(BlackHoleMod.THROWN_BLACK_HOLE, ThrownRenderer::new);
        EntityRendererRegistry.register(BlackHoleMod.BLACK_HOLE, BlackHoleRenderer::new);
    }

    /** 飞行中的黑洞手雷：一个小黑球 + 旋转的橙色光环（不依赖 ThrownItemEntityRenderer） */
    public static class ThrownRenderer extends EntityRenderer<ThrownBlackHole> {
        private static final Identifier CORE = Identifier.of("blackhole", "textures/entity/black_hole_core.png");
        private static final Identifier DISK = Identifier.of("blackhole", "textures/entity/black_hole_disk.png");

        public ThrownRenderer(EntityRendererFactory.Context context) {
            super(context);
        }

        @Override
        public void render(ThrownBlackHole entity, float yaw, float tickDelta, MatrixStack matrices,
                           VertexConsumerProvider vertexConsumers, int light) {
            float t = entity.age + tickDelta;
            matrices.push();
            matrices.multiply(this.dispatcher.getRotation());
            matrices.multiply(RotationAxis.POSITIVE_Z.rotation(t * 0.22f));
            drawQuad(vertexConsumers, matrices, DISK, 0.62f, 1.0f, 0.62f, 0.22f, 0.9f);
            drawQuad(vertexConsumers, matrices, CORE, 0.30f, 1.0f, 1.0f, 1.0f, 1.0f);
            matrices.pop();
            super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
        }

        private static void drawQuad(VertexConsumerProvider vertexConsumers, MatrixStack matrices,
                                     Identifier texture, float half, float r, float g, float b, float a) {
            VertexConsumer vc = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucentEmissive(texture));
            MatrixStack.Entry entry = matrices.peek();
            vc.vertex(entry, -half, -half, 0f).color(r, g, b, a).texture(0f, 1f)
                    .overlay(OverlayTexture.DEFAULT_UV).light(0xF000F0).normal(entry, 0f, 0f, 1f);
            vc.vertex(entry, half, -half, 0f).color(r, g, b, a).texture(1f, 1f)
                    .overlay(OverlayTexture.DEFAULT_UV).light(0xF000F0).normal(entry, 0f, 0f, 1f);
            vc.vertex(entry, half, half, 0f).color(r, g, b, a).texture(1f, 0f)
                    .overlay(OverlayTexture.DEFAULT_UV).light(0xF000F0).normal(entry, 0f, 0f, 1f);
            vc.vertex(entry, -half, half, 0f).color(r, g, b, a).texture(0f, 0f)
                    .overlay(OverlayTexture.DEFAULT_UV).light(0xF000F0).normal(entry, 0f, 0f, 1f);
        }

        @Override
        public Identifier getTexture(ThrownBlackHole entity) {
            return CORE;
        }
    }
}
