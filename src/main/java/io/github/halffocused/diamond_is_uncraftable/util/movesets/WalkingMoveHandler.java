package io.github.halffocused.diamond_is_uncraftable.util.movesets;


import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import io.github.halffocused.diamond_is_uncraftable.capability.Stand;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.AbstractStandEntity;
import io.github.halffocused.diamond_is_uncraftable.network.message.server.SAnimatePacket;
import io.github.halffocused.diamond_is_uncraftable.util.ICommandGrab;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.ArrayList;

public class WalkingMoveHandler {

    private enum Targeting {WITH_MASTER, ALL, HOSTILE_ONLY, PLAYERS_ONLY}

    Targeting currentTargeting = Targeting.WITH_MASTER;

    Stance.WalkingMove nextMove = null;
    private final AbstractStandEntity stand;
    ArrayList<Stance> stanceList = new ArrayList<>();
    int activeStanceId;
    LivingEntity currentTarget = null; //null if no target
    boolean walkingOrIdle = false; // true if walking
    boolean outOfRange = false;
    boolean tooFar = false;
    boolean tooClose = false;
    String lastAnimation = "joejan";
    String currentAnimation = "";
    int attackCooldown = 10;

    /*
    Command grab stuff :/
     */
    boolean hasCommandGrab;
    LivingEntity commandGrabEntity = null;


    public WalkingMoveHandler(AbstractStandEntity standIn, int defaultStance){
        stand = standIn;
        activeStanceId = defaultStance;
        hasCommandGrab = standIn instanceof ICommandGrab;
    }

    public WalkingMoveHandler addStance(Stance stanceIn){
        stanceList.add(stanceIn);
        return this;
    }

    public Stance getActiveStance(){
        for(Stance stance : stanceList){
            if(stance.getId() == activeStanceId){
                return stance;
            }
        }
        return null; //Should never, ever, happen :)
    }

    public int getActiveStanceId(){
        return getActiveStance().getId();
    }

    public void setActiveStance(int activeStanceId) {
        if(getActiveStanceId() != activeStanceId) {
            this.activeStanceId = activeStanceId;
            currentTarget = null;
        }
    }

    public boolean isMoveActive(){
        for(Stance.WalkingMove move : getActiveStance().getMoves()){
            if(move.move.getFramedata().isActive()){
                return true;
            }
        }
        return false;
    }

    public Stance.WalkingMove getActiveMove(){
        for(Stance.WalkingMove move : getActiveStance().getMoves()){
            if(move.move.getFramedata().isActive()){
                return move;
            }
        }
        return null;
    }

    public void tick(boolean attackKeyDown, boolean specialKeyDown){
        if(stand.world.isRemote){return;}


        if(stand instanceof ICommandGrab) {
            if (commandGrabEntity != null) {
                if (commandGrabEntity.isAlive()) {
                    double distance = ((ICommandGrab) stand).commandGrabDistance();
                    Vec3d positionVector = this.stand.getPositionVec().add(this.stand.getLookVec().normalize().mul(distance, distance, distance));
                    commandGrabEntity.setPosition(positionVector.x,positionVector.y,positionVector.z);
                }
            }
        }

        if(attackCooldown == 1){
            nextMove = null;
        }

        pickTarget();
        if(commandGrabEntity == null) {
            handleMovement();
        }
        handleAnimations();
        Stand.getLazyOptional(stand.getMaster()).ifPresent(stand -> {
            stand.setPreventUnsummon(currentTarget != null);
        });

        if(currentTarget != null) {
            if (!currentTarget.isAlive()) {
                currentTarget = null;
            }
        }

        attackCooldown = Math.max(0, attackCooldown - 1);

        for(Stance.WalkingMove walkingMove : getActiveStance().getMoves()){ //Tick active moves
            if(walkingMove.move.getFramedata().isActive()){
                walkingMove.move.getFramedata().attackTick(stand);
            }
        }
    }

    public void setMoveActive(int id){
            for (Stance.WalkingMove Wmove : getActiveStance().getMoves()) {
                if (Wmove.move.getId() == id) {
                    Wmove.move.getFramedata().initAttack();
                    attackCooldown = Wmove.cooldown + Wmove.move.getFramedata().getAttackDuration();
                }
            }
    }


