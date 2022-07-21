package io.github.halffocused.diamond_is_uncraftable.entity.stand;

import io.github.halffocused.diamond_is_uncraftable.capability.Stand;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.attack.WeatherReportPunchEntity;
import io.github.halffocused.diamond_is_uncraftable.init.EffectInit;
import io.github.halffocused.diamond_is_uncraftable.util.movesets.HoveringMoveHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.World;

@SuppressWarnings("ConstantConditions")
public class WeatherReportEntity extends AbstractStandEntity {
    private int weatherTick;

    public WeatherReportEntity(EntityType<? extends MobEntity> type, World worldIn) {
        super(type, worldIn);
    }

    public HoveringMoveHandler getController(){
        return null;
    }

    public void changeWeather() {

        if(!world.isRemote) {

            if (weatherTick == 2)
                weatherTick = 0;
            else
                weatherTick++;

            switch (weatherTick) {
                case 0: {
                    world.getWorldInfo().setRaining(false);
                    world.getWorldInfo().setThundering(false);
                    break;
                }
                case 1: {
                    world.getWorldInfo().setRaining(true);
                    world.getWorldInfo().setThundering(false);
                    break;
                }
                case 2: {
                    world.getWorldInfo().setRaining(true);
                    world.getWorldInfo().setThundering(true);
                    break;
                }
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (world.rand.nextInt(35) == 1)
            spawnExplosionParticle();
        if (getMaster() != null) {
            followMaster();
            setRotationYawHead(master.rotationYawHead);
            setRotation(master.rotationYaw, master.rotationPitch);
            Stand.getLazyOptional(master).ifPresent(props -> ability = props.getAbility());

            if (ability) {
                if (!world.isRemote)
                    world.getServer().getWorld(dimension).getEntities().filter(entity -> entity != master)
                            .filter(entity -> entity instanceof LivingEntity)
                            .filter(entity -> !(entity instanceof AbstractStandEntity))
                            .filter(entity -> !entity.areEyesInFluid(FluidTags.WATER))
                            .forEach(entity -> {
                                if (entity.getDistance(master) <= 10) {
                                    ((LivingEntity) entity).addPotionEffect(new EffectInstance(EffectInit.OXYGEN_POISONING.get(), 150, 5));
                                    if (!master.isCreative()) {
                                        master.getFoodStats().addStats(0, -0.1f);
                                        master.getFoodStats().addExhaustion(0.05f);
                                    }
                                }
                            });
            }
            if (master.swingProgressInt == 0 && !attackRush)
                attackTick = 0;
            if (attackRush) {
                master.setSprinting(false);
                attackTicker++;
                if (attackTicker >= 10)
                    if (!world.isRemote) {
                        master.setSprinting(false);
                        WeatherReportPunchEntity weatherReportPunch1 = new WeatherReportPunchEntity(world, this, master);
                        weatherReportPunch1.randomizePositions();
                        weatherReportPunch1.shoot(master, rotationPitch, rotationYaw, 1.8f, 0.2f);
                        world.addEntity(weatherReportPunch1);
                        WeatherReportPunchEntity weatherReportPunch2 = new WeatherReportPunchEntity(world, this, master);
                        weatherReportPunch2.randomizePositions();
                        weatherReportPunch2.shoot(master, rotationPitch, rotationYaw, 1.8f, 0.2f);
                        world.addEntity(weatherReportPunch2);
                    }
                if (attackTicker >= 110) {
                    attackRush = false;
                    attackTicker = 0;
                }
            }
        }
    }
}
