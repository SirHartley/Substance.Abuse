package com.fs.starfarer.api.alcoholism.itemPlugins;

import com.fs.starfarer.api.alcoholism.conditions.AlcoholResourceCondition;
import com.fs.starfarer.api.alcoholism.memory.AlcoholAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.InstallableIndustryItemPlugin;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseInstallableItemEffect;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.ArrayList;
import java.util.List;

public class RecipeBookInstallableItemEffect extends BaseInstallableItemEffect {

    public RecipeBookInstallableItemEffect(String id) {
        super(id);
    }

    @Override
    public void apply(Industry industry) {
        if (industry instanceof BaseIndustry) {
            BaseIndustry b = (BaseIndustry) industry;
            List<AlcoholAPI> alcoholList = getRecipeBookAlcohols(industry);
            float maxAmt = industry.getMarket().getSize();
            int perAlcohol = (int) Math.ceil(maxAmt/ alcoholList.size());

            if (industry.isFunctional()) {
                for (AlcoholAPI alcohol : alcoholList) {
                    if (maxAmt > 0) {
                        int output = Math.min(perAlcohol, Math.round(maxAmt));

                        for (String s : alcohol.getDemandsForProduction()){
                            int curr = industry.getDemand(s).getQuantity().getModifiedInt();
                            if(curr > 0) applyDemand(b, curr + 1, s);
                            else applyDemand(b, output, s);
                        }

                        applySupply(b, output, alcohol.getCommodityId());
                        maxAmt -= output;
                    }
                }
            }
        }
    }

    @Override
    public void unapply(Industry industry) {
        BaseIndustry ind = (BaseIndustry) industry;

        for (AlcoholAPI alcohol : getRecipeBookAlcohols(industry)) {
            ind.supply(alcohol.getCommodityId(), 0);

            for (String s : alcohol.getDemandsForProduction()) {
                ind.demand(s, 0);
            }
        }
    }

    public List<AlcoholAPI> getRecipeBookAlcohols(Industry industry) {
        if (industry.getSpecialItem() instanceof RecipeBookSpecialItemData) {
            RecipeBookSpecialItemData data = (RecipeBookSpecialItemData) industry.getSpecialItem();
            if (data != null) return data.getAlcoholList();
        }

        return new ArrayList<>();
    }


    @Override
    protected void addItemDescriptionImpl(Industry industry, TooltipMakerAPI text, SpecialItemData data,
                                InstallableIndustryItemPlugin.InstallableItemDescriptionMode mode, String pre, float pad) {

        if (data instanceof RecipeBookSpecialItemData) {
            text.addPara(pre + "Allows a Brewery to produce the following drinks:", pad);

            RecipeBookSpecialItemData recipeData = (RecipeBookSpecialItemData) data;
            if (industry != null) {
                int size = industry.getMarket().getSize();
                if (size < recipeData.getAlcoholList().size())
                    text.addPara("The colony is too small to produce all recipes at once!", Misc.getNegativeHighlightColor(), 10f);
            }

            for (AlcoholAPI alcohol : recipeData.getAlcoholList()) {
                text.addPara(BaseIntelPlugin.BULLET + "%s",
                        pad, alcohol.getFaction().getColor(),
                        alcohol.getName());
            }
        }
    }

    private static void applySupply(BaseIndustry ind, int amt, String... commodityIDs) {
        for (String s : commodityIDs) {
            AlcoholResourceCondition.applySupply(ind, s, amt);
        }
    }

    private static void applyDemand(BaseIndustry ind, int amt, String... commodityIDs) {
        for (String s : commodityIDs) {
            AlcoholResourceCondition.applyDemand(ind, s, amt);
        }
    }
}
