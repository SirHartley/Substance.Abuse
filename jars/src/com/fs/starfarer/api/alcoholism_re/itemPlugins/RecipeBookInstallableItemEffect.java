package com.fs.starfarer.api.alcoholism_re.itemPlugins;

import com.fs.starfarer.api.alcoholism_re.conditions.AlcoholResourceCondition;
import com.fs.starfarer.api.alcoholism_re.memory.IndustrialAlcohol;
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
            List<IndustrialAlcohol> industrialAlcoholList = getRecipeBookAlcohols(industry);
            int maxAmt = industry.getMarket().getSize();
            int perAlcohol = (int) Math.ceil(maxAmt/ industrialAlcoholList.size());

            if (industry.isFunctional()) {
                for (IndustrialAlcohol industrialAlcohol : industrialAlcoholList) {
                    if (maxAmt > 0) {
                        int output = Math.min(perAlcohol, maxAmt);

                        for (String s : industrialAlcohol.getDemandsForProduction()){
                            int curr = industry.getDemand(s).getQuantity().getModifiedInt();
                            if(curr > 0) applyDemand(b, curr + 1, s);
                            else applyDemand(b, output, s);
                        }

                        applySupply(b, output, industrialAlcohol.getCommodityId());
                        maxAmt -= output;
                    }
                }
            }
        }
    }

    @Override
    public void unapply(Industry industry) {
        BaseIndustry ind = (BaseIndustry) industry;

        for (IndustrialAlcohol industrialAlcohol : getRecipeBookAlcohols(industry)) {
            ind.supply(industrialAlcohol.getCommodityId(), 0);

            for (String s : industrialAlcohol.getDemandsForProduction()) {
                ind.demand(s, 0);
            }
        }
    }

    public List<IndustrialAlcohol> getRecipeBookAlcohols(Industry industry) {
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

            for (IndustrialAlcohol industrialAlcohol : recipeData.getAlcoholList()) {
                text.addPara(BaseIntelPlugin.BULLET + "%s",
                        pad, industrialAlcohol.getFaction().getColor(),
                        industrialAlcohol.getName());
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
