package io.github.halffocused.diamond_is_uncraftable.capability;

import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import io.github.halffocused.diamond_is_uncraftable.network.message.server.SSyncStandPerWorldCapabilityPacket;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import static io.github.halffocused.diamond_is_uncraftable.util.Util.Null;

public class StandPerWorldCapability implements ICapabilitySerializable<INBT> {
    @CapabilityInject(StandPerWorldCapability.class)
    public static final Capability<StandPerWorldCapability> WORLD = Null();
    World world;
    private LazyOptional<StandPerWorldCapability> holder = LazyOptional.of(() -> new StandPerWorldCapability(getWorld()));
    private List<Integer> takenStandIDs = new ArrayList<>();

    public StandPerWorldCapability(@Nonnull World world) {
        this.world = world;
    }

    public static LazyOptional<StandPerWorldCapability> getLazyOptional(World world) {
        return world.getCapability(WORLD);
    }

    public static StandPerWorldCapability getCapabilityFromWorld(World world) {
        return world.getCapability(WORLD).orElse(new StandPerWorldCapability(world));
    }

    public static void register() {
        CapabilityManager.INSTANCE.register(StandPerWorldCapability.class, new Capability.IStorage<StandPerWorldCapability>() {
            @Nonnull
            @Override
            public INBT writeNBT(Capability<StandPerWorldCapability> capability, StandPerWorldCapability instance, Direction side) {
                CompoundNBT nbt = new CompoundNBT();

                ListNBT takenStandIDs = new ListNBT();
                instance.getTakenStandIDs().forEach(id -> {
                    CompoundNBT compoundNBT = new CompoundNBT();
                    compoundNBT.putInt("StandID", id);
                    takenStandIDs.add(compoundNBT);
                });
                nbt.put("TakenStandIDs", takenStandIDs);

                return nbt;
            }

            @Override
            public void readNBT(Capability<StandPerWorldCapability> capability, StandPerWorldCapability instance, Direction side, INBT nbt) {
                CompoundNBT compoundNBT = (CompoundNBT) nbt;

                compoundNBT.getList("TakenStandIDs", Constants.NBT.TAG_COMPOUND).forEach(inbt -> {
                    if (inbt instanceof CompoundNBT && ((CompoundNBT) inbt).contains("StandID"))
                        instance.addTakenStandId(((CompoundNBT) inbt).getInt("StandID"));
                });
            }
        }, () -> new StandPerWorldCapability(Null()));
    }

    public List<Integer> getTakenStandIDs() {
        return takenStandIDs;
    }


    public void addTakenStandId(int id) {
        takenStandIDs.add(id);
        onDataUpdated();
    }

    public void removeTakenStandId(int id) {
        takenStandIDs.remove(new Integer(id));
        onDataUpdated();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        return capability == WORLD ? holder.cast() : LazyOptional.empty();
    }

    public World getWorld() {
        return world;
    }

    public void onDataUpdated() {
        if (!world.isRemote)
            DiamondIsUncraftable.INSTANCE.send(PacketDistributor.ALL.noArg(), new SSyncStandPerWorldCapabilityPacket(this));
    }

    @Override
    public INBT serializeNBT() {
        return StandPerWorldCapability.WORLD.getStorage().writeNBT(WORLD, holder.orElseThrow(() -> new IllegalArgumentException("LazyOptional is empty!")), null);
    }

    @Override
    public void deserializeNBT(INBT nbt) {
        StandPerWorldCapability.WORLD.getStorage().readNBT(WORLD, holder.orElseThrow(() -> new IllegalArgumentException("LazyOptional is empty!")), null, nbt);
    }

}
