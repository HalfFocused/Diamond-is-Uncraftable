package io.github.halffocused.diamond_is_uncraftable.entity.stand;

import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import io.github.halffocused.diamond_is_uncraftable.capability.Stand;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.attack.EmeraldSplashEntity;
import io.github.halffocused.diamond_is_uncraftable.network.message.server.SSyncHierophantGreenPacket;
import io.github.halffocused.diamond_is_uncraftable.util.movesets.HoveringMoveHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;

@SuppressWarnings("ConstantConditions")
public class HierophantGreenEntity extends AbstractStandEntity {
    public LivingEntity possessedEntity;
    public float yaw, pitch;

    public HierophantGreenEntity(EntityType<? extends AbstractStandEntity> type, World world) {
        super(type, world);
    }

    public HoveringMoveHandler getController(){
        return null;
    }

    public void setPossessedEntity(int entityID) {
        if (getMaster() == null) return;
        Entity entity = world.getEntityByID(entityID);
        if (entity instanceof LivingEntity) {
            possessedEntity = (LivingEntity) entity;
            master.setInvulnerable(true);
        } else if (entity == null)
            possessedEntity = null;
        if (!world.isRemote)
            DiamondIsUncraftable.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) master), new SSyncHierophantGreenPacket(getEntityId(), entityID));
    }

    @Override
    public void tick() {
        super.tick();
        if (master != null) {
            Stand.getLazyOptional(master).ifPresent(props -> props.setAbilityActive(possessedEntity != null));

            if (possessedEntity != null) {
                possessedEntity.rotationYaw = master.rotationYaw;
                possessedEntity.rotationPitch = master.rotationPitch * 0.5f;
                possessedEntity.setRotation(rotationYaw, rotationPitch);
                possessedEntity.prevRotationYaw = rotationYaw;
                possessedEntity.renderYawOffset = rotationYaw;
                possessedEntity.rotationYawHead = renderYawOffset;
                if (possessedEntity instanceof MobEntity) {
                    ((MobEntity) possessedEntity).goalSelector.disableFlag(Goal.Flag.LOOK);
                    ((MobEntity) possessedEntity).goalSelector.disableFlag(Goal.Flag.MOVE);
                }
            }

            followMaster();
            setRotationYawHead(master.rotationYawHead);
            setRotation(master.rotationYaw, master.rotationPitch);

            if (master.swingProgressInt == 0 && !attackRush)
                attackTick = 0;
            if (attackRush) {
                master.setSprinting(false);
                attackTicker++;
                if (attackTicker >= 10)
                    if (!world.isRemote) {
                        master.setSprinting(false);
                        EmeraldSplashEntity emeraldSplashEntity = new EmeraldSplashEntity(world, this, master);
                        emeraldSplashEntity.randomizePositions();
                        emeraldSplashEntity.shoot(master, master.rotationPitch, master.rotationYaw, 2, 0.25f);
                        world.addEntity(emeraldSplashEntity);
                        EmeraldSplashEntity emeraldSplashEntity1 = new EmeraldSplashEntity(world, this, master);
                        emeraldSplashEntity1.randomizePositions();
                        emeraldSplashEntity1.shoot(master, master.rotationPitch, master.rotationYaw, 2, 0.25f);
                        world.addEntity(emeraldSplashEntity1);
                    }
                if (attackTicker >= 80) {
                    attackRush = false;
                    attackTicker = 0;
                }
            }
        }
    }

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        if (possessedEntity instanceof MobEntity) {
            ((MobEntity) possessedEntity).goalSelector.enableFlag(Goal.Flag.LOOK);
            ((MobEntity) possessedEntity).goalSelector.enableFlag(Goal.Flag.MOVE);
        }
    }
}
