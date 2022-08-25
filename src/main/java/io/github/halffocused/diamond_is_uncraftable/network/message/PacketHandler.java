package io.github.halffocused.diamond_is_uncraftable.network.message;

import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import io.github.halffocused.diamond_is_uncraftable.network.message.client.*;
import io.github.halffocused.diamond_is_uncraftable.network.message.server.*;
import net.minecraftforge.fml.network.NetworkDirection;

import java.util.Optional;

public class PacketHandler {
    static int networkId = 0;

    public static void register() {
        registerPacket(CStandSummonPacket.class, new CStandSummonPacket(), NetworkDirection.PLAY_TO_SERVER);
        registerPacket(CToggleAbilityPacket.class, new CToggleAbilityPacket(), NetworkDirection.PLAY_TO_SERVER);
        registerPacket(CAerosmithMovePacket.class, new CAerosmithMovePacket(), NetworkDirection.PLAY_TO_SERVER);
        registerPacket(CSyncStandAbilitiesPacket.class, new CSyncStandAbilitiesPacket(), NetworkDirection.PLAY_TO_SERVER);
        registerPacket(SSyncStandCapabilityPacket.class, new SSyncStandCapabilityPacket(), NetworkDirection.PLAY_TO_CLIENT);
        registerPacket(SSyncTimestopCapabilityPacket.class, new SSyncTimestopCapabilityPacket(), NetworkDirection.PLAY_TO_CLIENT);
        registerPacket(SSyncStandPerWorldCapabilityPacket.class, new SSyncStandPerWorldCapabilityPacket(), NetworkDirection.PLAY_TO_CLIENT);
        registerPacket(SSyncCombatCapabilityCombat.class, new SSyncCombatCapabilityCombat(), NetworkDirection.PLAY_TO_CLIENT);
        registerPacket(SSyncStandMasterPacket.class, new SSyncStandMasterPacket(), NetworkDirection.PLAY_TO_CLIENT);
        registerPacket(SSyncSilverChariotArmorPacket.class, new SSyncSilverChariotArmorPacket(), NetworkDirection.PLAY_TO_CLIENT);
        registerPacket(CHierophantGreenPossessionPacket.class, new CHierophantGreenPossessionPacket(), NetworkDirection.PLAY_TO_SERVER);
        registerPacket(SSyncStandEffectsCapabilityPacket.class, new SSyncStandEffectsCapabilityPacket(), NetworkDirection.PLAY_TO_CLIENT);
        registerPacket(CChargeAttackPacket.class, new CChargeAttackPacket(), NetworkDirection.PLAY_TO_SERVER);
        registerPacket(CSwitchStandActPacket.class, new CSwitchStandActPacket(), NetworkDirection.PLAY_TO_SERVER);
        registerPacket(SSyncStickyFingersDisguisePacket.class, new SSyncStickyFingersDisguisePacket(), NetworkDirection.PLAY_TO_CLIENT);
        registerPacket(SAerosmithPacket.class, new SAerosmithPacket(), NetworkDirection.PLAY_TO_CLIENT);
        registerPacket(SSyncStandChunkEffectCapabilityPacket.class, new SSyncStandChunkEffectCapabilityPacket(), NetworkDirection.PLAY_TO_CLIENT);
        registerPacket(SSyncHierophantGreenPacket.class, new SSyncHierophantGreenPacket(), NetworkDirection.PLAY_TO_CLIENT);
        registerPacket(CAerosmithRotationPacket.class, new CAerosmithRotationPacket(), NetworkDirection.PLAY_TO_SERVER);

        registerPacket(SRemoveEntityPacket.class, new SRemoveEntityPacket(), NetworkDirection.PLAY_TO_CLIENT);

        registerPacket(SAnimatePacket.class, new SAnimatePacket(), NetworkDirection.PLAY_TO_CLIENT);
        registerPacket(SParticlePacket.class, new SParticlePacket(), NetworkDirection.PLAY_TO_CLIENT);
        registerPacket(CStandMasterMovementPacket.class, new CStandMasterMovementPacket(), NetworkDirection.PLAY_TO_SERVER);
        registerPacket(CWalkingStandTargetPacket.class, new CWalkingStandTargetPacket(), NetworkDirection.PLAY_TO_SERVER);

        registerPacket(CTimeSkipEffectPacket.class, new CTimeSkipEffectPacket(), NetworkDirection.PLAY_TO_SERVER);

        registerPacket(CAddStandToWorldData.class, new CAddStandToWorldData(), NetworkDirection.PLAY_TO_SERVER);

        registerPacket(SSyncWorldTimestopCapability.class, new SSyncWorldTimestopCapability(), NetworkDirection.PLAY_TO_CLIENT);

    }

    public static <MSG> void registerPacket(Class<MSG> clazz, IMessage<MSG> message) {
        DiamondIsUncraftable.INSTANCE.registerMessage(networkId++, clazz, message::encode, message::decode, message::handle);
    }

    public static <MSG> void registerPacket(Class<MSG> clazz, IMessage<MSG> message, NetworkDirection direction) { //Includes a NetworkDirection parameter.
        DiamondIsUncraftable.INSTANCE.registerMessage(networkId++, clazz, message::encode, message::decode, message::handle, Optional.of(direction));
    }
}
