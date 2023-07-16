package com.fs.starfarer.api.alcoholism.memory;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.ModPlugin;
import com.fs.starfarer.api.campaign.SpecialItemSpecAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.loading.X;

import java.util.ArrayList;
import java.util.List;

public class CustomAlcohol extends BaseAlcohol{
    //4 slots for ingredients
    //need 1 ingredient for alcohol
    //base ingredient effect is 50%
    //if multiple of same type are used, increase by x1.5 for each similar type, stacking (4 same ingredients is x1,5^3)
    //if multiple of not same type, increase synergy effect by 1.25 for each type

    public List<Effect> ingredients = new ArrayList<>();
    public float cost;
    public String name;
    public String desc;

    public static final float BASE_INGREDIENT_STRENGTH = 0.05f;

    public CustomAlcohol(String id, String name, String desc, String factionIdForColours,
                             String... ingredients) {
        this.id = id;
        this.commodityId = id + "_c";
        this.factionId = factionIdForColours;

        this.name = name;
        this.desc = desc;

        calculateEffects(ingredients);
    }

    public void register(){
        overwriteSpec();
        AlcoholRepo.ALCOHOL_MAP.put(id, this);
        CustomAlcoholMemory.getInstanceOrRegister().add(this);
        AddictionMemory.getInstanceOrRegister().addIfNeeded(this);
    }

    public void calculateEffects(String[] ingredients){
        float baseSynergyMult = Global.getSettings().getFloat("INGREDIENT_SYNERGY_MULT");
        float baseEffectMult = Global.getSettings().getFloat("INGREDIENT_EFFECT_MULT");
        float addictionMult = 1f;
        float cost = Global.getSettings().getInt("CUSTOM_ALCOHOL_BASE_COST");

        List<Ingredient> ingredientList = new ArrayList<>();
        for (String id : ingredients) ingredientList.add(AlcoholRepo.INGREDIENT_MAP.get(id));

        for (Ingredient ingredient : new ArrayList<>(ingredientList)){
            addictionMult += ingredient.strength * BASE_INGREDIENT_STRENGTH; //total addiction mult
            cost += Global.getSettings().getInt("CUSTOM_ALCOHOL_BASE_COST_INCREASE") * ingredient.cost; //costs

            float effectStrength = Global.getSettings().getFloat("INGREDIENT_BASE_EFFECT");
            float synergyStrength = Global.getSettings().getFloat("INGREDIENT_BASE_SYNERGY");;

            for (Ingredient ingredient2 : ingredientList){
                if (ingredient.id.equals(ingredient2.id)) continue;

                if (ingredient.type == ingredient2.type) effectStrength *= baseEffectMult;
                else synergyStrength *= baseSynergyMult;
            }

            this.ingredients.add(new Effect(ingredient.id, effectStrength, synergyStrength));
        }

        this.mult = addictionMult;
        this.cost = cost;

        ModPlugin.log("new custom alcohol "+ id + " " + name + " " + factionId + " " + " | mult " + mult + " cost " + cost);

    }

    public void overwriteSpec(){
        /*//specialItemSpec for the commodity first cause it's getting stored by commodity spec
        SpecialItemSpecAPI specialItemCommoditySpec = Global.getSettings().getSpecialItemSpec(getCommodityId());
        specialItemCommoditySpec.setDesc(desc);
        ((X) specialItemCommoditySpec).setName(name);*/

        //then we do commodity spec
        CommoditySpecAPI spec = Global.getSettings().getCommoditySpec(getCommodityId());
        spec.setBasePrice(cost);

        Global.getSettings().getDescription(id, Description.Type.RESOURCE).setText1("THIS IS A TEST");

        //then we do the actual special item
        SpecialItemSpecAPI specialItemSpec = Global.getSettings().getSpecialItemSpec(getId());
        specialItemSpec.setDesc(desc);
        ((X) specialItemSpec).setName(name);
        ((X) specialItemSpec).setBasePrice(cost);
    }

    public static class Effect{
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
