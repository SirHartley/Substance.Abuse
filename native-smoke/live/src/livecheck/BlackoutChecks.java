package livecheck;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.intel.DrunkFleetIntel;
import com.fs.starfarer.api.alcoholism.memory.AddictionMemory;
import com.fs.starfarer.api.alcoholism.memory.Alcohol;
import com.fs.starfarer.api.alcoholism.memory.AlcoholRepo;
import com.fs.starfarer.api.alcoholism.scripts.BlackoutScript;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// changelog rows 13 to 17 and the 1.1.f blackout changes. keep this group last, it moves the player and splits the fleet
public final class BlackoutChecks {
    private static CampaignFleetAPI drunk = null;
    private static LocationAPI origin = null;

    private BlackoutChecks() {
    }

    private static final class Mover extends BlackoutScript {
        void move() {
            movePlayerFleet();
        }
    }

    private static boolean anyConsuming() {
        for (Alcohol a : AlcoholRepo.ALCOHOL_MAP.values()) if (a.getAddictionStatus().isConsuming()) return true;
        return false;
    }

    private static void row17(Ctx c) throws Exception {
        AddictionMemory memory = new AddictionMemory();
        for (int i = 0; i < 31; i++) memory.addTotalDailyAddictionGainToMemory(1f);
        c.near(memory.getTotaladdictionGainLastMonth(), 30f, 0.001f, "row17 31 days hold 30 entries");

        VarHandle map = MethodHandles.privateLookupIn(AddictionMemory.class, MethodHandles.lookup())
                .findVarHandle(AddictionMemory.class, "monthlyAddictionIncreaseMap", Map.class);
        memory = new AddictionMemory();
        @SuppressWarnings("unchecked")
        Map<Integer, Float> days = (Map<Integer, Float>) map.get(memory);
        days.put(31, 1f);
        for (int i = 0; i < 31; i++) memory.addTotalDailyAddictionGainToMemory(1f);
        c.near(memory.getTotaladdictionGainLastMonth(), 30f, 0.001f, "row17 old save day 31 entry goes away");
    }

