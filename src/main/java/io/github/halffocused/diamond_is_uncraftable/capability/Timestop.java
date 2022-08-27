package io.github.halffocused.diamond_is_uncraftable.capability;

import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
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
public class Timestop implements ICapabilitySerializable<INBT> {
    @CapabilityInject(Timestop.class)
    public static final Capability<Timestop> TIMESTOP = Null();
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
    private Map<String, Float> damage = new ConcurrentHashMap<>();
    private LazyOptional<Timestop> holder = LazyOptional.of(() -> new Timestop(getEntity()));

    public Timestop(@Nonnull Entity entity) {
        this.entity = entity;
    }

    public static Timestop getCapabilityFromEntity(Entity entity) {
        return entity.getCapability(TIMESTOP).orElse(new Timestop(entity));
    }

    public static LazyOptional<Timestop> getLazyOptional(Entity entity) {
        return entity.getCapability(TIMESTOP);
    }

    public static void register() {
        CapabilityManager.INSTANCE.register(Timestop.class, new Capability.IStorage<Timestop>() {
            @Override
            public INBT writeNBT(Capability<Timestop> capability, Timestop instance, Direction side) {
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
                ListNBT listNBT = new ListNBT();
                instance.getDamage().forEach((source, amount) -> {
                    CompoundNBT compoundNBT = new CompoundNBT();
                    compoundNBT.putString("source", source);
                    compoundNBT.putFloat("amount", amount);
                    listNBT.add(compoundNBT);
                });
                nbt.put("damage", listNBT);
                return nbt;
            }

            @Override
            public void readNBT(Capability<Timestop> capability, Timestop instance, Direction side, INBT nbt) {
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
                compoundNBT.getList("damage", Constants.NBT.TAG_COMPOUND).forEach(compound -> {
                    if (compound instanceof CompoundNBT && ((CompoundNBT) compound).contains("source") && ((CompoundNBT) compound).contains("amount"))
                        instance.getDamage().put(((CompoundNBT) compound).getString("source"), ((CompoundNBT) compound).getFloat("amount"));
                });
            }
        }, () -> new Timestop(Null()));
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

    public Map<String, Float> getDamage() {
        return damage;
    }

    public void setDamage(Map<String, Float> damage) {
        this.damage = damage;
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

    public void putDamage(Map<String, Float> damage) {
        this.damage = damage;
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
                fire == 0;
    }

    public void onDataUpdated() {
        if (entity != null)
            if (!entity.world.isRemote)
                DiamondIsUncraftable.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), new SSyncTimestopCapabilityPacket(this));
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
        this.damage = new ConcurrentHashMap<>();
        onDataUpdated();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        return capability == TIMESTOP ? holder.cast() : LazyOptional.empty();
    }

    @Override
    public INBT serializeNBT() {
        return TIMESTOP.getStorage().writeNBT(TIMESTOP, holder.orElseThrow(() -> new IllegalArgumentException("LazyOptional is empty!")), null);
    }

    @Override
    public void deserializeNBT(INBT nbt) {
        TIMESTOP.getStorage().readNBT(TIMESTOP, holder.orElseThrow(() -> new IllegalArgumentException("LazyOptional is empty!")), null, nbt);
    }
}
