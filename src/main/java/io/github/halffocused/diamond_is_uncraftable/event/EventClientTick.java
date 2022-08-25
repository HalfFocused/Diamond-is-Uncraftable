package io.github.halffocused.diamond_is_uncraftable.event;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import io.github.halffocused.diamond_is_uncraftable.capability.Stand;
import io.github.halffocused.diamond_is_uncraftable.client.gui.*;
import io.github.halffocused.diamond_is_uncraftable.config.DiamondIsUncraftableConfig;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.AerosmithEntity;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.HierophantGreenEntity;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.StickyFingersEntity;
import io.github.halffocused.diamond_is_uncraftable.item.StandDiscItem;
import io.github.halffocused.diamond_is_uncraftable.network.message.client.CAerosmithRotationPacket;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import io.github.halffocused.diamond_is_uncraftable.util.timestop.TimestopHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.shader.Shader;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
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

            if (stand.getStandID() == Util.StandID.AEROSMITH && stand.getStandOn() && stand.getAbility())
                StreamSupport.stream(Minecraft.getInstance().world.getAllEntities().spliterator(), false)
                        .filter(entity -> entity instanceof AerosmithEntity)
                        .filter(entity -> ((AerosmithEntity) entity).getMaster() != null)
                        .filter(entity -> ((AerosmithEntity) entity).getMaster().equals(player))
                        .forEach(entity -> {
                            Minecraft.getInstance().setRenderViewEntity(entity);
                            DiamondIsUncraftable.INSTANCE.sendToServer(new CAerosmithRotationPacket(entity.getEntityId(), ((AerosmithEntity) entity).getYaw(1), entity.rotationPitch, ((AerosmithEntity) entity).rotationYawHead));
                        });
            /**
            if (stand.getStandID() == Util.StandID.STICKY_FINGERS && stand.getStandOn())
                StreamSupport.stream(Minecraft.getInstance().world.getAllEntities().spliterator(), false)
                        .filter(entity -> entity instanceof StickyFingersEntity)
                        .filter(entity -> ((StickyFingersEntity) entity).getMaster() != null)
                        .filter(entity -> ((StickyFingersEntity) entity).getMaster().equals(player))
                        .forEach(entity -> {
                            if (((StickyFingersEntity) entity).disguiseEntity != null) {
                                Minecraft.getInstance().setRenderViewEntity(((StickyFingersEntity) entity).disguiseEntity);
                                Minecraft.getInstance().gameSettings.thirdPersonView = 1;
                            } else {
                                Minecraft.getInstance().setRenderViewEntity(player);
                                Minecraft.getInstance().gameSettings.thirdPersonView = 0;
                            }
                        });
             */
            if (stand.getStandID() == Util.StandID.HIEROPHANT_GREEN && stand.getStandOn() && stand.getAbility())
                StreamSupport.stream(Minecraft.getInstance().world.getAllEntities().spliterator(), false)
                        .filter(entity -> entity instanceof HierophantGreenEntity)
                        .filter(entity -> ((HierophantGreenEntity) entity).getMaster() != null)
                        .filter(entity -> ((HierophantGreenEntity) entity).getMaster().equals(player))
                        .forEach(entity -> {
                            if (((HierophantGreenEntity) entity).possessedEntity != null) {
                                Minecraft.getInstance().setRenderViewEntity(((HierophantGreenEntity) entity).possessedEntity);
                                Minecraft.getInstance().gameSettings.thirdPersonView = 1;
                                DiamondIsUncraftable.INSTANCE.sendToServer(new CAerosmithRotationPacket(((HierophantGreenEntity) entity).possessedEntity.getEntityId(), ((HierophantGreenEntity) entity).possessedEntity.rotationYaw, ((HierophantGreenEntity) entity).possessedEntity.rotationPitch, ((HierophantGreenEntity) entity).possessedEntity.rotationYawHead));
                            } else {
                                Minecraft.getInstance().setRenderViewEntity(player);
                                Minecraft.getInstance().gameSettings.thirdPersonView = 0;
                            }
                        });
            if (!player.isSpectator() && !stand.getStandOn())
                if (Minecraft.getInstance().renderViewEntity != player)
                    Minecraft.getInstance().setRenderViewEntity(player);
        });
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void renderGameOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;
        //new StandGUI().render();
        new CarbonDioxideRadarGUI().renderRadar();
        new TimeSkipEffectGUI().renderEffect();
        //new MomentumMeterGUI().renderMeter();
