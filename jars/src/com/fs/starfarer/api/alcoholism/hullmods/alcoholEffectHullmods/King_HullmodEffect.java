package com.fs.starfarer.api.alcoholism.hullmods.alcoholEffectHullmods;

import com.fs.starfarer.api.alcoholism.hullmods.BaseAlcoholHullmodEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;

public class King_HullmodEffect extends BaseAlcoholHullmodEffect {

    private static final float MAX_CR_READINESS_BONUS_PERCENT = 25f;
    private static final float MAX_CR_RECOVERY_BONUS_PERCENT = 40f;
    private static final float MAX_CREW_LOSS_REDUCTION_PERCENT = -20f;
    private static final float MAX_RECOVERY_CHANCE_PERCENT = 50f;
    private static final float MAX_PPT_TIME_INCREASE = 30f;

    private static final float NEG_ACCURACY_LOSS_PERCENT = -10f;
    private static final float NEG_SHIP_UPKEEP_INCREASE_PERCENT = 10f;

    private static final float WITHDRAWAL_MAX_CR_REDUCTION = -20f;
    private static final float WITHDRAWAL_CR_RECOVERY_RED_PERCENT = -20f;
    private static final float WITHDRAWAL_CREW_LOSS_INCREASE_PERCENT = 20f;
    private static final float WITHDRAWAL_SHIP_LOSS_INCREASE_PERCENT = 20f;

    @Override
    public void applyPositives(MutableShipStatsAPI stats, float effectMult, String id) {
        //Increase max CR by 25%
        //increase peak CR by 30s
        //Increase CR Recovery by 50%
        //Reduce crew losses by 20%
        //Ships more likely to be recoverable

        stats.getMaxCombatReadiness().modifyMult(id, getPercentToCorrectedMult(MAX_CR_READINESS_BONUS_PERCENT), getDesc());
        stats.getPeakCRDuration().modifyFlat(id, MAX_PPT_TIME_INCREASE * effectMult, getDesc());
        stats.getBaseCRRecoveryRatePercentPerDay().modifyMult(id, getPercentToCorrectedMult(MAX_CR_RECOVERY_BONUS_PERCENT), getDesc());
        stats.getCrewLossMult().modifyMult(id, getPercentToCorrectedMult(MAX_CREW_LOSS_REDUCTION_PERCENT), getDesc());

        stats.getDynamic().getMod(Stats.INDIVIDUAL_SHIP_RECOVERY_MOD).modifyMult(id, getPercentToCorrectedMult(MAX_RECOVERY_CHANCE_PERCENT), getDesc());
        stats.getBreakProb().modifyMult(id, getPercentToCorrectedMult(-MAX_RECOVERY_CHANCE_PERCENT), getDesc());
    }

    @Override
    public void applyNegatives(MutableShipStatsAPI stats, float effectMult, String id) {
        //Reduce autofire accuracy
        //Slightly increase ship upkeep

        stats.getAutofireAimAccuracy().modifyMult(id, getPercentToCorrectedMult(NEG_ACCURACY_LOSS_PERCENT), getDesc());
        stats.getSuppliesPerMonth().modifyMult(id, getPercentToCorrectedMult(NEG_SHIP_UPKEEP_INCREASE_PERCENT), getDesc());
    }

    @Override
    public void applyWithdrawal(MutableShipStatsAPI stats, float effectMult, String id) {
        //Lower max CR
        //Lower CR recovery
        //Increase crew loss
        //Ships more likely to be lost

        stats.getMaxCombatReadiness().modifyMult(id, getPercentToCorrectedMult(WITHDRAWAL_MAX_CR_REDUCTION), getDesc());
        stats.getBaseCRRecoveryRatePercentPerDay().modifyMult(id, getPercentToCorrectedMult(WITHDRAWAL_CR_RECOVERY_RED_PERCENT), getDesc());
        stats.getCrewLossMult().modifyMult(id, getPercentToCorrectedMult(WITHDRAWAL_CREW_LOSS_INCREASE_PERCENT), getDesc());

        stats.getDynamic().getMod(Stats.INDIVIDUAL_SHIP_RECOVERY_MOD).modifyMult(id, getPercentToCorrectedMult(-WITHDRAWAL_SHIP_LOSS_INCREASE_PERCENT), getDesc());
        stats.getBreakProb().modifyMult(id, getPercentToCorrectedMult(WITHDRAWAL_SHIP_LOSS_INCREASE_PERCENT), getDesc());
    }

