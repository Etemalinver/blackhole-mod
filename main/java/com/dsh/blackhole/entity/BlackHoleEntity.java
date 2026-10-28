package com.dsh.blackhole.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

/**
 * 黑洞本体：站在原地吸 50 格内的实体、持续造成伤害、喷吸积盘粒子。
 * 生命周期 12 秒（240 tick），结束时来一次“蒸发”爆发。
 */
public class BlackHoleEntity extends Entity {
    /** 吸附半径（方块） */
    public static final double RADIUS = 50.0;
    /** 存活时长（tick） */
    public static final int LIFETIME = 240;
    /** 每 tick 的吸附加速度系数 */
    private static final double PULL = 0.16;
    /** 每 10 tick 结算一次伤害，中心最大 8 点 */
    private static final float MAX_DAMAGE = 8.0f;

    private int age;
    private LivingEntity owner;

    public BlackHoleEntity(EntityType<?> type, World world) {
        super(type, world);
        this.setNoGravity(true);
        this.age = 0;
    }

    public void setOwner(LivingEntity owner) {
        this.owner = owner;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        // 不需要同步数据：位置与年龄两端各自算
    }

    @Override
    public void tick() {
        super.tick();
        this.age++;

        if (this.getWorld().isClient) {
            spawnClientParticles();
            return;
        }

        if (this.age == 1) {
            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 6.0f, 0.35f);
        }

        attractEntities();
        spawnServerParticles();

        if (this.age % 40 == 0) {
            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.BLOCK_PORTAL_AMBIENT, SoundCategory.PLAYERS, 4.0f, 0.4f);
        }

        if (this.age >= LIFETIME) {
            collapse();
        }
    }

    private void attractEntities() {
        Box area = new Box(this.getX() - RADIUS, this.getY() - RADIUS, this.getZ() - RADIUS,
                this.getX() + RADIUS, this.getY() + RADIUS, this.getZ() + RADIUS);

        List<Entity> nearby = this.getWorld().getOtherEntities(this, area,
                e -> !(e instanceof BlackHoleEntity) && e.isAlive());

        for (Entity target : nearby) {
            Vec3d toCenter = this.getPos().subtract(target.getPos());
            double distance = toCenter.length();
            if (distance > RADIUS || distance < 0.05) continue;

            double falloff = 1.0 - (distance / RADIUS);
            falloff *= falloff; // 越靠近越强

            Vec3d dir = toCenter.normalize();
            // 切向分量：让被吸的东西绕着转，像吸积盘
            Vec3d tangent = new Vec3d(-dir.z, 0.0, dir.x).multiply(0.55 * falloff);

            double accel = PULL * (0.35 + falloff * 2.2);
            Vec3d add = dir.multiply(accel).add(tangent.multiply(accel * 0.5));
            // 眼内轻微上浮，避免全贴地
            add = add.add(0.0, 0.012 * falloff, 0.0);

            target.addVelocity(add.x, add.y, add.z);
            target.velocityModified = true;
            target.fallDistance = 0.0f;

            if (this.age % 10 == 0) {
                float damage = (float) (MAX_DAMAGE * Math.pow(falloff, 1.5));
                if (damage >= 0.5f) {
                    target.damage(this.getWorld().getDamageSources().magic(), damage);
                }
            }
        }
    }

    private void collapse() {
        World world = this.getWorld();
        world.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 8.0f, 0.5f);
        if (world instanceof ServerWorld sw) {
            sw.spawnParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 12, 1.5, 1.5, 1.5, 0.0);
            sw.spawnParticles(ParticleTypes.END_ROD, this.getX(), this.getY(), this.getZ(), 80, 2.0, 2.0, 2.0, 0.4);
        }
        this.discard();
    }

    private void spawnServerParticles() {
        if (!(this.getWorld() instanceof ServerWorld sw)) return;

        double t = this.age * 0.16;

        // 吸积盘：两层旋臂，越靠外转得越慢
        for (int i = 0; i < 3; i++) {
            double angle = t + i * (Math.PI * 2.0 / 3.0);
            double r = 2.6 - (this.age % 40) * 0.03;
            double px = this.getX() + Math.cos(angle) * r;
            double pz = this.getZ() + Math.sin(angle) * r;
            double py = this.getY() + 0.15 + Math.sin(angle * 2.0) * 0.25;
            sw.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, px, py, pz, 1, 0.02, 0.02, 0.02, 0.0);
            sw.spawnParticles(ParticleTypes.END_ROD, this.getX() + Math.cos(angle + 1.05) * (r + 1.2),
                    py, this.getZ() + Math.sin(angle + 1.05) * (r + 1.2), 1, 0.0, 0.0, 0.0, 0.02);
        }

        // 被吸进来的东西：向心的暗色残影
        sw.spawnParticles(ParticleTypes.PORTAL, this.getX(), this.getY() + 0.4, this.getZ(),
                6, 1.8, 1.2, 1.8, 0.9);

        if (this.age % 4 == 0) {
            sw.spawnParticles(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY() + 0.3, this.getZ(),
                    10, 3.5, 2.0, 3.5, 0.35);
        }
    }

    private void spawnClientParticles() {
        // 客户端只做少量补充，主体粒子由服务端广播
        if (this.age % 3 != 0) return;
        for (int i = 0; i < 2; i++) {
            double angle = this.age * 0.2 + i * Math.PI;
            this.getWorld().addParticle(ParticleTypes.PORTAL,
                    this.getX() + Math.cos(angle) * 2.2,
                    this.getY() + 0.2,
                    this.getZ() + Math.sin(angle) * 2.2,
                    0.0, 0.06, 0.0);
        }
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        this.age = nbt.getInt("Age");
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putInt("Age", this.age);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canHit() {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    public int getAge() {
        return this.age;
    }

    public LivingEntity getOwner() {
        return this.owner;
    }
}
