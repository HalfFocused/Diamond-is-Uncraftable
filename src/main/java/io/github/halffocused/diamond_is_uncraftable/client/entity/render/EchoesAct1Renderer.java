package io.github.halffocused.diamond_is_uncraftable.client.entity.render;

import io.github.halffocused.diamond_is_uncraftable.client.entity.model.EchoesAct1Model;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.EchoesAct1Entity;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class EchoesAct1Renderer extends AbstractStandRenderer<EchoesAct1Entity, EchoesAct1Model> {
    public EchoesAct1Renderer(EntityRendererManager manager) {
        super(manager, new EchoesAct1Model());
    }

    @Override
    public ResourceLocation getEntityTexture(EchoesAct1Entity entity) {
        return Util.ResourceLocations.ECHOES_ACT_1;
    }
}
