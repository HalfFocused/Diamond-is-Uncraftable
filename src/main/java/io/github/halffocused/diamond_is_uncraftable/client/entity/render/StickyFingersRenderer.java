package io.github.halffocused.diamond_is_uncraftable.client.entity.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.halffocused.diamond_is_uncraftable.client.entity.model.StickyFingersModel;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.StickyFingersEntity;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class StickyFingersRenderer extends AbstractStandRenderer<StickyFingersEntity, StickyFingersModel> {
    public StickyFingersRenderer(EntityRendererManager renderManagerIn) {
        super(renderManagerIn, new StickyFingersModel());
    }

    @Override
    public void render(StickyFingersEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        if (entityIn.disguiseEntity == null)
            super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    @Override
    public ResourceLocation getEntityTexture(StickyFingersEntity entity) {
        return Util.ResourceLocations.STICKY_FINGERS;
    }
}

