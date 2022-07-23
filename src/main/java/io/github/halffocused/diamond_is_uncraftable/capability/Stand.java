package io.github.halffocused.diamond_is_uncraftable.capability;

import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import io.github.halffocused.diamond_is_uncraftable.config.JojoBizarreSurvivalConfig;
import io.github.halffocused.diamond_is_uncraftable.network.message.server.SSyncStandCapabilityPacket;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.PacketDistributor;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static io.github.halffocused.diamond_is_uncraftable.util.Util.Null;
import static io.github.halffocused.diamond_is_uncraftable.util.Util.StandID.*;

/**
 * The {@link Capability} used for storing the player's Stand ability.
 */
public class Stand implements ICapabilitySerializable<INBT> {
    @CapabilityInject(Stand.class)
    public static final Capability<Stand> STAND = Null(); //Null method suppresses warnings
    private final PlayerEntity player;
    private int standID;
    /**
     * The {@link Entity#getEntityId()} of the player's Stand, can be used in conjunction with a {@link net.minecraft.world.World} to get the entity.
     */
    private int standEntityID;
    private int standAct;
    private boolean standOn;
    private double cooldown;
    private double timeLeft = 1000;
    private String diavolo = "";
    private boolean ability = false;
    private boolean abilityActive;
    private int transformed;
    private boolean noClip;
    private double invulnerableTicks;
    private float standDamage;
    private boolean charging;
    private int abilityUseCount;
    private BlockPos blockPos = BlockPos.ZERO;
    private List<ChunkPos> affectedChunkList = new ArrayList<>();
    private int experiencePoints;
    private int prevExperiencePoints;
    private long gameTime = -1;
    private long dayTime = -1;
    private int abilitiesUnlocked;
    private boolean preventUnsummon;
    private boolean preventUnsummon2;
    private int momentum = 0;
    private int restraint = 0;
    private int epitaphEffectTicker = 0;
    private double maxStandEnergy = 100;
    private double currentStandEnergy = 100;
    private double energyRegenerationRate = 5;
    private int energyRegenerationCooldown = 0;
    private boolean experiencingTimeSkip = false;
    private boolean experiencingTimeStop = false;
    private int instantTimeStopFrame = 0;
    private int bombEntityId = 0;
    private boolean queueBombRemoval = false;

    private int invincibleTicks = 0;
    private int reflexCooldown = 0;
    private int standUnsummonedTime = 0;

    /*
        Whether or not the next time the stand's master takes damage is nullified.
        Handle with care! This could very easily turn the stand into GER 2 if handled incorrectly.
        Amount of times I have accidentally made the stand master immortal: 4
    */
    private boolean counterBuffer = false;

    private Map<ChunkPos, Map<BlockPos, BlockState>> crazyDiamondBlocks = new ConcurrentHashMap<>();
    private LazyOptional<Stand> holder = LazyOptional.of(() -> new Stand(getPlayer()));

    public Stand(@Nonnull PlayerEntity player) {
        this.player = player;
    }

    public static Stand getCapabilityFromPlayer(PlayerEntity player) {
        return player.getCapability(STAND).orElse(new Stand(player));
    }

    public static LazyOptional<Stand> getLazyOptional(PlayerEntity player) {
        return player.getCapability(STAND);
    }

