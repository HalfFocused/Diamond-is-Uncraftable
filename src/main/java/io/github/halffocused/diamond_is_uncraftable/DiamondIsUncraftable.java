package io.github.halffocused.diamond_is_uncraftable;

import io.github.halffocused.diamond_is_uncraftable.capability.*;
import io.github.halffocused.diamond_is_uncraftable.command.impl.StandCommand;
import io.github.halffocused.diamond_is_uncraftable.config.DiamondIsUncraftableConfig;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.AbstractStandEntity;
import io.github.halffocused.diamond_is_uncraftable.event.loot.DesertChestModifier;
import io.github.halffocused.diamond_is_uncraftable.init.*;
import io.github.halffocused.diamond_is_uncraftable.network.message.PacketHandler;
import io.github.halffocused.diamond_is_uncraftable.particle.ParticleList;
import io.github.halffocused.diamond_is_uncraftable.proxy.ClientProxy;
import io.github.halffocused.diamond_is_uncraftable.proxy.IProxy;
import io.github.halffocused.diamond_is_uncraftable.proxy.ServerProxy;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.data.DataGenerator;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.data.GlobalLootModifierProvider;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootTableIdCondition;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;
import software.bernie.geckolib3.GeckoLib;

/**
 * @author HalfFocused
 * <p>
 * The main {@link Mod} class, used mostly for registering objects.
 * Much respect to Novarch for being the beginning of this project!!
 * After he left, I took over, so you may see conflicting coding styles and old systems lurking around.
 */
@Mod("diamond_is_uncraftable")
public class DiamondIsUncraftable {
    public static final IProxy PROXY = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> ServerProxy::new);
    public static final String MOD_ID = "diamond_is_uncraftable";
    public static final ResourceLocation STRUCTURE = new ResourceLocation(MOD_ID, "desert_structure");
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    @ObjectHolder(MOD_ID + ":desert_structure")
    public static Structure<NoFeatureConfig> DESERT_STRUCTURE;

    private static final DeferredRegister<GlobalLootModifierSerializer<?>> GLOBAL_LOOT_MODIFIER = DeferredRegister.create(ForgeRegistries.LOOT_MODIFIER_SERIALIZERS, MOD_ID);

    private static final RegistryObject<DesertChestModifier.Serializer> DESERT_LOOT = GLOBAL_LOOT_MODIFIER.register("desert_loot", DesertChestModifier.Serializer::new);

    public DiamondIsUncraftable() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;
        modBus.addListener(this::setup);
        forgeBus.addListener(this::registerCommands); //FMLServerStartingEvent is fired on the Forge bus.

        EventInit.registerForgeBus(MinecraftForge.EVENT_BUS);
        ItemInit.ITEMS.register(modBus);
        EntityInit.ENTITY_TYPES.register(modBus);
        SoundInit.SOUNDS.register(modBus);
        ParticleList.PARTICLES.register(modBus);
        EffectInit.EFFECTS.register(modBus);
        GeckoLib.initialize();
        DiamondIsUncraftableConfig.register(ModLoadingContext.get());
        GLOBAL_LOOT_MODIFIER.register(FMLJavaModLoadingContext.get().getModEventBus());
        modBus.register(this);
    }

    @SubscribeEvent
    public void registerFeatures(RegistryEvent.Register<Feature<?>> args) {
        //DesertStructurePieces.DESERT_STRUCTURE_PIECE = Registry.register(Registry.STRUCTURE_PIECE, STRUCTURE, DesertStructurePieces.Piece::new);
        //args.getRegistry().register(new DesertStructure(NoFeatureConfig::deserialize).setRegistryName(STRUCTURE));
    }

    private static class DataProvider extends GlobalLootModifierProvider {
        public DataProvider(DataGenerator gen, String modid) {
            super(gen, modid);
        }

        @Override
        protected void start() {
            add("dungeon_loot", DESERT_LOOT.get(), new DesertChestModifier(
                    new ILootCondition[]{LootTableIdCondition.builder(new ResourceLocation("arrow_in_desert_structure")).build()}, ItemInit.STAND_ARROW.get())
            );
        }
    }

    private void setup(FMLCommonSetupEvent event) {
        Stand.register();
        Timestop.register();
        StandEffects.register();
        StandChunkEffects.register();
        StandPlayerEffects.register();
        StandTileEntityEffects.register();
        PacketHandler.register();
        StandPerWorldCapability.register();
        CombatCapability.register();
        WorldTimestopCapability.register();




        DeferredWorkQueue.runLater(() -> { //This is deprecated for no reason at all.
            /*
            if (biome.getCategory() != Biome.Category.DESERT) return;
            biome.addStructure(DESERT_STRUCTURE.withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG));
            biome.addFeature(GenerationStage.Decoration.UNDERGROUND_STRUCTURES, DESERT_STRUCTURE.withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG).withPlacement(Placement.NOPE.configure(IPlacementConfig.NO_PLACEMENT_CONFIG)));
            */

            GlobalEntityTypeAttributes.put(EntityInit.KILLER_QUEEN.get(), AbstractStandEntity.setCustomAttributes().create());
            GlobalEntityTypeAttributes.put(EntityInit.KILLER_QUEEN_BTD.get(), AbstractStandEntity.setCustomAttributes().create());
            GlobalEntityTypeAttributes.put(EntityInit.SILVER_CHARIOT.get(), AbstractStandEntity.setCustomAttributes().create());
            GlobalEntityTypeAttributes.put(EntityInit.THE_WORLD.get(), AbstractStandEntity.setCustomAttributes().create());
            GlobalEntityTypeAttributes.put(EntityInit.KING_CRIMSON.get(), AbstractStandEntity.setCustomAttributes().create());
        });


    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class EventHandlers {
        @SubscribeEvent
        public static void runData(GatherDataEvent event)
        {
            event.getGenerator().addProvider(new DataProvider(event.getGenerator(), MOD_ID));
        }
    }

    private void registerCommands(RegisterCommandsEvent event){
        StandCommand.register(event.getDispatcher());
    }

    @MethodsReturnNonnullByDefault
    public static class JojoItemGroup extends ItemGroup {
        public static final ItemGroup INSTANCE = new JojoItemGroup();

        private JojoItemGroup() {
            super(MOD_ID);
        }

        @Override
        public ItemStack createIcon() {
            return new ItemStack(ItemInit.STAND_ARROW.get());
        }

        @Override
        public boolean hasSearchBar() {
            return true;
        }
    }
}
