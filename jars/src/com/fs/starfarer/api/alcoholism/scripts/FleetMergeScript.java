package com.fs.starfarer.api.alcoholism.scripts;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.ModPlugin;
import com.fs.starfarer.api.alcoholism.intel.DrunkFleetIntel;
import com.fs.starfarer.api.campaign.CampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.Misc;

public class FleetMergeScript implements EveryFrameScript {

    private final CampaignFleetAPI fleet;
    private boolean isDone = false;

    public FleetMergeScript(CampaignFleetAPI fleet){
        this.fleet = fleet;
    }

    @Override
    public boolean isDone() {
        return isDone;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {
        if(isDone) return;
        if(Misc.getDistance(Global.getSector().getPlayerFleet(), fleet) < 100f) {
            Global.getSector().getMemoryWithoutUpdate().unset(DrunkFleetIntel.MEMORY_KEY_PRE + fleet.getId());
            mergeFleetWithPlayerFleet(fleet);
        }
    }

    public void mergeFleetWithPlayerFleet(CampaignFleetAPI otherFleet) {
        ModPlugin.log("transferring fleet");

        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();

        FleetDataAPI playerFleetData = playerFleet.getFleetData();
        FleetDataAPI otherFleetData = otherFleet.getFleetData();

        //add all the members and move officers
        for (FleetMemberAPI m : otherFleetData.getMembersListCopy()) {
            ModPlugin.log("moving " + m.getVariant().getFullDesignationWithHullName());
            //fleet Member transaction
            playerFleetData.addFleetMember(m);
            otherFleetData.removeFleetMember(m);
        }

        playerFleet.getCargo().addAll(otherFleet.getCargo());
        Global.getSector().reportFleetDespawned(otherFleet, CampaignEventListener.FleetDespawnReason.REACHED_DESTINATION, playerFleet);
        otherFleet.getContainingLocation().removeEntity(otherFleet);

        isDone = true;
    }
}
