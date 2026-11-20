package com.fs.starfarer.api.alcoholism.scripts;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.ModPlugin;
import com.fs.starfarer.api.alcoholism.intel.DrunkFleetIntel;
import com.fs.starfarer.api.alcoholism.memory.AddictionBrain;
import com.fs.starfarer.api.alcoholism.memory.Alcohol;
import com.fs.starfarer.api.alcoholism.memory.AlcoholRepo;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import com.fs.starfarer.campaign.CampaignEngine;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BlackoutScript implements EveryFrameScript {

    public static final float MAXIMUM_DISTANCE_TO_NON_CENTERED_ENTITY = 400f;
    public static final String ITEM_DROP_KEY = "$Alcohol_hasDroppedItem";
    public static final float EXT_BAR_TAB_MIN = 1000f;
    public static final float EXT_BAR_TAB_MAX = 5000f;
    public static final float EXT_CREW_DESERTION_FRACTION = 0.05f;
    public static final float EXT_REPUTATION_LOSS = 0.03f;
    public static final float EXT_WAKE_UP_DISTANCE = 600f;

    private static List<String> QUIP_LIST = new ArrayList<String>(){{
        add("You blacked out after getting incredibly drunk. Good job.");
        add("Turns out, you are a lightweight. Without memory of where you are or what happened, there is a fun journey ahead.");
        add("You have exactly zero memories of last night, but apparently acquired a new Tri-Pad and a foreign pair of slippers. All in all, great success!");
        add("Shooting yourself in the face would probably give less of a headache than you have now. Too many drinks, looks like.");
        add("Your crew celebrated the party of their lives yesterday, though you remember very little - except for a very, very stupid dare.");
        add("You are pretty sure that the hot nav officer flirted with you over drinks yesterday. They do not appear to be around. Neither is anyone else, for that matter.");
        add("After getting absolutely destroyed with your bridge crew, you fell unconscious. Someone dragged you into your bathroom and left you with a towel. How nice!");
        add("It was either too much, or someone slipped something in your drink. Either way, your situation now is sub-optimal, to say the least.");
        add("Even drinking wants to be learned, and you just received a very important lesson: know your limits.");
        add("And after another 'last drink' you lose consciousness and hit the floor. There goes your date for the night.");
        add("Your Mother warned you not to mix too many different drinks. You really should have listened.");
        add("The drink you so brilliantly called 'Saturation Bombardment' to the displeasure of anyone who was not yet absolutely destroyed, did, in fact, bomb your mind.");
        add("Your Tri-Pad is filled with messages from your supposed dates, and you apparently sent thousands of credits to some Kazeron Royal promising to multiply it. Good job.");
        add("There is a heart drawn with lipstick on your Tri-Pad, and you now own a brand new, barely used, red bra. You do not own much else.");
        add("Your personal handgun was replaced with an antique, pre-collapse revolver. This would be cool, were it not for the absence of both your memories and your fleet.");
        add("None of your contacts respond to your calls - apparently, you confessed your love to each of them yesterday while blackout drunk.");
        add("You find a treasure map in your mouth. The handwriting does not look sober, and it might be yours. Whoops.");
    }};

    private boolean done = false;

    public static void register() {
        ModPlugin.log("creating BlackoutScript instance");

        BlackoutScript script = new BlackoutScript();
        Global.getSector().addScript(script);
    }

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {
        if(isDone()) return;

        CampaignEngine.getInstance().getCampaignUI().showNoise(0.5F, 0.25F, 1.5F);

        boolean survival = Misc.isExtended();
        CampaignFleetAPI drunkFleet = null;

        if(Global.getSector().getPlayerFleet().getFleetData().getNumMembers() > 1){
            drunkFleet = transferPlayerFleetToEmpty();
            if(survival) takeFlagshipNeedsFrom(drunkFleet);
            else addCargoToPlayerFleet();
            Global.getSector().getIntelManager().addIntel(new DrunkFleetIntel(drunkFleet));
            Global.getSector().getMemoryWithoutUpdate().set(DrunkFleetIntel.MEMORY_KEY_PRE + drunkFleet.getId(), true);
        }

        String costs = "";
        if(survival) {
            MarketAPI market = movePlayerFleetInRange() ? null : movePlayerFleetNearby();
            String charged = chargeSurvivalCosts(drunkFleet, market);
            if(!charged.isEmpty()) costs = "\n\n" + charged;
        } else movePlayerFleet();

        giveFirstBlackoutRecipe();
        stopAllConsumption();

        Global.getSector().getCampaignUI().showMessageDialog(QUIP_LIST.get(new Random().nextInt(QUIP_LIST.size()))
                + costs
                + "\n\nThis situation appears to be a result of consuming too much alcohol at once. Check your intel entries for more info.");
        Global.getSector().setPaused(true);

        done = true;
    }

    // any fleet size and both modes, after the move so it lands in the flagship's cargo
    private void giveFirstBlackoutRecipe(){
        MemoryAPI mem = Global.getSector().getMemoryWithoutUpdate();
        if(mem.getBoolean(ITEM_DROP_KEY)) return;

        WeightedRandomPicker<Alcohol> picker = new WeightedRandomPicker<>();
        picker.addAll(new ArrayList<>(AlcoholRepo.ALCOHOL_MAP.values()));
        Global.getSector().getPlayerFleet().getCargo().addSpecial(new SpecialItemData(picker.pick().getIndustryItemId(), null), 1);
        mem.set(ITEM_DROP_KEY, true);
    }

    private void stopAllConsumption(){
        for (Alcohol alcohol : AlcoholRepo.ALCOHOL_MAP.values()){
            alcohol.getAddictionStatus().setConsuming(false);
        }
    }

    private void addCargoToPlayerFleet(){
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        fleet.forceSync();

        CargoAPI cargo = fleet.getCargo();
        float maxCargo = fleet.getCargo().getMaxCapacity();
        int minCrew = Math.round(fleet.getFleetData().getMinCrew());
        float suppliesPerMonth = fleet.getLogistics().getShipMaintenanceSupplyCost() * 30;

        ModPlugin.log("supplies per month " + suppliesPerMonth + " max " + maxCargo);

        cargo.addCrew(minCrew);
        cargo.addFuel(cargo.getMaxFuel());
        cargo.addSupplies(Math.min((suppliesPerMonth * 3) + fleet.getLogistics().getTotalRepairAndRecoverySupplyCost(), maxCargo - 5f));

        List<Alcohol> alcoholList = new ArrayList<>(AlcoholRepo.ALCOHOL_MAP.values());
        WeightedRandomPicker<Alcohol> picker = new WeightedRandomPicker<Alcohol>(true);
        picker.addAll(alcoholList);

        Random random = new Random();

        if(cargo.getSpaceLeft() > 1f){
            while (cargo.getSpaceLeft() > 1f && !picker.isEmpty()){
                if(!picker.isEmpty()){
                    String commodity = picker.pickAndRemove().getCommodityId();
                    int amt = random.nextInt(Math.min((int) Math.ceil(cargo.getSpaceLeft()), 50));
                    cargo.addCommodity(commodity, amt);
                }
            }
        }

        fleet.forceSync();
    }

    // survival: the flagship keeps what it needs from the fleet's own cargo, nothing is added
    private void takeFlagshipNeedsFrom(CampaignFleetAPI drunkFleet){
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        fleet.forceSync();

        CargoAPI from = drunkFleet.getCargo();
        CargoAPI to = fleet.getCargo();

        float crew = Math.min(from.getCrew(), Math.round(fleet.getFleetData().getMinCrew()));
        float fuel = Math.min(from.getFuel(), to.getMaxFuel());
        float supplies = Math.min(from.getSupplies(), Math.min(fleet.getLogistics().getShipMaintenanceSupplyCost() * AddictionBrain.DAYS_PER_MONTH, to.getMaxCapacity()));

        from.removeCrew((int) crew);
        to.addCrew((int) crew);
        from.removeFuel(fuel);
        to.addFuel(fuel);
        from.removeSupplies(supplies);
        to.addSupplies(supplies);

        fleet.forceSync();
    }

    // survival: a system the flagship can fly back from on the fuel it carries, false when none is in range
    private boolean movePlayerFleetInRange() {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        float perLY = fleet.getLogistics().getFuelCostPerLightYear();
        float range = perLY > 0f ? fleet.getCargo().getFuel() / perLY : Float.MAX_VALUE;

        StarSystemAPI system = pickDestinationSystem(range, fleet.getContainingLocation());
        if (system == null) return false;

        placePlayerFleet(system, false);
        return true;
    }

    // survival fallback: wake up at the closest market of the same location, or a short way off when there is none
    private MarketAPI movePlayerFleetNearby() {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();

        MarketAPI closest = null;
        float minDist = Float.MAX_VALUE;

        for (MarketAPI market : Misc.getMarketsInLocation(fleet.getContainingLocation())) {
            if (market.isHidden() || market.getPrimaryEntity() == null) continue;

            float dist = Misc.getDistance(fleet, market.getPrimaryEntity());
            if (dist < minDist) {
                closest = market;
                minDist = dist;
            }
        }

        Vector2f center = closest != null ? closest.getPrimaryEntity().getLocation() : fleet.getLocation();
        float radius = closest != null ? closest.getPrimaryEntity().getRadius() + 150f : EXT_WAKE_UP_DISTANCE;
        Vector2f point = Misc.getPointAtRadius(center, radius);

        fleet.setLocation(point.x, point.y);
        fleet.setVelocity(0, 0);
        Global.getSector().getCampaignUI().resetViewOffset();
        return closest;
    }

    private String chargeSurvivalCosts(CampaignFleetAPI drunkFleet, MarketAPI market) {
        CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();

        float tab = Math.round((EXT_BAR_TAB_MIN + new Random().nextFloat() * (EXT_BAR_TAB_MAX - EXT_BAR_TAB_MIN)) / 100f) * 100f;
        tab = Math.min(tab, cargo.getCredits().get());
        cargo.getCredits().subtract(tab);

        CargoAPI crewCargo = drunkFleet != null ? drunkFleet.getCargo() : cargo;
        int deserters = Math.round(crewCargo.getCrew() * EXT_CREW_DESERTION_FRACTION);
        crewCargo.removeCrew(deserters);

        List<String> lines = new ArrayList<>();
        if (tab > 0f) lines.add("Your bar tab came to " + Misc.getDGSCredits(tab) + ".");
        if (deserters > 0) lines.add(deserters + " of your crew deserted.");

        if (market != null && !market.isPlayerOwned() && !market.getFactionId().equals(Factions.PLAYER)) {
            CoreReputationPlugin.CustomRepImpact impact = new CoreReputationPlugin.CustomRepImpact();
            impact.delta = -EXT_REPUTATION_LOSS;
            Global.getSector().adjustPlayerReputation(new CoreReputationPlugin.RepActionEnvelope(CoreReputationPlugin.RepActions.CUSTOM, impact, null, null, true), market.getFactionId());
            lines.add("The locals at " + market.getName() + " will remember the night.");
        }

        return String.join(" ", lines);
    }

    protected void movePlayerFleet() {
        placePlayerFleet(pickDestinationSystem(Float.MAX_VALUE, null), true);
    }

    private void placePlayerFleet(StarSystemAPI system, boolean restoreCR) {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        SectorEntityToken jp = Misc.getDistressJumpPoint(system);

        fleet.getContainingLocation().removeEntity(fleet);
        fleet.setContainingLocation(system);
        system.addEntity(fleet);
        fleet.setLocation(jp.getLocation().x + 300, jp.getLocation().y + 300);
        Global.getSector().setCurrentLocation(fleet.getContainingLocation());

        Global.getSector().setLastPlayerBattleTimestamp(Long.MIN_VALUE);
        Global.getSector().setLastPlayerBattleWon(false);

        fleet.getFleetData().setSyncNeeded();
        fleet.getFleetData().syncIfNeeded();

        if (restoreCR) {
            for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
                float max = member.getRepairTracker().getMaxCR();
                member.getRepairTracker().setCR(max);
            }
        }

        if (fleet.getAbility(Abilities.TRANSPONDER) != null) {
            fleet.getAbility(Abilities.TRANSPONDER).activate();
        }

        Global.getSector().getCampaignUI().resetViewOffset();
        Misc.clearAreaAroundPlayer(2000f);
    }

    private static StarSystemAPI pickDestinationSystem(float maxLY, LocationAPI exclude) {
        Vector2f from = Global.getSector().getPlayerFleet().getLocationInHyperspace();
        WeightedRandomPicker<StarSystemAPI> safe = new WeightedRandomPicker<>();
        WeightedRandomPicker<StarSystemAPI> any = new WeightedRandomPicker<>();

        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            if (system == exclude || Misc.getDistressJumpPoint(system) == null) continue;
            if (Misc.getDistanceLY(from, system.getLocation()) > maxLY) continue;
            any.add(system);

            if (system.hasTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER) || system.hasPulsar() || system.hasBlackHole()) continue;
            if (system.hasTag(Tags.THEME_HIDDEN) || system.hasTag(Tags.TEMPORARY_LOCATION)) continue;
            safe.add(system);
        }

        return safe.isEmpty() ? any.pick() : safe.pick();
    }

    public static CampaignFleetAPI transferPlayerFleetToEmpty() {
        //AI mode is false, this leaves fuel and supply use enabled
        CampaignFleetAPI newFleet = Global.getFactory().createEmptyFleet(Global.getSector().getPlayerFaction().getId(), "Hungover Fleet", true);
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();

        FleetDataAPI playerFleetData = playerFleet.getFleetData();
        FleetDataAPI newFleetData = newFleet.getFleetData();

        //add all the members and move officers
        for (FleetMemberAPI m : playerFleetData.getMembersListCopy()) {
            if(m.getCaptain() != null && m.getCaptain().isPlayer()) continue;

            //set all officers null, they are coming with us on an adventure
            if(m.getCaptain() != null && !m.getCaptain().isAICore() && !Misc.isUnremovable(m.getCaptain())) {
                m.setCaptain(null);
            }

            playerFleetData.removeFleetMember(m);
            newFleetData.addFleetMember(m);
        }

        //set memory stuff
        MemoryAPI newFleetMemory = newFleet.getMemoryWithoutUpdate();
        newFleetMemory.set(MemFlags.MEMORY_KEY_MAKE_NON_HOSTILE, true);
        newFleetMemory.set(MemFlags.MEMORY_KEY_NEVER_AVOID_PLAYER_SLOWLY, true);
        newFleetMemory.set(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS, true);
        newFleetMemory.set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);
        newFleetMemory.set(MemFlags.ENTITY_MISSION_IMPORTANT, true);

        newFleet.setNoAutoDespawn(true);
        newFleet.setNoFactionInName(true);
        newFleet.setTransponderOn(false);
        newFleet.setAI(null);
        newFleet.setNullAIActionText("Sobering up");

        Global.getSector().getCurrentLocation().spawnFleet(playerFleet, 20f, 20f, newFleet);

        newFleet.getCargo().addAll(playerFleet.getCargo());
        playerFleet.getCargo().clear();

        SectorEntityToken focus = getOrbitFocusAtTokenPosition(newFleet);
        if(focus.getOrbit() != null) newFleet.setOrbit(focus.getOrbit().makeCopy());
        newFleet.setVelocity(0, 0);

        newFleet.addScript(new FleetMergeScript(newFleet));

        newFleet.setDiscoverable(true);
        newFleet.getDetectedRangeMod().modifyFlat("gen", 5000f);

        return newFleet;
    }

    public static SectorEntityToken getOrbitFocusAtTokenPosition(SectorEntityToken token) {
        LocationAPI location = token.getContainingLocation();

        SectorEntityToken orbitFocus = token.getContainingLocation().createToken(token.getLocation());
        orbitFocus.setLocation(token.getLocation().x, token.getLocation().y);

        if (location.isNebula() || location.isHyperspace()) return orbitFocus;
        StarSystemAPI system = token.getStarSystem();

        for (SectorEntityToken systemEntity : system.getAllEntities()) {
            if (!systemEntity.isStar() && systemEntity instanceof PlanetAPI || systemEntity instanceof JumpPointAPI) {
                //check if there is a planet or JP closer than max dist
                //if there is, orbit it
                float dist = Misc.getDistance(token, systemEntity);
                boolean isInOrbitRange = dist < (systemEntity.getRadius() + MAXIMUM_DISTANCE_TO_NON_CENTERED_ENTITY);

                if (isInOrbitRange) {
                    float orbitDistance = dist < systemEntity.getRadius() ? systemEntity.getRadius() + 100f : dist;

                    float orbitPeriod = systemEntity.getOrbit() != null ? systemEntity.getOrbit().getOrbitalPeriod() : 31f;
                    OrbitAPI orbit = Global.getFactory().createCircularOrbit(systemEntity, 0f, orbitDistance, orbitPeriod);
                    orbitFocus.setOrbit(orbit);
                    return orbitFocus;
                }
            }
        }

        //if there is not, get the orbit duration of the closest entity orbiting center or sun and use that instead to orbit the center
        SectorEntityToken closestEntity = getClosestEntityWithCenteredOrbit(token);

        float dist = Misc.getDistance(token, system.getCenter());
        // deep space systems have no star
        float starRadius = system.getStar() != null ? system.getStar().getRadius() : 0f;
        float orbitDistance = dist < starRadius ? starRadius + 700f : dist;
        float angle = Misc.getAngleInDegrees(system.getCenter().getLocation(), token.getLocation());
        float orbitPeriod = closestEntity != null ? closestEntity.getCircularOrbitPeriod() : 31f;

        orbitFocus.setCircularOrbit(system.getCenter(), angle, orbitDistance, orbitPeriod);

        return orbitFocus;
    }

    public static SectorEntityToken getClosestEntityWithCenteredOrbit(SectorEntityToken toToken) {
        StarSystemAPI system = toToken.getStarSystem();
        SectorEntityToken closestEntity = null;
        float minDist = Float.MAX_VALUE;

        for (SectorEntityToken entity : system.getAllEntities()) {
            if (entity.getOrbit() != null && entity.getOrbitFocus() != null) {
                String id = entity.getOrbitFocus().getId();
                boolean orbitFocusIsStarOrSystemCenter = id.equals(system.getCenter().getId()) || (system.getStar() != null && id.equals(system.getStar().getId()));

                if (!orbitFocusIsStarOrSystemCenter) continue;

                float dist = Misc.getDistance(toToken, entity);
                if (dist < minDist) {
                    closestEntity = entity;
                    minDist = dist;
                }
            }
        }

        return closestEntity;
    }

}
