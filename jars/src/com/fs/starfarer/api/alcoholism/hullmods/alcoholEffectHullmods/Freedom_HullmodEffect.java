package com.fs.starfarer.api.alcoholism.hullmods.alcoholEffectHullmods;

import com.fs.starfarer.api.alcoholism.hullmods.BaseAlcoholHullmodEffect;
import com.fs.starfarer.api.alcoholism.hullmods.campaignEffects.TariffModifier;
import com.fs.starfarer.api.alcoholism.hullmods.campaignEffects.TerrainEffectModifier;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;

public class Freedom_HullmodEffect extends BaseAlcoholHullmodEffect {

    public static final float MAX_TARIFF_DECREASE_PERCENT = -20f;
    public static final float MAX_SENSOR_PROFILE_RED_PERCENT = 20f;
    public static final float MAX_FIGHTER_REFIT_DECREASE_PERCENT = -20f;
    public static final float MAX_FIGHTER_RANGE_INCREASE_PERCENT = 30f;
    public static final float MAX_DAMAGE_TO_FIGHTERS_RED_PERCENT = -10f;

    public static final float NEG_TERRAIN_EFFECT_INCREASE = 10f;
    public static final float NEG_ROF_DECREASE_PERCENT = -10f;

    public static final float WITHDRAWAL_TARIFF_INCREASE_PERCENT = 20f;
    public static final float WITHDRAWAL_MAX_CR_REDUCTION = -20f;
    public static final float WITHDRAWAL_DAMAGE_TO_FIGHTERS_INCREASE_PERCENT = 20f;
    public static final float WITHDRAWAL_REPLACEMENT_RATE_RED_PERCENT = 10f;

    @Override
    public void applyPositives(MutableShipStatsAPI stats, float effectMult, String id) {
        //Lowers Taxes by 30%
        //Decrease sensor profile
        //CARRIER THINGS
        stats.getFighterRefitTimeMult().modifyMult(id, getPercentToCorrectedMult(MAX_FIGHTER_REFIT_DECREASE_PERCENT), getDesc());
        stats.getFighterWingRange().modifyMult(id, getPercentToCorrectedMult(MAX_FIGHTER_RANGE_INCREASE_PERCENT), getDesc());
        stats.getDamageToFighters().modifyMult(id, getPercentToCorrectedMult(MAX_DAMAGE_TO_FIGHTERS_RED_PERCENT), getDesc());

        stats.getSensorProfile().modifyMult(id, getPercentToCorrectedMult(MAX_SENSOR_PROFILE_RED_PERCENT), getDesc());
        TariffModifier.register();
    }

    @Override
    public void applyNegatives(MutableShipStatsAPI stats, float effectMult, String id) {
        //Damage taken by terrain increased
        //RoF on non-missile weapons decreased
        TerrainEffectModifier.register();

        stats.getBallisticRoFMult().modifyMult(id, getPercentToCorrectedMult(NEG_ROF_DECREASE_PERCENT), getDesc());
        stats.getEnergyRoFMult().modifyMult(id, getPercentToCorrectedMult(NEG_ROF_DECREASE_PERCENT), getDesc());
    }

    @Override
    public void applyWithdrawal(MutableShipStatsAPI stats, float effectMult, String id) {
        //Lower max CR
        //increase tariffs
        stats.getMaxCombatReadiness().modifyMult(id, getPercentToCorrectedMult(WITHDRAWAL_MAX_CR_REDUCTION), getDesc());
        stats.getDamageToFighters().modifyMult(id, getPercentToCorrectedMult(WITHDRAWAL_DAMAGE_TO_FIGHTERS_INCREASE_PERCENT), getDesc());
        stats.getDynamic().getMod(Stats.REPLACEMENT_RATE_DECREASE_MULT).modifyMult(id, getPercentToCorrectedMult(WITHDRAWAL_REPLACEMENT_RATE_RED_PERCENT), getDesc());
    }

