package com.dsh.blackhole.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import com.dsh.blackhole.BlackHoleMod;

public class ThrownBlackHole extends Entity {
    public ThrownBlackHole(EntityType<?> type, World world) {
        super(type, world);
        this.setNoGravity(true);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
    }

    @Override
    public void tick() {
        super.tick();

        World world = this.getWorld();

        if (world.isClient) {
            if (this.age % 2 == 0) {
                world.addParticle(ParticleTypes.PORTAL, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
            }
            return;
        }

        Vec3d velocity = this.getVelocity();
        if (velocity.lengthSquared() < 0.0001) {
            velocity = new Vec3d(0.0, -0.4, 0.0);
        }

        Vec3d from = this.getPos();
        Vec3d to = from.add(velocity);

        HitResult hit = world.raycast(new RaycastContext(from, to,
                RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, this));

        if (hit.getType() != HitResult.Type.MISS) {
            this.setPosition(hit.getPos());
            spawnHole();
            return;
        }

        this.setPosition(to);
        this.setVelocity(velocity.multiply(0.985).add(0.0, -0.03, 0.0));

        if (this.age > 120) {
            spawnHole();
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
