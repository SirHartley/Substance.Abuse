package com.fs.starfarer.api.alcoholism_re.hullmods.alcoholEffectHullmods;

import com.fs.starfarer.api.alcoholism_re.hullmods.BaseAlcoholHullmodEffect;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.combat.DefenseUtils;

import java.awt.*;

public class Stout_HullmodEffect extends BaseAlcoholHullmodEffect {

    public static final float MAX_REGENERATION_PER_SEC_PERCENT = 1f;
    public static final float MAX_WEAPON_RANGE_BONUS_PERCENT = 20f;

    public static final float NEG_WEAP_TURN_RATE_DECREASE_PERCENT = -10f;
    public static final float NEG_WEAP_DAMAGE_TAKEN = 10f;

    public static final float WITHDRAWAL_MAX_CR_REDUCTION = -20f;
    public static final float WITHDRAWAL_DAMAGE_TAKEN_INCREASE = 15f;
    public static final float WITHDRAWAL_WEAPON_FLUX_COST_INCREASE = 15f;

    @Override
    public void applyPositives(MutableShipStatsAPI stats, float effectMult, String id) {
        //Increase weapon Range
        stats.getWeaponRangeThreshold().modifyMult(id, getPercentToCorrectedMult(MAX_WEAPON_RANGE_BONUS_PERCENT), getDesc());
        stats.getWeaponRangeMultPastThreshold().modifyMult(id, getPercentToCorrectedMult(MAX_WEAPON_RANGE_BONUS_PERCENT), getDesc());
    }

    @Override
    public void applyNegatives(MutableShipStatsAPI stats, float effectMult, String id) {
        //Slightly lower weapon turn rate, lower accuracy
        stats.getWeaponTurnRateBonus().modifyMult(id, getPercentToCorrectedMult(NEG_WEAP_TURN_RATE_DECREASE_PERCENT), getDesc());
        stats.getBeamWeaponTurnRateBonus().modifyMult(id, getPercentToCorrectedMult(NEG_WEAP_TURN_RATE_DECREASE_PERCENT), getDesc());
        stats.getWeaponDamageTakenMult().modifyMult(id, getPercentToCorrectedMult(NEG_WEAP_DAMAGE_TAKEN), getDesc());
    }

    @Override
    public void applyWithdrawal(MutableShipStatsAPI stats, float effectMult, String id) {
        //Lower max CR,
        //increase damage to armor and hull
        //increase weapon flux costs

        stats.getMaxCombatReadiness().modifyMult(id, getPercentToCorrectedMult(WITHDRAWAL_MAX_CR_REDUCTION), getDesc());

        stats.getArmorDamageTakenMult().modifyMult(id, getPercentToCorrectedMult(WITHDRAWAL_DAMAGE_TAKEN_INCREASE), getDesc());
        stats.getHullDamageTakenMult().modifyMult(id, getPercentToCorrectedMult(WITHDRAWAL_DAMAGE_TAKEN_INCREASE), getDesc());

        stats.getBallisticWeaponFluxCostMod().modifyMult(id, getPercentToCorrectedMult(WITHDRAWAL_WEAPON_FLUX_COST_INCREASE), getDesc());
        stats.getBeamWeaponFluxCostMult().modifyMult(id, getPercentToCorrectedMult(WITHDRAWAL_WEAPON_FLUX_COST_INCREASE), getDesc());
        stats.getEnergyWeaponFluxCostMod().modifyMult(id, getPercentToCorrectedMult(WITHDRAWAL_WEAPON_FLUX_COST_INCREASE), getDesc());
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        super.applyEffectsAfterShipCreation(ship, id);

        ship.removeListenerOfClass(AlcoholArmorRegen.class);
        ship.addListener(new AlcoholArmorRegen(ship, getAlcohol().getAddictionStatus().getAddictionValue()));
    }

