package com.fs.starfarer.api.alcoholism.listeners;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.ModPlugin;
import com.fs.starfarer.api.alcoholism.memory.AlcoholRepo;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.listeners.ListenerManagerAPI;

import java.util.ArrayList;
import java.util.List;

public class AlcoholStackReplacer implements CargoTabListener {


    //todo implement check if this alcohol was overridden with another version, and become "spoiled" if it is


    //transient
    public static void register() {
        ListenerManagerAPI manager = Global.getSector().getListenerManager();
        if(!manager.hasListenerOfClass(AlcoholStackReplacer.class)) {
            ModPlugin.log("creating AlcoholStackReplacer instance");
            manager.addListener(new AlcoholStackReplacer(), true);
        }
    }

    @Override
    public void reportCargoOpened(CargoAPI cargo) {
        convertAlcoholStacksToSpecialItem(cargo);
    }

    @Override
    public void reportCargoClosed(CargoAPI cargo) {
        convertAlcoholStacksToCommodity(cargo);
    }

    private void convertAlcoholStacksToCommodity(CargoAPI cargo){
        List<CargoStackAPI> stacksToSwitch = new ArrayList<>();

        for (CargoStackAPI s : cargo.getStacksCopy()){
            if(s.isSpecialStack()){
                String id = s.getSpecialDataIfSpecial().getId();

                if(AlcoholRepo.isAlcohol(id)){
                   stacksToSwitch.add(s);
                }
            }
        }

        for (CargoStackAPI s : stacksToSwitch){
            float amt = s.getSize();
            String id = s.getSpecialDataIfSpecial().getId();

            cargo.removeItems(CargoAPI.CargoItemType.SPECIAL, s.getSpecialDataIfSpecial(), amt);
            cargo.addCommodity(AlcoholRepo.convertID(id), amt);
        }
    }

    private void convertAlcoholStacksToSpecialItem(CargoAPI cargo){
        List<CargoStackAPI> stacksToSwitch = new ArrayList<>();

        for (CargoStackAPI s : cargo.getStacksCopy()){
            if(s.isCommodityStack()){
                String id = s.getCommodityId();

                if(AlcoholRepo.isAlcohol(id)){
                    stacksToSwitch.add(s);
                }
            }
        }

        for (CargoStackAPI s : stacksToSwitch){
            float amt = s.getSize();
            String id = s.getCommodityId();
            cargo.removeCommodity(id, amt);
            cargo.addSpecial(new SpecialItemData(AlcoholRepo.convertID(id), null), amt);
        }
    }
}
