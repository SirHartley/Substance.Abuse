package livecheck;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.hullmods.campaignEffects.ExcessOPStripper;
import com.fs.starfarer.api.alcoholism.hullmods.campaignEffects.TariffModifier;
import com.fs.starfarer.api.alcoholism.memory.AlcoholRepo;
import com.fs.starfarer.api.campaign.CampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.RefitScreenListener;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;

import java.util.ArrayList;
import java.util.List;

// changelog rows 4, 5, 6, 8, 11, 12
public final class DrinkChecks {
    private static final String STOCK_MODULE = "module_onslaught_armor_left_Standard";

    private DrinkChecks() {
    }

    private static MarketAPI npcMarket() {
        return Global.getSector().getEconomy().getMarket("jangala");
    }

    private static float tariffMult(MarketAPI market, String id) {
        MutableStat.StatMod mod = market.getTariff().getMultStatMod(id);
        return mod == null ? 1f : mod.getValue();
    }

    private static boolean tariffListener() {
        for (CampaignEventListener l : Global.getSector().getAllListeners()) if (l instanceof TariffModifier) return true;
        return false;
    }

    private static boolean carriesDrink(ShipVariantAPI v) {
        for (String mod : v.getHullMods()) if (AlcoholRepo.ALCOHOL_MAP.containsKey(mod)) return true;
        return false;
    }

    private static int moduleVents(ShipVariantAPI v) {
        int vents = 0;
        for (String slot : v.getModuleSlots()) vents += v.getModuleVariant(slot).getNumFluxVents();
        return vents;
    }

    private static void row4(List<Step> steps) {
        steps.add(c -> {
            Kit.reset();
            Global.getSector().getMemoryWithoutUpdate().set(ExcessOPStripper.HAS_SHOWN_WARNING_KEY, true);
            c.days(1.2f);
        });
        steps.add(c -> {
            FleetMemberAPI a = Kit.ship("hammerhead_Balanced");
            c.put("r4a", a.getId());
            Kit.consume(AlcoholRepo.ABSYNTH, 0.5f, 500f);
            c.days(1.2f);
        });
        steps.add(c -> {
            FleetMemberAPI a = Kit.member((String) c.get("r4a"));
            c.expect(a.getVariant().hasHullMod(AlcoholRepo.ABSYNTH), "row4 ship A carries the absynth hullmod");
            for (RefitScreenListener l : Global.getSector().getListenerManager().getListeners(RefitScreenListener.class)) {
                l.reportFleetMemberVariantSaved(a, null);
            }
            FleetMemberAPI b = Kit.ship("hammerhead_Balanced");
            c.put("r4b", b.getId());
            Kit.overOp(a);
            Kit.overOp(b);
            c.put("r4bVents", b.getVariant().getNumFluxVents());
            c.expect(Kit.unusedOp(a) < 0 && Kit.unusedOp(b) < 0, "row4 ships A and B are over the op limit");
            float left = Kit.cargo().getCommodityQuantity(Kit.drink(AlcoholRepo.ABSYNTH).getCommodityId());
            Kit.cargo().removeCommodity(Kit.drink(AlcoholRepo.ABSYNTH).getCommodityId(), left);
            c.days(10f);
        });
        steps.add(c -> {
            c.expect(Kit.drink(AlcoholRepo.ABSYNTH).getAddictionStatus().isWithdrawal(), "row4 absynth in withdrawal");
            c.expect(Kit.unusedOp(Kit.member((String) c.get("r4a"))) >= 0, "row4 ship A stripped to its op limit");
            c.expect(Kit.member((String) c.get("r4b")).getVariant().getNumFluxVents() == (Integer) c.get("r4bVents"), "row4 ship B never refit on absynth keeps its vents");
            Kit.reset();
            FleetMemberAPI s = Kit.ship("hammerhead_Balanced");
            c.put("r4c", s.getId());
            Kit.overOp(s);
            c.put("r4cVents", s.getVariant().getNumFluxVents());
            c.days(2.2f);
        });
        steps.add(c -> {
            c.expect(Kit.member((String) c.get("r4c")).getVariant().getNumFluxVents() == (Integer) c.get("r4cVents"), "row4 ship C keeps its vents without absynth");
            for (String key : new String[]{"r4a", "r4b", "r4c"}) Kit.fleet().getFleetData().removeFleetMember(Kit.member((String) c.get(key)));
        });
    }

