package io.github.halffocused.diamond_is_uncraftable.world;

import java.util.function.Supplier;

import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;

public class UniqueStandWorldData extends WorldSavedData implements Supplier {
    public CompoundNBT data = new CompoundNBT();

    public UniqueStandWorldData()
    {
        super(DiamondIsUncraftable.MOD_ID);
    }

    public UniqueStandWorldData(String name)
    {
        super(name);
    }

    @Override
    public void read(CompoundNBT nbt)
    {
        data = nbt.getCompound("TakenStands");
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt)
    {
        nbt.put("TakenStands", data);
        return nbt;
    }

    public static UniqueStandWorldData forWorld(ServerWorld world)
    {
        DimensionSavedDataManager storage = world.getSavedData();
        Supplier<UniqueStandWorldData> sup = new UniqueStandWorldData();

        return storage.getOrCreate(sup, DiamondIsUncraftable.MOD_ID);
    }

    @Override
    public Object get()
    {
        return this;
    }
}