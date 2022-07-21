package io.github.halffocused.diamond_is_uncraftable.client.entity.render;

import io.github.halffocused.diamond_is_uncraftable.entity.StandArrowEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.SpectralArrowRenderer;
import net.minecraft.util.ResourceLocation;

public class StandArrowRenderer extends ArrowRenderer<StandArrowEntity> {
    public StandArrowRenderer(EntityRendererManager manager) {
        super(manager);
    }

    @Override
    public ResourceLocation getEntityTexture(StandArrowEntity entity) {
        return SpectralArrowRenderer.RES_SPECTRAL_ARROW;
    }
}
