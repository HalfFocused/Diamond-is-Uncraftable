package io.github.halffocused.diamond_is_uncraftable.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

public class DiamondIsUncraftableConfig {
    public static final Common COMMON;
    public static final Client CLIENT;
    private static final ForgeConfigSpec commonSpec;
    private static final ForgeConfigSpec clientSpec;

    static {
        final Pair<Common, ForgeConfigSpec> specPairCommon = new ForgeConfigSpec.Builder().configure(Common::new);
        commonSpec = specPairCommon.getRight();
        COMMON = specPairCommon.getLeft();

        final Pair<Client, ForgeConfigSpec> specPairClient = new ForgeConfigSpec.Builder().configure(Client::new);
        clientSpec = specPairClient.getRight();
        CLIENT = specPairClient.getLeft();
    }

    public static void register(final ModLoadingContext context) {
        context.registerConfig(ModConfig.Type.COMMON, commonSpec);
        context.registerConfig(ModConfig.Type.CLIENT, clientSpec);
    }

    public static class Common {
        public final ForgeConfigSpec.BooleanValue saveStandOnDeath;
        public final ForgeConfigSpec.IntValue timeStopRange;
        public final ForgeConfigSpec.IntValue maxStickyFingersBlocks;
        public final ForgeConfigSpec.DoubleValue standDamageMultiplier;
        public final ForgeConfigSpec.BooleanValue uniqueStandMode;

        Common(final ForgeConfigSpec.Builder builder) {
            builder.push("common");

            standDamageMultiplier = builder
                    .comment("Set the damage multiplier for stand attacks. 2.0 would result in double damage, 0.5 in half damage, etc.")
                    .defineInRange("standDamageMultiplier", 1.0, 0, 100.0F);

            uniqueStandMode = builder
                    .comment("Enable Unique Stand Mode: This mode causes the stand arrow to not roll stands that have already been rolled.\n Note that on large servers this will severely limit the amount of players that can have a stand.\n Stands will become available again whenever a player loses their stand for any reason.")
                    .define("uniqueStandMode", false);

            saveStandOnDeath = builder
                    .comment("Toggle save Stand on death")
                    .define("saveStandOnDeath", true);

            timeStopRange = builder
                    .comment("Set the range (in Chunks) of all time stop effects. Time stop's range is a square centered on it's user.\n Total chunks affected for ranges: \n Range of 1 = 1 chunk. Range of 8 ~= 225 chunks. Range of 16 ~= 1000 chunks. Range of 32 ~= 4000 chunks.")
                    .defineInRange("timeStopRange", 8, 1, 32);

            maxStickyFingersBlocks = builder
                    .comment("Currently Unused: Set the maximum amount of zipped blocks a Sticky Fingers user can have at once. Clusters of over 100 zipped blocks get laggy.")
                    .defineInRange("stickyFingersLimit", 100, 10, 200);

            builder.pop();
        }
    }

    public static class Client {
        public final ForgeConfigSpec.BooleanValue playStandSpawnSounds;
        public final ForgeConfigSpec.BooleanValue noTimestopDesaturation;
        public final ForgeConfigSpec.BooleanValue noTimeSkipFlash;

        Client(ForgeConfigSpec.Builder builder) {
            builder.push("client");

            playStandSpawnSounds = builder
                    .comment("Toggle Stand spawn sounds.")
                    .define("playStandSpawnSounds", true);

            noTimestopDesaturation = builder
                    .comment("If this is true, there is no screen color desaturation on Time Stop moves.")
                    .define("noInvertedColors", false);

            noTimeSkipFlash = builder
                    .comment("If this is true, there is no purple flash on Time Skip.")
                    .define("noTimeSkipFlash", false);

            builder.pop();
        }
    }
}
