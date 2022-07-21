package io.github.halffocused.diamond_is_uncraftable.client.entity.render;

import io.github.halffocused.diamond_is_uncraftable.client.entity.model.TuskAct2Model;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.TuskAct2Entity;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class TuskAct2Renderer extends AbstractStandRenderer<TuskAct2Entity, TuskAct2Model> {
    public TuskAct2Renderer(EntityRendererManager renderManagerIn) {
        super(renderManagerIn, new TuskAct2Model());
    }

    @Override
    public ResourceLocation getEntityTexture(TuskAct2Entity entity) {
        return Util.ResourceLocations.TUSK_ACT_2;
    }
}

