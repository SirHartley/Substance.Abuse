package livecheck;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.industry.Brewery;
import com.fs.starfarer.api.alcoholism.memory.AlcoholAPI;
import com.fs.starfarer.api.alcoholism.memory.AlcoholRepo;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class EconomyChecks {
    private EconomyChecks() {
    }

    public static List<Step> steps() {
        List<Step> steps = new ArrayList<>();
        steps.add(c -> c.days(35f));
        steps.add(c -> {
            for (Map.Entry<String, String> e : LoadChecks.BREWERIES.entrySet()) {
                MarketAPI market = Global.getSector().getEconomy().getMarket(e.getKey());
                if (market == null) continue;
                Industry brewery = market.getIndustry(Brewery.INDUSTRY_ID);
                c.expect(brewery != null, e.getKey() + " keeps its brewery after a month");
                if (brewery == null) continue;
                AlcoholAPI a = AlcoholRepo.get(e.getValue());
                int supply = brewery.getSupply(a.getCommodityId()).getQuantity().getModifiedInt();
                c.expect(supply > 0, e.getKey() + " brewery supplies " + a.getCommodityId() + ", got " + supply);
            }
        });
        return steps;
    }
}
