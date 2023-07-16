package com.fs.starfarer.api.alcoholism.itemPlugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.memory.IndustrialAlcohol;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoTransferHandlerAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.campaign.impl.items.GenericSpecialItemPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;

public class RecipeBookItemPlugin extends GenericSpecialItemPlugin {

    public static final String RECIPE_BOOK_ID = "alcoholism_recipe_book";

    public void performRightClickAction() {
        CargoAPI cargo = stack.getCargo();
        RecipeBookSpecialItemData recipeBookSpecialItemData = getSpecialItemData();

        if(recipeBookSpecialItemData != null){
            for (IndustrialAlcohol industrialAlcohol : recipeBookSpecialItemData.getAlcoholList()){
                cargo.addSpecial(new SpecialItemData(industrialAlcohol.getIndustryItemId(), null), 1f);
            }
        }
    }

    public boolean hasRightClickAction() {
        return true;
    }

    public boolean shouldRemoveOnRightClickAction() {
        return true;
    }

    private RecipeBookSpecialItemData getSpecialItemData(){
        return stack.getSpecialDataIfSpecial() instanceof RecipeBookSpecialItemData ? (RecipeBookSpecialItemData) stack.getSpecialDataIfSpecial() : null;
    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, CargoTransferHandlerAPI transferHandler, Object stackSource) {
        float pad = 3f;
        float opad = 10f;
        float small = 5f;
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color b = Misc.getButtonTextColor();
        b = Misc.getPositiveHighlightColor();

        //title and origin
        tooltip.addTitle(getName());
        tooltip.addPara( "Mix and match!", Misc.getGrayColor(), pad);

        tooltip.setBulletedListMode("Installed in:");
        tooltip.setBulletWidth(100f);
        addInstalledInSection(tooltip, opad);
        tooltip.setBulletedListMode(null);

        tooltip.addPara("This notebook can store recipes for beverages.", pad);
        tooltip.addPara("You can add recipes to it by right-clicking them.", opad);
        tooltip.addPara("Installing this in a brewery will split the output and stockpiling between all alcohols in the book.", opad);

        tooltip.addPara("Contains recipes for:", pad);

        RecipeBookSpecialItemData recipeBookSpecialItemData = getSpecialItemData();
        if(recipeBookSpecialItemData != null){
            for (IndustrialAlcohol industrialAlcohol : recipeBookSpecialItemData.getAlcoholList()){
                tooltip.addPara(industrialAlcohol.getName(), industrialAlcohol.getFaction().getColor(), pad);
            }
        }

        addCostLabel(tooltip, opad, transferHandler, stackSource);

        tooltip.addPara("Right-click to unbundle recipes", b, opad);
    }

    public int getPrice(MarketAPI market, SubmarketAPI submarket) {
        int amt = 0;
        RecipeBookSpecialItemData data = getSpecialItemData();
        if(data != null) for (IndustrialAlcohol industrialAlcohol : data.getAlcoholList()) amt += Global.getSettings().getSpecialItemSpec(industrialAlcohol.getIndustryItemId()).getBasePrice();
        return amt;
    }
}