    public static void register() {
        CapabilityManager.INSTANCE.register(Stand.class, new Capability.IStorage<Stand>() {
            @Nonnull
            @Override
            public INBT writeNBT(Capability<Stand> capability, Stand instance, Direction side) {
                CompoundNBT nbt = new CompoundNBT();
                nbt.putInt("standID", instance.getStandID());
                nbt.putInt("standAct", instance.getAct());
                nbt.putBoolean("standOn", instance.getStandOn());
                nbt.putDouble("cooldown", instance.getCooldown());
                nbt.putDouble("timeLeft", instance.getTimeLeft());
                nbt.putBoolean("ability", instance.getAbility());
                nbt.putInt("transformed", instance.getTransformed());
                nbt.putString("diavolo", instance.getDiavolo());
                nbt.putBoolean("noClip", instance.getNoClip());
                nbt.putInt("standEntityID", instance.getPlayerStand());
                nbt.putBoolean("abilityActive", instance.getAbilityActive());
                nbt.putDouble("invulnerableTicks", instance.getInvulnerableTicks());
                nbt.putFloat("standDamage", instance.getStandDamage());
                nbt.putBoolean("charging", instance.isCharging());
                nbt.putInt("abilityUseCount", instance.getAbilityUseCount());
                nbt.putDouble("blockPosX", instance.getBlockPos().getX());
                nbt.putDouble("blockPosY", instance.getBlockPos().getY());
                nbt.putDouble("blockPosZ", instance.getBlockPos().getZ());
                nbt.putInt("experiencePoints", instance.getExperiencePoints());
                nbt.putInt("prevExperiencePoints", instance.getPrevExperiencePoints());
                nbt.putLong("gameTime", instance.getGameTime());
                nbt.putLong("dayTime", instance.getDayTime());
                nbt.putInt("abilitiesUnlocked", instance.getAbilitiesUnlocked());
                nbt.putBoolean("preventUnsummon", instance.getPreventUnsummon());
                nbt.putBoolean("preventUnsummon2", instance.getPreventUnsummon2());
                nbt.putInt("momentum", instance.getMomentum());
                nbt.putInt("restraint", instance.getRestraint());
                nbt.putInt("epitaphTicker", instance.getTimeSkipEffectTicker());
                nbt.putDouble("currentEnergy", instance.getCurrentStandEnergy());
                nbt.putDouble("maxEnergy", instance.getMaxStandEnergy());
                nbt.putDouble("energyRegeneration", instance.getEnergyRegenerationRate());
                nbt.putInt("energyCooldown", instance.getEnergyCooldown());
                nbt.putBoolean("timeskip", instance.getExperiencingTimeSkip());
                nbt.putBoolean("timestop", instance.getExperiencingTimeStop());
                nbt.putBoolean("counterBuffer", instance.getCounterBuffer());
                nbt.putInt("timeStopFrame", instance.getInstantTimeStopFrame());
                nbt.putInt("invincibleTicks", instance.getInvincibleTicks());
                nbt.putInt("reflexCooldown", instance.getReflexCooldown());
                nbt.putInt("standUnsummonedTime", instance.getStandUnsummonedTime());
                nbt.putInt("bombEntity", instance.getBombEntityId());
                nbt.putBoolean("queueBombRemoval", instance.getQueueBombRemoval());
                ListNBT affectedChunkList = new ListNBT();
                instance.getAffectedChunkList().forEach(pos -> {
                    CompoundNBT compoundNBT = new CompoundNBT();
                    compoundNBT.putInt("chunkX", pos.x);
                    compoundNBT.putInt("chunkZ", pos.z);
                    affectedChunkList.add(compoundNBT);
                });
                nbt.put("affectedChunkList", affectedChunkList);
                ListNBT crazyDiamondBlocks = new ListNBT();
                instance.crazyDiamondBlocks.forEach((pos, list) -> {
                    CompoundNBT compoundNBT = new CompoundNBT();
                    compoundNBT.putInt("chunkPosX", pos.x);
                    compoundNBT.putInt("chunkPosZ", pos.z);
                    ListNBT listNBT = new ListNBT();
                    list.forEach((blockPos, blockState) -> {
                        CompoundNBT compound = new CompoundNBT();
                        compound.putDouble("blockPosX", blockPos.getX());
                        compound.putDouble("blockPosY", blockPos.getY());
                        compound.putDouble("blockPosZ", blockPos.getZ());
                        compound.putInt("blockState", Block.getStateId(blockState));
                        listNBT.add(compound);
                    });
                    compoundNBT.put("blockPosList", listNBT);
                    crazyDiamondBlocks.add(compoundNBT);
                });
                nbt.put("crazyDiamondBlocks", crazyDiamondBlocks);
                return nbt;
            }

            @Override
            public void readNBT(Capability<Stand> capability, Stand instance, Direction side, INBT nbt) {
                CompoundNBT compoundNBT = (CompoundNBT) nbt;
                instance.standID = compoundNBT.getInt("standID");
                instance.standAct = compoundNBT.getInt("standAct");
                instance.standOn = compoundNBT.getBoolean("standOn");
                instance.cooldown = compoundNBT.getDouble("cooldown");
                instance.timeLeft = compoundNBT.getDouble("timeLeft");
                instance.ability = compoundNBT.getBoolean("ability");
                instance.transformed = compoundNBT.getInt("transformed");
                instance.diavolo = compoundNBT.getString("diavolo");
                instance.noClip = compoundNBT.getBoolean("noClip");
                instance.standEntityID = compoundNBT.getInt("standEntityID");
                instance.abilityActive = compoundNBT.getBoolean("abilityActive");
                instance.invulnerableTicks = compoundNBT.getDouble("invulnerableTicks");
                instance.standDamage = compoundNBT.getFloat("standDamage");
                instance.charging = compoundNBT.getBoolean("charging");
                instance.abilityUseCount = compoundNBT.getInt("abilityUseCount");
                instance.blockPos = new BlockPos(compoundNBT.getDouble("blockPosX"), compoundNBT.getDouble("blockPosY"), compoundNBT.getDouble("blockPosZ"));
                instance.experiencePoints = compoundNBT.getInt("experiencePoints");
                instance.prevExperiencePoints = compoundNBT.getInt("prevExperiencePoints");
                instance.gameTime = compoundNBT.getLong("gameTime");
                instance.dayTime = compoundNBT.getLong("dayTime");
                instance.abilitiesUnlocked = compoundNBT.getInt("abilitiesUnlocked");
                instance.preventUnsummon = compoundNBT.getBoolean("preventUnsummon");
                instance.preventUnsummon2 = compoundNBT.getBoolean("preventUnsummon2");
                instance.momentum = compoundNBT.getInt("momentum");
                instance.restraint = compoundNBT.getInt("restraint");
                instance.epitaphEffectTicker = compoundNBT.getInt("epitaphTicker");
                instance.currentStandEnergy = compoundNBT.getDouble("currentEnergy");
                instance.maxStandEnergy = compoundNBT.getDouble("maxEnergy");
                instance.energyRegenerationRate = compoundNBT.getDouble("energyRegeneration");
                instance.energyRegenerationCooldown = compoundNBT.getInt("energyCooldown");
                instance.experiencingTimeSkip = compoundNBT.getBoolean("timeskip");
                instance.experiencingTimeStop = compoundNBT.getBoolean("timestop");
                instance.counterBuffer = compoundNBT.getBoolean("counterBuffer");
                instance.instantTimeStopFrame = compoundNBT.getInt("timeStopFrame");
                instance.invincibleTicks = compoundNBT.getInt("invincibleTicks");
                instance.reflexCooldown = compoundNBT.getInt("reflexCooldown");
                instance.standUnsummonedTime = compoundNBT.getInt("standUnsummonedTime");
                instance.bombEntityId = compoundNBT.getInt("bombEntity");
                instance.queueBombRemoval = compoundNBT.getBoolean("queueBombRemoval");

                compoundNBT.getList("affectedChunkList", Constants.NBT.TAG_COMPOUND).forEach(inbt -> {
                    if (inbt instanceof CompoundNBT && ((CompoundNBT) inbt).contains("chunkX"))
                        instance.affectedChunkList.add(new ChunkPos(((CompoundNBT) inbt).getInt("chunkX"), ((CompoundNBT) inbt).getInt("chunkZ")));
                });
                compoundNBT.getList("crazyDiamondBlocks", Constants.NBT.TAG_COMPOUND).forEach(compound -> {
                    if (compound instanceof CompoundNBT && ((CompoundNBT) compound).contains("chunkPosX")) {
                        Map<BlockPos, BlockState> map = new ConcurrentHashMap<>();
                        ((CompoundNBT) compound).getList("blockPosList", Constants.NBT.TAG_COMPOUND).forEach(inbt -> {
                            if (inbt instanceof CompoundNBT && ((CompoundNBT) inbt).contains("blockPosX"))
                                map.put(new BlockPos(((CompoundNBT) compound).getDouble("blockPosX"), ((CompoundNBT) compound).getDouble("blockPosY"), ((CompoundNBT) compound).getDouble("blockPosZ")), Block.getStateById(compoundNBT.getInt("blockState")));
                        });
                        instance.crazyDiamondBlocks.put(new ChunkPos(((CompoundNBT) compound).getInt("chunkPosX"), ((CompoundNBT) compound).getInt("chunkPosX")), map);
                    }
                });
            }
        }, () -> new Stand(Null()));
    }

