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
import com.fs.starfarer.api.alcoholism.ui.BreweryOptionProvider;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;

public class ModPlugin extends BaseModPlugin {

    //make handler for custom alcohol running it as special item exclusively
    //make 3 hullmods and allow applying 3 customs at the same time, display tt "only 3" when filled
    //no recipe needed

    public static final String SETUP_KEY = "alcohol_setup";

    public static void log(String Text) {
        if (Global.getSettings().isDevMode()) Global.getLogger(ModPlugin.class).info(Text);
    }

    public static void warn(String Text) {
        if (Global.getSettings().isDevMode()) Global.getLogger(ModPlugin.class).warn(Text);
    }

    @Override
    public void onGameLoad(boolean newGame) {
        super.onGameLoad(newGame);

        //technical basis
        NewDayTracker.register();
        CargoUIOpenChecker.register();
        RefitUIOpenChecker.register();
        AlcoholStackReplacer.register();
        BreweryOptionProvider.register();

        //alcohol functionality
        AlcoholRepo.loadIngredients();
        CustomAlcoholMemory.getInstanceOrRegister().initAll();
        AddictionMemory.getInstanceOrRegister().refresh();
        AlcoholConsumptionManager.getInstanceOrRegister();
        ExcessOPStripper.register();

        //economy
        ResourceConditionApplicator.register();
        ResourceConditionApplicator.applyIngredientConditions();
        BreweryPlacer.placeBreweries();

        //industry items
        AlcoholRepo.addBreweryRecipesToItemEffectRepo();
        AlcoholRepo.addRecipeBookToItemEffectRepo();

        if (newGame) devAddCustomAlcohol();
        if (Global.getSettings().isDevMode()) devActions();
    }

    public static void devAddCustomAlcohol() {
        CustomAlcohol alcohol1 = new CustomAlcohol(null, "This is a test description entry override for a custom alcohol", "graphics/items/custom/custom1.png", "player",
                "alcoholism_trop_fruit", "alcoholism_fruit", "alcoholism_rare_botanics", "alcoholism_salt");

        alcohol1.init();
    }

    public static void devActions() {
        CargoAPI c = Global.getSector().getPlayerFleet().getCargo();
        for (AlcoholAPI baseAlcohol : AlcoholRepo.getIndustrialAlcoholList()) {
            c.addCommodity(baseAlcohol.getCommodityId(), 10);
        }

        for (AlcoholAPI alcohol : AlcoholRepo.getCustomAlcoholList()) {
            c.addSpecial(new SpecialItemData(CustomAlcohol.CUSTOM_ALCOHOL_ITEM_ID, alcohol.getId()), 10);
        }

        for (AlcoholAPI a : AlcoholRepo.getAllAlcohol()) {
            log("ALCOHOL_DEV " + a.getId() + " has addiction entry: " + AddictionMemory.getInstanceOrRegister().contains(a));
        }

        for (Ingredient ingredient : AlcoholRepo.INGREDIENT_MAP.values()) {
            log("ALCOHOL_DEV " + ingredient.id + " - " + Global.getSettings().getCommoditySpec(ingredient.id).getName());
        }
    }
}
