package io.github.halffocused.diamond_is_uncraftable.client.entity.render;

import io.github.halffocused.diamond_is_uncraftable.client.entity.model.AerosmithModel;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.AerosmithEntity;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class AerosmithRenderer extends AbstractStandRenderer<AerosmithEntity, AerosmithModel> {
    public AerosmithRenderer(EntityRendererManager manager) {
        super(manager, new AerosmithModel());
    }

    @Override
    public ResourceLocation getEntityTexture(AerosmithEntity entity) {
        return Util.ResourceLocations.AEROSMITH;
    }
}
