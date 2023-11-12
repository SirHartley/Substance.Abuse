package com.fs.starfarer.api.alcoholism.itemPlugins;

import com.fs.starfarer.api.alcoholism.memory.AlcoholAPI;
import com.fs.starfarer.api.alcoholism.memory.AlcoholRepo;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CargoTransferHandlerAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.InstallableIndustryItemPlugin;
import com.fs.starfarer.api.campaign.impl.items.GenericSpecialItemPlugin;
import com.fs.starfarer.api.impl.campaign.econ.impl.InstallableItemEffect;
import com.fs.starfarer.api.impl.campaign.econ.impl.ItemEffectsRepo;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;

public class RecipeItemPlugin extends GenericSpecialItemPlugin {

    @Override
    public boolean hasRightClickAction() {
        return true;
    }

    @Override
    public void performRightClickAction() {
        CargoAPI cargo = stack.getCargo();
        RecipeBookSpecialItemData data = null;

        for (CargoStackAPI stack : cargo.getStacksCopy()){
            if (stack.isSpecialStack() && stack.getSpecialDataIfSpecial() instanceof RecipeBookSpecialItemData){
                data = (RecipeBookSpecialItemData) stack.getSpecialDataIfSpecial();
                break;
            }
        }

        if(data == null){
            data = new RecipeBookSpecialItemData(RecipeBookItemPlugin.RECIPE_BOOK_ID, getAlcoholId());
            cargo.addSpecial(data, 1);
        } else {
            data.addAlcohol(getAlcoholId());
        }
    }

    public String getAlcoholId(){
        for (AlcoholAPI alcohol : AlcoholRepo.getNonCustomAlcoholList()){
           if(alcohol.getIndustryItemId().equals(getId())) return alcohol.getId();
        }

        return null;
    }

    @Override
    public boolean shouldRemoveOnRightClickAction() {
        return true;
    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, CargoTransferHandlerAPI transferHandler, Object stackSource) {
        float pad = 0f;
        float opad = 10f;

        tooltip.addTitle(getName());

        LabelAPI design = Misc.addDesignTypePara(tooltip, getDesignType(), opad);

        float bulletWidth = 70f;
        if (design != null) {
            bulletWidth = design.computeTextWidth("Design type: ");
        }

        InstallableItemEffect effect = ItemEffectsRepo.ITEM_EFFECTS.get(getId());
        if (effect != null) {
            tooltip.setBulletWidth(bulletWidth);
            tooltip.setBulletColor(Misc.getGrayColor());

            tooltip.setBulletedListMode("Installed in:");
            tooltip.setBulletWidth(100f);
            addInstalledInSection(tooltip, opad);

            tooltip.setBulletedListMode(null);

            if (!spec.getDesc().isEmpty()) {
                Color c = Misc.getTextColor();
                //if (useGray) c = Misc.getGrayColor();
                tooltip.addPara(spec.getDesc(), c, opad);
            }
            effect.addItemDescription(null, tooltip, new SpecialItemData(getId(), null), InstallableIndustryItemPlugin.InstallableItemDescriptionMode.CARGO_TOOLTIP);
        } else {
            if (!spec.getDesc().isEmpty()) {
                Color c = Misc.getTextColor();
                tooltip.addPara(spec.getDesc(), c, opad);
            }
        }

        addCostLabel(tooltip, opad, transferHandler, stackSource);

        tooltip.addPara("Right-click to add to recipe book", Misc.getPositiveHighlightColor(), opad);

    }


}