    public Map<ChunkPos, Map<BlockPos, BlockState>> getCrazyDiamondBlocks() {
        return crazyDiamondBlocks;
    }

    public void putCrazyDiamondBlock(@Nonnull ChunkPos pos, @Nonnull BlockPos blockPos, @Nonnull BlockState state) {
        if (!crazyDiamondBlocks.containsKey(pos)) {
            Map<BlockPos, BlockState> map = new ConcurrentHashMap<>();
            map.put(blockPos, state);
            crazyDiamondBlocks.put(pos, map);
        } else
            crazyDiamondBlocks.get(pos).put(blockPos, state);
        onDataUpdated();
    }

    public PlayerEntity getPlayer() {
        return player;
    }

    public int getStandID() {
        return standID;
    }

    public void setStandID(int value) {

        if(value != 0){
            Util.giveAdvancement((ServerPlayerEntity) player, "obtainstand");
        }

        this.standID = value;

        switch (value){
            case SILVER_CHARIOT:
                this.maxStandEnergy = 100;
                this.energyRegenerationRate = 10;
                this.currentStandEnergy = 100;
                this.energyRegenerationCooldown = 0;
                break;
            default:
                this.maxStandEnergy = 100;
                this.energyRegenerationRate = 5;
                this.currentStandEnergy = 100;
                this.energyRegenerationCooldown = 0;
                break;
        }
        this.setStandUnsummonedTime(20 * 60 * 5);


        onDataUpdated();
    }

