package io.github.halffocused.diamond_is_uncraftable.event;

import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import io.github.halffocused.diamond_is_uncraftable.capability.Stand;
import io.github.halffocused.diamond_is_uncraftable.capability.StandPerWorldCapability;
import io.github.halffocused.diamond_is_uncraftable.capability.Timestop;
import io.github.halffocused.diamond_is_uncraftable.config.DiamondIsUncraftableConfig;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.*;
import io.github.halffocused.diamond_is_uncraftable.network.message.server.SSyncStandCapabilityPacket;
import io.github.halffocused.diamond_is_uncraftable.util.timestop.TimestopHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.*;

import static io.github.halffocused.diamond_is_uncraftable.util.Util.StandID.*;

@Mod.EventBusSubscriber(modid = DiamondIsUncraftable.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EventSyncCapability {
    @SubscribeEvent
    public static void saveStand(PlayerEvent.Clone event) {
        if (!event.isWasDeath() || DiamondIsUncraftableConfig.COMMON.saveStandOnDeath.get()) {
            Stand.getLazyOptional(event.getOriginal()).ifPresent(originalProps -> {
                ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
                Stand.getLazyOptional(player).ifPresent(newProps -> newProps.clone(originalProps));
            });
        }

        if(event.isWasDeath() && !DiamondIsUncraftableConfig.COMMON.saveStandOnDeath.get()){
            if(DiamondIsUncraftableConfig.COMMON.uniqueStandMode.get()){
                Objects.requireNonNull(event.getPlayer().world.getServer()).getWorld(DimensionType.OVERWORLD);
                StandPerWorldCapability.getLazyOptional(event.getPlayer().world.getServer().getWorld(DimensionType.OVERWORLD)).ifPresent(uniqueStandHandler -> {
                    ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
                    Stand stand = Stand.getCapabilityFromPlayer(player);
                    List standAssignments = Collections.singletonList(STANDS);
                    if(uniqueStandHandler.getTakenStandIDs().contains(standAssignments.indexOf(stand.getStandID()))){
                        uniqueStandHandler.removeTakenStandId(standAssignments.indexOf(stand.getStandID()));
                    }
                });
            }
        }
    }

    @SubscribeEvent
    public static void playerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
        Stand.getLazyOptional(player).ifPresent(props -> DiamondIsUncraftable.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new SSyncStandCapabilityPacket(props)));
    }

    @SubscribeEvent
    public static void playerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
        Stand.getLazyOptional(player).ifPresent(props -> DiamondIsUncraftable.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new SSyncStandCapabilityPacket(props)));
    }

    @SubscribeEvent
    public static void playerJoinWorld(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
        Stand.getLazyOptional(player).ifPresent(props -> DiamondIsUncraftable.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new SSyncStandCapabilityPacket(props)));
    }

    @SubscribeEvent
    public static void playerLogOut(PlayerEvent.PlayerLoggedOutEvent event) {
        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
        player.setInvulnerable(false);
        Stand.getLazyOptional(player).ifPresent(props -> { //It's a lot of code to run on logout, but some horrible bugs occur without it.
            if (!player.world.isRemote) {

                //This is completely safe to call on players who cannot stop time. All it does is remove any timestopped chunks matching the player's UUID.
                TimestopHelper.endTimeStop(player);
                Timestop.getLazyOptional(player).ifPresent(Timestop::clear);

                props.setExperiencingTimeStop(false);
                props.setExperiencingTimeSkip(false);

                player.getServerWorld().getEntities()
                        .filter(entity -> entity instanceof SheerHeartAttackEntity)
                        .filter(entity -> ((SheerHeartAttackEntity) entity).getMaster().equals(player))
                        .forEach(Entity::remove);

                player.getServerWorld().getEntities()
                        .filter(entity -> entity instanceof AbstractStandEntity)
                        .filter(entity -> ((AbstractStandEntity) entity).getMaster().equals(player))
                        .forEach(entity -> {
                            if(entity instanceof KingCrimsonEntity){
                                if(((KingCrimsonEntity) entity).timeEraseActive){
                                    ((KingCrimsonEntity) entity).endTimeSkip();
                                }
                            }
                            entity.remove();
                        });

                if(props.getStandID() == PURPLE_HAZE){
                    props.setPreventUnsummon2(false);
                    props.setRage(0);
                }

                DiamondIsUncraftable.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new SSyncStandCapabilityPacket(props));
            }
        });
    }
}
