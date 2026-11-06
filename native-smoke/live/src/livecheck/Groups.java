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
                case "fleet":
                    all.addAll(FleetChecks.steps());
                    break;
                case "drinks":
                    all.addAll(DrinkChecks.steps());
                    break;
                case "market":
                    all.addAll(MarketChecks.steps());
                    break;
                case "codex":
                    all.addAll(CodexChecks.steps());
                    break;
                case "blackout":
                    all.addAll(BlackoutChecks.steps());
                    break;
                case "battle_drinking":
                    all.addAll(DrinkBattle.steps(false));
                    break;
                case "battle_withdrawal":
                    all.addAll(DrinkBattle.steps(true));
                    break;
                default:
                    throw new IllegalArgumentException("Unknown group " + one);
            }
        }
        return all;
    }
}
