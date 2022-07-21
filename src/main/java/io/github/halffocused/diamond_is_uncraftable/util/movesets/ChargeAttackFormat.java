package io.github.halffocused.diamond_is_uncraftable.util.movesets;

import java.util.ArrayList;

public class ChargeAttackFormat {

    /*
    This is yet another class that I probably don't need but will totally use :)
     */


    ArrayList<ChargeAttackNode> nodeList = new ArrayList<ChargeAttackNode>();

    String chargingAnimation;
    String chargingAnimation2;
    int animation1Duration;

    boolean active = false;
    boolean complexCharge = false;
    HoveringMoveHandler controller;

    public ChargeAttackFormat(String chargingAnimationIn){
        chargingAnimation = chargingAnimationIn;
    }

    public ChargeAttackFormat(String firstChargingAnimationIn, int animation1DurationIn, String secondChargingAnimationIn){
        complexCharge = true;
        chargingAnimation = firstChargingAnimationIn;
        chargingAnimation2 = secondChargingAnimationIn;
        animation1Duration = animation1DurationIn;
    }

    public ChargeAttackFormat addChargeNode(int tickIn, int moveOnReleaseIn, boolean voluntaryIn){
        nodeList.add(new ChargeAttackNode(tickIn, moveOnReleaseIn, voluntaryIn));
        return this;
    }

    public boolean checkForInvoluntary(int chargeAttackTicksIn){
        for(ChargeAttackNode node : nodeList){
            if(chargeAttackTicksIn > node.tick){
                if(!node.voluntary){
                    return true;
                }
            }
        }
        return false;
    }



    public int getMoveToActivate(int chargeAttackTicksIn){
        int returnValue = -1;

        for(int i = 0; i <= chargeAttackTicksIn; i++){
            for(ChargeAttackNode node : nodeList){
                if(node.tick == i){
                    returnValue = node.moveOnRelease;
                }
            }
        }
        controller.setMostRecentChargeAttackTicks(chargeAttackTicksIn);
        return returnValue;
    }

    public ChargeAttackNode getNodeToActivate(int chargeAttackTicksIn){
        ChargeAttackNode returnValue = null;

        for(int i = 0; i <= chargeAttackTicksIn; i++){
            for(ChargeAttackNode node : nodeList){
                if(node.tick == i){
                    returnValue = node;
                }
            }
        }
        return returnValue;
    }

    class ChargeAttackNode {
        int tick;
        int moveOnRelease;
        boolean voluntary; //If false, the move will automatically be used when the threshold is reached.

        public ChargeAttackNode(int tickIn, int moveOnReleaseIn, boolean voluntaryIn){
            tick = tickIn;
            moveOnRelease = moveOnReleaseIn;
            voluntary = voluntaryIn;
        }

    }

    public boolean getIsCharging(){
        return active;
    }

    public boolean isComplexCharge() {return complexCharge;}

    public int getAnimation1Duration(){return animation1Duration;}

    public String getChargingAnimation2() {return chargingAnimation2;}

    public String getChargingAnimation(){
        return chargingAnimation;
    }

    public void setMoveHandler(HoveringMoveHandler in){
        controller = in;
    }

    public ChargeAttackNode getFollowingNode(int currentChargeTick){
        int lastNodeTick = 0;
        for(ChargeAttackNode node : nodeList){
            if(node.tick > lastNodeTick){
                lastNodeTick = node.tick; //Get the last node tick
            }
        }


        for(int i = currentChargeTick + 1; i <= lastNodeTick; i++){
            for(ChargeAttackNode node : nodeList){
                if(node.tick == i){
                    return node;
                }
            }
        }
        return null;
    }

}