    private void handleMovement(){

        if (currentTarget == null) { //No target, automatically follows master
            if (stand.getDistance(stand.getMaster()) > getActiveStance().getWanderDistanceMax()) {
                outOfRange = true;
                tooFar = true;
            }

            if(stand.getDistance(stand.getMaster()) < getActiveStance().getWanderDistanceMin()){
                outOfRange = true;
                tooClose = true;
            }

            if (outOfRange) {
                if (stand.canEntityBeSeen(stand.getMaster())) {
                    walkingOrIdle = true;
                    Vec3d movementVector = stand.getMaster().getPositionVector().subtract(stand.getPositionVec()).normalize().mul(getActiveStance().getMovementSpeed(), 0, getActiveStance().getMovementSpeed());
                    movementVector = tooClose ? movementVector.inverse() : movementVector;


                    if(tooFar){
                        stand.faceEntity(stand.getMaster(), Float.MAX_VALUE, Float.MAX_VALUE);
                    }
                    stand.setMotion(movementVector.x, stand.getMotion().y, movementVector.z);

                    handleJump(new AxisAlignedBB(stand.getPosX() - 1.2, stand.getPosY(), stand.getPosZ() - 1.2, stand.getPosX() + 1.2, stand.getPosY() + 0.35, stand.getPosZ() + 1.2));

                } else {
                    walkingOrIdle = false;
                }

                if(stand.getDistance(stand.getMaster()) > getActiveStance().getWanderDistanceMin() * 1.2 && stand.getDistance(stand.getMaster()) < getActiveStance().getWanderDistanceMax() * 0.8){
                    outOfRange = false;
                    tooClose = false;
                    tooFar = false;
                }

            } else {
                walkingOrIdle = false;
            }
        } else {
            if(!isMoveActive()) {
                stand.faceEntity(currentTarget, Float.MAX_VALUE, Float.MAX_VALUE);
            }

            if(nextMove == null){
                nextMove = pickNextMove(getActiveStance());
            }
            outOfRange = stand.getDistance(currentTarget) > nextMove.distanceToUse * 0.8;

            if (outOfRange) {
                if (stand.canEntityBeSeen(currentTarget) && attackCooldown == 0 && !isMoveActive()) {
                    walkingOrIdle = true;
                    Vec3d movementVector = currentTarget.getPositionVector().subtract(stand.getPositionVec()).normalize().mul(getActiveStance().getMovementSpeed(), 0, getActiveStance().getMovementSpeed());
                    stand.setMotion(movementVector.x, stand.getMotion().y, movementVector.z);

                    handleJump(new AxisAlignedBB(stand.getPosX() - 1.2, stand.getPosY(), stand.getPosZ() - 1.2, stand.getPosX() + 1.2, stand.getPosY() + 0.35, stand.getPosZ() + 1.2));

                    if (stand.getDistance(currentTarget) <= nextMove.distanceToUse * 0.8) {
                        outOfRange = false;
                    }
                } else {
                    walkingOrIdle = false;
                }

            } else {
                walkingOrIdle = false;
                if(!isMoveActive() && attackCooldown == 0) {
                    setMoveActive(nextMove.id);
                }
            }
        }
    }

    private void handleJump(AxisAlignedBB p_70972_1_) {
        int i = MathHelper.floor(p_70972_1_.minX);
        int j = MathHelper.floor(p_70972_1_.minY);
        int k = MathHelper.floor(p_70972_1_.minZ);
        int l = MathHelper.floor(p_70972_1_.maxX);
        int i1 = MathHelper.floor(p_70972_1_.maxY);
        int j1 = MathHelper.floor(p_70972_1_.maxZ);

        for(int k1 = i; k1 <= l; ++k1) {
            for(int l1 = j; l1 <= i1; ++l1) {
                for(int i2 = k; i2 <= j1; ++i2) {
                    BlockPos blockpos = new BlockPos(k1, l1, i2);
                    BlockState blockstate = stand.world.getBlockState(blockpos);
                    if (!blockstate.isAir(stand.world, blockpos) && blockstate.isSolid() && blockstate.getMaterial() != Material.SNOW) {
                        stand.setPosition(stand.getPosX(), stand.getPosY() + 0.15, stand.getPosZ());
                    }
                }
            }
        }
    }

    private void handleAnimations(){
        if(stand instanceof ICommandGrab && commandGrabEntity != null ){
            if (commandGrabEntity.isAlive()) {
                setAnimation(((ICommandGrab) stand).holdingAnimation(), true);
            }
        }else if(isMoveActive()){
            setAnimation(getActiveMove().move.animation, true);
        }else{
            setAnimation(walkingOrIdle ? getActiveStance().getMovingAnimation() : getActiveStance().getIdleAnimation(), true);
        }
    }

