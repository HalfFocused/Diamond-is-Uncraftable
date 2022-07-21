package io.github.halffocused.diamond_is_uncraftable.init;

import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import io.github.halffocused.diamond_is_uncraftable.world.dimension.D4CDimensionTypeEnd;
import io.github.halffocused.diamond_is_uncraftable.world.dimension.D4CDimensionTypeNether;
import io.github.halffocused.diamond_is_uncraftable.world.dimension.D4CDimensionType;
import io.github.halffocused.diamond_is_uncraftable.world.dimension.MadeInHeavenDimensionType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ModDimension;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Deprecated //Replace with .json dimensions in 1.16.
public class DimensionInit {
    public static DeferredRegister<ModDimension> DIMENSIONS = new DeferredRegister<>(ForgeRegistries.MOD_DIMENSIONS, DiamondIsUncraftable.MOD_ID);


    public static final RegistryObject<ModDimension> D4C_DIMENSION = DIMENSIONS.register("d4c_dimension", D4CDimensionType::new);
    public static final RegistryObject<ModDimension> D4C_DIMENSION_NETHER = DIMENSIONS.register("d4c_dimension_nether", D4CDimensionTypeNether::new);
    public static final RegistryObject<ModDimension> D4C_DIMENSION_END = DIMENSIONS.register("d4c_dimension_end", D4CDimensionTypeEnd::new);
    public static final RegistryObject<ModDimension> MADE_IN_HEAVEN_DIMENSION = DIMENSIONS.register("made_in_heaven_dimension", MadeInHeavenDimensionType::new);

    public static final ResourceLocation D4C_DIMENSION_TYPE = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "d4c_dimension_overworld");
    public static final ResourceLocation D4C_DIMENSION_TYPE_NETHER = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "d4c_dimension_nether");
    public static final ResourceLocation D4C_DIMENSION_TYPE_END = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "d4c_dimension_end");
    public static final ResourceLocation MADE_IN_HEAVEN_DIMENSION_TYPE = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "made_in_heaven_dimension");
}
