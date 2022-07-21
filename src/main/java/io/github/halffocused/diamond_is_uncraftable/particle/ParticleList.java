package io.github.halffocused.diamond_is_uncraftable.particle;

import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ParticleList {

    public static final DeferredRegister<ParticleType<?>> PARTICLES = new DeferredRegister<>(ForgeRegistries.PARTICLE_TYPES, DiamondIsUncraftable.MOD_ID);

    public static final RegistryObject<BasicParticleType> MENACING_PARTICLE = PARTICLES.register("menacing_particle",() -> new BasicParticleType(true));

    public static final RegistryObject<BasicParticleType> ZIPPER = PARTICLES.register("zipper_particle",() -> new BasicParticleType(true));

}
