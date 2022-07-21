package io.github.halffocused.diamond_is_uncraftable.client.entity.render;

import io.github.halffocused.diamond_is_uncraftable.client.entity.model.D4CModel;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.D4CEntity;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class D4CRenderer extends AbstractStandRenderer<D4CEntity, D4CModel> {
    public D4CRenderer(EntityRendererManager renderManagerIn) {
        super(renderManagerIn, new D4CModel());
    }

    @Override
    public ResourceLocation getEntityTexture(D4CEntity entity) {
        return Util.ResourceLocations.D4C;
    }
}

