package io.github.halffocused.diamond_is_uncraftable.entity.stand;

import com.google.common.util.concurrent.AtomicDouble;
import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import io.github.halffocused.diamond_is_uncraftable.capability.Stand;
import io.github.halffocused.diamond_is_uncraftable.capability.StandEffects;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.attack.AbstractStandAttackEntity;
import io.github.halffocused.diamond_is_uncraftable.event.custom.StandEvent;
import io.github.halffocused.diamond_is_uncraftable.init.EntityInit;
import io.github.halffocused.diamond_is_uncraftable.init.SoundInit;
import io.github.halffocused.diamond_is_uncraftable.network.message.server.SSyncStandMasterPacket;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import io.github.halffocused.diamond_is_uncraftable.util.movesets.HoveringMoveHandler;
import io.github.halffocused.diamond_is_uncraftable.util.movesets.MovementAnimationHolder;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.server.management.PreYggdrasilConverter;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.PacketDistributor;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings({"unused", "ConstantConditions"})
public abstract class AbstractStandEntity extends MobEntity implements IEntityAdditionalSpawnData, IAnimatable {
    private static final DataParameter<Optional<UUID>> MASTER_UNIQUE_ID = EntityDataManager.createKey(AbstractStandEntity.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    public boolean ability, attackRush;
    public int attackTick, attackTicker;
    protected PlayerEntity master;

    EntityType<AbstractStandEntity> entityType;

    boolean attacking = false;
    boolean hasAttackTarget = false;
    int ticksSinceLastPunch;
    public int masterKeybindInput;

    private LivingEntity mostRecentlyDamagedEntity = null;

    public HoveringMoveHandler controller;

    private final AnimationFactory factory = new AnimationFactory(this);


    public String currentAnimation;
    public boolean animationLooping;

    boolean lastIsCharging = false;
    boolean lastIsCrouching = false;

    public AbstractStandEntity(EntityType<? extends MobEntity> type, World worldIn) {
        super(type, worldIn);
    }

    public static AttributeModifierMap.MutableAttribute setCustomAttributes() {
        return MobEntity.func_233666_p_().createMutableAttribute(Attributes.MAX_HEALTH, 20.0D);
    }

    /**
     * @return Returns the Stand's spawn sound.
     */
    public SoundEvent getSpawnSound(){
        return SoundInit.SUMMON_STAND.get();
    }

    /**
     * @return The Stand's current {@link AbstractStandEntity#master}, also makes sure it isn't <code>null</code>.
     */
    public PlayerEntity getMaster() { //Don't listen to your IDE, this can be null during a relog.
        return master == null ? master = world.getPlayerByUuid(getMasterUUID()) : master;
    }

    /**
     * Sets the Stand's master, should never be <code>null</code> as it would most likely <b>crash</b> the game.
     *
     * @param master The {@link PlayerEntity} that will be set as the Stand's {@link AbstractStandEntity#master}.
     */
    public void setMaster(@Nonnull PlayerEntity master) {
        this.master = master;
    }

    /**
     * @return The {@link UUID} of the Stand's master from it's {@link EntityDataManager}.
     */
    public UUID getMasterUUID() {
        return dataManager.get(MASTER_UNIQUE_ID).orElse(null);
    }

    /**
     * Sets the Stand's master's {@link UUID} to the {@link EntityDataManager}.
     *
     * @param masterUUID The {@link UUID} that will be set as the Stand's {@link AbstractStandEntity#MASTER_UNIQUE_ID}
     */
    public void setMasterUUID(@Nullable UUID masterUUID) {
        dataManager.set(MASTER_UNIQUE_ID, Optional.ofNullable(masterUUID));
    }

    /**
     * Plays the Stand's {@link AbstractStandEntity#getSpawnSound()}.
     */
    public void playSpawnSound() {
        world.playSound(null, master.getPosition(), getSpawnSound(), SoundCategory.NEUTRAL, 1, 1);
    }

    /**
     * Makes the Stand follow it's {@link AbstractStandEntity#master}
     */
    public void followMaster() {
        if (this.master == null) return;

            double standSpacing = -1.5;
            Vector3d position = Util.rotationVectorIgnoreY(master).mul(standSpacing, standSpacing, -1.75).add(master.getPositionVec());
            position = position.mul(1, 0, 1);
            setPosition(position.x, master.getPosY() + 0.45, position.z);

    }

    /**
     * Makes the Stand dodge oncoming attacks, such as TNT, arrows and falling blocks.
     */
    private void dodgeAttacks() {
        if (world.isRemote) return;
        world.getServer().getWorld(world.getDimensionKey()).getEntities()
                .filter(entity -> entity instanceof TNTEntity || entity instanceof ArrowEntity || entity instanceof FallingBlockEntity || entity instanceof ProjectileItemEntity)
                .filter(entity -> entity.getDistance(master) <= Math.PI * 8)
                .forEach(entity -> {
                    double distanceX = getPosX() - entity.getPosX();
                    double distanceY = getPosY() - entity.getPosY();
                    double distanceZ = getPosZ() - entity.getPosZ();
                    if (distanceX > 0)
                        moveForward -= 0.3;
                    else if (distanceX < 0)
                        moveForward += 0.3;
                    if (distanceY > 0)
                        moveVertical -= 0.3;
                    else if (distanceY < 0)
                        moveVertical += 0.3;
                    if (distanceZ > 0)
                        moveStrafing -= 0.3;
                    else if (distanceZ < 0)
                        moveStrafing += 0.3;
                });
    }

    /**
     * Called every tick to update the entity's position/logic and remove it if conditions are met.
     */
    @Override
    public void tick() {
        super.tick(); //Queues the tick method to run, code in tick() method won't run if removed.
        fallDistance = 0; //Mutes that god forsaken fall sound, not even overriding the playFallSound method helps without this.
        if (!world.isRemote && getMaster() != null) { //Calls getMaster to set the master to a @Nonnull value.

            stopRiding();

            if (!master.isAlive()) {
                MinecraftForge.EVENT_BUS.post(new StandEvent.MasterDeathEvent(master, this));
                remove();
            }
            if (master.isSpectator()) remove();
            MinecraftForge.EVENT_BUS.post(new StandEvent.StandTickEvent(master, this)); //Fired after all death checks are passed to avoid confusion.

            Stand.getLazyOptional(master).ifPresent(props -> {
                if (!props.getStandOn()) {
                    MinecraftForge.EVENT_BUS.post(new StandEvent.StandUnsummonedEvent(master, this));
                    remove();
                }
            });
            StandEffects.getLazyOptional(this).ifPresent(props -> {
                if (props.isThreeFreeze()) {
                    master.setMotion(0, getMotion().getY(), 0);
                    master.velocityChanged = true;
                }
            });
            if(getController() != null) { //Literally never happens other than in development when making abilities before attacks.
                getController().tick(lastIsCharging, lastIsCrouching);
            }
        }
    }

    /**
     * Redirects attacks from the Stand to it's master.
     *
     * @param damageSource The {@link DamageSource} damaging the Stand.
     * @param damage       The amount of damage taken.
     * @return Always returns <code>false</code> to prevent the Stand from taking damage, and because I'm paranoid.
     */
    @Override
    public boolean attackEntityFrom(DamageSource damageSource, float damage) {
        if (master == null || damageSource.getTrueSource() == master || damageSource == DamageSource.CACTUS || damageSource == DamageSource.FALL || Util.isTimeStoppedForEntity(master))
            return false; //Prevents Stands from taking damage they shouldn't, fall damage, cactus damage, etc.

        Stand.getLazyOptional(master).ifPresent(stand -> {
            if(!stand.getExperiencingTimeStop() && !stand.getExperiencingTimeSkip()) {
                master.attackEntityFrom(damageSource, damage * getDamageSharingPercentage());
            }
        });

        return false;
    }

    /**
     * Makes Stand's phase through non Stand entities.
     *
     * @param entityIn The {@link Entity} being collided with.
     */
    @Override
    public void applyEntityCollision(Entity entityIn) {
        if (entityIn instanceof AbstractStandEntity || entityIn instanceof AbstractStandAttackEntity)
            super.applyEntityCollision(entityIn);
    }

    /**
     * Posts the {@link StandEvent.StandSummonedEvent} and sends a {@link SSyncStandMasterPacket} because I'm paranoid.
     */
    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        if (MinecraftForge.EVENT_BUS.post(new StandEvent.StandSummonedEvent(master, this)))
            remove(); //Removes the Stand if the Stand summon event is cancelled.
        if (!world.isRemote && master != null) {
            DiamondIsUncraftable.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> this), new SSyncStandMasterPacket(getEntityId(), master.getEntityId()));
            Stand.getLazyOptional(master).ifPresent(props -> props.setPlayerStand(getEntityId())); //Sets the Stand's Entity#getEntityID to the player's capability.
        }
    }

    /**
     * Fires the {@link StandEvent.StandRemovedEvent}, will cause major issues if super isn't called.
     */
    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        MinecraftForge.EVENT_BUS.post(new StandEvent.StandRemovedEvent(master, this));
        if (master != null)
            Stand.getLazyOptional(master).ifPresent(props -> props.setPlayerStand(0)); //Resets the Stand#getPlayerStand for easier null checks.
        master.sendStatusMessage(new StringTextComponent(""), true); //Clear the action bar.
    }

    /**
     * Prevents the Stand from despawning due to being far from players.
     *
     * @param distanceToClosestPlayer The Stand's distance to the nearest player.
     * @return Whether or not the Stand should despawn.
     */
    @Override
    public boolean canDespawn(double distanceToClosestPlayer) {
        return false;
    }

    /**
     * Makes the Stand not render flames when it's on fire as it looks stupid.
     *
     * @return Whether the entity should render as on fire.
     */
    @Override
    public boolean canRenderOnFire() {
        return false;
    }

    @Override
    public boolean isAIDisabled() {
        return false;
    }

    @Override
    public boolean isEntityInsideOpaqueBlock() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    protected AxisAlignedBB getBoundingBox(Pose pose) {
        return new AxisAlignedBB(0, 0, 0, 0, 0, 0);
    }

    @Override
    protected void collideWithEntity(Entity entity) {
        if (entity instanceof AbstractStandEntity || entity instanceof AbstractStandAttackEntity)
            applyEntityCollision(entity);
    }

    /**
     * Mutes the annoying fall sound.
     */
    @Override
    protected void playFallSound() {
    }

    /**
     * Mute the splash sounds created by the rapid teleportation of the stands  :)
     */

    @Override
    public boolean isInWater() {
        return false;
    }
    @Override
    protected void doWaterSplashEffect() {
    }
    @Override
    protected void playSwimSound(float volume) {
    }


    /**
     * Writes data from the server to a {@link PacketBuffer}.
     *
     * @param buffer The {@link PacketBuffer} to write to.
     */
    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeInt(getMaster() == null ? -1 : master.getEntityId());
    }

    /**
     * Reads the data written to the {@link PacketBuffer} by the server from the client, syncing that data to the client.
     *
     * @param additionalData The {@link PacketBuffer} to read from.
     */
    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        Entity entity = world.getEntityByID(additionalData.readInt());
        if (entity instanceof PlayerEntity)
            setMaster((PlayerEntity) entity);
    }

    /**
     * Registers the Stand's master's UUID to the {@link EntityDataManager}.
     */
    @Override
    protected void registerData() {
        super.registerData();
        dataManager.register(MASTER_UNIQUE_ID, Optional.empty());
    }

    /**
     * Writes the Stand's master's UUID to the {@link CompoundNBT} for future use.
     */
    @Override
    public void writeAdditional(CompoundNBT compoundNBT) {
        super.writeAdditional(compoundNBT);
        if (getMasterUUID() != null)
            compoundNBT.putUniqueId("MasterUUID", getMasterUUID());
    }

    /**
     * Sets the Stand's master to the one written to the {@link CompoundNBT}.
     */
    @Override
    public void readAdditional(CompoundNBT compoundNBT) {
        super.readAdditional(compoundNBT);
        UUID id;
        if (compoundNBT.contains("MasterUUID"))
            id = compoundNBT.getUniqueId("MasterUUID");
        else
            id = PreYggdrasilConverter.convertMobOwnerIfNeeded(getServer(), compoundNBT.getUniqueId("MasterUUID").toString());

        if (id != null)
            setMasterUUID(id);
    }

    /**
     * Very important for custom entities, if not implemented the game can crash with a {@link NullPointerException} and/or the entity won't render.
     */
    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public void setMasterKeybindInput(int masterKeybindInputIn) {
        this.masterKeybindInput = masterKeybindInputIn;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "controller", 2, this::predicate));
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event)
    {
        if(world.isRemote) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation(currentAnimation, animationLooping));
        }
        return PlayState.CONTINUE;
    }
    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    public void setAnimation(String animationNameIn, boolean animationLoopingIn){
        this.currentAnimation = animationNameIn;
        this.animationLooping = animationLoopingIn;
    }

    /**
     * @return The move that should be used when the stand's owner left-clicks without sneaking.
     */
    public int getJabMoveId(){
        return 1;
    }
    /**
     * @return The move that should be used when the stand's owner left-clicks while sneaking.
     */
    public int getBarrageMoveId(){
        return 2;
    }


    public void goToAttackPosition(double distanceIn){

        Vector3d position = master.getLookVec().mul(distanceIn, distanceIn, distanceIn).add(master.getPositionVec());
        for (double i = position.getY() - 0.5; world.getBlockState(new BlockPos(position.getX(), i, position.getZ())).isSolid(); i++)
            position = position.add(0, 0.5, 0);
        setPositionAndUpdate(position.getX(), position.getY() + 0.75, position.getZ());

        Stand.getLazyOptional(master).ifPresent(props -> {
            props.setPreventUnsummon(true);
        });
        hasAttackTarget = true;
    }

    /**
     * I could very easily have just written 5 methods and avoided the holder class.
     * This isn't hard-coded because there are stands that change their movement animations based on certain conditions.
     * @return A MovementAnimationHolder().
     */
    public MovementAnimationHolder getMovementAnimations(){
        return new MovementAnimationHolder().create("idle", "forward", "left", "right", "backwards");
    }

    public HoveringMoveHandler getController(){
        return null;
    }

    /**
     * This method can be called on frames of stand moves, useful to create custom frames for niche cases.
     *
     * @param message1 The integer sent with the frame, generally used to differentiate the content of the next two parameters.
     */

    public void messageFrame(int message1, Object message2, Object message3){

    }

    /**
     * Some stands need to keep track of whatever entity they attacked last. This is called on every attack hitbox.
     * When multiple entities are hit, whatever entity is handled last (essentially random) is chosen.
     * @param entityIn The LivingEntity attacked last.
     */
    public void setMostRecentlyDamagedEntity(@Nullable LivingEntity entityIn){
        mostRecentlyDamagedEntity = entityIn;
    }

    public LivingEntity getMostRecentlyDamagedEntity(){
        if(mostRecentlyDamagedEntity != null) {
            if (!mostRecentlyDamagedEntity.isAlive()) {
                mostRecentlyDamagedEntity = null;
            }
        }
        return mostRecentlyDamagedEntity;
    }

    public boolean spendEnergy(double amount){

        return spendEnergy(amount, false);
    }

    /**
     * Attempt to spend energy.
     * @param amount The amount of energy being spent.
     * @param ignoreActionability Should this energy be spendable even if the stand is not currently actionable?
     * @return If the user has enough, spend it and return true. Otherwise, don't spend it and return false.
     */
    public boolean spendEnergy(double amount, boolean ignoreActionability){ //Channeled energy spending shouldn't require the stand to be actionable.

        AtomicBoolean state = new AtomicBoolean(false);

        if(!world.isRemote) {
            Stand.getLazyOptional(getMaster()).ifPresent(stand -> {

                if (stand.getCurrentStandEnergy() >= amount && ((getController().isActionable()) || ignoreActionability)) {
                    stand.setCurrentStandEnergy(stand.getCurrentStandEnergy() - amount);
                    stand.setEnergyCooldown(40);
                    state.set(true);
                } else {
                    state.set(false);
                }
            });
        }
        return state.get();
    }

    /**
     * Remove energy from a stand user. If they don't have enough, just hit 0.
     * @param amount How much energy to remove
     */
    public void penalizeEnergy(double amount){
        if(!world.isRemote) {
            Stand.getLazyOptional(getMaster()).ifPresent(stand -> {
                stand.setCurrentStandEnergy(Math.max(0, stand.getCurrentStandEnergy() - amount));
                stand.setEnergyCooldown(20);
            });
        }
    }

    /**
     * Called every tick to update the stands controller (Stand.getController)
     * @param isCharging Is the player inputting left click.
     * @param isBarrage Is the player sneaking.
     */
    public void chargeAttack(boolean isCharging, boolean isBarrage) {
        if (getMaster() == null) return;

        lastIsCharging = isCharging;
        lastIsCrouching = isBarrage;
    }

    /**
     * Add stand energy, up to the maximum amount.
     * @param amount The amount of energy to add.
     */
    public void addEnergy(double amount){
        if(!world.isRemote()) {
            Stand.getLazyOptional(getMaster()).ifPresent(stand -> {
                stand.setCurrentStandEnergy(Math.min(stand.getMaxStandEnergy(), stand.getCurrentStandEnergy() + amount));
            });
        }
    }

    /**
     * Check if the stand user has a certain amount of energy.
     * @param amount The amount of energy being checked.
     * @return True if the stand user's energy is greater than or equal to @param amount.
     */
    public boolean energyAtThreshold(double amount){

        AtomicBoolean state = new AtomicBoolean(false);

        Stand.getLazyOptional(getMaster()).ifPresent(stand -> {
            state.set(stand.getCurrentStandEnergy() >= amount);
        });
        return state.get();
    }

    /**
     * Get the percentage of a stand user's remaining energy.
     * @return Returns a double value between 0 and 1.
     */
    public double getEnergyPercentage(){
        AtomicDouble doublePercentage = new AtomicDouble(-1);

        Stand.getLazyOptional(getMaster()).ifPresent(stand -> {
            doublePercentage.set(stand.getCurrentStandEnergy() / stand.getMaxStandEnergy());
        });
        return doublePercentage.get();
    }

    /**
     * @return the modifier applied to damage received by the stand before dealt to the stand's master.
     */
    public float getDamageSharingPercentage(){
        return 0.5f;
    }

    public int attackParticle(){
        return 3;
    }
}
