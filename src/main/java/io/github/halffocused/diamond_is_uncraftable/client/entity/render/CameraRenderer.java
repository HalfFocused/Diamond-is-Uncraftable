package io.github.halffocused.diamond_is_uncraftable.client.entity.render;

import io.github.halffocused.diamond_is_uncraftable.client.entity.model.CameraModel;
import io.github.halffocused.diamond_is_uncraftable.entity.CameraEntity;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.util.ResourceLocation;

public class CameraRenderer extends LivingRenderer<CameraEntity, CameraModel> {
    public CameraRenderer(EntityRendererManager renderManagerIn) {
        super(renderManagerIn, new CameraModel(), 0);
    }

    @Override
    public ResourceLocation getEntityTexture(CameraEntity entity) {
        return Util.ResourceLocations.CMOON;
    }
}

