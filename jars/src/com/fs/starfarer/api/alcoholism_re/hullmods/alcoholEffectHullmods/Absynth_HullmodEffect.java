package com.fs.starfarer.api.alcoholism_re.hullmods.alcoholEffectHullmods;

import com.fs.starfarer.api.alcoholism_re.hullmods.BaseAlcoholHullmodEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;

public class Absynth_HullmodEffect extends BaseAlcoholHullmodEffect {

    public static final float MAX_OP_TO_COMPENSATE_PERCENT = -20f;

    /**
     * Malfunction check made once per second on average. Range is 0 (no chance) to 1 (100% chance).
     */
    private static final float NEG_SMALL_MALFUNCTION_PROB = 0.002f; //~10% chance per minute
    private static final float WITH_MALFUNCTION_PROB = 0.007f; //~ 35% chance per minute
    private static final float WITH_MAX_CR_REDUCTION = -20f;

    @Override
    public boolean affectsOPCosts() {
        return true;
    }

    @Override
    public void applyPositives(MutableShipStatsAPI stats, float effectMult, String id) {
        //Increase available OP by (max) 20%
        stats.getDynamic().getMod(Stats.LARGE_BALLISTIC_MOD).modifyMult(id, getPercentToCorrectedMult(MAX_OP_TO_COMPENSATE_PERCENT));
        stats.getDynamic().getMod(Stats.MEDIUM_BALLISTIC_MOD).modifyMult(id, getPercentToCorrectedMult(MAX_OP_TO_COMPENSATE_PERCENT));
        stats.getDynamic().getMod(Stats.SMALL_BALLISTIC_MOD).modifyMult(id, getPercentToCorrectedMult(MAX_OP_TO_COMPENSATE_PERCENT));

        stats.getDynamic().getMod(Stats.LARGE_ENERGY_MOD).modifyMult(id, getPercentToCorrectedMult(MAX_OP_TO_COMPENSATE_PERCENT));
        stats.getDynamic().getMod(Stats.MEDIUM_ENERGY_MOD).modifyMult(id, getPercentToCorrectedMult(MAX_OP_TO_COMPENSATE_PERCENT));
        stats.getDynamic().getMod(Stats.SMALL_ENERGY_MOD).modifyMult(id, getPercentToCorrectedMult(MAX_OP_TO_COMPENSATE_PERCENT));

        stats.getDynamic().getMod(Stats.LARGE_BEAM_MOD).modifyMult(id, getPercentToCorrectedMult(MAX_OP_TO_COMPENSATE_PERCENT));
        stats.getDynamic().getMod(Stats.MEDIUM_BEAM_MOD).modifyMult(id, getPercentToCorrectedMult(MAX_OP_TO_COMPENSATE_PERCENT));
        stats.getDynamic().getMod(Stats.SMALL_BEAM_MOD).modifyMult(id, getPercentToCorrectedMult(MAX_OP_TO_COMPENSATE_PERCENT));

        stats.getDynamic().getMod(Stats.LARGE_MISSILE_MOD).modifyMult(id, getPercentToCorrectedMult(MAX_OP_TO_COMPENSATE_PERCENT));
        stats.getDynamic().getMod(Stats.MEDIUM_MISSILE_MOD).modifyMult(id, getPercentToCorrectedMult(MAX_OP_TO_COMPENSATE_PERCENT));
        stats.getDynamic().getMod(Stats.SMALL_MISSILE_MOD).modifyMult(id, getPercentToCorrectedMult(MAX_OP_TO_COMPENSATE_PERCENT));

        stats.getDynamic().getMod(Stats.LARGE_PD_MOD).modifyMult(id, getPercentToCorrectedMult(MAX_OP_TO_COMPENSATE_PERCENT));
        stats.getDynamic().getMod(Stats.MEDIUM_PD_MOD).modifyMult(id, getPercentToCorrectedMult(MAX_OP_TO_COMPENSATE_PERCENT));
        stats.getDynamic().getMod(Stats.SMALL_PD_MOD).modifyMult(id, getPercentToCorrectedMult(MAX_OP_TO_COMPENSATE_PERCENT));

        stats.getDynamic().getMod(Stats.FIGHTER_COST_MOD).modifyMult(id, getPercentToCorrectedMult(MAX_OP_TO_COMPENSATE_PERCENT));
        stats.getDynamic().getMod(Stats.BOMBER_COST_MOD).modifyMult(id, getPercentToCorrectedMult(MAX_OP_TO_COMPENSATE_PERCENT));
        stats.getDynamic().getMod(Stats.INTERCEPTOR_COST_MOD).modifyMult(id, getPercentToCorrectedMult(MAX_OP_TO_COMPENSATE_PERCENT));
        stats.getDynamic().getMod(Stats.SUPPORT_COST_MOD).modifyMult(id, getPercentToCorrectedMult(MAX_OP_TO_COMPENSATE_PERCENT));
    }

