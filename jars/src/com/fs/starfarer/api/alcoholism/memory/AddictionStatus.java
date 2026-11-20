package com.fs.starfarer.api.alcoholism.memory;

public class AddictionStatus {
    private float addictionValue;
    private boolean isConsuming = false;
    // opened part of a unit the fleet drinks from, older saves load 0
    private float openedUnits;

    public AddictionStatus(){
        this.addictionValue = 0f;
    }

    public float getAddictionValue() {
        return addictionValue;
    }

    public void increment(float amt) {
        float next = addictionValue + amt;
        if(next > 1f) addictionValue = 1f;
        else if(next < 0f) addictionValue = 0f;
        else addictionValue += amt;
    }

    public boolean isConsuming() {
        return isConsuming;
    }

    public void setConsuming(boolean consuming) {
        this.isConsuming = consuming;
    }

    public float getOpenedUnits() {
        return openedUnits;
    }

    public void setOpenedUnits(float openedUnits) {
        this.openedUnits = Math.max(0f, openedUnits);
    }

    public boolean isWithdrawal(){
        return isAddicted() && !isConsuming;
    }

    public boolean isAddicted(){
        return getAddictionValue() > 0f;
    }
}
