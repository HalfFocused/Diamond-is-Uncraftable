package io.github.halffocused.diamond_is_uncraftable.util.movesets;

import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import io.github.halffocused.diamond_is_uncraftable.capability.Stand;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.AbstractStandEntity;
import io.github.halffocused.diamond_is_uncraftable.network.message.server.SAnimatePacket;
import io.github.halffocused.diamond_is_uncraftable.util.*;
import io.github.halffocused.diamond_is_uncraftable.util.globalabilities.TimestopHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.ArrayList;

public class HoveringMoveHandler {

    String currentAnimation;

    final static int chargeAttackBuffer = 5; //how long an input has to be held for the charge attack to register.
    final int barWidth = 20; //Width of the bars that appear in the action bar. 20 was my first instinct and I have liked it the most.

    boolean standHasAttackPosition;

    int chargeTicks;
    boolean pilotMode;
    int prevChargeTicks;
    String lastAnimation = "";

    public static class RepositionConstants{
        public static final int MASTER_POSITION = -1;
        public static final int IDLE_POSITION = -2;
        public static final int DO_NOT_MOVE = -3;
    }


    int unactionableTicks = 0;
    int unactionableTicksLastTick;

    int masterKeybindInput = 0;

    int mostRecentChargeAttackTicks = 0;

    /*
    Charge attack variables.
     */
    boolean hasChargeAttack = false;
    ChargeAttackFormat chargeAttackFormat = null;
    boolean forceChargeAttackRelease = false;

    ArrayList<Move> moveList = new ArrayList<>();

    private final AbstractStandEntity stand;


    public HoveringMoveHandler(AbstractStandEntity standIn){
        stand = standIn;
        pilotMode = false;
    }

    public HoveringMoveHandler(AbstractStandEntity standIn, boolean pilotModeIn){
        stand = standIn;
        pilotMode = pilotModeIn;
    }

    public void tick(boolean attackKeyDown, boolean specialKeyDown){
        if(stand.world.isRemote){return;}
        Stand.getLazyOptional(stand.getMaster()).ifPresent(props -> {

            if(Util.isTimeStoppedForEntity(stand.getMaster()) || props.getExperiencingTimeSkip()){
                setUnactionableTicks(1);
            }

            if(stand instanceof IMomentum){
                if(!props.getExperiencingTimeStop() || Util.canStandMoveInStoppedTime(props.getStandID()))
                props.setMomentum(Math.max(0, props.getMomentum() - (((IMomentum) stand).getMomentumDrainRate() / 20.0)));
            }

            handleAnimations();
            handleInputs(attackKeyDown, specialKeyDown);
            handleActionBar(specialKeyDown);
        });


        for(Move move : moveList){ //Tick active moves
            if(move.getFramedata().isActive()){
                move.getFramedata().attackTick(stand);
            }
        }

        Stand.getLazyOptional(stand.getMaster()).ifPresent(props -> {
            props.setPreventUnsummon(isMoveActive());

            if(unactionableTicks > 0){
                props.setPreventUnsummon3(true);
            }else if (unactionableTicksLastTick > 0){
                props.setPreventUnsummon3(false);
            }
        });



        unactionableTicksLastTick = unactionableTicks;
        unactionableTicks = Math.max(unactionableTicks - 1, 0);

        if (isMoveActive()) {
            if(getActiveMove().getRepositionDistance() > 0 && !standHasAttackPosition){
                stand.goToAttackPosition(getActiveMove().getRepositionDistance());
                standHasAttackPosition = true;
            }else if(getActiveMove().getRepositionDistance() == RepositionConstants.MASTER_POSITION){
                stand.setPosition(stand.getMaster().getPosX(), stand.getMaster().getPosY(), stand.getMaster().getPosZ());
                stand.setRotationYawHead(stand.getMaster().rotationYawHead);
                stand.setRotation(stand.getMaster().rotationYaw, stand.getMaster().rotationPitch);
            }else if(getActiveMove().getRepositionDistance() == RepositionConstants.IDLE_POSITION){
                stand.followMaster();
                stand.setRotationYawHead(stand.getMaster().rotationYawHead);
                stand.setRotation(stand.getMaster().rotationYaw, stand.getMaster().rotationPitch);
            }
        }else{
            stand.followMaster();
            stand.setRotationYawHead(stand.getMaster().rotationYawHead);
            stand.setRotation(stand.getMaster().rotationYaw, stand.getMaster().rotationPitch);
        }

    }

