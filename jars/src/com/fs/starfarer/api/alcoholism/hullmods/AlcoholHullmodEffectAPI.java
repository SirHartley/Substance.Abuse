package com.fs.starfarer.api.alcoholism.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public interface AlcoholHullmodEffectAPI {
    void applyPositives(MutableShipStatsAPI stats, float effectMult, String id);
    void applyNegatives(MutableShipStatsAPI stats, float effectMult, String id);
    void applyWithdrawal(MutableShipStatsAPI stats, float effectMult, String id);
    void addCurrentEffectSection(TooltipMakerAPI tooltip, float effectMult);
    void addPositiveEffectTooltip(TooltipMakerAPI tooltip, float effectMult);
    void addNegativeEffectTooltip(TooltipMakerAPI tooltip, float effectMult);
    void addWithdrawalEffectTooltip(TooltipMakerAPI tooltip, float effectMult);
}
