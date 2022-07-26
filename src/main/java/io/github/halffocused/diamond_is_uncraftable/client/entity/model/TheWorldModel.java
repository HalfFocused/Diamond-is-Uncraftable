package io.github.halffocused.diamond_is_uncraftable.client.entity.model;

import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import io.github.halffocused.diamond_is_uncraftable.capability.Stand;
import io.github.halffocused.diamond_is_uncraftable.config.DiamondIsUncraftableConfig;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.TheWorldEntity;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.model.AnimatedGeoModel;

import java.util.concurrent.atomic.AtomicReference;

public class TheWorldModel extends AnimatedGeoModel<TheWorldEntity> {

    @Override
    public ResourceLocation getAnimationFileLocation(TheWorldEntity entity) {
        return new ResourceLocation(DiamondIsUncraftable.MOD_ID, "animations/theworld.animation.json");
    }

    @Override
    public ResourceLocation getModelLocation(TheWorldEntity entity) {
        return new ResourceLocation(DiamondIsUncraftable.MOD_ID, "geo/theworld.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(TheWorldEntity entity) {
        return new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/the_world.png");
    }
}