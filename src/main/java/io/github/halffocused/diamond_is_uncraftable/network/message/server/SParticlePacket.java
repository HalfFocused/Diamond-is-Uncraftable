package io.github.halffocused.diamond_is_uncraftable.network.message.server;

import io.github.halffocused.diamond_is_uncraftable.network.message.IMessage;
import io.github.halffocused.diamond_is_uncraftable.particle.ParticleList;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.*;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Random;
import java.util.function.Supplier;

public class SParticlePacket implements IMessage<SParticlePacket> {
    private double Particle;
    private double X;
    private double Y;
    private double Z;
    private double dX;
    private double dY;
    private double dZ;
    private int amount;

    public SParticlePacket() {
    }

    public SParticlePacket(double particle, double x, double y, double z, double dx, double dy, double dz, int amount) {
        this.Particle = particle;
        this.X=x;
        this.Y=y;
        this.Z=z;
        this.dX=dx;
        this.dY=dy;
        this.dZ=dz;
        this.amount = amount;
    }

    @Override
    public void encode(SParticlePacket message, PacketBuffer buffer) {
        buffer.writeDouble(message.Particle);
        buffer.writeDouble(message.X);
        buffer.writeDouble(message.Y);
        buffer.writeDouble(message.Z);
        buffer.writeDouble(message.dX);
        buffer.writeDouble(message.dY);
        buffer.writeDouble(message.dZ);
        buffer.writeInt(message.amount);
    }

    @Override
    public SParticlePacket decode(PacketBuffer buffer) {
        return new SParticlePacket(buffer.readDouble(),buffer.readDouble(),buffer.readDouble(),buffer.readDouble(),buffer.readDouble(),buffer.readDouble(),buffer.readDouble(),buffer.readInt());
    }

