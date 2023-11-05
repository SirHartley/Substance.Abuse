package com.fs.starfarer.api.alcoholism_re.memory;

import java.util.List;

public class Ingredient {
    public String id;
    public List<String> industry;
    public List<String> planetType;
    public List<String> forbiddenPlanetType;
    public double rarity;
    public int type;
    public int strength;
    public int cost;
    public EffectData synergy;
    public EffectData effect;
    public EffectData drawback;
    public EffectData withdrawal;

    public Ingredient(String id, List<String> industry, List<String> planetType, List<String> forbiddenPlanetType, double rarity, int type, int strength, int cost, EffectData synergy, EffectData effect, EffectData drawback, EffectData withdrawal) {
        this.id = id;
        this.industry = industry;
        this.planetType = planetType;
        this.forbiddenPlanetType = forbiddenPlanetType;
        this.rarity = rarity;
        this.type = type;
        this.strength = strength;
        this.cost = cost;
        this.synergy = synergy;
        this.effect = effect;
        this.drawback = drawback;
        this.withdrawal = withdrawal;
    }

    public static class EffectData {
        public EffectData(String statModId, float effectMax, Mode effectMode) {
            this.statModId = statModId;
            this.effectMax = effectMax;
            this.effectMode = effectMode;
        }

        public enum Mode {
            MULT,
            PERCENT,
            FLAT
        }

        public String statModId;
        public float effectMax;
        public Mode effectMode;

        public static Mode toData (String s){
            for (Mode m : Mode.values()){
                if(m.toString().equalsIgnoreCase(s)) return m;
            }

            return null;
        }
    }

    // TODO: 18/03/2023 make a static method that translates the statmods into ship mods, as in: feed shipAPI instance and have apply depending on requ. level ect
}
