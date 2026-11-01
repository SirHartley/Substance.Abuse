package com.fs.starfarer.api.alcoholism;

import com.fs.starfarer.api.alcoholism.listeners.AlcoholConsumptionManager;
import com.fs.starfarer.api.alcoholism.memory.AddictionBrain;
import com.fs.starfarer.api.alcoholism.memory.Alcohol;
import com.fs.starfarer.api.alcoholism.memory.AlcoholRepo;
import com.fs.starfarer.api.util.Pair;

import java.awt.*;

/**
 * Calculation stuff is not in AddictionBrain because it accesses campaign data
 */

public class TooltipHelper {

    public static Pair<String, Color> getResistanceBuildupStringAndColour(String id){
        Alcohol alcohol = AlcoholRepo.get(id);
        float mult = alcohol.getMult();

        if(mult > 1.7f) return new Pair<>("extreme", new Color(255, 30, 0, 255));
        if(mult > 1.5f) return new Pair<>("high", new Color(200, 120, 0, 255));
        if(mult > 1.2f) return new Pair<>("moderate", new Color(255, 220, 0, 255));
        if(mult >= 1f) return new Pair<>("slow", new Color(100,190,100,255));

        return new Pair<>("CHUG CHUG CHUG LETS GO", Color.MAGENTA);
    }

    public static String getConsumptionFactor(String id){
        Alcohol alcohol = AlcoholRepo.get(id);
        float roundedAddiction = Math.round(alcohol.getAddictionStatus().getAddictionValue() * 100f)/100f;
        return (1 + roundedAddiction) + "x";
    }

    public static String getCurrentEffectPercentString(String id){
        Alcohol alcohol = AlcoholRepo.get(id);
        return Math.round(alcohol.getAddictionStatus().getAddictionValue() * 100) + "%";
    }

    public static int getPredictedAmountRequiredForOneMonth(String id){
        Alcohol alcohol = AlcoholRepo.get(id);
        float daysPerMonth = AddictionBrain.DAYS_PER_MONTH;
        float addictionInOneMonth = getFutureAddictionAfterDaysPassed(alcohol, daysPerMonth);

        return (int) Math.ceil(AlcoholConsumptionManager.getRequiredUnitsPerDay(addictionInOneMonth) * daysPerMonth);
    }

    public static float getFutureAddictionAfterDaysPassed(Alcohol alcohol, float days){
        float mult = alcohol.getMult();
        float currentAddiction = alcohol.getAddictionStatus().getAddictionValue();
        float daysAddictet = AddictionBrain.getDaysAddicted(mult, currentAddiction);
        return AddictionBrain.getAddictionForDays(mult, days + daysAddictet);
    }

    public static int getAmountWillLastDays(Alcohol alcohol, float amt){
        float amtLasts = amt / AlcoholConsumptionManager.getCurrentRequiredUnitsPerDay(alcohol);
        float addictionAfterAmt = getFutureAddictionAfterDaysPassed(alcohol, amtLasts);
        return (int)Math.ceil(amt / AlcoholConsumptionManager.getRequiredUnitsPerDay(addictionAfterAmt));
    }

    public static String getDayOrDays(float days) {
        int d = (int) Math.round(days);
        String daysStr = "days";
        if (d == 1) {
            daysStr = "day";
        }
        return daysStr;
    }

    public static String getMonthOrMonths(float months) {
        int d = (int) Math.round(months);
        String daysStr = "months";
        if (d == 1) {
            daysStr = "month";
        }
        return daysStr;
    }
}
