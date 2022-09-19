package io.github.halffocused.diamond_is_uncraftable.init;

import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import io.github.halffocused.diamond_is_uncraftable.entity.StandArrowEntity;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.*;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.attack.*;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class EntityInit {
    public static DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITIES, DiamondIsUncraftable.MOD_ID);

    public static final RegistryObject<EntityType<KingCrimsonEntity>> KING_CRIMSON = ENTITY_TYPES
            .register("king_crimson",
                    () -> EntityType.Builder.create(KingCrimsonEntity::new, EntityClassification.CREATURE)
                            .disableSummoning().size(1.2f, 2.7f)
                            .build(new ResourceLocation(DiamondIsUncraftable.MOD_ID, "king_crimson").toString()));

    public static final RegistryObject<EntityType<KillerQueenEntity>> KILLER_QUEEN = ENTITY_TYPES
            .register("killer_queen",
                    () -> EntityType.Builder.create(KillerQueenEntity::new, EntityClassification.CREATURE)
                            .disableSummoning().size(1.2f, 2.7f)
                            .build(new ResourceLocation(DiamondIsUncraftable.MOD_ID, "killer_queen").toString()));

    public static final RegistryObject<EntityType<PurpleHazeEntity>> PURPLE_HAZE = ENTITY_TYPES
            .register("purple_haze",
                    () -> EntityType.Builder.create(PurpleHazeEntity::new, EntityClassification.CREATURE)
                            .disableSummoning().size(1.2f, 2.7f)
                            .build(new ResourceLocation(DiamondIsUncraftable.MOD_ID, "purple_haze").toString()));

    public static final RegistryObject<EntityType<TheWorldEntity>> THE_WORLD = ENTITY_TYPES
            .register("the_world",
                    () -> EntityType.Builder.create(TheWorldEntity::new, EntityClassification.CREATURE)
                            .disableSummoning().size(1.2f, 2.7f)
                            .build(new ResourceLocation(DiamondIsUncraftable.MOD_ID, "the_world").toString()));

    public static final RegistryObject<EntityType<SilverChariotEntity>> SILVER_CHARIOT = ENTITY_TYPES
            .register("silver_chariot",
                    () -> EntityType.Builder.create(SilverChariotEntity::new, EntityClassification.CREATURE)
                            .disableSummoning().size(1.2f, 2.7f)
                            .build(new ResourceLocation(DiamondIsUncraftable.MOD_ID, "silver_chariot").toString()));

    public static final RegistryObject<EntityType<SilverChariotSwordEntity>> SILVER_CHARIOT_SWORD = ENTITY_TYPES
            .register("silver_chariot_sword",
                    () -> EntityType.Builder.<SilverChariotSwordEntity>create(SilverChariotSwordEntity::new, EntityClassification.MISC)
                            .disableSummoning().size(0.3f, 0.2f)
                            .build(new ResourceLocation(DiamondIsUncraftable.MOD_ID, "silver_chariot_sword").toString()));

    public static final RegistryObject<EntityType<SheerHeartAttackEntity>> SHEER_HEART_ATTACK = ENTITY_TYPES
            .register("sheer_heart_attack",
                    () -> EntityType.Builder.<SheerHeartAttackEntity>create(SheerHeartAttackEntity::new, EntityClassification.MISC)
                            .disableSummoning().size(0.3f, 0.2f)
                            .build(new ResourceLocation(DiamondIsUncraftable.MOD_ID, "sheer_heart_attack").toString()));

    public static final RegistryObject<EntityType<StandArrowEntity>> STAND_ARROW = ENTITY_TYPES
            .register("stand_arrow",
                    () -> EntityType.Builder.<StandArrowEntity>create(StandArrowEntity::new, EntityClassification.MISC)
                            .disableSummoning().size(0.5f, 0.5f)
                            .build(new ResourceLocation(DiamondIsUncraftable.MOD_ID, "stand_arrow").toString()));

    public static final RegistryObject<EntityType<EmeraldSplashEntity>> EMERALD_SPLASH = ENTITY_TYPES
            .register("emerald_splash",
                    () -> EntityType.Builder.<EmeraldSplashEntity>create(EmeraldSplashEntity::new, EntityClassification.MISC)
                            .disableSummoning().size(0.2f, 0.2f)
                            .build(new ResourceLocation(DiamondIsUncraftable.MOD_ID, "emerald_splash").toString()));

    public static final RegistryObject<EntityType<StickyFingersEntity>> STICKY_FINGERS = ENTITY_TYPES
            .register("sticky_fingers",
                    () -> EntityType.Builder.create(StickyFingersEntity::new, EntityClassification.CREATURE)
                            .disableSummoning().size(1.2f, 2.7f)
                            .build(new ResourceLocation(DiamondIsUncraftable.MOD_ID, "sticky_fingers").toString()));
}