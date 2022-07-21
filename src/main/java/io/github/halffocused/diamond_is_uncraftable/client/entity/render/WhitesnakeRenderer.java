package io.github.halffocused.diamond_is_uncraftable.client.entity.render;

import io.github.halffocused.diamond_is_uncraftable.client.entity.model.WhitesnakeModel;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.WhitesnakeEntity;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class WhitesnakeRenderer extends AbstractStandRenderer<WhitesnakeEntity, WhitesnakeModel> {
    public WhitesnakeRenderer(EntityRendererManager renderManagerIn) {
        super(renderManagerIn, new WhitesnakeModel());
    }

    @Override
    public ResourceLocation getEntityTexture(WhitesnakeEntity entity) {
        return Util.ResourceLocations.WHITESNAKE;
    }
}

