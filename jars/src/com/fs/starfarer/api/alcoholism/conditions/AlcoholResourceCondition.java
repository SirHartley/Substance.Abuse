package com.fs.starfarer.api.alcoholism.conditions;

import com.fs.starfarer.api.alcoholism.industry.Brewery;
import com.fs.starfarer.api.alcoholism.memory.Alcohol;
import com.fs.starfarer.api.alcoholism.memory.AlcoholRepo;
import com.fs.starfarer.api.alcoholism.memory.FactionAlcoholHandler;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MutableCommodityQuantity;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class AlcoholResourceCondition extends BaseMarketConditionPlugin {

    private static final String LEGACY_CLEARED_KEY = "$alcoholism_legacy_economy_cleared";

    @Override
    public void apply(String id) {
        super.apply(id);

        if (market.isPlayerOwned()) {
            applyPlayerAlcohol();
            return;
        }

        applyAllNPCAlcohol();
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);

        clearLegacyEntries();

        BaseIndustry pop = getBaseIndustry(Industries.POPULATION);
        BaseIndustry lightIndustry = getBaseIndustry(Industries.LIGHTINDUSTRY);

        for (Alcohol alcohol : AlcoholRepo.ALCOHOL_MAP.values()) {
            String commodityId = alcohol.getCommodityId();
            if (pop != null) pop.demand(getModId(), commodityId, 0, "");
            if (lightIndustry == null) continue;

            lightIndustry.supply(getModId(), commodityId, 0, "");
            lightIndustry.supply(getDeficitModId(), commodityId, 0, "");
            for (String s : alcohol.getDemandsForProduction()) lightIndustry.demand(getModId(), s, 0, "");
        }
    }

    // up to 1.1.d drink supply and demand sat under the industry's own index 0 id and deficit id "3"
    private void clearLegacyEntries() {
        MemoryAPI memory = market.getMemoryWithoutUpdate();
        if (memory.getBoolean(LEGACY_CLEARED_KEY)) return;
        memory.set(LEGACY_CLEARED_KEY, true);

        BaseIndustry pop = getBaseIndustry(Industries.POPULATION);
        BaseIndustry lightIndustry = getBaseIndustry(Industries.LIGHTINDUSTRY);

        for (Alcohol alcohol : AlcoholRepo.ALCOHOL_MAP.values()) {
            String commodityId = alcohol.getCommodityId();
            if (pop != null) pop.demand(getIndustryModId(pop), commodityId, 0, "");
            if (lightIndustry == null) continue;

            lightIndustry.supply(getIndustryModId(lightIndustry), commodityId, 0, "");
            lightIndustry.supply("3", commodityId, 0, "");
            for (String s : alcohol.getDemandsForProduction()) {
                // vanilla light industry sets its own organics demand under the same id
                if (!Commodities.ORGANICS.equals(s)) lightIndustry.demand(getIndustryModId(lightIndustry), s, 0, "");
            }
        }
    }

    private static String getIndustryModId(Industry ind) {
        return "ind_" + ind.getId() + "_0";
    }

    private BaseIndustry getBaseIndustry(String industryId) {
        Industry ind = market.getIndustry(industryId);
        return ind instanceof BaseIndustry ? (BaseIndustry) ind : null;
    }

    private void applyPlayerAlcohol() {
        List<String> exports = getPlayerBreweryExports();

        for (String s : exports) {
            applyDemand(Industries.POPULATION, -2, s);
        }

        if (!exports.isEmpty())
            applyDemand(Industries.POPULATION, -4, AlcoholRepo.get(AlcoholRepo.WATER).getCommodityId());
    }

    private List<String> getPlayerBreweryExports() {
        List<String> commodityList = new ArrayList<>();

        for (MarketAPI m : Misc.getPlayerMarkets(true)) {
            if (m.getId().equals(market.getId())) continue;
            if (m.hasIndustry(Brewery.INDUSTRY_ID)) {
                for (MutableCommodityQuantity q : m.getIndustry(Brewery.INDUSTRY_ID).getAllSupply()) {
                    commodityList.add(q.getCommodityId());
                }
            }
        }

        return commodityList;
    }

    private void applyAllNPCAlcohol() {
        for (Alcohol alcohol : getAlcoholForMarket()) {
            applyNPCAlcohol(alcohol);
        }

        if (market.getFactionId().equals(Factions.INDEPENDENT))
            applyNPCAlcohol(AlcoholRepo.get(AlcoholRepo.WATER)); //everyone loves heroin
        if (market.getSize() > 5)
            applyDemand(Industries.POPULATION, AlcoholRepo.get(AlcoholRepo.TEA).getPopulationImportMod(), AlcoholRepo.get(AlcoholRepo.TEA).getCommodityId()); //tea demand to large worlds
    }

    private void applyNPCAlcohol(Alcohol alcohol) {
        String commodityID = alcohol.getCommodityId();

        if (!market.hasIndustry(Brewery.INDUSTRY_ID)) {
            applyDemand(Industries.LIGHTINDUSTRY, alcohol.getLightIndustryMod(), alcohol.getDemandsForProduction());
            applySupply(Industries.LIGHTINDUSTRY, alcohol.getLightIndustryMod(), commodityID);
        }

        applyDemand(Industries.POPULATION, alcohol.getPopulationImportMod(), commodityID);
    }

    private void applyDemand(String industryID, int marketSizeMod, String... commodityIDs) {
        BaseIndustry ind = getBaseIndustry(industryID);
        if (ind == null) return;
        int amt = Math.max(0, market.getSize() + marketSizeMod);

        for (String s : commodityIDs) {
            //don't demand anyhing that the industry supplies in higher numbers
            if (ind.getSupply(s).getQuantity().getModifiedInt() >= amt) continue;
            ind.demand(getModId(), s, amt, BaseIndustry.BASE_VALUE_TEXT);
        }
    }

    private void applySupply(String industryID, int marketSizeMod, String... commodityIDs) {
        BaseIndustry ind = getBaseIndustry(industryID);
        if (ind == null) return;
        int amt = Math.max(0, market.getSize() + marketSizeMod);

        for (String s : commodityIDs) {
            ind.supply(getModId(), s, amt, BaseIndustry.BASE_VALUE_TEXT);
            applyDeficitToProduction(ind, getDeficitModId(), getMaxDeficit(ind), s);
        }
    }

    public static void applyDemand(BaseIndustry ind, String id, int amt) {
        if (amt == 0) return;
        ind.demand(id, amt);
    }

    public static void applySupply(BaseIndustry ind, String commodityId, int amt) {
        if (amt == 0) return;
        ind.supply(commodityId, amt);
        applyDeficitToProduction(ind, "3", getMaxDeficit(ind), commodityId);
    }

    private static Pair<String, Integer> getMaxDeficit(BaseIndustry ind) {
        Pair<String, Integer> result = new Pair<String, Integer>();
        result.two = 0;

        for (MutableCommodityQuantity q : ind.getAllDemand()) {
            String id = q.getCommodityId();
            int demand = (int) ind.getDemand(id).getQuantity().getModifiedValue();
            CommodityOnMarketAPI com = ind.getMarket().getCommodityData(id);
            int available = com.getAvailable();

            int deficit = Math.max(demand - available, 0);
            if (deficit > result.two) {
                result.one = id;
                result.two = deficit;
            }
        }

        return result;
    }

    private static void applyDeficitToProduction(Industry ind, String modId, Pair<String, Integer> deficit, String commodity) {
        if (ind.getSupply(commodity).getQuantity().isUnmodified()) return;
        ind.supply(modId, commodity, -deficit.two, BaseIndustry.getDeficitText(deficit.one));
    }

    private List<Alcohol> getAlcoholForMarket() {
        String factionID = market.getFaction().getId();
        return FactionAlcoholHandler.getFactionAlcoholTypes(factionID);
    }

    @Override
    public boolean showIcon() {
        return false;
    }

    @Override
    public String getModId() {
        return condition.getId();
    }

    private String getDeficitModId() {
        return getModId() + "_deficit";
    }
}