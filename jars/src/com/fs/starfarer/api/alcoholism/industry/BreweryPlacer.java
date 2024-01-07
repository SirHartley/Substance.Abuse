package com.fs.starfarer.api.alcoholism.industry;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.ModPlugin;
import com.fs.starfarer.api.alcoholism.memory.AlcoholRepo;
import com.fs.starfarer.api.alcoholism.memory.AlcoholAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.util.Misc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BreweryPlacer {

    public static final String INIT_KEY = "$BreweriesHaveBeenPlaced";

    private static Map<String, String> breweryDestinations = new HashMap<String, String>() {{
        put("jangala", AlcoholRepo.STOUT);
        put("port_tse", AlcoholRepo.ABSYNTH);
        put("gilead", AlcoholRepo.TEARS);
        put("chalcedon", AlcoholRepo.FUEL);
        put("qaras", AlcoholRepo.BLOOD);
        put("volturn", AlcoholRepo.SUNSHINE);
        put("fikenhild", AlcoholRepo.KING);
        put("ailmar", AlcoholRepo.FREEDOM);
        put("orthrus", AlcoholRepo.WATER);
        put("asharu", AlcoholRepo.TEA);
    }};

    public static void placeBreweries() {
        MemoryAPI mem = Global.getSector().getMemoryWithoutUpdate();

        EconomyAPI economyAPI = Global.getSector().getEconomy();
        if (economyAPI.getMarket("jangala") != null) {
            ModPlugin.log("Placing Breweries, vanilla mode");
            fixQaras();

            for (Map.Entry<String, String> e : breweryDestinations.entrySet()) {
                if (economyAPI.getMarket(e.getKey()) != null) {
                    MarketAPI market = economyAPI.getMarket(e.getKey());
                    MemoryAPI marketMemory = market.getMemoryWithoutUpdate();

                    if (!market.hasIndustry(Brewery.INDUSTRY_ID) && !marketMemory.getBoolean(INIT_KEY)) {
                        market.addIndustry(Brewery.INDUSTRY_ID);
                        market.getIndustry(Brewery.INDUSTRY_ID).setSpecialItem(new SpecialItemData(AlcoholRepo.get(e.getValue()).getIndustryItemId(), null));
                        marketMemory.set(INIT_KEY, true);
                    }
                }
            }

        } else {
            if (mem.getBoolean(INIT_KEY)) return;

            ModPlugin.log("Placing Breweries, Nex Random mode");

            for (AlcoholAPI alcohol : AlcoholRepo.getIndustrialAlcoholList()) {
                FactionAPI faction = alcohol.getFaction();
                List<MarketAPI> marketList = Misc.getFactionMarkets(faction);
                if (marketList.size() < 1) continue;

                MarketAPI largest = null;
                int largestSize = 0;

                for (MarketAPI m : marketList) {
                    if (m.getSize() > largestSize && m.getIndustries().size() < 12) largest = m;
                }

                if (largest != null) {
                    largest.addIndustry(Brewery.INDUSTRY_ID);
                    largest.getIndustry(Brewery.INDUSTRY_ID).setSpecialItem(new SpecialItemData(alcohol.getIndustryItemId(), null));
                }
            }
        }

        mem.set(INIT_KEY, true);
    }

    //all pirate worlds have too low accessibility to support exports without overproduction
    private static void fixQaras() {
        MarketAPI m = Global.getSector().getEconomy().getMarket("qaras");
        if (m == null || m.isPlayerOwned() || m.getMemoryWithoutUpdate().getBoolean(INIT_KEY)) return;

        //stupid industrial planning is breaking this planet
        if (m.getAdmin() != null) {
            for (MutableCharacterStatsAPI.SkillLevelAPI skill : m.getAdmin().getStats().getSkillsCopy()) {
                if (skill.getSkill().isAdminSkill()) skill.setLevel(0);
            }
        }

        //for good measure
        m.getIndustry(Industries.SPACEPORT).setSpecialItem(new SpecialItemData(Items.FULLERENE_SPOOL, null));
    }
}
