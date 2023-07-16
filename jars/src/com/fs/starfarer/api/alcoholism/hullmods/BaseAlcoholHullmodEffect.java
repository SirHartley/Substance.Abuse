package com.fs.starfarer.api.alcoholism.hullmods;

import com.fs.starfarer.api.alcoholism.memory.AddictionStatus;
import com.fs.starfarer.api.alcoholism.memory.AlcoholRepo;
import com.fs.starfarer.api.alcoholism.memory.BaseAlcohol;
import com.fs.starfarer.api.campaign.CampaignUIAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;

public abstract class BaseAlcoholHullmodEffect extends SelfRepairingBuiltInHullmod implements AlcoholHullmodEffectAPI {

    @Override
    public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CampaignUIAPI.CoreUITradeMode mode) {
        return false;
    }

    @Override
    public Color getNameColor() {
        return getAlcohol().getFaction().getColor();
    }

    @Override
    public Color getBorderColor() {
        return getAlcohol().getAddictionStatus().isWithdrawal() ? Color.RED : Color.GREEN;
    }

    public BaseAlcohol getAlcohol(){
        return (BaseAlcohol) AlcoholRepo.get(spec.getId());
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id);

        BaseAlcohol baseAlcohol = getAlcohol();
        AddictionStatus status = baseAlcohol.getAddictionStatus();
        float effectMult = status.getAddictionValue();

        if(status.isWithdrawal()) applyWithdrawal(stats, effectMult, id);
        else applyPositives(stats, effectMult, id);

        applyNegatives(stats, effectMult,id);
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);

        getAlcohol().addStatusTooltip(tooltip, true);
        addCurrentEffectSection(tooltip, getAlcohol().getAddictionStatus().getAddictionValue());

        tooltip.addSectionHeading("", Misc.getGrayColor(), new Color(255,255,255,2), Alignment.MID, 10f);
    }

    public boolean isWithdrawal(){
        return getAlcohol().getAddictionStatus().isWithdrawal();
    }

    public String getDesc(){
        String post = isWithdrawal() ? " withdrawal" : "";
        return getAlcohol().getName() + post;
    }

    @Override
    public void addCurrentEffectSection(TooltipMakerAPI tooltip, float effectMult) {
        AddictionStatus status = getAlcohol().getAddictionStatus();

        if(status.isConsuming()) addPositiveEffectTooltip(tooltip, effectMult);
        addNegativeEffectTooltip(tooltip, effectMult);
        if(status.isWithdrawal())addWithdrawalEffectTooltip(tooltip, effectMult);
    }

    public float getPercentToCorrectedMult(float percent){
        return 1 + ((percent * getAlcohol().getAddictionStatus().getAddictionValue()) / 100);
    }

    public String getAbsPercentStringForTooltip(float percent){
        return getAbsPercentStringForTooltip(percent, 1f);

    }

    public String getAbsPercentStringForTooltip(float percent, float mult){
        return (int) Math.ceil(Math.abs(percent) * mult) + "%";

    }
}
