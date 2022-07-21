package io.github.halffocused.diamond_is_uncraftable.client.gui;

import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import io.github.halffocused.diamond_is_uncraftable.capability.Stand;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;

@SuppressWarnings("deprecation")
public class StandEnergyGUI extends AbstractGui {
    public static final Minecraft mc = Minecraft.getInstance();

    public void renderEnergyBar() {
        PlayerEntity player = mc.player;
        int scaledWidth = mc.getMainWindow().getScaledWidth();
        int scaledHeight = mc.getMainWindow().getScaledHeight();
        int x = scaledWidth / 2;
        if (mc.world == null) return;
        if (player == null) return;
        Stand.getLazyOptional(player).ifPresent(stand -> {
            if(stand.getStandOn()) {
                mc.getTextureManager().bindTexture(new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/gui/energy_meter.png"));
                int i = (int) stand.getMaxStandEnergy();
                if (i > 0) {
                    int j = 182;
                    int k = (int) ((stand.getCurrentStandEnergy() / stand.getMaxStandEnergy()) * 183.0F);
                    int l = scaledHeight - 32 + 3;
                    this.blit(x - 91, l, 0, 64, 182, 5);
                    if (k > 0) {
                        this.blit(x - 91, l, 0, 69, k, 5);
                    }
                }


                String s = "" + (int) stand.getCurrentStandEnergy();
                int i1 = (scaledWidth - mc.ingameGUI.getFontRenderer().getStringWidth(s)) / 2;
                int j1 = scaledHeight - 32 - 3;
                mc.ingameGUI.getFontRenderer().drawString(s, (float) (i1 + 1), (float) j1, 0);
                mc.ingameGUI.getFontRenderer().drawString(s, (float) (i1 - 1), (float) j1, 0);
                mc.ingameGUI.getFontRenderer().drawString(s, (float) i1, (float) (j1 + 1), 0);
                mc.ingameGUI.getFontRenderer().drawString(s, (float) i1, (float) (j1 - 1), 0);
                mc.ingameGUI.getFontRenderer().drawString(s, (float) i1, (float) j1, 15823613); //8453920

                if (Util.StandID.MOMENTUM_METER_STANDS.contains(stand.getStandID())) {
                    mc.getTextureManager().bindTexture(new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/gui/momentum_meter.png"));
                    i = (int) stand.getMaxStandEnergy();
                    if (i > 0) {
                        int j = 182;
                        int k = (int) ((stand.getMomentum() / 100.0) * 183.0F);
                        int l = scaledHeight - 37 - 8;
                        this.blit(x - 91, l, 0, 64, 182, 5);
                        if (k > 0) {
                            this.blit(x - 91, l, 0, 69, k, 5);
                        }
                    }

                    s = "" + stand.getMomentum();
                    i1 = (scaledWidth - mc.ingameGUI.getFontRenderer().getStringWidth(s)) / 2;
                    j1 = scaledHeight - 39 - 12;
                    mc.ingameGUI.getFontRenderer().drawString(s, (float) (i1 + 1), (float) j1, 0);
                    mc.ingameGUI.getFontRenderer().drawString(s, (float) (i1 - 1), (float) j1, 0);
                    mc.ingameGUI.getFontRenderer().drawString(s, (float) i1, (float) (j1 + 1), 0);
                    mc.ingameGUI.getFontRenderer().drawString(s, (float) i1, (float) (j1 - 1), 0);
                    mc.ingameGUI.getFontRenderer().drawString(s, (float) i1, (float) j1, stand.getMomentum() > 50 ? 7464704 : 16738665); //8453920
                }
            }else{ //Stand off energy display
                if(stand.getCurrentStandEnergy() / stand.getMaxStandEnergy() != 1){
                    String s = "" + (int) stand.getCurrentStandEnergy();
                    int i1 = (scaledWidth - mc.ingameGUI.getFontRenderer().getStringWidth(s)) / 2;
                    int j1 = scaledHeight - 32 - 12;
                    mc.ingameGUI.getFontRenderer().drawString(s, (float) (i1 + 1), (float) j1, 0);
                    mc.ingameGUI.getFontRenderer().drawString(s, (float) (i1 - 1), (float) j1, 0);
                    mc.ingameGUI.getFontRenderer().drawString(s, (float) i1, (float) (j1 + 1), 0);
                    mc.ingameGUI.getFontRenderer().drawString(s, (float) i1, (float) (j1 - 1), 0);
                    mc.ingameGUI.getFontRenderer().drawString(s, (float) i1, (float) j1, 15823613); //8453920
                }
            }
        });

    }
}
