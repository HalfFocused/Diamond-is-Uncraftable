package io.github.halffocused.diamond_is_uncraftable.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import io.github.halffocused.diamond_is_uncraftable.capability.Stand;
import io.github.halffocused.diamond_is_uncraftable.config.DiamondIsUncraftableConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.gui.GuiUtils;

public class BitesTheDustEffectGUI extends AbstractGui {
    public static final Minecraft mc = Minecraft.getInstance();

    public void renderEffect() {
        PlayerEntity player = mc.player;

        if (mc.world == null) return;
        if (player == null) return;

        if(!DiamondIsUncraftableConfig.CLIENT.noTimeSkipFlash.get()) {

            Stand.getLazyOptional(player).ifPresent(props -> {
                if (props.getBitesTheDustEffectTicker() > 0) {

                    //If I had randomly chosen numbers I'm pretty sure I would have gotten these faster than trying to actually figure them out.
                    //This was a very informative introduction to GUIs. What did I learn? Why I couldn't find a guide online.
                    double xScaleFactor = mc.getMainWindow().getScaledWidth() / 240.0;
                    double yScaleFactor = mc.getMainWindow().getScaledHeight() / 135.0;


                    GlStateManager.enableBlend();
                    GlStateManager.scaled(xScaleFactor, yScaleFactor, 0);

                    int frame = 46 - (props.getBitesTheDustEffectTicker());
                    String texture = "btd";
                    if(frame < 10){
                        texture += "00" + frame;
                    }else{
                        texture += "0" + frame;
                    }

                    Minecraft.getInstance().getTextureManager().bindTexture(new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/gui/bites_the_dust/" + texture + ".png"));
                    GuiUtils.drawTexturedModalRect(0, 0, 0, 0, 240, 135, 0);
                }
            });
        }
    }
}
