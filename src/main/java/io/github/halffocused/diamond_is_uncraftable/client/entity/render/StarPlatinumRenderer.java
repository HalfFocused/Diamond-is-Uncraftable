package io.github.halffocused.diamond_is_uncraftable.client.entity.render;

import io.github.halffocused.diamond_is_uncraftable.client.entity.model.StarPlatinumModel;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.StarPlatinumEntity;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class StarPlatinumRenderer extends AbstractStandRenderer<StarPlatinumEntity, StarPlatinumModel> {
    public StarPlatinumRenderer(EntityRendererManager renderManagerIn) {
        super(renderManagerIn, new StarPlatinumModel());
    }

    @Override
    public ResourceLocation getEntityTexture(StarPlatinumEntity entity) {
        return Util.ResourceLocations.STAR_PLATINUM;
    }
}

