package io.github.halffocused.diamond_is_uncraftable.client.entity.render;

import io.github.halffocused.diamond_is_uncraftable.client.entity.model.CMoonModel;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.CMoonEntity;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class CMoonRenderer extends AbstractStandRenderer<CMoonEntity, CMoonModel> {
    public CMoonRenderer(EntityRendererManager renderManagerIn) {
        super(renderManagerIn, new CMoonModel());
    }

    @Override
    public ResourceLocation getEntityTexture(CMoonEntity entity) {
        return Util.ResourceLocations.CMOON;
    }
}

