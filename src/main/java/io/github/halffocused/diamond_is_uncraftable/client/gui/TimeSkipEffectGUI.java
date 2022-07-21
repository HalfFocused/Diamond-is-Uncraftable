package io.github.halffocused.diamond_is_uncraftable.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import io.github.halffocused.diamond_is_uncraftable.capability.Stand;
import io.github.halffocused.diamond_is_uncraftable.config.JojoBizarreSurvivalConfig;
import io.github.halffocused.diamond_is_uncraftable.network.message.client.CTimeSkipEffectPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.gui.GuiUtils;

public class TimeSkipEffectGUI extends AbstractGui {
    public static final Minecraft mc = Minecraft.getInstance();

    public void renderEffect() {
        PlayerEntity player = mc.player;

        if (mc.world == null) return;
        if (player == null) return;

        if(!JojoBizarreSurvivalConfig.CLIENT.reducedFlashes.get()) {

            Stand.getLazyOptional(player).ifPresent(props -> {
                if (props.getTimeSkipEffectTicker() > 0) {
                /*
                RenderSystem.disableDepthTest();
                RenderSystem.depthMask(false);
                RenderSystem.defaultBlendFunc();
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

                Minecraft.getInstance().getTextureManager().bindTexture(new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/gui/timeskip/time_skip" + (16 - props.getTimeSkipEffectTicker()) + ".png"));

                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder bufferbuilder = tessellator.getBuffer();
                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
                bufferbuilder.pos(0.0D, mc.getMainWindow().getScaledHeight(), -90.0D).tex(0.0F, 1.0F).endVertex();
                bufferbuilder.pos(mc.getMainWindow().getScaledWidth(), mc.getMainWindow().getScaledHeight(), -90.0D).tex(1.0F, 1.0F).endVertex();
                bufferbuilder.pos(mc.getMainWindow().getScaledWidth(), 0.0D, -90.0D).tex(1.0F, 0.0F).endVertex();
                bufferbuilder.pos(0.0D, 0.0D, -90.0D).tex(0.0F, 0.0F).endVertex();
                tessellator.draw();
                RenderSystem.depthMask(false);
                RenderSystem.enableDepthTest();
                RenderSystem.enableAlphaTest();
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                 */

                    //If I had randomly chosen numbers I'm pretty sure I would have gotten these faster than trying to actually figure them out.
                    //This was a very informative introduction to GUIs. What did I learn? Why I couldn't find a guide online.
                    double xScaleFactor = mc.getMainWindow().getScaledWidth() / 256.0;
                    double yScaleFactor = mc.getMainWindow().getScaledHeight() / 154.0;


                    GlStateManager.enableBlend();
                    GlStateManager.scaled(xScaleFactor, yScaleFactor, 0);

                    int frame = props.getTimeSkipEffectTicker() / 2;
                    frame = frame == 0 ? 1 : frame;

                    Minecraft.getInstance().getTextureManager().bindTexture(new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/gui/timeskip/time_skip" + (16 - frame) + ".png"));
                    GuiUtils.drawTexturedModalRect(0, 0, 0, 0, 256, 154, 0);

                    DiamondIsUncraftable.INSTANCE.sendToServer(new CTimeSkipEffectPacket());
                }
            });
        }
    }
}
