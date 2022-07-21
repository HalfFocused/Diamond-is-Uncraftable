package io.github.halffocused.diamond_is_uncraftable.util.movesets;

import java.util.ArrayList;

public class Stance {

    private final int stanceId;
    private final double wanderDistanceMin;
    private final double wanderDistanceMax;
    private final double attackDistanceMin;
    private final double attackDistanceMax;
    private final double movementSpeed;
    private final String idleAnimation;
    private final String movingAnimation;
    private final boolean targetMaster;
    private ArrayList<WalkingMove> moveList = new ArrayList<>();

    public Stance(int stanceIdIn, double movementSpeedIn, double wanderDistanceMinIn, double wanderDistanceMaxIn, double attackDistanceMinIn, double attackDistanceMaxIn, String idleAnimationIn, String movingAnimationIn, boolean targetMasterIn){
        stanceId = stanceIdIn;
        movementSpeed = movementSpeedIn;
        wanderDistanceMin = wanderDistanceMinIn;
        wanderDistanceMax = wanderDistanceMaxIn;
        attackDistanceMin = attackDistanceMinIn;
        attackDistanceMax = attackDistanceMaxIn;
        idleAnimation = idleAnimationIn;
        movingAnimation = movingAnimationIn;
        targetMaster = targetMasterIn;
    }

    public Stance addWalkingMove(int id, Move move, double weight, double distanceToUse, int cooldown){

        moveList.add(new WalkingMove(id, move, weight, distanceToUse, cooldown));

        return this;
    }


    public ArrayList<WalkingMove> getMoves(){
        return moveList;
    }

    public ArrayList<WalkingMove> getRandomlySelectableMoves(){
        ArrayList<WalkingMove> returnList = new ArrayList<>();
        for(int i = 0; i < getMoves().size(); i++){
            if(getMoves().get(i).weight != 0){
                returnList.add(getMoves().get(i));
            }
        }

        return returnList;
    }

    public int getId(){
        return stanceId;
    }

    public double getMovementSpeed(){
        return movementSpeed;
    }

    public double getWanderDistanceMin() {
        return wanderDistanceMin;
    }

    public double getWanderDistanceMax() {
        return wanderDistanceMax;
    }

    public double getAttackDistanceMin() {
        return attackDistanceMin;
    }

    public double getAttackDistanceMax() {
        return attackDistanceMax;
    }

    public boolean getAttackMaster(){return targetMaster;}

    public String getIdleAnimation(){
        return idleAnimation;
    }

    public String getMovingAnimation(){
        return movingAnimation;
    }

    class WalkingMove{

        int id;
        Move move;
        double distanceToUse;
        double weight;
        int cooldown;

        public WalkingMove(int idIn, Move moveIn, double weightIn, double distanceToUseIn, int cooldownIn){
            id = idIn;
            move = moveIn;
            weight = weightIn;
            distanceToUse = distanceToUseIn;
            cooldown = cooldownIn;
        }
    }
}
