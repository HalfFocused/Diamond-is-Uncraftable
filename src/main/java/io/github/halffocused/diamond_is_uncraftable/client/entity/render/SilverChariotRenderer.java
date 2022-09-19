package io.github.halffocused.diamond_is_uncraftable.client.entity.render;

import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import io.github.halffocused.diamond_is_uncraftable.client.entity.model.SilverChariotModel;
import io.github.halffocused.diamond_is_uncraftable.client.entity.model.TheWorldModel;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.SilverChariotEntity;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.TheWorldEntity;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class SilverChariotRenderer extends GeoEntityRenderer<SilverChariotEntity> {
    public SilverChariotRenderer(EntityRendererManager renderManager) {
        super(renderManager, new SilverChariotModel());
    }

    @Override
    public ResourceLocation getEntityTexture(SilverChariotEntity entity) {
        return new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/silver_chariot.png");
    }
}

