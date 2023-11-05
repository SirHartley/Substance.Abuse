package com.fs.starfarer.api.alcoholism_re.hullmods.alcoholEffectHullmods;

import com.fs.starfarer.api.alcoholism_re.hullmods.BaseAlcoholHullmodEffect;
import com.fs.starfarer.api.alcoholism_re.memory.AddictionStatus;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;

public class Tea_HullmodEffect extends BaseAlcoholHullmodEffect {

    public static final float POS_SHIELD_ARC_PERCENT = 20f;
    public static final float POS_SHIELD_UNFOLD_PERCENT = -20f;
    public static final float POS_SHIELD_TURN_PERCENT = 20f;
    public static final float POS_PHASE_FLUX_GEN_PERCENT = -20f;
    public static final float POS_PHASE_CR_DEG_DURING_PHASE_PERCENT = -50f;
    public static final float POS_PHASE_MIN_SPEED_FLUX_LEVEL_PERCENT = 50f;

    public static final float NEG_SHIELD_UPKEEP_PERCENT = 10f;
    public static final float NEG_CLOAK_CD_PERCENT = 10f;

    public static final float WITHDRAWAL_CR_DEG_OUT_OF_PHASE_PERCENT = 50f;
    public static final float WITHDRAWAL_SHIELD_ARC_PERCENT = -20f;
    public static final float WITHDRAWAL_SHIELD_DAMAGE_PERCENT = 10f;
    public static final float WITHDRAWAL_MAX_CR_REDUCTION = -20f;

    @Override
    public void init(HullModSpecAPI spec) {
        super.init(spec);
    }

    @Override
    public void applyPositives(MutableShipStatsAPI stats, float effectMult, String id) {
        stats.getShieldArcBonus().modifyMult(id, getPercentToCorrectedMult(POS_SHIELD_ARC_PERCENT), getDesc());
        stats.getShieldUnfoldRateMult().modifyMult(id, getPercentToCorrectedMult(POS_SHIELD_UNFOLD_PERCENT), getDesc());
        stats.getShieldTurnRateMult().modifyMult(id, getPercentToCorrectedMult(POS_SHIELD_TURN_PERCENT), getDesc());
        stats.getPhaseCloakUpkeepCostBonus().modifyMult(id, getPercentToCorrectedMult(POS_PHASE_FLUX_GEN_PERCENT), getDesc());

        stats.getDynamic().getMod(Stats.PHASE_CLOAK_FLUX_LEVEL_FOR_MIN_SPEED_MOD).modifyPercent(id,Math.round(POS_PHASE_MIN_SPEED_FLUX_LEVEL_PERCENT * getAlcohol().getAddictionStatus().getAddictionValue()), getDesc());
    }

    @Override
    public void applyNegatives(MutableShipStatsAPI stats, float effectMult, String id) {
        stats.getShieldUpkeepMult().modifyMult(id, getPercentToCorrectedMult(NEG_SHIELD_UPKEEP_PERCENT), getDesc());
        stats.getPhaseCloakCooldownBonus().modifyMult(id, getPercentToCorrectedMult(NEG_CLOAK_CD_PERCENT), getDesc());
    }

    @Override
    public void applyWithdrawal(MutableShipStatsAPI stats, float effectMult, String id) {

        stats.getMaxCombatReadiness().modifyMult(id, getPercentToCorrectedMult(WITHDRAWAL_MAX_CR_REDUCTION), getDesc());
        stats.getShieldArcBonus().modifyMult(id, getPercentToCorrectedMult(WITHDRAWAL_SHIELD_ARC_PERCENT), getDesc());
        stats.getShieldDamageTakenMult().modifyMult(id, getPercentToCorrectedMult(WITHDRAWAL_SHIELD_DAMAGE_PERCENT), getDesc());
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        super.applyEffectsAfterShipCreation(ship, id);

        ship.removeListenerOfClass(AlcoholPhaseCRModifier.class);
        ship.addListener(new AlcoholPhaseCRModifier(ship, getAlcohol().getAddictionStatus(), id));
    }

