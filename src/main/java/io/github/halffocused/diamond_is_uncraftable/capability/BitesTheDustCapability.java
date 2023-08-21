package io.github.halffocused.diamond_is_uncraftable.capability;

import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import io.github.halffocused.diamond_is_uncraftable.network.message.server.SSyncBitesTheDustCapabilityPacket;
import io.github.halffocused.diamond_is_uncraftable.network.message.server.SSyncTimestopCapabilityPacket;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.github.halffocused.diamond_is_uncraftable.util.Util.Null;

@SuppressWarnings("unused")
public class BitesTheDustCapability implements ICapabilitySerializable<INBT> {
    @CapabilityInject(BitesTheDustCapability.class)
    public static final Capability<BitesTheDustCapability> BITES_THE_DUST = Null();
    private Entity entity;
    private double posX;
    private double posY;
    private double posZ;
    private double motionX;
    private double motionY;
    private double motionZ;
    private float rotationYaw;
    private float rotationPitch;
    private float rotationYawHead;
    private float fallDistance;
    private int fuse;
    private int fire;
    private int age;
    private float health;
    private int hunger;
    private int fateTicks;
    private boolean isMaster;
    private int ticksRemaining;
    private LazyOptional<BitesTheDustCapability> holder = LazyOptional.of(() -> new BitesTheDustCapability(getEntity()));

    public BitesTheDustCapability(@Nonnull Entity entity) {
        this.entity = entity;
    }

    public static BitesTheDustCapability getCapabilityFromEntity(Entity entity) {
        return entity.getCapability(BITES_THE_DUST).orElse(new BitesTheDustCapability(entity));
    }

    public static LazyOptional<BitesTheDustCapability> getLazyOptional(Entity entity) {
        return entity.getCapability(BITES_THE_DUST);
    }

    public static void register() {
        CapabilityManager.INSTANCE.register(BitesTheDustCapability.class, new Capability.IStorage<BitesTheDustCapability>() {
            @Override
            public INBT writeNBT(Capability<BitesTheDustCapability> capability, BitesTheDustCapability instance, Direction side) {
                CompoundNBT nbt = new CompoundNBT();
                nbt.putDouble("posX", instance.getPosX());
                nbt.putDouble("posY", instance.getPosY());
                nbt.putDouble("posZ", instance.getPosZ());
                nbt.putDouble("motionX", instance.getMotionX());
                nbt.putDouble("motionY", instance.getMotionY());
                nbt.putDouble("motionZ", instance.getMotionZ());
                nbt.putFloat("rotationYaw", instance.getRotationYaw());
                nbt.putFloat("rotationPitch", instance.getRotationPitch());
                nbt.putFloat("rotationYawHead", instance.getRotationYawHead());
                nbt.putFloat("fallDistance", instance.getFallDistance());
                nbt.putInt("fuse", instance.getFuse());
                nbt.putInt("fire", instance.getFire());
                nbt.putInt("age", instance.getAge());
                nbt.putFloat("health", instance.getHealth());
                nbt.putInt("hunger", instance.getHunger());
                nbt.putInt("fateTicks", instance.getFateTicks());
                nbt.putBoolean("isMaster", instance.getIsMaster());
                return nbt;
            }

            @Override
            public void readNBT(Capability<BitesTheDustCapability> capability, BitesTheDustCapability instance, Direction side, INBT nbt) {
                CompoundNBT compoundNBT = (CompoundNBT) nbt;
                instance.posX = (compoundNBT.getDouble("posX"));
                instance.posY = (compoundNBT.getDouble("posY"));
                instance.posZ = (compoundNBT.getDouble("posZ"));
                instance.motionX = (compoundNBT.getDouble("motionX"));
                instance.motionY = (compoundNBT.getDouble("motionY"));
                instance.motionZ = (compoundNBT.getDouble("motionZ"));
                instance.rotationYaw = (compoundNBT.getFloat("rotationYaw"));
                instance.rotationPitch = (compoundNBT.getFloat("rotationPitch"));
                instance.rotationYawHead = (compoundNBT.getFloat("rotationYawHead"));
                instance.fallDistance = (compoundNBT.getInt("fallDistance"));
                instance.fuse = (compoundNBT.getInt("fuse"));
                instance.fire = (compoundNBT.getInt("fire"));
                instance.age = (compoundNBT.getInt("age"));
                instance.health = (compoundNBT.getFloat("health"));
                instance.hunger = (compoundNBT.getInt("hunger"));
                instance.fateTicks = (compoundNBT.getInt("fateTicks"));
                instance.isMaster = (compoundNBT.getBoolean("isMaster"));
            }
        }, () -> new BitesTheDustCapability(Null()));
    }

    public Entity getEntity() {
        return entity;
    }

    public double getPosX() {
        return posX;
    }

    public double getPosY() {
        return posY;
    }

    public double getPosZ() {
        return posZ;
    }

    public void setPosition(double posX, double posY, double posZ) {
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        onDataUpdated();
    }

