package io.github.halffocused.diamond_is_uncraftable.event;

import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import io.github.halffocused.diamond_is_uncraftable.capability.Stand;
import io.github.halffocused.diamond_is_uncraftable.init.KeyInit;
import io.github.halffocused.diamond_is_uncraftable.network.message.client.*;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = DiamondIsUncraftable.MOD_ID, bus = Bus.FORGE, value = Dist.CLIENT)
public class EventHandleKeybinds {
    @SubscribeEvent
    public static void onInput(TickEvent.ClientTickEvent event) { //It's recommended to use ClientTickEvent instead of any of the input events.
        if (event.phase != TickEvent.Phase.END || !Minecraft.getInstance().isGameFocused()) return;
        Minecraft mc = Minecraft.getInstance();

        if (mc.player != null) {
            if (KeyInit.SPAWN_STAND.isPressed())
                DiamondIsUncraftable.INSTANCE.sendToServer(new CStandSummonPacket());

            if(KeyInit.SWITCH_TARGETING.isPressed())
                DiamondIsUncraftable.INSTANCE.sendToServer(new CWalkingStandTargetPacket());

            if (KeyInit.TOGGLE_ABILITY.isPressed())
                DiamondIsUncraftable.INSTANCE.sendToServer(new CToggleAbilityPacket());

            DiamondIsUncraftable.INSTANCE.sendToServer(new CChargeAttackPacket(mc.gameSettings.keyBindAttack.isKeyDown(), mc.player.isCrouching()));


            Stand.getLazyOptional(mc.player).ifPresent(props -> {
                if (props.hasAct())
                    if (KeyInit.SWITCH_ACT.isPressed())
                        DiamondIsUncraftable.INSTANCE.sendToServer(new CSwitchStandActPacket());

                    if (KeyInit.ABILITY1.isPressed())
                        DiamondIsUncraftable.INSTANCE.sendToServer(new CSyncStandAbilitiesPacket((byte) 1));
                    if (KeyInit.ABILITY2.isPressed())
                        DiamondIsUncraftable.INSTANCE.sendToServer(new CSyncStandAbilitiesPacket((byte) 2));
                    if (KeyInit.ABILITY3.isPressed())
                        DiamondIsUncraftable.INSTANCE.sendToServer(new CSyncStandAbilitiesPacket((byte) 3));

                if (props.getStandOn()) {
                    if (mc.gameSettings.keyBindBack.isKeyDown()) {
                        DiamondIsUncraftable.INSTANCE.sendToServer(new CStandMasterMovementPacket(CStandMasterMovementPacket.Direction.BACKWARDS));
                    }else if (mc.gameSettings.keyBindRight.isKeyDown()) {
                        DiamondIsUncraftable.INSTANCE.sendToServer(new CStandMasterMovementPacket(CStandMasterMovementPacket.Direction.RIGHT));
                    }else if (mc.gameSettings.keyBindLeft.isKeyDown()) {
                        DiamondIsUncraftable.INSTANCE.sendToServer(new CStandMasterMovementPacket(CStandMasterMovementPacket.Direction.LEFT));
                    }else{
                        DiamondIsUncraftable.INSTANCE.sendToServer(new CStandMasterMovementPacket(CStandMasterMovementPacket.Direction.NOT_MOVING));
                    }
                }
            });

        }
    }
}
