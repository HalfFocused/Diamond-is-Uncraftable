package io.github.halffocused.diamond_is_uncraftable.capability;

import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import io.github.halffocused.diamond_is_uncraftable.network.message.server.SSyncCombatCapabilityCombat;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static io.github.halffocused.diamond_is_uncraftable.util.Util.Null;

/**
 * The {@link Capability} used for storing combat related values like hit-stun.
 */
public class CombatCapability implements ICapabilitySerializable<INBT> {
    @CapabilityInject(CombatCapability.class)
    public static final Capability<CombatCapability> COMBAT = Null(); //Null method suppresses warnings
    private final Entity entity;
    private int hitstun = 0;

    private LazyOptional<CombatCapability> holder = LazyOptional.of(() -> new CombatCapability(getEntity()));

    public CombatCapability(@Nonnull Entity entityIn) {
        this.entity = entityIn;
    }

    public static CombatCapability getCapabilityFromEntity(Entity entityIn) {
        return entityIn.getCapability(COMBAT).orElse(new CombatCapability(entityIn));
    }

    public static LazyOptional<CombatCapability> getLazyOptional(Entity entityIn) {
        return entityIn.getCapability(COMBAT);
    }

    public static void register() {
        CapabilityManager.INSTANCE.register(CombatCapability.class, new Capability.IStorage<CombatCapability>() {
            @Nonnull
            @Override
            public INBT writeNBT(Capability<CombatCapability> capability, CombatCapability instance, Direction side) {
                CompoundNBT nbt = new CompoundNBT();
                nbt.putInt("hitstun", instance.getHitstun());
                return nbt;
            }

            @Override
            public void readNBT(Capability<CombatCapability> capability, CombatCapability instance, Direction side, INBT nbt) {
                CompoundNBT compoundNBT = (CompoundNBT) nbt;
                instance.hitstun = compoundNBT.getInt("hitstun");
            }
        }, () -> new CombatCapability(Null()));
    }

    public int getHitstun() {
        return this.hitstun;
    }

    public void setHitstun(int hitStunIn) {
        this.hitstun = hitStunIn;
        onDataUpdated();
    }

    public Entity getEntity() {
        return entity;
    }

    /**
     * Called to update the {@link Capability} to the client.
     */
    public void onDataUpdated() {
        if (!entity.world.isRemote)
            DiamondIsUncraftable.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), new SSyncCombatCapabilityCombat(this));
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        return capability == COMBAT ? holder.cast() : LazyOptional.empty();
    }

    @Override
    public INBT serializeNBT() {
        return CombatCapability.COMBAT.getStorage().writeNBT(COMBAT, holder.orElseThrow(() -> new IllegalArgumentException("LazyOptional is empty!")), null);
    }

    @Override
    public void deserializeNBT(INBT nbt) {
        CombatCapability.COMBAT.getStorage().readNBT(COMBAT, holder.orElseThrow(() -> new IllegalArgumentException("LazyOptional is empty!")), null, nbt);
    }
}
