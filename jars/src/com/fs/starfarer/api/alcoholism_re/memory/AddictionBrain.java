package com.fs.starfarer.api.alcoholism_re.memory;

import com.fs.starfarer.api.Global;

/**
 * literally 5head
 */

public class AddictionBrain {

    public static final float DAYS_PER_MONTH = 30f;
    public static final float CREW_PER_UNIT_PER_MONTH = Global.getSettings().getFloat("CREW_PER_UNIT_PER_MONTH");
    public static final float BASE_ADDICTION_GAIN_PER_DAY = Global.getSettings().getFloat("BASE_ADDICTION_GAIN_PER_DAY");
    public static final float MONTHLY_ADDICTION_GAIN_LIMIT_BEFORE_BLACKOUT = Global.getSettings().getFloat("MONTHLY_ADDICTION_GAIN_LIMIT_BEFORE_BLACKOUT");
    public static final float WATER_MULT = Global.getSettings().getFloat("WATER_MULT");;

    public static float getBaseConsumptionPerCrewPerDay() {
        return (1 / DAYS_PER_MONTH) / CREW_PER_UNIT_PER_MONTH;
    }

    //root function increase - this is the basis that everything is calculated off
    public static float getAddictionForDays(float mult, float days) {
        return (float) (mult * Math.sqrt((BASE_ADDICTION_GAIN_PER_DAY * Math.max(0f, days)) / 2));
    }

    //above root function, solved for "days"
    public static float getDaysAddicted(float mult, float addiction) {
        return (float) ((2f * Math.pow(addiction, 2)) / (Math.pow(mult, 2) * BASE_ADDICTION_GAIN_PER_DAY));
    }

    //consumption per day per crew member, for a certain addiction level
    public static float getConsumptionPerCrewPerDayForAddiction(float addiction) {
        return (1 + addiction) * getBaseConsumptionPerCrewPerDay();
    }

    //difference between current addiction and the addiction in X days
    public static float getAddictionIncrease(float mult, float currentAddiction, float days){
        float daysAddictet = getDaysAddicted(mult, currentAddiction);
        float addictionInDays = getAddictionForDays(mult, days + daysAddictet);
        return addictionInDays - currentAddiction;
    }
}
