package livecheck;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.memory.AddictionMemory;
import com.fs.starfarer.api.alcoholism.memory.AlcoholRepo;
import com.fs.starfarer.api.alcoholism.memory.CustomAlcohol;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;

import java.util.ArrayList;
import java.util.List;

public final class CustomChecks {
    private CustomChecks() {
    }

    private static CustomAlcohol alcohol(Ctx c) {
        return (CustomAlcohol) AlcoholRepo.get((String) c.get("custom"));
    }

    private static SpecialItemData item(Ctx c) {
        return new SpecialItemData(CustomAlcohol.CUSTOM_ALCOHOL_ITEM_ID, (String) c.get("custom"));
    }

    private static float inCargo(Ctx c) {
        return Global.getSector().getPlayerFleet().getCargo().getQuantity(CargoAPI.CargoItemType.SPECIAL, item(c));
    }

    public static List<Step> steps() {
        List<Step> steps = new ArrayList<>();
        steps.add(c -> {
            List<CustomAlcohol> custom = AlcoholRepo.getCustomAlcoholList();
            c.expect(!custom.isEmpty(), "new game has the test custom alcohol");
            if (custom.isEmpty()) return;
            CustomAlcohol a = custom.get(0);
            c.put("custom", a.getId());
            c.expect(a.getName() != null && !a.getName().isEmpty(), "custom alcohol name " + a.getName());
            c.expect(AddictionMemory.getInstanceOrRegister().contains(a), "custom alcohol has addiction status");
            Global.getSector().getPlayerFleet().getCargo().addSpecial(item(c), 100f);
            c.put("customStart", inCargo(c));
            a.getAddictionStatus().setConsuming(true);
            c.days(4f);
        });
        steps.add(c -> {
            if (c.get("custom") == null) return;
            c.expect(inCargo(c) < (Float) c.get("customStart"), "custom alcohol consumed, " + inCargo(c) + " left");
            c.expect(alcohol(c).getAddictionStatus().isAddicted(), "custom alcohol addiction builds");
            c.reload();
        });
        steps.add(c -> {
            if (c.get("custom") == null) return;
            c.expect(alcohol(c) != null, "custom alcohol survives save and load");
            alcohol(c).getAddictionStatus().setConsuming(false);
        });
        return steps;
    }
}