    public boolean isMoveActive(){
        for(Move move : moveList){
            if(move.getFramedata().isActive()){
                return true;
            }
        }
        return false;
    }

    public Move getActiveMove(){
        for(Move move : moveList){
            if(move.getFramedata().isActive()){
                return move;
            }
        }
        return null;
    }

     public void setMoveActive(int id){
        if(unactionableTicks == 0) {
            for (Move move : moveList) {
                if (move.getId() == id) {
                    move.getFramedata().initAttack();

                    Stand.getLazyOptional(stand.getMaster()).ifPresent(props -> {
                        props.setPreventUnsummon(true);
                    });
                }
            }
        }
    }

    public Move getMoveById(int id){
        for (Move move : moveList) {
            if (move.getId() == id) {
                return move;
            }
        }
        return null;
    }

    public void cancelActiveMoves(){
        for (Move move : moveList) {
            if (move.getFramedata().isActive()) {
                move.getFramedata().setActive(false);

                Stand.getLazyOptional(stand.getMaster()).ifPresent(props -> {
                    props.setPreventUnsummon(false);
                });
            }
        }
    }

    public HoveringMoveHandler addMove(String nameIn, int idIn, AttackFramedata frameIn, String animationIn){
        moveList.add(new Move(nameIn, frameIn, animationIn, idIn));
        return this;
    }

    public HoveringMoveHandler addMove(String nameIn, int idIn, AttackFramedata frameIn,String animationIn, double repositionDistanceIn){
        moveList.add(new Move(nameIn, frameIn, animationIn, idIn, repositionDistanceIn));
        return this;
    }

