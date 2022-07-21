package io.github.halffocused.diamond_is_uncraftable.client.entity.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.halffocused.diamond_is_uncraftable.capability.Stand;
import io.github.halffocused.diamond_is_uncraftable.client.entity.model.SheerHeartAttackModel;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.AbstractStandEntity;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.SheerHeartAttackEntity;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class SheerHeartAttackRenderer extends StandAttackRenderer<SheerHeartAttackEntity> {
    public SheerHeartAttackRenderer(EntityRendererManager renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    public void render(@Nonnull SheerHeartAttackEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        if (!(Minecraft.getInstance().renderViewEntity instanceof PlayerEntity) && !(Minecraft.getInstance().renderViewEntity instanceof AbstractStandEntity))
            return;
        SheerHeartAttackModel shaModel = new SheerHeartAttackModel();

        if (Minecraft.getInstance().renderViewEntity instanceof AbstractStandEntity)
            render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn, shaModel);
        else Stand.getLazyOptional((PlayerEntity) Minecraft.getInstance().renderViewEntity).ifPresent(props -> {
            if (props.getStandID() != 0)
                super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn, shaModel);
        });
    }

    @Override
    public ResourceLocation getEntityTexture(SheerHeartAttackEntity entity) {
        return Util.ResourceLocations.SHEER_HEART_ATTACK;
    }
}