    @Override
    public void addPositiveEffectTooltip(TooltipMakerAPI tooltip, float effectMult) {
        float opad = 10f;
        float spad = 3f;
        Color positive = Misc.getPositiveHighlightColor();
        Color neutral = Misc.getGrayColor();
        Color negative = Misc.getNegativeHighlightColor();

        tooltip.addSectionHeading("Positive Effect", Misc.getTextColor(), new Color(50, 100, 50, 255), Alignment.MID, 10f);

        tooltip.addPara("Adds %s armor regeneration per second  [Max.: %s]",
                opad,
                positive,
                Misc.getRoundedValueMaxOneAfterDecimal(Math.max(MAX_REGENERATION_PER_SEC_PERCENT * effectMult, 0.01f)) + "%",
                getAbsPercentStringForTooltip(MAX_REGENERATION_PER_SEC_PERCENT));

        tooltip.addPara("Increases weapon range by %s  [Max.: %s]",
                spad,
                positive,
                getAbsPercentStringForTooltip(MAX_WEAPON_RANGE_BONUS_PERCENT, effectMult),
                getAbsPercentStringForTooltip(MAX_WEAPON_RANGE_BONUS_PERCENT));
    }

    @Override
    public void addNegativeEffectTooltip(TooltipMakerAPI tooltip, float effectMult) {
        float opad = 10f;
        float spad = 3f;
        Color positive = Misc.getPositiveHighlightColor();
        Color neutral = Misc.getGrayColor();
        Color negative = Misc.getNegativeHighlightColor();

        tooltip.addSectionHeading("Negative Effect", Misc.getTextColor(), new Color(150, 100, 50, 255), Alignment.MID, 10f);

        tooltip.addPara("Reduces weapon turn rate by %s  [Max.: %s]",
                opad,
                negative,
                getAbsPercentStringForTooltip(NEG_WEAP_TURN_RATE_DECREASE_PERCENT, effectMult),
                getAbsPercentStringForTooltip(NEG_WEAP_TURN_RATE_DECREASE_PERCENT));

        tooltip.addPara("Increases weapon damage taken by %s  [Max.: %s]",
                spad,
                negative,
                getAbsPercentStringForTooltip(NEG_WEAP_DAMAGE_TAKEN, effectMult),
                getAbsPercentStringForTooltip(NEG_WEAP_DAMAGE_TAKEN));
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

        tooltip.addPara("Increases armor and hull damage taken by %s  [Max.: %s]",
                spad,
                bad,
                getAbsPercentStringForTooltip(WITHDRAWAL_DAMAGE_TAKEN_INCREASE, effectMult),
                getAbsPercentStringForTooltip(WITHDRAWAL_DAMAGE_TAKEN_INCREASE));

        tooltip.addPara("Increases weapon flux cost to fire by %s  [Max.: %s]",
                spad,
                bad,
                getAbsPercentStringForTooltip(WITHDRAWAL_WEAPON_FLUX_COST_INCREASE, effectMult),
                getAbsPercentStringForTooltip(WITHDRAWAL_WEAPON_FLUX_COST_INCREASE));
    }

    public static class AlcoholArmorRegen implements AdvanceableListener {
        protected ShipAPI ship;
        protected float fraction;

        public AlcoholArmorRegen(ShipAPI ship, float fraction) {
            this.ship = ship;
            this.fraction = fraction;
        }

        public void advance(float amount) {
            if (!DefenseUtils.hasArmorDamage(ship)) return;
            if (ship.isHulk()) return;

            ArmorGridAPI armorGrid = ship.getArmorGrid();
            final float[][] grid = armorGrid.getGrid();
            final float max = armorGrid.getMaxArmorInCell();

            float repairAmount = max * (MAX_REGENERATION_PER_SEC_PERCENT / 100) * fraction * amount;

            // Iterate through all armor cells and find any that aren't at max
            for (int x = 0; x < grid.length; x++) {
                for (int y = 0; y < grid[0].length; y++) {
                    if (grid[x][y] < max) {
                        float regen = grid[x][y] + repairAmount;
                        armorGrid.setArmorValue(x, y, regen);
                    }
                }
            }
        }
    }
}
