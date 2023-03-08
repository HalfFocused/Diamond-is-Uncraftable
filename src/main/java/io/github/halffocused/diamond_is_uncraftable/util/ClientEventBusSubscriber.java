package io.github.halffocused.diamond_is_uncraftable.util;

import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import io.github.halffocused.diamond_is_uncraftable.client.entity.model.*;
import io.github.halffocused.diamond_is_uncraftable.client.entity.render.*;
import io.github.halffocused.diamond_is_uncraftable.event.EventClientTick;
import io.github.halffocused.diamond_is_uncraftable.event.EventHandleKeybinds;
import io.github.halffocused.diamond_is_uncraftable.init.EntityInit;
import io.github.halffocused.diamond_is_uncraftable.init.KeyInit;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = DiamondIsUncraftable.MOD_ID, bus = Bus.MOD, value = Dist.CLIENT)
public class ClientEventBusSubscriber {
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        KeyInit.register();
        MinecraftForge.EVENT_BUS.register(EventHandleKeybinds.class);
        MinecraftForge.EVENT_BUS.register(EventClientTick.class);

        RenderingRegistry.registerEntityRenderingHandler(EntityInit.KING_CRIMSON.get(), KingCrimsonRenderer::new);

        RenderingRegistry.registerEntityRenderingHandler(EntityInit.KILLER_QUEEN.get(), KillerQueenRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityInit.KILLER_QUEEN_BTD.get(), KillerQueenBitesTheDustRenderer::new);

        RenderingRegistry.registerEntityRenderingHandler(EntityInit.SHEER_HEART_ATTACK.get(), SheerHeartAttackRenderer::new);

        RenderingRegistry.registerEntityRenderingHandler(EntityInit.PURPLE_HAZE.get(), PurpleHazeRenderer::new);

        RenderingRegistry.registerEntityRenderingHandler(EntityInit.THE_WORLD.get(), TheWorldRenderer::new);

        RenderingRegistry.registerEntityRenderingHandler(EntityInit.SILVER_CHARIOT.get(), SilverChariotRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityInit.SILVER_CHARIOT_SWORD.get(), manager -> new StandAttackRenderer<>(manager, new SilverChariotSwordModel()));

        RenderingRegistry.registerEntityRenderingHandler(EntityInit.STAND_ARROW.get(), StandArrowRenderer::new);

        RenderingRegistry.registerEntityRenderingHandler(EntityInit.EMERALD_SPLASH.get(), manager -> new EmeraldSplashRenderer(manager, event.getMinecraftSupplier().get().getItemRenderer())); //This renders as an emerald.

        RenderingRegistry.registerEntityRenderingHandler(EntityInit.STICKY_FINGERS.get(), StickyFingersRenderer::new);

        EventClientTick.init(event.getMinecraftSupplier().get());
    }
}