    public int getPlayerStand() {
        return standEntityID;
    }

    public void setPlayerStand(int standEntityID) {
        this.standEntityID = standEntityID;
        onDataUpdated();
    }

    public int getAct() {
        return standAct;
    }

    public void setAct(int standAct) {
        this.standAct = standAct;
        onDataUpdated();
    }

    public boolean getPreventUnsummon() {
        return preventUnsummon;
    }

    public void setPreventUnsummon(boolean preventUnsummonIn) {
        this.preventUnsummon = preventUnsummonIn;
        onDataUpdated();
    }

    public boolean getCounterBuffer() {
        return counterBuffer;
    }

    public void setCounterBuffer(boolean counterBuffer) {
        this.counterBuffer = counterBuffer;
        onDataUpdated();
    }

    public boolean getPreventUnsummon2() {
        return preventUnsummon2;
    }

    public void setPreventUnsummon2(boolean preventUnsummonIn2) {
        this.preventUnsummon2 = preventUnsummonIn2;
        onDataUpdated();
    }

    public boolean getQueueBombRemoval() {
        return queueBombRemoval;
    }

    public void setQueueBombRemoval(boolean queueBombRemovalIn) {
        this.queueBombRemoval = queueBombRemovalIn;
        onDataUpdated();
    }

    public int getMomentum() {
        return momentum;
    }

    public void setMomentum(int momentumIn) {
        this.momentum = momentumIn;
        onDataUpdated();
    }

    public int getBombEntityId(){
        return bombEntityId;
    }

    public void setBombEntityId(int bombEntityIdIn) {
        this.bombEntityId = bombEntityIdIn;
        onDataUpdated();
    }

    public int getEnergyCooldown() {
        return energyRegenerationCooldown;
    }

    public void setEnergyCooldown(int energyCooldown) {
        this.energyRegenerationCooldown = energyCooldown;
        onDataUpdated();
    }