//        if (Minecraft.getInstance().player == null) return;
//        int width = Minecraft.getInstance().getMainWindow().getFramebufferWidth();
//        int height = Minecraft.getInstance().getMainWindow().getFramebufferHeight();
//        NativeImage image = ScreenShotHelper.createScreenshot(width, height, Minecraft.getInstance().getFramebuffer());
//        DynamicTexture texture = new DynamicTexture(image);
//        Minecraft.getInstance().getTextureManager().bindTexture(Minecraft.getInstance().getTextureManager().getDynamicTextureLocation("screenshots", texture));
//        GuiUtils.drawContinuousTexturedBox(0, 0, 0, 0, width, height, width, height, 0, 0);
    }
    /**
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void deRenderScreen(EntityViewRenderEvent.FogDensity event) {
        event.setDensity(0.3f);
        if (event.getInfo().getRenderViewEntity() instanceof LivingEntity) {
            if (((LivingEntity) event.getInfo().getRenderViewEntity()).isPotionActive(EffectInit.OXYGEN_POISONING.get()))
                event.setCanceled(true);
            Timestop.getLazyOptional(event.getInfo().getRenderViewEntity()).ifPresent(timestop -> {
                if (timestop.getPosX() != 0)
                    event.setCanceled(true);
            });
        }
        event.setDensity(5);
    }
     */

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
                case Util.StandID.D4C: {
                    standName = "D4C";
                    break;
                }
                case Util.StandID.GOLD_EXPERIENCE: {
                    standName = "Gold Experience";
                    break;
                }
                case Util.StandID.MADE_IN_HEAVEN: {
                    standName = "Made in Heaven";
                    break;
                }
                case Util.StandID.GER: {
                    standName = "Gold Experience Requiem";
                    break;
                }
                case Util.StandID.AEROSMITH: {
                    standName = "Aerosmith";
                    break;
                }
                case Util.StandID.WEATHER_REPORT: {
                    standName = "Weather Report";
                    break;
                }
                case Util.StandID.KILLER_QUEEN: {
                    standName = "Killer Queen";
                    break;
                }
                case Util.StandID.CRAZY_DIAMOND: {
                    standName = "Crazy Diamond";
                    break;
                }
                case Util.StandID.PURPLE_HAZE: {
                    standName = "Purple Haze";
                    break;
                }
                case Util.StandID.THE_EMPEROR: {
                    standName = "The Emperor";
                    break;
                }
                case Util.StandID.WHITESNAKE: {
                    standName = "Whitesnake";
                    break;
                }
                case Util.StandID.CMOON: {
                    standName = "C-Moon";
                    break;
                }
                case Util.StandID.THE_WORLD: {
                    standName = "The World";
                    break;
                }
                case Util.StandID.STAR_PLATINUM: {
                    standName = "Star Platinum";
                    break;
                }
                case Util.StandID.SILVER_CHARIOT: {
                    standName = "Silver Chariot";
                    break;
                }
                case Util.StandID.MAGICIANS_RED: {
                    standName = "Magician's Red";
                    break;
                }
                case Util.StandID.THE_HAND: {
                    standName = "The Hand";
                    break;
                }
                case Util.StandID.HIEROPHANT_GREEN: {
                    standName = "Hierophant Green";
                    break;
                }
                case Util.StandID.GREEN_DAY: {
                    standName = "Green Day";
                    break;
                }
                case Util.StandID.TWENTIETH_CENTURY_BOY: {
                    standName = "20th Century Boy";
                    break;
                }
                case Util.StandID.THE_GRATEFUL_DEAD: {
                    standName = "The Grateful Dead";
                    break;
                }
                case Util.StandID.STICKY_FINGERS: {
                    standName = "Sticky Fingers";
                    break;
                }
                case Util.StandID.TUSK_ACT_1: {
                    standName = "Tusk (Act 1)";
                    break;
                }
                case Util.StandID.TUSK_ACT_2: {
                    standName = "Tusk (Act 2)";
                    break;
                }
                case Util.StandID.TUSK_ACT_3: {
                    standName = "Tusk (Act 3)";
                    break;
                }
                case Util.StandID.TUSK_ACT_4: {
                    standName = "Tusk (Act 4)";
                    break;
                }
                case Util.StandID.ECHOES_ACT_1: {
                    standName = "Echoes (Act 1)";
                    break;
                }
                case Util.StandID.ECHOES_ACT_2: {
                    standName = "Echoes (Act 2)";
                    break;
                }
                case Util.StandID.ECHOES_ACT_3: {
                    standName = "Echoes (Act 3)";
                    break;
                }
                case Util.StandID.BEACH_BOY: {
                    standName = "Beach Boy";
                    break;
                }
            }
        if (!standName.equals(""))
            event.getToolTip().add(new StringTextComponent(standName));
    }

    @SubscribeEvent
    public static void renderHand(RenderHandEvent event) {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        if (player == null) return;
        Stand.getLazyOptional(player).ifPresent(props -> {
            if (props.getStandOn())
                switch (props.getStandID()) {
                    default:
                        break;
                    case Util.StandID.AEROSMITH: {
                        if (props.getAbility())
                            event.setCanceled(true);
                        break;
                    }
                    case Util.StandID.HIEROPHANT_GREEN: {
                        if (props.getAbilityActive())
                            event.setCanceled(true);
                        break;
                    }
                    case Util.StandID.STICKY_FINGERS: {
                        StreamSupport.stream(Minecraft.getInstance().world.getAllEntities().spliterator(), false)
                                .filter(entity -> entity instanceof StickyFingersEntity)
                                .filter(entity -> ((StickyFingersEntity) entity).getMaster() != null)
                                .filter(entity -> ((StickyFingersEntity) entity).getMaster().equals(player))
                                .forEach(entity -> event.setCanceled(((StickyFingersEntity) entity).disguiseEntity != null));
                        break;
                    }
                }
        });
    }

    @SubscribeEvent
    public static void renderPlayer(RenderPlayerEvent.Pre event) {
        Stand.getLazyOptional(event.getPlayer()).ifPresent(stand -> {
            if (stand.getStandOn())
                switch (stand.getStandID()) {
                    case Util.StandID.STICKY_FINGERS: {
                        StreamSupport.stream(Minecraft.getInstance().world.getAllEntities().spliterator(), false)
                                .filter(entity -> entity instanceof StickyFingersEntity)
                                .filter(entity -> ((StickyFingersEntity) entity).getMaster() != null)
                                .filter(entity -> ((StickyFingersEntity) entity).getMaster().equals(event.getPlayer()))
                                .forEach(entity -> event.setCanceled(((StickyFingersEntity) entity).disguiseEntity != null));
                        break;
                    }
                    case Util.StandID.TUSK_ACT_3: {
                        event.setCanceled(stand.getAbilityActive());
                        break;
                    }
                    default:
                        break;
                }
        });
    }

    @SubscribeEvent
    public static void onRenderWorldLast(RenderWorldLastEvent event) {
        ClientWorld world = Minecraft.getInstance().world;
        Vec3d projectedView = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
        MatrixStack matrixStack = event.getMatrixStack();
        ClientPlayerEntity player = Minecraft.getInstance().player;
        float partialTicks = event.getPartialTicks();
        if (world == null) return;

        Stand.getLazyOptional(player).ifPresent(props -> {

            if ((props.getStandID() == Util.StandID.AEROSMITH && props.getStandOn() && props.getAbility()) ||
                    (props.getStandID() == Util.StandID.HIEROPHANT_GREEN && props.getStandOn() && props.getAbility() && props.getAbilityActive())) {
                double posX = MathHelper.lerp(partialTicks, player.lastTickPosX, player.getPosX());
                double posY = MathHelper.lerp(partialTicks, player.lastTickPosY, player.getPosY());
                double posZ = MathHelper.lerp(partialTicks, player.lastTickPosZ, player.getPosZ());
                float yaw = MathHelper.lerp(partialTicks, player.prevRotationYaw, player.rotationYaw);
                matrixStack.push();
                Minecraft.getInstance().getRenderManager().renderEntityStatic(
                        player,
                        posX - projectedView.getX(),
                        posY - projectedView.getY(),
                        posZ - projectedView.getZ(),
                        yaw,
                        partialTicks,
                        matrixStack,
                        Minecraft.getInstance().getRenderTypeBuffers().getBufferSource(),
                        Minecraft.getInstance().getRenderManager().getPackedLight(player, partialTicks)
                );
                matrixStack.pop();
            }
            if (event.getPhase() != EventPriority.NORMAL || player == null) return;

            /*
            if(!DiamondIsUncraftableConfig.CLIENT.noColorInversion.get()) {
                if (props.getExperiencingTimeStop()) {
                    Minecraft.getInstance().gameRenderer.loadShader(new ResourceLocation(DiamondIsUncraftable.MOD_ID, "shaders/timestop.json"));
                } else {
                    Minecraft.getInstance().gameRenderer.stopUseShader();
                }
            }

             */

        });
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public <T extends LivingEntity, M extends EntityModel<T>> void onRenderLiving(RenderLivingEvent.Pre<T, M> event) {
        if (Util.isTimeStoppedForEntity(mc.player)) {
            T entity = (T) event.getEntity();
            if (!entity.canUpdate() && event.getPartialRenderTick() != partialTickStoppedAt) {
                event.getRenderer().render(entity, MathHelper.lerp(partialTickStoppedAt, entity.rotationYaw, entity.prevRotationYaw), partialTickStoppedAt, event.getMatrixStack(), event.getBuffers(), event.getLight());
                event.setCanceled(true);
            }
        }
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
                    new StandEnergyGUI().renderEnergyBar();
                    //new StandLevelGUI().renderLevelScreen();
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
