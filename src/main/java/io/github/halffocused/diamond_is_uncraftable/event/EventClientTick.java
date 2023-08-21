package io.github.halffocused.diamond_is_uncraftable.event;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import io.github.halffocused.diamond_is_uncraftable.capability.Stand;
import io.github.halffocused.diamond_is_uncraftable.client.gui.*;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.StickyFingersEntity;
import io.github.halffocused.diamond_is_uncraftable.item.StandDiscItem;
import io.github.halffocused.diamond_is_uncraftable.network.message.client.CAerosmithRotationPacket;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.stream.StreamSupport;

@SuppressWarnings("ConstantConditions")
@Mod.EventBusSubscriber(modid = DiamondIsUncraftable.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class EventClientTick {

    private static EventClientTick instance = null;


    Minecraft mc;


    private EventClientTick(Minecraft mc) {
        this.mc = mc;
    }

    public static void init(Minecraft mc) {
        if (instance == null) {
            instance = new EventClientTick(mc);
            MinecraftForge.EVENT_BUS.register(instance);
        }
    }


    float partialTickStoppedAt;

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (Minecraft.getInstance().player == null) return;
        ClientPlayerEntity player = Minecraft.getInstance().player;

        Stand.getLazyOptional(player).ifPresent(stand -> {

            if (Minecraft.getInstance().world == null) return;

            if (stand.getExperiencingTimeStop()) {
                Minecraft.getInstance().gameRenderer.loadShader(new ResourceLocation(DiamondIsUncraftable.MOD_ID, "shaders/timestop.json"));
            } else {
                Minecraft.getInstance().gameRenderer.stopUseShader();
            }
            if (stand.getInstantTimeStopFrame() > 0) {
                Minecraft.getInstance().gameRenderer.loadShader(new ResourceLocation(DiamondIsUncraftable.MOD_ID, "shaders/timestop.json"));
            }
            if (!player.isSpectator() && !stand.getStandOn())
                if (Minecraft.getInstance().renderViewEntity != player)
                    Minecraft.getInstance().setRenderViewEntity(player);
        });
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void renderGameOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;
        new TimeSkipEffectGUI().renderEffect();
        new BitesTheDustGUI().render(event.getMatrixStack());
        new BitesTheDustEffectGUI().renderEffect();
    }

    @SubscribeEvent //This one still bugs me to this day, can't think of a way to automate it.
    public static void tooltipEvent(ItemTooltipEvent event) {
        if (!(event.getItemStack().getItem() instanceof StandDiscItem)) return;
        String standName = "";
        if (event.getItemStack().getTag() != null)
            switch (event.getItemStack().getTag().getInt("StandID")) {
                case Util.StandID.KING_CRIMSON: {
                    standName = "King Crimson";
                    break;
                }
                case Util.StandID.KILLER_QUEEN: {
                    standName = "Killer Queen";
                    break;
                }
                case Util.StandID.KILLER_QUEEN_BTD: {
                    standName = "Killer Queen: Bites The Dust";
                    break;
                }
                case Util.StandID.THE_WORLD: {
                    standName = "The World";
                    break;
                }
                case Util.StandID.SILVER_CHARIOT: {
                    standName = "Silver Chariot";
                    break;
                }
            }
        if (!standName.equals(""))
            event.getToolTip().add(new StringTextComponent(standName));
    }

    @SubscribeEvent
    public void onRenderExperienceBar(RenderGameOverlayEvent.Pre event)
    {
        if (event.getType() == RenderGameOverlayEvent.ElementType.EXPERIENCE && !mc.player.isRidingHorse())
        {
            Stand.getLazyOptional(mc.player).ifPresent(stand -> {
                if(stand.getStandID() != 0){
                    if(stand.getStandOn()) {
                        event.setCanceled(true);
                    }
                    new StandEnergyGUI().renderEnergyBar(event.getMatrixStack());
                }
            });
        }
    }

    @SubscribeEvent
    public void entityFogDensityEvent(EntityViewRenderEvent.FogDensity event){
        if(event.getInfo().getRenderViewEntity() != null){
            if(event.getInfo().getRenderViewEntity() instanceof PlayerEntity){
                Stand.getLazyOptional(mc.player).ifPresent(stand -> {
                    if(stand.getExperiencingTimeSkip()){
                        event.setCanceled(true);
                        event.setDensity(stand.getStandID() == Util.StandID.KING_CRIMSON ? 0.15f : 0.25f);
                    }
                });
            }
        }
    }

    @SubscribeEvent
    public void entityFogColorEvent(EntityViewRenderEvent.FogColors event){
        if(event.getInfo().getRenderViewEntity() != null){
            if(event.getInfo().getRenderViewEntity() instanceof PlayerEntity){
                Stand.getLazyOptional(mc.player).ifPresent(stand -> {
                    if(stand.getExperiencingTimeSkip()){
                        event.setRed(0f);
                        event.setGreen(0f);
                        event.setBlue(0f);
                    }
                });
            }
        }
    }

}
