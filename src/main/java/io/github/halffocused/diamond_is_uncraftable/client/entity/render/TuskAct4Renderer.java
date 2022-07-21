package io.github.halffocused.diamond_is_uncraftable.client.entity.render;

import io.github.halffocused.diamond_is_uncraftable.client.entity.model.TuskAct4Model;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.TuskAct4Entity;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class TuskAct4Renderer extends AbstractStandRenderer<TuskAct4Entity, TuskAct4Model> {
    public TuskAct4Renderer(EntityRendererManager renderManagerIn) {
        super(renderManagerIn, new TuskAct4Model());
    }

    @Override
    public ResourceLocation getEntityTexture(TuskAct4Entity entity) {
        return Util.ResourceLocations.TUSK_ACT_4;
    }
}

