package io.github.halffocused.diamond_is_uncraftable.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.halffocused.diamond_is_uncraftable.capability.BitesTheDustCapability;
import io.github.halffocused.diamond_is_uncraftable.util.globalabilities.BitesTheDustHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;

@SuppressWarnings("unused")
public class BitesTheDustGUI extends AbstractGui {
    public static final Minecraft mc = Minecraft.getInstance();

    protected void renderString(MatrixStack stack, String text, int... position) {
        if (position.length < 2)
            drawString(stack, mc.fontRenderer, text, 4, 4, 0xFFFFFF);
        else if (position.length == 2)
            drawString(stack, mc.fontRenderer, text, position[0], position[1], 0xFFFFFF);
    }

    public void render(MatrixStack stack) {
        assert mc.player != null;
        BitesTheDustCapability.getLazyOptional(mc.player).ifPresent(standEffects -> {
            if(!standEffects.isEmpty()) {
                if (BitesTheDustHelper.bitesTheDustPlayer == null) return;
                renderString(stack, "Bites The Dust - Time Remaining: " + (BitesTheDustCapability.getCapabilityFromEntity(BitesTheDustHelper.bitesTheDustPlayer).getAge() / 20) + " seconds");
            }
        });
    }
}
