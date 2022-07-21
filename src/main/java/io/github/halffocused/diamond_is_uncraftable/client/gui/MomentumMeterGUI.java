package io.github.halffocused.diamond_is_uncraftable.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import io.github.halffocused.diamond_is_uncraftable.capability.Stand;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.gui.GuiUtils;

@SuppressWarnings("deprecation")
public class MomentumMeterGUI extends AbstractGui {
    public static final Minecraft mc = Minecraft.getInstance();

    public void renderMeter() {
        PlayerEntity player = mc.player;
        int posX = mc.getMainWindow().getScaledWidth() - 180, posY = (mc.getMainWindow().getScaledHeight() - 256) / 2;

        if (mc.world == null) return;
        if (player == null) return;
        Stand.getLazyOptional(player).ifPresent(props -> {
            if (props.getStandID() == Util.StandID.THE_WORLD && props.getStandOn()) {
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.enableAlphaTest();

                GlStateManager.translated(posX, posY, 0);

                /**GlStateManager.translated(32, 192, 192);
                GlStateManager.scaled(4, 4, 0);

                GlStateManager.translated(-32, -192, -192); */

                Minecraft.getInstance().getTextureManager().bindTexture(new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/gui/momentum_meter.png"));
                GuiUtils.drawTexturedModalRect(0, 0, 0, 0, mc.getMainWindow().getScaledWidth(), mc.getMainWindow().getScaledHeight(), 0);


                GlStateManager.popMatrix();
            }
        });
    }
}
