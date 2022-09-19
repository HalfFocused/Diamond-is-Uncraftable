package io.github.halffocused.diamond_is_uncraftable.client.entity.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import io.github.halffocused.diamond_is_uncraftable.client.entity.model.PurpleHazeModel;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.PurpleHazeEntity;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

import javax.annotation.Nullable;

public class PurpleHazeRenderer extends GeoEntityRenderer<PurpleHazeEntity> {


    public PurpleHazeRenderer(EntityRendererManager renderManager) {
        super(renderManager, new PurpleHazeModel()); }

    @Override
    public ResourceLocation getEntityTexture(PurpleHazeEntity entity) {
        return new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/purple_haze.png");
    }

}