    @Override
    public void handle(SParticlePacket message, Supplier<NetworkEvent.Context> ctx) {
        if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            Random rand = new Random();
            if(message.Particle == 1) {
                for (int i = 0; i < message.amount; i++) {
                    double d0 = message.X + ((rand.nextInt((int) (message.dX * 10)) - (message.dX * 5))) / 10.0;
                    double d1 = message.Y + ((rand.nextInt((int) (message.dY * 10)) - (message.dY * 5))) / 10.0;
                    double d2 = message.Z + ((rand.nextInt((int) (message.dZ * 10)) - (message.dZ * 5))) / 10.0;
                    assert Minecraft.getInstance().world != null;
                    Minecraft.getInstance().world.addParticle(ParticleList.MENACING_PARTICLE.get(), d0, d1, d2, 0.1, 0.1, 0.1);
                }
            }else if(message.Particle == 2) {
                for (int i = 0; i < message.amount; i++) {
                    double d0 = message.X + ((rand.nextInt((int) (message.dX * 10)) - (message.dX * 5))) / 10.0;
                    double d1 = message.Y + ((rand.nextInt((int) (message.dY * 10)) - (message.dY * 5))) / 10.0;
                    double d2 = message.Z + ((rand.nextInt((int) (message.dZ * 10)) - (message.dZ * 5))) / 10.0;
                    assert Minecraft.getInstance().world != null;
                    Minecraft.getInstance().world.addParticle(ParticleTypes.SWEEP_ATTACK, d0, d1, d2, 0.1, 0.1, 0.1);
                }
            }else if(message.Particle == 3) {
                for (int i = 0; i < message.amount; i++) {
                    double d0 = message.X + ((rand.nextInt((int) (message.dX * 10)) - (message.dX * 5))) / 10.0;
                    double d1 = message.Y + ((rand.nextInt((int) (message.dY * 10)) - (message.dY * 5))) / 10.0;
                    double d2 = message.Z + ((rand.nextInt((int) (message.dZ * 10)) - (message.dZ * 5))) / 10.0;
                    assert Minecraft.getInstance().world != null;
                    Minecraft.getInstance().world.addParticle(ParticleTypes.EXPLOSION, d0, d1, d2, 0.1, 0.1, 0.1);
                }
            }else if(message.Particle == 4) {
                double bloodExplosionStrength = 3.35;
                for (int i = 0; i < message.amount; i++) {
                    double d0 = message.X + ((rand.nextInt((int) (message.dX * 10)) - (message.dX * 5))) / 10.0;
                    double d1 = message.Y + ((rand.nextInt((int) (message.dY * 10)) - (message.dY * 5))) / 10.0;
                    double d2 = message.Z + ((rand.nextInt((int) (message.dZ * 10)) - (message.dZ * 5))) / 10.0;
                    assert Minecraft.getInstance().world != null;
                    Minecraft.getInstance().world.addParticle(new BlockParticleData(ParticleTypes.BLOCK, Blocks.REDSTONE_BLOCK.getDefaultState()), d0, d1, d2, rand.nextFloat() * bloodExplosionStrength * (rand.nextBoolean() ? -1 : 1), rand.nextFloat() * bloodExplosionStrength * (rand.nextBoolean() ? -1 : 1) / 2, rand.nextFloat() * bloodExplosionStrength * (rand.nextBoolean() ? -1 : 1));
                }
            }else if(message.Particle == 5) {
                for (int i = 0; i < message.amount; i++) {
                    double d0 = message.X + ((rand.nextInt((int) (message.dX * 10)) - (message.dX * 5))) / 10.0;
                    double d1 = message.Y + ((rand.nextInt((int) (message.dY * 10)) - (message.dY * 5))) / 10.0;
                    double d2 = message.Z + ((rand.nextInt((int) (message.dZ * 10)) - (message.dZ * 5))) / 10.0;
                    assert Minecraft.getInstance().world != null;
                    Minecraft.getInstance().world.addParticle(ParticleTypes.EXPLOSION_EMITTER, d0, d1, d2, 0.1, 0.1, 0.1);
                }
            }else if(message.Particle == 6) {
                for (int i = 0; i < message.amount; i++) {
                    double d0 = message.X + ((rand.nextInt((int) (message.dX * 10)) - (message.dX * 5))) / 10.0;
                    double d1 = message.Y + ((rand.nextInt((int) (message.dY * 10)) - (message.dY * 5))) / 10.0;
                    double d2 = message.Z + ((rand.nextInt((int) (message.dZ * 10)) - (message.dZ * 5))) / 10.0;
                    assert Minecraft.getInstance().world != null;
                    Minecraft.getInstance().world.addParticle(new RedstoneParticleData(1, 0, 0, 1), d0, d1, d2, 0, -0.5, 0);
                }
            }else if(message.Particle == 7) {
                for (int i = 0; i < message.amount; i++) {
                    double d0 = message.X + ((rand.nextInt((int) (message.dX * 10)) - (message.dX * 5))) / 10.0;
                    double d1 = message.Y + ((rand.nextInt((int) (message.dY * 10)) - (message.dY * 5))) / 10.0;
                    double d2 = message.Z + ((rand.nextInt((int) (message.dZ * 10)) - (message.dZ * 5))) / 10.0;
                    assert Minecraft.getInstance().world != null;
                    Minecraft.getInstance().world.addParticle(ParticleTypes.END_ROD, d0, d1, d2, 0, 0.075, 0);
                }
            }else if(message.Particle == 8) {
                for (int i = 0; i < message.amount; i++) {
                    double d0 = message.X + ((rand.nextInt((int) (message.dX * 10)) - (message.dX * 5))) / 10.0;
                    double d1 = message.Y + ((rand.nextInt((int) (message.dY * 10)) - (message.dY * 5))) / 10.0;
                    double d2 = message.Z + ((rand.nextInt((int) (message.dZ * 10)) - (message.dZ * 5))) / 10.0;
                    assert Minecraft.getInstance().world != null;
                    Minecraft.getInstance().world.addParticle(ParticleTypes.ENTITY_EFFECT, d0, d1, d2, 0, 0.075, 0);
                }
            }else if(message.Particle == 9) {
                for (int i = 0; i < message.amount; i++) {
                    double d0 = message.X + ((rand.nextInt((int) (message.dX * 10)) - (message.dX * 5))) / 10.0;
                    double d1 = message.Y + ((rand.nextInt((int) (message.dY * 10)) - (message.dY * 5))) / 10.0;
                    double d2 = message.Z + ((rand.nextInt((int) (message.dZ * 10)) - (message.dZ * 5))) / 10.0;
                    assert Minecraft.getInstance().world != null;
                    Minecraft.getInstance().world.addParticle(ParticleTypes.FLASH, d0, d1, d2, 0, 0, 0);
                }
            }else if(message.Particle == 10) {
                for (int i = 0; i < message.amount; i++) {
                    double d0 = message.X + ((rand.nextInt((int) (message.dX * 10)) - (message.dX * 5))) / 10.0;
                    double d1 = message.Y + ((rand.nextInt((int) (message.dY * 10)) - (message.dY * 5))) / 10.0;
                    double d2 = message.Z + ((rand.nextInt((int) (message.dZ * 10)) - (message.dZ * 5))) / 10.0;
                    assert Minecraft.getInstance().world != null;
                    Minecraft.getInstance().world.addParticle(new RedstoneParticleData(0.917f, 0.913f, 0.243f, 1), d0, d1, d2, 0, 0, 0);
                }
            }else if(message.Particle == 11) {
                for (int i = 0; i < message.amount; i++) {
                    double d0 = message.X + ((rand.nextInt((int) (message.dX * 10)) - (message.dX * 5))) / 10.0;
                    double d1 = message.Y + ((rand.nextInt((int) (message.dY * 10)) - (message.dY * 5))) / 10.0;
                    double d2 = message.Z + ((rand.nextInt((int) (message.dZ * 10)) - (message.dZ * 5))) / 10.0;
                    assert Minecraft.getInstance().world != null;
                    Minecraft.getInstance().world.addParticle(ParticleList.ZIPPER.get(), d0, d1, d2, 0, 0, 0);
                }
            }else if(message.Particle == 12) {
                for (int i = 0; i < message.amount; i++) {
                    double d0 = message.X + ((rand.nextInt((int) (message.dX * 10)) - (message.dX * 5))) / 10.0;
                    double d1 = message.Y + ((rand.nextInt((int) (message.dY * 10)) - (message.dY * 5))) / 10.0;
                    double d2 = message.Z + ((rand.nextInt((int) (message.dZ * 10)) - (message.dZ * 5))) / 10.0;
                    assert Minecraft.getInstance().world != null;
                    Minecraft.getInstance().world.addParticle(new RedstoneParticleData(0.647f, 0.094f, 0.749f, 1), d0, d1, d2, 0, 0, 0);
                }
            }else if(message.Particle == 13) {
                for (int i = 0; i < message.amount; i++) {
                    double d0 = message.X + ((rand.nextInt((int) (message.dX * 10)) - (message.dX * 5))) / 10.0;
                    double d1 = message.Y + ((rand.nextInt((int) (message.dY * 10)) - (message.dY * 5))) / 10.0;
                    double d2 = message.Z + ((rand.nextInt((int) (message.dZ * 10)) - (message.dZ * 5))) / 10.0;
                    assert Minecraft.getInstance().world != null;
                    Minecraft.getInstance().world.addParticle(ParticleTypes.LARGE_SMOKE, d0, d1, d2, 0, 0, 0);
                }
            }else if(message.Particle == 14) {
                for (int i = 0; i < message.amount; i++) {
                    double d0 = message.X + ((rand.nextInt((int) (message.dX * 10)) - (message.dX * 5))) / 10.0;
                    double d1 = message.Y + ((rand.nextInt((int) (message.dY * 10)) - (message.dY * 5))) / 10.0;
                    double d2 = message.Z + ((rand.nextInt((int) (message.dZ * 10)) - (message.dZ * 5))) / 10.0;
                    assert Minecraft.getInstance().world != null;
                    Minecraft.getInstance().world.addParticle(ParticleTypes.LAVA, d0, d1, d2, rand.nextDouble() - 0.5, rand.nextDouble() * 2, rand.nextDouble() - 0.5);
                }
            }else if(message.Particle == 15) {
                for (int i = 0; i < message.amount; i++) {
                    double d0 = message.X + ((rand.nextInt((int) (message.dX * 10)) - (message.dX * 5))) / 10.0;
                    double d1 = message.Y + ((rand.nextInt((int) (message.dY * 10)) - (message.dY * 5))) / 10.0;
                    double d2 = message.Z + ((rand.nextInt((int) (message.dZ * 10)) - (message.dZ * 5))) / 10.0;
                    assert Minecraft.getInstance().world != null;
                    Minecraft.getInstance().world.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0, 0, 0);
                }
            }else if(message.Particle == 16) {
                for (int i = 0; i < message.amount; i++) {
                    double d0 = message.X + ((rand.nextInt((int) (message.dX * 10)) - (message.dX * 5))) / 10.0;
                    double d1 = message.Y + ((rand.nextInt((int) (message.dY * 10)) - (message.dY * 5))) / 10.0;
                    double d2 = message.Z + ((rand.nextInt((int) (message.dZ * 10)) - (message.dZ * 5))) / 10.0;
                    assert Minecraft.getInstance().world != null;
                    Minecraft.getInstance().world.addParticle(ParticleTypes.FLAME, d0, d1, d2, 0, 0, 0);
                }
            }
        }
        ctx.get().setPacketHandled(true);
    }
}
