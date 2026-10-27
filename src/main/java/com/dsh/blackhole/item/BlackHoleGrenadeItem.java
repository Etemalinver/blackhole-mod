package com.dsh.blackhole.item;

import com.dsh.blackhole.BlackHoleMod;
import com.dsh.blackhole.entity.ThrownBlackHole;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * 黑洞手雷：右键扔出去，落点生成一个持续 12 秒的黑洞。
 */
public class BlackHoleGrenadeItem extends Item {
    public BlackHoleGrenadeItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        world.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.PLAYERS, 0.8f, 0.45f);

        if (!world.isClient) {
            ThrownBlackHole projectile = new ThrownBlackHole(BlackHoleMod.THROWN_BLACK_HOLE, world);
            projectile.setItem(stack.copy());
            projectile.setOwner(user);
            projectile.setPosition(user.getX(), user.getEyeY() - 0.15, user.getZ());
            Vec3d look = user.getRotationVec(1.0f);
            projectile.setVelocity(look.x * 1.9, look.y * 1.9 + 0.12, look.z * 1.9);
            world.spawnEntity(projectile);
        }

        stack.decrementUnlessCreative(1, user);
        return TypedActionResult.success(stack, world.isClient());
    }
}
