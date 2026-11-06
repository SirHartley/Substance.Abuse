package livecheck;

import com.fs.starfarer.api.Global;

import java.util.ArrayList;
import java.util.List;

public final class Groups {
    private Groups() {
    }

    // comma separated groups run one after the other in one game, far faster than one boot per group
    public static List<Step> get(String group) {
        List<Step> all = new ArrayList<>();
        // long day skips otherwise run the fleet out of supplies, the accident report then covers the captures
        all.add(c -> Global.getSector().getPlayerFleet().getCargo().addSupplies(3000f));
        for (String one : group.split(",")) {
            switch (one) {
                case "load":
                    all.addAll(LoadChecks.steps());
                    break;
                case "addiction":
                    all.addAll(AddictionChecks.steps());
                    break;
                case "custom":
                    all.addAll(CustomChecks.steps());
                    break;
                case "economy":
                    all.addAll(EconomyChecks.steps());
                    break;
                case "blackout":
                    all.addAll(BlackoutChecks.steps());
                    break;
                default:
                    throw new IllegalArgumentException("Unknown group " + one);
            }
        }
        return all;
    }
}