    public int getRestraint() {
        return restraint;
    }

    public void setRestraint(int restraintIn) {
        this.restraint = restraintIn;
        onDataUpdated();
    }

    public int getInvincibleTicks() {
        return invincibleTicks;
    }

    public void setInvincibleTicks(int ticksIn) {
        this.invincibleTicks = ticksIn;
        onDataUpdated();
    }

    public int getReflexCooldown() {
        return reflexCooldown;
    }

    public void setReflexCooldown(int cooldownIn) {
        this.reflexCooldown = cooldownIn;
        onDataUpdated();
    }

    public int getStandUnsummonedTime() {
        return standUnsummonedTime;
    }

    public void setStandUnsummonedTime(int ticksIn) {
        this.standUnsummonedTime = ticksIn;
        onDataUpdated();
    }

    public int getTimeSkipEffectTicker() {
        return epitaphEffectTicker;
    }

    public void setTimeSkipEffectTicker(int effectTickerIn) {
        this.epitaphEffectTicker = effectTickerIn;
        onDataUpdated();
    }

    public int getInstantTimeStopFrame(){return instantTimeStopFrame;}

    public void setInstantTimeStopFrame(int frame) {
        this.instantTimeStopFrame = frame;
        onDataUpdated();
    }

    public void changeAct() {
        standAct++;
        if (standAct == getMaxAct())
            standAct = 0;
        onDataUpdated();
    }

    public boolean hasAct() {
        return Util.StandID.STANDS_WITH_ACTS.contains(getStandID());
    }

    public int getMaxAct() {
        switch (standID) {
            case TUSK_ACT_4:
                return 4;
            case BEACH_BOY:
            case ECHOES_ACT_3:
            case TUSK_ACT_3:
            case MADE_IN_HEAVEN:
                return 3;
            case ECHOES_ACT_2:
            case TUSK_ACT_2:
            case CMOON:
                return 2;
        }
        return 0;
    }

    public boolean getStandOn() {
        return this.standOn;
    }

    public void setStandOn(boolean value) {
        this.standOn = value;
        onDataUpdated();
    }

    public double getCooldown() {
        return this.cooldown;
    }

    public void setCooldown(double cooldown) {
        this.cooldown = cooldown;
        onDataUpdated();
    }

    public double getTimeLeft() {
        return timeLeft;
    }

    public void setTimeLeft(double timeLeft) {
        this.timeLeft = timeLeft;
        onDataUpdated();
    }

    public String getDiavolo() {
        return diavolo;
    }

    public void setDiavolo(String truth) {
        this.diavolo = truth;
        onDataUpdated();
    }

    public boolean getAbility() {
        return ability;
    }

    public void setAbility(boolean ability) {
        this.ability = ability;
        onDataUpdated();
    }

    public boolean getExperiencingTimeSkip() {
        return experiencingTimeSkip;
    }

    public void setExperiencingTimeSkip(boolean skip) {
        this.experiencingTimeSkip = skip;
        onDataUpdated();
    }

    public boolean getExperiencingTimeStop() {
        return experiencingTimeStop;
    }

    public void setExperiencingTimeStop(boolean stop) {
        this.experiencingTimeStop = stop;
        onDataUpdated();
    }

    public int getTransformed() {
        return transformed;
    }

    public void setTransformed(int value) {
        this.transformed = value;
        onDataUpdated();
    }

    public void addTransformed(int addition) {
        this.transformed += addition;
        onDataUpdated();
    }

    public boolean getNoClip() {
        return noClip;
    }

    public void setNoClip(boolean noClip) {
        this.noClip = noClip;
        onDataUpdated();
    }

    public boolean getAbilityActive() {
        return abilityActive;
    }

    public void setAbilityActive(boolean abilityActive) {
        this.abilityActive = abilityActive;
        onDataUpdated();
    }

    public double getInvulnerableTicks() {
        return invulnerableTicks;
    }

    public void setInvulnerableTicks(double invulnerableTicks) {
        this.invulnerableTicks = invulnerableTicks;
        onDataUpdated();
    }

