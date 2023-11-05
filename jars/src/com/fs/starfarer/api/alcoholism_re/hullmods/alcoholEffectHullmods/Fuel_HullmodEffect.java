package com.fs.starfarer.api.alcoholism_re.hullmods.alcoholEffectHullmods;

import com.fs.starfarer.api.alcoholism_re.hullmods.BaseAlcoholHullmodEffect;
import com.fs.starfarer.api.alcoholism_re.hullmods.campaignEffects.SustainedBurnNavigationModifier;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;

public class Fuel_HullmodEffect extends BaseAlcoholHullmodEffect {
    public static final float MAX_ACCEL_INCREASE = 30f;
    public static final float MAX_SPEED_INCREASE = 30f;
    public static final float MAX_MANEUV_INCREASE = 30f;
    public static final float MAX_SBURN_MANEUV_IMPROVE_MULT = 1f;

    public static final float NEG_WEAPON_RANGE_PERCENT = -10f;
    public static final float NEG_VENT_EFFECTIVENESS_PERCENT = -10f;

    public static final float WITHDRAWAL_MAX_CR_REDUCTION = -20f;
    public static final float WITHDRAWAL_ACCEL_DECREASE = -20;
    public static final float WITHDRAWAL_SPEED_DECREASE = -20;
    public static final float WITHDRAWAL_MANEUV_DECREASE = -20;

    public static final float WITHDRAWAL_85_BURN_DECREASE = -2;
    public static final float WITHDRAWAL_70_BURN_DECREASE = -1;

    @Override
    public void init(HullModSpecAPI spec) {
        super.init(spec);
    }

    @Override
    public void applyPositives(MutableShipStatsAPI stats, float effectMult, String id) {
        //increase ship accel, manoeuvrability and speed by 30%,
        //increase manoeuvrability under sustained burn
        stats.getAcceleration().modifyMult(id, getPercentToCorrectedMult(MAX_ACCEL_INCREASE), getDesc());
        stats.getMaxTurnRate().modifyMult(id, getPercentToCorrectedMult(MAX_MANEUV_INCREASE), getDesc());
        stats.getMaxSpeed().modifyMult(id, getPercentToCorrectedMult(MAX_SPEED_INCREASE), getDesc());

        SustainedBurnNavigationModifier.register();
    }

    @Override
    public void applyNegatives(MutableShipStatsAPI stats, float effectMult, String id) {
        //shorten weapon max range (makes it hard to see…)
        //Reduces effectiveness of vents by 30%
        stats.getWeaponRangeThreshold().modifyFlat(id, getPercentToCorrectedMult(NEG_WEAPON_RANGE_PERCENT), getDesc());
        stats.getWeaponRangeMultPastThreshold().modifyMult(id, getPercentToCorrectedMult(NEG_WEAPON_RANGE_PERCENT), getDesc());

        stats.getVentRateMult().modifyMult(id, getPercentToCorrectedMult(NEG_VENT_EFFECTIVENESS_PERCENT), getDesc());
    }

    @Override
    public void applyWithdrawal(MutableShipStatsAPI stats, float effectMult, String id) {
        //Lower max CR ,
        //Reduce ship accel and speed,
        //burn level -2
        stats.getMaxCombatReadiness().modifyMult(id, getPercentToCorrectedMult(WITHDRAWAL_MAX_CR_REDUCTION), getDesc());
        stats.getMaxSpeed().modifyMult(id, getPercentToCorrectedMult(WITHDRAWAL_SPEED_DECREASE), getDesc());

        if(effectMult > 0.5f){
            stats.getAcceleration().modifyMult(id, getPercentToCorrectedMult(WITHDRAWAL_ACCEL_DECREASE), getDesc());
            stats.getMaxTurnRate().modifyMult(id, getPercentToCorrectedMult(WITHDRAWAL_MANEUV_DECREASE), getDesc());
        }

        if(effectMult > 0.7f){
            float decrease = effectMult > 0.85 ? WITHDRAWAL_85_BURN_DECREASE : WITHDRAWAL_70_BURN_DECREASE;
            stats.getMaxBurnLevel().modifyFlat(id, decrease, getDesc());
        }
    }

