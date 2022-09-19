package io.github.halffocused.diamond_is_uncraftable.client.entity.render;

import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import io.github.halffocused.diamond_is_uncraftable.client.entity.model.KingCrimsonModel;
import io.github.halffocused.diamond_is_uncraftable.client.entity.model.TheWorldModel;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.KingCrimsonEntity;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.TheWorldEntity;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class KingCrimsonRenderer extends GeoEntityRenderer<KingCrimsonEntity> {
    public KingCrimsonRenderer(EntityRendererManager renderManager) {
        super(renderManager, new KingCrimsonModel());
    }

    @Override
    public ResourceLocation getEntityTexture(KingCrimsonEntity entity) {
        return new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/king_crimson.png");
    }
}

