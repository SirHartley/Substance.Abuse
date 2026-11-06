package livecheck;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.listeners.AlcoholConsumptionManager;
import com.fs.starfarer.api.alcoholism.memory.AddictionBrain;
import com.fs.starfarer.api.alcoholism.memory.AddictionStatus;
import com.fs.starfarer.api.alcoholism.memory.AlcoholAPI;
import com.fs.starfarer.api.alcoholism.memory.AlcoholRepo;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

import java.util.ArrayList;
import java.util.List;

public final class AddictionChecks {
    private AddictionChecks() {
    }

    private static AlcoholAPI stout() {
        return AlcoholRepo.get(AlcoholRepo.STOUT);
    }

    private static CargoAPI cargo() {
        return Global.getSector().getPlayerFleet().getCargo();
    }

    private static boolean fleetHasHullmod(String id) {
        List<FleetMemberAPI> members = Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy();
        for (FleetMemberAPI m : members) if (!m.getVariant().hasHullMod(id)) return false;
        return !members.isEmpty();
    }

    public static List<Step> steps() {
        List<Step> steps = new ArrayList<>();
        steps.add(c -> {
            AlcoholAPI a = stout();
            c.expect(a.getAddictionStatus().getAddictionValue() == 0f, "stout starts at no addiction");
            c.expect(cargo().getCrew() > 0, "fleet has crew");
            cargo().addCommodity(a.getCommodityId(), 500f);
            a.getAddictionStatus().setConsuming(true);
            c.put("stoutStart", cargo().getCommodityQuantity(a.getCommodityId()));
            c.days(10f);
        });
        steps.add(c -> {
            AlcoholAPI a = stout();
            AddictionStatus s = a.getAddictionStatus();
            float left = cargo().getCommodityQuantity(a.getCommodityId());
            c.expect(left < (Float) c.get("stoutStart"), "stout consumed, " + left + " left of " + c.get("stoutStart"));
            c.expect(s.isAddicted(), "addicted after 10 days, value " + s.getAddictionValue());
            float days = AddictionBrain.getDaysAddicted(a.getMult(), s.getAddictionValue());
            c.expect(days >= 8f && days <= 13f, "addiction matches days consumed, got " + days);
            c.expect(fleetHasHullmod(a.getEffectHullmodId()), "fleet has the stout hullmod");
            c.put("stoutValue", s.getAddictionValue());
            c.reload();
        });
        steps.add(c -> {
            AlcoholAPI a = stout();
            AddictionStatus s = a.getAddictionStatus();
            c.near(s.getAddictionValue(), (Float) c.get("stoutValue"), 0.001f, "addiction survives save and load");
            c.expect(s.isConsuming(), "still consuming after save and load");
            c.expect(fleetHasHullmod(a.getEffectHullmodId()), "fleet has the stout hullmod after save and load");
            int managers = Global.getSector().getListenerManager().getListeners(AlcoholConsumptionManager.class).size();
            c.expect(managers == 1, "one consumption manager after save and load, got " + managers);
            cargo().removeCommodity(a.getCommodityId(), cargo().getCommodityQuantity(a.getCommodityId()));
            c.days(3f);
        });
        steps.add(c -> {
            AddictionStatus s = stout().getAddictionStatus();
            c.expect(!s.isConsuming(), "consumption stops when cargo runs out");
            c.expect(s.isWithdrawal(), "in withdrawal, value " + s.getAddictionValue());
            c.expect(s.getAddictionValue() < (Float) c.get("stoutValue"), "addiction falls in withdrawal");
            c.until(() -> !stout().getAddictionStatus().isAddicted(), 30f, "withdrawal ends");
        });
        steps.add(c -> c.days(2f));
        steps.add(c -> c.expect(!fleetHasHullmod(stout().getEffectHullmodId()), "stout hullmod removed after withdrawal"));
        return steps;
    }
}
