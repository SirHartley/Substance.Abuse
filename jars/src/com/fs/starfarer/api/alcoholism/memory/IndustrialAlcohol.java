package com.fs.starfarer.api.alcoholism.memory;

//this is the new default alcohol
public class IndustrialAlcohol extends BaseAlcohol {

    private String industryItemId;
    private String[] demandsForProduction;
    private int lightIndustryMod;
    private int populationImportMod;

    //light industry mod (both import and export)
    //population import mod

    public IndustrialAlcohol(String id, float mult, String factionIdForColours,
                             int lightIndustryMod, int populationImportMod,
                             String... demandsForProduction) {
        this.id = id;
        this.commodityId = id + "_c";
        this.industryItemId = id + "_item";
        this.mult = mult;
        this.factionId = factionIdForColours;
        this.lightIndustryMod = lightIndustryMod;
        this.populationImportMod = populationImportMod;
        this.demandsForProduction = demandsForProduction;
    }

    public String getIndustryItemId() {
        return industryItemId;
    }

    public String[] getDemandsForProduction() {
        return demandsForProduction;
    }

    public int getLightIndustryMod() {
        return lightIndustryMod;
    }

    public int getPopulationImportMod() {
        return populationImportMod;
    }
}