    public float getStandDamage() {
        return standDamage;
    }

    public void setStandDamage(float standDamage) {
        this.standDamage = standDamage;
        onDataUpdated();
    }

    public boolean isCharging() {
        return charging;
    }

    public void setCharging(boolean charging) {
        this.charging = charging;
        onDataUpdated();
    }

    public int getAbilityUseCount() {
        return abilityUseCount;
    }


    public void setAbilityUseCount(int abilityUseCount) {
        this.abilityUseCount = abilityUseCount;
        onDataUpdated();
    }

    public double getCurrentStandEnergy() {
        return currentStandEnergy;
    }

    public double getMaxStandEnergy() {
        return maxStandEnergy;
    }
    public double getEnergyRegenerationRate() {
        return energyRegenerationRate;
    }

    public void setCurrentStandEnergy(double input) {
        this.currentStandEnergy = input;
        onDataUpdated();
    }
    public void setMaxStandEnergy(double input) {
        this.maxStandEnergy = input;
        onDataUpdated();
    }
    public void setEnergyRegenerationRate(double input) {
        this.energyRegenerationRate = input;
        onDataUpdated();
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public void setBlockPos(BlockPos blockPos) {
        this.blockPos = blockPos;
        onDataUpdated();
    }

    public int getPrevExperiencePoints() {
        return prevExperiencePoints;
    }

    public List<ChunkPos> getAffectedChunkList() {
        return affectedChunkList;
    }

    public void addAffectedChunk(ChunkPos pos) {
        affectedChunkList.add(pos);
        onDataUpdated();
    }

    public void addExperiencePoints(int experiencePoints) {
        this.prevExperiencePoints = this.experiencePoints;
        this.experiencePoints += experiencePoints;
        onDataUpdated();
    }

    public int getExperiencePoints() {
        return experiencePoints;
    }

    public long getGameTime() {
        return gameTime;
    }

    public void setGameTime(long time) {
        this.gameTime = time;
        onDataUpdated();
    }

    public long getDayTime() {
        return dayTime;
    }

    public void setDayTime(long time) {
        this.dayTime = time;
        onDataUpdated();
    }

    public int getAbilitiesUnlocked() {
        return abilitiesUnlocked;
    }

    public void addAbilityUnlocked(int amount) {
        abilitiesUnlocked += amount;
        onDataUpdated();
    }

    public void clone(Stand stand) {
        standID = stand.getStandID();
        standAct = stand.getAct();
        standOn = stand.getStandOn();
        cooldown = stand.getCooldown();
        timeLeft = stand.getTimeLeft();
        transformed = stand.getTransformed();
        diavolo = stand.getDiavolo();
        ability = stand.getAbility();
        standEntityID = stand.getPlayerStand();
        abilityActive = stand.getAbilityActive();
        invulnerableTicks = stand.getInvulnerableTicks();
        standDamage = stand.getStandDamage();
        charging = stand.isCharging();
        abilityUseCount = stand.getAbilityUseCount();
        affectedChunkList = stand.getAffectedChunkList();
        experiencePoints = stand.getExperiencePoints();
        prevExperiencePoints = stand.getPrevExperiencePoints();
        abilitiesUnlocked = stand.getAbilitiesUnlocked();
        preventUnsummon = stand.getPreventUnsummon();
        preventUnsummon2 = stand.getPreventUnsummon2();
        momentum = stand.getMomentum();
        restraint = stand.getRestraint();
        epitaphEffectTicker = stand.getTimeSkipEffectTicker();
        currentStandEnergy = stand.getCurrentStandEnergy();
        maxStandEnergy = stand.getMaxStandEnergy();
        energyRegenerationRate = stand.getEnergyRegenerationRate();
        energyRegenerationCooldown = stand.getEnergyCooldown();
        counterBuffer = stand.getCounterBuffer();
        experiencingTimeStop = stand.getExperiencingTimeStop();
        experiencingTimeSkip = stand.getExperiencingTimeSkip();
        instantTimeStopFrame = stand.getInstantTimeStopFrame();
        invincibleTicks = stand.getInvincibleTicks();
        standUnsummonedTime = stand.getStandUnsummonedTime();
        reflexCooldown = stand.getReflexCooldown();
        bombEntityId = stand.getBombEntityId();
        queueBombRemoval = stand.getQueueBombRemoval();
        onDataUpdated();
    }

    public void removeStand(boolean evolution) {

        List<int[]> standAssignments = Collections.singletonList(STANDS);

        if(JojoBizarreSurvivalConfig.COMMON.uniqueStandMode.get() && !evolution) {
            Objects.requireNonNull(player.getServer()).getWorld(DimensionType.OVERWORLD);
            StandPerWorldCapability.getLazyOptional(player.getServer().getWorld(DimensionType.OVERWORLD)).ifPresent(uniqueStandHandler -> {
                if (uniqueStandHandler.getTakenStandIDs().contains(ArrayUtils.indexOf(STANDS, getStandID()))) {
                    if (!isStandThatEvolves(getStandID())) {
                        uniqueStandHandler.removeTakenStandId(ArrayUtils.indexOf(STANDS, getStandID()));
                    } else {
                        if (getStandID() == MADE_IN_HEAVEN || getStandID() == CMOON || getStandID() == WHITESNAKE) {
                            uniqueStandHandler.removeTakenStandId(ArrayUtils.indexOf(STANDS, WHITESNAKE));
                        }
                        if (getStandID() == TUSK_ACT_1 || getStandID() == TUSK_ACT_2 || getStandID() == TUSK_ACT_3 || getStandID() == TUSK_ACT_4) {
                            uniqueStandHandler.removeTakenStandId(ArrayUtils.indexOf(STANDS, TUSK_ACT_1));
                        }
                        if (getStandID() == ECHOES_ACT_1 || getStandID() == ECHOES_ACT_2 || getStandID() == ECHOES_ACT_3) {
                            uniqueStandHandler.removeTakenStandId(ArrayUtils.indexOf(STANDS, ECHOES_ACT_1));
                        }
                    }
                }
            });
        }

        standOn = false;
        standAct = 0;
        standID = 0;
        cooldown = 0;
        timeLeft = 1000;
        transformed = 0;
        diavolo = "";
        ability = false;
        noClip = false;
        standEntityID = 0;
        abilityActive = false;
        invulnerableTicks = 0;
        standDamage = 0;
        charging = false;
        abilityUseCount = 0;
        affectedChunkList = new ArrayList<>();
        experiencePoints = 0;
        prevExperiencePoints = 0;
        abilitiesUnlocked = 0;
        preventUnsummon = false;
        preventUnsummon2 = false;
        momentum = 0;
        restraint = 0;
        epitaphEffectTicker = 0;
        experiencingTimeSkip = false;
        experiencingTimeStop = false;
        counterBuffer = false;
        currentStandEnergy = 100;
        maxStandEnergy = 100;
        energyRegenerationCooldown = 0;
        energyRegenerationRate = 5;
        instantTimeStopFrame = 0;
        invincibleTicks = 0;
        standUnsummonedTime = 0;
        reflexCooldown = 0;
        bombEntityId = 0;
        queueBombRemoval = false;
        onDataUpdated();
    }

    /**
     * Called to update the {@link Capability} to the client.
     */
    public void onDataUpdated() {
        if (!player.world.isRemote)
            DiamondIsUncraftable.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new SSyncStandCapabilityPacket(this));
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        return capability == STAND ? holder.cast() : LazyOptional.empty();
    }

    @Override
    public INBT serializeNBT() {
        return Stand.STAND.getStorage().writeNBT(STAND, holder.orElseThrow(() -> new IllegalArgumentException("LazyOptional is empty!")), null);
    }

    @Override
    public void deserializeNBT(INBT nbt) {
        Stand.STAND.getStorage().readNBT(STAND, holder.orElseThrow(() -> new IllegalArgumentException("LazyOptional is empty!")), null, nbt);
    }
}
