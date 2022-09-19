package io.github.halffocused.diamond_is_uncraftable.client.entity.render;

import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import io.github.halffocused.diamond_is_uncraftable.client.entity.model.TheWorldModel;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.AbstractStandEntity;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.TheWorldEntity;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class TheWorldRenderer extends GeoEntityRenderer<TheWorldEntity> {
    public TheWorldRenderer(EntityRendererManager renderManager) {
        super(renderManager, new TheWorldModel());
    }

    @Override
    public ResourceLocation getEntityTexture(TheWorldEntity entity) {
        return new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/the_world.png");
    }
}