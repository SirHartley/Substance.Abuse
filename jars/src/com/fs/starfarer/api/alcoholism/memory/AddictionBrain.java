package com.fs.starfarer.api.alcoholism.memory;

import com.fs.starfarer.api.alcoholism.config.Setting;

/**
 * literally 5head
 * Devmode adds 10 days of addiction each day, check AlcoholConsumptionManager
 */

public class AddictionBrain {
    public static final float DAYS_PER_MONTH = 30f;

    public static float getBaseConsumptionPerCrewPerDay() {
        return (1 / DAYS_PER_MONTH) / Setting.CREW_PER_UNIT_PER_MONTH.value();
    }

    //root function increase - this is the basis that everything is calculated off
    public static float getAddictionForDays(float mult, float days) {
        return (float) (mult * Math.sqrt((Setting.BASE_ADDICTION_GAIN_PER_DAY.value() * Math.max(0f, days)) / 2));
    }

    //above root function, solved for "days"
    public static float getDaysAddicted(float mult, float addiction) {
        return (float) ((2f * Math.pow(addiction, 2)) / (Math.pow(mult, 2) * Setting.BASE_ADDICTION_GAIN_PER_DAY.value()));
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
