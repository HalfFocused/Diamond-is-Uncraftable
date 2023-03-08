package io.github.halffocused.diamond_is_uncraftable.client.entity.render;

import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import io.github.halffocused.diamond_is_uncraftable.client.entity.model.KillerQueenBitesTheDustModel;
import io.github.halffocused.diamond_is_uncraftable.client.entity.model.KillerQueenModel;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.KillerQueenBitesTheDustEntity;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.KillerQueenEntity;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class KillerQueenBitesTheDustRenderer extends GeoEntityRenderer<KillerQueenBitesTheDustEntity> {
    public KillerQueenBitesTheDustRenderer(EntityRendererManager renderManager) {
        super(renderManager, new KillerQueenBitesTheDustModel());
    }

    @Override
    public ResourceLocation getEntityTexture(KillerQueenBitesTheDustEntity entity) {
        return new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/killer_queen_btd.png");
    }
}

