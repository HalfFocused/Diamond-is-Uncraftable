package io.github.halffocused.diamond_is_uncraftable.entity.stand;

import io.github.halffocused.diamond_is_uncraftable.capability.Stand;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.attack.GoldExperienceRequiemPunchEntity;
import io.github.halffocused.diamond_is_uncraftable.util.movesets.HoveringMoveHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

@SuppressWarnings("ConstantConditions")
public class GoldExperienceRequiemEntity extends AbstractStandEntity {
    private StringTextComponent truthname = new StringTextComponent("You will never reach the truth.");

    public GoldExperienceRequiemEntity(EntityType<? extends AbstractStandEntity> type, World world) {
        super(type, world);
    }

    public void toggleFlight() {
        if (getMaster() != null)
            getMaster().setNoGravity(!getMaster().hasNoGravity());
    }

    public HoveringMoveHandler getController(){
        return null;
    }

    @Override
    public void tick() {
        super.tick();
        if (getMaster() != null) {
            Stand.getLazyOptional(master).ifPresent(props -> {
                ability = props.getAbility();

                if (props.getTransformed() > 1)
                    props.setCooldown(props.getCooldown() - 1);
                if (props.getCooldown() <= 0) {
                    props.setTransformed(0);
                    props.setCooldown(60);
                }
                master.getFoodStats().addStats(20, 20);

                if (ability) {
                    if (master.getLastAttackedEntity() != null) {
                        if (master.getLastAttackedEntity() instanceof PlayerEntity)
                            props.setDiavolo(master.getLastAttackedEntity().getDisplayName().toString());
                    }
                    for (PlayerEntity playerEntity : world.getPlayers()) {
                        if (playerEntity != getMaster())
                            if (playerEntity.getLastAttackedEntity() == getMaster())
                                props.setDiavolo(playerEntity.getDisplayName().toString());
                    }

                    if (props.getDiavolo() != null && !props.getDiavolo().equals("")) {
                        for (PlayerEntity playerEntity : world.getPlayers()) {
                            if (playerEntity != getMaster()) {
                                if (playerEntity.getDisplayName().toString().equals(props.getDiavolo())) {
                                    if (playerEntity.isAlive()) {
                                        if (!playerEntity.world.isRemote)
                                            playerEntity.world.getServer().getWorld(playerEntity.dimension).getEntities()
                                                    .filter(entity -> entity instanceof MobEntity)
                                                    .forEach(entity -> ((MobEntity) entity).setAttackTarget(playerEntity));
                                        CreeperEntity truth = new CreeperEntity(EntityType.CREEPER, playerEntity.world);
                                        truth.setCustomName(truthname);
                                        truth.setPosition(playerEntity.getPosX(), playerEntity.getPosY(), playerEntity.getPosZ());
                                        truth.setAttackTarget(playerEntity);
                                        truth.setDropChance(EquipmentSlotType.MAINHAND, 0.0f);
                                        playerEntity.world.addEntity(truth);
                                        truth.setAttackTarget(playerEntity);
                                    } else {
                                        if (!world.isRemote) {
                                            world.getServer().getWorld(dimension).getEntities()
                                                    .filter(entity -> entity instanceof CreeperEntity)
                                                    .filter(entity -> entity.getCustomName().equals(truthname))
                                                    .forEach(Entity::remove);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            });

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
                        GoldExperienceRequiemPunchEntity goldExperienceRequiem1 = new GoldExperienceRequiemPunchEntity(world, this, master);
                        goldExperienceRequiem1.randomizePositions();
                        goldExperienceRequiem1.shoot(master, master.rotationPitch, master.rotationYaw, 1001, Float.MIN_VALUE);
                        world.addEntity(goldExperienceRequiem1);
                        GoldExperienceRequiemPunchEntity goldExperienceRequiem2 = new GoldExperienceRequiemPunchEntity(world, this, master);
                        goldExperienceRequiem2.randomizePositions();
                        goldExperienceRequiem2.shoot(master, master.rotationPitch, master.rotationYaw, 1001, Float.MIN_VALUE);
                        world.addEntity(goldExperienceRequiem2);
                    }
                if (attackTicker >= 110) {
                    attackRush = false;
                    attackTicker = 0;
                }
            }
        }
    }
}
