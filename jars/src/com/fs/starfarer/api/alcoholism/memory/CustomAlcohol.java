package com.fs.starfarer.api.alcoholism.memory;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.ModPlugin;
import com.fs.starfarer.api.campaign.SpecialItemSpecAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * id = custom item ID this uses, goes up to 100
 * uid = actual alcohol ID used to track what this is
 */

public class CustomAlcohol extends BaseAlcohol {
    //4 slots for ingredients
    //need 1 ingredient for alcohol
    //base ingredient effect is 50%
    //if multiple of same type are used, increase by x1.5 for each similar type, stacking (4 same ingredients is x1,5^3)
    //if multiple of not same type, increase synergy effect by 1.25 for each type

    public List<Effect> ingredients = new ArrayList<>();
    public float cost;
    public String name;
    public String desc;
    public String iconName;
    public String shortDesc;

    public String uid;
    public boolean hidden = false;
    public boolean spoiled = false; //if the id set was used for another alcohol, this one becomes spoiled.

    public static final float BASE_INGREDIENT_STRENGTH = 0.05f;

    public CustomAlcohol(String id, String name, String desc, String iconName, String factionIdForColours,
                         String... ingredients) {

        this.uid = Misc.genUID();

        this.id = id;
        this.commodityId = id + "_c";
        this.factionId = factionIdForColours;
        this.iconName = iconName;

        this.name = name == null ? AlcoholRepo.CUSTOM_ALCOHOL_NAME_LIST.get(MathUtils.getRandomNumberInRange(0, AlcoholRepo.CUSTOM_ALCOHOL_NAME_LIST.size() - 1)) : name;
        this.desc = desc;

        loadIcon();
        generateShortDesc();
        calculateEffects(ingredients);
    }

    public void init() {
        loadIcon();
        generateShortDesc();
        AlcoholRepo.ALCOHOL_MAP.put(id, this);
        AddictionMemory.getInstanceOrRegister().addIfNeeded(this);
        overwriteSpec();
    }

    public void register() {
        CustomAlcoholMemory.getInstanceOrRegister().add(this);
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

    public void overwriteSpec() {
        //then we do commodity spec
        CommoditySpecAPI spec = Global.getSettings().getCommoditySpec(getCommodityId());
        spec.setName(name);
        spec.setBasePrice(cost);
        spec.setIconName(iconName);

        //then we do the actual special item
        SpecialItemSpecAPI specialItemSpec = Global.getSettings().getSpecialItemSpec(getId());
        //specialItemSpec.setDesc(desc);
        specialItemSpec.setName(name);
        specialItemSpec.setBasePrice(cost);
        specialItemSpec.setIconName(iconName);

        Description description = Global.getSettings().getDescription(commodityId, Description.Type.RESOURCE);
        description.setText1(desc);
        description.setText2(shortDesc);
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
}
