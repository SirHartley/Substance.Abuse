package livecheck;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.intel.DrunkFleetIntel;
import com.fs.starfarer.api.alcoholism.memory.AlcoholAPI;
import com.fs.starfarer.api.alcoholism.memory.AlcoholRepo;
import com.fs.starfarer.api.campaign.CargoAPI;

import java.util.ArrayList;
import java.util.List;

// keep this group last, it moves the player and splits the fleet
public final class BlackoutChecks {
    private BlackoutChecks() {
    }

    private static boolean anyConsuming() {
        for (AlcoholAPI a : AlcoholRepo.getIndustrialAlcoholList()) if (a.getAddictionStatus().isConsuming()) return true;
        return false;
    }

    public static List<Step> steps() {
        List<Step> steps = new ArrayList<>();
        steps.add(c -> {
            CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
            for (AlcoholAPI a : AlcoholRepo.getIndustrialAlcoholList()) {
                cargo.addCommodity(a.getCommodityId(), 500f);
                a.getAddictionStatus().setConsuming(true);
            }
            c.put("members", Global.getSector().getPlayerFleet().getFleetData().getNumMembers());
            c.expect(anyConsuming(), "consuming all alcohols");
            c.until(() -> !anyConsuming(), 30f, "blackout stops all consumption");
        });
        steps.add(c -> {
            c.note("fleet members before blackout " + c.get("members") + ", after " + Global.getSector().getPlayerFleet().getFleetData().getNumMembers());
            if ((Integer) c.get("members") > 1) {
                c.expect(!Global.getSector().getIntelManager().getIntel(DrunkFleetIntel.class).isEmpty(), "drunk fleet intel added");
            }
        });
        return steps;
    }
}
