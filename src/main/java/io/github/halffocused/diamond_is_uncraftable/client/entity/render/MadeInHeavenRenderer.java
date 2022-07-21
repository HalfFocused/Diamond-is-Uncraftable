package io.github.halffocused.diamond_is_uncraftable.client.entity.render;

import io.github.halffocused.diamond_is_uncraftable.client.entity.model.MadeInHeavenModel;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.MadeInHeavenEntity;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class MadeInHeavenRenderer extends AbstractStandRenderer<MadeInHeavenEntity, MadeInHeavenModel> {
    public MadeInHeavenRenderer(EntityRendererManager renderManagerIn) {
        super(renderManagerIn, new MadeInHeavenModel());
    }

    @Override
    public ResourceLocation getEntityTexture(MadeInHeavenEntity entity) {
        return Util.ResourceLocations.MADE_IN_HEAVEN;
    }
}

