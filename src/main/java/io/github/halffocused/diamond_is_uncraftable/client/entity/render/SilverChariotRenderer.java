package io.github.halffocused.diamond_is_uncraftable.client.entity.render;

import io.github.halffocused.diamond_is_uncraftable.client.entity.model.SilverChariotModel;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class SilverChariotRenderer extends GeoEntityRenderer {


    public SilverChariotRenderer(EntityRendererManager renderManager) {
        super(renderManager, new SilverChariotModel());
    }

}

