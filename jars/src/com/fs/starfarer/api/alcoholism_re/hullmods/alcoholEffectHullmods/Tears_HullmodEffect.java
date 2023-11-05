package com.fs.starfarer.api.alcoholism_re.hullmods.alcoholEffectHullmods;

import com.fs.starfarer.api.alcoholism_re.hullmods.BaseAlcoholHullmodEffect;
import com.fs.starfarer.api.alcoholism_re.hullmods.campaignEffects.OfficerXPGainModifier;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;

public class Tears_HullmodEffect extends BaseAlcoholHullmodEffect {

    public static final float MAX_OFFICER_XP_INCREASE = 30f;

    public static final float MAX_MISSILE_AMMO_INCREASE = 30f;
    public static final float MAX_MISSILE_ROF_INCREASE = 30f;
    public static final float MAX_MISSILE_DAMAGE_INCREASE = 20f;

    public static final float NEG_MAX_WEAP_RECOIL_INCREASE = 10f;
    public static final float NEG_MAX_SHIP_TURN_RATE_DECREASE = -10f;
    public static final float NEG_MAX_SHIP_SPEED_DECREASE = -10f;

    public static final float WITHDRAWAL_MAX_CR_REDUCTION = -20f;
    public static final float WITHDRAWAL_OFFICER_XP_REDUCTION = -20f;
    public static final float WITHDRAWAL_VENT_RATE_REDUCTION = -20f;
    public static final float WITHDRAWAL_EDAMAGE_TAKEN_INCREASE = 20f;

    @Override
    public void applyPositives(MutableShipStatsAPI stats, float effectMult, String id) {
        //Increases officer XP gain

        stats.getMissileAmmoBonus().modifyMult(id, getPercentToCorrectedMult(MAX_MISSILE_AMMO_INCREASE), getDesc());
        stats.getMissileRoFMult().modifyMult(id, getPercentToCorrectedMult(MAX_MISSILE_ROF_INCREASE), getDesc());
        stats.getMissileWeaponDamageMult().modifyMult(id, getPercentToCorrectedMult(MAX_MISSILE_DAMAGE_INCREASE), getDesc());

        OfficerXPGainModifier.register();
    }

    @Override
    public void applyNegatives(MutableShipStatsAPI stats, float effectMult, String id) {
        //Reduces ship turn rate
        //Increases recoil
        //decreases speed

        stats.getMaxSpeed().modifyMult(id, getPercentToCorrectedMult(NEG_MAX_SHIP_SPEED_DECREASE), getDesc());
        stats.getRecoilPerShotMult().modifyMult(id, getPercentToCorrectedMult(NEG_MAX_WEAP_RECOIL_INCREASE), getDesc());

        if(effectMult > 0.5f) {
            stats.getMaxTurnRate().modifyMult(id, getPercentToCorrectedMult(NEG_MAX_SHIP_TURN_RATE_DECREASE), getDesc());
            stats.getTurnAcceleration().modifyMult(id, getPercentToCorrectedMult(NEG_MAX_SHIP_TURN_RATE_DECREASE), getDesc());
        }
    }

    @Override
    public void applyWithdrawal(MutableShipStatsAPI stats, float effectMult, String id) {
        //Lower max CR
        //Lowers officer XP gain rate (check if possible)
        //decrease vent speed
        //Increase effectiveness of energy and beam weapons against ships

        stats.getMaxCombatReadiness().modifyMult(id, getPercentToCorrectedMult(WITHDRAWAL_MAX_CR_REDUCTION), getDesc());

        stats.getEnergyDamageTakenMult().modifyMult(id, getPercentToCorrectedMult(WITHDRAWAL_EDAMAGE_TAKEN_INCREASE), getDesc());
        stats.getEmpDamageTakenMult().modifyMult(id, getPercentToCorrectedMult(WITHDRAWAL_EDAMAGE_TAKEN_INCREASE), getDesc());

        if(effectMult > 0.7f) stats.getVentRateMult().modifyMult(id, getPercentToCorrectedMult(WITHDRAWAL_VENT_RATE_REDUCTION), getDesc());
    }

