package com.fs.starfarer.api.alcoholism.hullmods.alcoholEffectHullmods;

import com.fs.starfarer.api.alcoholism.hullmods.BaseAlcoholHullmodEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;

public class Sunshine_HullmodEffect extends BaseAlcoholHullmodEffect {

    private static final float MAX_HARD_FLUX_DISSIPATION_PERCENT = 15f;
    private static final float MAX_FUEL_COST_PER_LY_RED_PERCENT = -25f;
    private static final float MAX_FLUX_DURING_PHASE_RED_PERCENT = -25f;

    private static final float NEG_OVERLOAD_DURATION_PERCENT = 10f;
    private static final float NEG_HULL_DAMAGE_PERCENT = 10f;

    public static final float WITHDRAWAL_MAX_CR_REDUCTION = -20f;
    public static final float WITHDRAWAL_SHIELD_DAMAGE_INCREASE = 20f;
    public static final float WITHDRAWAL_PHASE_COOLDOWN_INCREASE = 30f;

    @Override
    public void applyPositives(MutableShipStatsAPI stats, float effectMult, String id) {
        //lower fuel cost per LY
        //enable 15% hardflux dissipation through shields
        //decreases phase flux buildup

        stats.getHardFluxDissipationFraction().modifyFlat(id, (float)(MAX_HARD_FLUX_DISSIPATION_PERCENT * effectMult) /100, getDesc());
        stats.getFuelUseMod().modifyMult(id, getPercentToCorrectedMult(MAX_FUEL_COST_PER_LY_RED_PERCENT), getDesc());
        stats.getPhaseCloakUpkeepCostBonus().modifyMult(id, getPercentToCorrectedMult(MAX_FLUX_DURING_PHASE_RED_PERCENT), getDesc());
    }

    @Override
    public void applyNegatives(MutableShipStatsAPI stats, float effectMult, String id) {
        //Increase overload duration
        //Increase hull damage taken (if possible)

        stats.getHullDamageTakenMult().modifyMult(id, getPercentToCorrectedMult(NEG_HULL_DAMAGE_PERCENT), getDesc());
        stats.getOverloadTimeMod().modifyMult(id, getPercentToCorrectedMult(NEG_OVERLOAD_DURATION_PERCENT), getDesc());
    }

    @Override
    public void applyWithdrawal(MutableShipStatsAPI stats, float effectMult, String id) {
        //Lower max CR
        //Increase shield damage
        //Increase phase cooldown

        stats.getMaxCombatReadiness().modifyMult(id, getPercentToCorrectedMult(WITHDRAWAL_MAX_CR_REDUCTION), getDesc());
        stats.getShieldDamageTakenMult().modifyMult(id, getPercentToCorrectedMult(WITHDRAWAL_SHIELD_DAMAGE_INCREASE), getDesc());
        stats.getPhaseCloakCooldownBonus().modifyMult(id, getPercentToCorrectedMult(WITHDRAWAL_PHASE_COOLDOWN_INCREASE), getDesc());
    }

    @Override
    public void addPositiveEffectTooltip(TooltipMakerAPI tooltip, float effectMult) {
        float opad = 10f;
        float spad = 3f;
        Color positive = Misc.getPositiveHighlightColor();
        Color neutral = Misc.getGrayColor();
        Color negative = Misc.getNegativeHighlightColor();

        tooltip.addSectionHeading("Positive Effect", Misc.getTextColor(), new Color(50, 100, 50, 255), Alignment.MID, 10f);
        
        tooltip.addPara("Lowers fuel use per LY by %s  [Max.: %s]",
                opad,
                positive,
                getAbsPercentStringForTooltip(MAX_FUEL_COST_PER_LY_RED_PERCENT, effectMult),
                getAbsPercentStringForTooltip(MAX_FUEL_COST_PER_LY_RED_PERCENT));

        tooltip.addPara("Enables shielded hardflux dissipation: %s  [Max.: %s]",
                spad,
                positive,
                getAbsPercentStringForTooltip(MAX_HARD_FLUX_DISSIPATION_PERCENT, effectMult),
                getAbsPercentStringForTooltip(MAX_HARD_FLUX_DISSIPATION_PERCENT));

        tooltip.addPara("Decreases flux buildup during phase by %s  [Max.: %s]",
                spad,
                positive,
                getAbsPercentStringForTooltip(MAX_FLUX_DURING_PHASE_RED_PERCENT, effectMult),
                getAbsPercentStringForTooltip(MAX_FLUX_DURING_PHASE_RED_PERCENT));
    }

    @Override
    public void addNegativeEffectTooltip(TooltipMakerAPI tooltip, float effectMult) {
        float opad = 10f;
        float spad = 3f;
        Color positive = Misc.getPositiveHighlightColor();
        Color neutral = Misc.getGrayColor();
        Color negative = Misc.getNegativeHighlightColor();

        tooltip.addSectionHeading("Negative Effect", Misc.getTextColor(), new Color(150, 100, 50, 255), Alignment.MID, 10f);
        
        tooltip.addPara("Increases overload duration by %s  [Max.: %s]",
                opad,
                negative,
                getAbsPercentStringForTooltip(NEG_OVERLOAD_DURATION_PERCENT, effectMult),
                getAbsPercentStringForTooltip(NEG_OVERLOAD_DURATION_PERCENT));

        tooltip.addPara("Increases hull damage taken by %s  [Max.: %s]",
                spad,
                negative,
                getAbsPercentStringForTooltip(NEG_HULL_DAMAGE_PERCENT, effectMult),
                getAbsPercentStringForTooltip(NEG_HULL_DAMAGE_PERCENT));
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

        tooltip.addPara("Increases shield damage taken by %s  [Max.: %s]",
                spad,
                bad,
                getAbsPercentStringForTooltip(WITHDRAWAL_SHIELD_DAMAGE_INCREASE, effectMult),
                getAbsPercentStringForTooltip(WITHDRAWAL_SHIELD_DAMAGE_INCREASE));

        tooltip.addPara("Increases phase cloak cooldown by %s  [Max.: %s]",
                spad,
                bad,
                getAbsPercentStringForTooltip(WITHDRAWAL_PHASE_COOLDOWN_INCREASE, effectMult),
                getAbsPercentStringForTooltip(WITHDRAWAL_PHASE_COOLDOWN_INCREASE));

    }
}
