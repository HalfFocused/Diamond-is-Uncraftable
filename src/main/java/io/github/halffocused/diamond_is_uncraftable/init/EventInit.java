package io.github.halffocused.diamond_is_uncraftable.init;

import io.github.halffocused.diamond_is_uncraftable.entity.stand.KingCrimsonEntity;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.StarPlatinumEntity;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.TheWorldEntity;
import io.github.halffocused.diamond_is_uncraftable.event.*;
import io.github.halffocused.diamond_is_uncraftable.util.timestop.TimestopHelper;
import net.minecraftforge.eventbus.api.IEventBus;

import javax.annotation.Nonnull;

public class EventInit {
    public static void registerForgeBus(@Nonnull IEventBus bus) {
        bus.register(EventAttachCapabilities.class);
        bus.register(EventD4CTeleportProcessor.class);
        bus.register(EventSyncCapability.class);
        bus.register(EventAbilityGER.class);
        bus.register(EventHandleStandAbilities.class);
        bus.register(TimestopHelper.class);
    }
}
