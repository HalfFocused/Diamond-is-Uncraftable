package io.github.halffocused.diamond_is_uncraftable.client.entity.render;

import io.github.halffocused.diamond_is_uncraftable.client.entity.model.TheGratefulDeadModel;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.TheGratefulDeadEntity;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class TheGratefulDeadRenderer extends AbstractStandRenderer<TheGratefulDeadEntity, TheGratefulDeadModel> {
    public TheGratefulDeadRenderer(EntityRendererManager renderManagerIn) {
        super(renderManagerIn, new TheGratefulDeadModel());
    }

    @Override
    public ResourceLocation getEntityTexture(TheGratefulDeadEntity entity) {
        return Util.ResourceLocations.THE_GRATEFUL_DEAD;
    }
}

