package com.fs.starfarer.api.alcoholism_re.hullmods.alcoholEffectHullmods;

import com.fs.starfarer.api.alcoholism_re.hullmods.BaseAlcoholHullmodEffect;
import com.fs.starfarer.api.alcoholism_re.memory.AddictionBrain;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;

public class Water_HullmodEffect extends BaseAlcoholHullmodEffect {
    @Override
    public void applyPositives(MutableShipStatsAPI stats, float effectMult, String id) {

    }

    @Override
    public void applyNegatives(MutableShipStatsAPI stats, float effectMult, String id) {

    }

    @Override
    public void applyWithdrawal(MutableShipStatsAPI stats, float effectMult, String id) {

    }

    @Override
    public void addPositiveEffectTooltip(TooltipMakerAPI tooltip, float effectMult) {
        float opad = 10f;
        float spad = 3f;
        Color positive = Misc.getPositiveHighlightColor();
        Color neutral = Misc.getGrayColor();
        Color negative = Misc.getNegativeHighlightColor();
        
        tooltip.addSectionHeading("Positive Effect", Misc.getTextColor(), new Color(50, 100, 50, 255), Alignment.MID, 10f);        

        tooltip.addPara("Increases withdrawal decay by %s  [Max.: %s]",
                opad,
                positive,
                Math.round(1 + (AddictionBrain.WATER_MULT * effectMult)) + "x",
                Math.round(1 + AddictionBrain.WATER_MULT) + "x");
    }

    @Override
    public void addNegativeEffectTooltip(TooltipMakerAPI tooltip, float effectMult) {
        float opad = 10f;
        float spad = 3f;
        Color positive = Misc.getPositiveHighlightColor();
        Color neutral = Misc.getGrayColor();
        Color negative = Misc.getNegativeHighlightColor();

        tooltip.addSectionHeading("Negative Effect", Misc.getTextColor(), new Color(150, 100, 50, 255), Alignment.MID, 10f);

        tooltip.addPara("Decreases effect buildup by %s  [Max.: %s]",
                opad,
                negative,
                (int) Math.round(1 + (AddictionBrain.WATER_MULT * effectMult)) + "x",
                (int) Math.round(1 + AddictionBrain.WATER_MULT) + "x");
    }

    @Override
    public void addWithdrawalEffectTooltip(TooltipMakerAPI tooltip, float effectMult) {

    }
}
