package io.github.halffocused.diamond_is_uncraftable.init;

import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class SoundInit {
    public static DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, DiamondIsUncraftable.MOD_ID);

    public static final RegistryObject<SoundEvent> PUNCH_MISS = SOUNDS.register("punch_miss", () -> new SoundEvent(new ResourceLocation(DiamondIsUncraftable.MOD_ID, "punch_miss")));
    public static final RegistryObject<SoundEvent> RESUME_TIME_STAR_PLATINUM = SOUNDS.register("resume_time_star_platinum", () -> new SoundEvent(new ResourceLocation(DiamondIsUncraftable.MOD_ID, "resume_time_star_platinum")));

    /*
    Silver Chariot
     */
    public static final RegistryObject<SoundEvent> ARMOR_OFF_1 = SOUNDS.register("silverchariotarmoroff1", () -> new SoundEvent(new ResourceLocation(DiamondIsUncraftable.MOD_ID, "silverchariotarmoroff1")));
    public static final RegistryObject<SoundEvent> ARMOR_OFF_2 = SOUNDS.register("silverchariotarmoroff2", () -> new SoundEvent(new ResourceLocation(DiamondIsUncraftable.MOD_ID, "silverchariotarmoroff2")));
    public static final RegistryObject<SoundEvent> ARMOR_OFF_3 = SOUNDS.register("silverchariotarmoroff3", () -> new SoundEvent(new ResourceLocation(DiamondIsUncraftable.MOD_ID, "silverchariotarmoroff3")));

    /*
    Killer Queen
    */

    public static final RegistryObject<SoundEvent> DETONATION_CLICK = SOUNDS.register("kq_click", () -> new SoundEvent(new ResourceLocation(DiamondIsUncraftable.MOD_ID, "kq_click")));

    /*
    The World
     */
    public static final RegistryObject<SoundEvent> THE_WORLD_TIME_STOP = SOUNDS.register("twstop", () -> new SoundEvent(new ResourceLocation(DiamondIsUncraftable.MOD_ID, "twstop")));
    public static final RegistryObject<SoundEvent> THE_WORLD_TIME_RESUME = SOUNDS.register("twresume", () -> new SoundEvent(new ResourceLocation(DiamondIsUncraftable.MOD_ID, "twresume")));
    public static final RegistryObject<SoundEvent> THE_WORLD_SUMMON = SOUNDS.register("twsummon", () -> new SoundEvent(new ResourceLocation(DiamondIsUncraftable.MOD_ID, "twsummon")));
    public static final RegistryObject<SoundEvent> THE_WORLD_TELEPORT = SOUNDS.register("twteleport", () -> new SoundEvent(new ResourceLocation(DiamondIsUncraftable.MOD_ID, "twteleport")));

    /*
    King Crimson
    */
    public static final RegistryObject<SoundEvent> KING_CRIMSON_COMBO = SOUNDS.register("kccombo", () -> new SoundEvent(new ResourceLocation(DiamondIsUncraftable.MOD_ID, "kccombo")));
    public static final RegistryObject<SoundEvent> TIME_SKIP_AMBIANCE = SOUNDS.register("kcskipambiance", () -> new SoundEvent(new ResourceLocation(DiamondIsUncraftable.MOD_ID, "kcskipambiance")));
    public static final RegistryObject<SoundEvent> TIME_SKIP_END = SOUNDS.register("kcskipend", () -> new SoundEvent(new ResourceLocation(DiamondIsUncraftable.MOD_ID, "kcskipend")));
    public static final RegistryObject<SoundEvent> TIME_SKIP_BEGIN = SOUNDS.register("kcskipstart", () -> new SoundEvent(new ResourceLocation(DiamondIsUncraftable.MOD_ID, "kcskipstart")));


    /*
    Generic sounds shared by a lot of the stands
     */

    public static final RegistryObject<SoundEvent> PUNCH_1 = SOUNDS.register("punch1", () -> new SoundEvent(new ResourceLocation(DiamondIsUncraftable.MOD_ID, "punch1")));
    public static final RegistryObject<SoundEvent> PUNCH_2 = SOUNDS.register("punch2", () -> new SoundEvent(new ResourceLocation(DiamondIsUncraftable.MOD_ID, "punch2")));
    public static final RegistryObject<SoundEvent> PUNCH_3 = SOUNDS.register("punch3", () -> new SoundEvent(new ResourceLocation(DiamondIsUncraftable.MOD_ID, "punch3")));
    public static final RegistryObject<SoundEvent> PUNCH_4 = SOUNDS.register("punch4", () -> new SoundEvent(new ResourceLocation(DiamondIsUncraftable.MOD_ID, "punch4")));
    public static final RegistryObject<SoundEvent> PUNCH_5 = SOUNDS.register("punch5", () -> new SoundEvent(new ResourceLocation(DiamondIsUncraftable.MOD_ID, "punch5")));
    public static final RegistryObject<SoundEvent> PUNCH_6 = SOUNDS.register("punch6", () -> new SoundEvent(new ResourceLocation(DiamondIsUncraftable.MOD_ID, "punch6")));
    public static final RegistryObject<SoundEvent> PUNCH_7 = SOUNDS.register("punch7", () -> new SoundEvent(new ResourceLocation(DiamondIsUncraftable.MOD_ID, "punch7")));
    public static final RegistryObject<SoundEvent> SUMMON_STAND = SOUNDS.register("standout", () -> new SoundEvent(new ResourceLocation(DiamondIsUncraftable.MOD_ID, "standout")));



}
