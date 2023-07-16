package com.fs.starfarer.api.alcoholism.scripts;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.ModPlugin;
import com.fs.starfarer.api.alcoholism.listeners.CargoTabListener;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CoreUITabId;

import java.util.List;

public class CargoUIOpenChecker implements EveryFrameScript {

    boolean open = false;

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return true;
    }

    @Override
    public void advance(float amount) {
        boolean isOpen = CoreUITabId.CARGO.equals(Global.getSector().getCampaignUI().getCurrentCoreTab());
        boolean noInteraction = Global.getSector().getCampaignUI().getCurrentInteractionDialog() == null;

        if(noInteraction && isOpen && !open){
            ModPlugin.log("Player Cargo Open");

            List<CargoTabListener> list = Global.getSector().getListenerManager().getListeners(CargoTabListener.class);
            CargoAPI c = Global.getSector().getPlayerFleet().getCargo();

            for (CargoTabListener x : list) {
                x.reportCargoOpened(c);
            }

            open = true;
        } else if(noInteraction && !isOpen && open){
            ModPlugin.log("Player Cargo Closed");

            List<CargoTabListener> list = Global.getSector().getListenerManager().getListeners(CargoTabListener.class);
            CargoAPI c = Global.getSector().getPlayerFleet().getCargo();

            for (CargoTabListener x : list) {
                x.reportCargoClosed(c);
            }

            open = false;
        }
    }

    public static void register() {
        if(!Global.getSector().hasTransientScript(CargoUIOpenChecker.class)){
            ModPlugin.log("creating CargoUIOpenChecker instance");
            Global.getSector().addTransientScript(new CargoUIOpenChecker());
        }
    }
}
