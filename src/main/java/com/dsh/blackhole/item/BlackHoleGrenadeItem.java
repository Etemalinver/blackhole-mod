package com.dsh.blackhole.item;

import com.dsh.blackhole.BlackHoleMod;
import com.dsh.blackhole.entity.BlackHoleEntity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

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
            Vec3d from = user.getEyePos();
            Vec3d to = from.add(user.getRotationVec(1.0f).multiply(40.0));
            HitResult hit = world.raycast(new RaycastContext(from, to,
                    RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, user));
            Vec3d spawnAt = hit.getType() == HitResult.Type.MISS ? to : hit.getPos();
            BlackHoleEntity hole = new BlackHoleEntity(BlackHoleMod.BLACK_HOLE, world);
            hole.setPosition(spawnAt.getX(), spawnAt.getY(), spawnAt.getZ());
            hole.setOwner(user);
            world.spawnEntity(hole);
        }
        stack.decrementUnlessCreative(1, user);
        return TypedActionResult.success(stack, world.isClient());
    }
}