    public void setAnimation(String animationNameIn, boolean shouldLoop){
        if(!stand.world.isRemote) {
            if(!animationNameIn.equals(lastAnimation)) {
                DiamondIsUncraftable.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> stand), new SAnimatePacket(stand.getEntityId(), animationNameIn, shouldLoop));
                currentAnimation = animationNameIn;
                lastAnimation = animationNameIn;
            }
        }
    }

    /**
     * Weighted randomness formula from: https://stackoverflow.com/questions/6737283/weighted-randomness-in-java
     * Took me a lil bit to figure out how it worked but it's very cool!
     * @param stance The Stance
     * @return Walking Move
     */
    private Stance.WalkingMove pickNextMove(Stance stance){
        ArrayList<Stance.WalkingMove> list = getActiveStance().getRandomlySelectableMoves();
        double totalWeight = 0.0;
        for(Stance.WalkingMove move : list){
            totalWeight += move.weight;
        }
        int idx = 0;
        for(double r = Math.random() * totalWeight; idx < list.size() - 1; ++idx){
            r -= list.get(idx).weight;
            if(r < 0.0){break;}
        }
        return list.get(idx);
    }


    private void pickTarget(){
        ArrayList<LivingEntity> possibleTargets = new ArrayList<>();
        if((currentTargeting != Targeting.WITH_MASTER || getActiveStance().getAttackMaster()) && !stand.world.isRemote()){
            if(currentTarget == null) {
                if(currentTargeting == Targeting.ALL || getActiveStance().getAttackMaster()) {
                    stand.getServer().getWorld(stand.dimension).getEntities()
                            .filter(entity -> !entity.equals(stand))
                            .filter(entity -> stand.canEntityBeSeen(entity))
                            .filter(entity -> entity instanceof LivingEntity)
                            .filter(entity -> isEntityWithinAttackRange(getActiveStance(), (LivingEntity) entity))
                            .filter(entity -> !(entity instanceof PlayerEntity) || !((PlayerEntity) entity).isCreative() || entity.isSpectator())
                            .forEach(entity -> possibleTargets.add((LivingEntity) entity));
                }else if(currentTargeting == Targeting.HOSTILE_ONLY){
                    stand.getServer().getWorld(stand.dimension).getEntities()
                            .filter(entity -> !entity.equals(stand))
                            .filter(entity -> stand.canEntityBeSeen(entity))
                            .filter(entity -> entity instanceof MobEntity)
                            .filter(entity -> isEntityWithinAttackRange(getActiveStance(), (LivingEntity) entity))
                            .forEach(entity -> possibleTargets.add((LivingEntity) entity));
                }else if(currentTargeting == Targeting.PLAYERS_ONLY){
                    stand.getServer().getWorld(stand.dimension).getEntities()
                            .filter(entity -> !entity.equals(stand))
                            .filter(entity -> stand.canEntityBeSeen(entity))
                            .filter(entity -> entity instanceof PlayerEntity)
                            .filter(entity -> isEntityWithinAttackRange(getActiveStance(), (LivingEntity) entity))
                            .filter(entity -> !((PlayerEntity) entity).isCreative() || entity.isSpectator())
                            .forEach(entity -> possibleTargets.add((LivingEntity) entity));
                }

                if(!getActiveStance().getAttackMaster()){
                    possibleTargets.remove(stand.getMaster());
                }
                if(possibleTargets.size() > 0) {
                    currentTarget = possibleTargets.get(stand.getRNG().nextInt(possibleTargets.size()));
                }
            }
        }
    }

    private boolean isEntityWithinAttackRange(Stance stance, LivingEntity entity){
        return entity.getDistance(stand.getMaster()) > stance.getAttackDistanceMin() && entity.getDistance(stand.getMaster()) < getActiveStance().getAttackDistanceMax();
    }


    public void changeTargetting(){
        Style actionBarStyle = new Style().setColor(TextFormatting.LIGHT_PURPLE);

        switch (currentTargeting){
            case WITH_MASTER:
                currentTargeting = Targeting.ALL;
                stand.getMaster().sendStatusMessage(new StringTextComponent("Targeting all entities").setStyle(actionBarStyle), true);
                break;
            case ALL:
                currentTargeting = Targeting.HOSTILE_ONLY;
                stand.getMaster().sendStatusMessage(new StringTextComponent("Targeting hostile entities").setStyle(actionBarStyle), true);
                break;
            case HOSTILE_ONLY:
                currentTargeting = Targeting.PLAYERS_ONLY;
                stand.getMaster().sendStatusMessage(new StringTextComponent("Targeting players").setStyle(actionBarStyle), true);
                break;
            case PLAYERS_ONLY:
                currentTargeting = Targeting.WITH_MASTER;
                stand.getMaster().sendStatusMessage(new StringTextComponent("Following master").setStyle(actionBarStyle), true);
                break;
            default:
                break;
        }
        currentTarget = null;
    }

    public void startCommandGrab(LivingEntity entity){
        commandGrabEntity = entity;
    }
}
