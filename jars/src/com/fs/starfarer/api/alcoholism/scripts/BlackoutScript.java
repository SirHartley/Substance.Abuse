package com.fs.starfarer.api.alcoholism.scripts;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.ModPlugin;
import com.fs.starfarer.api.alcoholism.intel.DrunkFleetIntel;
import com.fs.starfarer.api.alcoholism.memory.AlcoholAPI;
import com.fs.starfarer.api.alcoholism.memory.AlcoholRepo;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import com.fs.starfarer.campaign.CampaignEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BlackoutScript implements EveryFrameScript {

    public static final float MAXIMUM_DISTANCE_TO_NON_CENTERED_ENTITY = 400f;

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
    public static final String ITEM_DROP_KEY = "$Alcohol_hasDroppedItem";

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

        if(Global.getSector().getPlayerFleet().getFleetData().getNumMembers() > 1){
            CampaignFleetAPI fleet = transferPlayerFleetToEmpty();
            addCargoToPlayerFleet();
            Global.getSector().getIntelManager().addIntel(new DrunkFleetIntel(fleet));
            Global.getSector().getMemoryWithoutUpdate().set(DrunkFleetIntel.MEMORY_KEY_PRE + fleet.getId(), true);
        }

        movePlayerFleet();
        stopAllConsumption();

        Global.getSector().getCampaignUI().showMessageDialog(QUIP_LIST.get(new Random().nextInt(QUIP_LIST.size()-1))
                + "\n\nThis situation appears to be a result of consuming too much alcohol at once. Check your intel entries for more info.");
        Global.getSector().setPaused(true);

        done = true;
    }

    private void stopAllConsumption(){
        for (AlcoholAPI baseAlcohol : AlcoholRepo.getAllAlcohol()){
            baseAlcohol.getAddictionStatus().setConsuming(false);
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

        MemoryAPI mem = Global.getSector().getMemoryWithoutUpdate();
        List<AlcoholAPI> alcoholList = new ArrayList<>(AlcoholRepo.getIndustrialAlcoholList());
        WeightedRandomPicker<AlcoholAPI> picker = new WeightedRandomPicker<>(true);
        picker.addAll(alcoholList);

        Random random = new Random();

        if(!mem.getBoolean(ITEM_DROP_KEY)){
            AlcoholAPI alcohol = picker.pick();
            cargo.addSpecial(new SpecialItemData(alcohol.getIndustryItemId(),null), 1);
            mem.set(ITEM_DROP_KEY, true);
        }

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

    protected void movePlayerFleet() {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();

        for (CampaignFleetAPI other : Global.getSector().getCurrentLocation().getFleets()) {
            MemoryAPI mem = other.getMemoryWithoutUpdate();
            if (mem.getBoolean(MemFlags.MEMORY_KEY_MAKE_HOSTILE_WHILE_TOFF)) {
                mem.removeAllRequired(MemFlags.MEMORY_KEY_MAKE_HOSTILE_WHILE_TOFF);
                //System.out.println("Hostile: " + mem.getBoolean(MemFlags.MEMORY_KEY_MAKE_HOSTILE_WHILE_TOFF));
            }
            mem.unset(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_OFF);

            if (!Misc.isPermaKnowsWhoPlayerIs(other)) {
                mem.unset(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON);
            }
        }

        WeightedRandomPicker<StarSystemAPI> systemPicker = new WeightedRandomPicker<>();
        systemPicker.addAll(Global.getSector().getStarSystems());

        StarSystemAPI system = systemPicker.pickAndRemove();
        while (Misc.getDistressJumpPoint(system) == null){
            system = systemPicker.pickAndRemove();
        }

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

        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            float max = member.getRepairTracker().getMaxCR();
            member.getRepairTracker().setCR(max);
        }

        if (fleet.getAbility(Abilities.TRANSPONDER) != null) {
            fleet.getAbility(Abilities.TRANSPONDER).activate();
        }

        Global.getSector().getCampaignUI().resetViewOffset();
        Misc.clearAreaAroundPlayer(2000f);
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

                    OrbitAPI orbit = Global.getFactory().createCircularOrbit(systemEntity, 0f, orbitDistance, systemEntity.getOrbit().getOrbitalPeriod());
                    orbitFocus.setOrbit(orbit);
                    return orbitFocus;
                }
            }
        }

        //if there is not, get the orbit duration of the closest entity orbiting center or sun and use that instead to orbit the center
        SectorEntityToken closestEntity = getClosestEntityWithCenteredOrbit(token);

        float dist = Misc.getDistance(token, system.getCenter());
        float orbitDistance = dist < system.getStar().getRadius() ? system.getStar().getRadius() + 700f : dist;
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
                boolean orbitFocusIsStarOrSystemCenter = id.equals(system.getCenter().getId()) || id.equals(system.getStar().getId());

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
