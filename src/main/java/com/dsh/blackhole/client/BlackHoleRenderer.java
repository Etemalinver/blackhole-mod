package com.dsh.blackhole.client;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

import com.dsh.blackhole.entity.BlackHoleEntity;

/**
 * 黑洞渲染：三层面向镜头的四边形叠出来
 *   1. 吸积盘（橙色旋涡，半透明叠加）
 *   2. 事件视界（纯黑圆盘，压在中间挡住星空）
 *   3. 引力透镜光环（青白色细环，反向旋转）
 */
public class BlackHoleRenderer extends EntityRenderer<BlackHoleEntity> {
    private static final Identifier CORE = Identifier.of("blackhole", "textures/entity/black_hole_core.png");
    private static final Identifier DISK = Identifier.of("blackhole", "textures/entity/black_hole_disk.png");
    private static final Identifier RING = Identifier.of("blackhole", "textures/entity/black_hole_ring.png");

    public BlackHoleRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public void render(BlackHoleEntity entity, float yaw, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light) {
        float t = entity.getAge() + tickDelta;

        // 生成时 10 tick 涨到全尺寸，最后 20 tick 收缩
        float grow = Math.min(1.0f, t / 10.0f)
                * Math.max(0.0f, 1.0f - Math.max(0.0f, (t - (BlackHoleEntity.LIFETIME - 20)) / 20.0f));
        if (grow <= 0.001f) {
            return;
        }

        matrices.push();
        // 面向镜头
        matrices.multiply(this.dispatcher.getRotation());
        matrices.multiply(RotationAxis.POSITIVE_Z.rotation(t * 0.05f));

        // 1. 吸积盘
        drawQuad(vertexConsumers, matrices, DISK, 3.6f * grow, 1.0f, 0.72f, 0.30f, 0.92f);
        // 2. 事件视界
        drawQuad(vertexConsumers, matrices, CORE, 1.85f * grow, 1.0f, 1.0f, 1.0f, 1.0f);
        // 3. 光环（反向慢转）
        matrices.multiply(RotationAxis.POSITIVE_Z.rotation(-t * 0.09f));
        drawQuad(vertexConsumers, matrices, RING, 2.7f * grow, 0.55f, 0.88f, 1.0f, 0.8f);

        matrices.pop();

        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    private static void drawQuad(VertexConsumerProvider vertexConsumers, MatrixStack matrices,
                                 Identifier texture, float half, float r, float g, float b, float a) {
        VertexConsumer vc = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucentEmissive(texture));
        MatrixStack.Entry entry = matrices.peek();
        float z = 0.02f;

        vc.vertex(entry, -half, -half, z).color(r, g, b, a).texture(0f, 1f)
                .overlay(OverlayTexture.DEFAULT_UV).light(0xF000F0).normal(entry, 0f, 0f, 1f);
        vc.vertex(entry, half, -half, z).color(r, g, b, a).texture(1f, 1f)
                .overlay(OverlayTexture.DEFAULT_UV).light(0xF000F0).normal(entry, 0f, 0f, 1f);
        vc.vertex(entry, half, half, z).color(r, g, b, a).texture(1f, 0f)
                .overlay(OverlayTexture.DEFAULT_UV).light(0xF000F0).normal(entry, 0f, 0f, 1f);
        vc.vertex(entry, -half, half, z).color(r, g, b, a).texture(0f, 0f)
                .overlay(OverlayTexture.DEFAULT_UV).light(0xF000F0).normal(entry, 0f, 0f, 1f);
    }

    @Override
    public Identifier getTexture(BlackHoleEntity entity) {
        return CORE;
    }
}
