package com.fs.starfarer.api.alcoholism.conditions;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.memory.AlcoholRepo;
import com.fs.starfarer.api.alcoholism.memory.Ingredient;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.HashMap;
import java.util.Map;

public class IngredientResourceCondition extends BaseHazardCondition {
    public static final int BASE_OUTPUT = -2;

    public static Map<String, Integer> MODIFIER = new HashMap<String, Integer>();
    static {
        MODIFIER.put(Conditions.FARMLAND_POOR, -1);
        MODIFIER.put(Conditions.FARMLAND_ADEQUATE, 0);
        MODIFIER.put(Conditions.FARMLAND_RICH, 1);
        MODIFIER.put(Conditions.FARMLAND_BOUNTIFUL, 2);
    }

    @Override
    public void apply(String id) {
        super.apply(id);
        if (market.isPlanetConditionMarketOnly() || market.getFaction() == null || Factions.NEUTRAL.equals(market.getFactionId())) return;

        Ingredient ingredient = AlcoholRepo.INGREDIENT_MAP.get(condition.getId());

        //this is pretty bad, but I am on coffee withdrawal and literally can't do any better
        for (String industryId : ingredient.industry){
            if (market.hasIndustry(industryId)){

                int mod = -10;
                BaseIndustry ind = (BaseIndustry) market.getIndustry(Industries.FARMING);
                CommoditySpecAPI spec = getCommoditySpec();

                if (spec == null) return;

                for (Map.Entry<String, Integer> entry : MODIFIER.entrySet()){
                    if (market.hasCondition(entry.getKey())) {
                        mod = entry.getValue();
                        break;
                    }
                }

                if (mod == -10) return; //no condition present

                ind.supply(id, spec.getId(), market.getSize() + BASE_OUTPUT + mod,"Special Ingredients");
            }
        }
    }

    private CommoditySpecAPI getCommoditySpec(){
        String commodityID = condition.getSpec().getId();
        if (commodityID != null) return Global.getSettings().getCommoditySpec(commodityID);

        return null;
    }

    @Override
    public String getName() {
        CommoditySpecAPI spec = getCommoditySpec();
        if (spec != null) return spec.getName();

        return super.getName();
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        CommoditySpecAPI spec = getCommoditySpec();
        if (spec == null) return;

        tooltip.addPara( "%s can be cultivated or found here.", 10f,Misc.getHighlightColor(), spec.getName());
        tooltip.addPara(Global.getSettings().getDescription(spec.getId(), Description.Type.RESOURCE).getText1(), 10f);

        super.createTooltipAfterDescription(tooltip, expanded);
    }
}
