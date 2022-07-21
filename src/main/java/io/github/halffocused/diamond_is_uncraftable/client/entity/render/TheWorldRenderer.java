package io.github.halffocused.diamond_is_uncraftable.client.entity.render;

import io.github.halffocused.diamond_is_uncraftable.client.entity.model.TheWorldModel;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class TheWorldRenderer extends GeoEntityRenderer {
    public TheWorldRenderer(EntityRendererManager renderManager) {
        super(renderManager, new TheWorldModel());
    }
}