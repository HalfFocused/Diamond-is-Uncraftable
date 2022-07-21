package io.github.halffocused.diamond_is_uncraftable.client.entity.render;

import io.github.halffocused.diamond_is_uncraftable.client.entity.model.GoldExperienceRequiemModel;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.GoldExperienceRequiemEntity;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class GoldExperienceRequiemRenderer extends AbstractStandRenderer<GoldExperienceRequiemEntity, GoldExperienceRequiemModel> {
    public GoldExperienceRequiemRenderer(EntityRendererManager renderManagerIn) {
        super(renderManagerIn, new GoldExperienceRequiemModel());
    }

    @Override
    public ResourceLocation getEntityTexture(GoldExperienceRequiemEntity entity) {
        return Util.ResourceLocations.GER;
    }
}

