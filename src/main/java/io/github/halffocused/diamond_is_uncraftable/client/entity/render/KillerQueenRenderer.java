package io.github.halffocused.diamond_is_uncraftable.client.entity.render;

import io.github.halffocused.diamond_is_uncraftable.client.entity.model.KillerQueenModel;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class KillerQueenRenderer extends GeoEntityRenderer {
    public KillerQueenRenderer(EntityRendererManager renderManager) {
        super(renderManager, new KillerQueenModel());
    }
}

