package io.github.halffocused.diamond_is_uncraftable.entity.stand;

import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import io.github.halffocused.diamond_is_uncraftable.capability.Stand;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.attack.AerosmithBulletEntity;
import io.github.halffocused.diamond_is_uncraftable.network.message.server.SAerosmithPacket;
import io.github.halffocused.diamond_is_uncraftable.util.movesets.HoveringMoveHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;

@SuppressWarnings("ConstantConditions")
public class AerosmithEntity extends AbstractStandEntity {
    public AerosmithEntity(EntityType<? extends MobEntity> type, World worldIn) {
        super(type, worldIn);
    }

    @Override
    public void playSpawnSound() {
        world.playSound(null, getMaster().getPosition(), getSpawnSound(), SoundCategory.NEUTRAL, 3, 1);
    }

    public HoveringMoveHandler getController(){
        return null;
    }

    public void shootBomb() {
        if (getMaster() == null) return;
        Stand.getLazyOptional(getMaster()).ifPresent(props -> {
            if (props.getCooldown() <= 0) {
                TNTEntity tnt = new TNTEntity(world, getPosX(), getPosY(), getPosZ(), getMaster());
                tnt.setFuse(20);
                tnt.setMotion(getLookVec().getX(), getLookVec().getY(), getLookVec().getZ());
                if (!world.isRemote)
                    world.addEntity(tnt);
                props.setCooldown(200);
            }
        });
    }

    @Override
    public void tick() {
        super.tick();
        setMotion(getMotion().getX(), 0, getMotion().getZ());
        if (master != null) {
            Stand.getLazyOptional(master).ifPresent(props -> {
                if (ability != props.getAbility()) {
                    if (ability && !world.isRemote)
                        DiamondIsUncraftable.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> this), new SAerosmithPacket(getEntityId(), (byte) 0));
                    ability = props.getAbility();
                }
                if (props.getCooldown() > 0)
                    props.setCooldown(props.getCooldown() - 1);
            });

            rotationYaw = master.rotationYaw;
            rotationPitch = master.rotationPitch * 0.5f;
            setRotation(rotationYaw, rotationPitch);
            prevRotationYaw = rotationYaw;
            renderYawOffset = rotationYaw;
            rotationYawHead = renderYawOffset;

            if (master.swingProgressInt == 0 && !ability && !attackRush)
                attackTick = 0;
            if (attackRush) {
                if (!ability)
                    master.setSprinting(false);
                attackTicker++;
                if (attackTicker >= 10)
                    if (!world.isRemote) {
                        master.setSprinting(false);
                        AerosmithBulletEntity aerosmithBullet1 = new AerosmithBulletEntity(world, this, master);
                        aerosmithBullet1.randomizePositions();
                        aerosmithBullet1.shoot(master, rotationPitch, rotationYaw, 4, 0.3f);
                        world.addEntity(aerosmithBullet1);
                        AerosmithBulletEntity aerosmithBullet2 = new AerosmithBulletEntity(world, this, master);
                        aerosmithBullet2.randomizePositions();
                        aerosmithBullet2.shoot(master, rotationPitch, rotationYaw, 4, 0.3f);
                        world.addEntity(aerosmithBullet2);
                    }
                if (attackTicker >= 110) {
                    attackRush = false;
                    attackTicker = 0;
                    attackTick = 0;
                }
            }
        }
    }

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        if (!world.isRemote)
            DiamondIsUncraftable.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> this), new SAerosmithPacket(getEntityId(), (byte) 0));
    }

    @Override
    public float getYaw(float partialTicks) {
        float modifiedYaw = super.getYaw(partialTicks) >= 180 ? super.getYaw(partialTicks) - 360 : super.getYaw(partialTicks) < 180 ? super.getYaw(partialTicks) + 360 : super.getYaw(partialTicks);

        return modifiedYaw;
    }
}