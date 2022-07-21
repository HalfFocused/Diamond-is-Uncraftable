package io.github.halffocused.diamond_is_uncraftable.client.entity.render;

import io.github.halffocused.diamond_is_uncraftable.client.entity.model.KingCrimsonModel;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class KingCrimsonRenderer extends GeoEntityRenderer {


    public KingCrimsonRenderer(EntityRendererManager renderManager) {
        super(renderManager, new KingCrimsonModel());
    }

}

