package io.github.halffocused.diamond_is_uncraftable.particle;

import net.minecraft.client.particle.*;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ZipperParticle extends SpriteTexturedParticle {


    protected ZipperParticle(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);


        this.setSize(0.08f, 0.08f);
        //this.particleScale *= this.rand.nextFloat() * 2.2F;
        //this.motionX *= (double)0.02f;
        //this.motionY *= (double)0.02f;
        //this.motionZ *= (double)0.02f;
        this.particleScale *= 1;
        this.motionX = 0;
        this.motionY = 0;
        this.motionZ = 0;
        this.maxAge = 10;

    }

    @Override
    public IParticleRenderType getRenderType() {
        return IParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void tick() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if(this.maxAge-- <= 0) {
            this.setExpired();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements IParticleFactory<BasicParticleType> {
        private final IAnimatedSprite spriteSet;

        public Factory(IAnimatedSprite sprite) {
            this.spriteSet = sprite;
        }


        @Override
        public Particle makeParticle(BasicParticleType typeIn, World worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            ZipperParticle menacingParticle = new ZipperParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed);
            menacingParticle.setColor(1.0f, 1.0f, 1.0f);
            menacingParticle.selectSpriteRandomly(this.spriteSet);
            return menacingParticle;
        }

    }


}