package com.fs.starfarer.api.alcoholism;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.hullmods.campaignEffects.ExcessOPStripper;
import com.fs.starfarer.api.alcoholism.industry.BreweryPlacer;
import com.fs.starfarer.api.alcoholism.listeners.AlcoholConsumptionManager;
import com.fs.starfarer.api.alcoholism.listeners.AlcoholStackReplacer;
import com.fs.starfarer.api.alcoholism.listeners.ResourceConditionApplicator;
import com.fs.starfarer.api.alcoholism.memory.*;
import com.fs.starfarer.api.alcoholism.scripts.CargoUIOpenChecker;
import com.fs.starfarer.api.alcoholism.scripts.NewDayTracker;
import com.fs.starfarer.api.alcoholism.scripts.RefitUIOpenChecker;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.codex.CodexDataV2;

public class ModPlugin extends BaseModPlugin {

    public static void log(String Text) {
        if(Global.getSettings().isDevMode()) Global.getLogger(ModPlugin.class).info(Text);
    }

    @Override
    public void onGameLoad(boolean newGame) {
        super.onGameLoad(newGame);

        if(Global.getSettings().isDevMode()) devActions();

        //technical basis
        NewDayTracker.register();
        CargoUIOpenChecker.register();
        RefitUIOpenChecker.register();

        //alcohol functionality
        SettingsHandler.getInstance().settingsChanged("alcoholism");
        AddictionMemory.getInstanceOrRegister().refresh();
        AlcoholStackReplacer.register();
        AlcoholConsumptionManager.getInstanceOrRegister();
        ExcessOPStripper.register();

        //economy
        FactionAlcoholHandler.assignFactionAlcohols();
        ResourceConditionApplicator.register();
        BreweryPlacer.placeBreweries();

        //industry items
        AlcoholRepo.addBreweryRecipesToItemEffectRepo();
        AlcoholRepo.addRecipeBookToItemEffectRepo();
    }

    @Override
    public void onAboutToLinkCodexEntries() {
        for (Alcohol alcohol : AlcoholRepo.ALCOHOL_MAP.values()) {
            CodexDataV2.makeRelated(
                    CodexDataV2.getCommodityEntryId(alcohol.getCommodityId()),
                    CodexDataV2.getItemEntryId(alcohol.getId()),
                    CodexDataV2.getItemEntryId(alcohol.getIndustryItemId()),
                    CodexDataV2.getHullmodEntryId(alcohol.getEffectHullmodId()));
        }
    }

    public static void devActions(){
        CargoAPI c = Global.getSector().getPlayerFleet().getCargo();
        for (Alcohol alcohol : AlcoholRepo.ALCOHOL_MAP.values()){
            c.addCommodity(alcohol.getCommodityId(), 50);
        }

        for (String a : AlcoholRepo.ALCOHOL_MAP.keySet()){
            log("ALCOHOL_DEV " + a);
        }

        c.addCommodity(Commodities.SUPPLIES, 200);
    }
}
