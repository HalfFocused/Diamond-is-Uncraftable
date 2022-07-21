package io.github.halffocused.diamond_is_uncraftable.particle;

import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DiamondIsUncraftable.MOD_ID,bus = Mod.EventBusSubscriber.Bus.MOD)
public class ParticleUtil {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerParticles(ParticleFactoryRegisterEvent event){
        Minecraft.getInstance().particles.registerFactory(ParticleList.MENACING_PARTICLE.get(), MenacingParticle.Factory::new);
        Minecraft.getInstance().particles.registerFactory(ParticleList.ZIPPER.get(), ZipperParticle.Factory::new);
    }

}
