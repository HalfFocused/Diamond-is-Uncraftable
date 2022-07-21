package io.github.halffocused.diamond_is_uncraftable.util;

import net.minecraft.util.SoundEvent;

/**
 * Although unnecessary this class fulfills my need to make everything that can be a custom data type a custom data type.
 * It also fulfills my need to never properly use javadocs.
 */
public class MoveEffects {
    private int particleId;
    private SoundEvent moveStartSound;
    private SoundEvent moveHitSound;

    public MoveEffects(int particleIdIn, SoundEvent moveStartSoundIn, SoundEvent moveHitSoundIn){
        particleId = particleIdIn;
        moveStartSound = moveStartSoundIn;
        moveHitSound = moveHitSoundIn;
    }

    public int getParticleId(){
        return particleId;
    }

    public SoundEvent getMoveStartSound(){
        return moveStartSound;
    }

    public SoundEvent getMoveHitSound(){
        return moveHitSound;
    }
}
