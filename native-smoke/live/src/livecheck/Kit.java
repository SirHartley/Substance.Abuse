package livecheck;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.hullmods.campaignEffects.OfficerXPGainModifier;
import com.fs.starfarer.api.alcoholism.listeners.AlcoholConsumptionManager;
import com.fs.starfarer.api.alcoholism.memory.AddictionStatus;
import com.fs.starfarer.api.alcoholism.memory.Alcohol;
import com.fs.starfarer.api.alcoholism.memory.AlcoholRepo;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.characters.OfficerDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.util.Misc;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;

public final class Kit {
    private Kit() {
    }

    public static CampaignFleetAPI fleet() {
        return Global.getSector().getPlayerFleet();
    }

    public static CargoAPI cargo() {
        return fleet().getCargo();
    }

    public static AlcoholConsumptionManager manager() {
        return AlcoholConsumptionManager.getInstanceOrRegister();
    }

    public static Alcohol drink(String id) {
        return AlcoholRepo.get(id);
    }

    public static void addiction(String id, float value) {
        AddictionStatus status = drink(id).getAddictionStatus();
        status.increment(-2f);
        status.increment(value);
    }

    public static void consume(String id, float value, float units) {
        addiction(id, value);
        cargo().addCommodity(drink(id).getCommodityId(), units);
        drink(id).getAddictionStatus().setConsuming(true);
    }

    public static void withdraw(String id, float value) {
        addiction(id, value);
        drink(id).getAddictionStatus().setConsuming(false);
    }

    public static void reset() {
        for (Alcohol a : AlcoholRepo.ALCOHOL_MAP.values()) {
            a.getAddictionStatus().setConsuming(false);
            a.getAddictionStatus().increment(-2f);
            float left = cargo().getCommodityQuantity(a.getCommodityId());
            if (left > 0f) cargo().removeCommodity(a.getCommodityId(), left);
            opened(a.getId(), 0f);
        }
        manager().reapply();
    }

    // opened units are new in 1.1.f, NaN on builds without them so baseline runs still compile
    public static float opened(String id) {
        try {
            MethodHandle h = MethodHandles.publicLookup().findVirtual(AddictionStatus.class, "getOpenedUnits", MethodType.methodType(float.class));
            return (float) h.invoke(drink(id).getAddictionStatus());
        } catch (Throwable t) {
            return Float.NaN;
        }
    }

    public static void opened(String id, float value) {
        try {
            MethodHandle h = MethodHandles.publicLookup().findVirtual(AddictionStatus.class, "setOpenedUnits", MethodType.methodType(void.class, float.class));
            h.invoke(drink(id).getAddictionStatus(), value);
        } catch (Throwable ignored) {
        }
    }

    public static void ext(boolean on) {
        Global.getSector().setExtMode(on);
    }

    public static int recipes(CargoAPI cargo) {
        int found = 0;
        for (CargoStackAPI stack : cargo.getStacksCopy()) {
            if (!stack.isSpecialStack()) continue;
            String id = stack.getSpecialDataIfSpecial().getId();
            if (id.equals("alcoholism_recipe_book")) found += (int) stack.getSize();
            for (Alcohol a : AlcoholRepo.ALCOHOL_MAP.values()) if (id.equals(a.getIndustryItemId())) found += (int) stack.getSize();
        }
        return found;
    }

    // stock variants are shared, every test ship gets its own copy
    public static FleetMemberAPI ship(CampaignFleetAPI into, String variantId) {
        FleetMemberAPI m = Global.getFactory().createFleetMember(FleetMemberType.SHIP, variantId);
        ShipVariantAPI v = m.getVariant().clone();
        v.setOriginalVariant(null);
        v.setHullVariantId(Misc.genUID());
        v.setSource(VariantSource.REFIT);
        m.setVariant(v, false, true);
        into.getFleetData().addFleetMember(m);
        m.getRepairTracker().setCR(m.getRepairTracker().getMaxCR());
        return m;
    }

    public static FleetMemberAPI ship(String variantId) {
        return ship(fleet(), variantId);
    }

    public static FleetMemberAPI member(String id) {
        for (FleetMemberAPI m : fleet().getFleetData().getMembersListCopy()) if (m.getId().equals(id)) return m;
        return null;
    }

    public static void overOp(FleetMemberAPI m) {
        ShipVariantAPI v = m.getVariant();
        v.setNumFluxVents(v.getNumFluxVents() + v.getUnusedOP(Global.getSector().getPlayerPerson().getStats()) + 10);
    }

    public static int unusedOp(FleetMemberAPI m) {
        return m.getVariant().getUnusedOP(Global.getSector().getPlayerPerson().getStats());
    }

    public static OfficerDataAPI officer() {
        List<OfficerDataAPI> officers = fleet().getFleetData().getOfficersCopy();
        if (!officers.isEmpty()) return officers.get(0);
        PersonAPI person = Global.getSector().getPlayerFaction().createRandomPerson();
        fleet().getFleetData().addOfficer(person);
        return fleet().getFleetData().getOfficerData(person);
    }

    public static OfficerXPGainModifier xpListener() {
        for (CampaignEventListener l : Global.getSector().getAllListeners()) {
            if (l instanceof OfficerXPGainModifier) return (OfficerXPGainModifier) l;
        }
        return null;
    }

    // vanilla order, FleetEncounterContext: engagement report, xp grant, battle report
    public static long battleXp(long granted) {
        OfficerXPGainModifier listener = xpListener();
        OfficerDataAPI data = officer();
        long before = data.getPerson().getStats().getXP();
        CampaignFleetAPI enemy = Global.getFactory().createEmptyFleet(Factions.PIRATES, "Live check", true);
        ship(enemy, "wolf_CS");
        fleet().getContainingLocation().addEntity(enemy);
        enemy.setLocation(fleet().getLocation().x + 100f, fleet().getLocation().y);
        BattleAPI battle = Global.getFactory().createBattle(fleet(), enemy);
        listener.reportPlayerEngagement(null);
        data.addXP(granted);
        listener.reportBattleOccurred(fleet(), battle);
        battle.leave(fleet(), false);
        battle.leave(enemy, false);
        enemy.getContainingLocation().removeEntity(enemy);
        return data.getPerson().getStats().getXP() - before;
    }
}
