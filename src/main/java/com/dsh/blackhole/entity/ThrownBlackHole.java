package com.dsh.blackhole.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

import com.dsh.blackhole.BlackHoleMod;

/**
 * 飞行中的黑洞手雷：撞到方块/实体后就地生成黑洞。
 * 1.21.1 的 ThrownItemEntity 是抽象类，必须实现 getDefaultItemStack()。
 */
public class ThrownBlackHole extends ThrownItemEntity {
    public ThrownBlackHole(EntityType<? extends ThrownBlackHole> type, World world) {
        super(type, world);
    }

    @Override
    protected ItemStack getDefaultItemStack() {
        return new ItemStack(BlackHoleMod.BLACK_HOLE_GRENADE);
    }

    @Override
    protected void onEntityHit(EntityHitResult result) {
        super.onEntityHit(result);
        spawnHole();
    }

    @Override
    protected void onCollision(HitResult result) {
        super.onCollision(result);
        spawnHole();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getWorld().isClient && this.age % 2 == 0) {
            this.getWorld().addParticle(ParticleTypes.PORTAL,
                    this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
        }
    }

    private void spawnHole() {
        World world = this.getWorld();
        if (!world.isClient) {
            BlackHoleEntity hole = new BlackHoleEntity(BlackHoleMod.BLACK_HOLE, world);
            hole.setPosition(this.getX(), this.getY(), this.getZ());
            Entity owner = this.getOwner();
            if (owner instanceof LivingEntity living) {
                hole.setOwner(living);
            }
            world.spawnEntity(hole);
            if (world instanceof ServerWorld sw) {
                sw.spawnParticles(ParticleTypes.FLASH, this.getX(), this.getY(), this.getZ(), 3, 0.2, 0.2, 0.2, 0.0);
            }
        }
        this.discard();
    }
}