    @Override
    public void addPositiveEffectTooltip(TooltipMakerAPI tooltip, float effectMult) {
        float opad = 10f;
        float spad = 3f;
        Color positive = Misc.getPositiveHighlightColor();
        Color neutral = Misc.getGrayColor();
        Color negative = Misc.getNegativeHighlightColor();

        tooltip.addSectionHeading("Positive Effect", Misc.getTextColor(), new Color(50, 100, 50, 255), Alignment.MID, 10f);
                
        tooltip.addPara("Increases max. combat readiness by %s  [Max.: %s]",
                opad,
                positive,
                getAbsPercentStringForTooltip(MAX_CR_READINESS_BONUS_PERCENT, effectMult),
                getAbsPercentStringForTooltip(MAX_CR_READINESS_BONUS_PERCENT));

        tooltip.addPara("Increases peak performance time by %s  [Max.: %s]",
                spad,
                positive,
                (int) Math.ceil(MAX_PPT_TIME_INCREASE * effectMult) + "s",
                (int) MAX_PPT_TIME_INCREASE + "s");

        tooltip.addPara("Increases combat readiness recovery by %s  [Max.: %s]",
                spad,
                positive,
                getAbsPercentStringForTooltip(MAX_CR_RECOVERY_BONUS_PERCENT, effectMult),
                getAbsPercentStringForTooltip(MAX_CR_RECOVERY_BONUS_PERCENT));

        tooltip.addPara("Ship recovery chance increased by %s  [Max.: %s]",
                spad,
                positive,
                getAbsPercentStringForTooltip(MAX_RECOVERY_CHANCE_PERCENT, effectMult),
                getAbsPercentStringForTooltip(MAX_RECOVERY_CHANCE_PERCENT));

        tooltip.addPara("Decreases crew losses by %s  [Max.: %s]",
                spad,
                positive,
                getAbsPercentStringForTooltip(MAX_CREW_LOSS_REDUCTION_PERCENT, effectMult),
                getAbsPercentStringForTooltip(MAX_CREW_LOSS_REDUCTION_PERCENT));

    }

    @Override
    public void addNegativeEffectTooltip(TooltipMakerAPI tooltip, float effectMult) {
        float opad = 10f;
        float spad = 3f;
        Color positive = Misc.getPositiveHighlightColor();
        Color neutral = Misc.getGrayColor();
        Color negative = Misc.getNegativeHighlightColor();

        tooltip.addSectionHeading("Negative Effect", Misc.getTextColor(), new Color(150, 100, 50, 255), Alignment.MID, 10f);
        
        tooltip.addPara("Reduces auto-fire accuracy by %s  [Max.: %s]",
                opad,
                negative,
                getAbsPercentStringForTooltip(NEG_ACCURACY_LOSS_PERCENT, effectMult),
                getAbsPercentStringForTooltip(NEG_ACCURACY_LOSS_PERCENT));

        tooltip.addPara("Increases upkeep by %s  [Max.: %s]",
                spad,
                negative,
                getAbsPercentStringForTooltip(NEG_SHIP_UPKEEP_INCREASE_PERCENT, effectMult),
                getAbsPercentStringForTooltip(NEG_SHIP_UPKEEP_INCREASE_PERCENT));
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

        tooltip.addPara("Lowers combat readiness recovery by %s  [Max.: %s]",
                spad,
                bad,
                getAbsPercentStringForTooltip(WITHDRAWAL_CR_RECOVERY_RED_PERCENT, effectMult),
                getAbsPercentStringForTooltip(WITHDRAWAL_CR_RECOVERY_RED_PERCENT));

        tooltip.addPara("Increases crew losses by %s  [Max.: %s]",
                spad,
                bad,
                getAbsPercentStringForTooltip(WITHDRAWAL_CREW_LOSS_INCREASE_PERCENT, effectMult),
                getAbsPercentStringForTooltip(WITHDRAWAL_CREW_LOSS_INCREASE_PERCENT));

        tooltip.addPara("Ship recovery chance decreased by %s  [Max.: %s]",
                spad,
                bad,
                getAbsPercentStringForTooltip(WITHDRAWAL_SHIP_LOSS_INCREASE_PERCENT, effectMult),
                getAbsPercentStringForTooltip(WITHDRAWAL_SHIP_LOSS_INCREASE_PERCENT));
    }
}
