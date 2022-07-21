package io.github.halffocused.diamond_is_uncraftable.client.entity.render;

import io.github.halffocused.diamond_is_uncraftable.client.entity.model.CrazyDiamondModel;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.CrazyDiamondEntity;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class CrazyDiamondRenderer extends AbstractStandRenderer<CrazyDiamondEntity, CrazyDiamondModel> {
    public CrazyDiamondRenderer(EntityRendererManager renderManagerIn) {
        super(renderManagerIn, new CrazyDiamondModel());
    }

    @Override
    public ResourceLocation getEntityTexture(CrazyDiamondEntity entity) {
        return Util.ResourceLocations.CRAZY_DIAMOND;
    }
}

