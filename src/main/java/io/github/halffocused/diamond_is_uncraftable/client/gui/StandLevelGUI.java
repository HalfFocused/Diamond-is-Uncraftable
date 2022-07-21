package io.github.halffocused.diamond_is_uncraftable.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;

public class StandLevelGUI extends AbstractGui
{
    public static final Minecraft mc = Minecraft.getInstance();

    private int width;
    private int height;

    public StandLevelGUI()
    {
        width = mc.getMainWindow().getScaledWidth();
        height = mc.getMainWindow().getScaledHeight();
    }

    public void renderLevelScreen(){
        drawCenteredString(mc.fontRenderer, "Stand Leveling: ", 10 + mc.fontRenderer.getStringWidth("Stand Leveling: ") / 2, height - 2 * (mc.fontRenderer.FONT_HEIGHT + 4), Integer.parseInt("FFAA00", 16));
        drawCenteredString(mc.fontRenderer, "Current Points: ", 10 + mc.fontRenderer.getStringWidth("Current Points: ") / 2, height - (mc.fontRenderer.FONT_HEIGHT + 4), Integer.parseInt("FFAA00", 16));

    }
}
