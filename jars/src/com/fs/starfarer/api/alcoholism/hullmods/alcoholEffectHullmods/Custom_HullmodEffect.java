package com.fs.starfarer.api.alcoholism.hullmods.alcoholEffectHullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.ModPlugin;
import com.fs.starfarer.api.alcoholism.hullmods.BaseAlcoholHullmodEffect;
import com.fs.starfarer.api.alcoholism.memory.AlcoholAPI;
import com.fs.starfarer.api.alcoholism.memory.AlcoholRepo;
import com.fs.starfarer.api.alcoholism.memory.CustomAlcohol;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;

public class Custom_HullmodEffect extends BaseAlcoholHullmodEffect {

    private void updateSpec(){
        if (getAlcohol() == null) {
            spec.setDisplayName("");
            spec.setSpriteName("graphics/conditions/placeholder.png");
        }

        spec.setDisplayName(getAlcohol().getName());
        spec.setSpriteName(((CustomAlcohol)getAlcohol()).iconName);
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        updateSpec();
        super.applyEffectsBeforeShipCreation(hullSize, stats, id);
    }

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

        tooltip.addPara("Test %s  [Max.: %s]",
                opad,
                positive,
                getAbsPercentStringForTooltip(0.5f, effectMult),
                getAbsPercentStringForTooltip(0.5f));
    }

    @Override
    public void addNegativeEffectTooltip(TooltipMakerAPI tooltip, float effectMult) {
        float opad = 10f;
        float spad = 3f;
        Color positive = Misc.getPositiveHighlightColor();
        Color neutral = Misc.getGrayColor();
        Color negative = Misc.getNegativeHighlightColor();

        tooltip.addSectionHeading("Negative Effect", Misc.getTextColor(), new Color(150, 100, 50, 255), Alignment.MID, 10f);

    }

    @Override
    public void addWithdrawalEffectTooltip(TooltipMakerAPI tooltip, float effectMult) {
        float opad = 10f;
        float spad = 3f;
        Color positive = Misc.getPositiveHighlightColor();
        Color negative = Misc.getNegativeHighlightColor();
        Color bad = Color.red;

        tooltip.addSectionHeading("Withdrawal Effect", Misc.getTextColor(), new Color(150, 50, 50, 255), Alignment.MID, 10f);
    }
}