    @Override
    public void applyNegatives(MutableShipStatsAPI stats, float effectMult, String id) {
        //Small Non-critical Malfunction chance
        stats.getWeaponMalfunctionChance().modifyFlat(id, NEG_SMALL_MALFUNCTION_PROB * effectMult);
    }

    @Override
    public void applyWithdrawal(MutableShipStatsAPI stats, float effectMult, String id) {
        //Critical malfunction chance
        //Lower max CR

        stats.getWeaponMalfunctionChance().modifyFlat(id, WITH_MALFUNCTION_PROB * effectMult);
        stats.getEngineMalfunctionChance().modifyFlat(id, NEG_SMALL_MALFUNCTION_PROB * effectMult); //smaller malfunction on engine cause it's the more unfun one
        stats.getMaxCombatReadiness().modifyMult(id, getPercentToCorrectedMult(WITH_MAX_CR_REDUCTION), getDesc());
    }

    @Override
    public void addPositiveEffectTooltip(TooltipMakerAPI tooltip, float effectMult) {
        float opad = 10f;
        float spad = 3f;
        Color positive = Misc.getPositiveHighlightColor();
        Color neutral = Misc.getGrayColor();
        Color negative = Misc.getNegativeHighlightColor();

        tooltip.addSectionHeading("Positive Effect", Misc.getTextColor(), new Color(50, 100, 50, 255), Alignment.MID, 10f);
        
        tooltip.addPara("Reduces weapon OP cost by %s  [Max.: %s]",
                opad,
                positive,
                getAbsPercentStringForTooltip(MAX_OP_TO_COMPENSATE_PERCENT, effectMult),
                getAbsPercentStringForTooltip(MAX_OP_TO_COMPENSATE_PERCENT));

        tooltip.addPara("Reduces fighter OP cost by %s  [Max.: %s]",
                spad,
                positive,
                getAbsPercentStringForTooltip(MAX_OP_TO_COMPENSATE_PERCENT, effectMult),
                getAbsPercentStringForTooltip(MAX_OP_TO_COMPENSATE_PERCENT));
    }

    @Override
    public void addNegativeEffectTooltip(TooltipMakerAPI tooltip, float effectMult) {
        float opad = 10f;
        float spad = 3f;
        Color positive = Misc.getPositiveHighlightColor();
        Color neutral = Misc.getGrayColor();
        Color negative = Misc.getNegativeHighlightColor();
        
        tooltip.addSectionHeading("Negative Effect", Misc.getTextColor(), new Color(150, 100, 50, 255), Alignment.MID, 10f);
        
        tooltip.addPara("Small chance to cause %s",
                opad,
                negative,
                "weapon malfunctions");
    }

    @Override
    public void addWithdrawalEffectTooltip(TooltipMakerAPI tooltip, float effectMult) {
        float opad = 10f;
        float spad = 3f;
        Color positive = Misc.getPositiveHighlightColor();
        Color bad = Color.red;
        Color negative = Misc.getNegativeHighlightColor();

        tooltip.addSectionHeading("Withdrawal Effect", Misc.getTextColor(), new Color(150, 50, 50, 255), Alignment.MID, 10f);
        
        tooltip.addPara("Lowers Max. combat readiness by %s  [Max.: %s]",
                opad,
                bad,
                getAbsPercentStringForTooltip(WITH_MAX_CR_REDUCTION, effectMult),
                getAbsPercentStringForTooltip(WITH_MAX_CR_REDUCTION));

        tooltip.addPara("High chance to cause %s",
                spad,
                bad,
                "weapon malfunctions");

        tooltip.addPara("High chance to cause %s",
                spad,
                bad,
                "engine malfunctions");
    }
}