    @Override
    public void addPositiveEffectTooltip(TooltipMakerAPI tooltip, float effectMult) {
        float opad = 10f;
        float spad = 3f;
        Color positive = Misc.getPositiveHighlightColor();
        Color neutral = Misc.getGrayColor();
        Color negative = Misc.getNegativeHighlightColor();

        tooltip.addSectionHeading("Positive Effect", Misc.getTextColor(), new Color(50, 100, 50, 255), Alignment.MID, 10f);
                
        tooltip.addPara("Increases max. speed by %s  [Max.: %s]",
                opad,
                positive,
                getAbsPercentStringForTooltip(MAX_SPEED_INCREASE, effectMult),
                getAbsPercentStringForTooltip(MAX_SPEED_INCREASE));

        tooltip.addPara("Increases acceleration by %s  [Max.: %s]",
                spad,
                positive,
                getAbsPercentStringForTooltip(MAX_ACCEL_INCREASE, effectMult),
                getAbsPercentStringForTooltip(MAX_ACCEL_INCREASE));

        tooltip.addPara("Increases maneuverability speed by %s  [Max.: %s]",
                spad,
                positive,
                getAbsPercentStringForTooltip(MAX_MANEUV_INCREASE, effectMult),
                getAbsPercentStringForTooltip(MAX_MANEUV_INCREASE));

        tooltip.addPara("Improves sustained burn maneuverability by %s  [Max.: %s]",
                spad,
                positive,
                Misc.getRoundedValueMaxOneAfterDecimal(1 + (Fuel_HullmodEffect.MAX_SBURN_MANEUV_IMPROVE_MULT * effectMult)) + "x",
                (int) (1 + Fuel_HullmodEffect.MAX_SBURN_MANEUV_IMPROVE_MULT) + "x");
    }

    @Override
    public void addNegativeEffectTooltip(TooltipMakerAPI tooltip, float effectMult) {
        float opad = 10f;
        float spad = 3f;
        Color positive = Misc.getPositiveHighlightColor();
        Color neutral = Misc.getGrayColor();
        Color negative = Misc.getNegativeHighlightColor();

        tooltip.addSectionHeading("Negative Effect", Misc.getTextColor(), new Color(150, 100, 50, 255), Alignment.MID, 10f);
        
        tooltip.addPara("Reduces weapon range by %s  [Max.: %s]",
                opad,
                negative,
                getAbsPercentStringForTooltip(NEG_WEAPON_RANGE_PERCENT, effectMult),
                getAbsPercentStringForTooltip(NEG_WEAPON_RANGE_PERCENT));

        tooltip.addPara("Reduces dissipation rate by %s  [Max.: %s]",
                spad,
                negative,
                getAbsPercentStringForTooltip(NEG_VENT_EFFECTIVENESS_PERCENT, effectMult),
                getAbsPercentStringForTooltip(NEG_VENT_EFFECTIVENESS_PERCENT));
    }

    @Override
    public void addWithdrawalEffectTooltip(TooltipMakerAPI tooltip, float effectMult) {
        float opad = 10f;
        float spad = 3f;
        Color positive = Misc.getPositiveHighlightColor();
        Color negative = Misc.getNegativeHighlightColor();
        Color bad = Color.red;

        tooltip.addSectionHeading("Withdrawal Effect", Misc.getTextColor(), new Color(150, 50, 50, 255), Alignment.MID, 10f);
        
        tooltip.addPara("Lowers max. combat readiness by %s  [Max.: %s]",
                opad,
                bad,
                getAbsPercentStringForTooltip(WITHDRAWAL_MAX_CR_REDUCTION, effectMult),
                getAbsPercentStringForTooltip(WITHDRAWAL_MAX_CR_REDUCTION));

        tooltip.addPara("Reduces max. speed by %s  [Max.: %s]",
                spad,
                bad,
                getAbsPercentStringForTooltip(WITHDRAWAL_SPEED_DECREASE, effectMult),
                getAbsPercentStringForTooltip(WITHDRAWAL_SPEED_DECREASE));

        if(effectMult > 0.5f){
            tooltip.addPara("Reduces acceleration by %s  [Max.: %s]",
                    spad,
                    bad,
                    getAbsPercentStringForTooltip(WITHDRAWAL_ACCEL_DECREASE, effectMult),
                    getAbsPercentStringForTooltip(WITHDRAWAL_ACCEL_DECREASE));

            tooltip.addPara("Reduces maneuverability by %s  [Max.: %s]",
                    spad,
                    bad,
                    getAbsPercentStringForTooltip(WITHDRAWAL_MANEUV_DECREASE, effectMult),
                    getAbsPercentStringForTooltip(WITHDRAWAL_MANEUV_DECREASE));
        }

        if(effectMult > 0.7f){
            float maxDecrease = effectMult > 0.85 ? WITHDRAWAL_85_BURN_DECREASE : WITHDRAWAL_70_BURN_DECREASE;
            float decrease = maxDecrease * effectMult;

            tooltip.addPara("Reduces max. burn by %s",
                    spad,
                    bad,
                    Math.round(decrease) + "");
        }
    }
}