    @Override
    public void addPositiveEffectTooltip(TooltipMakerAPI tooltip, float effectMult) {
        float opad = 10f;
        float spad = 3f;
        Color positive = Misc.getPositiveHighlightColor();
        Color neutral = Misc.getGrayColor();
        Color negative = Misc.getNegativeHighlightColor();

        tooltip.addSectionHeading("Positive Effect", Misc.getTextColor(), new Color(50, 100, 50, 255), Alignment.MID, 10f);

        tooltip.addPara("Increases shield arc by %s  [Max.: %s]",
                opad,
                positive,
                getAbsPercentStringForTooltip(POS_SHIELD_ARC_PERCENT, effectMult),
                getAbsPercentStringForTooltip(POS_SHIELD_ARC_PERCENT));

        tooltip.addPara("Increases shield unfold rate by %s  [Max.: %s]",
                spad,
                positive,
                getAbsPercentStringForTooltip(POS_SHIELD_UNFOLD_PERCENT, effectMult),
                getAbsPercentStringForTooltip(POS_SHIELD_UNFOLD_PERCENT));

        tooltip.addPara("Increases shield turn rate by %s  [Max.: %s]",
                spad,
                positive,
                getAbsPercentStringForTooltip(POS_SHIELD_TURN_PERCENT, effectMult),
                getAbsPercentStringForTooltip(POS_SHIELD_TURN_PERCENT));

        tooltip.addPara("Reduces flux generation during phase by %s  [Max.: %s]",
                spad,
                positive,
                getAbsPercentStringForTooltip(POS_PHASE_FLUX_GEN_PERCENT, effectMult),
                getAbsPercentStringForTooltip(POS_PHASE_FLUX_GEN_PERCENT));

        tooltip.addPara("Increases the max. flux level for zero-flux speed boost by %s  [Max.: %s]",
                spad,
                positive,
                getAbsPercentStringForTooltip(POS_PHASE_MIN_SPEED_FLUX_LEVEL_PERCENT, effectMult),
                getAbsPercentStringForTooltip(POS_PHASE_MIN_SPEED_FLUX_LEVEL_PERCENT));

        tooltip.addPara("Reduces CR decay while phased by %s  [Max.: %s]",
                spad,
                positive,
                getAbsPercentStringForTooltip(POS_PHASE_CR_DEG_DURING_PHASE_PERCENT, effectMult),
                getAbsPercentStringForTooltip(POS_PHASE_CR_DEG_DURING_PHASE_PERCENT));
    }

    @Override
    public void addNegativeEffectTooltip(TooltipMakerAPI tooltip, float effectMult) {
        float opad = 10f;
        float spad = 3f;
        Color positive = Misc.getPositiveHighlightColor();
        Color neutral = Misc.getGrayColor();
        Color negative = Misc.getNegativeHighlightColor();

        tooltip.addSectionHeading("Negative Effect", Misc.getTextColor(), new Color(150, 100, 50, 255), Alignment.MID, 10f);

        //shield upkeep +10 %
        //phase cloak cooldown +10%

        tooltip.addPara("Increases shield upkeep by %s  [Max.: %s]",
                opad,
                negative,
                getAbsPercentStringForTooltip(NEG_SHIELD_UPKEEP_PERCENT, effectMult),
                getAbsPercentStringForTooltip(NEG_SHIELD_UPKEEP_PERCENT));

        tooltip.addPara("Increases phase cooldown by %s  [Max.: %s]",
                spad,
                negative,
                getAbsPercentStringForTooltip(NEG_CLOAK_CD_PERCENT, effectMult),
                getAbsPercentStringForTooltip(NEG_CLOAK_CD_PERCENT));
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

        tooltip.addPara("Reduces shield arc by %s  [Max.: %s]",
                spad,
                bad,
                getAbsPercentStringForTooltip(WITHDRAWAL_SHIELD_ARC_PERCENT, effectMult),
                getAbsPercentStringForTooltip(WITHDRAWAL_SHIELD_ARC_PERCENT));

        tooltip.addPara("Increases damage taken by shields by %s  [Max.: %s]",
                spad,
                bad,
                getAbsPercentStringForTooltip(WITHDRAWAL_SHIELD_DAMAGE_PERCENT, effectMult),
                getAbsPercentStringForTooltip(WITHDRAWAL_SHIELD_DAMAGE_PERCENT));

        tooltip.addPara("Increases CR decay out of phase by %s  [Max.: %s]",
                spad,
                bad,
                getAbsPercentStringForTooltip(WITHDRAWAL_CR_DEG_OUT_OF_PHASE_PERCENT, effectMult),
                getAbsPercentStringForTooltip(WITHDRAWAL_CR_DEG_OUT_OF_PHASE_PERCENT));
    }

    public static class AlcoholPhaseCRModifier implements AdvanceableListener {
        protected ShipAPI ship;
        protected String id;
        protected AddictionStatus status;

        public AlcoholPhaseCRModifier(ShipAPI ship, AddictionStatus status, String id) {
            this.ship = ship;
            this.id = id;
            this.status = status;
        }

        public void advance(float amount) {
            if (ship.isHulk()) return;
            if (ship.getPhaseCloak() == null) return;

            if(status.isConsuming()){
                if(ship.isPhased()){
                    ship.getMutableStats().getCRLossPerSecondPercent().modifyMult(id, getPercentToCorrectedMult(POS_PHASE_CR_DEG_DURING_PHASE_PERCENT));
                } else ship.getMutableStats().getCRLossPerSecondPercent().unmodifyMult(id);
            } else if(status.isWithdrawal()){
                if(ship.isPhased()){
                    ship.getMutableStats().getCRLossPerSecondPercent().unmodifyMult(id);
                } else ship.getMutableStats().getCRLossPerSecondPercent().modifyMult(id, getPercentToCorrectedMult(WITHDRAWAL_CR_DEG_OUT_OF_PHASE_PERCENT));
            }
        }

        public float getPercentToCorrectedMult(float percent){
            return 1 + ((percent * status.getAddictionValue()) / 100);
        }
    }
}
