package com.fs.starfarer.api.alcoholism.memory;

// save shell, older saves hold one under KEY: delete on the next main game version update
@Deprecated
public class SettingsHandler {
    public static final String KEY = "alcoholism_settings";

    public float daysPerMonth = 30f;
    public float crewPerUnitPerMonth;
    public float baseAddictionGainPerDay;
    public float monthlyAddictionGainLimitBeforeBlackout;
    public float waterMult;
    public boolean adjustOP;
}
