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

    public static final RegistryObject<Effect> OXYGEN_POISONING = EFFECTS.register("oxygen_poisoning",
            () -> new OxygenPoisoningEffect(EffectType.HARMFUL, 7009526)
                    .addAttributesModifier(Attributes.MOVEMENT_SPEED, "81d95dec-554d-4199-adbc-a339d2a671d3", -0.2f, AttributeModifier.Operation.MULTIPLY_TOTAL));

    public static final RegistryObject<Effect> HAZE = EFFECTS.register("haze",
            () -> new HazeEffect(EffectType.HARMFUL, 9250166));

    public static final RegistryObject<Effect> STAND_WEAKNESS = EFFECTS.register("stand_weakness",
            () -> new StandWeaknessEffect(EffectType.HARMFUL, 16646144));

    public static final RegistryObject<Effect> STAND_STRENGTH = EFFECTS.register("stand_strength",
            () -> new StandStrengthEffect(EffectType.BENEFICIAL, 5826842));

    public static final RegistryObject<Effect> AGING = EFFECTS.register("aging",
            () -> new AgingEffect(EffectType.HARMFUL, 4606017)
                    .addAttributesModifier(Attributes.MOVEMENT_SPEED, "a660e8a9-dcec-48e6-abad-558df89b75fe", -0.1f, AttributeModifier.Operation.ADDITION)
                    .addAttributesModifier(Attributes.FOLLOW_RANGE, "a660e8a9-dcec-48e6-abad-558df89b75fe", -0.2f, AttributeModifier.Operation.ADDITION)
                    .addAttributesModifier(Attributes.KNOCKBACK_RESISTANCE, "a660e8a9-dcec-48e6-abad-558df89b75fe", -0.2f, AttributeModifier.Operation.ADDITION)
                    .addAttributesModifier(Attributes.MAX_HEALTH, "a660e8a9-dcec-48e6-abad-558df89b75fe", -0.2f, AttributeModifier.Operation.ADDITION)
                    .addAttributesModifier(Attributes.ATTACK_KNOCKBACK, "a660e8a9-dcec-48e6-abad-558df89b75fe", -0.2f, AttributeModifier.Operation.ADDITION)
                    .addAttributesModifier(Attributes.ATTACK_DAMAGE, "a660e8a9-dcec-48e6-abad-558df89b75fe", -0.2f, AttributeModifier.Operation.ADDITION));
}
