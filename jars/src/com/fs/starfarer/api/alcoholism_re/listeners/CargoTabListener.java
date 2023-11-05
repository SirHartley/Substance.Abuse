package com.fs.starfarer.api.alcoholism_re.listeners;

import com.fs.starfarer.api.campaign.CargoAPI;

public interface CargoTabListener {
    public void reportCargoOpened(CargoAPI cargo);
    public void reportCargoClosed(CargoAPI cargo);
}
