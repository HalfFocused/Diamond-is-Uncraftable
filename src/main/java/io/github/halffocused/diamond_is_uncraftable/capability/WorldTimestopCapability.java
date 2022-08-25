package io.github.halffocused.diamond_is_uncraftable.capability;

import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import io.github.halffocused.diamond_is_uncraftable.network.message.server.SSyncStandPerWorldCapabilityPacket;
import io.github.halffocused.diamond_is_uncraftable.network.message.server.SSyncWorldTimestopCapability;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
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
import java.util.UUID;

import static io.github.halffocused.diamond_is_uncraftable.util.Util.Null;

public class WorldTimestopCapability implements ICapabilitySerializable<INBT> {
    @CapabilityInject(WorldTimestopCapability.class)
    public static final Capability<WorldTimestopCapability> WORLD = Null();
    World world;
    private LazyOptional<WorldTimestopCapability> holder = LazyOptional.of(() -> new WorldTimestopCapability(getWorld()));
    ArrayList<TimestoppedChunk> timestoppedChunks = new ArrayList<>();

    public WorldTimestopCapability(@Nonnull World world) {
        this.world = world;
    }

    public static LazyOptional<WorldTimestopCapability> getLazyOptional(World world) {
        return world.getCapability(WORLD);
    }

    public static WorldTimestopCapability getCapabilityFromWorld(World world) {
        return world.getCapability(WORLD).orElse(new WorldTimestopCapability(world));
    }

    public static void register() {
        CapabilityManager.INSTANCE.register(WorldTimestopCapability.class, new Capability.IStorage<WorldTimestopCapability>() {
            @Nonnull
            @Override
            public INBT writeNBT(Capability<WorldTimestopCapability> capability, WorldTimestopCapability instance, Direction side) {
                CompoundNBT nbt = new CompoundNBT();

                ListNBT timestoppedChunks = new ListNBT();
                instance.getTimestoppedChunkPosList().forEach(timestoppedChunk -> {
                    CompoundNBT chunkNBT = new CompoundNBT();
                    chunkNBT.putUniqueId("masterUUID", timestoppedChunk.getUUID());
                    chunkNBT.putInt("chunkX", timestoppedChunk.pos.x);
                    chunkNBT.putInt("chunkZ", timestoppedChunk.pos.z);
                });
                nbt.put("timestoppedChunks", timestoppedChunks);

                return nbt;
            }

            @Override
            public void readNBT(Capability<WorldTimestopCapability> capability, WorldTimestopCapability instance, Direction side, INBT nbt) {
                CompoundNBT compoundNBT = (CompoundNBT) nbt;

                compoundNBT.getList("timestoppedChunks", Constants.NBT.TAG_COMPOUND).forEach(compound -> {
                    if(compound instanceof CompoundNBT){
                        CompoundNBT chunkNBT = (CompoundNBT) compound;
                        instance.addTimestoppedChunk(new ChunkPos(chunkNBT.getInt("chunkX"), chunkNBT.getInt("chunkZ")), chunkNBT.getUniqueId("masterUUID"));
                    }
                });
            }
        }, () -> new WorldTimestopCapability(Null()));
    }

    public ArrayList<TimestoppedChunk> getTimestoppedChunkPosList(){
        return timestoppedChunks;
    }

    public void addTimestoppedChunk(ChunkPos posIn, UUID uuidIn){
        TimestoppedChunk chunk = new TimestoppedChunk(posIn, uuidIn);
        if(!timestoppedChunks.contains(chunk)) {
            timestoppedChunks.add(chunk);
        }
        onDataUpdated();
    }

    public void removeTimestoppedChunk(ChunkPos posIn, UUID uuidIn){
        TimestoppedChunk chunk = new TimestoppedChunk(posIn, uuidIn);
        timestoppedChunks.removeIf(timestoppedChunk -> timestoppedChunk.equals(chunk));
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
            DiamondIsUncraftable.INSTANCE.send(PacketDistributor.ALL.noArg(), new SSyncWorldTimestopCapability(this));
    }

    @Override
    public INBT serializeNBT() {
        return WorldTimestopCapability.WORLD.getStorage().writeNBT(WORLD, holder.orElseThrow(() -> new IllegalArgumentException("LazyOptional is empty!")), null);
    }

    @Override
    public void deserializeNBT(INBT nbt) {
        WorldTimestopCapability.WORLD.getStorage().readNBT(WORLD, holder.orElseThrow(() -> new IllegalArgumentException("LazyOptional is empty!")), null, nbt);
    }

    public static class TimestoppedChunk{
        private UUID uuid;
        private ChunkPos pos;

        public TimestoppedChunk(ChunkPos posIn, UUID uuidIn){
            pos = posIn;
            uuid = uuidIn;
        }

        public ChunkPos getChunkPos(){
            return pos;
        }

        public UUID getUUID(){
            return uuid;
        }

        public boolean equals(TimestoppedChunk other){
            return this.getChunkPos().equals(other.getChunkPos()) && this.getUUID().equals(other.getUUID());
        }
    }

}
