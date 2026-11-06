package livecheck;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;

// reads the player cargo the way some skills and hullmods do, a pending fleet sync then runs for each ship
public class CargoProbe extends BaseHullMod {
    public static final String ID = "live_check_cargo_probe";
    public static int calls = 0;

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        calls++;
        Global.getSector().getPlayerFleet().getCargo();
    }
}