    @Override
    public void addPositiveEffectTooltip(TooltipMakerAPI tooltip, float effectMult) {
        float opad = 10f;
        float spad = 3f;
        Color positive = Misc.getPositiveHighlightColor();
        Color neutral = Misc.getGrayColor();
        Color negative = Misc.getNegativeHighlightColor();

        tooltip.addSectionHeading("Positive Effect", Misc.getTextColor(), new Color(50, 100, 50, 255), Alignment.MID, 10f);
        
        tooltip.addPara("Increases officer XP gain by %s  [Max.: %s]",
                opad,
                positive,
                getAbsPercentStringForTooltip(MAX_MISSILE_ROF_INCREASE, effectMult),
                getAbsPercentStringForTooltip(MAX_MISSILE_ROF_INCREASE));

        tooltip.addPara("Increases missile ammo by %s  [Max.: %s]",
                spad,
                positive,
                getAbsPercentStringForTooltip(MAX_MISSILE_AMMO_INCREASE, effectMult),
                getAbsPercentStringForTooltip(MAX_MISSILE_AMMO_INCREASE));

        tooltip.addPara("Increases missile rate of fire by %s  [Max.: %s]",
                spad,
                positive,
                getAbsPercentStringForTooltip(MAX_MISSILE_ROF_INCREASE, effectMult),
                getAbsPercentStringForTooltip(MAX_MISSILE_ROF_INCREASE));

        tooltip.addPara("Increases missile damage by %s  [Max.: %s]",
                spad,
                positive,
                getAbsPercentStringForTooltip(MAX_MISSILE_DAMAGE_INCREASE, effectMult),
                getAbsPercentStringForTooltip(MAX_MISSILE_DAMAGE_INCREASE));
    }

    @Override
    public void addNegativeEffectTooltip(TooltipMakerAPI tooltip, float effectMult) {
        float opad = 10f;
        float spad = 3f;
        Color positive = Misc.getPositiveHighlightColor();
        Color neutral = Misc.getGrayColor();
        Color negative = Misc.getNegativeHighlightColor();
        
        tooltip.addSectionHeading("Negative Effect", Misc.getTextColor(), new Color(150, 100, 50, 255), Alignment.MID, 10f);
       
        
        tooltip.addPara("Increases weapon recoil by %s  [Max.: %s]",
                opad,
                negative,
                getAbsPercentStringForTooltip(NEG_MAX_WEAP_RECOIL_INCREASE, effectMult),
                getAbsPercentStringForTooltip(NEG_MAX_WEAP_RECOIL_INCREASE));

        tooltip.addPara("Lowers max. ship speed by %s  [Max.: %s]",
                spad,
                negative,
                getAbsPercentStringForTooltip(NEG_MAX_SHIP_SPEED_DECREASE, effectMult),
                getAbsPercentStringForTooltip(NEG_MAX_SHIP_SPEED_DECREASE));

        if(effectMult > 0.5f) tooltip.addPara("Lowers ship turn rate by %s  [Max.: %s]",
                spad,
                negative,
                getAbsPercentStringForTooltip(NEG_MAX_SHIP_TURN_RATE_DECREASE, effectMult),
                getAbsPercentStringForTooltip(NEG_MAX_SHIP_TURN_RATE_DECREASE));

    }

    @Override
    public void addWithdrawalEffectTooltip(TooltipMakerAPI tooltip, float effectMult) {
        float opad = 10f;
        float spad = 3f;
        Color positive = Misc.getPositiveHighlightColor();
        Color negative = Misc.getNegativeHighlightColor();
        Color bad = Color.red;

        tooltip.addSectionHeading("Withdrawal Effect", Misc.getTextColor(), new Color(150, 50, 50, 255), Alignment.MID, 10f);
        
        tooltip.addPara("Lowers Max. combat readiness by %s  [Max.: %s]",
                opad,
                bad,
                getAbsPercentStringForTooltip(WITHDRAWAL_MAX_CR_REDUCTION, effectMult),
                getAbsPercentStringForTooltip(WITHDRAWAL_MAX_CR_REDUCTION));

        tooltip.addPara("Increases energy and EMP damage taken by %s  [Max.: %s]",
                spad,
                bad,
                getAbsPercentStringForTooltip(WITHDRAWAL_EDAMAGE_TAKEN_INCREASE, effectMult),
                getAbsPercentStringForTooltip(WITHDRAWAL_EDAMAGE_TAKEN_INCREASE));

        if(effectMult > 0.7f) tooltip.addPara("Decreases vent rate by %s  [Max.: %s]",
                spad,
                bad,
                getAbsPercentStringForTooltip(WITHDRAWAL_VENT_RATE_REDUCTION, effectMult),
                getAbsPercentStringForTooltip(WITHDRAWAL_VENT_RATE_REDUCTION));

    }
}
