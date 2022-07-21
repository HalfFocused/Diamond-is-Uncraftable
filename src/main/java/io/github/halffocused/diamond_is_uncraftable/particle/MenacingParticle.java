package io.github.halffocused.diamond_is_uncraftable.particle;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MenacingParticle extends SpriteTexturedParticle {


    protected MenacingParticle(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);


        this.setSize(0.04f, 0.04f);
        this.particleScale *= this.rand.nextFloat() * 2.2F;
        //this.motionX *= (double)0.02f;
        //this.motionY *= (double)0.02f;
        //this.motionZ *= (double)0.02f;
        this.motionX = 0;
        this.motionY = 0.04d;
        this.motionZ = 0;
        this.maxAge = (int)((30.0D * Math.random()) + 10);

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
        } else {
            this.move(this.motionX, this.motionY, this.motionZ);
            if(!(this.rand.nextInt(2)==1)) {
                this.motionX *= 1.0D;
                this.motionY *= 1.0D;
                this.motionZ *= 1.0D;
            }else{//random vibrations of the menacing particles
                this.motionY *= 1.0D;
                if(this.rand.nextBoolean()) {
                    if (this.rand.nextBoolean()) {
                        this.posX += this.rand.nextFloat() / 10;
                    } else {
                        this.posX += this.rand.nextFloat() / -10;
                    }
                }
                if(this.rand.nextBoolean()) {
                    if (this.rand.nextBoolean()) {
                        this.posZ += this.rand.nextFloat() / 10;
                    } else {
                        this.posZ += this.rand.nextFloat() / -10;
                    }
                }
            }
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
            MenacingParticle menacingParticle = new MenacingParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed);
            menacingParticle.setColor(1.0f, 1.0f, 1.0f);
            menacingParticle.selectSpriteRandomly(this.spriteSet);
            return menacingParticle;
        }

    }


}