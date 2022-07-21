package io.github.halffocused.diamond_is_uncraftable.client.entity.render;

import io.github.halffocused.diamond_is_uncraftable.client.entity.model.MagiciansRedModel;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.MagiciansRedEntity;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class MagiciansRedRenderer extends AbstractStandRenderer<MagiciansRedEntity, MagiciansRedModel> {
    public MagiciansRedRenderer(EntityRendererManager renderManagerIn) {
        super(renderManagerIn, new MagiciansRedModel());
    }

    @Override
    public ResourceLocation getEntityTexture(MagiciansRedEntity entity) {
        return Util.ResourceLocations.MAGICIANS_RED;
    }
}

