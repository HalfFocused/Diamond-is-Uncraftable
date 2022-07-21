package io.github.halffocused.diamond_is_uncraftable.client.entity.render;

import io.github.halffocused.diamond_is_uncraftable.client.entity.model.TheHandModel;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.TheHandEntity;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class TheHandRenderer extends AbstractStandRenderer<TheHandEntity, TheHandModel> {
    public TheHandRenderer(EntityRendererManager renderManagerIn) {
        super(renderManagerIn, new TheHandModel());
    }

    @Override
    public ResourceLocation getEntityTexture(TheHandEntity entity) {
        return Util.ResourceLocations.THE_HAND;
    }
}

