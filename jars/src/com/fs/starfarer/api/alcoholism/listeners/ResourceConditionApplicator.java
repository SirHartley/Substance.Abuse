package com.fs.starfarer.api.alcoholism.listeners;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.ModPlugin;
import com.fs.starfarer.api.alcoholism.memory.AlcoholRepo;
import com.fs.starfarer.api.alcoholism.memory.Ingredient;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.campaign.listeners.ListenerManagerAPI;
import com.fs.starfarer.api.campaign.listeners.PlayerColonizationListener;
import com.fs.starfarer.api.impl.campaign.econ.ResourceDepositsCondition;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

import java.util.Map;

import static com.fs.starfarer.api.alcoholism.ModPlugin.SETUP_KEY;

public class ResourceConditionApplicator implements PlayerColonizationListener, EconomyTickListener {

    public static final String COND_RESSOURCES = "alcohol_resourceCondition";

    public static void applyIngredientConditions(){
        Map<String, Object> persistentData = Global.getSector().getPersistentData();

        if (persistentData.containsKey(SETUP_KEY)) return;

        for (StarSystemAPI s : Global.getSector().getStarSystems()){
            if (!Misc.getMarketsInLocation(s).isEmpty()) continue;

            for (PlanetAPI p : s.getPlanets()){
                MarketAPI market = p.getMarket();
                if (market == null) continue;

                String planetType = p.getSpec().getPlanetType();

                ModPlugin.log(planetType);

                boolean hasFarmland = false;
                boolean hasOre = false;

                for (MarketConditionAPI condition : market.getConditions()) {
                    if (condition.getId().contains("farmland")) hasFarmland = true;
                    if (condition.getId().contains("ore") && condition.getPlugin() instanceof ResourceDepositsCondition) hasOre = true;
                }

                WeightedRandomPicker<String> picker = new WeightedRandomPicker<>();
                OUTER: for (Ingredient ingredient : AlcoholRepo.INGREDIENT_MAP.values()){
                    if (ingredient.id.equals("alcoholism_waste")) continue;

                    for (String forbidden : ingredient.forbiddenPlanetType) if (planetType.toLowerCase().contains(forbidden)) continue OUTER;
                    for (String required : ingredient.planetType) if (!planetType.toLowerCase().contains(required)) continue OUTER;

                    if (ingredient.industry.get(0).equals(Industries.FARMING) && !hasFarmland) continue;
                    if (ingredient.industry.get(0).equals(Industries.MINING) && !hasOre) continue;

                    picker.add(ingredient.id, (float) ingredient.rarity);


                    //ModPlugin.log("planet" + planetType + " " + ingredient.id + " " + ingredient.rarity + " farm " + hasFarmland + " ore " + hasOre);
                }

                if (picker.isEmpty()) continue;

                String commodity = picker.pick();
                market.addCondition(commodity);

                ModPlugin.log("Adding " + commodity + " ingredient to " + p.getName());
            }
        }

        persistentData.put(SETUP_KEY, true);
    }

    @Override
    public void reportPlayerColonizedPlanet(PlanetAPI planetAPI) {
        MarketAPI m = planetAPI.getMarket();
        applyRessourceCond(m);
    }

    @Override
    public void reportPlayerAbandonedColony(MarketAPI marketAPI) {

    }

    @Override
    public void reportEconomyTick(int i) {
        applyRessourceCondToAllMarkets();
    }

    @Override
    public void reportEconomyMonthEnd() {

    }

    private void applyRessourceCondToAllMarkets() {
        for (MarketAPI m : Global.getSector().getEconomy().getMarketsCopy()) {
            applyRessourceCond(m);
        }
    }

    private void applyRessourceCond(MarketAPI m) {
        if (m.isInEconomy() && !m.hasCondition(COND_RESSOURCES)) m.addCondition(COND_RESSOURCES);
    }

    //transient
    public static void register() {
        ListenerManagerAPI manager = Global.getSector().getListenerManager();
        if(!manager.hasListenerOfClass(ResourceConditionApplicator.class)) {
            ModPlugin.log("creating ResourceConditionApplicator instance");

            ResourceConditionApplicator listener = new ResourceConditionApplicator();
            manager.addListener(listener, true);
            listener.applyRessourceCondToAllMarkets();
        }
    }
}
