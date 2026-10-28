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
 * 榛戞礊鏈綋锛氱珯鍦ㄥ師鍦板惛 50 鏍煎唴鐨勫疄浣撱€佹寔缁€犳垚浼ゅ銆佸柗鍚哥Н鐩樼矑瀛愩€? * 鐢熷懡鍛ㄦ湡 12 绉掞紙240 tick锛夛紝缁撴潫鏃舵潵涓€娆♀€滆捀鍙戔€濈垎鍙戙€? */
public class BlackHoleEntity extends Entity {
    /** 鍚搁檮鍗婂緞锛堟柟鍧楋級 */
    public static final double RADIUS = 50.0;
    /** 瀛樻椿鏃堕暱锛坱ick锛?*/
    public static final int LIFETIME = 240;
    /** 姣?tick 鐨勫惛闄勫姞閫熷害绯绘暟 */
    private static final double PULL = 0.16;
    /** 姣?10 tick 缁撶畻涓€娆′激瀹筹紝涓績鏈€澶?8 鐐?*/
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
        // 涓嶉渶瑕佸悓姝ユ暟鎹細浣嶇疆涓庡勾榫勪袱绔悇鑷畻
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
            if (target == this.owner) continue;
            Vec3d toCenter = this.getPos().subtract(target.getPos());
            double distance = toCenter.length();
            if (distance > RADIUS || distance < 0.05) continue;

            double falloff = 1.0 - (distance / RADIUS);
            falloff *= falloff; // 瓒婇潬杩戣秺寮?
            Vec3d dir = toCenter.normalize();
            // 鍒囧悜鍒嗛噺锛氳琚惛鐨勪笢瑗跨粫鐫€杞紝鍍忓惛绉洏
            Vec3d tangent = new Vec3d(-dir.z, 0.0, dir.x).multiply(0.55 * falloff);

            double accel = PULL * (0.35 + falloff * 2.2);
            Vec3d add = dir.multiply(accel).add(tangent.multiply(accel * 0.5));
            // 鐪煎唴杞诲井涓婃诞锛岄伩鍏嶅叏璐村湴
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

        // 鍚哥Н鐩橈細涓ゅ眰鏃嬭噦锛岃秺闈犲杞緱瓒婃參
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

        // 琚惛杩涙潵鐨勪笢瑗匡細鍚戝績鐨勬殫鑹叉畫褰?        sw.spawnParticles(ParticleTypes.PORTAL, this.getX(), this.getY() + 0.4, this.getZ(),
                6, 1.8, 1.2, 1.8, 0.9);

        if (this.age % 4 == 0) {
            sw.spawnParticles(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY() + 0.3, this.getZ(),
                    10, 3.5, 2.0, 3.5, 0.35);
        }
    }

    private void spawnClientParticles() {
        // 瀹㈡埛绔彧鍋氬皯閲忚ˉ鍏咃紝涓讳綋绮掑瓙鐢辨湇鍔＄骞挎挱
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
