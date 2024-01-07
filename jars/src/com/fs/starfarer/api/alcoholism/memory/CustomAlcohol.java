package com.fs.starfarer.api.alcoholism.memory;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.ModPlugin;
import com.fs.starfarer.api.campaign.SpecialItemSpecAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.util.Misc;
import org.codehaus.janino.Mod;
import org.lazywizard.lazylib.MathUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class CustomAlcohol extends BaseAlcohol {
    //4 slots for ingredients
    //need 1 ingredient for alcohol
    //base ingredient effect is 50%
    //if multiple of same type are used, increase by x1.5 for each similar type, stacking (4 same ingredients is x1,5^3)
    //if multiple of not same type, increase synergy effect by 1.25 for each type

    public static final String CUSTOM_ALCOHOL_ITEM_ID = "alcoholism_custom";
    public static final String DEFAULT_EFFECT_HULLMOD = "alcoholism_custom_default";

    public List<Effect> ingredients = new ArrayList<>();
    public float cost;
    public String name;
    public String desc;
    public String iconName;
    public String shortDesc;
    public String effectHullmodId = DEFAULT_EFFECT_HULLMOD;

    public boolean hidden = false;

    public static final float BASE_INGREDIENT_STRENGTH = 0.05f;

    public CustomAlcohol(String name, String desc, String iconName, String factionIdForColours,
                         String... ingredients) {

        this.id = Misc.genUID();
        this.factionId = factionIdForColours;
        this.iconName = iconName;
        this.commodityId = CUSTOM_ALCOHOL_ITEM_ID;

        this.name = name == null ? AlcoholRepo.CUSTOM_ALCOHOL_NAME_LIST.get(MathUtils.getRandomNumberInRange(0, AlcoholRepo.CUSTOM_ALCOHOL_NAME_LIST.size() - 1)) : name;
        this.desc = desc;

        loadIcon();
        generateShortDesc();
        calculateEffects(ingredients);
    }

    public void init() {
        loadIcon();
        generateShortDesc();
        AlcoholRepo.add(this);
        AddictionMemory.getInstanceOrRegister().addIfNeeded(this);

        ModPlugin.log("Initializing custom Alcohol " + getId() + ", confirming presence in repo: " + (AlcoholRepo.get(id) != null));
    }

    public void loadIcon() {
        try {
            Global.getSettings().loadTexture(iconName);
        } catch (IOException e) {
            ModPlugin.log("FAILED TO LOAD ALCOHOL TEXTURE, DEFAULTING");
            iconName = "graphics/items/phoenix_stout.png";
        }
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public void calculateEffects(String[] ingredients) {
        float baseSynergyMult = Global.getSettings().getFloat("INGREDIENT_SYNERGY_MULT");
        float baseEffectMult = Global.getSettings().getFloat("INGREDIENT_EFFECT_MULT");
        float addictionMult = 1f;
        float cost = Global.getSettings().getInt("CUSTOM_ALCOHOL_BASE_COST");

        List<Ingredient> ingredientList = new ArrayList<>();
        for (String id : ingredients) ingredientList.add(AlcoholRepo.INGREDIENT_MAP.get(id));

        for (Ingredient ingredient : new ArrayList<>(ingredientList)) {
            addictionMult += ingredient.strength * BASE_INGREDIENT_STRENGTH; //total addiction mult
            cost += Global.getSettings().getInt("CUSTOM_ALCOHOL_BASE_COST_INCREASE") * ingredient.cost; //costs

            float effectStrength = Global.getSettings().getFloat("INGREDIENT_BASE_EFFECT");
            float synergyStrength = Global.getSettings().getFloat("INGREDIENT_BASE_SYNERGY");
            ;

            for (Ingredient ingredient2 : ingredientList) {
                if (ingredient.id.equals(ingredient2.id)) continue;

                if (ingredient.type == ingredient2.type) effectStrength *= baseEffectMult;
                else synergyStrength *= baseSynergyMult;
            }

            this.ingredients.add(new Effect(ingredient.id, effectStrength, synergyStrength));
        }

        this.mult = addictionMult;
        this.cost = cost;

        ModPlugin.log("new custom alcohol " + id + " " + name + " " + factionId + " " + " | mult " + mult + " cost " + cost);
    }

    @Override
    public String getName() {
        return name;
    }

    public void generateShortDesc() {
        //todo adjust to reflect effects once they are in
        //todo add adjectives "Fresh Herbs, Zesty Malt..."

        StringBuilder ingredientString = new StringBuilder();

        int i = 0;
        for (Effect effect : ingredients) {
            if (i > 0) ingredientString.append(", ");
            ingredientString.append(Global.getSettings().getCommoditySpec(effect.ingredientId).getName());
            i++;
        }

        if (ingredientString.length() <= 0)
            ingredientString.append("[Ingredient list malformed, re-check import validity]");

        this.shortDesc = "Test custom alcohol made from " + ingredientString.toString();
    }

    @Override
    public String getIndustryItemId() {
        return null;
    }

    @Override
    public String[] getDemandsForProduction() {
        return new String[0];
    }

    @Override
    public int getLightIndustryMod() {
        return 0;
    }

    @Override
    public int getPopulationImportMod() {
        return 0;
    }

    public static class Effect {
        String ingredientId;
        float synergyStrength;
        float effectStrength;

        public Effect(String ingredientId, float synergyStrength, float effectStrength) {
            this.ingredientId = ingredientId;
            this.synergyStrength = synergyStrength;
            this.effectStrength = effectStrength;
        }
    }

    @Override
    public String getEffectHullmodId() {
        if (AlcoholRepo.getActiveCustomAlcoholList().contains(this) && effectHullmodId.equals(DEFAULT_EFFECT_HULLMOD)) {
            effectHullmodId = getNextFreeHullmodId();
        } else if (!AlcoholRepo.getActiveCustomAlcoholList().contains(this) && !effectHullmodId.equals(DEFAULT_EFFECT_HULLMOD)) effectHullmodId = DEFAULT_EFFECT_HULLMOD;

        return effectHullmodId;
    }

    public String getNextFreeHullmodId(){
        List<String> takenIds = new ArrayList<>();
        List<String> available = new ArrayList<>(Arrays.asList("alcoholism_custom_1", "alcoholism_custom_2", "alcoholism_custom_3"));

        for (AlcoholAPI alcohol : AlcoholRepo.getActiveCustomAlcoholList()) if (!((CustomAlcohol) alcohol).effectHullmodId.equals(DEFAULT_EFFECT_HULLMOD)) takenIds.add(((CustomAlcohol) alcohol).effectHullmodId);

        String hullmodId = DEFAULT_EFFECT_HULLMOD;
        available.removeAll(takenIds);
        if (!available.isEmpty()) hullmodId = available.get(0);

        return hullmodId;
    }

    @Override
    public String getDesc() {
        return desc;
    }

    @Override
    public String getShortDesc() {
        return shortDesc;
    }
}
