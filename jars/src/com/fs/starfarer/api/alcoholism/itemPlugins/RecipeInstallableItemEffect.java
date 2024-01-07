package com.fs.starfarer.api.alcoholism.itemPlugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.ModPlugin;
import com.fs.starfarer.api.alcoholism.conditions.AlcoholResourceCondition;
import com.fs.starfarer.api.alcoholism.memory.AlcoholAPI;
import com.fs.starfarer.api.alcoholism.memory.AlcoholRepo;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.InstallableIndustryItemPlugin;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseInstallableItemEffect;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public class RecipeInstallableItemEffect extends BaseInstallableItemEffect {
    private final String alcoholId;

    public RecipeInstallableItemEffect(String alcoholId) {
        super((AlcoholRepo.get(alcoholId)).getIndustryItemId());
        this.alcoholId = alcoholId;
    }

    @Override
    public void apply(Industry industry) {
        if (industry instanceof BaseIndustry) {
            BaseIndustry b = (BaseIndustry) industry;

            if (industry.isFunctional()) {
                AlcoholAPI alcohol = AlcoholRepo.get(alcoholId);

                applyDemand(b, 0, ((alcohol).getDemandsForProduction()));
                applySupply(b, 0, ((alcohol).getCommodityId()));
                
            }
        }
    }

    @Override
    public void unapply(Industry industry) {
        AlcoholAPI alcohol = AlcoholRepo.get(alcoholId);
        BaseIndustry ind = (BaseIndustry) industry;

        ind.supply(alcohol.getCommodityId(), 0);

        for (String s : alcohol.getDemandsForProduction()) {
            ind.demand(s, 0);
        }
    }

    protected void addItemDescriptionImpl(Industry industry, TooltipMakerAPI text, SpecialItemData data,
                                          InstallableIndustryItemPlugin.InstallableItemDescriptionMode mode, String pre, float pad) {
        AlcoholAPI alcohol = AlcoholRepo.get(alcoholId);

        text.addPara(pre + "Allows a Brewery to produce %s",
                pad, alcohol.getFaction().getColor(),
                alcohol.getName());
        if (mode == InstallableIndustryItemPlugin.InstallableItemDescriptionMode.CARGO_TOOLTIP || mode == InstallableIndustryItemPlugin.InstallableItemDescriptionMode.INDUSTRY_TOOLTIP) {
            text.addPara(alcohol.getDesc(), 10f);
        }
    }

    private static void applySupply(BaseIndustry ind, int marketSizeMod, String... commodityIDs) {
        int amt = Math.max(0, ind.getMarket().getSize() + marketSizeMod);

        for (String s : commodityIDs) {
            AlcoholResourceCondition.applySupply(ind, s, amt);
        }
    }

    private static void applyDemand(BaseIndustry ind, int marketSizeMod, String... commodityIDs) {
        int amt = Math.max(0, ind.getMarket().getSize() + marketSizeMod);

        for (String s : commodityIDs) {
            AlcoholResourceCondition.applyDemand(ind, s, amt);
        }
    }
}
