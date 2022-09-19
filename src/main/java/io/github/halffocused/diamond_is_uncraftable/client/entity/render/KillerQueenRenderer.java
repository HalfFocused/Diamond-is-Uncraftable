package io.github.halffocused.diamond_is_uncraftable.client.entity.render;

import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import io.github.halffocused.diamond_is_uncraftable.client.entity.model.KillerQueenModel;
import io.github.halffocused.diamond_is_uncraftable.client.entity.model.KingCrimsonModel;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.KillerQueenEntity;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.KingCrimsonEntity;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class KillerQueenRenderer extends GeoEntityRenderer<KillerQueenEntity> {
    public KillerQueenRenderer(EntityRendererManager renderManager) {
        super(renderManager, new KillerQueenModel());
    }

    @Override
    public ResourceLocation getEntityTexture(KillerQueenEntity entity) {
        return new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/killer_queen.png");
    }
}

