package com.fs.starfarer.api.alcoholism_re.hullmods.alcoholEffectHullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism_re.ModPlugin;
import com.fs.starfarer.api.alcoholism_re.hullmods.BaseAlcoholHullmodEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;

public class Custom_HullmodEffect extends BaseAlcoholHullmodEffect {

    public static final float MAX_MISSILE_SPEED_INCREASE = 50f;
    public static final float MAX_PROJ_SPEED_INCREASE = 50f;
    public static final float MAX_FLUX_PERCENT_FOR_BOOST = 30f;

    public static final float NEG_MAX_FLUX_CAP_REDUCTION = -10f;
    public static final float NEG_MAX_SENSOR_PROFILE_INCREASE = 10f;

    public static final float WITHDRAWAL_MAX_CR_REDUCTION = -20f;
    public static final float WITHDRAWAL_MAX_CR_DEGRADATION_INCREASE_PERCENT = 20f;

    @Override
    public void applyPositives(MutableShipStatsAPI stats, float effectMult, String id) {
        stats.getProjectileSpeedMult().modifyMult(id, getPercentToCorrectedMult(MAX_PROJ_SPEED_INCREASE), getDesc());
        stats.getMissileMaxSpeedBonus().modifyMult(id, getPercentToCorrectedMult(MAX_MISSILE_SPEED_INCREASE), getDesc());
        stats.getZeroFluxMinimumFluxLevel().modifyFlat(id, getPercentToCorrectedMult(MAX_FLUX_PERCENT_FOR_BOOST), getDesc());
    }

    @Override
    public void applyNegatives(MutableShipStatsAPI stats, float effectMult, String id) {
        //Reduces effectiveness of flux caps by 30%
        //Increase sensor profile
        stats.getFluxCapacity().modifyMult(id, getPercentToCorrectedMult(NEG_MAX_FLUX_CAP_REDUCTION), getDesc());
        stats.getSensorProfile().modifyMult(id, getPercentToCorrectedMult(NEG_MAX_SENSOR_PROFILE_INCREASE), getDesc());
    }

    @Override
    public void applyWithdrawal(MutableShipStatsAPI stats, float effectMult, String id) {
        //Lower max CR
        //Increase CR deg after max
        //Makes all ships without officer behave as CAUTIOUS

        stats.getMaxCombatReadiness().modifyMult(id, getPercentToCorrectedMult(WITHDRAWAL_MAX_CR_REDUCTION), getDesc());
        stats.getCRLossPerSecondPercent().modifyMult(id, getPercentToCorrectedMult(WITHDRAWAL_MAX_CR_DEGRADATION_INCREASE_PERCENT), getDesc());
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        super.advanceInCombat(ship, amount);

        if(ship.getCaptain() != null
                || ship.getShipAI() == null
                || ship.getShipAI().getConfig().personalityOverride != null) return;

        ModPlugin.log("overriding ship personality " + ship.getVariant().getFullDesignationWithHullName());

        if(isWithdrawal()){
            ship.getShipAI().getConfig().personalityOverride = Personalities.CAUTIOUS;;
        } else if(getAlcohol().getAddictionStatus().isConsuming()){
            if(Global.getSector().getPlayerFaction().getDoctrine().getAggression() < 4) ship.getShipAI().getConfig().personalityOverride = Personalities.AGGRESSIVE;
        };
    }

    @Override
    public void addPositiveEffectTooltip(TooltipMakerAPI tooltip, float effectMult) {
        float opad = 10f;
        float spad = 3f;
        Color positive = Misc.getPositiveHighlightColor();
        Color neutral = Misc.getGrayColor();
        Color negative = Misc.getNegativeHighlightColor();

        tooltip.addSectionHeading("Positive Effect", Misc.getTextColor(), new Color(50, 100, 50, 255), Alignment.MID, 10f);

        tooltip.addPara("Increases missile speed by %s  [Max.: %s]",
                opad,
                positive,
                getAbsPercentStringForTooltip(MAX_MISSILE_SPEED_INCREASE, effectMult),
                getAbsPercentStringForTooltip(MAX_MISSILE_SPEED_INCREASE));

        tooltip.addPara("Increases projectile speed by %s  [Max.: %s]",
                spad,
                positive,
                getAbsPercentStringForTooltip(MAX_PROJ_SPEED_INCREASE, effectMult),
                getAbsPercentStringForTooltip(MAX_PROJ_SPEED_INCREASE));

        tooltip.addPara("Zero-Flux boost up to flux level: %s  [Max.: %s]",
                spad,
                positive,
                getAbsPercentStringForTooltip(MAX_FLUX_PERCENT_FOR_BOOST, effectMult),
                getAbsPercentStringForTooltip(MAX_FLUX_PERCENT_FOR_BOOST));

        tooltip.addPara("Ships without officers behave as %s",
                spad,
                positive,
                Misc.ucFirst(Personalities.AGGRESSIVE));
    }

    @Override
    public void addNegativeEffectTooltip(TooltipMakerAPI tooltip, float effectMult) {
        float opad = 10f;
        float spad = 3f;
        Color positive = Misc.getPositiveHighlightColor();
        Color neutral = Misc.getGrayColor();
        Color negative = Misc.getNegativeHighlightColor();

        tooltip.addSectionHeading("Negative Effect", Misc.getTextColor(), new Color(150, 100, 50, 255), Alignment.MID, 10f);

        tooltip.addPara("Reduces flux capacity by %s  [Max.: %s]",
                opad,
                negative,
                getAbsPercentStringForTooltip(NEG_MAX_FLUX_CAP_REDUCTION, effectMult),
                getAbsPercentStringForTooltip(NEG_MAX_FLUX_CAP_REDUCTION));

        tooltip.addPara("Increases sensor profile by %s  [Max.: %s]",
                spad,
                negative,
                getAbsPercentStringForTooltip(NEG_MAX_SENSOR_PROFILE_INCREASE, effectMult),
                getAbsPercentStringForTooltip(NEG_MAX_SENSOR_PROFILE_INCREASE));
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

        tooltip.addPara("Increases combat readiness decay by %s  [Max.: %s]",
                spad,
                bad,
                getAbsPercentStringForTooltip(WITHDRAWAL_MAX_CR_DEGRADATION_INCREASE_PERCENT, effectMult),
                getAbsPercentStringForTooltip(WITHDRAWAL_MAX_CR_DEGRADATION_INCREASE_PERCENT));

        tooltip.addPara("Ships without officers behave as %s",
                spad,
                bad,
                Misc.ucFirst(Personalities.CAUTIOUS));
    }
}
