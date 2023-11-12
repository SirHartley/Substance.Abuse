package com.fs.starfarer.api.alcoholism.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.industry.Brewery;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.listeners.BaseIndustryOptionProvider;
import com.fs.starfarer.api.campaign.listeners.DialogCreatorUI;
import com.fs.starfarer.api.campaign.listeners.ListenerManagerAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BreweryOptionProvider  extends BaseIndustryOptionProvider {

    public static Object CUSTOM_PLUGIN = new Object();

    public static void register() {
        ListenerManagerAPI listeners = Global.getSector().getListenerManager();
        if (!listeners.hasListenerOfClass(BreweryOptionProvider.class)) {
            listeners.addListener(new BreweryOptionProvider(), true);
        }
    }

    @Override
    public boolean isUnsuitable(Industry ind, boolean allowUnderConstruction) {
        boolean isTarget = ind.getId().equals(Brewery.INDUSTRY_ID) && ind.isFunctional();

        return super.isUnsuitable(ind, allowUnderConstruction)
                || !isTarget;
    }

    @Override
    public List<IndustryOptionData> getIndustryOptions(Industry ind) {
        if (isUnsuitable(ind, false)) return null;

        List<IndustryOptionData> result = new ArrayList<IndustryOptionData>();

        IndustryOptionData opt = new IndustryOptionData("Manage Brewery", CUSTOM_PLUGIN, ind, this);
        opt.color = new Color(150, 100, 255, 255);
        result.add(opt);

        return result;
    }

    @Override
    public void createTooltip(IndustryOptionData opt, TooltipMakerAPI tooltip, float width) {
        if (opt.id == CUSTOM_PLUGIN) {
            tooltip.addSectionHeading("Manage your Brewery", Alignment.MID, 0f);
            tooltip.addPara("Create and manage your own alcohol.", 10f);
        }
    }

    @Override
    public void optionSelected(IndustryOptionData opt, DialogCreatorUI ui) {
        if (opt.id == CUSTOM_PLUGIN) {
            BreweryDialoguePlugin plugin = new BreweryDialoguePlugin(opt.ind);
            ui.showDialog(null, plugin);
        }
    }
}
