package com.fs.starfarer.api.alcoholism.memory;

import com.fs.starfarer.api.alcoholism.ModPlugin;
import com.fs.starfarer.api.alcoholism.itemPlugins.RecipeBookInstallableItemEffect;
import com.fs.starfarer.api.alcoholism.itemPlugins.RecipeBookItemPlugin;
import com.fs.starfarer.api.alcoholism.itemPlugins.RecipeInstallableItemEffect;
import com.fs.starfarer.api.alcoholism.loading.Importer;
import com.fs.starfarer.api.impl.campaign.econ.impl.ItemEffectsRepo;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlcoholRepo {

    //Alcohol ID MUST match specialItem and hullmod ID
    public static final String STOUT = "alcoholism_stout";
    public static final String ABSYNTH = "alcoholism_absynth";
    public static final String TEARS = "alcoholism_tears";
    public static final String FUEL = "alcoholism_fuel";
    public static final String BLOOD = "alcoholism_blood";
    public static final String SUNSHINE = "alcoholism_sunshine";
    public static final String KING = "alcoholism_king";
    public static final String FREEDOM = "alcoholism_freedom";
    public static final String WATER = "alcoholism_water";
    public static final String TEA = "alcoholism_tea";

    public static Map<String, AlcoholAPI> ALCOHOL_MAP = new HashMap<String, AlcoholAPI>(){{
        put(STOUT, new IndustrialAlcohol(STOUT, 1.2f, Factions.HEGEMONY, -3, -2, new String[]{Commodities.FOOD, Commodities.ORGANICS}));
        put(ABSYNTH, new IndustrialAlcohol(ABSYNTH, 1.2f, Factions.TRITACHYON,  -3, -2, new String[]{Commodities.ORGANICS}));
        put(TEARS, new IndustrialAlcohol(TEARS, 1.35f, Factions.LUDDIC_CHURCH,  -3, -2, new String[]{Commodities.FOOD}));
        put(FUEL, new IndustrialAlcohol(FUEL, 1.45f, Factions.LUDDIC_PATH,  -3, -2, new String[]{Commodities.FUEL, Commodities.SUPPLIES}));
        put(BLOOD, new IndustrialAlcohol(BLOOD, 1.45f, Factions.PIRATES,  -3, -2, new String[]{Commodities.DRUGS, Commodities.ORGANICS}));
        put(SUNSHINE, new IndustrialAlcohol(SUNSHINE, 1.25f, Factions.DIKTAT,  -3, -2, new String[]{Commodities.FOOD, Commodities.ORGANICS}));
        put(KING, new IndustrialAlcohol(KING, 1.60f, Factions.PERSEAN,  -3, -2, new String[]{Commodities.FOOD, Commodities.ORGANICS}));
        put(FREEDOM, new IndustrialAlcohol(FREEDOM, 1.80f, Factions.INDEPENDENT,  -3, -2, new String[]{Commodities.FOOD, Commodities.ORGANICS}));
        put(WATER, new IndustrialAlcohol(WATER, 2f, Factions.INDEPENDENT, -4, -4,  new String[]{Commodities.DRUGS}));
        put(TEA, new IndustrialAlcohol(TEA, 1.25f, Factions.INDEPENDENT, -5, -5,  new String[]{Commodities.FOOD}));
    }};

    public static Map<String, Ingredient> INGREDIENT_MAP = new HashMap<>();
    public static List<String> CUSTOM_ALCOHOL_NAME_LIST = Importer.loadCustomAlcoholNames();

    public static List<AlcoholAPI> getNonCustomAlcoholList(){
        List<AlcoholAPI> menu = new ArrayList<>();
        for (AlcoholAPI alcohol : ALCOHOL_MAP.values()) if (!(alcohol instanceof CustomAlcohol)) menu.add(alcohol);
        return menu;
    }

    public static List<CustomAlcohol> getCustomAlcoholList(){
        List<CustomAlcohol> menu = new ArrayList<>();
        for (AlcoholAPI alcohol : ALCOHOL_MAP.values()) if (alcohol instanceof CustomAlcohol) menu.add((CustomAlcohol) alcohol);
        return menu;
    }

    public static AlcoholAPI get(String id){
        return AlcoholRepo.ALCOHOL_MAP.get(id);
    }

    public static boolean isAlcohol(String id) {
        String normalizedId = id.contains("_c") ? convertID(id) : id;
        return ALCOHOL_MAP.containsKey(normalizedId);
    }

    public static String convertID(String id) {
        if (id.contains("_c")) {
            String s = id.substring(0, id.length() - 2);
            ModPlugin.log("converting " + id + " to " + s);
            return s;
        }
        else return id + "_c";
    }

    public static void addBreweryRecipesToItemEffectRepo(){
        for (AlcoholAPI alcohol : getNonCustomAlcoholList()){
            ItemEffectsRepo.ITEM_EFFECTS.put(alcohol.getIndustryItemId(), new RecipeInstallableItemEffect(alcohol.getId()));
        }
    }

    public static void addRecipeBookToItemEffectRepo(){
        ItemEffectsRepo.ITEM_EFFECTS.put(RecipeBookItemPlugin.RECIPE_BOOK_ID, new RecipeBookInstallableItemEffect(RecipeBookItemPlugin.RECIPE_BOOK_ID));
    }

    public static void loadIngredients(){
        AlcoholRepo.INGREDIENT_MAP = Importer.loadIngredientData();
    }
}