    private static void row5(List<Step> steps) {
        steps.add(c -> {
            Kit.reset();
            FleetMemberAPI mine = Kit.ship("onslaught_mk1_Ancient");
            c.put("r5", mine.getId());
            CampaignFleetAPI npc = Global.getFactory().createEmptyFleet(Factions.HEGEMONY, "Live check", true);
            FleetMemberAPI theirs = Kit.ship(npc, "onslaught_mk1_Ancient");
            Kit.fleet().getContainingLocation().addEntity(npc);
            npc.setLocation(Kit.fleet().getLocation().x + 800f, Kit.fleet().getLocation().y);
            c.put("r5npc", theirs.getVariant().clone());
            c.put("r5vents", moduleVents(theirs.getVariant()));
            c.put("r5stock", Global.getSettings().getVariant(STOCK_MODULE).getNumFluxVents());
            Kit.consume(AlcoholRepo.STOUT, 0.2f, 500f);
            Kit.manager().onNewDay();
            Kit.manager().onNewDay();
            ShipVariantAPI v = mine.getVariant();
            c.expect(!v.getModuleSlots().isEmpty(), "row5 onslaught has modules");
            boolean own = true;
            boolean hullmod = true;
            for (String slot : v.getModuleSlots()) {
                hullmod &= v.getModuleVariant(slot).hasHullMod(AlcoholRepo.STOUT);
                own &= !v.getModuleVariant(slot).isStockVariant();
            }
            c.expect(hullmod, "row5 player modules carry the stout hullmod");
            c.expect(own, "row5 player modules are not stock variants");

            String slot = v.getModuleSlots().get(0);
            ShipVariantAPI module = v.getModuleVariant(slot);
            module.setNumFluxVents(module.getNumFluxVents() + module.getUnusedOP(Global.getSector().getPlayerPerson().getStats()) + 10);
            new ExcessOPStripper().correctMember(mine, Global.getSector().getPlayerPerson().getStats(), false);

            ShipVariantAPI other = theirs.getVariant();
            boolean clean = true;
            for (String s : other.getModuleSlots()) clean &= !carriesDrink(other.getModuleVariant(s));
            c.expect(clean, "row5 npc modules carry no drink hullmod");
            c.expect(!carriesDrink(Global.getSettings().getVariant(STOCK_MODULE)), "row5 stock module variant carries no drink hullmod");
            c.expect(moduleVents(other) == (Integer) c.get("r5vents"), "row5 npc modules keep their loadout");
            c.expect(Global.getSettings().getVariant(STOCK_MODULE).getNumFluxVents() == (Integer) c.get("r5stock"), "row5 stock module variant keeps its loadout");

            c.data().remove("live_check_r5npc");
            npc.getContainingLocation().removeEntity(npc);
            Kit.fleet().getFleetData().removeFleetMember(mine);
            Kit.reset();
        });
    }

    private static void row6(List<Step> steps) {
        steps.add(c -> {
            Kit.consume(AlcoholRepo.FREEDOM, 1f, 500f);
            c.days(1.2f);
        });
        steps.add(c -> {
            MarketAPI market = npcMarket();
            Global.getSector().reportPlayerOpenedMarket(market);
            c.near(tariffMult(market, AlcoholRepo.FREEDOM), 0.8f, 0.02f, "row6 sweet freedom tariff while open");
            Global.getSector().reportPlayerClosedMarket(market);
            c.near(tariffMult(market, AlcoholRepo.FREEDOM), 1f, 0.001f, "row6 tariff mod gone after close");
            Kit.reset();
        });
    }

    private static void row8(List<Step> steps) {
        steps.add(c -> {
            Kit.consume(AlcoholRepo.BLOOD, 1f, 500f);
            c.days(1.2f);
        });
        steps.add(c -> {
            FleetMemberAPI m = Kit.fleet().getFleetData().getMembersListCopy().get(0);
            Kit.fleet().getFleetData().setSyncNeeded();
            Kit.fleet().getFleetData().syncIfNeeded();
            MutableStat stat = m.getStats().getZeroFluxMinimumFluxLevel();
            MutableStat.StatMod mod = stat.getFlatStatMod(AlcoholRepo.BLOOD);
            c.expect(mod != null, "row8 zero flux mod present");
            if (mod != null) c.near(mod.getValue(), 0.3f, 0.02f, "row8 zero flux mod value");
            c.expect(stat.getModifiedValue() <= 0.31f, "row8 modified zero flux level " + stat.getModifiedValue() + " at most 0.31");
            Kit.reset();
        });
    }

    private static void row11(List<Step> steps) {
        steps.add(c -> {
            FleetMemberAPI m = Kit.ship("wolf_CS");
            c.put("r11", m.getId());
            m.setCaptain(Kit.officer().getPerson());
            Kit.consume(AlcoholRepo.TEARS, 1f, 500f);
            c.days(1.2f);
        });
        steps.add(c -> {
            c.expect(Kit.xpListener() != null, "row11 xp listener registered");
            c.near(Kit.battleXp(1000L), 1300f, 1f, "row11 tears xp in the same battle");
            c.reload();
        });
        steps.add(c -> c.frames(60));
        steps.add(c -> {
            c.expect(Kit.xpListener() != null, "row11 xp listener registered after load");
            c.near(Kit.battleXp(1000L), 1300f, 1f, "row11 tears xp right after load");
        });
    }

    private static void row12(List<Step> steps) {
        steps.add(c -> {
            Kit.reset();
            Kit.addiction(AlcoholRepo.FREEDOM, 1f);
            Kit.addiction(AlcoholRepo.TEARS, 1f);
            Kit.manager().onNewDay();
            Kit.withdraw(AlcoholRepo.FREEDOM, 1f);
            Kit.withdraw(AlcoholRepo.TEARS, 1f);
            c.reload();
        });
        steps.add(c -> c.frames(60));
        steps.add(c -> {
            c.expect(tariffListener(), "row12 tariff listener registered after load");
            c.expect(Kit.xpListener() != null, "row12 xp listener registered after load");
            MarketAPI market = npcMarket();
            Global.getSector().reportPlayerOpenedMarket(market);
            c.near(tariffMult(market, AlcoholRepo.FREEDOM), 1.2f, 0.02f, "row12 withdrawal tariff after load");
            Global.getSector().reportPlayerClosedMarket(market);
            if (Kit.xpListener() != null) c.near(Kit.battleXp(1000L), 800f, 1f, "row12 withdrawal xp after load");
            Kit.fleet().getFleetData().removeFleetMember(Kit.member((String) c.get("r11")));
            Kit.reset();
        });
    }

    public static List<Step> steps() {
        List<Step> steps = new ArrayList<>();
        row4(steps);
        row5(steps);
        row6(steps);
        row8(steps);
        row11(steps);
        row12(steps);
        return steps;
    }
}
