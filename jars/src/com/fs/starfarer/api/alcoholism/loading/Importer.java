package com.fs.starfarer.api.alcoholism.loading;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.ModPlugin;
import com.fs.starfarer.api.alcoholism.memory.Ingredient;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

public class Importer {
    public static final int NUM_ALCOHOL_IMAGES = 105;

    public static List<String> loadCustomAlcoholNames() {
        List<String> nameList = new ArrayList<>();
        try {
            JSONArray config = Global.getSettings().getMergedSpreadsheetDataForMod("name", "data/strings/custom_alcohol_names.csv", "alcoholism");
            for (int i = 0; i < config.length(); i++) {
                JSONObject row = config.getJSONObject(i);

                String name = row.getString("name");
                nameList.add(name);
            }

        } catch (IOException | JSONException ex) {
            Global.getLogger(Importer.class).error("Could not find Substance.Abuse custom_alcohol_names.csv, or something is wrong with the data format.", ex);
        }

        return nameList;
    }

    public static List<String> getCustomAlcoholImageNames(){
        Map<String, Object> mem = Global.getSector().getPersistentData();

        if (mem.containsKey("$customAlcoholImageList")) return (List<String>) mem.get("$customAlcoholImageList");

        List<String> imageList = new LinkedList<>();
        for (int i = 1; i <= NUM_ALCOHOL_IMAGES; i++){
            try {
                Global.getSettings().loadTexture("graphics/items/custom/custom" + i + ".png");
                imageList.add("graphics/items/custom/custom" + i + ".png");
            } catch (IOException e){
                ModPlugin.log("could not load texture for custom alcohol " + i + " at " + "graphics/items/custom/custom" + i + ".png");
            }
        }

        mem.put("$customAlcoholImageList", imageList);

        return imageList;
    }

    public static Map<String, Ingredient> loadIngredientData() {
        Map<String, Ingredient> ingredientMap = new HashMap<>();

        try {
            JSONArray config = Global.getSettings().getMergedSpreadsheetDataForMod("id", "data/campaign/ingredients.csv", "alcoholism");
            for (int i = 0; i < config.length(); i++) {
                JSONObject row = config.getJSONObject(i);

                String id = row.getString("id");
                List<String> industry = new ArrayList<>();

                for (String s : row.getString("industry").replaceAll("\\s", "").split(",")) {
                    industry.add(s.substring(1));
                }

                List<String> requiredPlanetType = new ArrayList<>();
                List<String> forbiddenPlanetTypes = new ArrayList<>();

                for (String s : row.getString("planet_type").replaceAll("\\s", "").split(",")) {
                    if (s.startsWith("!")) forbiddenPlanetTypes.add(s.substring(1));
                    else requiredPlanetType.add(s);
                }

                double rarity = row.getDouble("rarity_mod");
                int type = row.getInt("type");
                int strength = row.getInt("strength");
                int cost = row.getInt("cost");

                Ingredient.EffectData synergy = new Ingredient.EffectData(row.getString("synergy"), (float) row.getDouble("synergy_max"), Ingredient.EffectData.toData(row.getString("synergy_mode")));
                Ingredient.EffectData effect = new Ingredient.EffectData(row.getString("effect"), (float) row.getDouble("effect_max"), Ingredient.EffectData.toData(row.getString("effect_mode")));
                Ingredient.EffectData drawback = new Ingredient.EffectData(row.getString("drawback"), (float) row.getDouble("drawback_max"), Ingredient.EffectData.toData(row.getString("drawback_mode")));
                Ingredient.EffectData withdrawal = new Ingredient.EffectData(row.getString("withdrawal"), (float) row.getDouble("withdrawal_max"), Ingredient.EffectData.toData(row.getString("withdrawal_mode")));

                Ingredient ingredient = new Ingredient(id, industry, requiredPlanetType, forbiddenPlanetTypes,
                        rarity, type, strength, cost, synergy, effect, drawback, withdrawal);

                ingredientMap.put(id, ingredient);
            }
        } catch (IOException | JSONException ex) {
            Global.getLogger(Importer.class).error("Could not find Substance.Abuse ingredients.csv, or something is wrong with the data format.", ex);
        }

        return ingredientMap;
    }
}