    public HoveringMoveHandler addChargeAttack(ChargeAttackFormat formatIn){
        chargeAttackFormat = formatIn;
        chargeAttackFormat.setMoveHandler(this);
        hasChargeAttack = true;
        return this;
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

    public void setMasterInputKeybind(int inputIn){
        masterKeybindInput = inputIn;
    }

    private void handleAnimations(){

        Stand.getLazyOptional(stand.getMaster()).ifPresent(props -> {

            if(TimestopHelper.isTimeStopped(stand.getMaster().world, stand.getMaster())){
                setAnimation("timestuck", true);
            }else{
                MovementAnimationHolder movementAnimations = stand.getMovementAnimations();

                if (isMoveActive()) {
                    if (getActiveMove().getAnimation() != null) { //moves with null as their animation will play the movement animations
                        setAnimation(getActiveMove().animation, true);
                    } else {
                        if (stand.getMaster().isSprinting()) {
                            setAnimation(movementAnimations.forwardAnimation, true);
                        } else if (this.masterKeybindInput == 0) {
                            setAnimation(movementAnimations.backwardsAnimation, true);
                        } else if (this.masterKeybindInput == 2) {
                            setAnimation(movementAnimations.rightAnimation, true);
                        } else if (this.masterKeybindInput == 1) {
                            setAnimation(movementAnimations.leftAnimation, true);
                        } else if (this.masterKeybindInput == 3) {
                            setAnimation(movementAnimations.idleAnimation, true);
                        }
                    }
                } else if (hasChargeAttack && chargeTicks > chargeAttackBuffer) {
                    if (chargeAttackFormat.isComplexCharge()) {
                        if (chargeAttackFormat.getAnimation1Duration() > chargeTicks - chargeAttackBuffer + chargeAttackBuffer) {
                            setAnimation(chargeAttackFormat.getChargingAnimation(), true);
                        } else {
                            setAnimation(chargeAttackFormat.getChargingAnimation2(), true);
                        }
                    } else {
                        setAnimation(chargeAttackFormat.chargingAnimation, true);
                    }
                } else {
                    if (stand.getMaster().isSprinting()) {
                        setAnimation(movementAnimations.forwardAnimation, true);
                    } else if (this.masterKeybindInput == 0) {
                        setAnimation(movementAnimations.backwardsAnimation, true);
                    } else if (this.masterKeybindInput == 2) {
                        setAnimation(movementAnimations.rightAnimation, true);
                    } else if (this.masterKeybindInput == 1) {
                        setAnimation(movementAnimations.leftAnimation, true);
                    } else if (this.masterKeybindInput == 3) {
                        setAnimation(movementAnimations.idleAnimation, true);
                    }
                }
            }
        });

    }

    private void handleInputs(boolean attackKey, boolean specialKey) {
            Stand.getLazyOptional(stand.getMaster()).ifPresent(props -> {
                props.setCharging(attackKey);
                boolean allowChargeAttack = !(stand instanceof IOverrideChargeAttack) || (((IOverrideChargeAttack) stand).shouldAllowChargeAttack() || chargeTicks != 0);

                if (attackKey && (chargeTicks < chargeAttackBuffer || allowChargeAttack)) {
                        prevChargeTicks = chargeTicks;
                        chargeTicks++;
                } else {
                    prevChargeTicks = chargeTicks;
                    chargeTicks = 0;
                }
            });

        if(isMoveActive())
            chargeTicks = 0;

        if(chargeAttackFormat != null){
            if(chargeAttackFormat.checkForInvoluntary(chargeTicks)){
                chargeTicks = 0;
            }
        }
        if(forceChargeAttackRelease){
            chargeTicks = 0;
        }
        forceChargeAttackRelease = false;
        if (chargeTicks == 0 && chargeTicks != prevChargeTicks && !isMoveActive()) {

            standHasAttackPosition = false;
            int outgoingChargeAttackTicks = prevChargeTicks;

            if(outgoingChargeAttackTicks <= chargeAttackBuffer){
                if(specialKey){
                    setMoveActive(stand.getBarrageMoveId());
                }else{
                    setMoveActive(stand.getJabMoveId());
                }
            }else if (chargeAttackFormat != null){
                if(stand instanceof IOverrideChargeAttack){
                    ((IOverrideChargeAttack) stand).chargeAttackRelease(prevChargeTicks);
                }else {
                    if (chargeAttackFormat.getMoveToActivate(prevChargeTicks) != -1) {
                        setMoveActive(chargeAttackFormat.getMoveToActivate(prevChargeTicks));
                    }
                }
            }
        }
    }

    public void forceChargeAttackRelease(){
        forceChargeAttackRelease = true;
    }

    public boolean isActionable(){
        return !isMoveActive() && unactionableTicks == 0;
    }

    public void setUnactionableTicks(int ticksIn){
        if(unactionableTicks == 0){
            cancelActiveMoves();
        }
        unactionableTicks = ticksIn;
    }

    public void setMostRecentChargeAttackTicks(int tickIn){
        mostRecentChargeAttackTicks = tickIn;
    }

    public int getMostRecentChargeAttackTicks(){
        return mostRecentChargeAttackTicks;
    }

    private void handleActionBar(boolean isSneaking){

        //I am the only person who sees this code and understands it.

        //Update: There is no one left who sees this code and understands it.

        PlayerEntity master = stand.getMaster();
        StringTextComponent actionText = new StringTextComponent("");
        Style moveNameStyle = Style.EMPTY.applyFormatting(TextFormatting.GRAY).setBold(true);
        Style menacingMoveNameStyle = Style.EMPTY.applyFormatting(TextFormatting.LIGHT_PURPLE).setBold(true).setItalic(true);
        Style notationStyle = Style.EMPTY.applyFormatting(TextFormatting.GRAY).setBold(false);
        Style actionBarBracketStyle = Style.EMPTY.applyFormatting(TextFormatting.BLACK).setBold(true);
        Style positiveStyle = Style.EMPTY.applyFormatting(TextFormatting.GREEN).setBold(false);
        Style negativeStyle = Style.EMPTY.applyFormatting(TextFormatting.RED).setBold(false);
        Style sneakIndicatorOffStyle = Style.EMPTY.applyFormatting(TextFormatting.DARK_RED).setBold(true);
        Style sneakIndicatorOnStyle = Style.EMPTY.applyFormatting(TextFormatting.GREEN).setBold(true);
        if(isMoveActive()){
            actionText.appendSibling(new StringTextComponent(getActiveMove().getName()).setStyle(getActiveMove().isMenacing() ? menacingMoveNameStyle : moveNameStyle));
            actionText.appendSibling(new StringTextComponent(" [").setStyle(actionBarBracketStyle));
            for(int i = 0; i < barWidth; i++){
                if(lerp(0,barWidth, (double) getActiveMove().getFramedata().getTicker() / getActiveMove().getFramedata().getAttackDuration()) > i){
                    actionText.appendSibling(new StringTextComponent("|").setStyle(positiveStyle));
                }else{
                    actionText.appendSibling(new StringTextComponent("|").setStyle(negativeStyle));
                }
            }
            actionText.appendSibling(new StringTextComponent("] ").setStyle(actionBarBracketStyle));
        }else if(hasChargeAttack && chargeTicks > chargeAttackBuffer){

            if(stand instanceof IOverrideChargeAttack){
                actionText = ((IOverrideChargeAttack) stand).generateHudText(chargeTicks);
            }else{
                if (chargeAttackFormat.getMoveToActivate(chargeTicks) != -1) {
                    actionText.appendSibling(new StringTextComponent(getMoveById(chargeAttackFormat.getMoveToActivate(chargeTicks)).getName()).setStyle(moveNameStyle));
                }

                if (chargeAttackFormat.getFollowingNode(chargeTicks) != null) {
                    actionText.appendSibling(new StringTextComponent(" [").setStyle(actionBarBracketStyle));

                    for (int i = 0; i < barWidth; i++) {

                        if (chargeAttackFormat.getNodeToActivate(chargeTicks) != null) {
                            if (lerp(0, barWidth, (double) (chargeTicks - chargeAttackFormat.getNodeToActivate(chargeTicks).tick) / ((chargeAttackFormat.getFollowingNode(chargeTicks).tick) - chargeAttackFormat.getNodeToActivate(chargeTicks).tick)) > i) {
                                actionText.appendSibling(new StringTextComponent("|").setStyle(positiveStyle));
                            } else {
                                actionText.appendSibling(new StringTextComponent("|").setStyle(negativeStyle));
                            }
                        } else {
                            if (lerp(0, barWidth, ((double) chargeTicks) / chargeAttackFormat.getFollowingNode(chargeTicks).tick) > i) {
                                actionText.appendSibling(new StringTextComponent("|").setStyle(positiveStyle));
                            } else {
                                actionText.appendSibling(new StringTextComponent("|").setStyle(negativeStyle));
                            }
                        }


                    }
                    actionText.appendSibling(new StringTextComponent("]-> ").setStyle(actionBarBracketStyle));
                    actionText.appendSibling(new StringTextComponent(getMoveById(chargeAttackFormat.getFollowingNode(chargeTicks).moveOnRelease).getName()).setStyle(moveNameStyle));
                }
            }
        }else if(isActionable()){
            actionText.appendSibling(new StringTextComponent("[Sneak]").setStyle(isSneaking ? sneakIndicatorOnStyle : sneakIndicatorOffStyle));
            actionText.appendSibling(new StringTextComponent(" Left-Click: ").setStyle(notationStyle));

            if(isSneaking){
                if(getMoveById(stand.getBarrageMoveId()) != null) {
                    actionText.appendSibling(new StringTextComponent(getMoveById(stand.getBarrageMoveId()).getName()).setStyle(moveNameStyle));
                }
            }else{
                if(getMoveById(stand.getJabMoveId()) != null) {
                    actionText.appendSibling(new StringTextComponent(getMoveById(stand.getJabMoveId()).getName()).setStyle(moveNameStyle));
                }
            }
        }else{
            String timer = ((unactionableTicks / 20.0) + "").substring(0, 3).concat("s");
            actionText.appendSibling(new StringTextComponent("Not Actionable" + (unactionableTicks > 1 ? ": " + timer : "")).setStyle(sneakIndicatorOffStyle));
        }

        master.sendStatusMessage(actionText, true);
    }

    private double lerp(int min, int max, double value)
    {
        return min + value * (max - min);
    }
}
