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

public class ModPlugin extends BaseModPlugin {

    public static final String SETUP_KEY = "alcohol_setup";

    public static void log(String Text) {
        if (Global.getSettings().isDevMode()) Global.getLogger(ModPlugin.class).info(Text);
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
        //FactionAlcoholHandler.assignFactionAlcohols(); deprecated
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
        CustomAlcohol alcohol1 = new CustomAlcohol("alcoholism_custom_01", null, "This is a test description entry override for a custom alcohol", "graphics/items/custom/custom1.png", "player",
                "alcoholism_trop_fruit", "alcoholism_fruit", "alcoholism_rare_botanics", "alcoholism_salt");
        CustomAlcohol alcohol2 = new CustomAlcohol("alcoholism_custom_02", null, "This is a test description entry override for a custom alcohol", "graphics/items/custom/custom1.png", "player",
                "alcoholism_trop_fruit", "alcoholism_fruit", "alcoholism_rare_botanics", "alcoholism_salt");
        CustomAlcohol alcohol3 = new CustomAlcohol("alcoholism_custom_03", null, "This is a test description entry override for a custom alcohol", "graphics/items/custom/custom1.png", "player",
                "alcoholism_trop_fruit", "alcoholism_fruit", "alcoholism_rare_botanics", "alcoholism_salt");
        CustomAlcohol alcohol4 = new CustomAlcohol("alcoholism_custom_04", null, "This is a test description entry override for a custom alcohol", "graphics/items/custom/custom1.png", "player",
                "alcoholism_trop_fruit", "alcoholism_fruit", "alcoholism_rare_botanics", "alcoholism_salt");
        CustomAlcohol alcohol5 = new CustomAlcohol("alcoholism_custom_05", null, "This is a test description entry override for a custom alcohol", "graphics/items/custom/custom1.png", "player",
                "alcoholism_trop_fruit", "alcoholism_fruit", "alcoholism_rare_botanics", "alcoholism_salt");
        CustomAlcohol alcohol6 = new CustomAlcohol("alcoholism_custom_06", null, "This is a test description entry override for a custom alcohol", "graphics/items/custom/custom1.png", "player",
                "alcoholism_trop_fruit", "alcoholism_fruit", "alcoholism_rare_botanics", "alcoholism_salt");
        CustomAlcohol alcohol7 = new CustomAlcohol("alcoholism_custom_07", null, "This is a test description entry override for a custom alcohol", "graphics/items/custom/custom1.png", "player",
                "alcoholism_trop_fruit", "alcoholism_fruit", "alcoholism_rare_botanics", "alcoholism_salt");
        CustomAlcohol alcohol8 = new CustomAlcohol("alcoholism_custom_08", null, "This is a test description entry override for a custom alcohol", "graphics/items/custom/custom1.png", "player",
                "alcoholism_trop_fruit", "alcoholism_fruit", "alcoholism_rare_botanics", "alcoholism_salt");
        CustomAlcohol alcohol9 = new CustomAlcohol("alcoholism_custom_09", null, "This is a test description entry override for a custom alcohol", "graphics/items/custom/custom1.png", "player",
                "alcoholism_trop_fruit", "alcoholism_fruit", "alcoholism_rare_botanics", "alcoholism_salt");
        CustomAlcohol alcohol10 = new CustomAlcohol("alcoholism_custom_10", null, "This is a test description entry override for a custom alcohol", "graphics/items/custom/custom1.png", "player",
                "alcoholism_trop_fruit", "alcoholism_fruit", "alcoholism_rare_botanics", "alcoholism_salt");
        CustomAlcohol alcohol11 = new CustomAlcohol("alcoholism_custom_11", null, "This is a test description entry override for a custom alcohol", "graphics/items/custom/custom1.png", "player",
                "alcoholism_trop_fruit", "alcoholism_fruit", "alcoholism_rare_botanics", "alcoholism_salt");
        CustomAlcohol alcohol12 = new CustomAlcohol("alcoholism_custom_12", null, "This is a test description entry override for a custom alcohol", "graphics/items/custom/custom1.png", "player",
                "alcoholism_trop_fruit", "alcoholism_fruit", "alcoholism_rare_botanics", "alcoholism_salt");
        CustomAlcohol alcohol13 = new CustomAlcohol("alcoholism_custom_13", null, "This is a test description entry override for a custom alcohol", "graphics/items/custom/custom1.png", "player",
                "alcoholism_trop_fruit", "alcoholism_fruit", "alcoholism_rare_botanics", "alcoholism_salt");
        CustomAlcohol alcohol14 = new CustomAlcohol("alcoholism_custom_14", null, "This is a test description entry override for a custom alcohol", "graphics/items/custom/custom1.png", "player",
                "alcoholism_trop_fruit", "alcoholism_fruit", "alcoholism_rare_botanics", "alcoholism_salt");
        CustomAlcohol alcohol15 = new CustomAlcohol("alcoholism_custom_15", null, "This is a test description entry override for a custom alcohol", "graphics/items/custom/custom1.png", "player",
                "alcoholism_trop_fruit", "alcoholism_fruit", "alcoholism_rare_botanics", "alcoholism_salt");
        CustomAlcohol alcohol16 = new CustomAlcohol("alcoholism_custom_16", null, "This is a test description entry override for a custom alcohol", "graphics/items/custom/custom1.png", "player",
                "alcoholism_trop_fruit", "alcoholism_fruit", "alcoholism_rare_botanics", "alcoholism_salt");
        CustomAlcohol alcohol17 = new CustomAlcohol("alcoholism_custom_17", null, "This is a test description entry override for a custom alcohol", "graphics/items/custom/custom1.png", "player",
                "alcoholism_trop_fruit", "alcoholism_fruit", "alcoholism_rare_botanics", "alcoholism_salt");
        CustomAlcohol alcohol18 = new CustomAlcohol("alcoholism_custom_18", null, "This is a test description entry override for a custom alcohol", "graphics/items/custom/custom1.png", "player",
                "alcoholism_trop_fruit", "alcoholism_fruit", "alcoholism_rare_botanics", "alcoholism_salt");
        CustomAlcohol alcohol19 = new CustomAlcohol("alcoholism_custom_19", null, "This is a test description entry override for a custom alcohol", "graphics/items/custom/custom1.png", "player",
                "alcoholism_trop_fruit", "alcoholism_fruit", "alcoholism_rare_botanics", "alcoholism_salt");
        CustomAlcohol alcohol20 = new CustomAlcohol("alcoholism_custom_20", null, "This is a test description entry override for a custom alcohol", "graphics/items/custom/custom1.png", "player",
                "alcoholism_trop_fruit", "alcoholism_fruit", "alcoholism_rare_botanics", "alcoholism_salt");
        CustomAlcohol alcohol21 = new CustomAlcohol("alcoholism_custom_21", null, "This is a test description entry override for a custom alcohol", "graphics/items/custom/custom1.png", "player",
                "alcoholism_trop_fruit", "alcoholism_fruit", "alcoholism_rare_botanics", "alcoholism_salt");
        CustomAlcohol alcohol22 = new CustomAlcohol("alcoholism_custom_22", null, "This is a test description entry override for a custom alcohol", "graphics/items/custom/custom1.png", "player",
                "alcoholism_trop_fruit", "alcoholism_fruit", "alcoholism_rare_botanics", "alcoholism_salt");
        CustomAlcohol alcohol23 = new CustomAlcohol("alcoholism_custom_23", null, "This is a test description entry override for a custom alcohol", "graphics/items/custom/custom1.png", "player",
                "alcoholism_trop_fruit", "alcoholism_fruit", "alcoholism_rare_botanics", "alcoholism_salt");
        CustomAlcohol alcohol24 = new CustomAlcohol("alcoholism_custom_24", null, "This is a test description entry override for a custom alcohol", "graphics/items/custom/custom1.png", "player",
                "alcoholism_trop_fruit", "alcoholism_fruit", "alcoholism_rare_botanics", "alcoholism_salt");
        CustomAlcohol alcohol25 = new CustomAlcohol("alcoholism_custom_25", null, "This is a test description entry override for a custom alcohol", "graphics/items/custom/custom1.png", "player",
                "alcoholism_trop_fruit", "alcoholism_fruit", "alcoholism_rare_botanics", "alcoholism_salt");
        CustomAlcohol alcohol26 = new CustomAlcohol("alcoholism_custom_26", null, "This is a test description entry override for a custom alcohol", "graphics/items/custom/custom1.png", "player",
                "alcoholism_trop_fruit", "alcoholism_fruit", "alcoholism_rare_botanics", "alcoholism_salt");
        CustomAlcohol alcohol27 = new CustomAlcohol("alcoholism_custom_27", null, "This is a test description entry override for a custom alcohol", "graphics/items/custom/custom1.png", "player",
                "alcoholism_trop_fruit", "alcoholism_fruit", "alcoholism_rare_botanics", "alcoholism_salt");
        CustomAlcohol alcohol28 = new CustomAlcohol("alcoholism_custom_28", null, "This is a test description entry override for a custom alcohol", "graphics/items/custom/custom1.png", "player",
                "alcoholism_trop_fruit", "alcoholism_fruit", "alcoholism_rare_botanics", "alcoholism_salt");
        CustomAlcohol alcohol29 = new CustomAlcohol("alcoholism_custom_29", null, "This is a test description entry override for a custom alcohol", "graphics/items/custom/custom1.png", "player",
                "alcoholism_trop_fruit", "alcoholism_fruit", "alcoholism_rare_botanics", "alcoholism_salt");
        CustomAlcohol alcohol30 = new CustomAlcohol("alcoholism_custom_30", null, "This is a test description entry override for a custom alcohol", "graphics/items/custom/custom1.png", "player",
                "alcoholism_trop_fruit", "alcoholism_fruit", "alcoholism_rare_botanics", "alcoholism_salt");

        alcohol1.register();
        alcohol1.init();
        alcohol2.register();
        alcohol2.init();
        alcohol3.register();
        alcohol3.init();
        alcohol4.register();
        alcohol4.init();
        alcohol5.register();
        alcohol5.init();
        alcohol6.register();
        alcohol6.init();
        alcohol7.register();
        alcohol7.init();
        alcohol8.register();
        alcohol8.init();
        alcohol9.register();
        alcohol9.init();
        alcohol10.register();
        alcohol10.init();
        alcohol11.register();
        alcohol11.init();
        alcohol12.register();
        alcohol12.init();
        alcohol13.register();
        alcohol13.init();
        alcohol14.register();
        alcohol14.init();
        alcohol15.register();
        alcohol15.init();
        alcohol16.register();
        alcohol16.init();
        alcohol17.register();
        alcohol17.init();
        alcohol18.register();
        alcohol18.init();
        alcohol19.register();
        alcohol19.init();
        alcohol20.register();
        alcohol20.init();
        alcohol21.register();
        alcohol21.init();
        alcohol22.register();
        alcohol22.init();
        alcohol23.register();
        alcohol23.init();
        alcohol24.register();
        alcohol24.init();
        alcohol25.register();
        alcohol25.init();
        alcohol26.register();
        alcohol26.init();
        alcohol27.register();
        alcohol27.init();
        alcohol28.register();
        alcohol28.init();
        alcohol29.register();
        alcohol29.init();
        alcohol30.register();
        alcohol30.init();
    }

    public static void devActions() {
        CargoAPI c = Global.getSector().getPlayerFleet().getCargo();
        for (AlcoholAPI baseAlcohol : AlcoholRepo.ALCOHOL_MAP.values()) {
            c.addCommodity(baseAlcohol.getCommodityId(), 100);
        }

        for (AlcoholAPI a : AlcoholRepo.ALCOHOL_MAP.values()) {
            log("ALCOHOL_DEV " + a.getCommodityId() + " has addiction entry: " + AddictionMemory.getInstanceOrRegister().contains(a) + AddictionMemory.getInstanceOrRegister().getStatusForId(a.getId()));
        }

        for (Ingredient ingredient : AlcoholRepo.INGREDIENT_MAP.values()) {
            log("ALCOHOL_DEV " + ingredient.id + " - " + Global.getSettings().getCommoditySpec(ingredient.id).getName());
        }
    }
}