    @Override
    public void addPositiveEffectTooltip(TooltipMakerAPI tooltip, float effectMult) {
        float opad = 10f;
        float spad = 3f;
        Color positive = Misc.getPositiveHighlightColor();
        Color neutral = Misc.getGrayColor();
        Color negative = Misc.getNegativeHighlightColor();

        tooltip.addSectionHeading("Positive Effect", Misc.getTextColor(), new Color(50, 100, 50, 255), Alignment.MID, 10f);
        
        tooltip.addPara("Reduces trade tariffs by %s  [Max.: %s]",
                opad,
                positive,
                getAbsPercentStringForTooltip(MAX_TARIFF_DECREASE_PERCENT, effectMult),
                getAbsPercentStringForTooltip(MAX_TARIFF_DECREASE_PERCENT));

        tooltip.addPara("Reduces sensor profile by %s  [Max.: %s]",
                spad,
                positive,
                getAbsPercentStringForTooltip(MAX_SENSOR_PROFILE_RED_PERCENT, effectMult),
                getAbsPercentStringForTooltip(MAX_SENSOR_PROFILE_RED_PERCENT));

        tooltip.addPara("Reduces fighter refit time by %s  [Max.: %s]",
                spad,
                positive,
                getAbsPercentStringForTooltip(MAX_FIGHTER_REFIT_DECREASE_PERCENT, effectMult),
                getAbsPercentStringForTooltip(MAX_FIGHTER_REFIT_DECREASE_PERCENT));

        tooltip.addPara("Increases fighter range by %s  [Max.: %s]",
                spad,
                positive,
                getAbsPercentStringForTooltip(MAX_FIGHTER_RANGE_INCREASE_PERCENT, effectMult),
                getAbsPercentStringForTooltip(MAX_FIGHTER_RANGE_INCREASE_PERCENT));

        if(effectMult > 0.7f) tooltip.addPara("Reduces fighter damage taken by %s  [Max.: %s]",
                spad,
                positive,
                getAbsPercentStringForTooltip(MAX_DAMAGE_TO_FIGHTERS_RED_PERCENT, effectMult),
                getAbsPercentStringForTooltip(MAX_DAMAGE_TO_FIGHTERS_RED_PERCENT));

    }

    @Override
    public void addNegativeEffectTooltip(TooltipMakerAPI tooltip, float effectMult) {
        float opad = 10f;
        float spad = 3f;
        Color positive = Misc.getPositiveHighlightColor();
        Color neutral = Misc.getGrayColor();
        Color negative = Misc.getNegativeHighlightColor();
        
        tooltip.addSectionHeading("Negative Effect", Misc.getTextColor(), new Color(150, 100, 50, 255), Alignment.MID, 10f);
        
        tooltip.addPara("Increases the effect of terrain by %s  [Max.: %s]",
                opad,
                negative,
                getAbsPercentStringForTooltip(NEG_TERRAIN_EFFECT_INCREASE, effectMult),
                getAbsPercentStringForTooltip(NEG_TERRAIN_EFFECT_INCREASE));

        tooltip.addPara("Decreases energy rate of fire by %s  [Max.: %s]",
                spad,
                negative,
                getAbsPercentStringForTooltip(NEG_ROF_DECREASE_PERCENT, effectMult),
                getAbsPercentStringForTooltip(NEG_ROF_DECREASE_PERCENT));

        tooltip.addPara("Decreases ballistic rate of fire by %s  [Max.: %s]",
                spad,
                negative,
                getAbsPercentStringForTooltip(NEG_ROF_DECREASE_PERCENT, effectMult),
                getAbsPercentStringForTooltip(NEG_ROF_DECREASE_PERCENT));
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

        tooltip.addPara("Increases trade tariffs by by %s  [Max.: %s]",
                spad,
                bad,
                getAbsPercentStringForTooltip(WITHDRAWAL_TARIFF_INCREASE_PERCENT, effectMult),
                getAbsPercentStringForTooltip(WITHDRAWAL_TARIFF_INCREASE_PERCENT));

        tooltip.addPara("Increases Fighter replacement rate decay by %s  [Max.: %s]",
                spad,
                bad,
                getAbsPercentStringForTooltip(WITHDRAWAL_REPLACEMENT_RATE_RED_PERCENT, effectMult),
                getAbsPercentStringForTooltip(WITHDRAWAL_REPLACEMENT_RATE_RED_PERCENT));

        if(effectMult > 0.6f) tooltip.addPara("Increases fighter damage taken by %s  [Max.: %s]",
                spad,
                bad,
                getAbsPercentStringForTooltip(WITHDRAWAL_DAMAGE_TO_FIGHTERS_INCREASE_PERCENT, effectMult),
                getAbsPercentStringForTooltip(WITHDRAWAL_DAMAGE_TO_FIGHTERS_INCREASE_PERCENT));
    }

}
