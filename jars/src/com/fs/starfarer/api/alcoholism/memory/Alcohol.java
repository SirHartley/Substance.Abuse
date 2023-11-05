package com.fs.starfarer.api.alcoholism.memory;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.TooltipHelper;
import com.fs.starfarer.api.alcoholism.hullmods.AlcoholHullmodEffectAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;

import java.awt.*;

/**
 this is actually a deprecated class but required for the old mod integration system
 everything in use is either Industrial or Custom alcohol
 **/

public class Alcohol implements AlcoholAPI {
    private String id;
    private float mult;
    private String factionId;
    private String commodityId;
    private String industryItemId;
    private String[] demandsForProduction;
    private int lightIndustryMod;
    private int populationImportMod;

    //light industry mod (both import and export)
    //population import mod

    public Alcohol(String id, float mult, String factionIdForColours,
                   int lightIndustryMod, int populationImportMod,
                   String... demandsForProduction) {
        this.id = id;
        this.commodityId = id + "_c";
        this.industryItemId = id + "_item";
        this.mult = mult;
        this.factionId = factionIdForColours;
        this.lightIndustryMod = lightIndustryMod;
        this.populationImportMod = populationImportMod;
        this.demandsForProduction = demandsForProduction;
    }

    public String getCommodityId() {
        return commodityId;
    }

    public String getIndustryItemId() {
        return industryItemId;
    }

    public String[] getDemandsForProduction() {
        return demandsForProduction;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public AddictionStatus getAddictionStatus() {
        return AddictionMemory.getInstanceOrRegister().getStatusForId(id);
    }

    public int getLightIndustryMod() {
        return lightIndustryMod;
    }

    public int getPopulationImportMod() {
        return populationImportMod;
    }

    public void addStatusTooltip(TooltipMakerAPI tt) {
        addStatusTooltip(tt, false);
    }

    public void addStatusTooltip(TooltipMakerAPI tt, boolean forHullmod) {
        float opad = 10f;
        float spad = 3f;

        tt.addSectionHeading("Substance Information", Alignment.MID, opad);

        Pair<String, Color> addictivity = TooltipHelper.getResistanceBuildupStringAndColour(id);
        int amtPrediction = TooltipHelper.getPredictedAmountRequiredForOneMonth(id);
        CargoAPI c = Global.getSector().getPlayerFleet().getCargo();

        float amtInCargo;
        if (c.getCommodityQuantity(getCommodityId()) > 0f) amtInCargo = c.getCommodityQuantity(getCommodityId());
        else amtInCargo = c.getQuantity(CargoAPI.CargoItemType.SPECIAL, new SpecialItemData(id, null));

        int amtWillLastDays = TooltipHelper.getAmountWillLastDays(this, amtInCargo);
        int amtWillLastMonths = Math.round(amtWillLastDays / AddictionBrain.DAYS_PER_MONTH);

        String lastsFor;
        if (amtWillLastMonths > 1)
            lastsFor = amtWillLastMonths + " " + TooltipHelper.getMonthOrMonths(amtWillLastMonths);
        else lastsFor = amtWillLastDays + " " + TooltipHelper.getDayOrDays(amtWillLastDays);

        tt.addPara("Effect buildup speed: %s", opad, addictivity.two, addictivity.one.toUpperCase());
        if (getAddictionStatus().isAddicted())
            tt.addPara("Current effect level: %s", spad, Misc.getHighlightColor(), TooltipHelper.getCurrentEffectPercentString(id));
        else tt.addPara("Consume for effect!", spad);
        if (!forHullmod)
            tt.addPara("Required amount per Month: %s", spad, Misc.getHighlightColor(), amtPrediction + " Units");
        if (amtInCargo > 0) tt.addPara("Cargo will last for another: %s", spad, Misc.getHighlightColor(), lastsFor);

        if (!forHullmod)
            tt.addPara("A higher effect level leads to higher resistance, which leads to more consumption per day. Be wary of withdrawal effects when you run out!", Misc.getGrayColor(), opad);
    }

    public String getName() {
        return Global.getSettings().getSpecialItemSpec(getId()).getName();
    }

    public FactionAPI getFaction() {
        return Global.getSector().getFaction(factionId);
    }

    public float incrementAddiction(float days) {
        AddictionStatus status = getAddictionStatus();
        float addictionIncrease = AddictionBrain.getAddictionIncrease(mult, status.getAddictionValue(), days);
        status.increment(addictionIncrease);

        return addictionIncrease;
    }

    public float decreaseAddiction(float days) {
        AddictionStatus status = getAddictionStatus();
        float addictionIncrease = AddictionBrain.getAddictionIncrease(mult, status.getAddictionValue(), days);
        status.increment(addictionIncrease);

        return addictionIncrease;
    }

    @Override
    public void addEffectTooltip(TooltipMakerAPI tt, boolean forHullmod) {
        float opad = 10f;
        float spad = 3f;

        AddictionStatus status = getAddictionStatus();

        if (forHullmod) {
            addCurrentHullmodEffectSection(tt);
        } else {
            if (!status.isAddicted() && !status.isConsuming()) {
                tt.addSectionHeading("Effect Information", Alignment.MID, opad);
                addShortEffectText(tt);
                tt.addPara("Every alcohol also has a minor negative side.", Misc.getGrayColor(), spad);
            } else {
                addCurrentHullmodEffectSection(tt);
            }
        }
    }

    public void addShortEffectText(TooltipMakerAPI tooltip) {
        tooltip.addPara(Global.getSettings().getDescription(getCommodityId(), Description.Type.RESOURCE).getText2(), 10f);
    }

    public void addCurrentHullmodEffectSection(TooltipMakerAPI tooltip) {
        HullModSpecAPI spec = Global.getSettings().getHullModSpec(getEffectHullmodId());

        if (spec.getEffect() instanceof AlcoholHullmodEffectAPI) {
            AlcoholHullmodEffectAPI hullmod = (AlcoholHullmodEffectAPI) spec.getEffect();
            hullmod.addCurrentEffectSection(tooltip, getAddictionStatus().getAddictionValue());
        }
    }

    public String getEffectHullmodId() {
        return id;
    }

    public float getMult() {
        return mult;
    }
}
