package io.github.halffocused.diamond_is_uncraftable.client.entity.render;

import io.github.halffocused.diamond_is_uncraftable.client.entity.model.GoldExperienceModel;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.GoldExperienceEntity;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class GoldExperienceRenderer extends AbstractStandRenderer<GoldExperienceEntity, GoldExperienceModel> {
    public GoldExperienceRenderer(EntityRendererManager renderManagerIn) {
        super(renderManagerIn, new GoldExperienceModel());
    }

    @Override
    public ResourceLocation getEntityTexture(GoldExperienceEntity entity) {
        return Util.ResourceLocations.GOLD_EXPERIENCE;
    }
}

