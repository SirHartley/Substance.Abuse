package livecheck;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.TooltipHelper;
import com.fs.starfarer.api.alcoholism.memory.Alcohol;
import com.fs.starfarer.api.alcoholism.memory.AlcoholRepo;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import lunalib.backend.ui.settings.LunaSettingsLoader;
import lunalib.lunaSettings.LunaSettings;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

// changelog rows 1 to 3
public final class FleetChecks {
    private static final String LUNA_MOD = "alcoholism";
    private static final String WATER_KEY = "alcoholism_WATER_MULT";

    private FleetChecks() {
    }

    private static void water(float value) throws Exception {
        ((JSONObject) LunaSettingsLoader.getSettings().get(LUNA_MOD)).put(WATER_KEY, (double) value);
        LunaSettings.reportSettingsChanged(LUNA_MOD);
    }

    public static List<Step> steps() {
        List<Step> steps = new ArrayList<>();
        steps.add(c -> {
            Kit.reset();
            Alcohol a = Kit.drink(AlcoholRepo.STOUT);
            float forecast = TooltipHelper.getPredictedAmountRequiredForOneMonth(a.getId());
            int lasts = TooltipHelper.getAmountWillLastDays(a, 100f);
            c.note("row 2 forecast " + forecast + " units a month, 100 units last " + lasts + " days");
            Kit.consume(a.getId(), 0f, 100f);
            c.put("r2units", Kit.cargo().getCommodityQuantity(a.getCommodityId()));
            c.put("r2forecast", forecast);
            c.put("r2start", Global.getSector().getClock().getTimestamp());
            c.days(30f);
        });
        steps.add(c -> {
            Alcohol a = Kit.drink(AlcoholRepo.STOUT);
            float used = (Float) c.get("r2units") - Kit.cargo().getCommodityQuantity(a.getCommodityId());
            float forecast = (Float) c.get("r2forecast");
            float days = Global.getSector().getClock().getElapsedDaysSince((Long) c.get("r2start"));
            c.expect(Math.abs(used - forecast) <= 1f, "row2 units used " + used + " within 1 of forecast " + forecast);
            c.expect(used >= Math.floor(days) - 1f, "row2 at least 1 unit a day, " + used + " in " + days + " days");
            Kit.reset();
        });
        steps.add(c -> {
            List<FleetMemberAPI> ships = new ArrayList<>();
            for (int i = 0; i < 30; i++) {
                FleetMemberAPI m = Kit.ship("wolf_CS");
                m.getVariant().addPermaMod(CargoProbe.ID);
                ships.add(m);
            }
            Kit.fleet().getFleetData().setSyncNeeded();
            Kit.fleet().getFleetData().syncIfNeeded();
            int fleetSize = Kit.fleet().getFleetData().getNumMembers();

            Kit.manager().onNewDay();
            CargoProbe.calls = 0;
            Kit.manager().onNewDay();
            int quiet = CargoProbe.calls;

            Kit.consume(AlcoholRepo.STOUT, 0.2f, 500f);
            Kit.manager().onNewDay();
            CargoProbe.calls = 0;
            Kit.manager().onNewDay();
            int drinking = CargoProbe.calls;
            c.note("row 1 probe calls: " + quiet + " quiet day, " + drinking + " drinking day, " + fleetSize + " ships");
            c.expect(drinking > quiet, "row1 the drinking day refreshes stats");
            c.expect(drinking <= 3 * fleetSize, "row1 probe calls " + drinking + " at most 3 x " + fleetSize);

            for (FleetMemberAPI m : ships) Kit.fleet().getFleetData().removeFleetMember(m);
            Kit.reset();
            c.reload();
        });
        steps.add(c -> {
            Kit.addiction(AlcoholRepo.WATER, 1f);
            water(0.9f);
            c.near(Kit.manager().getWaterMult(), 0.9f, 0.001f, "row3 lunalib change reaches the mod after a load");
            water(0.5f);
            c.near(Kit.manager().getWaterMult(), 0.5f, 0.001f, "row3 lunalib value restored");
            Kit.reset();
        });
        return steps;
    }
}
