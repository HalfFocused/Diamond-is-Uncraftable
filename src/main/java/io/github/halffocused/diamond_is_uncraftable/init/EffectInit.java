package io.github.halffocused.diamond_is_uncraftable.init;

import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import io.github.halffocused.diamond_is_uncraftable.effect.*;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;


public class EffectInit {
    public static DeferredRegister<Effect> EFFECTS = DeferredRegister.create(ForgeRegistries.POTIONS, DiamondIsUncraftable.MOD_ID);

    public static final RegistryObject<Effect> HAZE = EFFECTS.register("haze",
            () -> new HazeEffect(EffectType.HARMFUL, 9250166));

    public static final RegistryObject<Effect> STAND_WEAKNESS = EFFECTS.register("stand_weakness",
            () -> new StandWeaknessEffect(EffectType.HARMFUL, 16646144));

    public static final RegistryObject<Effect> STAND_STRENGTH = EFFECTS.register("stand_strength",
            () -> new StandStrengthEffect(EffectType.BENEFICIAL, 5826842));
}