    private static void row13(Ctx c) {
        Mover mover = new Mover();
        int noJump = 0, pulsar = 0, blackHole = 0, cutOff = 0, hidden = 0;
        for (int i = 0; i < 300; i++) {
            mover.move();
            StarSystemAPI system = Kit.fleet().getStarSystem();
            if (Misc.getDistressJumpPoint(system) == null) noJump++;
            if (system.hasPulsar()) pulsar++;
            if (system.hasBlackHole()) blackHole++;
            if (system.hasTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER)) cutOff++;
            if (system.hasTag(Tags.THEME_HIDDEN)) hidden++;
        }
        c.note("row 13 destinations of 300: hidden " + hidden);
        c.expect(noJump == 0, "row13 every destination has a distress jump point, " + noJump + " had none");
        c.expect(pulsar == 0, "row13 no pulsar, got " + pulsar);
        c.expect(blackHole == 0, "row13 no black hole, got " + blackHole);
        c.expect(cutOff == 0, "row13 not cut off from hyperspace, got " + cutOff);
    }

    private static void row14(Ctx c) {
        StarSystemAPI system = Global.getSector().createStarSystem("live_check_starless");
        system.initNonStarCenter();
        PlanetAPI planet = system.addPlanet("live_check_planet", system.getCenter(), "Live Check", "barren", 0f, 100f, 3000f, 100f);
        planet.setFixedLocation(3000f, 0f);
        JumpPointAPI jump = Global.getFactory().createJumpPoint("live_check_jump", "Live Check Jump");
        jump.setFixedLocation(-3000f, 0f);
        system.addEntity(jump);

        float[][] spots = {{3050f, 0f}, {-3050f, 0f}, {0f, 6000f}};
        String[] names = {"planet", "jump point", "open space"};
        for (int i = 0; i < spots.length; i++) {
            SectorEntityToken token = system.createToken(spots[i][0], spots[i][1]);
            system.addEntity(token);
            SectorEntityToken focus = BlackoutScript.getOrbitFocusAtTokenPosition(token);
            c.expect(focus != null, "row14 focus near " + names[i] + " in a starless system");
            system.removeEntity(token);
        }
        Global.getSector().removeStarSystem(system);
    }

    private static void row16(Ctx c) throws Exception {
        VarHandle quips = MethodHandles.privateLookupIn(BlackoutScript.class, MethodHandles.lookup())
                .findStaticVarHandle(BlackoutScript.class, "QUIP_LIST", List.class);
        Object old = quips.get();
        List<String> one = new ArrayList<>();
        one.add("Live check quip, the only line.");
        quips.set(one);
        try {
            BlackoutScript script = new BlackoutScript();
            script.advance(1f);
            c.expect(script.isDone(), "row16 script done with a one line list");
        } finally {
            quips.set(old);
        }
        c.shot("blackout");
    }

    // same pick as BlackoutScript in survival: closest visible market of the location
    private static MarketAPI closestMarket(CampaignFleetAPI fleet) {
        MarketAPI closest = null;
        float min = Float.MAX_VALUE;
        for (MarketAPI m : Misc.getMarketsInLocation(fleet.getContainingLocation())) {
            if (m.isHidden() || m.getPrimaryEntity() == null) continue;
            float dist = Misc.getDistance(fleet, m.getPrimaryEntity());
            if (dist < min) {
                closest = m;
                min = dist;
            }
        }
        return closest;
    }

    private static CampaignFleetAPI hungover(LocationAPI where) {
        for (CampaignFleetAPI f : where.getFleets()) if ("Hungover Fleet".equals(f.getName()) && f.getFleetData().getNumMembers() > 0) return f;
        return null;
    }

    private static void survival(Ctx c) {
        CampaignFleetAPI player = Kit.fleet();
        while (player.getFleetData().getNumMembers() < 3) Kit.ship("wolf_CS");
        player.getCargo().getCredits().add(20000f);
        player.getCargo().addCrew(200);
        LocationAPI where = player.getContainingLocation();
        Vector2f from = new Vector2f(player.getLocation());
        MarketAPI market = closestMarket(player);
        String faction = market == null ? null : market.getFactionId();
        float rel = faction == null ? 0f : Global.getSector().getPlayerFaction().getRelationship(faction);
        float credits = player.getCargo().getCredits().get();
        int crew = player.getCargo().getCrew();
        float supplies = player.getCargo().getSupplies();
        float fuel = player.getCargo().getFuel();
        int recipes = Kit.recipes(player.getCargo());

        Kit.ext(true);
        try {
            new BlackoutScript().advance(1f);
        } finally {
            Kit.ext(false);
        }

        CampaignFleetAPI hung = hungover(where);
        c.expect(hung != null, "survival blackout fleet stays behind");
        c.expect(player.getContainingLocation() == where, "survival blackout wakes up in the same location");
        if (market != null) {
            float dist = Misc.getDistance(player.getLocation(), market.getPrimaryEntity().getLocation());
            c.expect(dist <= market.getPrimaryEntity().getRadius() + 200f, "survival blackout wakes up next to " + market.getName() + ", " + dist + " away");
            if (!Factions.PLAYER.equals(faction) && !market.isPlayerOwned()) {
                c.near(Global.getSector().getPlayerFaction().getRelationship(faction) - rel, -0.03f, 0.005f, "survival blackout costs standing with " + faction);
            }
        } else {
            c.near(Misc.getDistance(player.getLocation(), from), 600f, 5f, "survival blackout wakes up a short way off");
        }

        CargoAPI other = hung == null ? Global.getFactory().createCargo(true) : hung.getCargo();
        float paid = credits - player.getCargo().getCredits().get();
        int lost = crew - player.getCargo().getCrew() - other.getCrew();
        c.note("survival blackout paid " + paid + ", lost " + lost + " of " + crew + " crew");
        c.expect(paid >= 1000f && paid <= 5000f && Math.abs(paid - Math.round(paid / 100f) * 100f) < 0.5f, "survival blackout bar tab " + paid);
        c.expect(lost > 0 && lost <= Math.ceil(crew * 0.05f) + 1, "survival blackout deserters " + lost);
        c.near(player.getCargo().getSupplies() + other.getSupplies(), supplies, 0.5f, "survival blackout adds no supplies");
        c.near(player.getCargo().getFuel() + other.getFuel(), fuel, 0.5f, "survival blackout adds no fuel");
        c.expect(player.getCargo().getCrew() > 0, "survival blackout flagship keeps crew");
        c.expect(Kit.recipes(player.getCargo()) + Kit.recipes(other) == recipes, "survival blackout gives no recipe");
        c.shot("blackout_survival");
    }

    public static List<Step> steps() {
        List<Step> steps = new ArrayList<>();
        steps.add(c -> {
            row17(c);
            Kit.reset();
        });
        steps.add(c -> row14(c));
        steps.add(c -> {
            for (FleetMemberAPI m : Kit.fleet().getFleetData().getMembersListCopy()) {
                if (Kit.fleet().getFleetData().getNumMembers() > 1 && !m.isFlagship()) Kit.fleet().getFleetData().removeFleetMember(m);
            }
            row13(c);
        });
        steps.add(c -> row16(c));
        steps.add(c -> {
            Kit.ship("wolf_CS");
            Kit.ship("wolf_CS");
            c.expect(Kit.fleet().getFleetData().getNumMembers() == 3, "row15 three ship fleet, got " + Kit.fleet().getFleetData().getNumMembers());
            for (Alcohol a : AlcoholRepo.ALCOHOL_MAP.values()) Kit.consume(a.getId(), 0f, 500f);
            origin = Kit.fleet().getContainingLocation();
            c.put("r15recipes", Kit.recipes(Kit.fleet().getCargo()));
            c.until(() -> !anyConsuming(), 30f, "row15 blackout stops all consumption");
        });
        steps.add(c -> {
            for (CampaignFleetAPI f : origin.getFleets()) if ("Hungover Fleet".equals(f.getName())) drunk = f;
            c.expect(drunk != null, "row15 drunk fleet stays behind");
            c.expect(!Global.getSector().getIntelManager().getIntel(DrunkFleetIntel.class).isEmpty(), "row15 drunk fleet intel added");
            int recipes = Kit.recipes(Kit.fleet().getCargo()) + (drunk == null ? 0 : Kit.recipes(drunk.getCargo()));
            c.expect(recipes == (Integer) c.get("r15recipes"), "row15 blackout gives no recipe, " + recipes + " after " + c.get("r15recipes"));
            if (drunk == null) return;
            c.put("r15id", drunk.getId());
            if (Kit.fleet().getContainingLocation() == origin) {
                for (StarSystemAPI s : Global.getSector().getStarSystems()) {
                    if (s != origin) {
                        Colony.jump(s, drunk.getLocation().x, drunk.getLocation().y);
                        break;
                    }
                }
            } else {
                Kit.fleet().setLocation(drunk.getLocation().x, drunk.getLocation().y);
            }
            long start = Global.getSector().getClock().getTimestamp();
            c.until(() -> Global.getSector().getClock().getElapsedDaysSince(start) >= 1f || drunk.getFleetData().getNumMembers() == 0, 2f, "row15 a day at the same coordinates in another system",
                    () -> Kit.fleet().setLocation(drunk.getLocation().x, drunk.getLocation().y));
        });
        steps.add(c -> {
            if (drunk == null) return;
            c.expect(drunk.getFleetData().getNumMembers() > 0, "row15 fleet stays split in another system");
            Colony.jump(origin, drunk.getLocation().x + 20f, drunk.getLocation().y);
            c.until(() -> drunk.getFleetData().getNumMembers() == 0, 3f, "row15 fleet merges next to it");
        });
        steps.add(c -> {
            if (drunk == null) return;
            c.expect(!Global.getSector().getMemoryWithoutUpdate().contains(DrunkFleetIntel.MEMORY_KEY_PRE + c.get("r15id")), "row15 drunk fleet marker removed");
            c.until(() -> Global.getSector().getIntelManager().getIntel(DrunkFleetIntel.class).isEmpty()
                    || Global.getSector().getIntelManager().getIntel(DrunkFleetIntel.class).get(0).isEnding()
                    || Global.getSector().getIntelManager().getIntel(DrunkFleetIntel.class).get(0).isEnded(), 3f, "row15 drunk fleet intel removed");
        });
        steps.add(c -> {
            Kit.reset();
            survival(c);
        });
        return steps;
    }
}
