package io.github.halffocused.diamond_is_uncraftable.client.entity.render;

import io.github.halffocused.diamond_is_uncraftable.client.entity.model.GreenDayModel;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.GreenDayEntity;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class GreenDayRenderer extends AbstractStandRenderer<GreenDayEntity, GreenDayModel> {
    public GreenDayRenderer(EntityRendererManager manager) {
        super(manager, new GreenDayModel());
    }

    @Override
    public ResourceLocation getEntityTexture(GreenDayEntity entity) {
        return Util.ResourceLocations.GREEN_DAY;
    }
}
