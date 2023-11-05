package com.fs.starfarer.api.alcoholism_re.itemPlugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism_re.conditions.AlcoholResourceCondition;
import com.fs.starfarer.api.alcoholism_re.memory.AlcoholAPI;
import com.fs.starfarer.api.alcoholism_re.memory.AlcoholRepo;
import com.fs.starfarer.api.alcoholism_re.memory.IndustrialAlcohol;
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
        super(((IndustrialAlcohol) AlcoholRepo.get(alcoholId)).getIndustryItemId());
        this.alcoholId = alcoholId;
    }

    @Override
    public void apply(Industry industry) {
        if (industry instanceof BaseIndustry) {
            BaseIndustry b = (BaseIndustry) industry;

            if (industry.isFunctional()) {
                AlcoholAPI alcohol = AlcoholRepo.get(alcoholId);

                if (alcohol instanceof IndustrialAlcohol) {
                    applyDemand(b, 0, ((IndustrialAlcohol) alcohol).getDemandsForProduction());
                    applySupply(b, 0, ((IndustrialAlcohol) alcohol).getCommodityId());
                }
            }
        }
    }

    @Override
    public void unapply(Industry industry) {
        IndustrialAlcohol industrialAlcohol = (IndustrialAlcohol) AlcoholRepo.get(alcoholId);
        BaseIndustry ind = (BaseIndustry) industry;

        ind.supply(industrialAlcohol.getCommodityId(), 0);

        for (String s : industrialAlcohol.getDemandsForProduction()) {
            ind.demand(s, 0);
        }
    }

    protected void addItemDescriptionImpl(Industry industry, TooltipMakerAPI text, SpecialItemData data,
                                          InstallableIndustryItemPlugin.InstallableItemDescriptionMode mode, String pre, float pad) {
        IndustrialAlcohol industrialAlcohol = (IndustrialAlcohol) AlcoholRepo.get(alcoholId);

        text.addPara(pre + "Allows a Brewery to produce %s",
                pad, industrialAlcohol.getFaction().getColor(),
                industrialAlcohol.getName());
        if (mode == InstallableIndustryItemPlugin.InstallableItemDescriptionMode.CARGO_TOOLTIP || mode == InstallableIndustryItemPlugin.InstallableItemDescriptionMode.INDUSTRY_TOOLTIP) {
            text.addPara(Global.getSettings().getDescription(industrialAlcohol.getCommodityId(), Description.Type.RESOURCE).getText1(), 10f);
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
