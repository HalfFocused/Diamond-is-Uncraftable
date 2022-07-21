package io.github.halffocused.diamond_is_uncraftable.client.entity.render;

import io.github.halffocused.diamond_is_uncraftable.client.entity.model.HierophantGreenModel;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.HierophantGreenEntity;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class HierophantGreenRenderer extends AbstractStandRenderer<HierophantGreenEntity, HierophantGreenModel> {
    public HierophantGreenRenderer(EntityRendererManager renderManagerIn) {
        super(renderManagerIn, new HierophantGreenModel());
    }

    @Override
    public ResourceLocation getEntityTexture(HierophantGreenEntity entity) {
        return Util.ResourceLocations.HIEROPHANT_GREEN;
    }
}

