package com.dsh.blackhole;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dsh.blackhole.entity.BlackHoleEntity;
import com.dsh.blackhole.entity.ThrownBlackHole;
import com.dsh.blackhole.item.BlackHoleGrenadeItem;

public final class BlackHoleMod implements ModInitializer {
    public static final String MOD_ID = "blackhole";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    // ---- 物品 ----
    public static final Item BLACK_HOLE_GRENADE = new BlackHoleGrenadeItem(
            new Item.Settings().maxCount(16));

    // ---- 实体 ----
    public static final EntityType<ThrownBlackHole> THROWN_BLACK_HOLE = Registry.register(
            Registries.ENTITY_TYPE,
            id("thrown_black_hole"),
            EntityType.Builder.<ThrownBlackHole>create(ThrownBlackHole::new, SpawnGroup.MISC)
                    .dimensions(0.35f, 0.35f)
                    .maxTrackingRange(8)
                    .trackingTickInterval(10)
                    .build());

    public static final EntityType<BlackHoleEntity> BLACK_HOLE = Registry.register(
            Registries.ENTITY_TYPE,
            id("black_hole"),
            EntityType.Builder.<BlackHoleEntity>create(BlackHoleEntity::new, SpawnGroup.MISC)
                    .dimensions(1.0f, 1.0f)
                    .maxTrackingRange(16)
                    .trackingTickInterval(5)
                    .build());

    @Override
    public void onInitialize() {
        Registry.register(Registries.ITEM, id("black_hole_grenade"), BLACK_HOLE_GRENADE);

        Registry.register(Registries.ITEM_GROUP, id("main"), FabricItemGroup.builder()
                .icon(() -> new ItemStack(BLACK_HOLE_GRENADE))
                .displayName(Text.translatable("itemGroup.blackhole.main"))
                .entries((context, entries) -> entries.add(BLACK_HOLE_GRENADE))
                .build());

        LOGGER.info("[blackhole] loaded: 50-block gravity well ready");
    }
}
