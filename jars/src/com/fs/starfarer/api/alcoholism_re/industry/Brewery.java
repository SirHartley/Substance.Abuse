package com.fs.starfarer.api.alcoholism_re.industry;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism_re.memory.AlcoholAPI;
import com.fs.starfarer.api.alcoholism_re.memory.AlcoholRepo;
import com.fs.starfarer.api.campaign.SubmarketPlugin;
import com.fs.starfarer.api.campaign.econ.MutableCommodityQuantity;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.submarkets.LocalResourcesSubmarketPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;

public class Brewery extends BaseIndustry {

    public static final String INDUSTRY_ID = "alcohol_brewery";
    @Override
    public void apply() {
        super.apply(true);

        if(getSpecialItem() != null){
            int size = market.getSize();

            if (market.isPlayerOwned()) {
                SubmarketPlugin sub = Misc.getLocalResources(market);
                if (sub instanceof LocalResourcesSubmarketPlugin) {
                    LocalResourcesSubmarketPlugin lr = (LocalResourcesSubmarketPlugin) sub;
                    float mult = Global.getSettings().getFloat("stockpileMultExcess");

                    for (MutableCommodityQuantity q : supply.values()){
                        if(q.getQuantity().getModifiedInt() > 0){
                            lr.getStockpilingBonus(q.getCommodityId()).modifyFlat(getModId(0), size * mult);
                        }
                    }
                }
            }
        }

        if (getSpecialItem() == null || !isFunctional()) {
            supply.clear();
            demand.clear();
        }
    }

    @Override
    public void unapply() {
        super.unapply();

        if (market.isPlayerOwned()) {
            SubmarketPlugin sub = Misc.getLocalResources(market);
            if (sub instanceof LocalResourcesSubmarketPlugin) {
                LocalResourcesSubmarketPlugin lr = (LocalResourcesSubmarketPlugin) sub;

                for (AlcoholAPI alcohol : AlcoholRepo.ALCOHOL_MAP.values()){
                    lr.getStockpilingBonus(alcohol.getCommodityId()).unmodify();
                }
            }
        }

        supply.clear();
        demand.clear();
    }

    @Override
    protected void addPostDescriptionSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode) {
        super.addPostDescriptionSection(tooltip, mode);
        Color color = mode == IndustryTooltipMode.ADD_INDUSTRY ? Misc.getHighlightColor(): Misc.getNegativeHighlightColor();

        if(getSpecialItem() == null) tooltip.addPara("Requires a recipe to produce alcohol.", color, 10f);
    }

    @Override
    public boolean isIndustry() {
        return market.isPlayerOwned();
    }

    @Override
    protected boolean canImproveToIncreaseProduction() {
        return true;
    }
}
