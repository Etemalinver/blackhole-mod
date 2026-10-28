package com.dsh.blackhole.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.nbt.NbtCompound;
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
            return;
        }
        var velocity = this.getVelocity();
        var from = this.getPos();
        var to = from.add(velocity);
        var ctx = new net.minecraft.world.RaycastContext(from, to,
                net.minecraft.world.RaycastContext.ShapeType.COLLIDER,
                net.minecraft.world.RaycastContext.FluidHandling.NONE, this);
        var hit = world.raycast(ctx);
        if (hit.getType() != net.minecraft.util.hit.HitResult.Type.MISS) {
            this.setPosition(hit.getPos());
            this.spawnHole();
            return;
        }
        this.setPosition(to);
        this.setVelocity(velocity.multiply(0.985).add(0.0, -0.03, 0.0));
        if (this.age > 120) {
            this.spawnHole();
        }
    }

    private void spawnHole() {
        if (!this.getWorld().isClient) {
            BlackHoleEntity hole = new BlackHoleEntity(BlackHoleMod.BLACK_HOLE, this.getWorld());
            hole.setPosition(this.getX(), this.getY(), this.getZ());
            this.getWorld().spawnEntity(hole);
        }
        this.discard();
    }
}