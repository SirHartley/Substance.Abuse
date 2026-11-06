package livecheck;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.industry.Brewery;
import com.fs.starfarer.api.alcoholism.memory.AddictionMemory;
import com.fs.starfarer.api.alcoholism.memory.AlcoholAPI;
import com.fs.starfarer.api.alcoholism.memory.AlcoholRepo;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class LoadChecks {
    static final Map<String, String> BREWERIES = new LinkedHashMap<>();

    static {
        BREWERIES.put("jangala", AlcoholRepo.STOUT);
        BREWERIES.put("port_tse", AlcoholRepo.ABSYNTH);
        BREWERIES.put("gilead", AlcoholRepo.TEARS);
        BREWERIES.put("chalcedon", AlcoholRepo.FUEL);
        BREWERIES.put("qaras", AlcoholRepo.BLOOD);
        BREWERIES.put("volturn", AlcoholRepo.SUNSHINE);
        BREWERIES.put("fikenhild", AlcoholRepo.KING);
        BREWERIES.put("ailmar", AlcoholRepo.FREEDOM);
        BREWERIES.put("orthrus", AlcoholRepo.WATER);
        BREWERIES.put("asharu", AlcoholRepo.TEA);
    }

    private LoadChecks() {
    }

    public static List<Step> steps() {
        List<Step> steps = new ArrayList<>();
        steps.add(c -> {
            c.shot("campaign");
            List<AlcoholAPI> alcohols = AlcoholRepo.getIndustrialAlcoholList();
            c.expect(alcohols.size() == 10, "10 industrial alcohols, got " + alcohols.size());
            AddictionMemory memory = AddictionMemory.getInstanceOrRegister();
            for (AlcoholAPI a : alcohols) {
                String id = a.getId();
                c.expect(Global.getSettings().getCommoditySpec(a.getCommodityId()) != null, id + " commodity spec");
                c.expect(Global.getSettings().getSpecialItemSpec(id) != null, id + " special item spec");
                c.expect(Global.getSettings().getSpecialItemSpec(a.getIndustryItemId()) != null, id + " recipe item spec");
                c.expect(Global.getSettings().getHullModSpec(a.getEffectHullmodId()) != null, id + " hullmod spec");
                c.expect(a.getName() != null && !a.getName().isEmpty(), id + " name");
                c.expect(a.getFaction() != null, id + " faction");
                c.expect(a.getDesc() != null && a.getShortDesc() != null, id + " descriptions");
                c.expect(memory.contains(a), id + " has addiction status");
            }
            c.expect(!AlcoholRepo.INGREDIENT_MAP.isEmpty(), "ingredients imported, got " + AlcoholRepo.INGREDIENT_MAP.size());
        });
        steps.add(c -> {
            int placed = 0;
            for (Map.Entry<String, String> e : BREWERIES.entrySet()) {
                MarketAPI market = Global.getSector().getEconomy().getMarket(e.getKey());
                if (market == null) {
                    c.note("no market " + e.getKey() + ", " + e.getValue() + " has no brewery");
                    continue;
                }
                Industry brewery = market.getIndustry(Brewery.INDUSTRY_ID);
                c.expect(brewery != null, e.getKey() + " has a brewery");
                if (brewery == null) continue;
                placed++;
                String want = AlcoholRepo.get(e.getValue()).getIndustryItemId();
                c.expect(brewery.getSpecialItem() != null && want.equals(brewery.getSpecialItem().getId()), e.getKey() + " brewery has recipe " + want);
            }
            c.expect(placed >= 8, "breweries placed, got " + placed);
        });
        steps.add(c -> {
            int planets = 0;
            for (StarSystemAPI system : Global.getSector().getStarSystems()) {
                for (PlanetAPI planet : system.getPlanets()) {
                    if (planet.getMarket() == null) continue;
                    for (MarketConditionAPI condition : planet.getMarket().getConditions()) {
                        if (AlcoholRepo.INGREDIENT_MAP.containsKey(condition.getId())) {
                            planets++;
                            break;
                        }
                    }
                }
            }
            c.expect(planets > 0, "planets with an ingredient condition, got " + planets);
        });
        steps.add(c -> {
            CustomPanelAPI panel = Global.getSettings().createCustom(400f, 400f, null);
            for (AlcoholAPI a : AlcoholRepo.getAllAlcohol()) {
                TooltipMakerAPI tt = panel.createUIElement(400f, 400f, false);
                a.addStatusTooltip(tt, false);
                a.addStatusTooltip(tt, true);
                a.addEffectTooltip(tt, false);
                a.addEffectTooltip(tt, true);
                c.expect(tt.getHeightSoFar() > 0f, a.getId() + " tooltips build");
            }
        });
        return steps;
    }
}