    public double getMotionX() {
        return motionX;
    }

    public double getMotionY() {
        return motionY;
    }

    public double getMotionZ() {
        return motionZ;
    }

    public void setMotion(double motionX, double motionY, double motionZ) {
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
        onDataUpdated();
    }

    public float getRotationYaw() {
        return rotationYaw;
    }

    public float getRotationPitch() {
        return rotationPitch;
    }

    public float getRotationYawHead() {
        return rotationYawHead;
    }

    public void setRotation(float rotationYaw, float rotationPitch, float rotationYawHead) {
        this.rotationYaw = rotationYaw;
        this.rotationPitch = rotationPitch;
        this.rotationYawHead = rotationYawHead;
        onDataUpdated();
    }

    public float getFallDistance() {
        return fallDistance;
    }

    public void setFallDistance(float fallDistance) {
        this.fallDistance = fallDistance;
        onDataUpdated();
    }

    public int getFuse() {
        return fuse;
    }

    public void setFuse(int fuse) {
        this.fuse = fuse;
        onDataUpdated();
    }

    public boolean getIsMaster() {
        return isMaster;
    }

    public void setIsMaster(boolean isMaster) {
        this.isMaster = isMaster;
        onDataUpdated();
    }

    public float getHealth() {
        return health;
    }

    public void setHealth(float health) {
        this.health = health;
        onDataUpdated();
    }

    public int getHunger() {
        return hunger;
    }

    public void setHunger(int hunger) {
        this.hunger = hunger;
        onDataUpdated();
    }

    public int getFateTicks() {
        return fateTicks;
    }

    public void setFateTicks(int fateTicks) {
        this.fateTicks = fateTicks;
        onDataUpdated();
    }

    public int getFire() {
        return fire;
    }

    public void setFire(int fire) {
        this.fire = fire;
        onDataUpdated();
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
        onDataUpdated();
    }

    public void putPosX(double posX) {
        this.posX = posX;
    }

    public void putPosY(double posY) {
        this.posY = posY;
    }

    public void putPosZ(double posZ) {
        this.posZ = posZ;
    }

    public void putMotionX(double motionX) {
        this.motionX = motionX;
    }

    public void putMotionY(double motionY) {
        this.motionY = motionY;
    }

    public void putMotionZ(double motionZ) {
        this.motionZ = motionZ;
    }

    public void putRotationYaw(float rotationYaw) {
        this.rotationYaw = rotationYaw;
    }

    public void putRotationPitch(float rotationPitch) {
        this.rotationPitch = rotationPitch;
    }

    public void putRotationYawHead(float rotationYawHead) {
        this.rotationYawHead = rotationYawHead;
    }

    public void putFallDistance(float fallDistance) {
        this.fallDistance = fallDistance;
    }

    public void putFuse(int fuse) {
        this.fuse = fuse;
    }

    public void putFire(int fire) {
        this.fire = fire;
    }

    public void putAge(int age) {
        this.age = age;
    }

    public void putHealth(float health){
        this.health = health;
    }

    public void putHunger(int hunger){
        this.hunger = hunger;
    }

    public void putFateTicks(int fateTicks){
        this.fateTicks = fateTicks;
    }

    public boolean isEmpty() {
        return posX == 0 &&
                posY == 0 &&
                posZ == 0 &&
                motionX == 0 &&
                motionY == 0 &&
                motionZ == 0 &&
                rotationYaw == 0 &&
                rotationPitch == 0 &&
                rotationYawHead == 0 &&
                fallDistance == 0 &&
                fuse == 0 &&
                fire == 0 &&
                health == 0 &&
                hunger == 0 &&
                fateTicks == 0;
    }

    public void onDataUpdated() {
        if (entity != null)
            if (!entity.world.isRemote)
                DiamondIsUncraftable.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), new SSyncBitesTheDustCapabilityPacket(this));
    }

    public void clear() {
        this.posX = 0;
        this.posY = 0;
        this.posZ = 0;
        this.motionX = 0;
        this.motionY = 0;
        this.motionZ = 0;
        this.rotationYaw = 0;
        this.rotationPitch = 0;
        this.rotationYawHead = 0;
        this.fallDistance = 0;
        this.fuse = 0;
        this.fire = 0;
        this.health = 0;
        this.hunger = 0;
        this.fateTicks = 0;
        onDataUpdated();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        return capability == BITES_THE_DUST ? holder.cast() : LazyOptional.empty();
    }

    @Override
    public INBT serializeNBT() {
        return BITES_THE_DUST.getStorage().writeNBT(BITES_THE_DUST, holder.orElseThrow(() -> new IllegalArgumentException("LazyOptional is empty!")), null);
    }

    @Override
    public void deserializeNBT(INBT nbt) {
        BITES_THE_DUST.getStorage().readNBT(BITES_THE_DUST, holder.orElseThrow(() -> new IllegalArgumentException("LazyOptional is empty!")), null, nbt);
    }
}
