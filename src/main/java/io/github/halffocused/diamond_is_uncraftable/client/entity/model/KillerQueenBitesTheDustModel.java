package io.github.halffocused.diamond_is_uncraftable.client.entity.model;

import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.KillerQueenBitesTheDustEntity;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.KillerQueenEntity;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class KillerQueenBitesTheDustModel extends AnimatedGeoModel<KillerQueenBitesTheDustEntity> {

    @Override
    public ResourceLocation getAnimationFileLocation(KillerQueenBitesTheDustEntity entity) {
        return new ResourceLocation(DiamondIsUncraftable.MOD_ID, "animations/killerqueen.animation.json");
    }

    @Override
    public ResourceLocation getModelLocation(KillerQueenBitesTheDustEntity entity) {
        return new ResourceLocation(DiamondIsUncraftable.MOD_ID, "geo/killerqueen.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(KillerQueenBitesTheDustEntity entity) {
        return new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/killer_queen_btd.png");
    }
}